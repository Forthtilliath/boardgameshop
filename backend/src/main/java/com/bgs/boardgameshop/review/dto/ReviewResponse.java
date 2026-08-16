package com.bgs.boardgameshop.review.dto;

import com.bgs.boardgameshop.review.Review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long gameId,
        String gameName,
        String userFirstName,
        Integer rating,
        String comment,
        Instant createdAt
) {

    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getGame().getId(),
                review.getGame().getName(),
                review.getUser().getFirstName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
