package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.models.Feedback;
import com.esprit.services.FeedbackService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;

public class FeedbackController {

    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private Button btnEnvoyer, btnAnnuler;

    @FXML
    private ListView<Feedback> feedbackListView;

    private Materiel materiel;
    private int userId;
    private final FeedbackService feedbackService;

    public FeedbackController() {
        this.feedbackService = new FeedbackService();
    }

    public void setMateriel(Materiel materiel) {
        this.materiel = materiel;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        btnEnvoyer.setOnAction(e -> envoyerFeedback());
        btnAnnuler.setOnAction(e -> fermerFenetre());
        
        feedbackListView.setCellFactory(lv -> new ListCell<Feedback>() {
            @Override
            protected void updateItem(Feedback feedback, boolean empty) {
                super.updateItem(feedback, empty);
                if (empty || feedback == null) {
                    setText(null);
                } else {
                    setText(feedback.getUserName() + " : " + feedback.getContenu());
                }
            }
        });
        
        refreshFeedbacks();
    }

    private void refreshFeedbacks() {
        var feedbacks = feedbackService.getFeedbacksByMateriel(materiel.getId());
        feedbackListView.setItems(FXCollections.observableArrayList(feedbacks));
    }

    private void envoyerFeedback() {
        String contenu = feedbackTextArea.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le champ d'avis est vide !");
            return;
        }

        try {
            Feedback feedback = new Feedback(0, userId, materiel.getId(), contenu);
            feedbackService.ajouter(feedback);
            showSuccess("Succès", "Votre avis a été enregistré avec succès !");
            feedbackTextArea.clear();
            refreshFeedbacks();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement de votre avis.");
        }
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

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
