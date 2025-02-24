package com.esprit.controllers;

import com.esprit.models.Lieu;
import com.esprit.models.categorie_salle;
import com.esprit.services.LieuServiceImpl;
import com.esprit.services.CategorieServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.esprit.tests.Eutopia;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MainMenuController {

    @FXML private GridPane lieuxGrid;
    @FXML private Pagination pagination;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;

    private LieuServiceImpl lieuService = new LieuServiceImpl();
    private CategorieServiceImpl categorieService = new CategorieServiceImpl();
    private static final int ITEMS_PER_PAGE = 8;
    private ObservableList<Lieu> allLieux;
    private ObservableList<Lieu> filteredLieux;

    public MainMenuController() throws SQLException {
    }

    @FXML
    public void initialize() {
        // Get current user role
        String userRole = String.valueOf(Eutopia.getCurrentUser().getRole());
        
        loadLieux();
        setupSearch();
        setupCategoryFilter();
        
        // Configure button visibility based on role
        configureButtonsByRole(userRole);
    }

    private void configureButtonsByRole(String userRole) {
        // Only show admin buttons if user is admin
        boolean isAdmin = "Admin".equals(userRole);
        
        // Configure "Ajouter Lieu" button
        Button ajouterButton = (Button) searchField.getParent().lookup("Button");
        if (ajouterButton != null) {
            ajouterButton.setVisible(isAdmin);
            ajouterButton.setManaged(isAdmin);
        }
        
        // Configure all management buttons
        HBox navButtonsContainer = (HBox) lieuxGrid.getParent().lookup(".nav-buttons");
        if (navButtonsContainer != null) {
            // Hide the entire container for non-admin users
            navButtonsContainer.setVisible(isAdmin);
            navButtonsContainer.setManaged(isAdmin);
        }
    }

    public void loadLieux() {
        allLieux = FXCollections.observableArrayList(lieuService.rechercher());
        filteredLieux = FXCollections.observableArrayList(allLieux);
        updatePagination();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLieux();
        });
    }

    private void setupCategoryFilter() {
        List<String> categoryNames = categorieService.rechercher().stream()
                .map(categorie_salle::getNom)
                .collect(Collectors.toList());
        categoryNames.add(0, "Toutes les catégories");

        categoryFilter.setItems(FXCollections.observableArrayList(categoryNames));
        categoryFilter.getSelectionModel().selectFirst();

        categoryFilter.setOnAction(e -> filterLieux());
    }

    private void filterLieux() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCategoryName = categoryFilter.getValue();

        filteredLieux.setAll(allLieux.stream()
                .filter(lieu -> lieu.getNom().toLowerCase().contains(searchText))
                .filter(lieu -> "Toutes les catégories".equals(selectedCategoryName) ||
                        lieu.getCategorie().getNom().equals(selectedCategoryName))
                .collect(Collectors.toList()));

        updatePagination();
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

        // Only create admin buttons if user is admin
        String userRole = String.valueOf(Eutopia.getCurrentUser().getRole());
        if ("Admin".equals(userRole)) {
            Button modifierBtn = new Button("Modifier");
            modifierBtn.getStyleClass().addAll("button", "modifier");
            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.getStyleClass().addAll("button", "supprimer");

            modifierBtn.setOnAction(e -> modifierLieu(lieu));
            supprimerBtn.setOnAction(e -> supprimerLieu(lieu));

            VBox buttonContainer = new VBox(5, modifierBtn, supprimerBtn);
            buttonContainer.getStyleClass().add("button-container");
            
            card.getChildren().addAll(imageView, nomLabel, buttonContainer);
        } else {
            // For non-admin users, only show the image and name
            card.getChildren().addAll(imageView, nomLabel);
        }
        
        return card;
    }

    private ImageView createImageView(String imageUrl) {
        try {
            Image image;
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                // Use default image
                image = new Image(getClass().getResourceAsStream("/Images/defaultPlace.png"));
            } else {
                try {
                    // First try to load as URL/file path
                    image = new Image(imageUrl);
                    if (image.isError()) {
                        // If URL fails, try as resource
                        image = new Image(getClass().getResourceAsStream(imageUrl));
                    }
                } catch (Exception e) {
                    // If both attempts fail, use default
                    image = new Image(getClass().getResourceAsStream("/Images/defaultPlace.png"));
                }
            }
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(260);
            imageView.setFitHeight(195);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setCache(true);
            return imageView;
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default image in case of any error
            return new ImageView(new Image(getClass().getResourceAsStream("/Images/defaultPlace.png")));
        }
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
