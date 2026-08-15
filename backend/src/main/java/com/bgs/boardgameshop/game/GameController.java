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

    private final GameRepository gameRepository;

    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public List<GameResponse> getGames(@RequestParam(required = false) String category) {
        List<Game> games = (category == null || category.isBlank())
                ? gameRepository.findAll()
                : gameRepository.findByCategoryIgnoreCase(category);

        return games.stream().map(GameResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public GameResponse getGame(@PathVariable Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        return GameResponse.fromEntity(game);
    }
}
