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
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
public class photo {

    @FXML private FlowPane flowPanePhotos;
    @FXML private TextField txtUrlImage;
    @FXML private ImageView imagePreview;
    @FXML private ComboBox<Lieu> comboBoxLieux;

    private PhotoLieuServiceImpl photoService = new PhotoLieuServiceImpl();
    private LieuServiceImpl lieuService = new LieuServiceImpl();
    private ObservableList<PhotoLieu> photoList;
    private File selectedFile;
    private PhotoLieu selectedPhoto;
    private String selectedMainImageUrl;
    private static final String WAMP_UPLOAD_DIR = "C:\\wamp64\\www\\images\\";
    private static final String XAMPP_UPLOAD_DIR = "C:\\xampp\\htdocs\\images\\";
    private static final String UPLOAD_DIR = getUploadDir();
    private static final String IMAGE_URL_PREFIX = "http://localhost/images/";

    @FXML
    public void initialize() {
        loadLieux();
        comboBoxLieux.setOnAction(event -> afficherPhotosPourLieu());
    }

    private void loadLieux() {
        ObservableList<Lieu> lieux = FXCollections.observableArrayList(lieuService.rechercher());
        comboBoxLieux.setItems(lieux);
        comboBoxLieux.setConverter(new StringConverter<Lieu>() {
            @Override
            public String toString(Lieu lieu) {
                return lieu != null ? lieu.getNom() : "";
            }

            @Override
            public Lieu fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void afficherPhotosPourLieu() {
        Lieu selectedLieu = comboBoxLieux.getValue();
        if (selectedLieu != null) {
            flowPanePhotos.getChildren().clear();
            photoList = FXCollections.observableArrayList(photoService.rechercherParLieuId(selectedLieu.getId()));

            // Afficher l'image principale depuis Lieu
            if (selectedLieu.getImage() != null && !selectedLieu.getImage().isEmpty()) {
                addImageToFlowPane(selectedLieu.getImage(), true);
            }

            // Afficher les photos supplémentaires depuis photoslieu
            for (PhotoLieu photo : photoList) {
                if (photo.getUrlImage() != null && !photo.getUrlImage().isEmpty()) {
                    addImageToFlowPane(photo.getUrlImage(), false);
                }
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un lieu.");
        }
    }


    private void addImageToFlowPane(String imageUrl, boolean isMainImage) {
        try {
            // Créer une copie finale pour l'utilisation dans le lambda
            final String finalImageUrl;

            // Forcer l'URL HTTP pour l'image principale
            if (isMainImage && !imageUrl.startsWith(IMAGE_URL_PREFIX)) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                finalImageUrl = IMAGE_URL_PREFIX + fileName;
            } else {
                finalImageUrl = imageUrl; // Conserve la valeur originale si non modifiée
            }

            Image image = new Image(finalImageUrl, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(120);
            imageView.setFitWidth(120);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("image-view");

            if (isMainImage) {
                imageView.getStyleClass().add("main-image");
            }

            // Utiliser finalImageUrl dans le lambda
            imageView.setOnMouseClicked(event -> handleImageClick(finalImageUrl, isMainImage));

            flowPanePhotos.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + imageUrl);
            e.printStackTrace();
        }
    }
    private void handleImageClick(String imageUrl, boolean isMainImage) {
        Lieu lieu = comboBoxLieux.getValue();
        if (lieu != null) {
            if (isMainImage) {
                selectedMainImageUrl = imageUrl; // Stocker l'URL de l'image principale
                selectedPhoto = null; // Réinitialiser la photo normale
            } else {
                selectedPhoto = photoList.stream()
                        .filter(p -> p.getUrlImage().equals(imageUrl))
                        .findFirst()
                        .orElse(null);
                selectedMainImageUrl = null; // Réinitialiser l'image principale
            }
            txtUrlImage.setText(imageUrl);
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
            try {
                String imagePath = uploadImage(selectedFile.getAbsolutePath());
                if (imagePath != null && !imagePath.isEmpty()) {
                    PhotoLieu newPhoto = new PhotoLieu(
                            comboBoxLieux.getValue().getId(),
                            imagePath
                    );
                    photoService.ajouter(newPhoto);
                    afficherPhotosPourLieu();
                    clearFields();
                    showAlert("Succès", "Photo ajoutée avec succès !");
                } else {
                    showAlert("Erreur", "Échec de l'upload de l'image.");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Échec de l'ajout de la photo : " + e.getMessage());
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner un lieu et une image !");
        }
    }

    @FXML
    private void modifierPhoto() {
        if (selectedFile == null || comboBoxLieux.getValue() == null) return;
        try {
            String newUrl = uploadImage(selectedFile.getAbsolutePath());
            Lieu lieu = comboBoxLieux.getValue();

            // Cas 1 : Modification de la photo principale
            if (selectedMainImageUrl != null) {
                lieu.setImage(newUrl);
                lieuService.modifier(lieu);
            }
            // Cas 2 : Modification d'une photo secondaire
            else if (selectedPhoto != null) {
                selectedPhoto.setUrlImage(newUrl);
                photoService.modifier(selectedPhoto);
            }

            afficherPhotosPourLieu();
            showAlert("Succès", "Modification réussie !");
        } catch (Exception e) {
            showAlert("Erreur", "Échec: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerPhoto() {
        Lieu lieu = comboBoxLieux.getValue();
        if (lieu == null) return;

        // Cas 1 : Suppression de la photo principale
        if (selectedMainImageUrl != null) {
            lieu.setImage(null);
            lieuService.modifier(lieu);
            showAlert("Succès", "Image principale supprimée !");
            afficherPhotosPourLieu();
        }
        // Cas 2 : Suppression d'une photo secondaire
        else if (selectedPhoto != null) {
            photoService.supprimer(selectedPhoto);
            showAlert("Succès", "Photo supprimée !");
            afficherPhotosPourLieu();
        }
        clearFields();
    }

    @FXML
    private void clearFields() {
        selectedPhoto = null;
        selectedFile = null;
        txtUrlImage.clear();
        imagePreview.setImage(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToReservation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Reservation1View.fxml"));
            txtUrlImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLieu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LieuView.fxml"));
            txtUrlImage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String uploadImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        File sourceFile = new File(imagePath);
        if (!sourceFile.exists()) {
            return null;
        }

        String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
        Path destinationPath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(destinationPath.getParent());
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return IMAGE_URL_PREFIX + fileName;
    }
    // Add this method in both classes:
    private static String getUploadDir() {
        if (Files.exists(Paths.get(WAMP_UPLOAD_DIR))) {
            return WAMP_UPLOAD_DIR;
        } else if (Files.exists(Paths.get(XAMPP_UPLOAD_DIR))) {
            return XAMPP_UPLOAD_DIR;
        } else {
            throw new RuntimeException("Neither WAMP nor XAMPP directory found");
        }
    }
}