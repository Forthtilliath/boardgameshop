package com.bgs.boardgameshop.game;

public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(Long id) {
        super("Aucun tag trouvé avec l'identifiant " + id);
    }
}
