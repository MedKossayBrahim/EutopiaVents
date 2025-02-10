package com.esprit.services;

import com.esprit.models.Comment;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CommentService implements IService<Comment> {
    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Comment comment) {
        String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, comment.getPostId());
            pstmt.setLong(2, comment.getUserId());
            pstmt.setString(3, comment.getContent());
            pstmt.executeUpdate();
            System.out.println("Comment added successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void modifier(Comment comment) {
        String sql = "UPDATE comments SET content = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getId());
            pstmt.executeUpdate();
            System.out.println("Comment updated successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Comment comment) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, comment.getId());
            pstmt.executeUpdate();
            System.out.println("Comment deleted successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Comment> rechercher() {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT content, created_at FROM comments ORDER BY created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getString("content")
                );
                comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                comments.add(comment);
            }
            System.out.println("\nAll comments:");
            System.out.println(comments);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return comments;
    }
}

