package com.esprit.services;

import com.esprit.models.Categorie;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Categorie categorie) {
        String req = "INSERT INTO categorie (nom) VALUES ('"
                + categorie.getNom() + "')";
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Categorie categorie) {
        String req = "UPDATE categorie SET nom='" + categorie.getNom()
                + "' WHERE id=" + categorie.getId();
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Categorie categorie) {
        String req = "DELETE FROM categorie WHERE id=" + categorie.getId();
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Catégorie supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Categorie> rechercher() {
        List<Categorie> categories = new ArrayList<>();

        String req = "SELECT * FROM categorie";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                categories.add(new Categorie(
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
