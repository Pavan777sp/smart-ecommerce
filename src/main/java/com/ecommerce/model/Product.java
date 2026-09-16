package com.ecommerce.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Represents a product in the e-commerce catalog.
 */
public class Product {
    private int productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private String description;
    private Timestamp createdAt;

    public Product() {
    }

    public Product(int productId, String productName, String category, BigDecimal price, int stockQuantity, String description, Timestamp createdAt) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Product(String productName, String category, BigDecimal price, int stockQuantity, String description) {
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + productId +
                ", name='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stock=" + stockQuantity +
                '}';
    }
}
