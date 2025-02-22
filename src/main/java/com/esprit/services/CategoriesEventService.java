package com.esprit.services;

import com.esprit.models.CategoriesEvent;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesEventService {

    Connection connection = DataSource.getInstance().getConnection();

    public void ajouter(CategoriesEvent category) {
        // Vérifiez si la catégorie existe déjà
        if (isCategoryExists(category.getNom())) {
            System.out.println("Erreur: La catégorie existe déjà.");
            return; // Ou utilisez une alerte ici
        }

        String req = "INSERT INTO Categoriesevent (nom) VALUES (?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, category.getNom());
            pst.executeUpdate();

            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                category.setId(generatedKeys.getInt(1));
            }

            System.out.println("Catégorie ajoutée avec ID: " + category.getId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void modifier(CategoriesEvent category) {
        String req = "UPDATE Categoriesevent SET nom=? WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, category.getNom());
            pst.setInt(2, category.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Catégorie modifiée");
                // Afficher une alerte de succès ici
            } else {
                System.out.println("Aucune catégorie modifiée. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(CategoriesEvent category) {
        // Vérifiez si la catégorie est liée à un événement
        if (isCategoryLinkedToEvent(category.getId())) {
            System.out.println("Erreur: La catégorie ne peut pas être supprimée car elle est liée à un événement.");
            return; // Ou utilisez une alerte ici
        }

        String req = "DELETE FROM Categoriesevent WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, category.getId());

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Catégorie supprimée");
            } else {
                System.out.println("Aucune catégorie supprimée. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<CategoriesEvent> rechercher() {
        List<CategoriesEvent> categories = new ArrayList<>();
        String req = "SELECT * FROM Categoriesevent";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                categories.add(new CategoriesEvent(
                        rs.getInt("id"),
                        rs.getString("nom")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }

    // Méthode pour vérifier si la catégorie est liée à un événement
    public boolean isCategoryLinkedToEvent(int categoryId) {
        String req = "SELECT COUNT(*) FROM events WHERE categorie_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, categoryId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retourne true si la catégorie est liée à un événement
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification de la liaison de la catégorie à un événement: " + e.getMessage());
        }
        return false; // Retourne false si aucune liaison n'est trouvée
    }

    public boolean isCategoryExists(String categoryName) {
        String req = "SELECT COUNT(*) FROM Categoriesevent WHERE nom=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, categoryName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
