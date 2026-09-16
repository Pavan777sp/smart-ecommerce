package com.ecommerce.dao;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Order and OrderItem entity operations.
 */
public interface OrderDAO {
    int insertOrder(Connection conn, Order order) throws SQLException;
    void insertOrderItems(Connection conn, List<OrderItem> items) throws SQLException;
    Optional<Order> findById(int orderId) throws SQLException;
    List<Order> findByCustomerId(int customerId) throws SQLException;
    List<OrderItem> findOrderItems(int orderId) throws SQLException;
}
