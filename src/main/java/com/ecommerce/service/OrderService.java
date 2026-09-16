package com.ecommerce.service;

import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.model.Order;

import java.util.List;

/**
 * Business Service interface for Order operations and Transactional Checkout.
 */
public interface OrderService {
    /**
     * Executes the checkout process using JDBC Transaction Management.
     * Guarantees ACID properties: validates stock, inserts order & items,
     * decrements stock, clears cart, and commits, or rolls back completely on any failure.
     */
    Order checkout(int customerId)
            throws CustomerNotFoundException, InsufficientStockException, InvalidInputException;

    List<Order> getCustomerOrders(int customerId) throws CustomerNotFoundException;

    Order getOrderDetails(int orderId) throws InvalidInputException;
}
