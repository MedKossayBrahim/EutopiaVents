package com.esprit.services;

import com.esprit.models.Post;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostService implements IService<Post> {
    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Post post) {
        String sql = "INSERT INTO posts (user_id, title, content, author, category_id, created_at) VALUES (?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1,post.getUserId());
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getContent());
            pstmt.setString(4, post.getAuthor());
            pstmt.setInt(5, post.getCategoryId());

            pstmt.executeUpdate();
            System.out.println("Post added successfully");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Post post) {
        String sql = "UPDATE posts SET title = ?, content = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setInt(3, post.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Post updated successfully");
            } else {
                System.out.println("Post not found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Post post) {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, post.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Post deleted successfully");
            } else {
                System.out.println("Post not found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Post> rechercher() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.id, p.title, p.content, p.created_at, c.name as category_name " +
                    "FROM posts p " +
                    "LEFT JOIN categoriesposts c ON p.category_id = c.id " +
                    "ORDER BY p.created_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Post post = new Post(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content")
                );
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                posts.add(post);
            }
            System.out.println("\nAll posts:");
            System.out.println(posts);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return posts;
    }
} 