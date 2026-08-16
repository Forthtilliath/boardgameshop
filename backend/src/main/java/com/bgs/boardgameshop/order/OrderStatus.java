package com.bgs.boardgameshop.order;

/**
 * Cycle de vie d'une commande : paiement Stripe (EN_ATTENTE_PAIEMENT -> PAYEE/ECHOUEE),
 * puis suivi de la preparation cote admin (PAYEE -> EXPEDIEE -> LIVREE), ou ANNULEE
 * a tout moment avant expedition.
 */
public enum OrderStatus {
    EN_ATTENTE_PAIEMENT,
    PAYEE,
    ECHOUEE,
    EXPEDIEE,
    LIVREE,
    ANNULEE
}
