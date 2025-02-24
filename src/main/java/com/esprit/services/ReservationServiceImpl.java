package com.esprit.services;
import com.esprit.models.reservation1;
import com.esprit.utils.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationServiceImpl implements IService<reservation1> {
    private Connection connection;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ReservationServiceImpl() throws SQLException {
        connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(reservation1 reservation) {
        try {
            // 1. Vérifier l'existence de l'événement et récupérer sa capacité, sa date_debut et sa date_fin
            String eventQuery = "SELECT capacite, date_debut, date_fin FROM events WHERE id = ?";
            int eventCapacity = 0;
            LocalDateTime eventDebut = null;
            LocalDateTime eventFin = null;
            try (PreparedStatement eventStmt = connection.prepareStatement(eventQuery)) {
                eventStmt.setInt(1, reservation.getIdEvenement());
                try (ResultSet rs = eventStmt.executeQuery()) {
                    if (rs.next()) {
                        eventCapacity = rs.getInt("capacite");
                        eventDebut = rs.getTimestamp("date_debut").toLocalDateTime();
                        eventFin = rs.getTimestamp("date_fin").toLocalDateTime();
                    } else {
                        throw new RuntimeException("L'événement avec l'ID " + reservation.getIdEvenement() + " n'existe pas.");
                    }
                }
            }

            // Vérifier que l'événement n'est pas terminé
            if (eventFin.isBefore(LocalDateTime.now())) {
                throw new RuntimeException("L'événement est déjà terminé.");
            }

            // Vérifier que la réservation se situe dans la période de l'événement
            if (reservation.getDateDebut().isBefore(eventDebut) || reservation.getDateFin().isAfter(eventFin)) {
                throw new RuntimeException("La réservation doit être comprise dans la période de l'événement (du "
                        + eventDebut.format(DATE_TIME_FORMATTER)
                        + " au "
                        + eventFin.format(DATE_TIME_FORMATTER)
                        + ").");
            }

            // 2. Vérifier l'existence du lieu et récupérer sa capacité
            String lieuQuery = "SELECT capacite FROM lieu WHERE id = ?";
            int lieuCapacity = 0;
            try (PreparedStatement lieuStmt = connection.prepareStatement(lieuQuery)) {
                lieuStmt.setInt(1, reservation.getIdLieu());
                try (ResultSet rs = lieuStmt.executeQuery()) {
                    if (rs.next()) {
                        lieuCapacity = rs.getInt("capacite");
                    } else {
                        throw new RuntimeException("Le lieu avec l'ID " + reservation.getIdLieu() + " n'existe pas.");
                    }
                }
            }

            // 3. Vérifier que le lieu a une capacité suffisante pour l'événement
            if (eventCapacity > lieuCapacity) {
                throw new RuntimeException("La capacité du lieu (" + lieuCapacity
                        + ") n'est pas suffisante pour l'événement (capacité requise " + eventCapacity + ").");
            }

            // 4. Vérifier l'absence de chevauchement avec une réservation existante pour ce lieu
            String checkReservationSql = "SELECT COUNT(*) AS count FROM reservation1 " +
                    "WHERE lieu_id = ? AND (? < date_fin AND ? > date_debut)";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkReservationSql)) {
                Timestamp newStart = Timestamp.valueOf(reservation.getDateDebut());
                Timestamp newEnd   = Timestamp.valueOf(reservation.getDateFin());
                checkStmt.setInt(1, reservation.getIdLieu());
                checkStmt.setTimestamp(2, newStart);
                checkStmt.setTimestamp(3, newEnd);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new RuntimeException("Le lieu est déjà réservé pour la période du "
                                + reservation.getDateDebut().format(DATE_TIME_FORMATTER)
                                + " au "
                                + reservation.getDateFin().format(DATE_TIME_FORMATTER)
                                + ".");
                    }
                }
            }

            // 5. Aucune contrainte violée, insertion de la réservation dans la base de données
            String insertSql = "INSERT INTO reservation1 (evenement_id, lieu_id, date_debut, date_fin) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, reservation.getIdEvenement());
                insertStmt.setInt(2, reservation.getIdLieu());
                insertStmt.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
                insertStmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));

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
            // Vérifier si la réservation existe
            String checkSql = "SELECT COUNT(*) AS count FROM reservation1 WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, reservation.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt("count") == 0) {
                        throw new RuntimeException("La réservation avec l'ID " + reservation.getId() + " n'existe pas.");
                    }
                }
            }

            // Vérifier les contraintes comme dans la méthode ajouter


            String sql = "UPDATE reservation1 SET evenement_id = ?, lieu_id = ?, date_debut = ?, date_fin = ? WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, reservation.getIdEvenement());
                pst.setInt(2, reservation.getIdLieu());
                pst.setTimestamp(3, Timestamp.valueOf(reservation.getDateDebut()));
                pst.setTimestamp(4, Timestamp.valueOf(reservation.getDateFin()));
                pst.setInt(5, reservation.getId());

                int rows = pst.executeUpdate();
                if (rows == 0) {
                    throw new RuntimeException("La modification de la réservation a échoué, aucune ligne affectée.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la réservation : " + e.getMessage(), e);
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
        String sql = "SELECT id, evenement_id, lieu_id, date_debut, date_fin FROM reservation1 ORDER BY id";
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
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des réservations : " + e.getMessage(), e);
        }
        return reservations;
    }

    public List<Map<String, Object>> getAllEvenements() {
        List<Map<String, Object>> evenements = new ArrayList<>();
        String req = "SELECT * FROM events";

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                evenements.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des événements : " + e.getMessage(), e);
        }
        return evenements;
    }

    public List<Map<String, Object>> getAllLieux() {
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

    public List<reservation1> getAllReservations() {
        List<reservation1> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation1";

        try (PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int idLieu = rs.getInt("lieu_id");
                int idEvenement = rs.getInt("evenement_id");
                LocalDateTime dateDebut = rs.getTimestamp("date_debut").toLocalDateTime();
                LocalDateTime dateFin = rs.getTimestamp("date_fin").toLocalDateTime();

                reservation1 reservation = new reservation1(id, idLieu, idEvenement, dateDebut, dateFin);
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les réservations : " + e.getMessage(), e);
        }
        return reservations;
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

    public String getTitreEventById(int idEvenement) {
        String sql = "SELECT titre FROM events WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, idEvenement);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("titre");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du titre de l'événement : " + e.getMessage(), e);
        }
        return "Événement inconnu";
    }
}
