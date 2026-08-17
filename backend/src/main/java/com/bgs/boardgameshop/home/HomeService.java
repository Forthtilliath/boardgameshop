package com.bgs.boardgameshop.home;

import com.bgs.boardgameshop.game.GameFilter;
import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.game.GameService;
import com.bgs.boardgameshop.review.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class HomeService {

    private static final int SECTION_LIMIT = 4;

    private final GameService gameService;
    private final ReviewService reviewService;

    public HomeService(GameService gameService, ReviewService reviewService) {
        this.gameService = gameService;
        this.reviewService = reviewService;
    }

    @Transactional(readOnly = true)
    public HomeResponse getHome() {
        List<GameResponse> allGames = gameService.getGames(GameFilter.empty());

        List<GameResponse> newest = allGames.stream()
                .filter(g -> !g.preorder() && g.releaseDate() != null)
                .sorted(Comparator.comparing(GameResponse::releaseDate).reversed())
                .limit(SECTION_LIMIT)
                .toList();

        List<GameResponse> bestSellers = gameService.getGames(
                new GameFilter(null, null, null, null, null, null, null, "popularity", null)
        ).stream().limit(SECTION_LIMIT).toList();

        List<GameResponse> preorders = allGames.stream()
                .filter(GameResponse::preorder)
                .limit(SECTION_LIMIT)
                .toList();

        List<GameResponse> onSale = allGames.stream()
                .filter(GameResponse::onSale)
                .limit(SECTION_LIMIT)
                .toList();

        return new HomeResponse(newest, bestSellers, preorders, onSale, reviewService.getRecentReviews());
    }
}
