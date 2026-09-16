package com.ecommerce.util;

import com.ecommerce.exception.InvalidInputException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validates domain constraints on user inputs before database persistence.
 */
public class InputValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    private InputValidator() {
        // Prevent instantiation
    }

    /**
     * Validates that a string is neither null nor blank.
     */
    public static void validateNonEmpty(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty or contain only whitespace.");
        }
    }

    /**
     * Validates email syntax.
     */
    public static void validateEmail(String email) throws InvalidInputException {
        validateNonEmpty(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidInputException("Invalid email format: '" + email + "'. Expected format: user@domain.com");
        }
    }

    /**
     * Validates phone number format (10 to 15 digits, optional leading +).
     */
    public static void validatePhone(String phone) throws InvalidInputException {
        validateNonEmpty(phone, "Phone");
        String sanitized = phone.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(sanitized).matches()) {
            throw new InvalidInputException("Invalid phone number: '" + phone + "'. Must contain 10-15 numeric digits.");
        }
    }

    /**
     * Validates that price is strictly greater than 0.
     */
    public static void validatePositivePrice(BigDecimal price) throws InvalidInputException {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Product price must be greater than 0.00.");
        }
    }

    /**
     * Validates that stock quantity is non-negative (>= 0).
     */
    public static void validateNonNegativeStock(int stock) throws InvalidInputException {
        if (stock < 0) {
            throw new InvalidInputException("Stock quantity cannot be negative: " + stock);
        }
    }

    /**
     * Validates that requested quantity is strictly positive (> 0).
     */
    public static void validatePositiveQuantity(int quantity) throws InvalidInputException {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be greater than 0: " + quantity);
        }
    }
}
