package com.ecommerce.dao;

import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Product entity operations.
 */
public interface ProductDAO {
    int insertProduct(Product product) throws SQLException;
    Optional<Product> findById(int productId) throws SQLException;
    List<Product> findAll() throws SQLException;
    List<Product> findByName(String nameKeyword) throws SQLException;
    List<Product> findByCategory(String category) throws SQLException;
    List<String> findAllCategories() throws SQLException;
    boolean updateProduct(Product product) throws SQLException;
    boolean deleteProduct(int productId) throws SQLException;
    int getStock(int productId) throws SQLException;
    boolean deductStock(Connection conn, int productId, int quantity) throws SQLException;
}
