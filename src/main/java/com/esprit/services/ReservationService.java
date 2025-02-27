package com.esprit.services;

import com.esprit.models.Reservation;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IService<Reservation> {

    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Reservation reservation) {
        try {
            Statement st = connection.createStatement();
            
            // Vérifier si c'est une réservation avec événement ou avec utilisateur
            if (reservation.getEvenementId() != null && reservation.getEvenementId() != 0) {
                // Cas d'une réservation avec événement
                String getEventDatesQuery = "SELECT date_debut, date_fin FROM events WHERE id=" + reservation.getEvenementId();
                ResultSet rsEvent = st.executeQuery(getEventDatesQuery);
                
                if (!rsEvent.next()) {
                    throw new RuntimeException("L'événement avec l'ID " + reservation.getEvenementId() + " n'existe pas.");
                }
                
                // Utiliser les dates de l'événement
                Timestamp dateDebut = rsEvent.getTimestamp("date_debut");
                Timestamp dateFin = rsEvent.getTimestamp("date_fin");
                
                // Vérifier le stock et insérer la réservation
                verifierStockEtInserer(st, reservation, dateDebut, dateFin, true);
                
            } else if (reservation.getUserId() != 0) {
                // Cas d'une réservation avec utilisateur
                String checkUserQuery = "SELECT userID FROM users WHERE userID = " + reservation.getUserId();
                ResultSet rsUser = st.executeQuery(checkUserQuery);
                
                if (!rsUser.next()) {
                    throw new RuntimeException("L'utilisateur avec l'ID " + reservation.getUserId() + " n'existe pas.");
                }
                
                // Utiliser les dates fournies dans la réservation
                verifierStockEtInserer(st, reservation, reservation.getDateDebut(), reservation.getDateFin(), false);
                
            } else {
                throw new RuntimeException("La réservation doit avoir soit un utilisateur soit un événement associé.");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout de la réservation : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }

    // Méthode privée pour factoriser la vérification du stock et l'insertion
    private void verifierStockEtInserer(Statement st, Reservation reservation, 
                                      Timestamp dateDebut, Timestamp dateFin, 
                                      boolean isEventReservation) throws SQLException {
        // Vérifier le stock disponible
        String getMaterielQuery = "SELECT prix, quantite FROM materiel WHERE id=" + reservation.getMaterielId();
        ResultSet rsMateriel = st.executeQuery(getMaterielQuery);

        if (rsMateriel.next()) {
            int capaciteActuelle = rsMateriel.getInt("quantite");
            int nouvelleCapacite = capaciteActuelle - reservation.getQuantite();

            if (nouvelleCapacite >= 0) {
                // Construire la requête d'insertion en fonction du type de réservation
                StringBuilder req = new StringBuilder("INSERT INTO reservation (");
                StringBuilder values = new StringBuilder("VALUES (");

                // Ajouter les champs communs
                req.append("materiel_id, quantite, prix_total, date_debut, date_fin");
                values.append(reservation.getMaterielId()).append(", ")
                      .append(reservation.getQuantite()).append(", ")
                      .append(reservation.getPrixTotal()).append(", '")
                      .append(dateDebut).append("', '")
                      .append(dateFin).append("'");

                // Ajouter les champs spécifiques selon le type de réservation
                if (isEventReservation) {
                    req.append(", evenement_id");
                    values.append(", ").append(reservation.getEvenementId());
                } else {
                    req.append(", userid");
                    values.append(", ").append(reservation.getUserId());
                }

                // Finaliser la requête
                req.append(") ").append(values.append(")"));

                System.out.println("Requête d'insertion: " + req); // Debug
                st.executeUpdate(req.toString());

                // Mettre à jour la capacité du matériel
                String updateCapaciteQuery = "UPDATE materiel SET quantite=" + nouvelleCapacite + 
                                          " WHERE id=" + reservation.getMaterielId();
                st.executeUpdate(updateCapaciteQuery);
                
                System.out.println("Réservation ajoutée avec succès et capacité mise à jour.");
            } else {
                throw new RuntimeException("Stock insuffisant pour la réservation.");
            }
        } else {
            throw new RuntimeException("Le matériel avec l'ID " + reservation.getMaterielId() + " n'existe pas.");
        }
    }

    @Override
    public void modifier(Reservation reservation) {
        try {
            Connection connection = DataSource.getInstance().getConnection();
            Statement st = connection.createStatement();

            // 🔹 Récupérer le prix du matériel
            String getPrixQuery = "SELECT prix FROM materiel WHERE id=" + reservation.getMaterielId();
            ResultSet rs = st.executeQuery(getPrixQuery);

            if (rs.next()) {
                double prixUnitaire = rs.getDouble("prix");
                double prixTotal = prixUnitaire * reservation.getQuantite();

                // 🔹 Récupération des dates depuis l'événement
                String getEventDatesQuery = "SELECT date_debut, date_fin FROM events WHERE id=" + reservation.getEvenementId();
                ResultSet rsEvent = st.executeQuery(getEventDatesQuery);

                Timestamp dateDebut = null;
                Timestamp dateFin = null;

                if (rsEvent.next()) {
                    dateDebut = rsEvent.getTimestamp("date_debut");
                    dateFin = rsEvent.getTimestamp("date_fin");
                }

                // 🔹 Construction de la requête de mise à jour
                String req = "UPDATE reservation SET " +
                        "quantite=" + reservation.getQuantite() + ", " +
                        "prix_total=" + prixTotal + ", " +
                        "date_debut='" + dateDebut + "', " +
                        "date_fin='" + dateFin + "' " +
                        "WHERE id=" + reservation.getId();

                // 🔹 Exécution de la requête
                int rowsUpdated = st.executeUpdate(req);
                if (rowsUpdated > 0) {
                    System.out.println(" Réservation modifiée avec succès.");
                } else {
                    System.out.println(" Aucune réservation trouvée avec cet ID.");
                }
            } else {
                System.out.println(" Erreur : Matériel introuvable.");
            }
        } catch (SQLException e) {
            System.out.println(" Erreur SQL lors de la modification de la réservation : " + e.getMessage());
        }
    }

    @Override
    public List<Reservation> rechercher() {
        List<Reservation> reservations = new ArrayList<>();
        String req = "SELECT r.*, u.fullName FROM reservation r " +
                    "LEFT JOIN users u ON r.userid = u.userID";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Reservation reservation = new Reservation(
                    rs.getInt("id"),
                    rs.getInt("userid"),
                    rs.getInt("evenement_id"),
                    rs.getInt("materiel_id"),
                    rs.getInt("quantite"),
                    rs.getDouble("prix_total"),
                    rs.getTimestamp("date_debut"),
                    rs.getTimestamp("date_fin")
                );
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations : " + e.getMessage());
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
        return null;
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
        return null;
    }

    public String getUserName(int userId) {
        String userName = "Inconnu";
        try {
            Statement st = connection.createStatement();
            String query = "SELECT fullName FROM users WHERE userID = " + userId;
            ResultSet rs = st.executeQuery(query);
            
            if (rs.next()) {
                String nom = rs.getString("fullName");

                userName =  nom;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom d'utilisateur : " + e.getMessage());
        }
        return userName;
    }

}
