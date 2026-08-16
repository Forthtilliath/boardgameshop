package com.bgs.boardgameshop.payment;

/**
 * La commande n'est plus dans un etat qui permet de lancer/reprendre un paiement
 * (deja payee, echouee ou annulee).
 */
public class OrderNotPayableException extends RuntimeException {

    public OrderNotPayableException(Long orderId, String status) {
        super("La commande " + orderId + " n'est pas payable (statut actuel : " + status + ")");
    }
}
