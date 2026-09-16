package com.ecommerce.dao.impl;

import com.ecommerce.dao.OrderDAO;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
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
 * JDBC Implementation of OrderDAO supporting transactions.
 */
public class OrderDAOImpl implements OrderDAO {

    @Override
    public int insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, total_amount, order_status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getCustomerId());
            ps.setBigDecimal(2, order.getTotalAmount());
            ps.setString(3, order.getOrderStatus() != null ? order.getOrderStatus() : "COMPLETED");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    order.setOrderId(orderId);
                    return orderId;
                }
            }
        }
        return -1;
    }

    @Override
    public void insertOrderItems(Connection conn, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setInt(1, item.getOrderId());
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Order> findById(int orderId) throws SQLException {
        String sql = "SELECT order_id, customer_id, total_amount, order_status, order_date FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("order_status"),
                            rs.getTimestamp("order_date")
                    );
                    order.setItems(findOrderItems(orderId));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT order_id, customer_id, total_amount, order_status, order_date FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("order_id"),
                            rs.getInt("customer_id"),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("order_status"),
                            rs.getTimestamp("order_date")
                    ));
                }
            }
        }
        return orders;
    }

    @Override
    public List<OrderItem> findOrderItems(int orderId) throws SQLException {
        // Optimized JOIN query selecting explicit columns
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.product_id, oi.quantity, oi.price, p.product_name " +
                     "FROM order_items oi " +
                     "INNER JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.order_item_id ASC";

        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new OrderItem(
                            rs.getInt("order_item_id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price")
                    ));
                }
            }
        }
        return items;
    }
}
