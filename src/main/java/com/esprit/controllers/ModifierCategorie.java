package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.services.CategorieProduitService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifierCategorie {

    @FXML
    private TextField nomField;
    @FXML
    private TextField descriptionField; // Nouveau champ pour la description

    private final CategorieProduitService CategorieProduitService;
    private categorieproduit currentCategorie;

    public ModifierCategorie() throws SQLException {
        CategorieProduitService = new CategorieProduitService();
    }

    public void setCategorie(categorieproduit categorie) {
        this.currentCategorie = categorie;
        displayCategorie();
    }

    private void displayCategorie() {
        if (currentCategorie != null) {
            nomField.setText(currentCategorie.getNom());
            descriptionField.setText(currentCategorie.getDescription()); // Affichage de la description
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (currentCategorie != null) {
            currentCategorie.setNom(nomField.getText());
            currentCategorie.setDescription(descriptionField.getText()); // Mise à jour de la description
            CategorieProduitService.modifier(currentCategorie);
            closeWindow();
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

}
