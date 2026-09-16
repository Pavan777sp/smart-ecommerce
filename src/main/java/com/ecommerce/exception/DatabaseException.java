package com.ecommerce.exception;

/**
 * Thrown when an unrecoverable database or persistence failure occurs.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
