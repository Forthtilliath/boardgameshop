package com.bgs.boardgameshop.order;

/**
 * Cycle de vie d'une commande, pilote par le paiement Stripe (mode test).
 */
public enum OrderStatus {
    EN_ATTENTE_PAIEMENT,
    PAYEE,
    ECHOUEE,
    ANNULEE
}
