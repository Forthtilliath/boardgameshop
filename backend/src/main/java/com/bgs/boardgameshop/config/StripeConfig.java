package com.bgs.boardgameshop.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Initialise le SDK Stripe avec la cle secrete (mode test), fournie via la
 * variable d'environnement STRIPE_SECRET_KEY (jamais commitee).
 */
@Component
public class StripeConfig {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
