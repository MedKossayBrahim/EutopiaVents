package com.esprit.services;

import com.esprit.models.CategoriesEvent;
import com.esprit.utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesEventService {

    Connection connection = DataSource.getInstance().getConnection();

    public CategoriesEventService() throws SQLException {
    }

    public void ajouter(CategoriesEvent category) {
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
            } else {
                System.out.println("Aucune catégorie modifiée. Vérifiez l'ID.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(CategoriesEvent category) {
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
}
