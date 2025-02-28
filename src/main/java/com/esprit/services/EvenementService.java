package com.esprit.services;

import com.esprit.models.Evenement;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {

    private Connection connection;

    public EvenementService() throws SQLException {
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
        String req = "UPDATE events SET titre=?, description=?, date_debut=?, date_fin=?, " +
                "capacite=?, categorie_id=?, lieu_id=?, organisateur_id=?, prix=?, " +
                "statut=?, lieu_proprietaire=?, image=? WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, evenement.getTitre());
            pst.setString(2, evenement.getDescription());
            pst.setString(3, evenement.getDateDebut());
            pst.setString(4, evenement.getDateFin());
            pst.setInt(5, evenement.getCapacite());
            pst.setInt(6, evenement.getCategorieId());

            // Gestion du lieu_id et lieu_proprietaire
            if (evenement.getLieuId() > 0) {
                pst.setInt(7, evenement.getLieuId());
                pst.setNull(11, Types.VARCHAR); // lieu_proprietaire devient NULL
            } else {
                pst.setNull(7, Types.INTEGER);
                pst.setString(11, evenement.getLieu_proprietaire());
            }

            pst.setInt(8, evenement.getOrganisateurId());
            pst.setDouble(9, evenement.getPrix());
            pst.setString(10, evenement.getStatut());
            pst.setString(12, evenement.getImage());
            pst.setInt(13, evenement.getId());

            pst.executeUpdate();
            System.out.println("Événement modifié avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de l'événement: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la modification de l'événement", e);
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



        String req = "SELECT e.*, " +
                "u.userName AS organisateur_nom, " +
                "c.nom AS categorie_nom, " +
                "l.nom AS lieu_nom " +
                "FROM events e " +
                "LEFT JOIN users u ON e.organisateur_id = u.userID " +
                "LEFT JOIN categoriesevent c ON e.categorie_id = c.id " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id";

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Evenement evt = new Evenement(
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
                );
                evt.setOrganisateurNom(rs.getString("organisateur_nom"));
                evt.setCategorieNom(rs.getString("categorie_nom"));
                evt.setLieuNom(rs.getString("lieu_nom"));
                evenements.add(evt);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
        }
        return evenements;
    }

    public Evenement rechercherParId(int id) {
        Evenement evenement = null;
        String req = "SELECT e.*, " +
                "u.userName AS organisateur_nom, " +
                "c.nom AS categorie_nom, " +
                "l.nom AS lieu_nom " +
                "FROM events e " +
                "LEFT JOIN users u ON e.organisateur_id = u.userID " +
                "LEFT JOIN categoriesevent c ON e.categorie_id = c.id " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id " +
                "WHERE e.id = ?";






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
                            rs.getInt("categorie_id"),
                            rs.getInt("lieu_id"),
                            rs.getInt("organisateur_id"),
                            rs.getDouble("prix"),
                            rs.getString("statut"),
                            rs.getString("lieu_proprietaire"),
                            rs.getString("image")
                    );
                    evenement.setOrganisateurNom(rs.getString("organisateur_nom"));
                    evenement.setCategorieNom(rs.getString("categorie_nom"));
                    evenement.setLieuNom(rs.getString("lieu_nom"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'événement par ID: " + e.getMessage());
        }
        return evenement;
    }
    public void ajouterMaterielAEvenement(int evenementId, int materielId, int quantite) {
        String req = "INSERT INTO event_materiel (evenement_id, materiel_id, quantite) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenementId);
            pst.setInt(2, materielId);
            pst.setInt(3, quantite);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Matériel ajouté à l'événement avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du matériel à l'événement: " + e.getMessage());
        }
    }
    public List<String> getMaterielsByEvenement(int evenementId) {
        List<String> materiels = new ArrayList<>();
        String req = "SELECT m.libelle, em.quantite " +
                "FROM event_materiel em " +
                "JOIN materiel m ON em.materiel_id = m.id " +
                "WHERE em.evenement_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, evenementId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String materielInfo = rs.getString("libelle") + " (Quantité: " + rs.getInt("quantite") + ")";
                    materiels.add(materielInfo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des matériels pour l'événement: " + e.getMessage());
        }
        return materiels;
    }

    public List<Evenement> rechercherParOrganisateur(int organisateurId) {
        List<Evenement> evenements = new ArrayList<>();
        String req = "SELECT e.*, " +
                "u.userName AS organisateur_nom, " +
                "c.nom AS categorie_nom, " +
                "l.nom AS lieu_nom " +
                "FROM events e " +
                "LEFT JOIN users u ON e.organisateur_id = u.userID " +
                "LEFT JOIN categoriesevent c ON e.categorie_id = c.id " +
                "LEFT JOIN lieu l ON e.lieu_id = l.id " +
                "WHERE e.organisateur_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, organisateurId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Evenement evt = new Evenement(
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
                    );
                    evt.setOrganisateurNom(rs.getString("organisateur_nom"));
                    evt.setCategorieNom(rs.getString("categorie_nom"));
                    evt.setLieuNom(rs.getString("lieu_nom"));
                    evenements.add(evt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
        }
        return evenements;
    }

}
