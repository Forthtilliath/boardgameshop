package com.bgs.boardgameshop.security;

public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException() {
        super("Trop de tentatives de connexion, réessayez dans quelques minutes");
    }
}
