package com.bgs.boardgameshop.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AdminStatsResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        BigDecimal averageOrderAmount,
        /** Commandes EN_ATTENTE_PAIEMENT : le panier n'existant que côté client (localStorage,
         *  jamais persisté en base), c'est le proxy le plus proche de "paniers en cours" qu'on
         *  puisse mesurer côté serveur — un checkout démarré mais pas encore payé. */
        long pendingPaymentOrders,
        BestOrder bestOrder,
        List<DailyOrderCount> ordersByDay,
        List<DailyRevenue> revenueByDay,
        List<TopSellingGame> topSellingGames
) {

    public record TopSellingGame(
            Long gameId,
            String gameName,
            long quantitySold,
            BigDecimal revenue
    ) {
    }

    public record BestOrder(
            Long orderId,
            String customerName,
            BigDecimal totalAmount,
            Instant createdAt
    ) {
    }

    public record DailyOrderCount(LocalDate date, long count) {
    }

    public record DailyRevenue(LocalDate date, BigDecimal revenue) {
    }
}
