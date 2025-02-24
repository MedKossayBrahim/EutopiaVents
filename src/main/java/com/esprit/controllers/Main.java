package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main {

    @FXML
    private void openAjoutCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutCategorie.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCategorie.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeproduit.fxml")); // Path to your ListeProduit.fxml
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