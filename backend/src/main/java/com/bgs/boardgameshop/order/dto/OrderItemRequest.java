package com.bgs.boardgameshop.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long gameId,
        @Min(1) int quantity
) {
}
