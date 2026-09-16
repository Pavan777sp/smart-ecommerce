package com.ecommerce.dao.impl;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.model.Product;
import com.ecommerce.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of ProductDAO.
 */
public class ProductDAOImpl implements ProductDAO {

    private static final String SELECT_COLUMNS = "product_id, product_name, category, price, stock_quantity, description, created_at";

    @Override
    public int insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, price, stock_quantity, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setString(5, product.getDescription());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    product.setProductId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    @Override
    public Optional<Product> findById(int productId) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM products ORDER BY product_id ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> findByName(String nameKeyword) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM products WHERE LOWER(product_name) LIKE LOWER(?) ORDER BY product_name ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nameKeyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public List<Product> findByCategory(String category) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM products WHERE LOWER(category) = LOWER(?) ORDER BY product_name ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public List<String> findAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM products ORDER BY category ASC";
        List<String> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, category = ?, price = ?, stock_quantity = ?, description = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setString(5, product.getDescription());
            ps.setInt(6, product.getProductId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getStock(int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_quantity");
                }
            }
        }
        return -1;
    }

    @Override
    public boolean deductStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getBigDecimal("price"),
                rs.getInt("stock_quantity"),
                rs.getString("description"),
                rs.getTimestamp("created_at")
        );
    }
}
