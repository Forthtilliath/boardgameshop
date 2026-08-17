package com.bgs.boardgameshop.stockalert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    Optional<StockAlert> findByUserIdAndGameId(Long userId, Long gameId);

    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    /** Jeux suivis par l'utilisateur qui sont de nouveau disponibles (voir StockAlert javadoc). */
    List<StockAlert> findByUserIdAndGame_StockGreaterThan(Long userId, Integer stock);
}
