package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.services.CategorieProduitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AjoutCategorie {


    @FXML
    private TextField des;

    @FXML
    private TextField nom;

    @FXML
    void ajouter(ActionEvent event) {
        if (validateInput()) {
            CategorieProduitService ps = null;
            try {
                ps = new CategorieProduitService();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            ps.ajouter(new categorieproduit(nom.getText().trim(), des.getText().trim()));

            showAlert(Alert.AlertType.INFORMATION, "Confirmation", "Catégorie ajoutée avec succès.");
            clearFields();
        }
    }

    private boolean validateInput() {
        String nomValue = nom.getText().trim();
        String desValue = des.getText().trim();

        if (nomValue.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le nom de la catégorie ne peut pas être vide.");
            return false;
        }

        if (desValue.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "La description de la catégorie ne peut pas être vide.");
            return false;
        }

        if (nomValue.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le nom de la catégorie doit contenir au moins 3 caractères.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        nom.clear();
        des.clear();
    }
}