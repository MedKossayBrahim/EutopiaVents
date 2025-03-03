package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.esprit.models.CategoriesEvent;
import com.esprit.services.CategoriesEventService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import com.esprit.models.User;
import com.esprit.tests.Eutopia;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
        rootPane.getStylesheets().add(getClass().getResource("/eventsstyle.css").toExternalForm());
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
            cartIcon.setOnMouseEntered(e -> cartIcon.setOpacity(0.8));
            cartIcon.setOnMouseExited(e -> cartIcon.setOpacity(1.0));
            cartIcon.setOnMouseClicked(e -> goToPanier());
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
        eventsGrid.getStyleClass().add("events-grid");

        // Fixed to 2 columns
        int numColumns = 2;

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
        eventBox.setPadding(new Insets(0));
        eventBox.setSpacing(0);
        eventBox.setMinWidth(450);
        eventBox.setMaxWidth(450);
        eventBox.getStyleClass().add("event-box");

        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.setMinHeight(250);
        imageContainer.setMaxHeight(250);
        imageContainer.getStyleClass().add("image-container");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(450);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        // Load image
        String imagePath = evenement.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(imagePath);
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image for event: " + evenement.getTitre());
            }
        }

        imageContainer.getChildren().add(imageView);

        // Information container
        VBox infoContainer = new VBox(12);
        infoContainer.getStyleClass().add("info-container");

        // Title
        Label titreLabel = new Label(evenement.getTitre());
        titreLabel.getStyleClass().add("title-label");

        // Date avec label
        String dateDebut = evenement.getDateDebut().split(" ")[0];
        String dateFin = evenement.getDateFin().split(" ")[0];
        Label dateLabel = new Label("Date: Du " + dateDebut + " au " + dateFin);
        dateLabel.getStyleClass().add("date-label");

        // Prix avec label
        HBox prixBox = new HBox(10);
        prixBox.setAlignment(Pos.CENTER_LEFT);
        Label prixLabel = new Label("Prix: " + String.format("%.2f TND", evenement.getPrix()));
        prixLabel.getStyleClass().add("price-label");

        prixBox.getChildren().add(prixLabel);

        // Add all elements
        infoContainer.getChildren().addAll(titreLabel, dateLabel, prixBox);
        eventBox.getChildren().addAll(imageContainer, infoContainer);

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
    @FXML
    private void goToPanier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Panier.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Panier");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'ouverture du panier.");
            alert.showAndWait();
        }
    }

    @FXML
    private void goToReviews() {
        loadPage("/EventReviews.fxml");
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
