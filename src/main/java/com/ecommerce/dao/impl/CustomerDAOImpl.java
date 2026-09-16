package com.ecommerce.dao.impl;

import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.model.Customer;
import com.ecommerce.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of CustomerDAO.
 */
public class CustomerDAOImpl implements CustomerDAO {

    private static final String SELECT_COLUMNS = "customer_id, name, email, phone, password, created_at";

    @Override
    public int insertCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail().trim().toLowerCase());
            ps.setString(3, customer.getPhone().trim());
            ps.setString(4, customer.getPassword());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setCustomerId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    @Override
    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customers WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCustomer(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean existsByEmailExcludingId(String email, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?) AND customer_id != ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM customers ORDER BY customer_id ASC";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    @Override
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, password = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail().trim().toLowerCase());
            ps.setString(3, customer.getPhone().trim());
            ps.setString(4, customer.getPassword());
            ps.setInt(5, customer.getCustomerId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getTimestamp("created_at")
        );
    }
}
