package com.esprit.services;

import com.esprit.models.EventReview;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventReviewService implements IService<EventReview> {

    private Connection connection;

    public EventReviewService() throws SQLException {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(EventReview review) {
        String req = "INSERT INTO eventreviews (evenement_id, utilisateur_id, note, commentaire, date_creation) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, review.getEvenementId());
            pst.setInt(2, review.getUtilisateurId());
            pst.setInt(3, review.getNote());
            pst.setString(4, review.getCommentaire());
            pst.setTimestamp(5, Timestamp.valueOf(review.getDateCreation()));

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        review.setId(generatedKeys.getInt(1));
                        System.out.println("Review ajoutée avec ID: " + review.getId());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la review: " + e.getMessage());
        }
    }

    @Override
    public void modifier(EventReview review) {
        String req = "UPDATE eventreviews SET evenement_id=?, utilisateur_id=?, note=?, commentaire=?, date_creation=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, review.getEvenementId());
            pst.setInt(2, review.getUtilisateurId());
            pst.setInt(3, review.getNote());
            pst.setString(4, review.getCommentaire());
            pst.setTimestamp(5, Timestamp.valueOf(review.getDateCreation()));
            pst.setInt(6, review.getId());

            pst.executeUpdate();
            System.out.println("Review modifiée avec succès");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la review: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(EventReview review) {
        String req = "DELETE FROM eventreviews WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, review.getId());
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Review supprimée");
            } else {
                System.out.println("Aucune review supprimée. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la review: " + e.getMessage());
        }
    }

    @Override
    public List<EventReview> rechercher() {
        List<EventReview> reviews = new ArrayList<>();
        String req = "SELECT r.*, u.userName as nom_utilisateur, e.titre as titre_evenement " +
                "FROM eventreviews r " +
                "JOIN users u ON r.utilisateur_id = u.userID " +
                "JOIN events e ON r.evenement_id = e.id " +
                "ORDER BY r.date_creation DESC";

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                EventReview review = new EventReview(
                        rs.getInt("id"),
                        rs.getInt("evenement_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getInt("note"),
                        rs.getString("commentaire"),
                        rs.getTimestamp("date_creation").toLocalDateTime()
                );
                review.setNomUtilisateur(rs.getString("nom_utilisateur"));
                review.setTitreEvenement(rs.getString("titre_evenement"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des reviews: " + e.getMessage());
        }
        return reviews;
    }

    public List<EventReview> rechercherParEvenement(int evenementId) {
        List<EventReview> reviews = new ArrayList<>();
        String req = "SELECT r.*, u.userName as nom_utilisateur, e.titre as titre_evenement " +
                "FROM eventreviews r " +
                "JOIN users u ON r.utilisateur_id = u.userID " +
                "JOIN events e ON r.evenement_id = e.id " +
                "WHERE r.evenement_id = ? " +
                "ORDER BY r.date_creation DESC";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenementId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    EventReview review = new EventReview(
                            rs.getInt("id"),
                            rs.getInt("evenement_id"),
                            rs.getInt("utilisateur_id"),
                            rs.getInt("note"),
                            rs.getString("commentaire"),
                            rs.getTimestamp("date_creation").toLocalDateTime()
                    );
                    review.setNomUtilisateur(rs.getString("nom_utilisateur"));
                    review.setTitreEvenement(rs.getString("titre_evenement"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des reviews par événement: " + e.getMessage());
        }
        return reviews;
    }

    public List<EventReview> rechercherParUtilisateur(int utilisateurId) {
        List<EventReview> reviews = new ArrayList<>();
        String req = "SELECT r.*, u.userName as nom_utilisateur, e.titre as titre_evenement " +
                "FROM eventreviews r " +
                "JOIN users u ON r.utilisateur_id = u.userID " +
                "JOIN events e ON r.evenement_id = e.id " +
                "WHERE r.utilisateur_id = ? " +
                "ORDER BY r.date_creation DESC";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, utilisateurId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    EventReview review = new EventReview(
                            rs.getInt("id"),
                            rs.getInt("evenement_id"),
                            rs.getInt("utilisateur_id"),
                            rs.getInt("note"),
                            rs.getString("commentaire"),
                            rs.getTimestamp("date_creation").toLocalDateTime()
                    );
                    review.setNomUtilisateur(rs.getString("nom_utilisateur"));
                    review.setTitreEvenement(rs.getString("titre_evenement"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des reviews par utilisateur: " + e.getMessage());
        }
        return reviews;
    }

    public double getMoyenneNotesEvenement(int evenementId) {
        String req = "SELECT AVG(note) as moyenne FROM eventreviews WHERE evenement_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenementId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul de la moyenne des notes: " + e.getMessage());
        }
        return 0;
    }

    public boolean utilisateurADejaEvalue(int utilisateurId, int evenementId) {
        String req = "SELECT COUNT(*) as count FROM eventreviews WHERE utilisateur_id = ? AND evenement_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, utilisateurId);
            pst.setInt(2, evenementId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification si l'utilisateur a déjà évalué: " + e.getMessage());
        }
        return false;
    }
}