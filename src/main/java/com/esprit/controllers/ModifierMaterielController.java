package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.services.CategorieService;
import com.esprit.services.MaterielService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ModifierMaterielController {
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
    private Materiel currentMateriel;

    public ModifierMaterielController() {
        materielService = new MaterielService();
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        categorieComboBox.getItems().addAll(categorieService.rechercher());
    }

    public void setMateriel(Materiel materiel) {
        this.currentMateriel = materiel;
        displayMateriel();
    }

    private void displayMateriel() {
        if (currentMateriel != null) {
            libelleField.setText(currentMateriel.getLibelle());
            descriptionArea.setText(currentMateriel.getDescription());
            quantiteField.setText(String.valueOf(currentMateriel.getQuantite()));
            prixField.setText(String.valueOf(currentMateriel.getPrix()));
            imageUrlField.setText(currentMateriel.getImage_url());
            
            // Set category
            for (Categorie cat : categorieComboBox.getItems()) {
                if (cat.getId() == currentMateriel.getCategorieId()) {
                    categorieComboBox.setValue(cat);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (currentMateriel != null) {
            currentMateriel.setLibelle(libelleField.getText());
            currentMateriel.setDescription(descriptionArea.getText());
            currentMateriel.setQuantite(Integer.parseInt(quantiteField.getText()));
            currentMateriel.setCategorieId(categorieComboBox.getValue().getId());
            currentMateriel.setPrix(Double.parseDouble(prixField.getText()));
            currentMateriel.setImage_url(imageUrlField.getText());

            materielService.modifier(currentMateriel);
        }
    }
} 