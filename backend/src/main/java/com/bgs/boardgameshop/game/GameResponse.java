package com.bgs.boardgameshop.game;

import java.math.BigDecimal;

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
        Integer stock
) {

    public static GameResponse fromEntity(Game game) {
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
                game.getStock()
        );
    }
}
