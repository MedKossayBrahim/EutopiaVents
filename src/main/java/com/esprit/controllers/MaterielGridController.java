package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.services.MaterielService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MaterielGridController {

    @FXML
    private GridPane gridPaneMateriels;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    private final MaterielService materielService;
    private List<Materiel> allMateriels;
    private int currentPage = 0;
    private final int itemsPerPage = 6; // Number of items per page

    public MaterielGridController() {
        materielService = new MaterielService();
    }

    @FXML
    public void initialize() {
        allMateriels = materielService.rechercher(); // Retrieve all materials
        updateGrid();
        updateButtons();
    }

    private void updateGrid() {
        gridPaneMateriels.getChildren().clear(); // Clear the grid

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allMateriels.size());

        List<Materiel> pageMateriels = allMateriels.subList(startIndex, endIndex);

        int col = 0;
        int row = 0;

        for (Materiel materiel : pageMateriels) {
            VBox materielCard = createMaterielCard(materiel);
            gridPaneMateriels.add(materielCard, col, row);

            col++;
            if (col == 3) { // 3 items per row
                col = 0;
                row++;
            }
        }
    }

    private VBox createMaterielCard(Materiel materiel) {
        VBox card = new VBox(5);
        card.setStyle("-fx-border-color: black; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f8f8f8;");
        card.setPrefSize(200, 300); // Fixed size for each card

        // Name of the material
        Label nameLabel = new Label(materiel.getLibelle());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Material image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        try {
            Image image = new Image(materiel.getImage_url(), true);
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        // Material description
        Label descriptionLabel = new Label("Description : " + materiel.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px;");

        // Available quantity
        Label quantiteLabel = new Label("Quantity: " + materiel.getQuantite());
        quantiteLabel.setStyle("-fx-font-size: 12px;");

        // Price of the material
        Label prixLabel = new Label("Price: " + materiel.getPrix() + " TND");
        prixLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Add elements to the card
        card.getChildren().addAll(imageView, nameLabel, descriptionLabel, quantiteLabel, prixLabel);
        return card;
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
}