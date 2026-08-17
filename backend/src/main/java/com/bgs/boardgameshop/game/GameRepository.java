package com.bgs.boardgameshop.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {

    @Query("SELECT DISTINCT g.category FROM Game g WHERE g.category IS NOT NULL ORDER BY g.category")
    List<String> findDistinctCategories();
}
