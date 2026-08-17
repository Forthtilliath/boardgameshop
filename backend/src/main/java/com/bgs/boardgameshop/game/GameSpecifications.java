package com.bgs.boardgameshop.game;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construit dynamiquement la requête DB correspondant à un {@link GameFilter}.
 *
 * <p>Simplification assumée : le filtre de prix (et le tri associé, voir
 * {@code GameService#resolveSort}) porte sur le prix catalogue ({@code price}), pas sur
 * {@code finalPrice} (prix remisé "en direct"). La promo n'étant appliquée qu'à une
 * minorité de jeux en rotation, l'écart est mineur ; recalculer {@code finalPrice} en SQL
 * demanderait une expression CASE WHEN via l'API Criteria, jugée disproportionnée ici.
 */
final class GameSpecifications {

    private GameSpecifications() {
    }

    static Specification<Game> fromFilter(GameFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.category() != null && !filter.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.category().toLowerCase()));
            }
            if (filter.priceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.priceMin()));
            }
            if (filter.priceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.priceMax()));
            }
            if (filter.players() != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("minPlayers")),
                        cb.lessThanOrEqualTo(root.get("minPlayers"), filter.players())
                ));
                predicates.add(cb.or(
                        cb.isNull(root.get("maxPlayers")),
                        cb.greaterThanOrEqualTo(root.get("maxPlayers"), filter.players())
                ));
            }
            if (filter.maxDuration() != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("durationMinutes")),
                        cb.lessThanOrEqualTo(root.get("durationMinutes"), filter.maxDuration())
                ));
            }
            if (filter.age() != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("minAge")),
                        cb.lessThanOrEqualTo(root.get("minAge"), filter.age())
                ));
            }
            if (filter.tagIds() != null && !filter.tagIds().isEmpty()) {
                Join<Game, Tag> tagJoin = root.join("tags");
                predicates.add(tagJoin.get("id").in(filter.tagIds()));
                query.distinct(true);
            }
            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("publisher")), like)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
