package com.esprit.controllers;

import com.esprit.models.Role;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

    @FXML
    private Button btnAjoutCategorie;
    @FXML
    private Button btnListeCategorie;
    @FXML
    private Button btnAjoutProduit;
    @FXML
    private Button btnListeProduit;
    @FXML
    private Button btnListeCommande;

    @FXML
    public void initialize() {
        try {
//            JSONParser parser = new JSONParser();
//            JSONObject userSession = (JSONObject) parser.parse(new FileReader("user_session.json"));
//            String userRole = (String) userSession.get("role");

         //   if (!"Admin".equals(userRole)) {
               if (!(Eutopia.getCurrentUser().getRole() == Role.Admin)) {

                // Hide admin-only buttons
                btnAjoutCategorie.setVisible(false);
                btnAjoutCategorie.setManaged(false);
                btnListeCategorie.setVisible(false);
                btnListeCategorie.setManaged(false);
                btnAjoutProduit.setVisible(false);
                btnAjoutProduit.setManaged(false);
            }
            // btnListeProduit and btnListeCommande remain visible for all users
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading user session: " + e.getMessage());
        }
    }

    @FXML
    private void openAjoutCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutcategorieproduit.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void openListeCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listecategorieproduit.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Catégories");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    // New methods for managing products
    @FXML
    private void openAjoutProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterProduit.fxml")); // Path to your AjoutProduit.fxml
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void openListeProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeProduit.fxml")); // Path to your ListeProduit.fxml
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Produits");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    // New methods for managing commands
    @FXML
    private void openAjoutCommande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCommande.fxml")); // Path to your AjoutCommande.fxml
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Commande");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void openListeCommande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paniers.fxml")); // Path to your ListeCommande.fxml
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Commandes");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }
}