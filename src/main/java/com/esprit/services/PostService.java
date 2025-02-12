package com.esprit.services;

import com.esprit.models.Post;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements IService<Post> {
    
    public PostService() {
        // Empty constructor
    }

    public Connection getConnection() throws SQLException {
        return DataSource.getInstance().getConnection();
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "INSERT INTO posts (user_id, title, content, created_at, category_id, author, is_pinned) VALUES (?, ?, ?, NOW(), ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, post.getUserId());
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getContent());
            pstmt.setInt(4, post.getCategoryId());
            pstmt.setString(5, getUsernameById(post.getUserId()));
            pstmt.setInt(6, post.isPinned() ? 1 : 0);  // Convert boolean to int
            pstmt.executeUpdate();
            
            // Get the generated ID and set it in the post object
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    private String getUsernameById(int userId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT username FROM users WHERE userID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }
        return "Anonymous";
    }

    @Override
    public void modifier(Post post) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "UPDATE posts SET title=?, content=?, category_id=?, updated_at=NOW() WHERE post_id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setInt(3, post.getCategoryId());
            pstmt.setInt(4, post.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(Post post) throws SQLException {
        Connection conn = getConnection();
        String sql = "DELETE FROM posts WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, post.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Post> rechercher() throws SQLException {
        List<Post> posts = new ArrayList<>();
        Connection conn = getConnection();
        String sql = "SELECT p.*, u.username FROM posts p " +
                    "LEFT JOIN users u ON p.user_id = u.id " +
                    "ORDER BY p.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Post post = new Post(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content")
                );
                post.setAuthor(rs.getString("username"));
                post.setUserId(rs.getInt("user_id"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                posts.add(post);
            }
        }
        return posts;
    }

    public boolean isPostOwner(int postId, int userId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT COUNT(*) FROM posts WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void supprimer(int postId, int userId) throws SQLException {
        Connection conn = getConnection();
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public Post getPostById(int postId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT p.*, u.username FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Post post = new Post(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content")
                    );
                    post.setAuthor(rs.getString("username"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    post.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : 
                                    rs.getTimestamp("created_at").toLocalDateTime());
                    return post;
                }
            }
        }
        return null;
    }

    public List<Post> searchPosts(String searchText) throws SQLException {
        Connection conn = getConnection();
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM posts p " +
                    "LEFT JOIN users u ON p.user_id = u.id " +
                    "WHERE LOWER(p.title) LIKE ? OR LOWER(p.content) LIKE ? " +
                    "ORDER BY p.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content")
                    );
                    post.setAuthor(rs.getString("username"));
                    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    posts.add(post);
                }
            }
        }
        return posts;
    }

    
    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT p.*, u.username FROM posts p " +
                    "LEFT JOIN users u ON p.user_id = u.userID " +
                    "ORDER BY p.created_at DESC";
                    
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                post.setAuthor(rs.getString("username"));
                post.setCategoryId(rs.getInt("category_id"));
                posts.add(post);
                
                // Debug print
                System.out.println("Loaded post: " + post.getTitle());
            }
        }
        
        // Debug print
        System.out.println("Total posts loaded: " + posts.size());
        return posts;
    }

    public List<Post> getPinnedPosts() throws SQLException {
        Connection conn = getConnection();
        List<Post> posts = new ArrayList<>();
        String query = "SELECT p.*, u.userName as author_name FROM posts p " +
                      "LEFT JOIN users u ON p.user_id = u.userID " +
                      "WHERE p.is_pinned = true " +
                      "ORDER BY p.created_at DESC";
        
        try (PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setAuthor(rs.getString("author_name"));
                post.setPinned(true);
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    post.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                posts.add(post);
            }
        }
        return posts;
    }
} 