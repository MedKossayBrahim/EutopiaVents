package com.esprit.controllers;

import com.esprit.models.Lieu;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LieuDetailsController {
    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label villeLabel;
    @FXML private Label codePostalLabel;
    @FXML private Label capaciteLabel;
    @FXML private Label prixLabel;
    @FXML private Label categorieLabel;
    @FXML private ImageView imageView;

    private Lieu lieu;

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
        loadData();
    }

    private void loadData() {
        nomLabel.setText(lieu.getNom());
        adresseLabel.setText(lieu.getAdresse());
        villeLabel.setText(lieu.getVille());
        codePostalLabel.setText(lieu.getCodePostal());
        capaciteLabel.setText(String.valueOf(lieu.getCapacite()));
        prixLabel.setText(String.format("%.2f €", lieu.getPrix()));
        categorieLabel.setText(lieu.getCategorie().getNom());

        try {
            String imageUrl = lieu.getImage();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "/Images/defaultPlace.png";
            }
            Image image = new Image(imageUrl);
            if (image.isError()) {
                throw new Exception("Error loading image");
            }
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/Images/defaultPlace.png")));
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) nomLabel.getScene().getWindow()).close();
    }
}