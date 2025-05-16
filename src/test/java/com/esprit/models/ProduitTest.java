package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.esprit.utils.DataSource;

class ProduitTest {
    private produit produit;
    private Connection connection;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        produit = new produit(100, "Test Product", "Test Description", 99.99, 10, 1, "test.jpg");
        System.out.println("Created test product with ID: " + produit.getId());
        
        try {
            connection = DataSource.getInstance().getConnection();
            System.out.println("Database connection established successfully");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            fail("Database connection failed");
        }
    }

    @Test
    void testConstructorWithId() {
        System.out.println("\n=== Testing Constructor with ID ===");
        System.out.println("Verifying ID");
        assertEquals(100, produit.getId());
        System.out.println("Verifying name");
        assertEquals("Test Product", produit.getNom());
        System.out.println("Verifying description");
        assertEquals("Test Description", produit.getDescription());
        System.out.println("Verifying price");
        assertEquals(99.99, produit.getPrix());
        System.out.println("Verifying stock");
        assertEquals(10, produit.getStock());
        System.out.println("Verifying category ID");
        assertEquals(1, produit.getCategorieId());
        System.out.println("Verifying image URL");
        assertEquals("test.jpg", produit.getImageUrl());
        System.out.println("Constructor with ID test passed!");
    }

    @Test
    void testConstructorWithoutId() {
        System.out.println("\n=== Testing Constructor without ID ===");
        System.out.println("Creating new product without ID");
        produit produit2 = new produit("New Product", "New Description", 149.99, 5, 2, "new.jpg");
        System.out.println("Verifying name");
        assertEquals("New Product", produit2.getNom());
        System.out.println("Verifying description");
        assertEquals("New Description", produit2.getDescription());
        System.out.println("Verifying price");
        assertEquals(149.99, produit2.getPrix());
        System.out.println("Verifying stock");
        assertEquals(5, produit2.getStock());
        System.out.println("Verifying category ID");
        assertEquals(2, produit2.getCategorieId());
        System.out.println("Verifying image URL");
        assertEquals("new.jpg", produit2.getImageUrl());
        System.out.println("Constructor without ID test passed!");
    }

    @Test
    void testGettersAndSetters() {
        System.out.println("\n=== Testing Getters and Setters ===");
        System.out.println("Setting new values for all fields");
        produit.setId(2);
        produit.setNom("Updated Product");
        produit.setDescription("Updated Description");
        produit.setPrix(199.99);
        produit.setStock(20);
        produit.setCategorieId(3);
        produit.setImageUrl("updated.jpg");

        System.out.println("Verifying ID was updated");
        assertEquals(2, produit.getId());
        System.out.println("Verifying name was updated");
        assertEquals("Updated Product", produit.getNom());
        System.out.println("Verifying description was updated");
        assertEquals("Updated Description", produit.getDescription());
        System.out.println("Verifying price was updated");
        assertEquals(199.99, produit.getPrix());
        System.out.println("Verifying stock was updated");
        assertEquals(20, produit.getStock());
        System.out.println("Verifying category ID was updated");
        assertEquals(3, produit.getCategorieId());
        System.out.println("Verifying image URL was updated");
        assertEquals("updated.jpg", produit.getImageUrl());
        System.out.println("Getters and setters test passed!");
    }

    @Test
    void testToString() {
        System.out.println("\n=== Testing ToString ===");
        String expected = "Produit{id=100, nom='Test Product', description='Test Description', " +
                         "prix=99.99, stock=10, categorieId=1, imageUrl='test.jpg'}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + produit.toString());
        assertEquals(expected, produit.toString());
        System.out.println("ToString test passed!");
    }

    @Test
    void testDatabaseOperations() {
        System.out.println("\n=== Testing Database Operations ===");
        
        try {
            // First, ensure the test record doesn't exist
            System.out.println("Cleaning up any existing test record...");
            String cleanupSQL = "DELETE FROM produit WHERE id = ?";
            PreparedStatement cleanupStmt = connection.prepareStatement(cleanupSQL);
            cleanupStmt.setInt(1, produit.getId());
            int deleted = cleanupStmt.executeUpdate();
            System.out.println("Deleted " + deleted + " existing records");
            cleanupStmt.close();
            
            // Verify category exists before inserting
            System.out.println("Verifying category exists...");
            String categoryCheckSQL = "SELECT id FROM categorie_produit WHERE id = ?";
            PreparedStatement categoryCheckStmt = connection.prepareStatement(categoryCheckSQL);
            categoryCheckStmt.setInt(1, produit.getCategorieId());
            ResultSet categoryRs = categoryCheckStmt.executeQuery();
            assertTrue(categoryRs.next(), "Category ID " + produit.getCategorieId() + " should exist in categorie_produit table");
            categoryCheckStmt.close();
            categoryRs.close();
            
            // Test adding to database
            System.out.println("Testing database insertion...");
            String insertSQL = "INSERT INTO produit (id, nom, description, prix, stock, categorie_produit_id, image_url, password) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setInt(1, produit.getId());
            insertStmt.setString(2, produit.getNom());
            insertStmt.setString(3, produit.getDescription());
            insertStmt.setDouble(4, produit.getPrix());
            insertStmt.setInt(5, produit.getStock());
            insertStmt.setInt(6, produit.getCategorieId());
            insertStmt.setString(7, produit.getImageUrl());
            insertStmt.setString(8, "test_password"); // Adding a test password
            
            System.out.println("Executing insert with values: id=" + produit.getId() + 
                             ", name=" + produit.getNom() + 
                             ", description=" + produit.getDescription() + 
                             ", price=" + produit.getPrix() + 
                             ", stock=" + produit.getStock() + 
                             ", category_id=" + produit.getCategorieId() + 
                             ", image_url=" + produit.getImageUrl() +
                             ", password=test_password");
            
            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("Inserted " + rowsAffected + " row(s) into database");
            assertEquals(1, rowsAffected, "Should insert exactly one row");
            insertStmt.close();
            
            // Verify the insertion
            System.out.println("Verifying inserted data...");
            String selectSQL = "SELECT * FROM produit WHERE id = ?";
            PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
            selectStmt.setInt(1, produit.getId());
            System.out.println("Executing select query for id=" + produit.getId());
            
            ResultSet rs = selectStmt.executeQuery();
            boolean found = rs.next();
            System.out.println("Query result found: " + found);
            
            if (found) {
                System.out.println("Found record with values:");
                System.out.println("id: " + rs.getInt("id"));
                System.out.println("nom: " + rs.getString("nom"));
                System.out.println("description: " + rs.getString("description"));
                System.out.println("prix: " + rs.getDouble("prix"));
                System.out.println("stock: " + rs.getInt("stock"));
                System.out.println("categorie_produit_id: " + rs.getInt("categorie_produit_id"));
                System.out.println("image_url: " + rs.getString("image_url"));
                System.out.println("password: " + rs.getString("password"));
            }
            
            assertTrue(found, "Should find the inserted record");
            assertEquals(produit.getNom(), rs.getString("nom"));
            assertEquals(produit.getDescription(), rs.getString("description"));
            assertEquals(produit.getPrix(), rs.getDouble("prix"));
            assertEquals(produit.getStock(), rs.getInt("stock"));
            assertEquals(produit.getCategorieId(), rs.getInt("categorie_produit_id"));
            assertEquals(produit.getImageUrl(), rs.getString("image_url"));
            assertEquals("test_password", rs.getString("password"), "Password should match");
            System.out.println("Data verification successful");
            rs.close();
            
            // Test deleting from database
            System.out.println("Testing database deletion...");
            String deleteSQL = "DELETE FROM produit WHERE id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL);
            deleteStmt.setInt(1, produit.getId());
            
            rowsAffected = deleteStmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " row(s) from database");
            assertEquals(1, rowsAffected, "Should delete exactly one row");
            deleteStmt.close();
            
            // Verify the deletion
            System.out.println("Verifying deletion...");
            rs = selectStmt.executeQuery();
            assertFalse(rs.next(), "Should not find the deleted record");
            System.out.println("Deletion verification successful");
            
            // Clean up
            selectStmt.close();
            rs.close();
            
        } catch (SQLException e) {
            System.out.println("Database operation failed: " + e.getMessage());
            e.printStackTrace();
            fail("Database operation test failed");
        }
        
        System.out.println("Database operations test completed successfully!");
    }
} 