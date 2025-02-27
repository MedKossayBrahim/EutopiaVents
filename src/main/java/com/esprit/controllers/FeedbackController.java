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
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;

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

    private boolean isEditing = false;
    private Feedback currentEditingFeedback = null;

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
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLabel = new Label(feedback.getUserName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    
                    Label separator = new Label(" : ");
                    
                    Label contentLabel = new Label(feedback.getContenu());
                    contentLabel.setWrapText(true);
                    contentLabel.setPrefWidth(280);
                    
                    HBox textContainer = new HBox(5);
                    textContainer.getChildren().addAll(nameLabel, separator, contentLabel);
                    container.getChildren().add(textContainer);

                    if (feedback.getUserId() == userId) {
                        Button deleteBtn = new Button("✖");
                        deleteBtn.setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: #ff4444;" +
                            "-fx-font-size: 14px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 5 10;" +
                            "-fx-border-radius: 15;" +
                            "-fx-background-radius: 15"
                        );
                        
                        deleteBtn.setOnMouseEntered(e -> 
                            deleteBtn.setStyle(
                                "-fx-background-color: #ff4444;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5 10;" +
                                "-fx-border-radius: 15;" +
                                "-fx-background-radius: 15"
                            )
                        );
                        
                        deleteBtn.setOnMouseExited(e -> 
                            deleteBtn.setStyle(
                                "-fx-background-color: transparent;" +
                                "-fx-text-fill: #ff4444;" +
                                "-fx-font-size: 14px;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 5 10;" +
                                "-fx-border-radius: 15;" +
                                "-fx-background-radius: 15"
                            )
                        );

                        deleteBtn.setOnAction(e -> deleteFeedback(feedback));
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        container.getChildren().addAll(spacer, deleteBtn);
                    }
                    
                    setGraphic(container);
                }
            }
        });

        feedbackListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Feedback selectedFeedback = feedbackListView.getSelectionModel().getSelectedItem();
                if (selectedFeedback != null && selectedFeedback.getUserId() == userId) {
                    startEdit(selectedFeedback);
                }
            }
        });
        
        refreshFeedbacks();
    }

    private void refreshFeedbacks() {
        var feedbacks = feedbackService.getFeedbacksByMateriel(materiel.getId());
        feedbackListView.setItems(FXCollections.observableArrayList(feedbacks));
    }

    private void startEdit(Feedback feedback) {
        isEditing = true;
        currentEditingFeedback = feedback;
        feedbackTextArea.setText(feedback.getContenu());
        btnEnvoyer.setText("Modifier");
    }

    private void deleteFeedback(Feedback feedback) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Supprimer l'avis");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cet avis ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    feedbackService.supprimer(feedback.getId());
                    showSuccess("Succès", "Votre avis a été supprimé avec succès !");
                    refreshFeedbacks();
                } catch (Exception e) {
                    showAlert("Erreur", "Une erreur est survenue lors de la suppression de l'avis.");
                }
            }
        });
    }

    private void envoyerFeedback() {
        String contenu = feedbackTextArea.getText().trim();
        if (contenu.isEmpty()) {
            showAlert("Erreur", "Le champ d'avis est vide !");
            return;
        }

        try {
            if (isEditing) {
                currentEditingFeedback.setContenu(contenu);
                feedbackService.modifier(currentEditingFeedback);
                showSuccess("Succès", "Votre avis a été modifié avec succès !");
                isEditing = false;
                currentEditingFeedback = null;
                btnEnvoyer.setText("Envoyer");
            } else {
                Feedback feedback = new Feedback(0, userId, materiel.getId(), contenu);
                feedbackService.ajouter(feedback);
                showSuccess("Succès", "Votre avis a été enregistré avec succès !");
            }
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
