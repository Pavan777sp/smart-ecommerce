package com.ecommerce.service;

import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.model.Customer;

import java.util.List;

/**
 * Business Service interface for Customer operations.
 */
public interface CustomerService {
    Customer registerCustomer(String name, String email, String phone, String password)
            throws InvalidInputException;

    Customer getCustomerById(int customerId) throws CustomerNotFoundException;

    Customer getCustomerByEmail(String email) throws CustomerNotFoundException;

    List<Customer> getAllCustomers();

    boolean updateCustomer(int customerId, String name, String email, String phone, String password)
            throws CustomerNotFoundException, InvalidInputException;

    boolean deleteCustomer(int customerId) throws CustomerNotFoundException;
}
