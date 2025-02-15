package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class EventsController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane eventsGrid;

    private EvenementService evenementService = new EvenementService();

    @FXML
    public void initialize() {
        afficherEvenements();
    }

    private void afficherEvenements() {
        List<Evenement> evenements = evenementService.rechercher();
        int column = 0;
        int row = 0;

        for (Evenement evenement : evenements) {
            VBox eventBox = createEventBox(evenement);
            eventsGrid.add(eventBox, column, row);

            column++;
            if (column == 2) { // 2 colonnes par ligne
                column = 0;
                row++;
            }
        }
    }

    private VBox createEventBox(Evenement evenement) {
        VBox eventBox = new VBox();
        eventBox.setPadding(new Insets(10));
        eventBox.setSpacing(5);
        eventBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");

        // Image de l'événement
        ImageView imageView = new ImageView();
        if (evenement.getImage() != null && !evenement.getImage().isEmpty()) {
            Image image = new Image(evenement.getImage(), 150, 100, false, true);
            imageView.setImage(image);
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);

        // Labels des informations
        Label titreLabel = new Label(evenement.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold;");
        Label dateLabel = new Label("Du " + evenement.getDateDebut() + " au " + evenement.getDateFin());
        Label prixLabel = new Label("Prix: " + evenement.getPrix() + " TND");

        eventBox.getChildren().addAll(imageView, titreLabel, dateLabel, prixLabel);

        // Gestion du clic pour naviguer vers la page de détails
        eventBox.setOnMouseClicked(event -> ouvrirDetailsEvenement(evenement));

        return eventBox;
    }

    private void ouvrirDetailsEvenement(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer l'ID de l'événement
            EventDetailsController controller = loader.getController();
            controller.afficherDetails(evenement.getId()); // Passer l'ID de l'événement

            // Ouvrir une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de l'événement");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
