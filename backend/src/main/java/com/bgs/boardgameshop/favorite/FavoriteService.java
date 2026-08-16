package com.bgs.boardgameshop.favorite;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * L'utilisateur authentifié fourni par le controller (via @AuthenticationPrincipal)
 * provient d'une session Hibernate déjà fermée : on le recharge toujours par id
 * dans la transaction active avant de toucher sa collection lazy favoriteGames.
 */
@Service
public class FavoriteService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public FavoriteService(UserRepository userRepository, GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getFavorites(Long userId) {
        User user = loadUser(userId);
        return user.getFavoriteGames().stream()
                .map(GameResponse::fromEntity)
                .sorted(Comparator.comparing(GameResponse::name))
                .toList();
    }

    @Transactional
    public void addFavorite(Long userId, Long gameId) {
        User user = loadUser(userId);
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        user.getFavoriteGames().add(game);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavorite(Long userId, Long gameId) {
        User user = loadUser(userId);
        user.getFavoriteGames().removeIf(game -> game.getId().equals(gameId));
        userRepository.save(user);
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + userId));
    }
}
