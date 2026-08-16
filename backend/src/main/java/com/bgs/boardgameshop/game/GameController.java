package com.bgs.boardgameshop.game;

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

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameResponse> getGames(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) Integer players,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) List<Long> tags,
            @RequestParam(required = false) String sort
    ) {
        GameFilter filter = new GameFilter(category, priceMin, priceMax, players, maxDuration, age, tags, sort);
        return gameService.getGames(filter);
    }

    @GetMapping("/{id}")
    public GameResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }
}
