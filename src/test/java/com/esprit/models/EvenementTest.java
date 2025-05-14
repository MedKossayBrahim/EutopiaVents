package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EvenementTest {
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        evenement = new Evenement(1, "Test Event", "Test Description", "2024-03-20", "2024-03-21", 
                                100, 1, 1, 1, 99.99, "ACTIF", "Test Owner", "test.jpg");
        System.out.println("Created test event with ID: " + evenement.getId());
    }

    @Test
    void testConstructorWithAllFields() {
        System.out.println("\n=== Testing Constructor with All Fields ===");
        System.out.println("Verifying ID");
        assertEquals(1, evenement.getId());
        System.out.println("Verifying title");
        assertEquals("Test Event", evenement.getTitre());
        System.out.println("Verifying description");
        assertEquals("Test Description", evenement.getDescription());
        System.out.println("Verifying start date");
        assertEquals("2024-03-20", evenement.getDateDebut());
        System.out.println("Verifying end date");
        assertEquals("2024-03-21", evenement.getDateFin());
        System.out.println("Verifying capacity");
        assertEquals(100, evenement.getCapacite());
        System.out.println("Verifying category ID");
        assertEquals(1, evenement.getCategorieId());
        System.out.println("Verifying location ID");
        assertEquals(1, evenement.getLieuId());
        System.out.println("Verifying organizer ID");
        assertEquals(1, evenement.getOrganisateurId());
        System.out.println("Verifying price");
        assertEquals(99.99, evenement.getPrix());
        System.out.println("Verifying status");
        assertEquals("ACTIF", evenement.getStatut());
        System.out.println("Verifying location owner");
        assertEquals("Test Owner", evenement.getLieu_proprietaire());
        System.out.println("Verifying image");
        assertEquals("test.jpg", evenement.getImage());
        System.out.println("Constructor with all fields test passed!");
    }

    @Test
    void testConstructorWithoutLieuProprietaire() {
        System.out.println("\n=== Testing Constructor without Location Owner ===");
        System.out.println("Creating new event without location owner");
        Evenement evenement2 = new Evenement(2, "New Event", "New Description", "2024-04-01", 
                                           "2024-04-02", 50, 2, 2, 2, 149.99, "PLANIFIE");
        System.out.println("Verifying ID");
        assertEquals(2, evenement2.getId());
        System.out.println("Verifying title");
        assertEquals("New Event", evenement2.getTitre());
        System.out.println("Verifying description");
        assertEquals("New Description", evenement2.getDescription());
        System.out.println("Verifying start date");
        assertEquals("2024-04-01", evenement2.getDateDebut());
        System.out.println("Verifying end date");
        assertEquals("2024-04-02", evenement2.getDateFin());
        System.out.println("Verifying capacity");
        assertEquals(50, evenement2.getCapacite());
        System.out.println("Verifying category ID");
        assertEquals(2, evenement2.getCategorieId());
        System.out.println("Verifying location ID");
        assertEquals(2, evenement2.getLieuId());
        System.out.println("Verifying organizer ID");
        assertEquals(2, evenement2.getOrganisateurId());
        System.out.println("Verifying price");
        assertEquals(149.99, evenement2.getPrix());
        System.out.println("Verifying status");
        assertEquals("PLANIFIE", evenement2.getStatut());
        System.out.println("Constructor without location owner test passed!");
    }

    @Test
    void testGettersAndSetters() {
        System.out.println("\n=== Testing Getters and Setters ===");
        System.out.println("Setting new values for all fields");
        evenement.setId(3);
        evenement.setTitre("Updated Event");
        evenement.setDescription("Updated Description");
        evenement.setDateDebut("2024-05-01");
        evenement.setDateFin("2024-05-02");
        evenement.setCapacite(200);
        evenement.setCategorieId(3);
        evenement.setLieuId(3);
        evenement.setOrganisateurId(3);
        evenement.setPrix(199.99);
        evenement.setStatut("TERMINE");
        evenement.setLieu_proprietaire("Updated Owner");
        evenement.setImage("updated.jpg");
        evenement.setOrganisateurNom("John Doe");
        evenement.setCategorieNom("Conference");
        evenement.setLieuNom("Main Hall");

        System.out.println("Verifying ID was updated");
        assertEquals(3, evenement.getId());
        System.out.println("Verifying title was updated");
        assertEquals("Updated Event", evenement.getTitre());
        System.out.println("Verifying description was updated");
        assertEquals("Updated Description", evenement.getDescription());
        System.out.println("Verifying start date was updated");
        assertEquals("2024-05-01", evenement.getDateDebut());
        System.out.println("Verifying end date was updated");
        assertEquals("2024-05-02", evenement.getDateFin());
        System.out.println("Verifying capacity was updated");
        assertEquals(200, evenement.getCapacite());
        System.out.println("Verifying category ID was updated");
        assertEquals(3, evenement.getCategorieId());
        System.out.println("Verifying location ID was updated");
        assertEquals(3, evenement.getLieuId());
        System.out.println("Verifying organizer ID was updated");
        assertEquals(3, evenement.getOrganisateurId());
        System.out.println("Verifying price was updated");
        assertEquals(199.99, evenement.getPrix());
        System.out.println("Verifying status was updated");
        assertEquals("TERMINE", evenement.getStatut());
        System.out.println("Verifying location owner was updated");
        assertEquals("Updated Owner", evenement.getLieu_proprietaire());
        System.out.println("Verifying image was updated");
        assertEquals("updated.jpg", evenement.getImage());
        System.out.println("Verifying organizer name was updated");
        assertEquals("John Doe", evenement.getOrganisateurNom());
        System.out.println("Verifying category name was updated");
        assertEquals("Conference", evenement.getCategorieNom());
        System.out.println("Verifying location name was updated");
        assertEquals("Main Hall", evenement.getLieuNom());
        System.out.println("Getters and setters test passed!");
    }

    @Test
    void testToString() {
        System.out.println("\n=== Testing ToString ===");
        String expected = "Evenement{id=1, titre='Test Event', description='Test Description', " +
                         "dateDebut='2024-03-20', dateFin='2024-03-21', capacite=100, " +
                         "prix=99.99, statut='ACTIF', image='test.jpg'}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + evenement.toString());
        assertEquals(expected, evenement.toString());
        System.out.println("ToString test passed!");
    }

    @Test
    void testInvalidConstructor() {
        System.out.println("\n=== Testing Invalid Constructor ===");
        System.out.println("Attempting to create event with invalid location parameters");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Evenement(4, "Invalid Event", "Invalid Description", "2024-06-01", 
                         "2024-06-02", 50, 4, 0, 4, 99.99, "ACTIF", null, null);
        });
        System.out.println("Verifying exception message");
        assertEquals("L'événement doit avoir un lieu existant ou un lieu propriétaire.", 
                    exception.getMessage());
        System.out.println("Invalid constructor test passed!");
    }
} 