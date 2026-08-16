package com.bgs.boardgameshop.game;

import java.math.BigDecimal;
import java.util.List;

/**
 * Filtres et tri optionnels du catalogue public. Tous les champs sont
 * nullables : un champ non renseigne n'applique aucune restriction.
 */
public record GameFilter(
        String category,
        BigDecimal priceMin,
        BigDecimal priceMax,
        Integer players,
        Integer maxDuration,
        Integer age,
        List<Long> tagIds,
        String sort
) {

    public static GameFilter empty() {
        return new GameFilter(null, null, null, null, null, null, null, null);
    }
}
