package com.bgs.boardgameshop.game;

public class TagAlreadyExistsException extends RuntimeException {

    public TagAlreadyExistsException(String name) {
        super("Un tag existe deja avec le nom " + name);
    }
}
