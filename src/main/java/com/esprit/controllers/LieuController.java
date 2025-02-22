package com.esprit.controllers;

import com.esprit.models.Lieu;
import com.esprit.models.PhotoLieu;
import com.esprit.models.categorie_salle;
import com.esprit.services.LieuServiceImpl;
import com.esprit.services.CategorieServiceImpl;
import com.esprit.services.PhotoLieuServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Paths;
public class LieuController {

    @FXML private TextField tfNom;
    @FXML private TextField tfAdresse;
    @FXML private TextField tfVille;
    @FXML private TextField tfCodePostal;
    @FXML private TextField tfCapacite;
    @FXML private TextField tfPrix;
    @FXML private TextField tfImage;
    @FXML private ComboBox<categorie_salle> cbCategorie;

    @FXML private TableView<Lieu> lieuxTable;
    @FXML private TableColumn<Lieu, String> nomColumn;
    @FXML private TableColumn<Lieu, String> villeColumn;
    @FXML private TableColumn<Lieu, Integer> capaciteColumn;
    @FXML private TableColumn<Lieu, Double> prixColumn;
    @FXML private TableColumn<Lieu, String> categorieColumn;

    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    private MainMenuController mainMenuController;
    private LieuServiceImpl lieuService;
    private CategorieServiceImpl categorieService;
    private PhotoLieuServiceImpl photoLieuService;
    private ObservableList<Lieu> lieuxList;
    private Lieu selectedLieu;

    private static final String WAMP_UPLOAD_DIR = "C:\\wamp64\\www\\images\\";
    private static final String XAMPP_UPLOAD_DIR = "C:\\xampp\\htdocs\\images\\";
    private static final String UPLOAD_DIR = getUploadDir();
    private static final String IMAGE_URL_PREFIX = "http://localhost/images/";

