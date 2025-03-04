package com.esprit.services;

import com.esprit.models.reservation1;
import com.esprit.utils.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationServiceImpl implements IService<reservation1> {
    private Connection connection;
    private EvenementService evenementService;

    public ReservationServiceImpl() throws SQLException {
        connection = DataSource.getInstance().getConnection();
        evenementService = new EvenementService();
    }

    @Override
    public void ajouter(reservation1 reservation) {
        try {
            // 1. Vérifier l'absence de chevauchement avec une réservation existante pour ce lieu
            String checkReservationSql = "SELECT COUNT(*) AS count FROM reservation1 " +
                    "WHERE lieu_id = ? AND (? < date_fin AND ? > date_debut)";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkReservationSql)) {
                checkStmt.setInt(1, reservation.getIdLieu());
                checkStmt.setTimestamp(2, Timestamp.valueOf(reservation.getDateDebut()));
                checkStmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateFin()));
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new RuntimeException("Le lieu est déjà réservé pour la période spécifiée.");
                    }
                }
            }

            // 2. Vérifier l'absence de chevauchement avec un événement pour ce lieu
            if (!checkEventAvailability(reservation.getIdLieu(), reservation.getDateDebut(), reservation.getDateFin())) {
                throw new RuntimeException("Le lieu est déjà réservé pour un événement pendant la période spécifiée.");
            }

            // 3. Vérifier que l'utilisateur est défini
            if (reservation.getUserID() <= 0) {
                throw new RuntimeException("Vous devez être connecté pour effectuer une réservation.");
            }

            // 4. Insertion de la réservation dans la base de données
            String insertSql = "INSERT INTO reservation1 (evenement_id, lieu_id, userID, date_debut, date_fin, type_reservation) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setObject(1, null);  // evenement_id is always null for direct rentals
                insertStmt.setInt(2, reservation.getIdLieu());
                insertStmt.setInt(3, reservation.getUserID());
                insertStmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateDebut()));
                insertStmt.setTimestamp(5, Timestamp.valueOf(reservation.getDateFin()));
                insertStmt.setString(6, "location");

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            reservation.setId(generatedKeys.getInt(1));
                        }
                    }
                } else {
                    throw new RuntimeException("Aucune réservation insérée.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la réservation : " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(reservation1 reservation) {
        try {
            // 1. Vérifier si la réservation existe
            String checkSql = "SELECT COUNT(*) AS count FROM reservation1 WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, reservation.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") == 0) {
                        throw new RuntimeException("La réservation avec l'ID " + reservation.getId() + " n'existe pas.");
                    }
                }
            }

            // 2. Vérifier l'absence de chevauchement avec d'autres réservations
            String checkReservationSql = "SELECT COUNT(*) AS count FROM reservation1 " +
                    "WHERE lieu_id = ? AND id != ? AND (? < date_fin AND ? > date_debut)";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkReservationSql)) {
                checkStmt.setInt(1, reservation.getIdLieu());
                checkStmt.setInt(2, reservation.getId());
                checkStmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
                checkStmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new RuntimeException("Le lieu est déjà réservé pour la période spécifiée.");
                    }
                }
            }

            // 3. Vérifier l'absence de chevauchement avec un événement
            if (!checkEventAvailability(reservation.getIdLieu(), reservation.getDateDebut(), reservation.getDateFin())) {
                throw new RuntimeException("Le lieu est déjà réservé pour un événement pendant la période spécifiée.");
            }

            // 4. Vérifier que l'utilisateur est défini
            if (reservation.getUserID() <= 0) {
                throw new RuntimeException("Vous devez être connecté pour effectuer une réservation.");
            }

            // 5. Mise à jour de la réservation
            String sql = "UPDATE reservation1 SET lieu_id = ?, userID = ?, date_debut = ?, date_fin = ?, type_reservation = ? WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, reservation.getIdLieu());
                pst.setInt(2, reservation.getUserID());
                pst.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
                pst.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
                pst.setString(5, "location");
                pst.setInt(6, reservation.getId());

                int rows = pst.executeUpdate();
                if (rows == 0) {
                    throw new RuntimeException("La modification de la réservation a échoué, aucune ligne affectée.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la réservation : " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si un lieu est disponible (pas d'événement prévu) pour une période donnée
     * @param lieuId ID du lieu
     * @param dateDebut Date et heure de début
     * @param dateFin Date et heure de fin
     * @return true si le lieu est disponible, false sinon
     */
    public boolean checkEventAvailability(int lieuId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        try {
            String sql = "SELECT COUNT(*) AS count FROM events " +
                    "WHERE lieu_id = ? AND statut = 'acceptée' AND " +
                    "((date_debut <= ? AND date_fin >= ?) OR " +
                    "(date_debut <= ? AND date_fin >= ?) OR " +
                    "(date_debut >= ? AND date_fin <= ?))";

            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, lieuId);
                // Premier cas: l'événement commence avant et finit pendant
                pst.setString(2, dateFin.toString());
                pst.setString(3, dateDebut.toString());
                // Deuxième cas: l'événement commence pendant et finit après
                pst.setString(4, dateDebut.toString());
                pst.setString(5, dateFin.toString());
                // Troisième cas: l'événement est entièrement inclus
                pst.setString(6, dateDebut.toString());
                pst.setString(7, dateFin.toString());

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("Nombre d'événements en conflit: " + count);
                        return count == 0;
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la vérification de disponibilité: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void supprimer(reservation1 reservation) {
        try {
            String sql = "DELETE FROM reservation1 WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, reservation.getId());
                int rows = pst.executeUpdate();
                if (rows == 0) {
                    throw new RuntimeException("La suppression de la réservation a échoué, aucune ligne affectée.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la réservation : " + e.getMessage(), e);
        }
    }

    @Override
    public List<reservation1> rechercher() {
        List<reservation1> reservations = new ArrayList<>();
        String sql = "SELECT id, evenement_id, lieu_id, userID, date_debut, date_fin, type_reservation FROM reservation1 ORDER BY id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                reservation1 reservation = new reservation1(
                        rs.getInt("id"),
                        rs.getInt("lieu_id"),
                        rs.getInt("evenement_id"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime()
                );
                reservation.setUserID(rs.getInt("userID"));
                reservation.setTypeReservation(rs.getString("type_reservation"));
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des réservations : " + e.getMessage(), e);
        }
        return reservations;
    }



    public List<Map<String, Object>> getAllLieux() {
        // Code inchangé
        List<Map<String, Object>> lieux = new ArrayList<>();
        String req = "SELECT * FROM lieu";

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                lieux.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des lieux : " + e.getMessage(), e);
        }
        return lieux;
    }

    public String getNomLieuById(int idLieu) {

        String sql = "SELECT nom FROM lieu WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idLieu);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du nom du lieu : " + e.getMessage(), e);
        }
        return "Lieu inconnu";
    }

    public List<reservation1> rechercherReservationsUtilisateur(int userID) {
        List<reservation1> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation1 WHERE userID = ? ORDER BY date_debut";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, userID);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reservation1 reservation = new reservation1(
                            rs.getInt("id"),
                            rs.getInt("lieu_id"),
                            rs.getInt("evenement_id"),
                            rs.getTimestamp("date_debut").toLocalDateTime(),
                            rs.getTimestamp("date_fin").toLocalDateTime()
                    );
                    reservation.setUserID(rs.getInt("userID"));
                    reservation.setTypeReservation(rs.getString("type_reservation"));
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des réservations de l'utilisateur : " + e.getMessage(), e);
        }
        return reservations;
    }
}