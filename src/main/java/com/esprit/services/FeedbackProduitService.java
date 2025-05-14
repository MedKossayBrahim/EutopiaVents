package com.esprit.services;

import com.esprit.models.FeedbackProduit;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackProduitService {

    public void ajouter(FeedbackProduit feedback) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "INSERT INTO feedbackProduit (user_id, produit_id, comment, rating, date_created) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, feedback.getUserId());
            ps.setInt(2, feedback.getProduitId());
            ps.setString(3, feedback.getComment());
            ps.setInt(4, feedback.getRating());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            System.out.println("Product feedback added successfully");
        } catch (SQLException e) {
            System.err.println("Error adding product feedback: " + e.getMessage());
        }
    }

    public List<FeedbackProduit> getFeedbacksByProduit(int produitId) {
        List<FeedbackProduit> feedbacks = new ArrayList<>();
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "SELECT f.*, u.fullname FROM feedbackProduit f " +
                    "JOIN users u ON f.user_id = u.userID " +
                    "WHERE f.produit_id = ? " +
                    "ORDER BY f.date_created DESC";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, produitId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                FeedbackProduit feedback = new FeedbackProduit(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("produit_id"),
                        rs.getString("comment"),
                        rs.getInt("rating")
                );
                feedback.setUserName(rs.getString("fullname"));
                Timestamp dateCreated = rs.getTimestamp("date_created");
                if (dateCreated != null) {
                    feedback.setDateCreated(dateCreated.toLocalDateTime());
                }
                feedbacks.add(feedback);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product feedbacks: " + e.getMessage());
        }
        return feedbacks;
    }

    public void supprimer(int id) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "DELETE FROM feedbackProduit WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Product feedback deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting product feedback: " + e.getMessage());
        }
    }

    public void modifier(FeedbackProduit feedback) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "UPDATE feedbackProduit SET comment = ?, rating = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, feedback.getComment());
            ps.setInt(2, feedback.getRating());
            ps.setInt(3, feedback.getId());
            ps.executeUpdate();
            System.out.println("Product feedback updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating product feedback: " + e.getMessage());
        }
    }

    public double getMoyenneRating(int produitId) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "SELECT AVG(rating) as moyenne FROM feedbackProduit WHERE produit_id = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, produitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("moyenne");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0;
    }
    
    public boolean userHasFeedback(int userId, int produitId) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "SELECT COUNT(*) as count FROM feedbackProduit WHERE user_id = ? AND produit_id = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setInt(2, produitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user has feedback: " + e.getMessage());
        }
        return false;
    }
    
    public FeedbackProduit getUserFeedback(int userId, int produitId) {
        try (Connection connection = DataSource.getInstance().getConnection()) {
            String req = "SELECT f.*, u.fullname FROM feedbackProduit f " +
                    "JOIN users u ON f.user_id = u.userID " +
                    "WHERE f.user_id = ? AND f.produit_id = ?";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, userId);
            ps.setInt(2, produitId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                FeedbackProduit feedback = new FeedbackProduit(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("produit_id"),
                        rs.getString("comment"),
                        rs.getInt("rating")
                );
                feedback.setUserName(rs.getString("fullname"));
                Timestamp dateCreated = rs.getTimestamp("date_created");
                if (dateCreated != null) {
                    feedback.setDateCreated(dateCreated.toLocalDateTime());
                }
                return feedback;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user feedback: " + e.getMessage());
        }
        return null;
    }
} 