package com.ecommerce.dao;

import com.ecommerce.model.Customer;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Customer entity operations.
 */
public interface CustomerDAO {
    int insertCustomer(Customer customer) throws SQLException;
    Optional<Customer> findById(int customerId) throws SQLException;
    Optional<Customer> findByEmail(String email) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    boolean existsByEmailExcludingId(String email, int customerId) throws SQLException;
    List<Customer> findAll() throws SQLException;
    boolean updateCustomer(Customer customer) throws SQLException;
    boolean deleteCustomer(int customerId) throws SQLException;
}
