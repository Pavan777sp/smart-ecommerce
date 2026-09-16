package com.ecommerce.service.impl;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DatabaseException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.model.Customer;
import com.ecommerce.service.CustomerService;
import com.ecommerce.util.InputValidator;
import com.ecommerce.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of CustomerService with validations and duplicate checks.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO customerDAO;
    private final CartDAO cartDAO;

    public CustomerServiceImpl(CustomerDAO customerDAO, CartDAO cartDAO) {
        this.customerDAO = customerDAO;
        this.cartDAO = cartDAO;
    }

    @Override
    public Customer registerCustomer(String name, String email, String phone, String password)
            throws InvalidInputException {

        InputValidator.validateNonEmpty(name, "Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);
        InputValidator.validateNonEmpty(password, "Password");

        try {
            if (customerDAO.existsByEmail(email)) {
                throw new InvalidInputException("Email address '" + email + "' is already registered to another account.");
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            Customer customer = new Customer(name.trim(), email.trim(), phone.trim(), hashedPassword);
            int customerId = customerDAO.insertCustomer(customer);

            if (customerId > 0) {
                customer.setCustomerId(customerId);
                // Pre-create customer's shopping cart
                cartDAO.getOrCreateCartId(customerId);
                return customer;
            } else {
                throw new DatabaseException("Failed to register customer in database.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during customer registration: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer getCustomerById(int customerId) throws CustomerNotFoundException {
        try {
            return customerDAO.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " was not found."));
        } catch (SQLException e) {
            throw new DatabaseException("Database error while retrieving customer by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer getCustomerByEmail(String email) throws CustomerNotFoundException {
        try {
            return customerDAO.findByEmail(email)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer with email '" + email + "' was not found."));
        } catch (SQLException e) {
            throw new DatabaseException("Database error while retrieving customer by email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> getAllCustomers() {
        try {
            return customerDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error while retrieving all customers: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateCustomer(int customerId, String name, String email, String phone, String password)
            throws CustomerNotFoundException, InvalidInputException {

        Customer existing = getCustomerById(customerId);

        InputValidator.validateNonEmpty(name, "Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);

        try {
            if (customerDAO.existsByEmailExcludingId(email, customerId)) {
                throw new InvalidInputException("Email address '" + email + "' is already in use by another customer.");
            }

            String updatedPassword = (password != null && !password.trim().isEmpty())
                    ? PasswordUtil.hashPassword(password)
                    : existing.getPassword();

            Customer updated = new Customer(customerId, name.trim(), email.trim(), phone.trim(), updatedPassword, null);
            return customerDAO.updateCustomer(updated);
        } catch (SQLException e) {
            throw new DatabaseException("Database error while updating customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws CustomerNotFoundException {
        getCustomerById(customerId);
        try {
            return customerDAO.deleteCustomer(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error while deleting customer: " + e.getMessage(), e);
        }
    }
}
