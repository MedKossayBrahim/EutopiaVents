package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.services.CategorieService;
import com.esprit.services.MaterielService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class AjoutMaterielController {
    @FXML
    private TextField libelleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField quantiteField;
    @FXML
    private ComboBox<Categorie> categorieComboBox;
    @FXML
    private TextField prixField;
    @FXML
    private TextField imageUrlField;

    private final MaterielService materielService;
    private final CategorieService categorieService;

    public AjoutMaterielController() throws SQLException {
        materielService = new MaterielService();
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try {
            categorieComboBox.getItems().clear();
            categorieComboBox.getItems().addAll(categorieService.rechercher());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouterMateriel(ActionEvent event) {
        if (validateFields()) {
            try {
                Materiel materiel = new Materiel(
                        libelleField.getText(),
                        descriptionArea.getText(),
                        Integer.parseInt(quantiteField.getText()),
                        categorieComboBox.getValue().getId(),
                        Double.parseDouble(prixField.getText()),
                        imageUrlField.getText()
                );
                materielService.ajouter(materiel);
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel ajouté avec succès !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    private boolean validateFields() {
        String libelle = libelleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String quantiteStr = quantiteField.getText().trim();
        Categorie categorie = categorieComboBox.getValue();
        String prixStr = prixField.getText().trim();
        String imageUrl = imageUrlField.getText().trim();

        if (libelle.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ Libellé est obligatoire.");
            return false;
        }

        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ Description est obligatoire.");
            return false;
        }

        if (quantiteStr.isEmpty() || !quantiteStr.matches("\\d+")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être un nombre entier positif.");
            return false;
        }

        if (categorie == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner une catégorie.");
            return false;
        }

        if (prixStr.isEmpty() || !prixStr.matches("\\d+(\\.\\d+)?")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le prix doit être un nombre positif.");
            return false;
        }

        if (!isValidUrl(imageUrl)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez entrer une URL valide pour l'image.");
            return false;
        }

        return true;
    }

    private boolean isValidUrl(String url) {
        String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
        return Pattern.matches(urlRegex, url);
    }

    @FXML
    private void clearFields() {
        libelleField.clear();
        descriptionArea.clear();
        quantiteField.clear();
        categorieComboBox.setValue(null);
        prixField.clear();
        imageUrlField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
