package com.java.toystore.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Integer toyId, int requested, int available) {
        super("Insufficient stock for toy " + toyId
                + ": requested " + requested + ", available " + available);
    }
}
