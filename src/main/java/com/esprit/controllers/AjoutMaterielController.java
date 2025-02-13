package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.services.CategorieService;
import com.esprit.services.MaterielService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

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
    @FXML
    private ImageView imagePreview;

    private final MaterielService materielService;
    private final CategorieService categorieService;

    public AjoutMaterielController() {
        materielService = new MaterielService();
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        if (categorieComboBox != null) {
            loadCategories();
        }
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
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
        }
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
}