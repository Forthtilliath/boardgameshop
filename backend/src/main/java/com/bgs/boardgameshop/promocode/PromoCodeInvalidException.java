package com.bgs.boardgameshop.promocode;

/** Code promo inexistant, désactivé, expiré, ou dont le nombre d'utilisations max est atteint. */
public class PromoCodeInvalidException extends RuntimeException {

    public PromoCodeInvalidException(String code) {
        super("Le code promo \"" + code + "\" n'est pas (ou plus) valide");
    }
}
