package com.bgs.boardgameshop.stockalert;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public StockAlertService(
            StockAlertRepository stockAlertRepository,
            GameRepository gameRepository,
            UserRepository userRepository
    ) {
        this.stockAlertRepository = stockAlertRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void subscribe(Long userId, Long gameId) {
        if (stockAlertRepository.existsByUserIdAndGameId(userId, gameId)) {
            return;
        }
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + userId));
        stockAlertRepository.save(StockAlert.builder().user(user).game(game).build());
    }

    @Transactional
    public void unsubscribe(Long userId, Long gameId) {
        stockAlertRepository.findByUserIdAndGameId(userId, gameId).ifPresent(stockAlertRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(Long userId, Long gameId) {
        return stockAlertRepository.existsByUserIdAndGameId(userId, gameId);
    }

    /** Jeux suivis par l'utilisateur qui sont de nouveau en stock (pour le badge de notification). */
    @Transactional(readOnly = true)
    public List<GameResponse> getReadyAlerts(Long userId) {
        return stockAlertRepository.findByUserIdAndGame_StockGreaterThan(userId, 0).stream()
                .map(alert -> GameResponse.fromEntity(alert.getGame()))
                .toList();
    }
}
