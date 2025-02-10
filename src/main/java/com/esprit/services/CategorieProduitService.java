package com.esprit.services;

import com.esprit.models.categorie_produit;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public  class CategorieProduitService implements IService<categorie_produit> {
    Connection connection = DataSource.getInstance().getConnection();

    public CategorieProduitService() {
    }

    @Override
    public void ajouter(categorie_produit categorie_produit) {
        String req = "INSERT INTO categorie_produit (nom, description) VALUES ('" + categorie_produit.getNom() + "', '" + categorie_produit.getDescription() + "')";
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(categorie_produit categorie_produit) {
        String req = "UPDATE categorie_produit SET nom='" + categorie_produit.getNom() + "', description='" + categorie_produit.getDescription() + "' WHERE id=" + categorie_produit.getId();
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(categorie_produit categorie_produit) {
        String req = "DELETE FROM categorie_produit WHERE id=" + categorie_produit.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<categorie_produit> rechercher() {
        List<categorie_produit> categories_produit = new ArrayList<>();
        String req = "SELECT * FROM categorie_produit";

        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                categories_produit.add(new categorie_produit(
                        rs.getInt("id"),
                        rs.getString("nom")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return categories_produit;
    }
}

