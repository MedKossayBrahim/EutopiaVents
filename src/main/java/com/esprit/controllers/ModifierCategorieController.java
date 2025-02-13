package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ModifierCategorieController {
    @FXML
    private TextField nomField;

    private final CategorieService categorieService;
    private Categorie currentCategorie;

    public ModifierCategorieController() {
        categorieService = new CategorieService();
    }

    public void setCategorie(Categorie categorie) {
        this.currentCategorie = categorie;
        displayCategorie();
    }

    private void displayCategorie() {
        if (currentCategorie != null) {
            nomField.setText(currentCategorie.getNom());
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (currentCategorie != null) {
            currentCategorie.setNom(nomField.getText());
            categorieService.modifier(currentCategorie);
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