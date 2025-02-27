package com.esprit.services;

import com.esprit.models.Reservation;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    Connection connection = DataSource.getInstance().getConnection();

    public ReservationService() throws SQLException {
    }

    @Override
    public void ajouter(Reservation reservation) {
        try {
            Statement st = connection.createStatement();

            // Vérifier et obtenir les dates de l'événement si nécessaire
            java.sql.Date dateDebut = reservation.getDateDebut();
            java.sql.Date dateFin = reservation.getDateFin();

            if (reservation.getEvenementId() != null) {
                String getEventDatesQuery = "SELECT date_debut, date_fin FROM events WHERE id = ?";
                PreparedStatement psEvent = connection.prepareStatement(getEventDatesQuery);
                psEvent.setInt(1, reservation.getEvenementId());
                ResultSet rsEvent = psEvent.executeQuery();
                if (rsEvent.next()) {
                    dateDebut = rsEvent.getDate("date_debut");
                    dateFin = rsEvent.getDate("date_fin");
                }
            }

            // Vérifier la disponibilité du matériel
            String getMaterielQuery = "SELECT prix, quantite FROM materiel WHERE id = ?";
            PreparedStatement psMateriel = connection.prepareStatement(getMaterielQuery);
            psMateriel.setInt(1, reservation.getMaterielId());
            ResultSet rsMateriel = psMateriel.executeQuery();

            if (rsMateriel.next()) {
                double prixUnitaire = rsMateriel.getDouble("prix");
                int capaciteActuelle = rsMateriel.getInt("quantite");
                double prixTotal = prixUnitaire * reservation.getQuantite();
                int nouvelleCapacite = capaciteActuelle - reservation.getQuantite();

                if (nouvelleCapacite >= 0) {
                    // Insérer la réservation avec PreparedStatement
                    String insertQuery = "INSERT INTO reservation (userid, evenement_id, materiel_id, quantite, prix_total, date_debut, date_fin) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
                    PreparedStatement ps = connection.prepareStatement(insertQuery);
                    ps.setInt(1, reservation.getUserId());
                    
                    if (reservation.getEvenementId() != null) {
                        ps.setInt(2, reservation.getEvenementId());
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    
                    ps.setInt(3, reservation.getMaterielId());
                    ps.setInt(4, reservation.getQuantite());
                    ps.setDouble(5, prixTotal);
                    ps.setDate(6, (java.sql.Date) reservation.getDateDebut());
                    ps.setDate(7, (java.sql.Date) reservation.getDateFin());

                    ps.executeUpdate();

                    // Mettre à jour la quantité du matériel
                    String updateCapaciteQuery = "UPDATE materiel SET quantite = ? WHERE id = ?";
                    PreparedStatement psUpdate = connection.prepareStatement(updateCapaciteQuery);
                    psUpdate.setInt(1, nouvelleCapacite);
                    psUpdate.setInt(2, reservation.getMaterielId());
                    psUpdate.executeUpdate();

                    System.out.println("Réservation ajoutée avec succès et capacité mise à jour.");
                } else {
                    System.out.println("Erreur : Stock insuffisant pour la réservation.");
                    throw new SQLException("Stock insuffisant pour la réservation");
                }
            } else {
                System.out.println("Erreur : Matériel non trouvé.");
                throw new SQLException("Matériel non trouvé");
            }
            
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la réservation : " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Reservation reservation) {
        try {
            // Utiliser PreparedStatement pour plus de sécurité
            String req = "UPDATE reservation SET userid=?, quantite=?, prix_total=?, date_debut=?, date_fin=? WHERE id=?";
            
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, reservation.getUserId());
            ps.setInt(2, reservation.getQuantite());
            ps.setDouble(3, reservation.getPrixTotal());
            ps.setDate(4, reservation.getDateDebut());  // Utiliser setDate au lieu de setTimestamp
            ps.setDate(5, reservation.getDateFin());    // Utiliser setDate au lieu de setTimestamp
            ps.setInt(6, reservation.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Réservation modifiée avec succès.");
            } else {
                System.out.println("Aucune réservation trouvée avec cet ID.");
            }
            
        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de la modification de la réservation : " + e.getMessage());
        }
    }

    @Override
    public List<Reservation> rechercher() {
        List<Reservation> reservations = new ArrayList<>();

        String req = "SELECT * FROM reservation";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("id"),
                        rs.getInt("userid"),
                        rs.getInt("evenement_id"),
                        rs.getInt("materiel_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_total"),
                        rs.getDate("date_debut"),  // Changé de getTimestamp à getDate
                        rs.getDate("date_fin")     // Changé de getTimestamp à getDate
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return reservations;
    }
    public void supprimer(Reservation reservation) {
        String req = "DELETE FROM reservation WHERE id=" + reservation.getId();
        try {
            Statement st = connection.createStatement();
            int rowsDeleted = st.executeUpdate(req);
            if (rowsDeleted > 0) {
                System.out.println(" Réservation supprimée avec succès.");
            } else {
                System.out.println(" Aucune réservation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            System.out.println(" Erreur SQL lors de la suppression de la réservation : " + e.getMessage());
        }
    }
    public String getEventName(int eventId) {
        String req = "SELECT titre FROM events WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("titre");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'événement : " + e.getMessage());
        }
        return "0";
    }

    public String getMaterialName(int materialId) {
        String req = "SELECT libelle FROM materiel WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, materialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("libelle");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom du matériel : " + e.getMessage());
        }
        return "Inconnu";
    }

    public String getUserName(int userId) {
        String userName = "Inconnu";
        String query = "SELECT fullName FROM users WHERE userID = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                userName = resultSet.getString("fullName");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom d'utilisateur: " + e.getMessage());
        }
        
        return userName;
    }

}