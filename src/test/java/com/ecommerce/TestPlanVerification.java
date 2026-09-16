package com.ecommerce;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.impl.CartDAOImpl;
import com.ecommerce.dao.impl.CustomerDAOImpl;
import com.ecommerce.dao.impl.OrderDAOImpl;
import com.ecommerce.dao.impl.ProductDAOImpl;
import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.impl.CartServiceImpl;
import com.ecommerce.service.impl.CustomerServiceImpl;
import com.ecommerce.service.impl.OrderServiceImpl;
import com.ecommerce.service.impl.ProductServiceImpl;
import com.ecommerce.util.DBConnection;
import com.ecommerce.util.InputValidator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Automated Verification Suite covering the test plan defined in Requirement 19.
 * Validates domain rules, input checks, collection mechanics, and DB integration.
 */
public class TestPlanVerification {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("     RUNNING SMART E-COMMERCE ENGINE VERIFICATION SUITE         ");
        System.out.println("================================================================\n");

        runInputValidationTests();
        runCollectionsMechanicsTests();

        if (DBConnection.testConnection()) {
            System.out.println("\n[✓] MySQL database detected. Executing Integration Test Suite...");
            runDatabaseIntegrationTests();
        } else {
            System.out.println("\n[!] Notice: MySQL server is not currently reachable.");
            System.out.println("    Unit and validation test suites executed successfully.");
            System.out.println("    To run full DB integration tests, ensure MySQL is running and re-run this suite.");
        }

