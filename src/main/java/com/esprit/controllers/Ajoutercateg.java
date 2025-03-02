package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class Ajoutercateg {

    @FXML
    private TextField tfcategorie;

    @FXML
    void addcateg(ActionEvent event) throws SQLException {
        String categorieText = tfcategorie.getText().trim();

        if (!validateCategorie(categorieText)) {
            return;
        }

        CategorieService cs = new CategorieService();
        cs.ajouter(new Categorie(categorieText));

        showAlert(Alert.AlertType.INFORMATION, "Confirmation", "Catégorie ajoutée avec succès !");
        tfcategorie.clear(); // Nettoyer le champ après ajout
    }

    private boolean validateCategorie(String text) {
        if (text.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ Catégorie ne peut pas être vide.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}