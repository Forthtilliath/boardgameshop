package com.bgs.boardgameshop.admin.dto;

import com.bgs.boardgameshop.order.Order;
import com.bgs.boardgameshop.order.dto.OrderLineResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOrderResponse(
        Long id,
        Instant createdAt,
        String status,
        List<OrderLineResponse> lines,
        BigDecimal totalAmount,
        Long userId,
        String userEmail,
        String userFullName
) {

    public static AdminOrderResponse fromEntity(Order order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getStatus().name(),
                order.getLines().stream().map(OrderLineResponse::fromEntity).toList(),
                order.getTotalAmount(),
                order.getUser().getId(),
                order.getUser().getEmail(),
                order.getUser().getFirstName() + " " + order.getUser().getLastName()
        );
    }
}
