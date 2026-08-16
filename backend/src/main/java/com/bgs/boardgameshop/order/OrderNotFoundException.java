package com.bgs.boardgameshop.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Aucune commande trouvée avec l'identifiant " + id);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
