package com.bgs.boardgameshop.home;

import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.review.dto.ReviewResponse;

import java.util.List;

public record HomeResponse(
        List<GameResponse> newest,
        List<GameResponse> bestSellers,
        List<GameResponse> preorders,
        List<GameResponse> onSale,
        List<ReviewResponse> recentReviews
) {
}
