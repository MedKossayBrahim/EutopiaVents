package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {

    private Connection connection;

    public EvenementService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    @Override
    public void ajouter(Evenement evenement) {
        String req = "INSERT INTO events (titre, description, date_debut, date_fin, capacite, categorie_id, organisateur_id, prix, statut, lieu_id, lieu_proprietaire, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, evenement.getTitre());
            pst.setString(2, evenement.getDescription());
            pst.setString(3, evenement.getDateDebut());
            pst.setString(4, evenement.getDateFin());
            pst.setInt(5, evenement.getCapacite());
            pst.setInt(6, evenement.getCategorieId());  // Ajout de l'ID de la catégorie
            pst.setInt(7, evenement.getOrganisateurId());
            pst.setDouble(8, evenement.getPrix());
            pst.setString(9, evenement.getStatut());

            // Vérification du lieu
            if (evenement.getLieuId() != 0) {
                pst.setInt(10, evenement.getLieuId());
                pst.setNull(11, Types.VARCHAR);
            } else {
                pst.setNull(10, Types.INTEGER);
                pst.setString(11, evenement.getLieu_proprietaire());
            }

            pst.setString(12, evenement.getImage());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evenement.setId(generatedKeys.getInt(1));
                        System.out.println("Événement ajouté avec ID: " + evenement.getId());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'événement: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Evenement evenement) {
        String req = "UPDATE events SET titre=?, description=?, date_debut=?, date_fin=?, capacite=?, categorie_id=?, lieu_id=?, organisateur_id=?, prix=?, statut=?, lieu_proprietaire=?, image=? WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, evenement.getTitre());
            pst.setString(2, evenement.getDescription());
            pst.setString(3, evenement.getDateDebut());
            pst.setString(4, evenement.getDateFin());
            pst.setInt(5, evenement.getCapacite());
            pst.setInt(6, evenement.getCategorieId());  // Mise à jour de l'ID de la catégorie
            pst.setInt(7, evenement.getLieuId());
            pst.setInt(8, evenement.getOrganisateurId());
            pst.setDouble(9, evenement.getPrix());
            pst.setString(10, evenement.getStatut());
            pst.setString(11, evenement.getLieu_proprietaire());
            pst.setString(12, evenement.getImage());
            pst.setInt(13, evenement.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Événement modifié");
            } else {
                System.out.println("Aucun événement modifié. Vérifiez l'ID ou les contraintes.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'événement: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Evenement evenement) {
        String req = "DELETE FROM events WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenement.getId());
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Événement supprimé");
            } else {
                System.out.println("Aucun événement supprimé. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'événement: " + e.getMessage());
        }
    }


    public List<Evenement> rechercher() {
        List<Evenement> evenements = new ArrayList<>();
        String req = "SELECT * FROM events";
        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                evenements.add(new Evenement(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getString("date_debut"),
                        rs.getString("date_fin"),
                        rs.getInt("capacite"),
                        rs.getInt("categorie_id"),
                        rs.getInt("lieu_id"),
                        rs.getInt("organisateur_id"),
                        rs.getDouble("prix"),
                        rs.getString("statut"),
                        rs.getString("lieu_proprietaire"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
        }
        return evenements;
    }

    public Evenement rechercherParId(int id) {
        Evenement evenement = null;
        String req = "SELECT * FROM events WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    evenement = new Evenement(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("description"),
                            rs.getString("date_debut"),
                            rs.getString("date_fin"),
                            rs.getInt("capacite"),
                            rs.getInt("categorie_id"),  // Lecture de l'ID de la catégorie
                            rs.getInt("lieu_id"),
                            rs.getInt("organisateur_id"),
                            rs.getDouble("prix"),
                            rs.getString("statut"),
                            rs.getString("lieu_proprietaire"),
                            rs.getString("image")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'événement par ID: " + e.getMessage());
        }
        return evenement;
    }
}
