package com.esprit.services;

import com.esprit.models.categorie;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<categorie> {
    Connection connection = DataSource.getInstance().getConnection();

    public CategorieService() {
    }

    @Override
    public void ajouter(categorie categorie) {
        String req = "INSERT INTO categorie (nom, description) VALUES ('" + categorie.getNom() + "', '" + categorie.getDescription() + "')";
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(categorie categorie) {
        String req = "UPDATE categorie SET nom='" + categorie.getNom() + "', description='" + categorie.getDescription() + "' WHERE id=" + categorie.getId();
        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(categorie categorie) {
        String req = "DELETE FROM categorie WHERE id=" + categorie;

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<categorie> rechercher() {
        List<categorie> categories = new ArrayList<>();
        String req = "SELECT * FROM categorie";

        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                categories.add(new categorie(
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
