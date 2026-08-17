package com.bgs.boardgameshop.promocode;

public class PromoCodeNotFoundException extends RuntimeException {

    public PromoCodeNotFoundException(Long id) {
        super("Aucun code promo trouvé avec l'identifiant " + id);
    }
}
