package com.bgs.boardgameshop.payment;

/**
 * La commande n'est plus dans un état qui permet de lancer/reprendre un paiement
 * (déjà payée, échouée ou annulée).
 */
public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(Long orderId, String status) {
        super("La commande " + orderId + " n'est pas payable (statut actuel : " + status + ")");
    }
}
