package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.esprit.utils.DataSource;

class UserTest {
    private User user;
    private Connection connection;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        user = new User(100, "John Doe", "test@example.com", "password123", "johndoe", "profile.jpg", 1234567890, true, Role.Participant);
        System.out.println("Created test user with ID: " + user.getUserID());
        
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

    @Test
    void testDatabaseOperations() {
        System.out.println("\n=== Testing Database Operations ===");
        
        try {
            // First, ensure the test record doesn't exist
            System.out.println("Cleaning up any existing test record...");
            String cleanupSQL = "DELETE FROM users WHERE userID = ?";
            PreparedStatement cleanupStmt = connection.prepareStatement(cleanupSQL);
            cleanupStmt.setInt(1, user.getUserID());
            int deleted = cleanupStmt.executeUpdate();
            System.out.println("Deleted " + deleted + " existing records");
            cleanupStmt.close();
            
            // Test adding to database
            System.out.println("Testing database insertion...");
            String insertSQL = "INSERT INTO users (userID, fullname, email, password, userName, image, phone, isActive, role) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement insertStmt = connection.prepareStatement(insertSQL);
            insertStmt.setInt(1, user.getUserID());
            insertStmt.setString(2, user.getFullname());
            insertStmt.setString(3, user.getEmail());
            insertStmt.setString(4, user.getPasswd());
            insertStmt.setString(5, user.getUserName());
            insertStmt.setString(6, user.getImage());
            insertStmt.setInt(7, user.getPhone());
            insertStmt.setBoolean(8, user.getActive());
            insertStmt.setString(9, user.getRole().toString());
            
            System.out.println("Executing insert with values: id=" + user.getUserID() + 
                             ", fullname=" + user.getFullname() + 
                             ", email=" + user.getEmail() + 
                             ", password=" + user.getPasswd() + 
                             ", username=" + user.getUserName() + 
                             ", image=" + user.getImage() + 
                             ", phone=" + user.getPhone() + 
                             ", isActive=" + user.getActive() + 
                             ", role=" + user.getRole());
            
            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("Inserted " + rowsAffected + " row(s) into database");
            assertEquals(1, rowsAffected, "Should insert exactly one row");
            insertStmt.close();
            
            // Verify the insertion
            System.out.println("Verifying inserted data...");
            String selectSQL = "SELECT * FROM users WHERE userID = ?";
            PreparedStatement selectStmt = connection.prepareStatement(selectSQL);
            selectStmt.setInt(1, user.getUserID());
            System.out.println("Executing select query for id=" + user.getUserID());
            
            ResultSet rs = selectStmt.executeQuery();
            boolean found = rs.next();
            System.out.println("Query result found: " + found);
            
            if (found) {
                System.out.println("Found record with values:");
                System.out.println("userID: " + rs.getInt("userID"));
                System.out.println("fullname: " + rs.getString("fullname"));
                System.out.println("email: " + rs.getString("email"));
                System.out.println("password: " + rs.getString("password"));
                System.out.println("userName: " + rs.getString("userName"));
                System.out.println("image: " + rs.getString("image"));
                System.out.println("phone: " + rs.getInt("phone"));
                System.out.println("isActive: " + rs.getBoolean("isActive"));
                System.out.println("role: " + rs.getString("role"));
            }
            
            assertTrue(found, "Should find the inserted record");
            assertEquals(user.getFullname(), rs.getString("fullname"));
            assertEquals(user.getEmail(), rs.getString("email"));
            assertEquals(user.getPasswd(), rs.getString("password"));
            assertEquals(user.getUserName(), rs.getString("userName"));
            assertEquals(user.getImage(), rs.getString("image"));
            assertEquals(user.getPhone(), rs.getInt("phone"));
            assertEquals(user.getActive(), rs.getBoolean("isActive"));
            assertEquals(user.getRole().toString(), rs.getString("role"));
            System.out.println("Data verification successful");
            rs.close();
            
            // Test deleting from database
            System.out.println("Testing database deletion...");
            String deleteSQL = "DELETE FROM users WHERE userID = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL);
            deleteStmt.setInt(1, user.getUserID());
            
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