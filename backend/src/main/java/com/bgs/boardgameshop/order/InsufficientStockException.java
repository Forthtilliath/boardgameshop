package com.bgs.boardgameshop.order;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String gameName, int requested, int available) {
        super("Stock insuffisant pour \"" + gameName + "\" : " + requested
                + " demande(s), " + available + " disponible(s)");
    }
}
