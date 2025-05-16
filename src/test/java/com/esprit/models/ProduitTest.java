package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.esprit.services.ProduitService;

class ProduitTest {
    private produit produit;
    private ProduitService produitService;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        produit = new produit(100, "Test Product", "Test Description", 99.99, 10, 1, "test.jpg");
        System.out.println("Created test product with ID: " + produit.getId());
        
        produitService = new ProduitService();
        System.out.println("Service initialized successfully");
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
        
        // First, ensure the test record doesn't exist
        System.out.println("Cleaning up any existing test record...");
        produitService.supprimer(produit);
        System.out.println("Cleaned up any existing test record");
        
        // Test adding to database
        System.out.println("Testing database insertion...");
        System.out.println("Executing insert with values: id=" + produit.getId() + 
                         ", name=" + produit.getNom() + 
                         ", description=" + produit.getDescription() + 
                         ", price=" + produit.getPrix() + 
                         ", stock=" + produit.getStock() + 
                         ", category_id=" + produit.getCategorieId() + 
                         ", image_url=" + produit.getImageUrl());
        
        produitService.ajouter(produit);
        System.out.println("Inserted product into database");
        
        // Verify the insertion
        System.out.println("Verifying inserted data...");
        produit retrievedProduit = produitService.getOne(produit.getId());
        System.out.println("Retrieved product from database");
        
        assertNotNull(retrievedProduit, "Should find the inserted record");
        assertEquals(produit.getNom(), retrievedProduit.getNom());
        assertEquals(produit.getDescription(), retrievedProduit.getDescription());
        assertEquals(produit.getPrix(), retrievedProduit.getPrix());
        assertEquals(produit.getStock(), retrievedProduit.getStock());
        assertEquals(produit.getCategorieId(), retrievedProduit.getCategorieId());
        assertEquals(produit.getImageUrl(), retrievedProduit.getImageUrl());
        System.out.println("Data verification successful");
        
        // Test deleting from database
        System.out.println("Testing database deletion...");
        produitService.supprimer(produit);
        System.out.println("Deleted product from database");
        
        // Verify the deletion
        System.out.println("Verifying deletion...");
        retrievedProduit = produitService.getOne(produit.getId());
        assertNull(retrievedProduit, "Should not find the deleted record");
        System.out.println("Deletion verification successful");
        
        System.out.println("Database operations test completed successfully!");
    }
} 