package com.ecommerce.service;

import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Business Service interface for shopping cart operations.
 */
public interface CartService {
    void addProductToCart(int customerId, int productId, int quantity)
            throws CustomerNotFoundException, ProductNotFoundException, InsufficientStockException, InvalidInputException;

    void updateProductQuantity(int customerId, int productId, int newQuantity)
            throws CustomerNotFoundException, ProductNotFoundException, InsufficientStockException, InvalidInputException;

    void removeProductFromCart(int customerId, int productId)
            throws CustomerNotFoundException, ProductNotFoundException;

    Cart getCustomerCart(int customerId) throws CustomerNotFoundException;

    Map<Integer, CartItem> getCartItemsMap(int customerId) throws CustomerNotFoundException;

    BigDecimal calculateCartTotal(int customerId) throws CustomerNotFoundException;

    void clearCart(int customerId) throws CustomerNotFoundException;
}
