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
    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Category category) {
        String sql = "INSERT INTO categoriesposts (name, description, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.executeUpdate();
            System.out.println("Category added successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Category category) {
        String sql = "UPDATE categoriesposts SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getId());
            pstmt.executeUpdate();
            System.out.println("Category updated successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Category category) {
        String sql = "DELETE FROM categoriesposts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, category.getId());
            pstmt.executeUpdate();
                System.out.println("Category deleted successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Category> rechercher() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT name, description, created_at FROM categoriesposts ORDER BY created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getString("name"),
                    rs.getString("description")
                );
                categories.add(category);
            }
            System.out.println("\nAll categories:");
            System.out.println(categories);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }
} 