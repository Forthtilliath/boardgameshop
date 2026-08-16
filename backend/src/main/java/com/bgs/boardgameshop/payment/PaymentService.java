package com.bgs.boardgameshop.payment;

import com.bgs.boardgameshop.order.Order;
import com.bgs.boardgameshop.order.OrderService;
import com.bgs.boardgameshop.order.OrderStatus;
import com.bgs.boardgameshop.payment.dto.PaymentIntentResponse;
import com.bgs.boardgameshop.user.User;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Crée (ou réutilise) un Payment Intent Stripe pour une commande en attente
     * de paiement, et retourne le client secret nécessaire à Stripe Elements
     * côté frontend.
     */
    public PaymentIntentResponse createPaymentIntent(Long orderId, User currentUser) throws StripeException {
        Order order = orderService.getOwnedOrder(orderId, currentUser);

        if (order.getStatus() != OrderStatus.EN_ATTENTE_PAIEMENT) {
            throw new OrderNotPayableException(order.getId(), order.getStatus().name());
        }

        long amountInCents = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        // Carte bancaire uniquement : pas de Klarna/Bancontact/etc, inutile pour
        // une petite boutique de jeux de société et ça simplifie le formulaire.
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .putMetadata("orderId", String.valueOf(order.getId()))
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        orderService.attachPaymentIntent(order, intent.getId());

        return new PaymentIntentResponse(intent.getClientSecret());
    }

    /**
     * Traite un événement webhook Stripe déjà vérifié (signature validée par
     * le controller). Les événements inconnus/non pertinents sont ignorés.
     */
    public void handleWebhookEvent(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> orderService.markAsPaid(paymentIntent.getId());
            case "payment_intent.payment_failed" -> orderService.markAsFailed(paymentIntent.getId());
            default -> { /* événement non géré, ignoré volontairement */ }
        }
    }
}
