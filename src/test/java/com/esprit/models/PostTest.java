package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.esprit.utils.DataSource;
import com.esprit.services.PostService;

class PostTest {
    private Post post;
    private LocalDateTime now;
    private Connection connection;
    private PostService postService;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        now = LocalDateTime.now();
        // Using category_id = 8 which exists in categoriesposts table
        post = new Post(100, "Test Title", "Test Content", "Test Author", 8);
        post.setId(100); // Explicitly set the ID
        post.setUserId(7); // Set the user ID to the existing user
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        System.out.println("Created test post with ID: " + post.getId());
        
        try {
            connection = DataSource.getInstance().getConnection();
            postService = new PostService();
            System.out.println("Database connection established successfully");
            
            // Get the actual username for user ID 7
            String usernameSQL = "SELECT userName FROM users WHERE userID = ?";
            PreparedStatement stmt = connection.prepareStatement(usernameSQL);
            stmt.setInt(1, post.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String actualUsername = rs.getString("userName");
                post.setAuthor(actualUsername); // Update the author to match the actual username
                System.out.println("Set author to actual username: " + actualUsername);
            }
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            fail("Database connection failed");
        }
    }

    @Test
    void testDefaultConstructor() {
        System.out.println("\n=== Testing Default Constructor ===");
        System.out.println("Creating new Post with default constructor");
        Post defaultPost = new Post();
        System.out.println("Verifying post is not null");
        assertNotNull(defaultPost);
        System.out.println("Verifying createdAt is not null");
        assertNotNull(defaultPost.getCreatedAt());
        System.out.println("Verifying updatedAt is not null");
        assertNotNull(defaultPost.getUpdatedAt());
        System.out.println("Default constructor test passed!");
    }

    @Test
    void testConstructorWithBasicFields() {
        System.out.println("\n=== Testing Constructor with Basic Fields ===");
        System.out.println("Creating new Post with basic fields");
        Post basicPost = new Post("Basic Title", "Basic Content", "Basic Author");
        System.out.println("Verifying title");
        assertEquals("Basic Title", basicPost.getTitle());
        System.out.println("Verifying content");
        assertEquals("Basic Content", basicPost.getContent());
        System.out.println("Verifying author");
        assertEquals("Basic Author", basicPost.getAuthor());
        System.out.println("Basic fields constructor test passed!");
    }

    @Test
    void testConstructorWithAllFields() {
        System.out.println("\n=== Testing Constructor with All Fields ===");
        System.out.println("Verifying ID (should be 100 as set in constructor)");
        assertEquals(100, post.getId());
        System.out.println("Verifying title");
        assertEquals("Test Title", post.getTitle());
        System.out.println("Verifying content");
        assertEquals("Test Content", post.getContent());
        
        // Get the actual username for user ID 7
        try {
            String usernameSQL = "SELECT userName FROM users WHERE userID = ?";
            PreparedStatement stmt = connection.prepareStatement(usernameSQL);
            stmt.setInt(1, post.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String actualUsername = rs.getString("userName");
                System.out.println("Verifying author (should match username from users table)");
                assertEquals(actualUsername, post.getAuthor(), "Author should match the username from users table");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            fail("Failed to verify author: " + e.getMessage());
        }
        
        System.out.println("Verifying category ID");
        assertEquals(8, post.getCategoryId());
        System.out.println("Verifying isPinned is false");
        assertFalse(post.isPinned());
        System.out.println("All fields constructor test passed!");
    }

    @Test
    void testGettersAndSetters() {
        System.out.println("\n=== Testing Getters and Setters ===");
        System.out.println("Setting new values for all fields");
        post.setId(2);
        post.setTitle("Updated Title");
        post.setContent("Updated Content");
        post.setAuthor("Updated Author");
        post.setPinned(true);
        post.setCategoryId(2);
        post.setCategory("Test Category");

        System.out.println("Verifying ID was updated");
        assertEquals(2, post.getId());
        System.out.println("Verifying title was updated");
        assertEquals("Updated Title", post.getTitle());
        System.out.println("Verifying content was updated");
        assertEquals("Updated Content", post.getContent());
        System.out.println("Verifying author was updated");
        assertEquals("Updated Author", post.getAuthor());
        System.out.println("Verifying isPinned was updated");
        assertTrue(post.isPinned());
        System.out.println("Verifying categoryId was updated");
        assertEquals(2, post.getCategoryId());
        System.out.println("Verifying category was updated");
        assertEquals("Test Category", post.getCategory());
        System.out.println("Getters and setters test passed!");
    }

    @Test
    void testEqualsAndHashCode() {
        System.out.println("\n=== Testing Equals and HashCode ===");
        System.out.println("Creating two identical posts");
        Post post1 = new Post(100, "Test Title", "Test Content", "Test Author", 8);
        Post post2 = new Post(100, "Test Title", "Test Content", "Test Author", 8);
        System.out.println("Creating a different post");
        Post post3 = new Post(101, "Different Title", "Different Content", "Different Author", 8);

        System.out.println("Verifying equals for identical posts");
        assertTrue(post1.equals(post2));
        System.out.println("Verifying equals for different posts");
        assertFalse(post1.equals(post3));
        System.out.println("Verifying hashCode for identical posts");
        assertEquals(post1.hashCode(), post2.hashCode());
        System.out.println("Verifying hashCode for different posts");
        assertNotEquals(post1.hashCode(), post3.hashCode());
        System.out.println("Equals and hashCode test passed!");
    }

    @Test
    void testToString() {
        System.out.println("\n=== Testing ToString ===");
        String expected = "Post{title='Test Title', content='Test Content', createdAt=" + post.getCreatedAt() + "}";
        System.out.println("Expected toString output: " + expected);
        System.out.println("Actual toString output: " + post.toString());
        assertEquals(expected, post.toString());
        System.out.println("ToString test passed!");
    }

    @Test
    void testDatabaseOperations() {
        System.out.println("\n=== Testing Database Operations ===");
        
        try {
            // First, ensure the test record doesn't exist
            System.out.println("Cleaning up any existing test record...");
            postService.supprimer(post);
            System.out.println("Cleaned up any existing test record");
            
            // Test adding to database
            System.out.println("Testing database insertion...");
            System.out.println("Executing insert with values: id=" + post.getId() + 
                             ", title=" + post.getTitle() + 
                             ", content=" + post.getContent() + 
                             ", author=" + post.getAuthor() + 
                             ", category_id=" + post.getCategoryId() +
                             ", user_id=" + post.getUserId());
            
            postService.ajouter(post);
            System.out.println("Inserted post into database");
            
            // Verify the insertion
            System.out.println("Verifying inserted data...");
            Post retrievedPost = postService.getPostById(post.getId());
            System.out.println("Retrieved post from database");
            
            assertNotNull(retrievedPost, "Should find the inserted record");
            assertEquals(post.getTitle(), retrievedPost.getTitle());
            assertEquals(post.getContent(), retrievedPost.getContent());
            assertEquals(post.getAuthor(), retrievedPost.getAuthor(), "Author should match the username from users table");
            assertEquals(post.getCategoryId(), retrievedPost.getCategoryId());
            assertEquals(post.isPinned(), retrievedPost.isPinned());
            assertEquals(post.getUserId(), retrievedPost.getUserId());
            System.out.println("Data verification successful");
            
            // Test deleting from database
            System.out.println("Testing database deletion...");
            postService.supprimer(post);
            System.out.println("Deleted post from database");
            
            // Verify the deletion
            System.out.println("Verifying deletion...");
            retrievedPost = postService.getPostById(post.getId());
            assertNull(retrievedPost, "Should not find the deleted record");
            System.out.println("Deletion verification successful");
            
        } catch (SQLException e) {
            System.out.println("Database operation failed: " + e.getMessage());
            e.printStackTrace();
            fail("Database operation test failed");
        }
        
        System.out.println("Database operations test completed successfully!");
    }
} 