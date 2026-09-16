package com.ecommerce.dao;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Cart and CartItem entity operations.
 */
public interface CartDAO {
    int getOrCreateCartId(int customerId) throws SQLException;
    Optional<Cart> findCartByCustomerId(int customerId) throws SQLException;
    List<CartItem> findCartItemsByCustomerId(int customerId) throws SQLException;
    Optional<CartItem> findCartItem(int cartId, int productId) throws SQLException;
    boolean addOrUpdateItem(int cartId, int productId, int additionalQuantity) throws SQLException;
    boolean updateItemQuantity(int cartId, int productId, int newQuantity) throws SQLException;
    boolean removeItem(int cartId, int productId) throws SQLException;
    boolean clearCart(int cartId) throws SQLException;
    boolean clearCart(Connection conn, int cartId) throws SQLException;
}
