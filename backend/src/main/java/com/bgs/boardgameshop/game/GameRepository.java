package com.bgs.boardgameshop.game;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {

    @Query("SELECT DISTINCT g.category FROM Game g WHERE g.category IS NOT NULL ORDER BY g.category")
    List<String> findDistinctCategories();

    /**
     * Verrou pessimiste (SELECT ... FOR UPDATE) tenu le temps de la transaction
     * courante : évite qu'une deuxième commande concurrente ne lise le même stock
     * avant que la première ait commité sa décrémentation (vente du même dernier
     * exemplaire à deux clients à la fois). Utilisé uniquement sur le chemin critique
     * décrémentation/restitution de stock (OrderService), pas sur les lectures publiques.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Game g WHERE g.id = :id")
    Optional<Game> findByIdForUpdate(Long id);
}
