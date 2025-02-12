package com.esprit.services;

import com.esprit.models.Comment;
import com.esprit.utils.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class CommentService implements IService<Comment> {
    
    @Override
    public void ajouter(Comment comment) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "INSERT INTO comments (post_id, user_id, content, created_at) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, comment.getPostId());
            pstmt.setInt(2, comment.getUserId());
            pstmt.setString(3, comment.getContent());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void modifier(Comment comment) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "UPDATE comments SET content = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getId());
            pstmt.executeUpdate();
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
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT c.*, u.username FROM comments c " +
                    "LEFT JOIN users u ON c.user_id = u.id " +
                    "WHERE c.post_id = ? " +
                    "ORDER BY c.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
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

