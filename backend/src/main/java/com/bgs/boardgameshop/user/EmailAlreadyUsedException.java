package com.bgs.boardgameshop.user;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Un compte existe deja avec l'email " + email);
    }
}
