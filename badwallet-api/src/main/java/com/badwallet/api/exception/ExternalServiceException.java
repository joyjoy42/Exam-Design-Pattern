package com.badwallet.api.exception;

/** Raised by the payment-service proxy when the external billing API cannot be reached or errors out. */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
