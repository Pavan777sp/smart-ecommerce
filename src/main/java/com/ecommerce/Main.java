package com.ecommerce;

import com.ecommerce.dao.CartDAO;
import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.OrderDAO;
import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.impl.CartDAOImpl;
import com.ecommerce.dao.impl.CustomerDAOImpl;
import com.ecommerce.dao.impl.OrderDAOImpl;
import com.ecommerce.dao.impl.ProductDAOImpl;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.impl.CartServiceImpl;
import com.ecommerce.service.impl.CustomerServiceImpl;
import com.ecommerce.service.impl.OrderServiceImpl;
import com.ecommerce.service.impl.ProductServiceImpl;
import com.ecommerce.ui.ConsoleMenu;
import com.ecommerce.util.DBConnection;

import java.util.Scanner;

/**
 * Application Entry Point for Smart E-Commerce Backend Engine.
 * Wires layers following Dependency Injection / Inversion of Control.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // 1. Check Database Connectivity
        System.out.println("[*] Checking MySQL Database connection...");
        if (!DBConnection.testConnection()) {
            System.err.println("\n[!] WARNING: Unable to establish connection to MySQL database.");
            System.err.println("    Please ensure:");
            System.err.println("    1. MySQL 8+ service is running (e.g. net start MySQL80).");
            System.err.println("    2. The database 'smart_ecommerce' exists (database/schema.sql executed).");
            System.err.println("    3. Correct credentials are set in src/main/resources/db.properties.");
            System.err.println();

            promptCredentialConfiguration();
        } else {
            System.out.println("[✓] Database connection successfully established!");
        }

        // 2. Initialize DAO Layer
        ProductDAO productDAO = new ProductDAOImpl();
        CustomerDAO customerDAO = new CustomerDAOImpl();
        CartDAO cartDAO = new CartDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();

        // 3. Initialize Service Layer
        ProductService productService = new ProductServiceImpl(productDAO);
        CustomerService customerService = new CustomerServiceImpl(customerDAO, cartDAO);
        CartService cartService = new CartServiceImpl(cartDAO, productDAO, customerDAO);
        OrderService orderService = new OrderServiceImpl(orderDAO, cartDAO, productDAO, customerDAO);

        // 4. Initialize & Launch Console UI
        ConsoleMenu menu = new ConsoleMenu(productService, customerService, cartService, orderService);
        menu.start();
    }

    private static void promptCredentialConfiguration() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Would you like to enter database credentials now? (y/N): ");
        String choice = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(choice)) {
            System.out.print("Enter MySQL Host & Port (default: localhost:3306): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost:3306";

            System.out.print("Enter MySQL Username (default: root): ");
            String user = scanner.nextLine().trim();
            if (user.isEmpty()) user = "root";

            System.out.print("Enter MySQL Password: ");
            String pass = scanner.nextLine();

            String url = "jdbc:mysql://" + host + "/smart_ecommerce?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            DBConnection.setCredentials(url, user, pass);

            if (DBConnection.testConnection()) {
                System.out.println("[✓] Connected to MySQL successfully with provided credentials!");
            } else {
                System.out.println("[!] Connection still failed. Launching application anyway (DB operations may fail until MySQL is running).");
            }
        }
    }

    private static void printBanner() {
        System.out.println("========================================================================");
        System.out.println("            SMART E-COMMERCE BACKEND ENGINE (CORE JAVA + JDBC)         ");
        System.out.println("========================================================================");
        System.out.println(" Version: 1.0.0 | Architecture: 3-Tier Layered (Model-DAO-Service-UI)   ");
        System.out.println(" Technologies: Java 17+, MySQL 8, JDBC PreparedStatements, Collections   ");
        System.out.println("========================================================================\n");
    }
}
