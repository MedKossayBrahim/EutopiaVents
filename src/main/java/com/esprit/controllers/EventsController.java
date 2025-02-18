package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

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

    @FXML
    private Pagination eventsPagination;

    private EvenementService evenementService = new EvenementService();
    private static final int ITEMS_PER_PAGE = 8; // Changed to 8 events per page

    public EventsController() throws SQLException {
    }
    @FXML
    public void initialize() {
        List<Evenement> filteredEvents = evenementService.rechercher()
                .stream()
                .filter(evenement -> evenement.getCapacite() > 0 && "acceptée".equals(evenement.getStatut()))
                .toList();
                
        int pageCount = (filteredEvents.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        eventsPagination.setPageCount(Math.max(1, pageCount));
        eventsPagination.setPageFactory(this::createPage);
    }
    private Node createPage(int pageIndex) {
        // Get all events and filter them
        List<Evenement> allEvents = evenementService.rechercher()
                .stream()
                .filter(evenement -> evenement.getCapacite() > 0 && "acceptée".equals(evenement.getStatut()))
                .toList();
        
        // Calculate pagination indices
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allEvents.size());
        
        // Get events for current page
        List<Evenement> pageEvents = allEvents.subList(fromIndex, toIndex);
        
        // Clear existing grid
        eventsGrid.getChildren().clear();
        
        // Add events to grid
        int column = 0;
        int row = 0;
        
        for (Evenement evenement : pageEvents) {
            VBox eventBox = createEventBox(evenement);
            eventsGrid.add(eventBox, column, row);
            
            column++;
            if (column == 2) { // Two events per row
                column = 0;
                row++;
            }
        }
        
        return eventsGrid;
    }

    private VBox createEventBox(Evenement evenement) {
        VBox eventBox = new VBox();
        eventBox.setPadding(new Insets(15));
        eventBox.setSpacing(10);
        eventBox.setMaxWidth(500); // Set maximum width for the card
        eventBox.setMinWidth(400); // Set minimum width for consistency
        eventBox.setStyle("-fx-background-color: white; " +
                          "-fx-border-radius: 8; " +
                          "-fx-background-radius: 8; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Image handling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(eventBox.getMinWidth() - 30); // Full width minus padding
        imageView.setFitHeight(200); // Fixed height for consistency
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 4 4 0 0;"); // Rounded corners on top

        // Center the image
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setAlignment(Pos.CENTER);

        // Try to load image from database
        String imagePath = evenement.getImage();
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

        // Event information container
        VBox infoContainer = new VBox(8); // 8px spacing between elements
        infoContainer.setPadding(new Insets(10, 5, 5, 5));

        // Event information with black text
        Label titreLabel = new Label(evenement.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; " +
                            "-fx-font-size: 16px; " +
                            "-fx-text-fill: #000000;");
        titreLabel.setWrapText(true);
        
        Label dateLabel = new Label("Du " + evenement.getDateDebut() + " au " + evenement.getDateFin());
        dateLabel.setStyle("-fx-font-size: 14px; " +
                          "-fx-text-fill: #000000;");
        
        Label prixLabel = new Label("Prix: " + evenement.getPrix() + " TND");
        prixLabel.setStyle("-fx-font-size: 14px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-text-fill: #000000;");

        // Add all components to the info container
        infoContainer.getChildren().addAll(titreLabel, dateLabel, prixLabel);

        // Add all components to the main box
        eventBox.getChildren().addAll(imageContainer, infoContainer);

        // Hover effect
        eventBox.setOnMouseEntered(e -> 
            eventBox.setStyle("-fx-background-color: white; " +
                             "-fx-border-radius: 8; " +
                             "-fx-background-radius: 8; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0, 0, 0);")
        );
        eventBox.setOnMouseExited(e -> 
            eventBox.setStyle("-fx-background-color: white; " +
                             "-fx-border-radius: 8; " +
                             "-fx-background-radius: 8; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);")
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
            Rectangle placeholder = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
            placeholder.setFill(Color.LIGHTGRAY);
            placeholder.setArcWidth(8);
            placeholder.setArcHeight(8);
            StackPane parent = (StackPane) imageView.getParent();
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

    public void toUserEdit(MouseEvent mouseEvent) throws IOException {
        Eutopia.getSceneManager().switchScene("/editProfile.fxml",null);
    }
}
