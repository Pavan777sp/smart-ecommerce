package com.ecommerce.ui;

import com.ecommerce.exception.CustomerNotFoundException;
import com.ecommerce.exception.DatabaseException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Interactive Console Interface for the Smart E-Commerce Backend Engine.
 */
public class ConsoleMenu {

    private final ProductService productService;
    private final CustomerService customerService;
    private final CartService cartService;
    private final OrderService orderService;
    private final Scanner scanner;

    public ConsoleMenu(ProductService productService,
                       CustomerService customerService,
                       CartService cartService,
                       OrderService orderService) {
        this.productService = productService;
        this.customerService = customerService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> handleCustomerMenu();
                case 2 -> handleProductMenu();
                case 3 -> handleCartMenu();
                case 4 -> handleOrderMenu();
                case 5 -> {
                    System.out.println("\nThank you for using Smart E-Commerce Engine. Goodbye!");
                    exit = true;
                }
                default -> System.out.println("[!] Invalid option. Please select between 1 and 5.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n==========================================");
        System.out.println("       SMART E-COMMERCE SYSTEM            ");
        System.out.println("==========================================");
        System.out.println("1. Customer Management");
        System.out.println("2. Product Management");
        System.out.println("3. Cart Management");
        System.out.println("4. Order Management");
        System.out.println("5. Exit");
        System.out.println("==========================================");
    }

    // =========================================================================
    // 1. CUSTOMER MANAGEMENT MENU
    // =========================================================================
    private void handleCustomerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("1. Register Customer");
            System.out.println("2. View Customer");
            System.out.println("3. Update Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Back to Main Menu");

