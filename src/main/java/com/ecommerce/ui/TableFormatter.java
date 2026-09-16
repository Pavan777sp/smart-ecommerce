package com.ecommerce.ui;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Utility for rendering domain entities in formatted ASCII tables for console display.
 */
public class TableFormatter {

    public static void printProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            System.out.println("\n  [i] No products found.");
            return;
        }

        String format = "| %-6s | %-38s | %-16s | %10s | %8s |\n";
        String separator = "+--------+----------------------------------------+------------------+------------+----------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Product Name", "Category", "Price ($)", "Stock");
        System.out.println(separator);

        for (Product p : products) {
            String name = p.getProductName();
            if (name.length() > 36) {
                name = name.substring(0, 33) + "...";
            }
            System.out.printf(format,
                    p.getProductId(),
                    name,
                    p.getCategory(),
                    String.format("%.2f", p.getPrice()),
                    p.getStockQuantity());
        }
        System.out.println(separator);
        System.out.println("  Total Products: " + products.size());
    }

    public static void printProductDetails(Product p) {
        if (p == null) return;
        System.out.println("\n------------------------------------------------------------");
        System.out.println(" PRODUCT DETAILS");
        System.out.println("------------------------------------------------------------");
        System.out.println(" ID:           " + p.getProductId());
        System.out.println(" Name:         " + p.getProductName());
        System.out.println(" Category:     " + p.getCategory());
        System.out.println(" Price:        $" + String.format("%.2f", p.getPrice()));
        System.out.println(" In Stock:     " + p.getStockQuantity() + " units");
        System.out.println(" Description:  " + (p.getDescription() != null ? p.getDescription() : "N/A"));
        System.out.println(" Created At:   " + (p.getCreatedAt() != null ? p.getCreatedAt() : "N/A"));
        System.out.println("------------------------------------------------------------");
    }

    public static void printCustomers(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            System.out.println("\n  [i] No customers registered.");
            return;
        }

        String format = "| %-6s | %-25s | %-30s | %-15s |\n";
        String separator = "+--------+---------------------------+--------------------------------+-----------------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Name", "Email", "Phone");
        System.out.println(separator);

        for (Customer c : customers) {
            System.out.printf(format,
                    c.getCustomerId(),
                    c.getName(),
                    c.getEmail(),
                    c.getPhone());
        }
        System.out.println(separator);
    }

    public static void printCustomerDetails(Customer c) {
        if (c == null) return;
        System.out.println("\n------------------------------------------------------------");
        System.out.println(" CUSTOMER PROFILE");
        System.out.println("------------------------------------------------------------");
        System.out.println(" ID:         " + c.getCustomerId());
        System.out.println(" Name:       " + c.getName());
        System.out.println(" Email:      " + c.getEmail());
        System.out.println(" Phone:      " + c.getPhone());
        System.out.println(" Registered: " + (c.getCreatedAt() != null ? c.getCreatedAt() : "N/A"));
        System.out.println("------------------------------------------------------------");
    }

    public static void printCart(List<CartItem> items, BigDecimal total) {
        if (items == null || items.isEmpty()) {
            System.out.println("\n  [i] Your shopping cart is currently empty.");
            return;
        }

        String format = "| %-6s | %-32s | %10s | %8s | %12s |\n";
        String separator = "+--------+----------------------------------+------------+----------+--------------+";

        System.out.println("\n" + separator);
        System.out.printf(format, "PID", "Product Name", "Unit Price", "Quantity", "Subtotal ($)");
        System.out.println(separator);

        for (CartItem item : items) {
            String name = item.getProductName() != null ? item.getProductName() : "Product #" + item.getProductId();
            if (name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            System.out.printf(format,
                    item.getProductId(),
                    name,
                    String.format("$%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    String.format("$%.2f", item.getSubtotal()));
        }
        System.out.println(separator);
        System.out.printf("| %-62s | %12s |\n", "TOTAL ESTIMATED AMOUNT", String.format("$%.2f", total != null ? total : BigDecimal.ZERO));
        System.out.println("+-----------------------------------------------------------------+--------------+");
    }

    public static void printOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("\n  [i] No past orders found for this customer.");
            return;
        }

        String format = "| %-8s | %-12s | %-15s | %-22s |\n";
        String separator = "+----------+--------------+-----------------+------------------------+";

        System.out.println(separator);
        System.out.printf(format, "Order ID", "Total ($)", "Status", "Order Date");
        System.out.println(separator);

        for (Order o : orders) {
            System.out.printf(format,
                    "#" + o.getOrderId(),
                    String.format("$%.2f", o.getTotalAmount()),
                    o.getOrderStatus(),
                    o.getOrderDate() != null ? o.getOrderDate().toString() : "N/A");
        }
        System.out.println(separator);
    }

    public static void printOrderInvoice(Order order) {
        if (order == null) return;
        System.out.println("\n============================================================");
        System.out.println("                 OFFICIAL ORDER INVOICE                     ");
        System.out.println("============================================================");
        System.out.println(" Order ID:      #" + order.getOrderId());
        System.out.println(" Customer ID:   #" + order.getCustomerId());
        System.out.println(" Status:        " + order.getOrderStatus());
        System.out.println(" Placed Date:   " + (order.getOrderDate() != null ? order.getOrderDate() : "Just now"));
        System.out.println("------------------------------------------------------------");

        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            String format = "| %-6s | %-28s | %6s | %10s | %10s |\n";
            String sep = "+--------+------------------------------+--------+------------+------------+";
            System.out.println(sep);
            System.out.printf(format, "PID", "Item Name", "Qty", "Price", "Subtotal");
            System.out.println(sep);
            for (OrderItem oi : items) {
                String name = oi.getProductName() != null ? oi.getProductName() : "Product #" + oi.getProductId();
                if (name.length() > 26) {
                    name = name.substring(0, 23) + "...";
                }
                System.out.printf(format,
                        oi.getProductId(),
                        name,
                        oi.getQuantity(),
                        String.format("$%.2f", oi.getPrice()),
                        String.format("$%.2f", oi.getSubtotal()));
            }
            System.out.println(sep);
        }

        System.out.println(" TOTAL BILLED:  $" + String.format("%.2f", order.getTotalAmount()));
        System.out.println("============================================================\n");
    }
}
