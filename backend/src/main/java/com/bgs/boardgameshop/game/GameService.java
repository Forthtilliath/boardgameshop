package com.bgs.boardgameshop.game;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getGames(String category) {
        List<Game> games = (category == null || category.isBlank())
                ? gameRepository.findAll()
                : gameRepository.findByCategoryIgnoreCase(category);

        return games.stream().map(GameResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(Long id) {
        return GameResponse.fromEntity(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Game findEntity(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }
}
