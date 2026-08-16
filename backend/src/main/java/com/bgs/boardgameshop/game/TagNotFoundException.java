package com.bgs.boardgameshop.game;

public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(Long id) {
        super("Aucun tag trouve avec l'identifiant " + id);
    }
}
