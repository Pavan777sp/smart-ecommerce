package com.ecommerce.service.impl;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.exception.DatabaseException;
import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import com.ecommerce.util.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementation of ProductService with input validation and business rules.
 */
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public Product addProduct(String name, String category, BigDecimal price, int stock, String description)
            throws InvalidInputException {

        InputValidator.validateNonEmpty(name, "Product name");
        InputValidator.validateNonEmpty(category, "Category");
        InputValidator.validatePositivePrice(price);
        InputValidator.validateNonNegativeStock(stock);

        Product product = new Product(name.trim(), category.trim(), price, stock, description != null ? description.trim() : "");
        try {
            int generatedId = productDAO.insertProduct(product);
            if (generatedId > 0) {
                product.setProductId(generatedId);
                return product;
            } else {
                throw new DatabaseException("Failed to insert product into database.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while adding product: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(int productId) throws ProductNotFoundException {
        try {
            return productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " was not found."));
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while fetching product: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> getAllProducts() {
        try {
            return productDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while retrieving all products: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> searchProductsByName(String nameKeyword) {
        if (nameKeyword == null || nameKeyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return productDAO.findByName(nameKeyword.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while searching products by name: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Product> searchProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return productDAO.findByCategory(category.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while searching products by category: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getAllCategories() {
        try {
            List<String> list = productDAO.findAllCategories();
            return new TreeSet<>(list); // Sorted unique set of categories
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while fetching categories: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProduct(int productId, String name, String category, BigDecimal price, int stock, String description)
            throws ProductNotFoundException, InvalidInputException {

        // Check existence
        getProductById(productId);

        InputValidator.validateNonEmpty(name, "Product name");
        InputValidator.validateNonEmpty(category, "Category");
        InputValidator.validatePositivePrice(price);
        InputValidator.validateNonNegativeStock(stock);

        Product product = new Product(productId, name.trim(), category.trim(), price, stock, description != null ? description.trim() : "", null);
        try {
            return productDAO.updateProduct(product);
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws ProductNotFoundException {
        getProductById(productId);
        try {
            return productDAO.deleteProduct(productId);
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while deleting product (it may be linked to existing orders): " + e.getMessage(), e);
        }
    }

    @Override
    public int checkStock(int productId) throws ProductNotFoundException {
        try {
            int stock = productDAO.getStock(productId);
            if (stock < 0) {
                throw new ProductNotFoundException("Product with ID " + productId + " was not found.");
            }
            return stock;
        } catch (SQLException e) {
            throw new DatabaseException("Database error occurred while checking product stock: " + e.getMessage(), e);
        }
    }
}
