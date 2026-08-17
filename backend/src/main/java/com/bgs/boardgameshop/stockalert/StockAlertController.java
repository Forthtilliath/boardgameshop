package com.bgs.boardgameshop.stockalert;

import com.bgs.boardgameshop.game.GameResponse;
import com.bgs.boardgameshop.security.SecurityUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Alertes "retour en stock" (in-app uniquement, pas d'email — voir IMPROVEMENT.md). */
@RestController
@RequestMapping("/api/stock-alerts")
public class StockAlertController {

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
    }

    @GetMapping("/{gameId}")
    public boolean isSubscribed(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        return stockAlertService.isSubscribed(principal.getUser().getId(), gameId);
    }

    @PostMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        stockAlertService.subscribe(principal.getUser().getId(), gameId);
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        stockAlertService.unsubscribe(principal.getUser().getId(), gameId);
    }

    /** Jeux suivis désormais de nouveau en stock (badge de notification). */
    @GetMapping("/ready")
    public List<GameResponse> getReadyAlerts(@AuthenticationPrincipal SecurityUser principal) {
        return stockAlertService.getReadyAlerts(principal.getUser().getId());
    }

    /** Acquitte une alerte "prête" (retire le jeu du badge sans empêcher un futur réabonnement). */
    @DeleteMapping("/ready/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismissReadyAlert(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        stockAlertService.unsubscribe(principal.getUser().getId(), gameId);
    }
}
