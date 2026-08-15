package com.bgs.boardgameshop.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Aucune commande trouvee avec l'identifiant " + id);
    }
}
