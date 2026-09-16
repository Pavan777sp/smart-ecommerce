package com.ecommerce.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for password hashing and verification using SHA-256.
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Computes the SHA-256 hex string for a given raw password.
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in JVM", e);
        }
    }

    /**
     * Checks whether a raw password matches the stored password.
     * Supports matching either the raw text (for simple seed demo) or SHA-256 hash.
     */
    public static boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;
        if (rawPassword.equals(storedPassword)) {
            return true;
        }
        String hashedInput = hashPassword(rawPassword);
        return hashedInput.equalsIgnoreCase(storedPassword);
    }
}
