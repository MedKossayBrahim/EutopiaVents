package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.models.produit;
import com.esprit.services.CategorieProduitService;
import com.esprit.services.CategorieService;
import com.esprit.services.ProduitService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.*;
import java.sql.SQLException;
import java.util.List;

public class ModifierProduit {

    @FXML
    private TextField nomField; // Changed from libelleField to nomField
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField stockField; // Changed from quantiteField to stockField
    @FXML
    private ComboBox<categorieproduit> categorieComboBox;
    @FXML
    private TextField prixField;
    @FXML
    private ImageView imageView; // Assuming you want to display the image
    @FXML
    private Button uploadImageButton; // Button to upload image

    private final ProduitService produitService;
    private final CategorieProduitService categorieProduitService;
    private produit currentProduit;
    private byte[] imageBytes;

    public ModifierProduit() throws SQLException {
        produitService = new ProduitService(); // Changed from MaterielService to ProduitService
        categorieProduitService = new CategorieProduitService();
    }

    @FXML
    public void initialize() {
        // Vérifier que l'ImageView est correctement injecté
        if (imageView == null) {
            System.out.println("Warning: ImageView was not injected by FXML loader");
            imageView = new ImageView();
        }

        loadCategories();

        // Configuration initiale de l'ImageView
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
    }

    private void loadCategories() {
        List<categorieproduit> categories = categorieProduitService.rechercher();
        categorieComboBox.getItems().addAll(categories);
    }

    public void setProduit(produit produit) { // Changed from setMateriel to setProduit
        this.currentProduit = produit; // Changed from currentMateriel to currentProduit
        displayProduit(); // Changed from displayMateriel to displayProduit
    }

    private void displayProduit() {
        if (currentProduit != null) {
            nomField.setText(currentProduit.getNom());
            descriptionArea.setText(currentProduit.getDescription());
            stockField.setText(String.valueOf(currentProduit.getStock()));
            prixField.setText(String.valueOf(currentProduit.getPrix()));

            // Set category
            for (categorieproduit cat : categorieComboBox.getItems()) {
                if (cat.getId() == currentProduit.getCategorieId()) {
                    categorieComboBox.setValue(cat);
                    break;
                }
            }

            // Display image if available
            if (currentProduit.getImage() != null) {
                try {
                    Image image = new Image(new ByteArrayInputStream(currentProduit.getImage()));
                    imageView.setImage(image);
                } catch (Exception e) {
                    System.out.println("Erreur lors du chargement de l'image : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleSauvegarder() {
        if (currentProduit != null) { // Changed from currentMateriel to currentProduit
            currentProduit.setNom(nomField.getText()); // Changed from setLibelle to setNom
            currentProduit.setDescription(descriptionArea.getText());
            currentProduit.setStock(Integer.parseInt(stockField.getText())); // Changed from setQuantite to setStock
            currentProduit.setCategorieId(categorieComboBox.getValue().getId());
            currentProduit.setPrix(Double.parseDouble(prixField.getText()));

            // Assuming you have a method to get the image as byte[]
            // currentProduit.setImage(getImageBytes()); // Update this line to get the image bytes

            produitService.modifier(currentProduit); // Changed from materielService to produitService

            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Produit modifié avec succès");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleImageUpload() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                // Créer un nouvel ImageView si null
                if (imageView == null) {
                    imageView = new ImageView();
                }

                // Charger l'image
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);

                // Convertir l'image en bytes pour le stockage
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        bos.write(buf, 0, readNum);
                    }
                    imageBytes = bos.toByteArray();
                }
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement de l'image : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}