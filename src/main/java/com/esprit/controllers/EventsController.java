package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import com.esprit.models.CategoriesEvent;
import com.esprit.services.CategoriesEventService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import com.esprit.models.User;
import com.esprit.tests.Eutopia;

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
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CategoriesEvent> categoryFilter;

    @FXML
    private Button btnAjouterCateg;
    @FXML
    private Button btnAjouterEvenement;
    @FXML
    private Button btnModifierEvenement;
    @FXML
    private Button btnGererEvenements;

    @FXML
    private Button btnReviews;

    private EvenementService evenementService = new EvenementService();
    private CategoriesEventService categoriesEventService = new CategoriesEventService();
    private static final int ITEMS_PER_PAGE = 8; // Changed to 8 events per page

    private static final String CREAM_BG = "#faf6f3";
    private static final String ACCENT_COLOR = "#007bff";
    private static final String TEXT_COLOR = "#2c3e50";
    private static final String SECONDARY_TEXT = "#6c757d";

    public EventsController() throws SQLException {
    }
    @FXML

    public void initialize() {
        setupSearchField();
        setupCategoryFilter();
        setupPagination();
        setupCartIcon();
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser != null) {
            // Vérifier le type de service en fonction du rôle de l'utilisateur
            switch (currentUser.getRole()) {
                case Admin:
                    // Admin peut voir tous les boutons
                    break;

                case Organisateur:
                    // Organisateur peut voir tous les boutons sauf GererEvenements et AjouterCateg
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                case Participant:
                    // Participant ne peut voir aucun bouton de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                default:
                    // Par défaut, cacher tous les boutons de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;
            }
        } else {
            // Si aucun utilisateur n'est connecté, cacher le bouton des reviews
            if (btnReviews != null) {
                btnReviews.setVisible(false);
                btnReviews.setManaged(false);
            }
        }
    }

    private void setupSearchField() {
        // Real-time search implementation
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePaginationWithSearch(newValue);
        });
    }

    private void setupCartIcon() {
        ImageView cartIcon = (ImageView) rootPane.lookup("#cartIcon");
        if (cartIcon != null) {
            // Add hover effect
            cartIcon.setOnMouseEntered(e -> cartIcon.setOpacity(0.8));
            cartIcon.setOnMouseExited(e -> cartIcon.setOpacity(1.0));
        }
    }

    private void setupCategoryFilter() {
        // Load categories
        List<CategoriesEvent> categories = categoriesEventService.rechercher();
        categoryFilter.getItems().add(new CategoriesEvent("Toutes les catégories")); // Add "All" option
        categoryFilter.getItems().addAll(categories);

        // Set default value
        categoryFilter.setValue(categoryFilter.getItems().get(0));

        // Add listener for category changes
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePaginationWithSearch(searchField.getText());
        });
    }

    private void setupPagination() {
        updatePaginationWithSearch("");
    }

    private void updatePaginationWithSearch(String searchText) {
        CategoriesEvent selectedCategory = categoryFilter.getValue();

        // Get current date
        java.time.LocalDate currentDate = java.time.LocalDate.now();

        List<Evenement> filteredEvents = evenementService.rechercher()
                .stream()
                .filter(evenement -> {
                    // Check if search text matches
                    boolean matchesSearch = searchText.isEmpty() ||
                            evenement.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                            evenement.getDescription().toLowerCase().contains(searchText.toLowerCase());

                    // Check if category matches
                    boolean matchesCategory = selectedCategory == null ||
                            selectedCategory.getNom().equals("Toutes les catégories") ||
                            evenement.getCategorieId() == selectedCategory.getId();

                    // Parse end date - handle datetime format
                    java.time.LocalDate eventEndDate;
                    try {
                        String dateFin = evenement.getDateFin();
                        // Check if date contains time part
                        if (dateFin.contains(" ")) {
                            // Extract only the date part (before the space)
                            dateFin = dateFin.split(" ")[0];
                        }
                        eventEndDate = java.time.LocalDate.parse(dateFin);
                    } catch (Exception e) {
                        System.err.println("Erreur de parsing de date pour l'événement " + evenement.getId() + ": " + e.getMessage());
                        // If date parsing fails, consider the event as expired
                        return false;
                    }

                    // Check if event is accepted, has capacity, and end date is not passed
                    return (evenement.getCapacite() > 0 &&
                            "acceptée".equals(evenement.getStatut()) &&
                            !eventEndDate.isBefore(currentDate) &&
                            matchesSearch &&
                            matchesCategory);
                })
                .toList();

        int pageCount = (filteredEvents.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        eventsPagination.setPageCount(Math.max(1, pageCount));
        eventsPagination.setCurrentPageIndex(0);
        eventsPagination.setPageFactory(pageIndex -> createPage(pageIndex, filteredEvents));
    }

    private Node createPage(int pageIndex, List<Evenement> filteredEvents) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredEvents.size());
        List<Evenement> pageEvents = filteredEvents.subList(fromIndex, toIndex);

        eventsGrid.getChildren().clear();

        // Calculate number of columns based on grid width
        int numColumns = 2; // Fixed to 2 columns for consistency

        for (int i = 0; i < pageEvents.size(); i++) {
            VBox eventBox = createEventBox(pageEvents.get(i));
            int row = i / numColumns;
            int column = i % numColumns;
            eventsGrid.add(eventBox, column, row);
        }

        return eventsGrid;
    }

    private VBox createEventBox(Evenement evenement) {
        VBox eventBox = new VBox();
        eventBox.setPadding(new Insets(15));
        eventBox.setSpacing(10);
        eventBox.setMaxWidth(500);
        eventBox.setMinWidth(400);
        eventBox.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Image handling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(eventBox.getMinWidth() - 30);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 4 4 0 0;");

        // Center the image
        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setAlignment(Pos.CENTER);

        // Charger l'image de l'événement
        String imagePath = evenement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(imagePath);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image for event: " + evenement.getTitre());
            }
        }

        // Event information container
        VBox infoContainer = new VBox(8);
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
    private void handleSearch() {
        String searchText = searchField.getText().trim();

        // Récupérer tous les événements
        List<Evenement> allEvents = evenementService.rechercher();

        // Filtrer les événements en fonction de searchText
        List<Evenement> filteredEvents = allEvents.stream()
                .filter(evenement -> evenement.getTitre().toLowerCase().contains(searchText.toLowerCase()) ||
                        evenement.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                .toList();

        // Mettre à jour l'affichage avec les événements filtrés
        eventsGrid.getChildren().clear(); // Clear existing grid
        for (Evenement evenement : filteredEvents) {
            VBox eventBox = createEventBox(evenement);
            eventsGrid.getChildren().add(eventBox);
        }
    }

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCateg.fxml");
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
    @FXML private void goToPanier() {
        loadPage("/Panier.fxml");
    }

    @FXML
    private void goToReviews() {
        loadPage("/Reviews.fxml");
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
