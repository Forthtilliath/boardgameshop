package com.bgs.boardgameshop.game;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameResponse> getGames(@RequestParam(required = false) String category) {
        return gameService.getGames(category);
    }

    @GetMapping("/{id}")
    public GameResponse getGame(@PathVariable Long id) {
        return gameService.getGame(id);
    }
}
