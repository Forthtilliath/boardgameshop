package com.bgs.boardgameshop.admin.dto;

import com.bgs.boardgameshop.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
