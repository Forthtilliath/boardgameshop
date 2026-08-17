package com.bgs.boardgameshop.order.dto;

import com.bgs.boardgameshop.order.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Instant createdAt,
        String status,
        List<OrderLineResponse> lines,
        BigDecimal totalAmount,
        String promoCode,
        BigDecimal discountAmount
) {

    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCreatedAt(),
                order.getStatus().name(),
                order.getLines().stream().map(OrderLineResponse::fromEntity).toList(),
                order.getTotalAmount(),
                order.getPromoCode(),
                order.getDiscountAmount()
        );
    }
}
