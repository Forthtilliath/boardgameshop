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
import java.util.UUID;

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

    /** Renvoie le jeton de partage de l'utilisateur, en le générant s'il n'existe pas encore. */
    @Transactional
    public String getOrCreateShareToken(Long userId) {
        User user = loadUser(userId);
        if (user.getShareToken() == null) {
            user.setShareToken(UUID.randomUUID().toString());
            userRepository.save(user);
        }
        return user.getShareToken();
    }

    /** Favoris consultables publiquement via un lien de partage (lecture seule, sans authentification). */
    @Transactional(readOnly = true)
    public List<GameResponse> getFavoritesByShareToken(String token) {
        User user = userRepository.findByShareToken(token)
                .orElseThrow(() -> new ShareLinkNotFoundException(token));
        return user.getFavoriteGames().stream()
                .map(GameResponse::fromEntity)
                .sorted(Comparator.comparing(GameResponse::name))
                .toList();
    }

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + userId));
    }
}
