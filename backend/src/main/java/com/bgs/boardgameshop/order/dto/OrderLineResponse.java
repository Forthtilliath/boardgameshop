package com.bgs.boardgameshop.order.dto;

import com.bgs.boardgameshop.order.OrderLine;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long gameId,
        String gameName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {

    public static OrderLineResponse fromEntity(OrderLine line) {
        return new OrderLineResponse(
                line.getGameId(),
                line.getGameName(),
                line.getUnitPrice(),
                line.getQuantity(),
                line.getLineTotal()
        );
    }
}
