package com.bgs.boardgameshop.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Regroupé en quatre sections (ventes, catalogue, avis, utilisateurs) pour que
 * le dashboard admin reste lisible côté front malgré le nombre de métriques.
 */
public record AdminStatsResponse(
        SalesStats sales,
        CatalogStats catalog,
        ReviewStats reviews,
        UserStats users
) {

    public record SalesStats(
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
            List<TopSellingGame> topSellingGames,
            /** Répartition de TOUTES les commandes (tous statuts confondus) par statut. */
            List<StatusCount> ordersByStatus,
            List<CategoryRevenue> revenueByCategory,
            List<PromoCodeUsage> promoCodeUsage,
            BigDecimal totalDiscountGiven
    ) {
    }

    public record CatalogStats(
            BigDecimal totalStockValue,
            long outOfStockCount,
            long lowStockCount,
            List<LowStockGame> lowStockGames,
            long activePromotionsCount,
            List<TopFavoriteGame> topFavoriteGames,
            /** Jeux en rupture avec au moins une alerte de retour en stock active : signal de réassort prioritaire. */
            List<RestockDemandGame> restockDemand
    ) {
    }

    public record ReviewStats(
            Double averageRating,
            long totalReviews,
            /** Un point par note de 1 à 5, toujours les 5 présents même à zéro avis. */
            List<RatingBucket> ratingDistribution,
            List<TopRatedGame> topRatedGames,
            List<TopRatedGame> worstRatedGames,
            List<MostReviewedGame> mostReviewedGames
    ) {
    }

    public record UserStats(
            long totalUsers,
            List<DailySignupCount> signupsByDay,
            List<TopCustomer> topCustomers,
            /** Part des utilisateurs ayant au moins une commande payée, en pourcentage (0-100). */
            double conversionRate
    ) {
    }

    public record TopSellingGame(Long gameId, String gameName, long quantitySold, BigDecimal revenue) {
    }

    public record BestOrder(Long orderId, String customerName, BigDecimal totalAmount, Instant createdAt) {
    }

    public record DailyOrderCount(LocalDate date, long count) {
    }

    public record DailyRevenue(LocalDate date, BigDecimal revenue) {
    }

    public record StatusCount(String status, long count) {
    }

    public record CategoryRevenue(String category, BigDecimal revenue) {
    }

    public record PromoCodeUsage(String code, int usesCount, Integer maxUses, boolean active) {
    }

    public record LowStockGame(Long gameId, String gameName, int stock) {
    }

    public record TopFavoriteGame(Long gameId, String gameName, long favoriteCount) {
    }

    public record RestockDemandGame(Long gameId, String gameName, long alertCount) {
    }

    public record RatingBucket(int stars, long count) {
    }

    public record TopRatedGame(Long gameId, String gameName, double averageRating, int reviewsCount) {
    }

    public record MostReviewedGame(Long gameId, String gameName, int reviewsCount) {
    }

    public record DailySignupCount(LocalDate date, long count) {
    }

    public record TopCustomer(Long userId, String customerName, BigDecimal totalSpent, long orderCount) {
    }
}
