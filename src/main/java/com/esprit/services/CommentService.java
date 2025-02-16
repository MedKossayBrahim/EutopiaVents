package com.esprit.services;

import com.esprit.models.Comment;
import com.esprit.utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class CommentService implements IServiceF<Comment> {
    
    @Override
    public void ajouter(Comment comment) throws SQLException {
        String query = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
        
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pst.setInt(1, comment.getPostId());
            pst.setInt(2, comment.getUserId());
            pst.setString(3, comment.getContent());
            
            pst.executeUpdate();
            
            // Get the generated ID and created_at timestamp
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                    
                    // Get the created_at timestamp from the database
                    String timeQuery = "SELECT created_at FROM comments WHERE id = ?";
                    try (PreparedStatement timePst = conn.prepareStatement(timeQuery)) {
                        timePst.setInt(1, comment.getId());
                        ResultSet timeRs = timePst.executeQuery();
                        if (timeRs.next()) {
                            comment.setCreatedAt(timeRs.getTimestamp("created_at").toLocalDateTime());
                        }
                    }
                }
            }
            
            // Get the username for the comment
            String userQuery = "SELECT userName FROM users WHERE userID = ?";
            try (PreparedStatement userPst = conn.prepareStatement(userQuery)) {
                userPst.setInt(1, comment.getUserId());
                ResultSet rs = userPst.executeQuery();
                if (rs.next()) {
                    comment.setUsername(rs.getString("userName"));
                }
            }
        }
    }

    @Override
    public void modifier(Comment comment) throws SQLException {
        String query = "UPDATE comments SET content = ? WHERE id = ?";
        
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            
            pst.setString(1, comment.getContent());
            pst.setInt(2, comment.getId());
            pst.executeUpdate();
        }
    }

    public void supprimer(int commentId, int userId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(Comment comment) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Comment> rechercher() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT c.*, u.username FROM comments c " +
                    "LEFT JOIN users u ON c.user_id = u.id " +
                    "ORDER BY c.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setPostId(rs.getInt("post_id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setContent(rs.getString("content"));
                comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                comment.setUsername(rs.getString("username"));
                comments.add(comment);
            }
        }
        return comments;
    }

    public List<Comment> getCommentsForPost(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.userName as username " +
                       "FROM comments c " +
                       "LEFT JOIN users u ON c.user_id = u.userID " +
                       "WHERE c.post_id = ? " +
                       "ORDER BY c.created_at DESC";
        
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, postId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getInt("post_id"),
                    rs.getInt("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                comment.setUsername(rs.getString("username"));
                comments.add(comment);
            }
        }
        
        return comments;
    }

    public Comment getCommentById(int commentId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT c.*, u.username FROM comments c " +
                    "LEFT JOIN users u ON c.user_id = u.id " +
                    "WHERE c.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    comment.setUsername(rs.getString("username"));
                    return comment;
                }
            }
        }
        return null;
    }
}

