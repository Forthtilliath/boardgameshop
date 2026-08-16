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
        Integer reviewsCount
) {

    public static GameResponse fromEntity(Game game) {
        boolean onSale = isDiscountActive(game);
        BigDecimal finalPrice = onSale
                ? game.getPrice()
                        .multiply(BigDecimal.valueOf(100 - game.getDiscountPercent()))
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
                game.getDiscountPercent(),
                game.getDiscountEndsAt(),
                finalPrice,
                onSale,
                game.getReleaseDate() != null && game.getReleaseDate().isAfter(LocalDate.now()),
                game.getTags().stream().map(TagResponse::fromEntity).toList(),
                game.getReviewsAverage(),
                game.getReviewsCount()
        );
    }

    private static boolean isDiscountActive(Game game) {
        if (game.getDiscountPercent() == null) {
            return false;
        }
        return game.getDiscountEndsAt() == null || game.getDiscountEndsAt().isAfter(Instant.now());
    }
}
