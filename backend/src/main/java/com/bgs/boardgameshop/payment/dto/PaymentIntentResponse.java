package com.bgs.boardgameshop.payment.dto;

public record PaymentIntentResponse(
        String clientSecret
) {
}
