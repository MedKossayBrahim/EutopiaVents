package com.esprit.controllers;

import com.esprit.models.categorie_salle;
import com.esprit.services.CategorieServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AjouterCategorie {

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfDescription;
    @FXML
    private TableView<categorie_salle> categoriesTable;
    @FXML
    private TableColumn<categorie_salle, String> nomColumn;
    @FXML
    private TableColumn<categorie_salle, String> descriptionColumn;

    private CategorieServiceImpl categorieService;
    private ObservableList<categorie_salle> categoriesList;

    @FXML
    public void initialize() {
        categorieService = new CategorieServiceImpl();

        // Configuration des colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Chargement des données
        refreshCategoriesList();

        // Double-clic sur une ligne pour voir les détails
        categoriesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && categoriesTable.getSelectionModel().getSelectedItem() != null) {
                navigateToAffichage(categoriesTable.getSelectionModel().getSelectedItem());
            }
        });

        // Validation en temps réel
        tfNom.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNomUnique(newValue);
        });
    }

    private void refreshCategoriesList() {
        categoriesList = FXCollections.observableArrayList(categorieService.rechercher());
        categoriesTable.setItems(categoriesList);
    }

    private void validateNomUnique(String nom) {
        boolean exists = categoriesList.stream()
                .anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom.trim()));

        if (exists) {
            tfNom.setStyle("-fx-border-color: red;");
            showTooltip(tfNom, "Ce nom de catégorie existe déjà!");
        } else {
            tfNom.setStyle("");
            removeTooltip(tfNom);
        }
    }

    @FXML
    void addCategorie(ActionEvent event) {
        if (validateInput()) {
            try {
                categorie_salle nouvelleCategorie = new categorie_salle(
                        tfNom.getText().trim(),
                        tfDescription.getText().trim()
                );

                categorieService.ajouter(nouvelleCategorie);

                // Rafraîchir la liste
                refreshCategoriesList();

                // Vider les champs
                clearFields();

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Catégorie ajoutée avec succès!");

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    void voirCategories() {
        if (!categoriesTable.getItems().isEmpty()) {
            navigateToAffichage(categoriesTable.getItems().get(0));
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information",
                    "Aucune catégorie disponible.");
        }
    }

    private boolean validateInput() {
        String nom = tfNom.getText().trim();
        String description = tfDescription.getText().trim();

        if (nom.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Tous les champs sont obligatoires!");
            return false;
        }

        if (categoriesList.stream().anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom))) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une catégorie avec ce nom existe déjà!");
            return false;
        }

        return true;
    }

    private void showTooltip(Control control, String message) {
        Tooltip tooltip = new Tooltip(message);
        Tooltip.install(control, tooltip);
    }

    private void removeTooltip(Control control) {
        Tooltip.uninstall(control, control.getTooltip());
    }

    private void clearFields() {
        tfNom.clear();
        tfDescription.clear();
        tfNom.setStyle("");
        removeTooltip(tfNom);
    }

    private void navigateToAffichage(categorie_salle categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficheCategorie.fxml"));
            Parent root = loader.load();

            AfficheCategorie ac = loader.getController();
            ac.setCategorieDetails(categorie);

            tfNom.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la navigation: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}