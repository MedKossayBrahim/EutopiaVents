package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        user = new User(1, "John Doe", "test@example.com", "password123", "johndoe", "profile.jpg", 1234567890, true, Role.Participant);
        System.out.println("Created test user with ID: " + user.getUserID());
    }

    @Test
    void testConstructorWithId() {
        System.out.println("\n=== Testing Constructor with ID ===");
        System.out.println("Verifying ID");
        assertEquals(1, user.getUserID());
        System.out.println("Verifying full name");
        assertEquals("John Doe", user.getFullname());
        System.out.println("Verifying email");
        assertEquals("test@example.com", user.getEmail());
        System.out.println("Verifying password");
        assertEquals("password123", user.getPasswd());
        System.out.println("Verifying username");
        assertEquals("johndoe", user.getUserName());
        System.out.println("Verifying image");
        assertEquals("profile.jpg", user.getImage());
        System.out.println("Verifying phone");
        assertEquals(1234567890, user.getPhone());
        System.out.println("Verifying active status");
        assertTrue(user.getActive());
        System.out.println("Verifying role");
        assertEquals(Role.Participant, user.getRole());
        System.out.println("Constructor with ID test passed!");
    }

    @Test
    void testConstructorWithoutId() {
        System.out.println("\n=== Testing Constructor without ID ===");
        System.out.println("Creating new user without ID");
        User user2 = new User("Jane Smith", "janesmith", "jane@example.com", "newpass123", 987654321);
        System.out.println("Verifying full name");
        assertEquals("Jane Smith", user2.getFullname());
        System.out.println("Verifying username");
        assertEquals("janesmith", user2.getUserName());
        System.out.println("Verifying email");
        assertEquals("jane@example.com", user2.getEmail());
        System.out.println("Verifying password");
        assertEquals("newpass123", user2.getPasswd());
        System.out.println("Verifying phone");
        assertEquals(987654321, user2.getPhone());
        System.out.println("Verifying active status");
        assertTrue(user2.getActive());
        System.out.println("Verifying default image");
        assertEquals("http://localhost/img/default.png", user2.getImage());
        System.out.println("Constructor without ID test passed!");
    }

    @Test
    void testGettersAndSetters() {
        System.out.println("\n=== Testing Getters and Setters ===");
        System.out.println("Setting new values for all fields");
        user.setUserID(2);
        user.setFullname("Updated Name");
        user.setEmail("updated@example.com");
        user.setPasswd("newpass123");
        user.setUserName("updateduser");
        user.setImage("newprofile.jpg");
        user.setPhone(987654321);
        user.setActive(false);
        user.setRole(Role.Admin);

        System.out.println("Verifying ID was updated");
        assertEquals(2, user.getUserID());
        System.out.println("Verifying full name was updated");
        assertEquals("Updated Name", user.getFullname());
        System.out.println("Verifying email was updated");
        assertEquals("updated@example.com", user.getEmail());
        System.out.println("Verifying password was updated");
        assertEquals("newpass123", user.getPasswd());
        System.out.println("Verifying username was updated");
        assertEquals("updateduser", user.getUserName());
        System.out.println("Verifying image was updated");
        assertEquals("newprofile.jpg", user.getImage());
        System.out.println("Verifying phone was updated");
        assertEquals(987654321, user.getPhone());
        System.out.println("Verifying active status was updated");
        assertFalse(user.getActive());
        System.out.println("Verifying role was updated");
        assertEquals(Role.Admin, user.getRole());
        System.out.println("Getters and setters test passed!");
    }

    @Test
    void testToString() {
        System.out.println("\n=== Testing ToString ===");
        String expected = "User{userID=1, nom='John Doe', email='test@example.com', " +
                         "passwd='password123', userName='johndoe', image='profile.jpg', " +
                         "phone=1234567890, isActive=true, role=Participant}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + user.toString());
        assertEquals(expected, user.toString());
        System.out.println("ToString test passed!");
    }
} 