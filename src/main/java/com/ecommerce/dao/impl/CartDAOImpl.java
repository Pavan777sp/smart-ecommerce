package com.ecommerce.dao.impl;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
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
 * JDBC Implementation of CartDAO.
 */
public class CartDAOImpl implements CartDAO {

    @Override
    public int getOrCreateCartId(int customerId) throws SQLException {
        String selectSql = "SELECT cart_id FROM carts WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_id");
                }
            }
        }

        // Cart does not exist, create one
        String insertSql = "INSERT INTO carts (customer_id) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public Optional<Cart> findCartByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT cart_id, customer_id, created_at FROM carts WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart(
                            rs.getInt("cart_id"),
                            rs.getInt("customer_id"),
                            rs.getTimestamp("created_at")
                    );
                    cart.setItems(findCartItemsByCustomerId(customerId));
                    return Optional.of(cart);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<CartItem> findCartItemsByCustomerId(int customerId) throws SQLException {
        // Optimized JOIN query selecting only needed columns
        String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.added_at, " +
                     "p.product_name, p.category, p.price, p.stock_quantity " +
                     "FROM cart_items ci " +
                     "INNER JOIN carts c ON ci.cart_id = c.cart_id " +
                     "INNER JOIN products p ON ci.product_id = p.product_id " +
                     "WHERE c.customer_id = ? " +
                     "ORDER BY ci.added_at DESC";

        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CartItem(
                            rs.getInt("cart_item_id"),
                            rs.getInt("cart_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("stock_quantity"),
                            rs.getTimestamp("added_at")
                    ));
                }
            }
        }
        return items;
    }

    @Override
    public Optional<CartItem> findCartItem(int cartId, int productId) throws SQLException {
        String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.added_at, " +
                     "p.product_name, p.category, p.price, p.stock_quantity " +
                     "FROM cart_items ci " +
                     "INNER JOIN products p ON ci.product_id = p.product_id " +
                     "WHERE ci.cart_id = ? AND ci.product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CartItem(
                            rs.getInt("cart_item_id"),
                            rs.getInt("cart_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("stock_quantity"),
                            rs.getTimestamp("added_at")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addOrUpdateItem(int cartId, int productId, int additionalQuantity) throws SQLException {
        // Upsert: If product already exists in cart, update quantity; else insert new record
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, additionalQuantity);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateItemQuantity(int cartId, int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, cartId);
            ps.setInt(3, productId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeItem(int cartId, int productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clearCart(int cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cartId);
            return ps.executeUpdate() >= 0;
        }
    }

    @Override
    public boolean clearCart(Connection conn, int cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            return ps.executeUpdate() >= 0;
        }
    }
}
