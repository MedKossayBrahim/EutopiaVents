package com.esprit.services;

import com.esprit.models.categorieproduit;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategorieProduitService implements IService<categorieproduit> {
    Connection connection = DataSource.getInstance().getConnection();

    public CategorieProduitService() throws SQLException {
    }

    @Override
    public void ajouter(categorieproduit categorieproduit) {
        String req = "INSERT INTO categorie_produit (nom, description) VALUES ('" + categorieproduit.getNom() + "', '" + categorieproduit.getDescription() + "')";
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(categorieproduit categorieproduit) {
        String req = "UPDATE categorie_produit SET nom='" + categorieproduit.getNom() + "', description='" + categorieproduit.getDescription() + "' WHERE id=" + categorieproduit.getId();
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(categorieproduit categorieproduit) {
        String req = "DELETE FROM categorie_produit WHERE id=" + categorieproduit.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<categorieproduit> rechercher() {
        List<categorieproduit> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie_produit";

        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                categories.add(new categorieproduit(
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
