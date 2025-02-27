package com.esprit.services;

import com.esprit.models.Feedback;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackService {
    private Connection connection = DataSource.getInstance().getConnection();

    public void ajouter(Feedback feedback) {
        try {
            String req = "INSERT INTO feedback (user, materiel_id, contenu) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, feedback.getUserId());
            ps.setInt(2, feedback.getMaterielId());
            ps.setString(3, feedback.getContenu());
            ps.executeUpdate();
            System.out.println("Feedback ajouté avec succès");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du feedback : " + e.getMessage());
        }
    }

    public List<Feedback> getFeedbacksByMateriel(int materielId) {
        List<Feedback> feedbacks = new ArrayList<>();
        try {
            String req = "SELECT f.*, u.fullName FROM feedback f " +
                        "JOIN users u ON f.user = u.userID " +
                        "WHERE f.materiel_id = ? " +
                        "ORDER BY f.id DESC";
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, materielId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Feedback feedback = new Feedback(
                    rs.getInt("id"),
                    rs.getInt("user"),
                    rs.getInt("materiel_id"),
                    rs.getString("contenu")
                );
                feedback.setUserName(rs.getString("fullName"));
                feedbacks.add(feedback);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des feedbacks : " + e.getMessage());
        }
        return feedbacks;
    }
} 