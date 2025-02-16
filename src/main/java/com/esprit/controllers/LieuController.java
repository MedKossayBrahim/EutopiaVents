package com.esprit.controllers;

import com.esprit.models.Lieu;
import com.esprit.models.categorie_salle;
import com.esprit.services.CategorieServiceImpl;
import com.esprit.services.LieuServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.SQLException;

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

    private LieuServiceImpl lieuService;
    private CategorieServiceImpl categorieService;
    private ObservableList<Lieu> lieuxList;
    private Lieu selectedLieu;

    @FXML
    public void initialize() throws SQLException {
        lieuService = new LieuServiceImpl();
        categorieService = new CategorieServiceImpl();

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
    }


    private void loadCategories() {
        ObservableList<categorie_salle> categories =
                FXCollections.observableArrayList(categorieService.rechercher());
        cbCategorie.setItems(categories);
        // Personnaliser l'affichage des catégories dans le ComboBox
        cbCategorie.setCellFactory(lv -> new ListCell<categorie_salle>() {
            @Override
            protected void updateItem(categorie_salle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });

        // Personnaliser l'affichage de la catégorie sélectionnée
        cbCategorie.setButtonCell(new ListCell<categorie_salle>() {
            @Override
            protected void updateItem(categorie_salle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
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
                refreshLieuxList();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Lieu ajouté avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    @FXML
    private void modifierLieu() {
        if (validateInput()) {
            try {
                updateLieuFromFields();
                lieuService.modifier(selectedLieu);
                refreshLieuxList();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Lieu modifié avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la modification: " + e.getMessage());
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
                    lieuService.supprimer(selectedLieu);
                    refreshLieuxList();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Lieu supprimé avec succès!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de la suppression: " + e.getMessage());
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
        selectedLieu.setImage(tfImage.getText().trim());
        selectedLieu.setCategorie(cbCategorie.getValue());
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

}