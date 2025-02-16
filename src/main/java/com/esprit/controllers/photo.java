package com.esprit.controllers;

import com.esprit.models.PhotoLieu;
import com.esprit.models.Lieu;
import com.esprit.services.PhotoLieuServiceImpl;
import com.esprit.services.LieuServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;

public class photo {

    @FXML
    private FlowPane flowPanePhotos;
    @FXML
    private TextField txtUrlImage;
    @FXML
    private ImageView imagePreview;
    @FXML
    private ComboBox<Lieu> comboBoxLieux;

    private PhotoLieuServiceImpl photoService = new PhotoLieuServiceImpl();
    private LieuServiceImpl lieuService = new LieuServiceImpl();
    private ObservableList<PhotoLieu> photoList;
    private File selectedFile;
    private PhotoLieu selectedPhoto; // Nouvelle variable pour tracker la sélection

    @FXML
    public void initialize() {
        loadLieux();
    }

    private void loadLieux() {
        ObservableList<Lieu> lieux = FXCollections.observableArrayList(lieuService.rechercher());
        comboBoxLieux.setItems(lieux);
    }

    @FXML
    private void afficherPhotosPourLieu() {
        Lieu selectedLieu = comboBoxLieux.getValue();
        if (selectedLieu != null) {
            flowPanePhotos.getChildren().clear();
            // Appel direct au service filtré
            photoList = FXCollections.observableArrayList(photoService.rechercherParLieuId(selectedLieu.getId()));

            for (PhotoLieu photo : photoList) {
                ImageView imageView = new ImageView(new Image("file:" + photo.getUrlImage()));
                imageView.setFitHeight(120);
                imageView.setFitWidth(120);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("image-view");

                imageView.setOnMouseClicked(event -> {
                    selectedPhoto = photo;
                    txtUrlImage.setText(photo.getUrlImage());
                    imagePreview.setImage(new Image("file:" + photo.getUrlImage()));
                    showAlert("Information", "Sélectionnez une nouvelle image puis cliquez sur Modifier");
                });

                flowPanePhotos.getChildren().add(imageView);
            }
        }
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            txtUrlImage.setText(selectedFile.getAbsolutePath());
            afficherImage(selectedFile.getAbsolutePath());
        }
    }

    private void afficherImage(String path) {
        try {
            Image image = new Image("file:" + path);
            imagePreview.setImage(image);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger l'image : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterPhoto() {
        if (selectedFile != null && comboBoxLieux.getValue() != null) {
            PhotoLieu newPhoto = new PhotoLieu(
                    comboBoxLieux.getValue().getId(),
                    selectedFile.getAbsolutePath()
            );
            photoService.ajouter(newPhoto);
            afficherPhotosPourLieu();
            clearFields();
            showAlert("Succès", "Photo ajoutée avec succès !");
        } else {
            showAlert("Erreur", "Veuillez sélectionner un lieu et une image !");
        }
    }

    @FXML
    private void modifierPhoto() {
        if (selectedPhoto != null && selectedFile != null) {
            try {
                // Mettre à jour avec la nouvelle image
                selectedPhoto.setUrlImage(selectedFile.getAbsolutePath());
                photoService.modifier(selectedPhoto);
                afficherPhotosPourLieu();
                showAlert("Succès", "Photo modifiée avec succès !");
                clearFields();
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la modification : " + e.getMessage());
            }
        } else {
            showAlert("Avertissement", "Sélectionnez une photo et choisissez une nouvelle image !");
        }
    }

    @FXML
    private void supprimerPhoto() {
        if (selectedPhoto != null) {
            photoService.supprimer(selectedPhoto);
            afficherPhotosPourLieu();
            clearFields();
            showAlert("Succès", "Photo supprimée avec succès !");
        } else {
            showAlert("Erreur", "Aucune photo sélectionnée !");
        }
    }

    @FXML
    private void clearFields() {
        selectedPhoto = null;
        selectedFile = null;
        txtUrlImage.clear();
        imagePreview.setImage(null);
        comboBoxLieux.getSelectionModel().clearSelection();
        flowPanePhotos.getChildren().clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    //navigation vers reservation
    @FXML
    private void goToReservation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Reservation1View.fxml"));
            txtUrlImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //navigation retour vers lieu
    @FXML
    private void goToLieu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LieuView.fxml"));
            txtUrlImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}