package com.esprit.services;

import com.esprit.models.produit;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<produit> {
    Connection connection = DataSource.getInstance().getConnection();

    public ProduitService() {
    }

    @Override
    public void ajouter(produit produit) {
        String req = "INSERT INTO produit (nom, description, prix, stock, categorie_produit_id) VALUES ('"
                + produit.getNom() + "', '" + produit.getDescription() + "', " + produit.getPrix() + ", "
                + produit.getStock() + ", " + produit.getCategorie_produitId() + ")";

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Produit ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(produit produit) {
        String req = "UPDATE produit SET nom='" + produit.getNom() + "', description='" + produit.getDescription()
                + "', prix=" + produit.getPrix() + ", stock=" + produit.getStock() + ", categorie_produit_id="
                + produit.getCategorie_produitId() + " WHERE id=" + produit.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Produit modifié avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(produit produit) {
        String req = "DELETE FROM produit WHERE id=" + produit.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Produit supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<produit> rechercher() {
        List<produit> produits = new ArrayList<>();
        String req = "SELECT * FROM produit";

        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                produits.add(new produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getInt("categorie_produit_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return produits;
    }
}

