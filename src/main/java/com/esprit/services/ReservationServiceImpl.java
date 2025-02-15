package com.esprit.services;

import com.esprit.models.reservation1;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationServiceImpl implements IService<reservation1> {
    private Connection connection;

    public ReservationServiceImpl() {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(reservation1 reservation) {
        String sql = "INSERT INTO reservation1 (evenement_id, lieu_id, date_debut, date_fin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reservation.getIdEvenement());
            pst.setInt(2, reservation.getIdLieu());
            pst.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
            pst.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));

            int rows = pst.executeUpdate();
            if (rows > 0) {
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                    System.out.println("Réservation ajoutée : " + reservation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réservation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(reservation1 reservation) {
        String sql = "UPDATE reservation1 SET evenement_id = ?, lieu_id = ?, date_debut = ?, date_fin = ? WHERE id = ?"; // Correction ici
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, reservation.getIdEvenement());
            pst.setInt(2, reservation.getIdLieu());
            pst.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
            pst.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
            pst.setInt(5, reservation.getId());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Réservation modifiée : " + reservation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(reservation1 reservation) {
        String sql = "DELETE FROM reservation1 WHERE id = ?"; // Correction ici
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, reservation.getId());
            int rows = pst.executeUpdate();
            if (rows > 0) {
                System.out.println("Réservation supprimée : " + reservation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<reservation1> rechercher() {
        List<reservation1> reservations = new ArrayList<>();
        String sql = "SELECT id, evenement_id, lieu_id, date_debut, date_fin FROM reservation1 ORDER BY id"; // Table correcte
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                reservation1 reservation = new reservation1(
                        rs.getInt("id"),
                        rs.getInt("lieu_id"),
                        rs.getInt("evenement_id"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime()
                );
                reservations.add(reservation);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans rechercher() : " + e.getMessage());
            e.printStackTrace();
        }
        return reservations;
    }


    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
