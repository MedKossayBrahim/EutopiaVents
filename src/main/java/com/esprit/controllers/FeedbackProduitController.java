package com.esprit.controllers;

import com.esprit.models.produit;
import com.esprit.models.FeedbackProduit;
import com.esprit.services.FeedbackProduitService;
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
import javafx.scene.layout.VBox;
import java.util.List;

public class FeedbackProduitController {

    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private Button btnEnvoyer, btnAnnuler;

    @FXML
    private ListView<FeedbackProduit> feedbackListView;

    private produit produit;
    private int userId;
    private final FeedbackProduitService feedbackService;

    private boolean isEditing = false;
    private FeedbackProduit currentEditingFeedback = null;

    public FeedbackProduitController() {
        this.feedbackService = new FeedbackProduitService();
    }

    public void setProduit(produit produit) {
        this.produit = produit;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        btnEnvoyer.setOnAction(e -> envoyerFeedback());
        btnAnnuler.setOnAction(e -> fermerFenetre());

        feedbackListView.setCellFactory(lv -> new ListCell<FeedbackProduit>() {
            @Override
            protected void updateItem(FeedbackProduit feedback, boolean empty) {
                super.updateItem(feedback, empty);
                if (empty || feedback == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    VBox contentBox = new VBox(5);

                    // Display name and content
                    HBox textContainer = new HBox(5);
                    Label nameLabel = new Label(feedback.getUserName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    Label separator = new Label(" : ");
                    Label contentLabel = new Label(feedback.getComment());
                    contentLabel.setWrapText(true);
                    contentLabel.setPrefWidth(280);
                    textContainer.getChildren().addAll(nameLabel, separator, contentLabel);

                    // Display stars based on rating
                    HBox starsBox = new HBox(2);
                    for (int i = 1; i <= 5; i++) {
                        Label star = new Label(i <= feedback.getRating() ? "★" : "☆");
                        star.setStyle("-fx-text-fill: gold; -fx-font-size: 14px;");
                        starsBox.getChildren().add(star);
                    }

                    contentBox.getChildren().addAll(textContainer, starsBox);
                    container.getChildren().add(contentBox);

                    // Add delete button for user's own feedback
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
                FeedbackProduit selectedFeedback = feedbackListView.getSelectionModel().getSelectedItem();
                if (selectedFeedback != null && selectedFeedback.getUserId() == userId) {
                    startEdit(selectedFeedback);
                }
            }
        });

        refreshFeedbacks();
    }

    private void refreshFeedbacks() {
        var feedbacks = feedbackService.getFeedbacksByProduit(produit.getId());
        feedbackListView.setItems(FXCollections.observableArrayList(feedbacks));
    }

    private void startEdit(FeedbackProduit feedback) {
        isEditing = true;
        currentEditingFeedback = feedback;
        feedbackTextArea.setText(feedback.getComment());
        btnEnvoyer.setText("Modifier");
    }

    private void deleteFeedback(FeedbackProduit feedback) {
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
            // Enhanced rating calculation with debug output
            int rating = calculateRating(contenu);
            System.out.println("Feedback text: \"" + contenu + "\"");
            System.out.println("Calculated rating: " + rating);

            if (isEditing) {
                currentEditingFeedback.setComment(contenu);
                currentEditingFeedback.setRating(rating);
                feedbackService.modifier(currentEditingFeedback);
                showSuccess("Succès", "Votre avis a été modifié avec succès !");
                isEditing = false;
                currentEditingFeedback = null;
                btnEnvoyer.setText("Envoyer");
            } else {
                FeedbackProduit feedback = new FeedbackProduit(0, userId, produit.getId(), contenu, rating);
                feedbackService.ajouter(feedback);
                showSuccess("Succès", "Votre avis a été enregistré avec succès !");
            }
            feedbackTextArea.clear();
            refreshFeedbacks();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de l'enregistrement de votre avis.");
            e.printStackTrace();
        }
    }

    // Enhanced rating calculation based on text sentiment
    private int calculateRating(String text) {
        // This is a simplified and more direct implementation
        text = text.toLowerCase();
        
        System.out.println("Analyzing sentiment for: \"" + text + "\"");
        
        // Check for explicit rating mentions first
        if (text.contains("5 star") || text.contains("5 étoile") || text.contains("cinq étoile") || 
            text.contains("5/5") || text.contains("5 sur 5")) {
            System.out.println("Found explicit 5-star rating");
            return 5;
        }
        if (text.contains("4 star") || text.contains("4 étoile") || text.contains("quatre étoile") || 
            text.contains("4/5") || text.contains("4 sur 5")) {
            System.out.println("Found explicit 4-star rating");
            return 4;
        }
        if (text.contains("3 star") || text.contains("3 étoile") || text.contains("trois étoile") || 
            text.contains("3/5") || text.contains("3 sur 5")) {
            System.out.println("Found explicit 3-star rating");
            return 3;
        }
        if (text.contains("2 star") || text.contains("2 étoile") || text.contains("deux étoile") || 
            text.contains("2/5") || text.contains("2 sur 5")) {
            System.out.println("Found explicit 2-star rating");
            return 2;
        }
        if (text.contains("1 star") || text.contains("1 étoile") || text.contains("une étoile") || 
            text.contains("1/5") || text.contains("1 sur 5") || text.contains("0/5") || 
            text.contains("0 étoile") || text.contains("zéro étoile")) {
            System.out.println("Found explicit 1-star rating");
            return 1;
        }
        
        // Direct keyword matching for extreme ratings
        String[] veryPositiveKeywords = {"excellent", "parfait", "génial", "superbe", "magnifique", "formidable", "merveilleux", "adore", "j'adore", "incroyable", "5 étoiles", "5/5", "perfect", "amazing", "awesome", "love it", "fantastic"};
        String[] positiveKeywords = {"bien", "bon", "super", "agréable", "satisfait", "content", "heureux", "recommande", "aime", "j'aime", "pratique", "utile", "efficace", "good", "nice", "great", "useful", "happy", "recommend"};
        String[] veryNegativeKeywords = {"horrible", "terrible", "catastrophique", "affreux", "déteste", "je déteste", "nul", "pire", "cauchemar", "arnaque", "dégoûtant", "immonde", "inutilisable", "0/5", "1/5", "sucks", "trash", "garbage", "awful", "worst", "hate", "useless", "crap", "rubbish", "junk", "waste", "terrible"};
        String[] negativeKeywords = {"mauvais", "décevant", "déçu", "problème", "défaut", "cassé", "cher", "lent", "difficile", "compliqué", "n'aime pas", "pas aimé", "ne recommande pas", "bad", "disappointing", "expensive", "slow", "difficult", "complicated", "don't like", "not good", "poor"};
        
        // Check for direct matches first (most reliable)
        for (String keyword : veryPositiveKeywords) {
            if (text.contains(keyword)) {
                System.out.println("Found very positive keyword: " + keyword);
                return 5; // Very positive
            }
        }
        
        for (String keyword : veryNegativeKeywords) {
            if (text.contains(keyword)) {
                System.out.println("Found very negative keyword: " + keyword);
                return 1; // Very negative
            }
        }
        
        // Count positive and negative matches
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String keyword : positiveKeywords) {
            if (text.contains(keyword)) {
                System.out.println("Found positive keyword: " + keyword);
                positiveCount++;
            }
        }
        
        for (String keyword : negativeKeywords) {
            if (text.contains(keyword)) {
                System.out.println("Found negative keyword: " + keyword);
                negativeCount++;
            }
        }
        
        System.out.println("Positive count: " + positiveCount + ", Negative count: " + negativeCount);
        
        // Check for negations that might flip sentiment
        String[] negations = {"ne pas", "n'est pas", "n'a pas", "pas", "jamais", "aucun", "aucune"};
        for (String negation : negations) {
            if (text.contains(negation)) {
                System.out.println("Found negation: " + negation);
                // Check if negation applies to positive words
                for (String keyword : positiveKeywords) {
                    if (text.contains(keyword) && isNearWord(text, negation, keyword, 3)) {
                        System.out.println("Negation applies to positive word: " + keyword);
                        positiveCount--;
                        negativeCount++;
                    }
                }
                // Check if negation applies to negative words
                for (String keyword : negativeKeywords) {
                    if (text.contains(keyword) && isNearWord(text, negation, keyword, 3)) {
                        System.out.println("Negation applies to negative word: " + keyword);
                        negativeCount--;
                        positiveCount++;
                    }
                }
            }
        }
        
        System.out.println("After negation processing - Positive count: " + positiveCount + ", Negative count: " + negativeCount);
        
        // Determine rating based on counts
        if (positiveCount > 0 && negativeCount == 0) {
            return positiveCount >= 2 ? 5 : 4; // Very positive or positive
        } else if (negativeCount > 0 && positiveCount == 0) {
            return negativeCount >= 2 ? 1 : 2; // Very negative or negative
        } else if (positiveCount > negativeCount) {
            return 4; // More positive than negative
        } else if (negativeCount > positiveCount) {
            return 2; // More negative than positive
        } else {
            // If we get here, it's truly neutral or mixed
            return 3;
        }
    }
    
    // Helper method to check if two words are near each other
    private boolean isNearWord(String text, String word1, String word2, int maxDistance) {
        int index1 = text.indexOf(word1);
        int index2 = text.indexOf(word2);
        
        if (index1 < 0 || index2 < 0) return false;
        
        // Calculate word distance (in characters)
        int distance = Math.abs(index1 - index2);
        return distance <= maxDistance * 5; // Approximate character count
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