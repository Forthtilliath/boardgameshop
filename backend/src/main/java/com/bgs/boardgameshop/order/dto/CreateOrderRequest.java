package com.bgs.boardgameshop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Le panier ne peut pas être vide")
        @Valid
        List<OrderItemRequest> items
) {
}