        System.out.println("\n================================================================");
        System.out.printf(" VERIFICATION SUMMARY: Passed: %d | Failed: %d\n", testsPassed, testsFailed);
        System.out.println("================================================================");
    }

    private static void assertTrue(String testName, boolean condition, String failureReason) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> " + failureReason);
            testsFailed++;
        }
    }

    private static void runInputValidationTests() {
        System.out.println("--- 1. Testing Input & Business Validation Rules ---");

        // Test 1: Empty name
        try {
            InputValidator.validateNonEmpty("   ", "Customer name");
            assertTrue("Reject empty name", false, "Allowed whitespace name");
        } catch (InvalidInputException e) {
            assertTrue("Reject empty name", true, "");
        }

        // Test 2: Invalid email
        try {
            InputValidator.validateEmail("invalid-email-address");
            assertTrue("Reject invalid email", false, "Allowed bad email");
        } catch (InvalidInputException e) {
            assertTrue("Reject invalid email format", true, "");
        }

        // Test 3: Valid email
        try {
            InputValidator.validateEmail("test.developer@domain.com");
            assertTrue("Accept valid email format", true, "");
        } catch (InvalidInputException e) {
            assertTrue("Accept valid email format", false, e.getMessage());
        }

        // Test 4: Invalid phone
        try {
            InputValidator.validatePhone("12345"); // Too short
            assertTrue("Reject short phone number", false, "Allowed 5-digit phone");
        } catch (InvalidInputException e) {
            assertTrue("Reject short phone number", true, "");
        }

        // Test 5: Valid phone
        try {
            InputValidator.validatePhone("9876543210");
            assertTrue("Accept valid 10-digit phone number", true, "");
        } catch (InvalidInputException e) {
            assertTrue("Accept valid 10-digit phone number", false, e.getMessage());
        }

        // Test 6: Non-positive price
        try {
            InputValidator.validatePositivePrice(BigDecimal.ZERO);
            assertTrue("Reject zero price", false, "Allowed zero price");
        } catch (InvalidInputException e) {
            assertTrue("Reject zero price", true, "");
        }

        // Test 7: Negative stock
        try {
            InputValidator.validateNonNegativeStock(-5);
            assertTrue("Reject negative stock", false, "Allowed negative stock");
        } catch (InvalidInputException e) {
            assertTrue("Reject negative stock", true, "");
        }

        // Test 8: Non-positive quantity
        try {
            InputValidator.validatePositiveQuantity(0);
            assertTrue("Reject zero quantity", false, "Allowed 0 quantity");
        } catch (InvalidInputException e) {
            assertTrue("Reject zero quantity", true, "");
        }
    }

    private static void runCollectionsMechanicsTests() {
        System.out.println("\n--- 2. Testing Java Collections Framework Invariants ---");

        // Test Map<Integer, CartItem> indexing
        Map<Integer, CartItem> cartMap = new HashMap<>();
        CartItem item1 = new CartItem(1, 101, 2);
        item1.setUnitPrice(new BigDecimal("49.99"));
        item1.setProductName("Test Headphone");

        cartMap.put(item1.getProductId(), item1);
        assertTrue("Map indexing by Product ID", cartMap.containsKey(101), "Product not in map");

        // Test Set<Integer> uniqueness tracking
        Set<Integer> uniquePids = new HashSet<>();
        uniquePids.add(101);
        uniquePids.add(102);
        uniquePids.add(101); // duplicate
        assertTrue("Set uniqueness preserves 2 distinct IDs", uniquePids.size() == 2, "Size was " + uniquePids.size());

        // Test Cart total calculation
        Cart cart = new Cart();
        cart.getItems().add(item1);
        CartItem item2 = new CartItem(1, 102, 3);
        item2.setUnitPrice(new BigDecimal("10.00"));
        cart.getItems().add(item2);

        BigDecimal expectedTotal = new BigDecimal("49.99").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("10.00").multiply(BigDecimal.valueOf(3))); // 99.98 + 30.00 = 129.98
        assertTrue("Cart total calculation arithmetic", cart.calculateTotal().compareTo(expectedTotal) == 0,
                "Expected " + expectedTotal + " but got " + cart.calculateTotal());
    }

    private static void runDatabaseIntegrationTests() {
        ProductDAO productDAO = new ProductDAOImpl();
        CustomerDAO customerDAO = new CustomerDAOImpl();
        CartDAO cartDAO = new CartDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();

        ProductService productService = new ProductServiceImpl(productDAO);
        CustomerService customerService = new CustomerServiceImpl(customerDAO, cartDAO);
        CartService cartService = new CartServiceImpl(cartDAO, productDAO, customerDAO);
        OrderService orderService = new OrderServiceImpl(orderDAO, cartDAO, productDAO, customerDAO);

        String testEmail = "test.suite." + System.currentTimeMillis() + "@example.com";
        int createdCustId = -1;
        int createdProdId = -1;

        try {
            // 1. Customer registration
            Customer c = customerService.registerCustomer("Test User", testEmail, "9876543210", "Secret123");
            createdCustId = c.getCustomerId();
            assertTrue("Customer registered with valid ID", createdCustId > 0, "Invalid ID generated");

            // 2. Duplicate email check
            try {
                customerService.registerCustomer("Another User", testEmail, "9876543211", "Secret123");
                assertTrue("Duplicate email registration rejected", false, "Allowed duplicate email");
            } catch (InvalidInputException e) {
                assertTrue("Duplicate email registration rejected", true, "");
            }

            // 3. Product creation
            Product p = productService.addProduct("Automated Test Laptop", "Laptops", new BigDecimal("899.99"), 5, "Unit test product");
            createdProdId = p.getProductId();
            assertTrue("Product created successfully", createdProdId > 0, "Invalid product ID generated");

            // 4. Product search by name
            List<Product> searchResults = productService.searchProductsByName("Automated Test Laptop");
            assertTrue("Product found via name search", !searchResults.isEmpty(), "Search returned empty");

            // 5. Add to cart with valid quantity
            cartService.addProductToCart(createdCustId, createdProdId, 2);
            Cart cart = cartService.getCustomerCart(createdCustId);
            final int pIdToCheck = createdProdId;
            assertTrue("Product present in cart with quantity 2",
                    cart.getItems().stream().anyMatch(ci -> ci.getProductId() == pIdToCheck && ci.getQuantity() == 2),
                    "Cart item not found");

            // 6. Insufficient stock check on add to cart (existing 2 + 4 = 6 > 5 available)
            try {
                cartService.addProductToCart(createdCustId, createdProdId, 4);
                assertTrue("Insufficient stock check rejected excessive quantity", false, "Allowed overstock");
            } catch (InsufficientStockException e) {
                assertTrue("Insufficient stock check rejected excessive quantity", true, "");
            }

            // 7. Checkout execution (ACID transaction)
            Order order = orderService.checkout(createdCustId);
            assertTrue("Order placed successfully with ID", order.getOrderId() > 0, "Order failed");

            // 8. Verify stock reduction (5 - 2 = 3)
            int updatedStock = productService.checkStock(createdProdId);
            assertTrue("Stock deducted accurately to 3", updatedStock == 3, "Stock was: " + updatedStock);

            // 9. Verify cart cleared after checkout
            Cart cartAfterCheckout = cartService.getCustomerCart(createdCustId);
            assertTrue("Cart cleared after successful checkout", cartAfterCheckout.getItems().isEmpty(), "Cart not empty");

        } catch (Exception e) {
            System.err.println("  [ERROR] Exception in integration test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test data
            try {
                if (createdCustId > 0) customerService.deleteCustomer(createdCustId);
                if (createdProdId > 0) productService.deleteProduct(createdProdId);
            } catch (Exception ignored) {
            }
        }
    }
}
