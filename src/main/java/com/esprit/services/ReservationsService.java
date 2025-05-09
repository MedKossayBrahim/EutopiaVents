package com.esprit.services;

import com.esprit.models.Reservations;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationsService {

    Connection connection = DataSource.getInstance().getConnection();

    public ReservationsService() throws SQLException {
    }

    // Méthode pour ajouter une réservation
    public void ajouter(Reservations reservation) {
        // Récupérer le prix de l'événement
        double prixEvenement = getPrixEvenement(reservation.getEvenementId());
        reservation.setPrixTotal(prixEvenement * reservation.getQuantite());

        String req = "INSERT INTO Reservations (evenement_id, utilisateur_id, quantite, prix_total, statut, date_reservation) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, reservation.getEvenementId());
            pst.setInt(2, reservation.getUtilisateurId());
            pst.setInt(3, reservation.getQuantite());
            pst.setDouble(4, reservation.getPrixTotal());
            pst.setString(5, reservation.getStatut());
            pst.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // Date actuelle pour date_reservation

            pst.executeUpdate();

            // Récupérer l'ID généré
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                reservation.setId(generatedKeys.getInt(1));
            }

            System.out.println("Réservation ajoutée avec ID: " + reservation.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }

    // Méthode pour modifier une réservation
    public void modifier(Reservations reservation) {
        // Récupérer le prix de l'événement
        double prixEvenement = getPrixEvenement(reservation.getEvenementId());
        reservation.setPrixTotal(prixEvenement * reservation.getQuantite());

        String req = "UPDATE Reservations SET evenement_id=?, utilisateur_id=?, quantite=?, prix_total=?, statut=? WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, reservation.getEvenementId());
            pst.setInt(2, reservation.getUtilisateurId());
            pst.setInt(3, reservation.getQuantite());
            pst.setDouble(4, reservation.getPrixTotal());
            pst.setString(5, reservation.getStatut());
            pst.setInt(6, reservation.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Réservation modifiée avec succès.");
            } else {
                System.out.println("Aucune réservation trouvée avec l'ID : " + reservation.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la réservation : " + e.getMessage());
        }
    }

    // Méthode pour supprimer une réservation
    public void supprimer(int reservationId) {
        String req = "DELETE FROM Reservations WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, reservationId);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Réservation supprimée avec succès.");
            } else {
                System.out.println("Aucune réservation trouvée avec l'ID : " + reservationId);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la réservation : " + e.getMessage());
        }
    }

    // Méthode pour récupérer toutes les réservations
    public List<Reservations> rechercher() {
        List<Reservations> reservations = new ArrayList<>();
        String req = "SELECT * FROM Reservations";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                reservations.add(new Reservations(
                        rs.getInt("id"),
                        rs.getInt("evenement_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_total"),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
        return reservations;
    }

    // Nouvelle méthode pour récupérer les réservations d'un utilisateur spécifique
    public List<Reservations> rechercherParUtilisateur(int userId) {
        List<Reservations> reservations = new ArrayList<>();
        String req = "SELECT * FROM Reservations WHERE utilisateur_id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                reservations.add(new Reservations(
                        rs.getInt("id"),
                        rs.getInt("evenement_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_total"),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
        return reservations;
    }

    // Méthode pour confirmer l'achat
    public void confirmerAchat(int reservationId, int quantite) {
        // Récupérer la réservation
        Reservations reservation = getReservationById(reservationId);
        if (reservation == null) {
            System.out.println("Réservation non trouvée.");
            return;
        }

        // Vérifier la capacité de l'événement
        if (!verifierCapacite(reservation.getEvenementId(), quantite)) {
            System.out.println("Capacité insuffisante pour cet événement.");
            return;
        }

        // Mettre à jour la quantité et le prix total
        reservation.setQuantite(quantite);
        double prixEvenement = getPrixEvenement(reservation.getEvenementId());
        reservation.setPrixTotal(prixEvenement * quantite);
        reservation.setStatut("confirmé");

        // Mettre à jour la réservation dans la base de données
        modifier(reservation);

        // Décrémenter la capacité de l'événement
        decrementerCapacite(reservation.getEvenementId(), quantite);

        System.out.println("Achat confirmé avec succès.");
    }

    private void decrementerCapacite(int evenementId, int quantite) {
        String req = "UPDATE events SET capacite = capacite - ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, quantite);
            pst.setInt(2, evenementId);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Capacité de l'événement mise à jour avec succès.");
            } else {
                System.out.println("Erreur lors de la mise à jour de la capacité de l'événement.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de la capacité : " + e.getMessage());
        }
    }


    // Méthode pour récupérer une réservation par son ID
    private Reservations getReservationById(int reservationId) {
        String req = "SELECT * FROM Reservations WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, reservationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Reservations(
                        rs.getInt("id"),
                        rs.getInt("evenement_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_total"),
                        rs.getString("statut")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la réservation : " + e.getMessage());
        }
        return null;
    }

    // Méthode pour récupérer le prix d'un événement
    private double getPrixEvenement(int evenementId) {
        String req = "SELECT prix FROM Events WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, evenementId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getDouble("prix");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du prix de l'événement : " + e.getMessage());
        }
        return 0.0;
    }

    // Méthode pour vérifier la capacité de l'événement
    private boolean verifierCapacite(int evenementId, int quantite) {
        String req = "SELECT capacite FROM Events WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, evenementId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int capacite = rs.getInt("capacite");
                return quantite <= capacite;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de la capacité : " + e.getMessage());
        }
        return false;
    }
}