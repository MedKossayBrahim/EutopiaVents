package com.esprit.services;

import com.esprit.models.Like;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LikeService implements IServiceF<Like> {

    @Override
    public void ajouter(Like like) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "INSERT INTO likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, like.getPostId());
            pstmt.setInt(2, like.getUserId());
            pstmt.executeUpdate();
            System.out.println("Like added successfully");
        }
    }

    @Override
    public void modifier(Like like) throws SQLException {
        // Likes typically don't need modification
    }

    @Override
    public void supprimer(Like like) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, like.getPostId());
            pstmt.setInt(2, like.getUserId());
            pstmt.executeUpdate();
            System.out.println("Like removed successfully");
        }
    }

    @Override
    public List<Like> rechercher() throws SQLException {
        List<Like> likes = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT post_id, COUNT(*) as like_count FROM likes GROUP BY post_id ORDER BY created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Like like = new Like(
                    rs.getInt("post_id"),
                    rs.getInt("like_count")
                );
                likes.add(like);
            }
        }
        return likes;
    }

    public void ajouter(int postId, int userId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "INSERT INTO likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void supprimer(int postId, int userId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public boolean isPostLikedByUser(int postId, int userId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ? AND user_id = ?";
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

    public int getLikesCount(int postId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Like> getLikesForPost(int postId) throws SQLException {
        List<Like> likes = new ArrayList<>();
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT l.*, u.username FROM likes l " +
                    "LEFT JOIN users u ON l.user_id = u.id " +
                    "WHERE l.post_id = ? " +
                    "ORDER BY l.created_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Like like = new Like(
                        rs.getInt("post_id"),
                        rs.getInt("user_id")
                    );
                    like.setId(rs.getInt("id"));
                    like.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    likes.add(like);
                }
            }
        }
        return likes;
    }

    public Like getLikeById(int likeId) throws SQLException {
        Connection conn = DataSource.getInstance().getConnection();
        String sql = "SELECT l.*, u.username FROM likes l " +
                    "LEFT JOIN users u ON l.user_id = u.id " +
                    "WHERE l.id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, likeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Like like = new Like(
                        rs.getInt("post_id"),
                        rs.getInt("user_id")
                    );
                    like.setId(rs.getInt("id"));
                    like.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return like;
                }
            }
        }
        return null;
    }
} 