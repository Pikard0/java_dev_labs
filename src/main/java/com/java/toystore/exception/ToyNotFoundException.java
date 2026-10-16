package com.java.toystore.exception;

public class ToyNotFoundException extends RuntimeException {
    public ToyNotFoundException(Integer id) {
        super("Toy not found: " + id);
    }
}
