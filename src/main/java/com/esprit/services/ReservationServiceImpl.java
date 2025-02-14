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

    public ReservationServiceImpl() {
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
                        System.err.println("Erreur : L'événement avec l'ID " + reservation.getIdEvenement() + " n'existe pas.");
                        return;
                    }
                }
            }

            // Vérifier que l'événement n'est pas terminé
            if (eventFin.isBefore(LocalDateTime.now())) {
                System.err.println("Erreur : L'événement est déjà terminé.");
                return;
            }

            // Vérifier que la réservation se situe dans la période de l'événement
            if (reservation.getDateDebut().isBefore(eventDebut) || reservation.getDateFin().isAfter(eventFin)) {
                System.err.println("Erreur : La réservation doit être comprise dans la période de l'événement ("
                        + eventDebut + " à " + eventFin + ").");
                return;
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
                        System.err.println("Erreur : Le lieu avec l'ID " + reservation.getIdLieu() + " n'existe pas.");
                        return;
                    }
                }
            }

            // 3. Vérifier que le lieu a une capacité suffisante pour l'événement
            if (eventCapacity > lieuCapacity) {
                System.err.println("Erreur : La capacité du lieu (" + lieuCapacity
                        + ") n'est pas suffisante pour l'événement (capacité requise " + eventCapacity + ").");
                return;
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
                        System.err.println("Erreur : Le lieu est déjà réservé pour la période "
                                + reservation.getDateDebut() + " - " + reservation.getDateFin() + ".");
                        return;
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
                            System.out.println("Réservation ajoutée avec succès : " + reservation);
                        }
                    }
                } else {
                    System.err.println("Erreur : aucune réservation insérée.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réservation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(reservation1 reservation) {
        String sql = "UPDATE reservation1 SET evenement_id = ?, lieu_id = ?, date_debut = ?, date_fin = ? WHERE id = ?";
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
        String sql = "DELETE FROM reservation1 WHERE id = ?";
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
        String sql = "SELECT id, evenement_id, lieu_id, date_debut, date_fin FROM reservation1 ORDER BY id";
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

    // Méthodes d'affichage et de gestion avec affichage
    public void ajouterEtAfficher(reservation1 reservation) {
        ajouter(reservation);
        afficherReservations();
    }

    public void modifierEtAfficher(reservation1 reservation) {
        modifier(reservation);
        afficherReservations();
    }

    public void supprimerEtAfficher(reservation1 reservation) {
        supprimer(reservation);
        afficherReservations();
    }

    public void afficherReservations() {
        System.out.println("Liste des réservations :");
        for (reservation1 reservation : rechercher()) {
            System.out.println(reservation);
        }
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
                // Pour chaque colonne, on récupère le nom et la valeur
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                evenements.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans getAllEvenements() : " + e.getMessage());
            e.printStackTrace();
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
                // Pour chaque colonne, récupérer le nom et la valeur
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                lieux.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans getAllLieux() : " + e.getMessage());
            e.printStackTrace();
        }
        return lieux;
    }

}
