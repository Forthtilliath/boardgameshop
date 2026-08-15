package com.bgs.boardgameshop.game;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long id) {
        super("Aucun jeu trouve avec l'identifiant " + id);
    }
}
