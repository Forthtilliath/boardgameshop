package com.bgs.boardgameshop.game;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Representation exposee par l'API pour un jeu du catalogue.
 */
public record GameResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        String imageUrl,
        String publisher,
        Integer minPlayers,
        Integer maxPlayers,
        Integer durationMinutes,
        Integer stock,
        Integer minAge,
        LocalDate releaseDate,
        Integer discountPercent,
        Instant discountEndsAt,
        BigDecimal finalPrice,
        boolean onSale,
        boolean preorder,
        List<TagResponse> tags,
        Double reviewsAverage,
        Integer reviewsCount,
        Long baseGameId,
        String baseGameName
) {

    public static GameResponse fromEntity(Game game) {
        Integer discountPercent = game.getDiscountPercent();
        Instant discountEndsAt = game.getDiscountEndsAt();
        boolean onSale = discountPercent != null
                && (discountEndsAt == null || discountEndsAt.isAfter(Instant.now()));

        BigDecimal finalPrice = onSale
                ? game.getPrice()
                        .multiply(BigDecimal.valueOf(100 - discountPercent))
                        .divide(BigDecimal.valueOf(100))
                : game.getPrice();

        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getPrice(),
                game.getCategory(),
                game.getImageUrl(),
                game.getPublisher(),
                game.getMinPlayers(),
                game.getMaxPlayers(),
                game.getDurationMinutes(),
                game.getStock(),
                game.getMinAge(),
                game.getReleaseDate(),
                discountPercent,
                discountEndsAt,
                finalPrice,
                onSale,
                game.getReleaseDate() != null && game.getReleaseDate().isAfter(LocalDate.now()),
                game.getTags().stream().map(TagResponse::fromEntity).toList(),
                game.getReviewsAverage(),
                game.getReviewsCount(),
                game.getBaseGame() != null ? game.getBaseGame().getId() : null,
                game.getBaseGame() != null ? game.getBaseGame().getName() : null
        );
    }
}
