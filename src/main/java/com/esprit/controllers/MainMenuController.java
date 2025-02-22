package com.esprit.controllers;

import com.esprit.models.Lieu;
import com.esprit.services.LieuServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MainMenuController {

    @FXML private GridPane lieuxGrid;
    @FXML private Pagination pagination;
    @FXML private TextField searchField;

    private LieuServiceImpl lieuService = new LieuServiceImpl();
    private static final int ITEMS_PER_PAGE = 8;
    private ObservableList<Lieu> allLieux;
    private ObservableList<Lieu> filteredLieux;

    @FXML
    public void initialize() {
        loadLieux();
        setupSearch();
    }

    public void loadLieux() {
        allLieux = FXCollections.observableArrayList(lieuService.rechercher());
        filteredLieux = FXCollections.observableArrayList(allLieux);
        updatePagination();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredLieux.setAll(allLieux.stream()
                    .filter(lieu -> lieu.getNom().toLowerCase().contains(newValue.toLowerCase()))
                    .collect(Collectors.toList()));
            updatePagination();
        });
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredLieux.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredLieux.size());
        List<Lieu> pageItems = filteredLieux.subList(fromIndex, toIndex);

        lieuxGrid.getChildren().clear();

        int row = 0;
        int col = 0;
        for (Lieu lieu : pageItems) {
            lieuxGrid.add(createLieuCard(lieu), col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }

        return new VBox(lieuxGrid);
    }

    private VBox createLieuCard(Lieu lieu) {
        VBox card = new VBox(5);
        card.getStyleClass().add("lieu-card");

        ImageView imageView = createImageView(lieu.getImage());
        Label nomLabel = new Label(lieu.getNom());
        nomLabel.getStyleClass().add("card-title");

        Button modifierBtn = new Button("Modifier");
        modifierBtn.getStyleClass().addAll("button", "modifier");
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.getStyleClass().addAll("button", "supprimer");

        modifierBtn.setOnAction(e -> modifierLieu(lieu));
        supprimerBtn.setOnAction(e -> supprimerLieu(lieu));

        VBox buttonContainer = new VBox(5, modifierBtn, supprimerBtn);
        buttonContainer.getStyleClass().add("button-container");

        card.getChildren().addAll(imageView, nomLabel, buttonContainer);
        return card;
    }

    private ImageView createImageView(String imageUrl) {
        Image image;
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Créer une ImageView vide ou définir un comportement alternatif
            return new ImageView();
        } else {
            image = new Image(imageUrl, 520, 390, true, true);
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(260);
        imageView.setFitHeight(195);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }



    private void modifierLieu(Lieu lieu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LieuView.fxml"));
            Parent root = loader.load();
            LieuController controller = loader.getController();
            controller.setLieu(lieu);
            controller.setMainMenuController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier un Lieu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification du lieu.");
        }
    }

    private void supprimerLieu(Lieu lieu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le lieu");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + lieu.getNom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                lieuService.supprimer(lieu);
                loadLieux();
                showAlert("Succès", "Le lieu a été supprimé avec succès.");
            }
        });
    }

    @FXML
    private void goToAjouterLieu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LieuView.fxml"));
            Parent root = loader.load();
            LieuController controller = loader.getController();
            controller.setMainMenuController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Lieu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout de lieu.");
        }
    }

    @FXML
    private void goToGestionCategories() {
        loadView("/AjoutCategorie.fxml", "Gestion des Catégories");
    }

    @FXML
    private void goToGestionReservations() {
        loadView("/Reservation1View.fxml", "Gestion des Réservations");
    }

    @FXML
    private void goToGestionPhotos() {
        loadView("/photoView.fxml", "Gestion des Photos");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre " + title);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refreshLieux() {
        loadLieux();
    }
}