            int choice = readInt("Select option: ");
            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> viewCustomer();
                case 3 -> updateCustomer();
                case 4 -> deleteCustomer();
                case 5 -> back = true;
                default -> System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void registerCustomer() {
        System.out.println("\n[+] REGISTER NEW CUSTOMER");
        String name = readString("Enter Full Name: ");
        String email = readString("Enter Email: ");
        String phone = readString("Enter Phone Number: ");
        String password = readString("Enter Password: ");

        try {
            Customer created = customerService.registerCustomer(name, email, phone, password);
            System.out.println("[✓] Customer successfully registered with ID: #" + created.getCustomerId());
        } catch (InvalidInputException e) {
            System.out.println("[!] Validation Error: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void viewCustomer() {
        System.out.println("\n[*] VIEW CUSTOMER");
        System.out.println("1. View by Customer ID");
        System.out.println("2. View by Email");
        System.out.println("3. List All Customers");
        int sub = readInt("Select option: ");

        try {
            if (sub == 1) {
                int id = readInt("Enter Customer ID: ");
                Customer c = customerService.getCustomerById(id);
                TableFormatter.printCustomerDetails(c);
            } else if (sub == 2) {
                String email = readString("Enter Customer Email: ");
                Customer c = customerService.getCustomerByEmail(email);
                TableFormatter.printCustomerDetails(c);
            } else if (sub == 3) {
                List<Customer> all = customerService.getAllCustomers();
                TableFormatter.printCustomers(all);
            } else {
                System.out.println("[!] Invalid option.");
            }
        } catch (CustomerNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void updateCustomer() {
        System.out.println("\n[*] UPDATE CUSTOMER");
        int id = readInt("Enter Customer ID to update: ");
        try {
            Customer existing = customerService.getCustomerById(id);
            TableFormatter.printCustomerDetails(existing);

            System.out.println("Leave blank to keep existing values:");
            String name = readString("Enter New Name [" + existing.getName() + "]: ");
            if (name.trim().isEmpty()) name = existing.getName();

            String email = readString("Enter New Email [" + existing.getEmail() + "]: ");
            if (email.trim().isEmpty()) email = existing.getEmail();

            String phone = readString("Enter New Phone [" + existing.getPhone() + "]: ");
            if (phone.trim().isEmpty()) phone = existing.getPhone();

            String password = readString("Enter New Password (or leave blank to keep unchanged): ");

            boolean updated = customerService.updateCustomer(id, name, email, phone, password);
            if (updated) {
                System.out.println("[✓] Customer ID #" + id + " updated successfully.");
            }
        } catch (CustomerNotFoundException | InvalidInputException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void deleteCustomer() {
        System.out.println("\n[-] DELETE CUSTOMER");
        int id = readInt("Enter Customer ID to delete: ");
        String confirm = readString("Are you sure you want to delete Customer #" + id + "? (y/N): ");
        if ("y".equalsIgnoreCase(confirm.trim())) {
            try {
                boolean deleted = customerService.deleteCustomer(id);
                if (deleted) {
                    System.out.println("[✓] Customer #" + id + " and associated cart deleted.");
                }
            } catch (CustomerNotFoundException e) {
                System.out.println("[!] " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("[x] Deletion Error: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // =========================================================================
    // 2. PRODUCT MANAGEMENT MENU
    // =========================================================================
    private void handleProductMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- PRODUCT MANAGEMENT ---");
            System.out.println("1. Add Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Product");
            System.out.println("4. Search by Category");
            System.out.println("5. Update Product");
            System.out.println("6. Delete Product");
            System.out.println("7. Back to Main Menu");

            int choice = readInt("Select option: ");
            switch (choice) {
                case 1 -> addProduct();
                case 2 -> viewAllProducts();
                case 3 -> searchProduct();
                case 4 -> searchByCategory();
                case 5 -> updateProduct();
                case 6 -> deleteProduct();
                case 7 -> back = true;
                default -> System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void addProduct() {
        System.out.println("\n[+] ADD NEW PRODUCT");
        String name = readString("Enter Product Name: ");
        String category = readString("Enter Category: ");
        BigDecimal price = readBigDecimal("Enter Price: ");
        int stock = readInt("Enter Initial Stock Quantity: ");
        String description = readString("Enter Description: ");

        try {
            Product created = productService.addProduct(name, category, price, stock, description);
            System.out.println("[✓] Product added successfully with ID: #" + created.getProductId());
        } catch (InvalidInputException e) {
            System.out.println("[!] Validation Error: " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void viewAllProducts() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("\n  [i] No products currently in catalog.");
            return;
        }

        int pageSize = 15;
        int totalProducts = products.size();
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);
        int currentPage = 1;

        while (true) {
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalProducts);
            List<Product> pageItems = products.subList(start, end);

            System.out.printf("\n=== PRODUCTS CATALOG (Page %d of %d - Showing %d to %d of %d) ===\n",
                    currentPage, totalPages, start + 1, end, totalProducts);
            TableFormatter.printProducts(pageItems);

            if (totalPages <= 1) break;

            System.out.print("[N]ext Page | [P]revious Page | [Q]uit to Menu | Jump to page (number): ");
            String nav = scanner.nextLine().trim();
            if ("q".equalsIgnoreCase(nav)) {
                break;
            } else if ("n".equalsIgnoreCase(nav)) {
                if (currentPage < totalPages) currentPage++;
                else System.out.println("Already on the last page.");
            } else if ("p".equalsIgnoreCase(nav)) {
                if (currentPage > 1) currentPage--;
                else System.out.println("Already on the first page.");
            } else {
                try {
                    int p = Integer.parseInt(nav);
                    if (p >= 1 && p <= totalPages) {
                        currentPage = p;
                    } else {
                        System.out.println("Invalid page number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input command.");
                }
            }
        }
    }

    private void searchProduct() {
        System.out.println("\n[*] SEARCH PRODUCT");
        System.out.println("1. Search by Product ID");
        System.out.println("2. Search by Name (Keyword)");
        int sub = readInt("Select option: ");

        if (sub == 1) {
            int id = readInt("Enter Product ID: ");
            try {
                Product p = productService.getProductById(id);
                TableFormatter.printProductDetails(p);
            } catch (ProductNotFoundException e) {
                System.out.println("[!] " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("[x] System Error: " + e.getMessage());
            }
        } else if (sub == 2) {
            String keyword = readString("Enter Name Keyword: ");
            List<Product> results = productService.searchProductsByName(keyword);
            TableFormatter.printProducts(results);
        } else {
            System.out.println("[!] Invalid option.");
        }
    }

    private void searchByCategory() {
        System.out.println("\n[*] AVAILABLE CATEGORIES:");
        Set<String> categories = productService.getAllCategories();
        int idx = 1;
        for (String cat : categories) {
            System.out.println("  " + idx++ + ". " + cat);
        }

        String catChoice = readString("Enter category name: ");
        List<Product> results = productService.searchProductsByCategory(catChoice);
        TableFormatter.printProducts(results);
    }

    private void updateProduct() {
        System.out.println("\n[*] UPDATE PRODUCT");
        int id = readInt("Enter Product ID to update: ");
        try {
            Product existing = productService.getProductById(id);
            TableFormatter.printProductDetails(existing);

            System.out.println("Leave blank to retain current values:");
            String name = readString("Enter New Name [" + existing.getProductName() + "]: ");
            if (name.trim().isEmpty()) name = existing.getProductName();

            String cat = readString("Enter New Category [" + existing.getCategory() + "]: ");
            if (cat.trim().isEmpty()) cat = existing.getCategory();

            String priceStr = readString("Enter New Price [" + existing.getPrice() + "]: ");
            BigDecimal price = priceStr.trim().isEmpty() ? existing.getPrice() : new BigDecimal(priceStr.trim());

            String stockStr = readString("Enter New Stock Quantity [" + existing.getStockQuantity() + "]: ");
            int stock = stockStr.trim().isEmpty() ? existing.getStockQuantity() : Integer.parseInt(stockStr.trim());

            String desc = readString("Enter New Description [" + existing.getDescription() + "]: ");
            if (desc.trim().isEmpty()) desc = existing.getDescription();

            boolean updated = productService.updateProduct(id, name, cat, price, stock, desc);
            if (updated) {
                System.out.println("[✓] Product #" + id + " updated successfully.");
            }
        } catch (ProductNotFoundException | InvalidInputException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number input. Update cancelled.");
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void deleteProduct() {
        System.out.println("\n[-] DELETE PRODUCT");
        int id = readInt("Enter Product ID to delete: ");
        String confirm = readString("Are you sure you want to delete Product #" + id + "? (y/N): ");
        if ("y".equalsIgnoreCase(confirm.trim())) {
            try {
                boolean deleted = productService.deleteProduct(id);
                if (deleted) {
                    System.out.println("[✓] Product #" + id + " deleted successfully.");
                }
            } catch (ProductNotFoundException e) {
                System.out.println("[!] " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("[x] Deletion Error: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // =========================================================================
    // 3. CART MANAGEMENT MENU
    // =========================================================================
    private void handleCartMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- CART MANAGEMENT ---");
            System.out.println("1. Add to Cart");
            System.out.println("2. Remove from Cart");
            System.out.println("3. Update Quantity");
            System.out.println("4. View Cart");
            System.out.println("5. Clear Cart");
            System.out.println("6. Back to Main Menu");

            int choice = readInt("Select option: ");
            switch (choice) {
                case 1 -> addToCart();
                case 2 -> removeFromCart();
                case 3 -> updateCartQuantity();
                case 4 -> viewCart();
                case 5 -> clearCart();
                case 6 -> back = true;
                default -> System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void addToCart() {
        System.out.println("\n[+] ADD PRODUCT TO CART");
        int customerId = readInt("Enter Customer ID: ");
        int productId = readInt("Enter Product ID: ");
        int quantity = readInt("Enter Quantity: ");

        try {
            cartService.addProductToCart(customerId, productId, quantity);
            System.out.println("[✓] Product added/updated in cart successfully!");
        } catch (CustomerNotFoundException | ProductNotFoundException | InsufficientStockException | InvalidInputException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void removeFromCart() {
        System.out.println("\n[-] REMOVE PRODUCT FROM CART");
        int customerId = readInt("Enter Customer ID: ");
        int productId = readInt("Enter Product ID to remove: ");

        try {
            cartService.removeProductFromCart(customerId, productId);
            System.out.println("[✓] Product removed from cart.");
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void updateCartQuantity() {
        System.out.println("\n[*] UPDATE CART ITEM QUANTITY");
        int customerId = readInt("Enter Customer ID: ");
        int productId = readInt("Enter Product ID: ");
        int newQty = readInt("Enter New Quantity: ");

        try {
            cartService.updateProductQuantity(customerId, productId, newQty);
            System.out.println("[✓] Cart quantity updated.");
        } catch (CustomerNotFoundException | ProductNotFoundException | InsufficientStockException | InvalidInputException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void viewCart() {
        System.out.println("\n[*] VIEW CUSTOMER CART");
        int customerId = readInt("Enter Customer ID: ");

        try {
            Cart cart = cartService.getCustomerCart(customerId);
            TableFormatter.printCart(cart.getItems(), cart.calculateTotal());
        } catch (CustomerNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void clearCart() {
        System.out.println("\n[-] CLEAR CART");
        int customerId = readInt("Enter Customer ID: ");
        String confirm = readString("Are you sure you want to clear this cart? (y/N): ");
        if ("y".equalsIgnoreCase(confirm.trim())) {
            try {
                cartService.clearCart(customerId);
                System.out.println("[✓] Cart cleared successfully.");
            } catch (CustomerNotFoundException e) {
                System.out.println("[!] " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("[x] System Error: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. ORDER MANAGEMENT MENU
    // =========================================================================
    private void handleOrderMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- ORDER MANAGEMENT ---");
            System.out.println("1. Checkout Cart");
            System.out.println("2. View Customer Orders");
            System.out.println("3. View Order Details");
            System.out.println("4. Back to Main Menu");

            int choice = readInt("Select option: ");
            switch (choice) {
                case 1 -> checkoutCart();
                case 2 -> viewCustomerOrders();
                case 3 -> viewOrderDetails();
                case 4 -> back = true;
                default -> System.out.println("[!] Invalid choice. Please try again.");
            }
        }
    }

    private void checkoutCart() {
        System.out.println("\n==========================================");
        System.out.println("         CHECKOUT & PAYMENT SIMULATION    ");
        System.out.println("==========================================");
        int customerId = readInt("Enter Customer ID: ");

        try {
            // Preview cart before placing order
            Cart cart = cartService.getCustomerCart(customerId);
            if (cart.getItems().isEmpty()) {
                System.out.println("[!] Your shopping cart is empty. Please add items before checking out.");
                return;
            }

            TableFormatter.printCart(cart.getItems(), cart.calculateTotal());
            String confirm = readString("Confirm checkout and place order? (y/N): ");
            if (!"y".equalsIgnoreCase(confirm.trim())) {
                System.out.println("Checkout aborted.");
                return;
            }

            System.out.println("\n[⏳] Executing ACID transaction: validating inventory, creating order, updating stock...");
            Order placedOrder = orderService.checkout(customerId);
            System.out.println("[✓] TRANSACTION COMMITTED SUCCESSFULLY!");
            TableFormatter.printOrderInvoice(placedOrder);

        } catch (CustomerNotFoundException | InsufficientStockException | InvalidInputException e) {
            System.out.println("[!] Transaction Aborted (Rolled Back): " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Failure: " + e.getMessage());
        }
    }

    private void viewCustomerOrders() {
        System.out.println("\n[*] VIEW CUSTOMER ORDERS");
        int customerId = readInt("Enter Customer ID: ");

        try {
            List<Order> orders = orderService.getCustomerOrders(customerId);
            TableFormatter.printOrders(orders);
        } catch (CustomerNotFoundException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    private void viewOrderDetails() {
        System.out.println("\n[*] VIEW ORDER DETAILS");
        int orderId = readInt("Enter Order ID: ");

        try {
            Order order = orderService.getOrderDetails(orderId);
            TableFormatter.printOrderInvoice(order);
        } catch (InvalidInputException e) {
            System.out.println("[!] " + e.getMessage());
        } catch (DatabaseException e) {
            System.out.println("[x] System Error: " + e.getMessage());
        }
    }

    // =========================================================================
    // DEFENSIVE SCANNER HELPERS
    // =========================================================================
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid whole number.");
            }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                BigDecimal bd = new BigDecimal(input);
                if (bd.compareTo(BigDecimal.ZERO) > 0) {
                    return bd;
                }
                System.out.println("  [!] Price must be greater than 0.00.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid decimal number (e.g. 29.99).");
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
