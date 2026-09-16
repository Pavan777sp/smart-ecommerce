package com.ecommerce.exception;

/**
 * Thrown when a requested customer account cannot be found.
 */
public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
