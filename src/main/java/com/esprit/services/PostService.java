package com.esprit.services;

import com.esprit.models.Post;
import com.esprit.models.Role;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.time.LocalDateTime;

public class PostService implements IServiceF<Post> {
    
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
        String sql = "UPDATE posts SET title=?, content=?, category_id=?, updated_at=NOW() WHERE id=?";
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
                Timestamp createdAt = rs.getTimestamp("created_at");
                post.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
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
        // First check if user is admin
        boolean isAdmin = false;
        try {
//            Path sessionPath = Path.of("user_session.json");
//            JSONParser parser = new JSONParser();
//            JSONObject sessionData = (JSONObject) parser.parse(new FileReader(sessionPath.toFile()));
//            String role = (String) sessionData.get("role");
//            isAdmin = "Admin".equalsIgnoreCase(role);
              isAdmin = Eutopia.getCurrentUser().getRole() == Role.Admin;

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Allow deletion if user is post owner OR admin
        String query = isAdmin ? 
            "DELETE FROM posts WHERE id = ?" :
            "DELETE FROM posts WHERE id = ? AND user_id = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setInt(1, postId);
            if (!isAdmin) {
                pst.setInt(2, userId);
            }
            
            pst.executeUpdate();
        }
    }

    public Post getPostById(int postId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT p.*, u.username FROM posts p LEFT JOIN users u ON p.user_id = u.userID WHERE p.id = ?";
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
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    post.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    post.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : 
                                    (createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now()));
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
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    post.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
                    posts.add(post);
                }
            }
        }
        return posts;
    }

    
    public List<Post> getAllPosts() throws SQLException {
        // First check if the posts table exists
        checkPostsTable();
        
        List<Post> posts = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        
        // Print database connection info
        System.out.println("Database connection: " + (conn != null ? "Successful" : "Failed"));
        
        // Modified query to use explicit column names
        String sql = "SELECT p.id, p.user_id, p.title, p.content, p.created_at, p.updated_at, " +
                    "p.category_id, p.is_pinned, u.username " +
                    "FROM posts p " +
                    "LEFT JOIN users u ON p.user_id = u.userID " +
                    "ORDER BY p.created_at DESC";
        
        System.out.println("Executing SQL: " + sql);
                    
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            // Print column names from metadata
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("ResultSet has " + columnCount + " columns:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Column " + i + ": " + metaData.getColumnName(i) + 
                                  " (" + metaData.getColumnTypeName(i) + ")");
            }
            
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                try {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setUserId(rs.getInt("user_id"));
                    post.setTitle(rs.getString("title"));
                    post.setContent(rs.getString("content"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        post.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    post.setAuthor(rs.getString("username"));
                    post.setCategoryId(rs.getInt("category_id"));
                    post.setPinned(rs.getBoolean("is_pinned"));
                    
                    posts.add(post);
                    
                    // Debug print
                    System.out.println("Loaded post #" + rowCount + ": ID=" + post.getId() + 
                                      ", Title=" + post.getTitle() + 
                                      ", Author=" + post.getAuthor());
                } catch (SQLException e) {
                    System.err.println("Error processing row " + rowCount + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (rowCount == 0) {
                System.out.println("WARNING: Query returned 0 rows!");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            closeConnection(conn);
        }
        
        // Debug print
        System.out.println("Total posts loaded: " + posts.size());
        return posts;
    }

    /**
     * Checks if the posts table exists and has the expected structure
     */
    private void checkPostsTable() {
        Connection conn = null;
        try {
            conn = DataSource.getInstance().getConnection();
            DatabaseMetaData dbMeta = conn.getMetaData();
            
            // Check if posts table exists
            ResultSet tables = dbMeta.getTables(null, null, "posts", null);
            if (!tables.next()) {
                System.err.println("ERROR: 'posts' table does not exist in the database!");
                tables.close();
                return;
            }
            tables.close();
            
            // Check table structure
            ResultSet columns = dbMeta.getColumns(null, null, "posts", null);
            System.out.println("Columns in posts table:");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                System.out.println("  - " + columnName + " (" + dataType + ")");
            }
            columns.close();
            
            // Check if there are any rows in the table
            Statement stmt = conn.createStatement();
            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM posts");
            if (countRs.next()) {
                int count = countRs.getInt(1);
                System.out.println("Total rows in posts table: " + count);
                if (count == 0) {
                    System.out.println("WARNING: posts table is empty!");
                }
            }
            countRs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking posts table: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeConnection(conn);
        }
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
                Timestamp createdAt = rs.getTimestamp("created_at");
                post.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
                
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