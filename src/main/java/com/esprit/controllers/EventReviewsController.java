package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.models.EventReview;
import com.esprit.models.User;
import com.esprit.services.EvenementService;
import com.esprit.services.EventReviewService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EventReviewsController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox eventsContainer;

    @FXML
    private Label titleLabel;

    @FXML
    private Button retourButton;

    private EvenementService evenementService = new EvenementService();
    private EventReviewService reviewService = new EventReviewService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) {
            showNoUserMessage();
            return;
        }

        loadPastEvents();
    }

    private void showNoUserMessage() {
        eventsContainer.getChildren().clear();
        Label messageLabel = new Label("Veuillez vous connecter pour voir vos événements passés");
        messageLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
        eventsContainer.getChildren().add(messageLabel);
    }

    private void loadPastEvents() {
        eventsContainer.getChildren().clear();

        List<Evenement> pastEvents = evenementService.rechercherEvenementsPassesParUtilisateur(currentUser.getUserID());

        if (pastEvents.isEmpty()) {
            Label emptyLabel = new Label("Vous n'avez pas participé à des événements passés");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
            eventsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Evenement event : pastEvents) {
            VBox eventCard = createEventCard(event);
            eventsContainer.getChildren().add(eventCard);
        }
    }

    private VBox createEventCard(Evenement evenement) {
        VBox eventBox = new VBox();
        eventBox.setPadding(new Insets(15));
        eventBox.setSpacing(10);
        eventBox.setMaxWidth(Double.MAX_VALUE);
        eventBox.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Event header with image and info
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // Load image directly
        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            imageView.setImage(new Image(evenement.getImage()));
        }

        // Event info
        VBox infoBox = new VBox(5);

        Label titleLabel = new Label(evenement.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label dateLabel = new Label("Du " + evenement.getDateDebut() + " au " + evenement.getDateFin());
        dateLabel.setStyle("-fx-text-fill: #666;");

        String lieu = evenement.getLieuId() != 0 ? evenement.getLieuNom() : evenement.getLieu_proprietaire();
        Label lieuLabel = new Label("Lieu: " + lieu);
        lieuLabel.setStyle("-fx-text-fill: #666;");

        // Ajouter un bouton pour voir toutes les reviews
        Button viewAllReviewsBtn = new Button("Voir tous les avis");
        viewAllReviewsBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        viewAllReviewsBtn.setOnAction(e -> showAllReviews(evenement));

        infoBox.getChildren().addAll(titleLabel, dateLabel, lieuLabel, viewAllReviewsBtn);

        headerBox.getChildren().addAll(imageView, infoBox);

        // Check if user has already reviewed this event
        boolean hasReviewed = reviewService.utilisateurADejaEvalue(currentUser.getUserID(), evenement.getId());

        // Review section
        VBox reviewBox = new VBox(10);
        reviewBox.setPadding(new Insets(10, 0, 0, 0));

        if (hasReviewed) {
            // Show existing review
            List<EventReview> userReviews = reviewService.rechercherParUtilisateur(currentUser.getUserID())
                    .stream()
                    .filter(r -> r.getEvenementId() == evenement.getId())
                    .toList();

            if (!userReviews.isEmpty()) {
                EventReview userReview = userReviews.get(0);

                Label reviewTitle = new Label("Votre avis");
                reviewTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

                HBox ratingBox = createRatingDisplay(userReview.getNote());

                Label commentLabel = new Label(userReview.getCommentaire());
                commentLabel.setWrapText(true);
                commentLabel.setStyle("-fx-font-style: italic;");

                Label dateReviewLabel = new Label("Posté le " +
                        userReview.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                dateReviewLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

                reviewBox.getChildren().addAll(reviewTitle, ratingBox, commentLabel, dateReviewLabel);
            }
        } else {
            // Show review form
            Label reviewTitle = new Label("Donnez votre avis");
            reviewTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(Pos.CENTER_LEFT);

            ToggleGroup ratingGroup = new ToggleGroup();
            int[] selectedRating = {0};

            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                ToggleButton star = new ToggleButton("★");
                star.setToggleGroup(ratingGroup);
                star.setUserData(rating);
                star.setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-font-size: 24;");

                star.setOnAction(e -> {
                    selectedRating[0] = rating;
                    updateStars(ratingBox, rating);
                });

                ratingBox.getChildren().add(star);
            }

            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Partagez votre expérience...");
            commentArea.setPrefRowCount(3);
            commentArea.setWrapText(true);

            Button submitButton = new Button("Soumettre");
            submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            submitButton.setOnAction(e -> {
                if (selectedRating[0] == 0) {
                    showAlert(Alert.AlertType.WARNING, "Note requise", "Veuillez attribuer une note à cet événement.");
                    return;
                }

                if (commentArea.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Commentaire requis", "Veuillez ajouter un commentaire.");
                    return;
                }

                // Create and save review
                EventReview review = new EventReview(
                        evenement.getId(),
                        currentUser.getUserID(),
                        selectedRating[0],
                        commentArea.getText().trim()
                );

                reviewService.ajouter(review);
                showAlert(Alert.AlertType.INFORMATION, "Avis enregistré", "Merci pour votre avis !");
                
                // Reload the page
                loadPastEvents();
            });
            
            reviewBox.getChildren().addAll(reviewTitle, ratingBox, commentArea, submitButton);
        }
        
        // Add all components to the main box
        eventBox.getChildren().addAll(headerBox, new Separator(), reviewBox);
        
        return eventBox;
    }
    
    private void updateStars(HBox ratingBox, int selectedRating) {
        for (int i = 0; i < ratingBox.getChildren().size(); i++) {
            ToggleButton star = (ToggleButton) ratingBox.getChildren().get(i);
            if (i < selectedRating) {
                star.setStyle("-fx-background-color: transparent; -fx-text-fill: #FFD700; -fx-font-size: 24;");
            } else {
                star.setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-font-size: 24;");
            }
        }
    }
    
    private HBox createRatingDisplay(int rating) {
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-text-fill: " + (i <= rating ? "#FFD700" : "#ccc") + "; -fx-font-size: 18;");
            ratingBox.getChildren().add(star);
        }
        
        return ratingBox;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/events-view.fxml"));
            Parent newPage = loader.load();
            Scene scene = rootPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger la page /events-view.fxml");
        }
    }
    
    @FXML
    private void goToEventsView() {
        loadPage("/events-view.fxml");
    }
    
    private void loadPage(String page) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(page));
            Scene scene = rootPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche une popup avec toutes les reviews d'un événement
     */
    private void showAllReviews(Evenement evenement) {
        // Créer une nouvelle fenêtre pour afficher toutes les reviews
        Stage reviewStage = new Stage();
        reviewStage.setTitle("Avis pour " + evenement.getTitre());
        
        // Récupérer toutes les reviews pour cet événement
        List<EventReview> eventReviews = reviewService.rechercherParEvenement(evenement.getId());
        
        // Créer le contenu de la fenêtre
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        // Titre et informations sur l'événement
        Label titleLabel = new Label("Avis pour " + evenement.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Note moyenne
        double averageRating = reviewService.getMoyenneNotesEvenement(evenement.getId());
        HBox averageRatingBox = new HBox(10);
        averageRatingBox.setAlignment(Pos.CENTER_LEFT);
        
        Label averageLabel = new Label(String.format("Note moyenne: %.1f/5", averageRating));
        averageLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        HBox starsBox = createRatingDisplay((int) Math.round(averageRating));
        
        averageRatingBox.getChildren().addAll(averageLabel, starsBox);
        
        // Nombre d'avis
        Label countLabel = new Label(eventReviews.size() + " avis au total");
        countLabel.setStyle("-fx-text-fill: #666;");
        
        content.getChildren().addAll(titleLabel, averageRatingBox, countLabel, new Separator());
        
        // Si aucun avis, afficher un message
        if (eventReviews.isEmpty()) {
            Label noReviewsLabel = new Label("Aucun avis pour cet événement");
            noReviewsLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            content.getChildren().add(noReviewsLabel);
        } else {
            // Créer un ScrollPane pour contenir toutes les reviews
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            
            VBox reviewsContainer = new VBox(15);
            reviewsContainer.setPadding(new Insets(10, 5, 10, 5));
            
            // Ajouter chaque review
            for (EventReview review : eventReviews) {
                VBox reviewBox = createReviewBox(review);
                reviewsContainer.getChildren().add(reviewBox);
            }
            
            scrollPane.setContent(reviewsContainer);
            scrollPane.setPrefHeight(400);
            
            content.getChildren().add(scrollPane);
        }
        
        // Bouton pour fermer la fenêtre
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        closeButton.setOnAction(e -> reviewStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(buttonBox);
        
        // Configurer la scène
        Scene scene = new Scene(content, 600, 500);
        reviewStage.setScene(scene);
        reviewStage.show();
    }
    
    /**
     * Crée une boîte contenant les détails d'une review
     */
    private VBox createReviewBox(EventReview review) {
        VBox reviewBox = new VBox(8);
        reviewBox.setPadding(new Insets(15));
        reviewBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        // En-tête avec nom d'utilisateur et date
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label userLabel = new Label(review.getNomUtilisateur());
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label dateLabel = new Label(review.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        
        headerBox.getChildren().addAll(userLabel, spacer, dateLabel);
        
        // Note
        HBox ratingBox = createRatingDisplay(review.getNote());
        
        // Commentaire
        Label commentLabel = new Label(review.getCommentaire());
        commentLabel.setWrapText(true);
        
        reviewBox.getChildren().addAll(headerBox, ratingBox, commentLabel);
        
        return reviewBox;
    }
}