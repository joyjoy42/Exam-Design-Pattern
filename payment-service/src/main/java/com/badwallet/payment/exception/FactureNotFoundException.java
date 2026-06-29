package com.badwallet.payment.exception;

public class FactureNotFoundException extends RuntimeException {
    public FactureNotFoundException(String message) {
        super(message);
    }
}
