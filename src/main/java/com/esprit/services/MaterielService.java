package com.esprit.services;

import com.esprit.models.Materiel;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaterielService implements IService<Materiel> {

    Connection connection = DataSource.getInstance().getConnection();

    @Override
    public void ajouter(Materiel materiel) {
        String req = "INSERT INTO materiel (libelle, description, quantite, categorie_id, prix, image_url) VALUES ('"
                + materiel.getLibelle() + "', '"
                + materiel.getDescription() + "', "
                + materiel.getQuantite() + ", "
                + materiel.getCategorieId() + ", "
                + materiel.getPrix() + ", '"
                + materiel.getImage_url() + "')";
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Matériel ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Materiel materiel) {
        String req = "UPDATE materiel SET libelle='" + materiel.getLibelle()
                + "', description='" + materiel.getDescription()
                + "', quantite=" + materiel.getQuantite()
                + ", categorie_id=" + materiel.getCategorieId()
                + ", prix=" + materiel.getPrix()
                + ", image_url='" + materiel.getImage_url() + "'"
                + " WHERE id=" + materiel.getId();

        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Matériel modifié avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(Materiel materiel) {
        String req = "DELETE FROM materiel WHERE id=" + materiel.getId();
        try {
            Statement st = connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Matériel supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Materiel> rechercher() {
        List<Materiel> materiels = new ArrayList<>();

        String req = "SELECT M.id, M.libelle, M.description,M.categorie_id, M.quantite, C.nom AS categorie_nom,M.categorie_id, M.prix, M.image_url " +
                "FROM materiel AS M " +
                "JOIN categorie AS C ON M.categorie_id = C.id;";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                materiels.add(new Materiel(
                        rs.getInt("id"),
                        rs.getString("libelle"),
                        rs.getString("description"),
                        rs.getInt("quantite"),
                        rs.getInt("categorie_id"), // Utilisation du nom de la catégorie
                        rs.getDouble("prix"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des matériels : " + e.getMessage());
        }
        return materiels;
    }

}
