package com.esprit.controllers;

import com.esprit.models.Feedback;
import com.esprit.models.Materiel;
import com.esprit.services.MaterielService;
import com.esprit.tests.Eutopia;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;
import java.util.stream.Collectors;
import com.esprit.utils.OpenAIUtil;
import com.esprit.services.FeedbackService;

import java.io.IOException;
import java.util.List;
import java.net.URL;

public class MaterielGridController {

    @FXML
    private GridPane gridPaneMateriels;

    @FXML
    private Button previousButton, nextButton, btnMaterielList, btnCategoriesList;

    private final MaterielService materielService;
    private final FeedbackService feedbackService;
    private List<Materiel> allMateriels;
    private int currentPage = 0;
    private final int itemsPerPage = 6; // Nombre d'éléments par page

    public MaterielGridController() throws SQLException {
        materielService = new MaterielService();
        feedbackService = new FeedbackService();
    }

    @FXML
    public void initialize() {
        allMateriels = materielService.rechercher();
        setupNavigationButtons();
        updateGrid();
        updateButtons();
    }

    private void updateGrid() {
        gridPaneMateriels.getChildren().clear();
        gridPaneMateriels.setAlignment(javafx.geometry.Pos.CENTER);

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allMateriels.size());
        List<Materiel> pageMateriels = allMateriels.subList(startIndex, endIndex);

        int col = 0, row = 0;
        for (Materiel materiel : pageMateriels) {
            VBox materielCard = createMaterielCard(materiel);
            gridPaneMateriels.add(materielCard, col, row);
            GridPane.setHalignment(materielCard, javafx.geometry.HPos.CENTER);
            GridPane.setValignment(materielCard, javafx.geometry.VPos.CENTER);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createMaterielCard(Materiel materiel) {
        VBox card = new VBox(15);
        card.getStyleClass().add("material-card");
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 20;");
        card.setPrefSize(300, 420);
        card.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(materiel.getLibelle());
        nameLabel.getStyleClass().add("title");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #2c3e50;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(160);
        imageView.setFitWidth(160);
        try {
            Image image = new Image(materiel.getImage_url(), true);
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }

        // Créer la zone d'étoiles IA
        HBox aiRatingBox = new HBox(5);
        aiRatingBox.setAlignment(Pos.CENTER);

        // Récupérer tous les avis pour ce matériel
        List<Feedback> feedbacks = feedbackService.getFeedbacksByMateriel(materiel.getId());
        List<String> reviews = feedbacks.stream()
                .map(Feedback::getContenu)
                .collect(Collectors.toList());

        // Si il y a des avis, analyser avec l'IA
        if (!reviews.isEmpty()) {
            int aiRating = OpenAIUtil.analyzeMaterielReviews(reviews);

            // Créer le label pour la note IA
            Label aiLabel = new Label("Rate : ");
            aiLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

            // Afficher les étoiles
            HBox starsBox = new HBox(2);
            for (int i = 1; i <= 5; i++) {
                Label star = new Label(i <= aiRating ? "★" : "☆");
                star.setStyle("-fx-text-fill: gold; -fx-font-size: 18px;");
                starsBox.getChildren().add(star);
            }

            aiRatingBox.getChildren().addAll(aiLabel, starsBox);
        }

        Label descriptionLabel = new Label("Description : " + materiel.getDescription());
        descriptionLabel.getStyleClass().add("description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label quantiteLabel = new Label("Quantité: " + materiel.getQuantite());
        quantiteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label prixLabel = new Label("Prix: " + materiel.getPrix() + " TND");
        prixLabel.getStyleClass().add("price");
        prixLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        Button avisButton = new Button("Avis");
        avisButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 20; -fx-font-size: 14px;");
        avisButton.setOnAction(e -> openFeedbackWindow(materiel, getCurrentUserId()));

        card.getChildren().addAll(imageView, nameLabel, aiRatingBox, descriptionLabel,
                quantiteLabel, prixLabel, avisButton);

        return card;
    }

    @FXML
    private void openListeReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeReservation.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Réservations");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePreviousButtonAction() {
        if (currentPage > 0) {
            currentPage--;
            updateGrid();
            updateButtons();
        }
    }

    @FXML
    private void handleNextButtonAction() {
        if ((currentPage + 1) * itemsPerPage < allMateriels.size()) {
            currentPage++;
            updateGrid();
            updateButtons();
        }
    }

    private void updateButtons() {
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * itemsPerPage >= allMateriels.size());
    }

    private void setupNavigationButtons() {
        btnMaterielList = new Button("Liste des Matériels");
        btnCategoriesList = new Button("Liste des Catégories");
        Button btnNouvelleReservation = new Button("Nouvelle Réservation");

        // Style des boutons
        btnMaterielList.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");
        btnCategoriesList.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20;");
        btnNouvelleReservation.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20;");

        // Actions des boutons
        btnMaterielList.setOnAction(event -> navigateToMaterielList());
        btnCategoriesList.setOnAction(event -> navigateToCategoriesList());
        btnNouvelleReservation.setOnAction(event -> openReservationWindow());

        // Ajout dans un HBox pour bien organiser l'affichage
        HBox buttonContainer = new HBox(20, btnMaterielList, btnCategoriesList, btnNouvelleReservation);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        gridPaneMateriels.add(buttonContainer, 0, 5, 3, 1); // Centrer sur 3 colonnes
    }

    @FXML
    private void navigateToMaterielList() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeMateriel.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste Materiel");
            stage.setScene(new Scene(root));
            stage.show();


        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
        }
    }


    @FXML
    private void navigateToCategoriesList() {
        try {
            // Use a more robust method to load the FXML file
            java.net.URL url = getClass().getResource("/ListeCategorie.fxml");
            if (url == null) {
                throw new IOException("Cannot find ListeCategorie.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des catégories");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
            e.printStackTrace(); // Add stack trace for more detailed error information
            
            // Show an error dialog with more information
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la liste des catégories");
            alert.setContentText("Détails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void openstats(ActionEvent actionEvent) {
        try {
            // Utiliser une méthode plus robuste pour charger le FXML
            URL fxmlUrl = getClass().getResource("/statistiques.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find statistiques.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques");
            stage.setScene(new Scene(root));



            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", e);
        }
    }

    @FXML
    private void openReservationWindow() {
        try {
            int userId = getCurrentUserId();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ReservationWindow.fxml"));
            ReservationWindowController controller = new ReservationWindowController(userId);
            loader.setController(controller);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réservation");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHidden(e -> updateGrid());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la fenêtre de réservation");
            alert.setContentText("Détails : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getCurrentUserId() {
        // Pour le test, on retourne l'ID utilisateur 24
        return Eutopia.getCurrentUser().getUserID();
    }

    private void openFeedbackWindow(Materiel materiel, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedbackWindow.fxml"));
            FeedbackController controller = new FeedbackController();
            controller.setMateriel(materiel);
            controller.setUserId(userId);
            loader.setController(controller);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Avis - " + materiel.getLibelle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Erreur lors de l'ouverture de la fenêtre des avis");
        alert.setContentText("Détails : " + e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }
}