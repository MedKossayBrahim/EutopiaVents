package com.esprit.controllers;

import com.esprit.models.PhotoLieu;
import com.esprit.services.PhotoLieuServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class photo {

    @FXML
    private TableView<PhotoLieu> tablePhotos;

    @FXML
    private TableColumn<PhotoLieu, Integer> colId;

    @FXML
    private TableColumn<PhotoLieu, Integer> colLieuId;

    @FXML
    private TableColumn<PhotoLieu, String> colUrl;

    @FXML
    private TextField txtLieuId;

    @FXML
    private TextField txtUrlImage;

    private PhotoLieuServiceImpl photoService = new PhotoLieuServiceImpl();
    private ObservableList<PhotoLieu> photoList;

    @FXML
    public void initialize() {
        // Configuration des colonnes de la TableView
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colLieuId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getLieuId()).asObject());
        colUrl.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUrlImage()));

        // Charger les photos depuis la base
        loadPhotos();

        // Lorsqu'une photo est sélectionnée dans le tableau, remplir les champs de saisie
        tablePhotos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtLieuId.setText(String.valueOf(newSelection.getLieuId()));
                txtUrlImage.setText(newSelection.getUrlImage());
            }
        });
    }

    private void loadPhotos() {
        photoList = FXCollections.observableArrayList(photoService.rechercher());
        tablePhotos.setItems(photoList);
    }

    @FXML
    private void ajouterPhoto() {
        try {
            int lieuId = Integer.parseInt(txtLieuId.getText());
            String url = txtUrlImage.getText();
            PhotoLieu photoObj = new PhotoLieu(lieuId, url);
            photoService.ajouter(photoObj);
            loadPhotos();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le Lieu ID doit être un nombre.");
        }
    }

    @FXML
    private void modifierPhoto() {
        PhotoLieu selected = tablePhotos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int lieuId = Integer.parseInt(txtLieuId.getText());
                String url = txtUrlImage.getText();
                selected.setLieuId(lieuId);
                selected.setUrlImage(url);
                photoService.modifier(selected);
                loadPhotos();
                clearFields();
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le Lieu ID doit être un nombre.");
            }
        } else {
            showAlert("Erreur", "Veuillez sélectionner une photo à modifier.");
        }
    }

    @FXML
    private void supprimerPhoto() {
        PhotoLieu selected = tablePhotos.getSelectionModel().getSelectedItem();
        if (selected != null) {
            photoService.supprimer(selected);
            loadPhotos();
            clearFields();
        } else {
            showAlert("Erreur", "Veuillez sélectionner une photo à supprimer.");
        }
    }

    private void clearFields() {
        txtLieuId.clear();
        txtUrlImage.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
