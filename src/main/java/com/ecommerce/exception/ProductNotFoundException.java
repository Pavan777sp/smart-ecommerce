package com.ecommerce.exception;

/**
 * Thrown when a requested product cannot be found in the catalog.
 */
public class ProductNotFoundException extends Exception {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
