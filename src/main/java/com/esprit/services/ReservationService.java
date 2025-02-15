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

            //  Récupérer la date de début et de fin de l'événement
            String getEventDatesQuery = "SELECT date_debut, date_fin FROM events WHERE id=" + reservation.getEvenementId();
            ResultSet rsEvent = st.executeQuery(getEventDatesQuery);

            if (rsEvent.next()) {
                // 🔹 Stocker les dates récupérées
                Timestamp dateDebut = rsEvent.getTimestamp("date_debut");
                Timestamp dateFin = rsEvent.getTimestamp("date_fin");

                //  Récupérer le prix unitaire du matériel
                String getPrixQuery = "SELECT prix FROM materiel WHERE id=" + reservation.getMaterielId();
                ResultSet rsMateriel = st.executeQuery(getPrixQuery);

                if (rsMateriel.next()) {
                    double prixUnitaire = rsMateriel.getDouble("prix");
                    double prixTotal = prixUnitaire * reservation.getQuantite();

                    // 🔹 Étape 3 : Insérer la réservation avec les dates récupérées
                    String req = "INSERT INTO reservation (evenement_id, materiel_id, quantite, prix_total, date_debut, date_fin) VALUES ("
                            + reservation.getEvenementId() + ", "
                            + reservation.getMaterielId() + ", "
                            + reservation.getQuantite() + ", "
                            + prixTotal + ", '"
                            + dateDebut + "', '"
                            + dateFin + "')";

                    st.executeUpdate(req);
                    System.out.println(" Réservation ajoutée avec succès.");
                }
            }
        } catch (SQLException e) {
            System.out.println(" Erreur lors de l'ajout de la réservation : " + e.getMessage());
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

        String req = "SELECT * FROM reservation";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("id"),
                        rs.getInt("evenement_id"),
                        rs.getInt("materiel_id"),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_total"),
                        rs.getTimestamp("date_debut"),
                        rs.getTimestamp("date_fin")
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

}
