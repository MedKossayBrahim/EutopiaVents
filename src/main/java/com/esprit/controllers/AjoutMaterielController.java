package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.models.Materiel;
import com.esprit.services.CategorieService;
import com.esprit.services.MaterielService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private ImageView imagePreview;

    private File selectedImageFile; // Fichier image sélectionné
    private final MaterielService materielService;
    private final CategorieService categorieService;

    public AjoutMaterielController() throws SQLException {
        materielService = new MaterielService();
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        loadCategories();

        // Configurer l'affichage de la ComboBox pour montrer uniquement le nom de la catégorie
        categorieComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getNom());
            }
        });

        categorieComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getNom());
            }
        });
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
                Categorie selectedCategorie = categorieComboBox.getValue();
                if (selectedCategorie == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une catégorie valide.");
                    return;
                }

                // Copier l'image dans le dossier C:\xampp\htdocs\img
                String imageUrl = copyImageToServer();

                Materiel materiel = new Materiel(
                        libelleField.getText(),
                        descriptionArea.getText(),
                        Integer.parseInt(quantiteField.getText()),
                        selectedCategorie.getId(), // Utiliser l'ID de la catégorie sélectionnée
                        Double.parseDouble(prixField.getText()),
                        imageUrl
                );
                materielService.ajouter(materiel);
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel ajouté avec succès !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            // Afficher l'aperçu de l'image
            Image image = new Image(selectedImageFile.toURI().toString());
            imagePreview.setImage(image);
        }
    }
    private String copyImageToServer() throws IOException {
        if (selectedImageFile == null) {
            throw new IllegalArgumentException("Aucune image sélectionnée.");
        }

        // Chemin du dossier de destination
        String destinationDir = "C:/xampp/htdocs/img/";
        Path destinationPath = Paths.get(destinationDir + selectedImageFile.getName());

        // Copier le fichier
        Files.copy(selectedImageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Retourner l'URL de l'image
        return "http://localhost/img/" + selectedImageFile.getName();
    }

    private boolean validateFields() {
        String libelle = libelleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String quantiteStr = quantiteField.getText().trim();
        Categorie categorie = categorieComboBox.getValue();
        String prixStr = prixField.getText().trim();

        if (libelle.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ Libellé est obligatoire.");
            return false;
        }

        // Vérification de l'unicité du libellé
        if (!materielService.isLibelleUnique(libelle)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Ce libellé existe déjà. Veuillez en choisir un autre.");
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

        if (selectedImageFile == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner une image.");
            return false;
        }

        return true;
    }


    @FXML
    private void clearFields() {
        libelleField.clear();
        descriptionArea.clear();
        quantiteField.clear();
        categorieComboBox.setValue(null);
        prixField.clear();
        imagePreview.setImage(null);
        selectedImageFile = null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}