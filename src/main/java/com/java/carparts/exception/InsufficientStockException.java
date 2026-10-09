package com.java.carparts.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Integer productId, int requested, int available) {
        super("Insufficient stock for product " + productId
                + ": requested " + requested + ", available " + available);
    }
}
