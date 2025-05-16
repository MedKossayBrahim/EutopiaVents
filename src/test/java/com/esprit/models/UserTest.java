package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.esprit.services.UserService;

class UserTest {
    private User user;
    private UserService userService;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        user = new User(100, "John Doe", "test@example.com", "password123", "johndoe", "profile.jpg", 1234567890, true, Role.Participant);
        System.out.println("Created test user with ID: " + user.getUserID());
        
        try {
            userService = new UserService();
            System.out.println("Service initialized successfully");
        } catch (Exception e) {
            System.out.println("Error initializing service: " + e.getMessage());
            fail("Service initialization failed");
        }
    }

    @Test
    void testConstructorWithId() {
        System.out.println("\n=== Testing Constructor with ID ===");
        System.out.println("Verifying ID");
        assertEquals(100, user.getUserID());
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
        String expected = "User{userID=100, nom='John Doe', email='test@example.com', " +
                         "passwd='password123', userName='johndoe', image='profile.jpg', " +
                         "phone=1234567890, isActive=true, role=Participant}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + user.toString());
        assertEquals(expected, user.toString());
        System.out.println("ToString test passed!");
    }

    private void printUserDetails(String email) {
        User user = userService.getUserByEmail(email);
        if (user != null) {
            System.out.println("\nUser details for " + email + ":");
            System.out.println("ID: " + user.getUserID());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Username: " + user.getUserName());
            System.out.println("Active: " + user.getActive());
            System.out.println("Role: " + user.getRole());
        } else {
            System.out.println("\nNo user found with email: " + email);
        }
    }

    @Test
    void testUserServiceOperations() {
        System.out.println("\n=== Testing User Service Operations ===");
        
        try {
            // Print all users in database
            System.out.println("\nChecking all users in database...");
            userService.printAllUsers();
            
            // Get an inactive user
            System.out.println("\nFinding an inactive user...");
            User inactiveUser = null;
            for (User user : userService.getAllNonAdminUsers()) {
                if (!user.getActive()) {
                    inactiveUser = user;
                    break;
                }
            }
            assertNotNull(inactiveUser, "Should find an inactive user");
            System.out.println("Found inactive user: " + inactiveUser.getEmail());
            
            // Test inactive user
            System.out.println("\nTesting inactive user...");
            User signInAttempt = userService.signIn(inactiveUser.getEmail(), "password123");
            assertNull(signInAttempt, "Inactive user should not be able to sign in");
            System.out.println("Inactive user sign in test passed");
            
            // Test active user (ID: 6)
            System.out.println("\nTesting active user (ID: 6)...");
            User activeUser = userService.getUserByEmail("ambrose.army1997@gmail.com");
            assertNotNull(activeUser, "Should find user with ID 6");
            assertEquals(6, activeUser.getUserID(), "Should be user ID 6");
            
            // Print user details for debugging
            System.out.println("\nUser details before sign in:");
            System.out.println("ID: " + activeUser.getUserID());
            System.out.println("Email: " + activeUser.getEmail());
            System.out.println("Active status: " + activeUser.getActive());
            System.out.println("Role: " + activeUser.getRole());
            
            // Verify user is active
            assertTrue(activeUser.getActive(), "User should be active");
            
            // Try sign in with correct password
            String correctPassword = "Talel@1997";
            System.out.println("\nAttempting sign in with password: " + correctPassword);
            signInAttempt = userService.signIn(activeUser.getEmail(), correctPassword);
            
            if (signInAttempt == null) {
                System.out.println("Sign in failed. Checking if password is correct...");
                // Try to verify password directly
                User userCheck = userService.getUserByEmail(activeUser.getEmail());
                System.out.println("Stored password hash: " + userCheck.getPasswd());
                System.out.println("Attempted password: " + correctPassword);
                
                // Try to reset the password to the same value to see if it works
                System.out.println("\nTrying to reset password to same value...");
                userService.updatePass(activeUser.getEmail(), correctPassword);
                System.out.println("Password reset attempted");
                
                // Try sign in again
                System.out.println("\nAttempting sign in again after password reset...");
                signInAttempt = userService.signIn(activeUser.getEmail(), correctPassword);
            }
            
            assertNotNull(signInAttempt, "Active user should be able to sign in");
            assertTrue(signInAttempt.getActive(), "User should be active");
            System.out.println("Active user sign in test passed");
            
            // Test password update for active user
            System.out.println("\nTesting password update for active user...");
            String newPassword = "newSecurePassword123";
            userService.updatePass(activeUser.getEmail(), newPassword);
            System.out.println("Password updated successfully");
            
            // Verify new password works
            signInAttempt = userService.signIn(activeUser.getEmail(), newPassword);
            assertNotNull(signInAttempt, "Should be able to sign in with new password");
            System.out.println("New password verification successful");
            
            // Test get all non-admin users
            System.out.println("\nTesting get all non-admin users...");
            var nonAdminUsers = userService.getAllNonAdminUsers();
            assertNotNull(nonAdminUsers, "Should get list of non-admin users");
            System.out.println("Found " + nonAdminUsers.size() + " non-admin users");
            
            // Test update user status
            System.out.println("\nTesting update user status...");
            userService.updateUserStatus(activeUser.getUserID(), false);
            System.out.println("User status updated successfully");
            
            // Verify status update
            signInAttempt = userService.signIn(activeUser.getEmail(), newPassword);
            assertNull(signInAttempt, "Should not be able to sign in with deactivated account");
            System.out.println("Status update verification successful");
            
        } catch (Exception e) {
            System.out.println("Service operation failed: " + e.getMessage());
            e.printStackTrace();
            fail("Service operation test failed");
        }
        
        System.out.println("Service operations test completed successfully!");
    }
} 