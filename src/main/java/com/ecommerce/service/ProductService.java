package com.ecommerce.service;

import com.ecommerce.exception.InvalidInputException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Business Service interface for Product operations.
 */
public interface ProductService {
    Product addProduct(String name, String category, BigDecimal price, int stock, String description)
            throws InvalidInputException;

    Product getProductById(int productId) throws ProductNotFoundException;

    List<Product> getAllProducts();

    List<Product> searchProductsByName(String nameKeyword);

    List<Product> searchProductsByCategory(String category);

    Set<String> getAllCategories();

    boolean updateProduct(int productId, String name, String category, BigDecimal price, int stock, String description)
            throws ProductNotFoundException, InvalidInputException;

    boolean deleteProduct(int productId) throws ProductNotFoundException;

    int checkStock(int productId) throws ProductNotFoundException;
}
