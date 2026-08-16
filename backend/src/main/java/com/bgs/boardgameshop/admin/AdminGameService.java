package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminGameRequest;
import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.game.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminGameService {

    private final GameRepository gameRepository;
    private final TagService tagService;

    public AdminGameService(GameRepository gameRepository, TagService tagService) {
        this.gameRepository = gameRepository;
        this.tagService = tagService;
    }

    @Transactional
    public GameResponse createGame(AdminGameRequest request) {
        Game game = Game.builder().build();
        applyRequest(game, request);
        return GameResponse.fromEntity(gameRepository.save(game));
    }

    @Transactional
    public GameResponse updateGame(Long id, AdminGameRequest request) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
        applyRequest(game, request);
        return GameResponse.fromEntity(gameRepository.save(game));
    }

    @Transactional
    public void deleteGame(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new GameNotFoundException(id);
        }
        gameRepository.deleteById(id);
    }

    private void applyRequest(Game game, AdminGameRequest request) {
        game.setName(request.name());
        game.setDescription(request.description());
        game.setPrice(request.price());
        game.setCategory(request.category());
        game.setImageUrl(request.imageUrl());
        game.setPublisher(request.publisher());
        game.setMinPlayers(request.minPlayers());
        game.setMaxPlayers(request.maxPlayers());
        game.setDurationMinutes(request.durationMinutes());
        game.setStock(request.stock());
        game.setMinAge(request.minAge());
        game.setReleaseDate(request.releaseDate());
        game.setDiscountPercent(request.discountPercent());
        game.setDiscountEndsAt(request.discountEndsAt());
        game.setTags(tagService.findByIds(request.tagIds()));
    }
}
