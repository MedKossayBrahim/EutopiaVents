package com.esprit.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PostTest {
    private Post post;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Setting up test environment ===");
        now = LocalDateTime.now();
        post = new Post(1, "Test Title", "Test Content", "Test Author", 1);
        System.out.println("Created test post with ID: " + post.getId());
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
        System.out.println("Verifying ID (should be 0 as not set in constructor)");
        assertEquals(0, post.getId());
        System.out.println("Verifying title");
        assertEquals("Test Title", post.getTitle());
        System.out.println("Verifying content");
        assertEquals("Test Content", post.getContent());
        System.out.println("Verifying author");
        assertEquals("Test Author", post.getAuthor());
        System.out.println("Verifying category ID");
        assertEquals(1, post.getCategoryId());
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
        Post post1 = new Post(1, "Test Title", "Test Content", "Test Author", 1);
        Post post2 = new Post(1, "Test Title", "Test Content", "Test Author", 1);
        System.out.println("Creating a different post");
        Post post3 = new Post(2, "Different Title", "Different Content", "Different Author", 2);

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
} 