package com.ecommerce.service.impl;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DatabaseException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of CartService with stock verification and cart synchronization.
 */
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;

    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO, CustomerDAO customerDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.customerDAO = customerDAO;
    }

    private void ensureCustomerExists(int customerId) throws CustomerNotFoundException {
        try {
            if (customerDAO.findById(customerId).isEmpty()) {
                throw new CustomerNotFoundException("Customer with ID " + customerId + " was not found.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking customer existence: " + e.getMessage(), e);
        }
    }

    private Product ensureProductExists(int productId) throws ProductNotFoundException {
        try {
            return productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " was not found."));
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking product existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void addProductToCart(int customerId, int productId, int quantity)
            throws CustomerNotFoundException, ProductNotFoundException, InsufficientStockException, InvalidInputException {

        ensureCustomerExists(customerId);
        Product product = ensureProductExists(productId);
        InputValidator.validatePositiveQuantity(quantity);

        try {
            int cartId = cartDAO.getOrCreateCartId(customerId);
            Optional<CartItem> existingItemOpt = cartDAO.findCartItem(cartId, productId);

            int currentCartQuantity = existingItemOpt.map(CartItem::getQuantity).orElse(0);
            int targetQuantity = currentCartQuantity + quantity;

            if (targetQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException(productId, targetQuantity, product.getStockQuantity());
            }

            cartDAO.addOrUpdateItem(cartId, productId, quantity);
        } catch (SQLException e) {
            throw new DatabaseException("Database error adding product to cart: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProductQuantity(int customerId, int productId, int newQuantity)
            throws CustomerNotFoundException, ProductNotFoundException, InsufficientStockException, InvalidInputException {

        ensureCustomerExists(customerId);
        Product product = ensureProductExists(productId);
        InputValidator.validatePositiveQuantity(newQuantity);

        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(productId, newQuantity, product.getStockQuantity());
        }

        try {
            int cartId = cartDAO.getOrCreateCartId(customerId);
            boolean updated = cartDAO.updateItemQuantity(cartId, productId, newQuantity);
            if (!updated) {
                throw new ProductNotFoundException("Product ID " + productId + " is not currently in the customer's cart.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating cart item quantity: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeProductFromCart(int customerId, int productId)
            throws CustomerNotFoundException, ProductNotFoundException {

        ensureCustomerExists(customerId);
        ensureProductExists(productId);

        try {
            int cartId = cartDAO.getOrCreateCartId(customerId);
            boolean removed = cartDAO.removeItem(cartId, productId);
            if (!removed) {
                throw new ProductNotFoundException("Product ID " + productId + " was not found in cart.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error removing item from cart: " + e.getMessage(), e);
        }
    }

    @Override
    public Cart getCustomerCart(int customerId) throws CustomerNotFoundException {
        ensureCustomerExists(customerId);
        try {
            return cartDAO.findCartByCustomerId(customerId)
                    .orElseGet(() -> {
                        try {
                            int newCartId = cartDAO.getOrCreateCartId(customerId);
                            return new Cart(newCartId, customerId, null);
                        } catch (SQLException e) {
                            throw new DatabaseException("Database error initializing cart: " + e.getMessage(), e);
                        }
                    });
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving customer cart: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<Integer, CartItem> getCartItemsMap(int customerId) throws CustomerNotFoundException {
        ensureCustomerExists(customerId);
        try {
            List<CartItem> items = cartDAO.findCartItemsByCustomerId(customerId);
            Map<Integer, CartItem> itemMap = new HashMap<>(items.size());
            for (CartItem item : items) {
                itemMap.put(item.getProductId(), item);
            }
            return itemMap;
        } catch (SQLException e) {
            throw new DatabaseException("Database error mapping cart items: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal calculateCartTotal(int customerId) throws CustomerNotFoundException {
        Cart cart = getCustomerCart(customerId);
        return cart.calculateTotal();
    }

    @Override
    public void clearCart(int customerId) throws CustomerNotFoundException {
        ensureCustomerExists(customerId);
        try {
            int cartId = cartDAO.getOrCreateCartId(customerId);
            cartDAO.clearCart(cartId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error clearing customer cart: " + e.getMessage(), e);
        }
    }
}
