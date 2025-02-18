package com.esprit.services;

import com.esprit.models.Lieu;
import com.esprit.models.categorie_salle;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuServiceImpl implements IService<Lieu>{
    private Connection connection = DataSource.getInstance().getConnection();

    public LieuServiceImpl() throws SQLException {
    }

    @Override
    public void ajouter(Lieu lieu) {
        // Remplacer categorie_id par categorie_salle_id dans la requête
        String sql = "INSERT INTO lieu (nom, adresse, ville, code_postal, capacite, image, categorie_salle_id, prix) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, lieu.getNom());
            stmt.setString(2, lieu.getAdresse());
            stmt.setString(3, lieu.getVille());
            stmt.setString(4, lieu.getCodePostal());
            stmt.setInt(5, lieu.getCapacite());
            stmt.setString(6, lieu.getImage());
            stmt.setInt(7, lieu.getCategorie().getId());
            stmt.setDouble(8, lieu.getPrix());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        lieu.setId(generatedKeys.getInt(1)); // Mettre à jour l'ID dans l'objet
                    }
                }
            }
            System.out.println("Lieu ajouté : " + lieu);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Code d'erreur pour violation de contrainte unique
                System.out.println("Erreur : Un lieu avec ce nom existe déjà.");
            } else {
                System.out.println("Erreur lors de l'ajout du lieu : " + e.getMessage());
            }
        }
    }
    @Override
    public void modifier(Lieu lieu) {
        String updateReq = "UPDATE lieu SET nom=?, adresse=?, ville=?, code_postal=?, capacite=?, image=?, categorie_salle_id=?, prix=? WHERE id=?";

        try (PreparedStatement updateStmt = connection.prepareStatement(updateReq)) {
            // Paramétrage direct de la requête UPDATE
            updateStmt.setString(1, lieu.getNom());
            updateStmt.setString(2, lieu.getAdresse());
            updateStmt.setString(3, lieu.getVille());
            updateStmt.setString(4, lieu.getCodePostal());
            updateStmt.setInt(5, lieu.getCapacite());
            updateStmt.setString(6, lieu.getImage());
            updateStmt.setInt(7, lieu.getCategorie().getId());
            updateStmt.setDouble(8, lieu.getPrix());
            updateStmt.setInt(9, lieu.getId());

            // Exécution directe de la mise à jour
            int rowsAffected = updateStmt.executeUpdate();

            // Vérification basique du résultat
            if (rowsAffected > 0) {
                System.out.println("Lieu modifié avec succès : " + lieu);
            } else {
                System.out.println("Aucun lieu trouvé avec l'ID : " + lieu.getId());
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Erreur : Conflit d'unicité (nom probablement déjà utilisé)");
        } catch (SQLException e) {
            System.out.println("Erreur technique lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Lieu lieu) {
        String req = "DELETE FROM lieu WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, lieu.getId());
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Lieu supprimé : " + lieu);
            } else {
                System.out.println("Aucun lieu trouvé avec l'ID : " + lieu.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du lieu : " + e.getMessage());
        }
    }

    @Override
    public List<Lieu> rechercher() {
        List<Lieu> lieux = new ArrayList<>();
        String req = "SELECT l.*, c.id AS cat_id, c.nom AS cat_nom, c.description AS cat_description " +
                "FROM lieu l " +
                "JOIN categorie_salle c ON l.categorie_salle_id = c.id";

        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                categorie_salle categoriesalle = new categorie_salle(
                        rs.getInt("cat_id"),
                        rs.getString("cat_nom"),
                        rs.getString("cat_description")
                );

                Lieu lieu = new Lieu(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("ville"),
                        rs.getString("code_postal"),
                        rs.getInt("capacite"),
                        rs.getString("image"),
                        categoriesalle,
                        rs.getDouble("prix")
                );
                lieux.add(lieu);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche des lieux : " + e.getMessage());
        }
        return lieux;
    }


    // Méthode pour récupérer la catégorie associée à un lieu (en utilisant la table categorie_salle)
    private categorie_salle getCategorieById(int id) {
        String req = "SELECT * FROM categorie_salle WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new categorie_salle(rs.getInt("id"), rs.getString("nom"), rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la catégorie : " + e.getMessage());
        }
        return null;
    }
    //nouvelles methodes**********************************************
    public void afficherLieux() {
        List<Lieu> lieux = this.rechercher();
        lieux.forEach(System.out::println);
    }

    public Lieu getPremierLieu() {
        List<Lieu> lieux = this.rechercher();
        return lieux.isEmpty() ? null : lieux.get(0);
    }

    public Lieu getDernierLieu() {
        List<Lieu> lieux = this.rechercher();
        return lieux.isEmpty() ? null : lieux.get(lieux.size() - 1);
    }


}