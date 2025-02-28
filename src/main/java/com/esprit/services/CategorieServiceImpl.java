package com.esprit.services;

import com.esprit.models.categorie_salle;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieServiceImpl implements IService<categorie_salle>{
    //etablir la connexion a l base de donnes
    Connection connection = DataSource.getInstance().getConnection();

    public CategorieServiceImpl() throws SQLException {
    }


    public void ajouter(categorie_salle categoriesalle) {
        String req = "INSERT INTO categorie_salle (nom, description) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, categoriesalle.getNom());
            pst.setString(2, categoriesalle.getDescription());
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    categoriesalle.setId(rs.getInt(1));
                }
            }
            System.out.println("Catégorie ajoutée : " + categoriesalle);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Code d'erreur pour violation de contrainte unique
                System.out.println("Erreur : Une catégorie avec ce nom existe déjà.");
            } else {
                System.out.println("Erreur lors de l'ajout de la catégorie : " + e.getMessage());
            }
        }
    }

    @Override
    public void modifier(categorie_salle categoriesalle) {
        String checkReq = "SELECT * FROM categorie_salle WHERE id = ?";
        String updateReq = "UPDATE categorie_salle SET nom = ?, description = ? WHERE id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkReq);
             PreparedStatement updateStmt = connection.prepareStatement(updateReq)) {

            checkStmt.setInt(1, categoriesalle.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentNom = rs.getString("nom");
                String currentDescription = rs.getString("description");

                if (!currentNom.equals(categoriesalle.getNom()) || !currentDescription.equals(categoriesalle.getDescription())) {
                    updateStmt.setString(1, categoriesalle.getNom());
                    updateStmt.setString(2, categoriesalle.getDescription());
                    updateStmt.setInt(3, categoriesalle.getId());

                    try {
                        int rowsAffected = updateStmt.executeUpdate();
                        if (rowsAffected > 0) {
                            System.out.println("Catégorie modifiée : " + categoriesalle);
                        }
                    } catch (SQLException e) {
                        if (e.getSQLState().equals("23000")) {
                            System.out.println("Erreur : Une catégorie avec ce nom existe déjà.");
                        } else {
                            throw e;
                        }
                    }
                } else {
                    System.out.println("Aucune modification nécessaire pour la catégorie : " + categoriesalle);
                }
            } else {
                System.out.println("Catégorie non trouvée avec l'ID : " + categoriesalle.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(categorie_salle categoriesalle) {
        String req = "DELETE FROM categorie_salle WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, categoriesalle.getId());
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Catégorie supprimée : " + categoriesalle);
            } else {
                System.out.println("Aucune catégorie trouvée avec l'ID : " + categoriesalle.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la catégorie : " + e.getMessage());
        }
    }

    @Override
    public List<categorie_salle> rechercher() {
        List<categorie_salle> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie_salle";
        try (PreparedStatement pst = connection.prepareStatement(req);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                categorie_salle categoriesalle = new categorie_salle(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
                categories.add(categoriesalle);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche des catégories : " + e.getMessage());
        }
        return categories;
    }
    public categorie_salle getCategoryByName(String name) {
        String req = "SELECT * FROM categorie_salle WHERE nom = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setString(1, name);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new categorie_salle(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche de la catégorie par nom : " + e.getMessage());
        }
        return null;
    }

    //un ajout du code*******
    //cest une methode qui ajoute plusieurs categorie a la fois en utilisant la methode d'ajout principal utilise dans le main
    public void addCategories(String[] noms, String[] descriptions) {
        for (int i = 0; i < noms.length; i++) {
            categorie_salle nouvelleCategorie = new categorie_salle(noms[i], descriptions[i]);
            System.out.println("\nTentative d'ajout de la catégorie : " + nouvelleCategorie);
            this.ajouter(nouvelleCategorie);
        }
    }

    // Méthode pour modifier la première catégorie existante
    public void modifyFirstCategory(String nouveauNom, String nouvelleDescription) {
        List<categorie_salle> categories = this.rechercher();
        if (!categories.isEmpty()) {
            categorie_salle cat = categories.get(0);
            String ancienNom = cat.getNom();
            String ancienneDesc = cat.getDescription();
            cat.setNom(nouveauNom);
            cat.setDescription(nouvelleDescription);
            System.out.println("\nModification de la première catégorie :");
            System.out.println("Ancien nom: " + ancienNom + " → Nouveau nom: " + nouveauNom);
            System.out.println("Ancienne description: " + ancienneDesc + " → Nouvelle description: " + nouvelleDescription);
            this.modifier(cat);
        } else {
            System.out.println("Aucune catégorie à modifier.");
        }
    }

    // Méthode pour supprimer la dernière catégorie (si plusieurs existent)
    public void deleteLastCategory() {
        List<categorie_salle> categories = this.rechercher();
        if (categories.size() > 1) {
            categorie_salle cat = categories.get(categories.size() - 1);
            System.out.println("\nSuppression de la catégorie : " + cat);
            this.supprimer(cat);
        } else {
            System.out.println("Impossible de supprimer la catégorie, il doit en rester au moins une.");
        }
    }
}