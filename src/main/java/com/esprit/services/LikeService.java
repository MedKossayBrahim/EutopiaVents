package com.esprit.services;

import com.esprit.models.Like;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LikeService implements IService<Like> {
    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Like like) {
        String sql = "INSERT INTO likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, like.getPostId());
            pstmt.setLong(2, like.getUserId());
            pstmt.executeUpdate();
            System.out.println("Like added successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Like like) {
    }

    @Override
    public void supprimer(Like like) {
        String sql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, like.getPostId());
            pstmt.setLong(2, like.getUserId());
            pstmt.executeUpdate();
            System.out.println("Like removed successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Like> rechercher() {
        List<Like> likes = new ArrayList<>();
        String sql = "SELECT post_id, COUNT(*) as like_count FROM likes GROUP BY post_id ORDER BY created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Like like = new Like(
                    rs.getInt("post_id"),
                    rs.getInt("like_count")
                );
                likes.add(like);
            }
            System.out.println("\nAll likes:");
            System.out.println(likes);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return likes;
    }

} 