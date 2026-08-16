package com.bgs.boardgameshop.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentIntentRequest(
        @NotNull Long orderId
) {
}
