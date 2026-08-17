package com.bgs.boardgameshop.promocode;

public class PromoCodeAlreadyExistsException extends RuntimeException {

    public PromoCodeAlreadyExistsException(String code) {
        super("Un code promo existe déjà avec le code " + code);
    }
}
