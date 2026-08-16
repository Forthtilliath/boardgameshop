package com.bgs.boardgameshop.order;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Impossible de passer une commande de " + from + " a " + to);
    }
}
