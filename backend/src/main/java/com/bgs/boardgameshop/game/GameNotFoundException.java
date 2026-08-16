package com.bgs.boardgameshop.game;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("Aucun jeu trouvé avec l'identifiant " + id);
    }
}
