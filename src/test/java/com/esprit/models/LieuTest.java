package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.esprit.services.LieuServiceImpl;
import java.sql.SQLException;

class LieuTest {
    private Lieu lieu;
    private categorie_salle testCategorie;
    private LieuServiceImpl lieuService;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        testCategorie = new categorie_salle(1, "Test Category");
        lieu = new Lieu(100, "Test Venue", "123 Test Street", "Test City", "12345", 100, "test.jpg", testCategorie, 500.0);
        System.out.println("Created test location with ID: " + lieu.getId());
        
        try {
            lieuService = new LieuServiceImpl();
            System.out.println("Database connection established successfully");
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            fail("Database connection failed");
        }
    }

    @Test
    void testConstructorWithAllFields() {
        System.out.println("\n=== Testing Constructor with all fields ===");
        System.out.println("Verifying ID");
        assertEquals(100, lieu.getId());
        System.out.println("Verifying name");
        assertEquals("Test Venue", lieu.getNom());
        System.out.println("Verifying address");
        assertEquals("123 Test Street", lieu.getAdresse());
        System.out.println("Verifying city");
        assertEquals("Test City", lieu.getVille());
        System.out.println("Verifying postal code");
        assertEquals("12345", lieu.getCodePostal());
        System.out.println("Verifying capacite");
        assertEquals(100, lieu.getCapacite());
        System.out.println("Verifying image");
        assertEquals("test.jpg", lieu.getImage());
        System.out.println("Verifying categorie");
        assertEquals(testCategorie, lieu.getCategorie());
        System.out.println("Verifying prix");
        assertEquals(500.0, lieu.getPrix());
        System.out.println("Constructor with all fields test passed!");
    }

    @Test
    void testConstructorWithBasicFields() {
        System.out.println("\n=== Testing Constructor with basic fields ===");
        System.out.println("Creating new location without ID");
        Lieu basicLieu = new Lieu("Basic Venue", "Basic City", 50);
        System.out.println("Verifying name");
        assertEquals("Basic Venue", basicLieu.getNom());
        System.out.println("Verifying city");
        assertEquals("Basic City", basicLieu.getVille());
        System.out.println("Verifying capacite");
        assertEquals(50, basicLieu.getCapacite());
        System.out.println("Verifying prix");
        assertEquals(0.0, basicLieu.getPrix());
        System.out.println("Constructor with basic fields test passed!");
    }

    @Test
    void testGettersAndSetters() {
        System.out.println("\n=== Testing Getters and Setters ===");
        System.out.println("Setting new values for all fields");
        categorie_salle newCategorie = new categorie_salle(2, "New Category");
        
        lieu.setId(2);
        lieu.setNom("Updated Venue");
        lieu.setAdresse("456 New Street");
        lieu.setVille("New City");
        lieu.setCodePostal("54321");
        lieu.setCapacite(200);
        lieu.setImage("updated.jpg");
        lieu.setCategorie(newCategorie);
        lieu.setPrix(750.0);

        System.out.println("Verifying ID was updated");
        assertEquals(2, lieu.getId());
        System.out.println("Verifying name was updated");
        assertEquals("Updated Venue", lieu.getNom());
        System.out.println("Verifying address was updated");
        assertEquals("456 New Street", lieu.getAdresse());
        System.out.println("Verifying city was updated");
        assertEquals("New City", lieu.getVille());
        System.out.println("Verifying postal code was updated");
        assertEquals("54321", lieu.getCodePostal());
        System.out.println("Verifying capacite was updated");
        assertEquals(200, lieu.getCapacite());
        System.out.println("Verifying image was updated");
        assertEquals("updated.jpg", lieu.getImage());
        System.out.println("Verifying categorie was updated");
        assertEquals(newCategorie, lieu.getCategorie());
        System.out.println("Verifying prix was updated");
        assertEquals(750.0, lieu.getPrix());
        System.out.println("Getters and setters test passed!");
    }

    @Test
    void testToString() {
        System.out.println("\n=== Testing ToString ===");
        String expected = "Lieu{id=100, nom='Test Venue', ville='Test City', capacite=100, prix=500.0}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + lieu.toString());
        assertEquals(expected, lieu.toString());
        System.out.println("ToString test passed!");
    }

    @Test
    void testDatabaseOperations() {
        System.out.println("\n=== Testing Database Operations ===");
        
        // First, ensure the test record doesn't exist
        System.out.println("Cleaning up any existing test record...");
        lieuService.supprimer(lieu);
        System.out.println("Cleaned up any existing test record");
        
        // Test adding to database
        System.out.println("Testing database insertion...");
        System.out.println("Executing insert with values: id=" + lieu.getId() + 
                         ", nom=" + lieu.getNom() + 
                         ", adresse=" + lieu.getAdresse() + 
                         ", ville=" + lieu.getVille() +
                         ", code_postal=" + lieu.getCodePostal() +
                         ", capacite=" + lieu.getCapacite() +
                         ", image=" + lieu.getImage() +
                         ", categorie_id=" + lieu.getCategorie().getId() +
                         ", prix=" + lieu.getPrix());
        
        lieuService.ajouter(lieu);
        System.out.println("Inserted location into database");
        
        // Verify the insertion
        System.out.println("Verifying inserted data...");
        Lieu retrievedLieu = lieuService.getLieuById(lieu.getId());
        System.out.println("Retrieved location from database");
        
        assertNotNull(retrievedLieu, "Should find the inserted record");
        assertEquals(lieu.getNom(), retrievedLieu.getNom());
        assertEquals(lieu.getAdresse(), retrievedLieu.getAdresse());
        assertEquals(lieu.getVille(), retrievedLieu.getVille());
        assertEquals(lieu.getCodePostal(), retrievedLieu.getCodePostal());
        assertEquals(lieu.getCapacite(), retrievedLieu.getCapacite());
        assertEquals(lieu.getImage(), retrievedLieu.getImage());
        assertEquals(lieu.getCategorie().getId(), retrievedLieu.getCategorie().getId());
        assertEquals(lieu.getPrix(), retrievedLieu.getPrix());
        System.out.println("Data verification successful");
        
        // Test deleting from database
        System.out.println("Testing database deletion...");
        lieuService.supprimer(lieu);
        System.out.println("Deleted location from database");
        
        // Verify the deletion
        System.out.println("Verifying deletion...");
        retrievedLieu = lieuService.getLieuById(lieu.getId());
        assertNull(retrievedLieu, "Should not find the deleted record");
        System.out.println("Deletion verification successful");
        
        System.out.println("Database operations test completed successfully!");
    }
} 