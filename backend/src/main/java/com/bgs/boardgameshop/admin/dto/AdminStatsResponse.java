package com.bgs.boardgameshop.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        List<TopSellingGame> topSellingGames
) {

    public record TopSellingGame(
            Long gameId,
            String gameName,
            long quantitySold,
            BigDecimal revenue
    ) {
    }
}
