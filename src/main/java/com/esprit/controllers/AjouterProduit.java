package com.esprit.controllers;

import com.esprit.models.produit;
import com.esprit.models.categorieproduit;
import com.esprit.services.ProduitService;
import com.esprit.services.CategorieProduitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    private ComboBox<categorieproduit> categorieComboBox;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField imagePathField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Label imageNameLabel;

    private byte[] image;

    private File selectedFile;
    private static final String HTDOCS_PATH = "C:/xampp/htdocs/images/";
    private static final String IMAGE_URL_PREFIX = "http://localhost/images/";
    private final ProduitService produitService;
    private final CategorieProduitService categorieProduitService;

    public AjouterProduit() {
        try {
            produitService = new ProduitService();
            categorieProduitService = new CategorieProduitService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        try {
            // Load categories from database
            List<categorieproduit> categories = categorieProduitService.rechercher();
            categorieComboBox.getItems().addAll(categories);

            // Set up the display format for categories
            categorieComboBox.setConverter(new StringConverter<categorieproduit>() {
                @Override
                public String toString(categorieproduit categorie) {
                    return categorie != null ? categorie.getNom() : "";
                }

                @Override
                public categorieproduit fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            imagePreview.setImage(image);
            imagePathField.setText(selectedFile.getName());
            imageNameLabel.setText("Selected: " + selectedFile.getName());
        }
    }

    @FXML
    private void ajouterProduit() {
        try {
            // Validation
            if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() || 
                prixField.getText().isEmpty() || stockField.getText().isEmpty() || 
                categorieComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont obligatoires");
                return;
            }

            String imageUrl = "";
            if (selectedFile != null) {
                String timestamp = System.currentTimeMillis() + "";
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String newFileName = timestamp + "_" + selectedFile.getName();
                
                File directory = new File(HTDOCS_PATH);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                Path destination = Paths.get(HTDOCS_PATH + newFileName);
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                
                imageUrl = IMAGE_URL_PREFIX + newFileName;
            }

            categorieproduit selectedCategory = categorieComboBox.getValue();
            
            produit produit = new produit(
                0,
                nomField.getText(),
                descriptionField.getText(),
                Double.parseDouble(prixField.getText()),
                Integer.parseInt(stockField.getText()),
                selectedCategory.getId(),
                imageUrl
            );

            produitService.ajouter(produit);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès");
            annuler();
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void annuler() {
        try {
            // Clear all fields
            nomField.clear();
            descriptionField.clear();
            prixField.clear();
            stockField.clear();
            categorieComboBox.setValue(null);
            
            if (imagePathField != null) {
                imagePathField.clear();
            }
            
            if (imagePreview != null) {
                imagePreview.setImage(null);
            }
            
            if (imageNameLabel != null) {
                imageNameLabel.setText("");
            }
            
            selectedFile = null;
            
            // Close the window
            if (nomField != null && nomField.getScene() != null) {
                Stage stage = (Stage) nomField.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}