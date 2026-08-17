package com.bgs.boardgameshop.favorite;

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

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<GameResponse> getFavorites(@AuthenticationPrincipal SecurityUser principal) {
        return favoriteService.getFavorites(principal.getUser().getId());
    }

    /** Génère (ou renvoie) le jeton permettant de partager sa liste de favoris en lecture seule. */
    @GetMapping("/share-link")
    public ShareLinkResponse getShareLink(@AuthenticationPrincipal SecurityUser principal) {
        return new ShareLinkResponse(favoriteService.getOrCreateShareToken(principal.getUser().getId()));
    }

    /** Consultation publique d'une liste de favoris partagée, sans authentification. */
    @GetMapping("/shared/{token}")
    public List<GameResponse> getSharedFavorites(@PathVariable String token) {
        return favoriteService.getFavoritesByShareToken(token);
    }

    @PostMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        favoriteService.addFavorite(principal.getUser().getId(), gameId);
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@PathVariable Long gameId, @AuthenticationPrincipal SecurityUser principal) {
        favoriteService.removeFavorite(principal.getUser().getId(), gameId);
    }
}
