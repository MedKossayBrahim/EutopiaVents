package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProduitTest {
    private produit produit;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        produit = new produit(1, "Test Product", "Test Description", 99.99, 10, 1, "test.jpg");
        System.out.println("Created test product with ID: " + produit.getId());
    }

    @Test
    void testConstructorWithId() {
        System.out.println("\n=== Testing Constructor with ID ===");
        System.out.println("Verifying ID");
        assertEquals(1, produit.getId());
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
        String expected = "Produit{id=1, nom='Test Product', description='Test Description', " +
                         "prix=99.99, stock=10, categorieId=1, imageUrl='test.jpg'}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + produit.toString());
        assertEquals(expected, produit.toString());
        System.out.println("ToString test passed!");
    }
} 