package com.esprit.services;

import com.esprit.models.commande;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<commande> {
    Connection connection = DataSource.getInstance().getConnection();

    public CommandeService() {
    }

    @Override
    public void ajouter(commande commande) {
        String req = "INSERT INTO commande (participant_id , produit_id, quantite) VALUES ("
                + commande.getParticipantId() + ", " + commande.getProduitId() + ", " + commande.getQuantite() + ")";

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Commande ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(commande commande) {
        String req = "UPDATE commande SET participant_id=" + commande.getParticipantId()
                + ", produit_id=" + commande.getProduitId() + ", quantite=" + commande.getQuantite()
                + " WHERE id=" + commande.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Commande modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(commande commande) {
        String req = "DELETE FROM commande WHERE id=" + commande.getId();

        try {
            Statement st = this.connection.createStatement();
            st.executeUpdate(req);
            System.out.println("Commande supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<commande> rechercher() {
        List<commande> commandes = new ArrayList<>();
        String req = "SELECT * FROM commande";

        try {
            Statement st = this.connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                commandes.add(new commande(
                        rs.getInt("id"),
                        rs.getInt("participant_id"),
                        rs.getInt("produit_id"),
                        rs.getInt("quantite")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return commandes;
    }
}
