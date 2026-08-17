package com.bgs.boardgameshop.order;

/** La facture n'est disponible qu'une fois la commande payée (ou au-delà dans son cycle de vie). */
public class InvoiceNotAvailableException extends RuntimeException {

    public InvoiceNotAvailableException(Long orderId) {
        super("La facture de la commande " + orderId + " n'est pas encore disponible (paiement non confirmé)");
    }
}
