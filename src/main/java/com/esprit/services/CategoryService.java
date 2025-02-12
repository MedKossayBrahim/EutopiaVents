package com.esprit.services;

import com.esprit.models.Category;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryService implements IService<Category> {

    @Override
    public void ajouter(Category category) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "INSERT INTO categoriesposts (name, description, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.executeUpdate();
            System.out.println("Category added successfully");
        }
    }

    @Override
    public void modifier(Category category) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "UPDATE categoriesposts SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            pstmt.executeUpdate();
            System.out.println("Category updated successfully");
        }
    }

    @Override
    public void supprimer(Category category) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "DELETE FROM categoriesposts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, category.getId());
            pstmt.executeUpdate();
            System.out.println("Category deleted successfully");
        }
    }

    @Override
    public List<Category> rechercher() throws SQLException {
        List<Category> categories = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT id, name, description, created_at FROM categoriesposts ORDER BY created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")
                );
                category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                categories.add(category);
            }
        }
        return categories;
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT id, name, description, created_at FROM categoriesposts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    );
                    category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return category;
                }
            }
        }
        return null;
    }

    public List<Category> searchCategories(String searchText) throws SQLException {
        List<Category> categories = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT * FROM categoriesposts WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                    );
                    category.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    categories.add(category);
                }
            }
        }
        return categories;
    }
} 