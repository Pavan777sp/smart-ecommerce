package com.ecommerce.util;

import com.ecommerce.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage database connections using JDBC DriverManager.
 * Reads connection configurations dynamically from db.properties.
 */
public class DBConnection {
    private static final Properties properties = new Properties();
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/smart_ecommerce?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        loadProperties();
        loadDriver();
    }

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    private static void loadProperties() {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                // Fallback default values
                properties.setProperty("db.url", DEFAULT_URL);
                properties.setProperty("db.username", DEFAULT_USER);
                properties.setProperty("db.password", DEFAULT_PASSWORD);
                properties.setProperty("db.driver", DEFAULT_DRIVER);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read db.properties file, utilizing defaults: " + e.getMessage());
            properties.setProperty("db.url", DEFAULT_URL);
            properties.setProperty("db.username", DEFAULT_USER);
            properties.setProperty("db.password", DEFAULT_PASSWORD);
            properties.setProperty("db.driver", DEFAULT_DRIVER);
        }
    }

    private static void loadDriver() {
        String driver = properties.getProperty("db.driver", DEFAULT_DRIVER);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver not found in classpath. Ensure mysql-connector-j is included.", e);
        }
    }

    /**
     * Obtains a new JDBC Connection from DriverManager.
     *
     * @return Connection to MySQL database
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url", DEFAULT_URL);
        String user = properties.getProperty("db.username", DEFAULT_USER);
        String pass = properties.getProperty("db.password", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Verifies if a valid connection can be established.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes one or more database resources quietly without propagating exceptions.
     *
     * @param closeables Resources to close
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Helper to set custom credentials at runtime (e.g. for user configuration).
     */
    public static void setCredentials(String url, String username, String password) {
        if (url != null) properties.setProperty("db.url", url);
        if (username != null) properties.setProperty("db.username", username);
        if (password != null) properties.setProperty("db.password", password);
    }
}