    @FXML
    public void initialize() {
        lieuService = new LieuServiceImpl();
        categorieService = new CategorieServiceImpl();
        photoLieuService = new PhotoLieuServiceImpl();

        // Configuration des colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        categorieColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCategorie().getNom()));

        // Chargement des catégories dans le ComboBox
        loadCategories();

        // Chargement des lieux
        refreshLieuxList();

        // Gestion de la sélection
        lieuxTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedLieu = newSelection;
                        showLieuDetails(newSelection);
                        btnModifier.setDisable(false);
                        btnSupprimer.setDisable(false);
                        btnAjouter.setDisable(true);
                    }
                });

        tfImage.setOnMouseClicked(event -> choisirImage());
    }

    private void loadCategories() {
        ObservableList<categorie_salle> categories =
                FXCollections.observableArrayList(categorieService.rechercher());
        cbCategorie.setItems(categories);
        cbCategorie.setCellFactory(lv -> new ListCell<categorie_salle>() {
            @Override
            protected void updateItem(categorie_salle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        cbCategorie.setButtonCell(new ListCell<categorie_salle>() {
            @Override
            protected void updateItem(categorie_salle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
    }

    private void refreshLieuxList() {
        lieuxList = FXCollections.observableArrayList(lieuService.rechercher());
        lieuxTable.setItems(lieuxList);
    }

    @FXML
    private void ajouterLieu() {
        if (validateInput()) {
            try {
                Lieu lieu = createLieuFromFields();
                lieuService.ajouter(lieu);

                if (mainMenuController != null) {
                    mainMenuController.refreshLieux();
                }

                refreshLieuxList();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Lieu ajouté avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }
    @FXML
    private void modifierLieu() {
        if (validateInput()) {
            try {
                updateLieuFromFields(); // Ne modifie pas l'image ici

                // Si le champ image n'est PAS vide
                if (!tfImage.getText().trim().isEmpty()) {
                    // Si le texte dans tfImage commence déjà par IMAGE_URL_PREFIX, l'image a déjà été uploadée
                    if (tfImage.getText().startsWith(IMAGE_URL_PREFIX)) {
                        selectedLieu.setImage(tfImage.getText());
                    } else {
                        // L'image est un chemin local, on l'upload
                        String imagePath = uploadImage(tfImage.getText());
                        selectedLieu.setImage(imagePath != null ? imagePath : "");

                        // Mettre à jour la photo associée
                        if (imagePath != null && !imagePath.isEmpty()) {
                            PhotoLieu existingPhoto = photoLieuService.rechercherParLieuId(selectedLieu.getId())
                                    .stream()
                                    .findFirst()
                                    .orElse(null);
                            if (existingPhoto != null) {
                                existingPhoto.setUrlImage(imagePath);
                                photoLieuService.modifier(existingPhoto);
                            } else {
                                PhotoLieu newPhoto = new PhotoLieu(selectedLieu.getId(), imagePath);
                                photoLieuService.ajouter(newPhoto);
                            }
                        }
                    }
                }

                // Mise à jour du lieu dans la base de données
                lieuService.modifier(selectedLieu);

                // Rafraîchir l'affichage
                if (mainMenuController != null) {
                    mainMenuController.refreshLieux();
                }
                refreshLieuxList();
                clearFields();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Lieu modifié avec succès!");

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void supprimerLieu() {
        if (selectedLieu != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Voulez-vous vraiment supprimer ce lieu ?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                try {
                    // Supprimer les photos associées
                    photoLieuService.rechercherParLieuId(selectedLieu.getId())
                            .forEach(photo -> photoLieuService.supprimer(photo));

                    // Supprimer le lieu
                    lieuService.supprimer(selectedLieu);
                    if (mainMenuController != null) {
                        mainMenuController.refreshLieux();
                    }
                    refreshLieuxList();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Lieu supprimé avec succès!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void annuler() {
        clearFields();
    }

    private void showLieuDetails(Lieu lieu) {
        tfNom.setText(lieu.getNom());
        tfAdresse.setText(lieu.getAdresse());
        tfVille.setText(lieu.getVille());
        tfCodePostal.setText(lieu.getCodePostal());
        tfCapacite.setText(String.valueOf(lieu.getCapacite()));
        tfPrix.setText(String.valueOf(lieu.getPrix()));
        tfImage.setText(lieu.getImage());
        cbCategorie.setValue(lieu.getCategorie());
    }

    private void clearFields() {
        tfNom.clear();
        tfAdresse.clear();
        tfVille.clear();
        tfCodePostal.clear();
        tfCapacite.clear();
        tfPrix.clear();
        tfImage.clear();
        cbCategorie.setValue(null);
        selectedLieu = null;
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnAjouter.setDisable(false);
    }

    private boolean validateInput() {
        String errorMessage = "";

        if (tfNom.getText().trim().isEmpty()) errorMessage += "Nom invalide\n";
        if (tfAdresse.getText().trim().isEmpty()) errorMessage += "Adresse invalide\n";
        if (tfVille.getText().trim().isEmpty()) errorMessage += "Ville invalide\n";
        if (tfCodePostal.getText().trim().isEmpty()) errorMessage += "Code postal invalide\n";

        try {
            Integer.parseInt(tfCapacite.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Capacité invalide\n";
        }

        try {
            Double.parseDouble(tfPrix.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Prix invalide\n";
        }

        if (cbCategorie.getValue() == null) errorMessage += "Sélectionnez une catégorie\n";

        if (!errorMessage.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage);
            return false;
        }

        return true;
    }

    private Lieu createLieuFromFields() {
        return new Lieu(
                tfNom.getText().trim(),
                tfAdresse.getText().trim(),
                tfVille.getText().trim(),
                tfCodePostal.getText().trim(),
                Integer.parseInt(tfCapacite.getText().trim()),
                tfImage.getText().trim(),
                cbCategorie.getValue(),
                Double.parseDouble(tfPrix.getText().trim())
        );
    }

    private void updateLieuFromFields() {
        selectedLieu.setNom(tfNom.getText().trim());
        selectedLieu.setAdresse(tfAdresse.getText().trim());
        selectedLieu.setVille(tfVille.getText().trim());
        selectedLieu.setCodePostal(tfCodePostal.getText().trim());
        selectedLieu.setCapacite(Integer.parseInt(tfCapacite.getText().trim()));
        selectedLieu.setPrix(Double.parseDouble(tfPrix.getText().trim()));
        selectedLieu.setCategorie(cbCategorie.getValue());
        // On NE modifie PAS l'image ici !
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToCategorie() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficheCategorie.fxml"));
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPhotoView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/photoView.fxml"));
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String uploadImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return selectedLieu.getImage(); // Retourne l'image existante
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

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Uploader l'image et obtenir l'URL HTTP
                String imageUrl = uploadImage(selectedFile.getAbsolutePath());
                tfImage.setText(imageUrl); // Stocker l'URL, pas le chemin local
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'upload : " + e.getMessage());
            }
        }
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
    public void setMainMenuController(MainMenuController controller) {
        this.mainMenuController = controller;
    }

    public void setLieu(Lieu lieu) {
        this.selectedLieu = lieu;
        showLieuDetails(lieu);
    }
}