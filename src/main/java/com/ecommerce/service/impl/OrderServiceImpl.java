package com.ecommerce.service.impl;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DatabaseException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of OrderService managing ACID JDBC transactions during checkout.
 */
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final CustomerDAO customerDAO;

    public OrderServiceImpl(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO, CustomerDAO customerDAO) {
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.customerDAO = customerDAO;
    }

    @Override
    public Order checkout(int customerId)
            throws CustomerNotFoundException, InsufficientStockException, InvalidInputException {

        // Validate customer existence
        try {
            if (customerDAO.findById(customerId).isEmpty()) {
                throw new CustomerNotFoundException("Customer with ID " + customerId + " does not exist.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error validating customer during checkout: " + e.getMessage(), e);
        }

        // Fetch cart items
        List<CartItem> cartItems;
        int cartId;
        try {
            cartId = cartDAO.getOrCreateCartId(customerId);
            cartItems = cartDAO.findCartItemsByCustomerId(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving cart during checkout: " + e.getMessage(), e);
        }

        if (cartItems.isEmpty()) {
            throw new InvalidInputException("Checkout failed: Your shopping cart is empty. Please add items before checking out.");
        }

        // Collections demonstration: Use Set<Integer> to track unique product IDs in the checkout
        Set<Integer> uniqueProductIds = new HashSet<>();
        for (CartItem item : cartItems) {
            uniqueProductIds.add(item.getProductId());
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // 1. Begin Transaction
            conn.setAutoCommit(false);

            // 2. Validate current stock for every product in the cart
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                int currentStock = productDAO.getStock(item.getProductId());
                if (currentStock < item.getQuantity()) {
                    throw new InsufficientStockException(item.getProductId(), item.getQuantity(), Math.max(0, currentStock));
                }
                orderTotal = orderTotal.add(item.getSubtotal());
            }

            // 3. Create the Order header
            Order newOrder = new Order();
            newOrder.setCustomerId(customerId);
            newOrder.setTotalAmount(orderTotal);
            newOrder.setOrderStatus("COMPLETED");

            int generatedOrderId = orderDAO.insertOrder(conn, newOrder);
            if (generatedOrderId <= 0) {
                throw new DatabaseException("Failed to generate order record in database.");
            }
            newOrder.setOrderId(generatedOrderId);

            // 4. Create and persist Order Items
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem(
                        generatedOrderId,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                );
                orderItem.setProductName(item.getProductName());
                orderItems.add(orderItem);
            }
            orderDAO.insertOrderItems(conn, orderItems);
            newOrder.setItems(orderItems);

            // 5. Decrement product inventory atomically (fails if stock became insufficient)
            for (CartItem item : cartItems) {
                boolean deducted = productDAO.deductStock(conn, item.getProductId(), item.getQuantity());
                if (!deducted) {
                    throw new InsufficientStockException("Inventory deduction failed for Product ID " +
                            item.getProductId() + " (concurrent purchase detected).");
                }
            }

            // 6. Clear customer's shopping cart
            cartDAO.clearCart(conn, cartId);

            // 7. Commit Transaction
            conn.commit();
            return newOrder;

        } catch (SQLException | InsufficientStockException | DatabaseException e) {
            // Rollback on any failure
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Transaction rollback failed: " + rollbackEx.getMessage());
                }
            }

            if (e instanceof InsufficientStockException) {
                throw (InsufficientStockException) e;
            } else if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            } else {
                throw new DatabaseException("Transaction checkout failure: " + e.getMessage(), e);
            }
        } finally {
            if (conn != null) {
                try {
                    // Restore default auto-commit behavior
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error restoring connection auto-commit: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public List<Order> getCustomerOrders(int customerId) throws CustomerNotFoundException {
        try {
            if (customerDAO.findById(customerId).isEmpty()) {
                throw new CustomerNotFoundException("Customer with ID " + customerId + " was not found.");
            }
            return orderDAO.findByCustomerId(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving customer orders: " + e.getMessage(), e);
        }
    }

    @Override
    public Order getOrderDetails(int orderId) throws InvalidInputException {
        try {
            return orderDAO.findById(orderId)
                    .orElseThrow(() -> new InvalidInputException("Order with ID " + orderId + " was not found."));
        } catch (SQLException e) {
            throw new DatabaseException("Database error retrieving order details: " + e.getMessage(), e);
        }
    }
}
