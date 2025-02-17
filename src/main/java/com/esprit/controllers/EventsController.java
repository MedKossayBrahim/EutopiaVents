package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.io.File;
import java.util.Objects;

public class EventsController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private GridPane eventsGrid;

    private EvenementService evenementService = new EvenementService();

    public EventsController() throws SQLException {
    }

    @FXML
    public void initialize() {
        afficherEvenements();
    }

    private void afficherEvenements() {
        List<Evenement> evenements = evenementService.rechercher();
        int column = 0;
        int row = 0;

        for (Evenement evenement : evenements) {
            if (evenement.getCapacite() > 0) {
                VBox eventBox = createEventBox(evenement);
                eventsGrid.add(eventBox, column, row);

                column++;
                if (column == 2) {
                    column = 0;
                    row++;
                }
            }
        }
    }

    private VBox createEventBox(Evenement evenement) {
        VBox eventBox = new VBox();
        eventBox.setPadding(new Insets(10));
        eventBox.setSpacing(5);
        eventBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");

        // Image handling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);

        // Try to load image from database
        String imagePath = evenement.getImage(); // Get the image path from database
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(imagePath);
                if (!image.isError()) {
                    imageView.setImage(image);
                } else {
                    loadDefaultImage(imageView);
                }
            } catch (Exception e) {
                System.err.println("Error loading image for event: " + evenement.getTitre());
                loadDefaultImage(imageView);
            }
        } else {
            loadDefaultImage(imageView);
        }

        // Event information
        Label titreLabel = new Label(evenement.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label dateLabel = new Label("Du " + evenement.getDateDebut() + " au " + evenement.getDateFin());
        dateLabel.setStyle("-fx-font-size: 12px;");
        
        Label prixLabel = new Label("Prix: " + evenement.getPrix() + " TND");
        prixLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2196F3;");

        // Add all components to the box
        eventBox.getChildren().addAll(imageView, titreLabel, dateLabel, prixLabel);

        // Hover effect
        eventBox.setOnMouseEntered(e -> 
            eventBox.setStyle("-fx-border-color: #2196F3; -fx-border-width: 1px; -fx-background-color: #f5f5f5;")
        );
        eventBox.setOnMouseExited(e -> 
            eventBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: #f9f9f9;")
        );

        // Click handler
        eventBox.setOnMouseClicked(event -> ouvrirDetailsEvenement(evenement));

        return eventBox;
    }

    private void loadDefaultImage(ImageView imageView) {
        try {
            String defaultImagePath = "/Images/default-event.png";
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(defaultImagePath)));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
            // Create a placeholder rectangle if even default image fails
            Rectangle placeholder = new Rectangle(150, 100);
            placeholder.setFill(Color.LIGHTGRAY);
            imageView.setImage(null);
            VBox parent = (VBox) imageView.getParent();
            if (parent != null) {
                parent.getChildren().set(parent.getChildren().indexOf(imageView), placeholder);
            }
        }
    }

    private void ouvrirDetailsEvenement(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetailsController controller = loader.getController();
            controller.afficherDetails(evenement.getId());

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de l'événement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCategEvent.fxml");
    }
    @FXML
    private void goToAjouterEvenement() {
        loadPage("/AjouterEvenement.fxml");
    }


    @FXML
    private void goToModifierEvenement() {
        loadPage("/ModifierEvenement.fxml");
    }

    @FXML
    private void goToEventsView() {
        loadPage("/events-view.fxml");
    }

    @FXML
    private void goToGererEvenements() {
        loadPage("/GererEvenements.fxml");
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
}
