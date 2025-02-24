package com.esprit.controllers;

import com.esprit.models.categorie;
import com.esprit.models.produit;
import com.esprit.services.CategorieService;
import com.esprit.services.ProduitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjouterProduit {

    @FXML
    private TextField nomField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField prixField;

    @FXML
    private TextField stockField;

    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private ImageView imageView;

    private byte[] image;

    private Map<String, Integer> categoriesMap = new HashMap<>();

    @FXML
    public void initialize() {
        CategorieService categorieService = new CategorieService();
        List<categorie> categories = categorieService.rechercher();

        // Remplir le ComboBox avec les catégories
        for (categorie cat : categories) {
            String displayText = cat.getNom();
            categorieComboBox.getItems().add(displayText);
            categoriesMap.put(displayText, cat.getId());
        }
    }

    @FXML
    void ajouterProduit(ActionEvent event) {
        try {
            // Validation des champs requis
            if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                    prixField.getText().isEmpty() || stockField.getText().isEmpty() ||
                    categorieComboBox.getValue() == null) {
                showAlert("Erreur", "Champs manquants", "Veuillez remplir tous les champs obligatoires");
                return;
            }

            // Récupérer les valeurs des champs
            String nom = nomField.getText();
            String description = descriptionField.getText();
            double prix = Double.parseDouble(prixField.getText());
            int stock = Integer.parseInt(stockField.getText());

            // Récupérer l'ID de la catégorie sélectionnée
            String selectedCategoryName = categorieComboBox.getValue();
            Integer categorieId = categoriesMap.get(selectedCategoryName);

            if (categorieId == null) {
                showAlert("Erreur", "Catégorie invalide", "Veuillez sélectionner une catégorie valide");
                return;
            }

            // Créer un nouvel objet Produit
            produit produit = new produit(nom, description, prix, stock, categorieId, image);

            // Ajouter le produit via le service
            ProduitService produitService = new ProduitService();
            produitService.ajouter(produit);

            // Afficher une alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Produit ajouté avec succès");
            alert.showAndWait();

            // Fermer la fenêtre
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "Valeurs invalides",
                    "Veuillez vérifier les valeurs numériques saisies (prix et stock)");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                image = Files.readAllBytes(file.toPath());
                Image img = new Image(new ByteArrayInputStream(image));
                imageView.setImage(img);
            } catch (IOException e) {
                showAlert("Erreur fichier", "Impossible de lire le fichier",
                        "Une erreur s'est produite lors de la lecture du fichier.");
            }
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        nomField.clear();
        descriptionField.clear();
        prixField.clear();
        stockField.clear();
        categorieComboBox.setValue(null);
        imageView.setImage(null);
        image = null;
    }
}