package com.esprit.services;

import com.esprit.models.commande;
import com.esprit.utils.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<commande> {
    Connection connection = DataSource.getInstance().getConnection();

    public CommandeService() throws SQLException {
    }

    @Override
    public void ajouter(commande commande) {
        String req = "INSERT INTO commande (participant_id, produit_id, quantite, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, commande.getClientId());
            pst.setInt(2, commande.getProduitId());
            pst.setInt(3, commande.getQuantite());
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
            System.out.println("Commande ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(commande commande) {
        String req = "UPDATE commande SET participant_id=?, produit_id=?, quantite=? WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, commande.getClientId());
            pst.setInt(2, commande.getProduitId());
            pst.setInt(3, commande.getQuantite());
            pst.setInt(4, commande.getId());
            pst.executeUpdate();
            System.out.println("Commande modifiée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(commande commande) {
        String req = "DELETE FROM commande WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(req)) {
            pst.setInt(1, commande.getId());
            pst.executeUpdate();
            System.out.println("Commande supprimée avec succès.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<commande> rechercher() {
        List<commande> commandes = new ArrayList<>();
        String req = "SELECT * FROM commande";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                commande cmd = new commande(
                    rs.getInt("id"),
                    rs.getInt("participant_id"),
                    rs.getInt("produit_id"),
                    rs.getInt("quantite")
                );
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    cmd.setCreatedAt(createdAt.toLocalDateTime());
                }
                commandes.add(cmd);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return commandes;
    }
}
