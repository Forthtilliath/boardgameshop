package com.bgs.boardgameshop.payment;

import com.bgs.boardgameshop.payment.dto.CreatePaymentIntentRequest;
import com.bgs.boardgameshop.payment.dto.PaymentIntentResponse;
import com.bgs.boardgameshop.security.SecurityUser;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-intent")
    public PaymentIntentResponse createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            @AuthenticationPrincipal SecurityUser principal
    ) throws StripeException {
        return paymentService.createPaymentIntent(request.orderId(), principal.getUser());
    }

    /**
     * Appelé directement par Stripe (pas par le frontend) : aucune authentification
     * JWT, la sécurité repose sur la vérification de la signature de la requête.
     * Le corps doit rester brut (pas de désincorporation Jackson automatique)
     * pour que cette vérification soit possible.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }

        paymentService.handleWebhookEvent(event);
        return ResponseEntity.ok().build();
    }
}
