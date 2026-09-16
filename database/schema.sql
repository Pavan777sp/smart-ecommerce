-- ====================================================================
-- SMART E-COMMERCE BACKEND ENGINE - DATABASE SCHEMA
-- Normalized 3NF Relational Database Schema for MySQL 8+
-- ====================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS smart_ecommerce
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_ecommerce;

-- 2. Drop existing tables in reverse dependency order (clean install)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;

-- --------------------------------------------------------------------
-- Table: customers
-- Description: Stores registered customer account details
-- --------------------------------------------------------------------
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_name_not_empty CHECK (CHAR_LENGTH(TRIM(name)) > 0)
) ENGINE=InnoDB;

-- Index for customer email lookups and authentication
CREATE INDEX idx_customer_email ON customers(email);

-- --------------------------------------------------------------------
-- Table: products
-- Description: Stores catalog items across varied categories
-- --------------------------------------------------------------------
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price_positive CHECK (price > 0.00),
    CONSTRAINT chk_product_stock_non_negative CHECK (stock_quantity >= 0),
    CONSTRAINT chk_product_name_not_empty CHECK (CHAR_LENGTH(TRIM(product_name)) > 0)
) ENGINE=InnoDB;

-- Indexes for frequently searched product fields
CREATE INDEX idx_product_name ON products(product_name);
CREATE INDEX idx_product_category ON products(category);
CREATE INDEX idx_product_price ON products(price);

-- --------------------------------------------------------------------
-- Table: carts
-- Description: Represents an active shopping cart (1-to-1 with customer)
-- --------------------------------------------------------------------
CREATE TABLE carts (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_cart_customer ON carts(customer_id);

-- --------------------------------------------------------------------
-- Table: cart_items
-- Description: Items currently placed in a customer's shopping cart
-- --------------------------------------------------------------------
CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_cartitem_quantity_positive CHECK (quantity > 0),
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cartitems_cart FOREIGN KEY (cart_id)
        REFERENCES carts(cart_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_cartitems_product FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_cartitem_cart ON cart_items(cart_id);
CREATE INDEX idx_cartitem_product ON cart_items(product_id);

-- --------------------------------------------------------------------
-- Table: orders
-- Description: Placed customer orders with aggregate total amount
-- --------------------------------------------------------------------
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_total_non_negative CHECK (total_amount >= 0.00),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_date ON orders(order_date);

-- --------------------------------------------------------------------
-- Table: order_items
-- Description: Line items for an order, preserving historical unit price
-- --------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_orderitem_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_orderitem_price_non_negative CHECK (price >= 0.00),
    CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_orderitems_product FOREIGN KEY (product_id)
        REFERENCES products(product_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_orderitem_order ON order_items(order_id);
CREATE INDEX idx_orderitem_product ON order_items(product_id);
