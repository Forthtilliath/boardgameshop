package com.bgs.boardgameshop.game;

import com.bgs.boardgameshop.common.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final int DEFAULT_PAGE_SIZE = 24;

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public PageResponse<GameResponse> getGames(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Integer players,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) List<Long> tags,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        GameFilter filter = new GameFilter(category, priceMin, priceMax, players, maxDuration, age, tags, sort, search);
        return PageResponse.of(gameService.getGames(filter, PageRequest.of(page, size)));
    }

    /** Catégories distinctes du catalogue complet, pour les chips de filtre (indépendant de la page courante). */
    @GetMapping("/categories")
    public List<String> getCategories() {
        return gameService.getCategories();
    }

    @GetMapping("/{id}")
    public GameResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }

    @GetMapping("/{id}/related")
    public List<GameResponse> getRelatedGames(@PathVariable Long id) {
        return gameService.getRelatedGames(id);
    }
}
