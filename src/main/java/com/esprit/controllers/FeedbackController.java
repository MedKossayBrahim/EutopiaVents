package com.esprit.controllers;

import com.esprit.models.Materiel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class FeedbackController {

    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private Button btnEnvoyer, btnAnnuler;

    private final Materiel materiel;
    private final int userId;

    public FeedbackController(Materiel materiel, int userId) {
        this.materiel = materiel;
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        btnEnvoyer.setOnAction(e -> envoyerFeedback());
        btnAnnuler.setOnAction(e -> fermerFenetre());
    }

    private void envoyerFeedback() {
        String feedback = feedbackTextArea.getText().trim();
        if (feedback.isEmpty()) {
            showAlert("Erreur", "Le champ d'avis est vide !");
            return;
        }

        System.out.println("Avis envoyé pour " + materiel.getLibelle() + " par utilisateur " + userId + " : " + feedback);
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
