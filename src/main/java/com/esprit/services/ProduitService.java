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

    public ProduitService() {
        // no need to get connection here anymore
    }

    @Override
    public void ajouter(produit produit) {
        String req = "INSERT INTO produit (nom, description, prix, stock, categorie_produit_id, image_url) VALUES ('"
                + produit.getNom() + "', '" + produit.getDescription() + "', " + produit.getPrix() + ", "
                + produit.getStock() + ", " + produit.getCategorieId() + ", '" + produit.getImageUrl() + "')";

        try (Connection connection = DataSource.getInstance().getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate(req);
            System.out.println("Produit ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du produit : " + e.getMessage());
        }
    }

    @Override
    public void modifier(produit produit) {
        String req = "UPDATE produit SET nom='" + produit.getNom() + "', description='" + produit.getDescription()
                + "', prix=" + produit.getPrix() + ", stock=" + produit.getStock() + ", categorie_produit_id="
                + produit.getCategorieId() + ", image_url='" + produit.getImageUrl() + "' WHERE id=" + produit.getId();

        try (Connection connection = DataSource.getInstance().getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate(req);
            System.out.println("Produit modifié avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du produit : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(produit produit) {
        String req = "DELETE FROM produit WHERE id=" + produit.getId();

        try (Connection connection = DataSource.getInstance().getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate(req);
            System.out.println("Produit supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du produit : " + e.getMessage());
        }
    }

    @Override
    public List<produit> rechercher() {
        List<produit> produits = new ArrayList<>();
        String req = "SELECT * FROM produit";

        try (Connection connection = DataSource.getInstance().getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                produits.add(new produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getInt("categorie_produit_id"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits : " + e.getMessage());
        }

        return produits;
    }

    public produit getOne(int produitId) {
        String req = "SELECT * FROM produit WHERE id=" + produitId;

        try (Connection connection = DataSource.getInstance().getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            if (rs.next()) {
                return new produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getInt("stock"),
                        rs.getInt("categorie_produit_id"),
                        rs.getString("image_url")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du produit : " + e.getMessage());
        }

        return null;
    }
}
