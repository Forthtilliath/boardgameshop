package com.bgs.boardgameshop.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUser_IdAndGame_Id(Long userId, Long gameId);

    List<Review> findByGame_IdOrderByCreatedAtDesc(Long gameId);

    List<Review> findTop6ByOrderByCreatedAtDesc();
}
