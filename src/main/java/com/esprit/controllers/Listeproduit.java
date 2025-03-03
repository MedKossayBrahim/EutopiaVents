package com.esprit.controllers;

import com.esprit.models.Role;
import com.esprit.models.commande;
import com.esprit.models.produit;
import com.esprit.models.User;
import com.esprit.models.FeedbackProduit;
import com.esprit.services.CommandeService;
import com.esprit.services.ProduitService;
import com.esprit.services.FeedbackProduitService;
import com.esprit.tests.Eutopia;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.Node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.List;

public class Listeproduit {

    @FXML
    private TableView<produit> produitTable;
    @FXML
    private TableColumn<produit, ImageView> imageColumn;
    @FXML
    private TableColumn<produit, String> nomColumn;
    @FXML
    private TableColumn<produit, Integer> stockColumn;
    @FXML
    private TableColumn<produit, Double> prixColumn;
    @FXML
    private TableColumn<produit, Void> actionsColumn;
    @FXML
    private Button ajouterAuPanierBtn;
    @FXML
    private Button btnAjoutCategorie;
    @FXML
    private Button btnListeCategorie;
    @FXML
    private Button btnListeCommande;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> priceFilter;

    @FXML private GridPane produitsGrid;
    @FXML private Pagination pagination;

    private final ProduitService produitService;
    private final FeedbackProduitService feedbackService;

    private static int clientConnecteId;

    private ObservableList<produit> allProduits;
    private ObservableList<produit> filteredProduits;

    private static final int ITEMS_PER_PAGE = 8;
    private static final String HTDOCS_PATH = "file:///C:/xampp/htdocs/images/";

    public Listeproduit() throws SQLException {
        produitService = new ProduitService();
        feedbackService = new FeedbackProduitService();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing Listeproduit controller");
        
        // Get current user from Eutopia
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser != null) {
            clientConnecteId = currentUser.getUserID();
            System.out.println("User logged in with ID: " + clientConnecteId);
            System.out.println("User role: " + currentUser.getRole());
            
            // Enable buttons based on user role
            boolean isAdmin = Role.Admin.equals(currentUser.getRole());
            btnAjoutCategorie.setVisible(isAdmin);
            btnAjoutCategorie.setManaged(isAdmin);
        } else {
            System.out.println("No user logged in");
            clientConnecteId = -1; // No user logged in
            
            // Disable admin buttons if no user is logged in
            btnAjoutCategorie.setVisible(false);
            btnAjoutCategorie.setManaged(false);
        }
        
        setupFilters();
        loadProduits();
        setupAjouterAuPanierButton();
    }

    // Method to handle opening the Add Category page
    @FXML
    public void openAjoutCategorie() {
        // Check if user is admin
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null || !Role.Admin.equals(currentUser.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", 
                    "Vous devez être administrateur pour ajouter des catégories.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutcategorieproduit.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page d'ajout de catégorie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to handle opening the Category List page
    @FXML
    public void openListeCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listecategorieproduit.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Liste des Catégories");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des catégories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to handle opening the Add Product page
    @FXML
    public void openAjoutProduit() {
        // Check if user is admin
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null || !Role.Admin.equals(currentUser.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", 
                    "Vous devez être administrateur pour ajouter des produits.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterProduit.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page d'ajout de produit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to handle opening the Cart page
    @FXML
    public void openListeCommande() {
        // Check if user is logged in
        if (clientConnecteId <= 0) {
            showLoginRequiredDialog();
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paniers.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Panier de Produits");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le panier: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setClientConnecte(int clientId) {
        clientConnecteId = clientId;
    }

    private void setupColumns() {
        // Configuration de la colonne d'images
        imageColumn.setCellValueFactory(param -> {
            produit p = param.getValue();
            ImageView imageView = new ImageView();
            if (p.getImageUrl() != null) {
                Image image = new Image(p.getImageUrl());
                imageView.setImage(image);
            }
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            return new SimpleObjectProperty<>(imageView);
        });

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Configuration de la colonne des actions (boutons modifier et supprimer)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");

            {
                // Style pour le bouton supprimer
                deleteBtn.getStyleClass().add("button-danger");

                deleteBtn.setOnAction(event -> {
                    produit selectedProduit = getTableView().getItems().get(getIndex());
                    supprimerProduit(selectedProduit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, modifyBtn, deleteBtn); // Ajout d'un espacement de 10 pixels
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setupFilters() {
        // Configuration du champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProduits();
        });

        // Configuration du filtre de prix
        priceFilter.setItems(FXCollections.observableArrayList(
                "Tous les prix",
                "Moins de 50",
                "50 - 100",
                "Plus de 100"
        ));
        priceFilter.getSelectionModel().selectFirst();
        priceFilter.setOnAction(e -> filterProduits());
    }

    private void loadProduits() {
        allProduits = FXCollections.observableArrayList(produitService.rechercher());
        filteredProduits = FXCollections.observableArrayList(allProduits);

        if (produitTable != null) {
            produitTable.setItems(filteredProduits);
        } else {
            System.out.println("produitTable est null !");
        }

        updatePagination();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredProduits.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1); // Éviter pageCount = 0
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        try {
            int fromIndex = pageIndex * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredProduits.size());

            // Vérifier si la liste est vide
            List<produit> pageItems = filteredProduits.isEmpty() ?
                    List.of() : filteredProduits.subList(fromIndex, toIndex);

            produitsGrid.getChildren().clear();
            produitsGrid.getRowConstraints().clear();
            produitsGrid.getColumnConstraints().clear();

            int row = 0;
            int col = 0;
            for (produit produit : pageItems) {
                VBox productCard = createProductCard(produit);
                produitsGrid.add(productCard, col, row);
                col++;
                if (col == 3) {  // Changed from 4 to 3 columns to accommodate wider cards
                    col = 0;
                    row++;
                }
            }

            return new VBox(produitsGrid);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
            return new VBox();
        }
    }

    private VBox createProductCard(produit produit) {
        // Main card container
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(300);
        
        // Add transition effect for smooth hover
        card.setStyle("-fx-transition: all 0.2s ease;");
        
        // Image container with hover effect
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefHeight(200);
        
        // Product image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.getStyleClass().add("product-image");
        imageView.setPreserveRatio(true);

        try {
            String imageUrl = produit.getImageUrl();
            System.out.println("Loading image from URL: " + imageUrl);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = new Image(imageUrl,
                        300, 200,
                        true,
                        true,
                        true);

                image.errorProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        System.out.println("Error loading image: " + image.getException());
                        loadDefaultImage(imageView);
                    }
                });

                imageView.setImage(image);
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
            loadDefaultImage(imageView);
        }
        
        // Quick action buttons that appear on hover
        HBox quickActions = new HBox(15);
        quickActions.getStyleClass().add("quick-actions");
        quickActions.setAlignment(Pos.CENTER);
        
        // Cart button
        Button quickAddBtn = new Button();
        quickAddBtn.getStyleClass().add("quick-action-button");
        quickAddBtn.setGraphic(createIcon("cart-plus", 16));
        quickAddBtn.setTooltip(new Tooltip("Ajouter au panier"));
        quickAddBtn.setOnAction(e -> {
            if (clientConnecteId <= 0) {
                showLoginRequiredDialog();
            } else {
                handleAddToCart(produit);
            }
        });
        
        // Feedback button
        Button quickFeedbackBtn = new Button();
        quickFeedbackBtn.getStyleClass().add("quick-action-button");
        quickFeedbackBtn.setGraphic(createIcon("star", 16));
        quickFeedbackBtn.setTooltip(new Tooltip("Donner votre avis"));
        quickFeedbackBtn.setOnAction(e -> openFeedbackWindow(produit));
        
        // Admin-only delete button
        Button quickDeleteBtn = new Button();
        quickDeleteBtn.getStyleClass().add("quick-action-button");
        quickDeleteBtn.setGraphic(createIcon("trash", 16));
        quickDeleteBtn.setTooltip(new Tooltip("Supprimer le produit"));
        quickDeleteBtn.setOnAction(e -> supprimerProduit(produit));
        
        // Only show delete button for admin users
        User currentUser = Eutopia.getCurrentUser();
        boolean isAdmin = currentUser != null && Role.Admin.equals(currentUser.getRole());
        quickDeleteBtn.setVisible(isAdmin);
        quickDeleteBtn.setManaged(isAdmin);
        
        if (isAdmin) {
            quickActions.getChildren().addAll(quickAddBtn, quickFeedbackBtn, quickDeleteBtn);
        } else {
            quickActions.getChildren().addAll(quickAddBtn, quickFeedbackBtn);
        }
        
        // Add image and quick actions to the image container
        imageContainer.getChildren().addAll(imageView, quickActions);
        
        // Product info container
        VBox infoContainer = new VBox(8);
        infoContainer.getStyleClass().add("product-info-container");
        infoContainer.setPadding(new Insets(15));
        infoContainer.setAlignment(Pos.TOP_LEFT);
        
        // Product name
        Label nameLabel = new Label(produit.getNom());
        nameLabel.getStyleClass().add("product-title");
        nameLabel.setWrapText(true);
        
        // Product price with stock indicator
        HBox priceStockContainer = new HBox(10);
        priceStockContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label priceLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        priceLabel.getStyleClass().add("product-price");
        
        Label stockLabel = new Label(produit.getStock() > 0 ? "En stock" : "Rupture de stock");
        stockLabel.getStyleClass().add(produit.getStock() > 0 ? "stock-available" : "stock-unavailable");
        
        priceStockContainer.getChildren().addAll(priceLabel, stockLabel);
        
        // Rating display
        HBox ratingBox = new HBox(2);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setPadding(new Insets(5, 0, 5, 0));
        
        // Get average rating
        double avgRating = feedbackService.getMoyenneRating(produit.getId());
        int roundedRating = (int) Math.round(avgRating);
        
        // Create star rating display
        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= roundedRating ? "★" : "☆");
            star.setStyle("-fx-text-fill: gold; -fx-font-size: 14px;");
            ratingBox.getChildren().add(star);
        }
        
        // Add rating value if there are ratings
        if (avgRating > 0) {
            Label ratingValue = new Label(String.format(" (%.1f)", avgRating));
            ratingValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            ratingBox.getChildren().add(ratingValue);
        }
        
        // Product description (truncated)
        String description = produit.getDescription();
        if (description != null && description.length() > 80) {
            description = description.substring(0, 80) + "...";
        }
        
        Text descriptionText = new Text(description);
        descriptionText.getStyleClass().add("product-description");
        descriptionText.setWrappingWidth(270);
        
        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button addToCartBtn = new Button("Ajouter au panier");
        addToCartBtn.getStyleClass().add("button-primary");
        addToCartBtn.setPrefWidth(180);
        addToCartBtn.setOnAction(e -> {
            if (clientConnecteId <= 0) {
                showLoginRequiredDialog();
            } else {
                handleAddToCart(produit);
            }
        });
        
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("button-danger");
        deleteBtn.setPrefWidth(100);
        deleteBtn.setOnAction(e -> supprimerProduit(produit));

        
        buttonBox.getChildren().addAll(addToCartBtn, deleteBtn);
        
        // Add all elements to the info container
        infoContainer.getChildren().addAll(nameLabel, priceStockContainer, ratingBox, descriptionText, buttonBox);
        
        // Add all components to the main card
        card.getChildren().addAll(imageContainer, infoContainer);
        
        // Add hover effect to the entire card
        card.setOnMouseEntered(e -> {
            quickActions.setVisible(true);
            quickActions.setOpacity(1);
            
            // Add a subtle scale effect to the card
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(0, 0, 0, 0.2)));
        });
        
        card.setOnMouseExited(e -> {
            quickActions.setVisible(false);
            quickActions.setOpacity(0);
            
            // Reset the card to normal
            card.setScaleX(1);
            card.setScaleY(1);
            card.setEffect(null);
        });
        
        // Initially hide quick actions
        quickActions.setVisible(false);
        quickActions.setOpacity(0);
        
        return card;
    }
    
    // Helper method to create icons for buttons
    private Node createIcon(String iconName, int size) {
        Label icon = new Label();
        icon.getStyleClass().add("icon-" + iconName);
        icon.setPrefSize(size, size);
        return icon;
    }

    private void loadDefaultImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/Images/default-product.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.out.println("Error loading default image: " + e.getMessage());
        }
    }

    private void filterProduits() {
        String searchText = searchField.getText().toLowerCase();
        String selectedPriceRange = priceFilter.getValue();

        filteredProduits.setAll(allProduits.stream()
                .filter(produit ->
                        produit.getNom().toLowerCase().contains(searchText)
                )
                .filter(produit -> {
                    double prix = produit.getPrix();
                    return switch (selectedPriceRange) {
                        case "Moins de 50" -> prix < 50;
                        case "50 - 100" -> prix >= 50 && prix <= 100;
                        case "Plus de 100" -> prix > 100;
                        default -> true; // "Tous les prix"
                    };
                })
                .collect(Collectors.toList()));

        updatePagination();
    }

    private void supprimerProduit(produit produit) {
        // Check if user is admin
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null || !Role.Admin.equals(currentUser.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", 
                    "Vous devez être administrateur pour supprimer des produits.");
            return;
        }
        
        // Confirmation avant suppression
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le produit");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + produit.getNom() + " ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    produitService.supprimer(produit);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
                    loadProduits(); // Rechargez la liste après la suppression
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupAjouterAuPanierButton() {
        ajouterAuPanierBtn.setOnAction(event -> {
            // Check if user is logged in
            if (clientConnecteId <= 0) {
                showLoginRequiredDialog();
                return;
            }
            
            // Vérifier si des produits sont sélectionnés dans la grille
            if (filteredProduits.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aucun produit", "Aucun produit disponible.");
                return;
            }

            // Ouvrir une boîte de dialogue pour sélectionner un produit
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Sélectionner un produit");
            dialog.setHeaderText("Ajouter au panier");
            dialog.setContentText("Choisissez un produit:");

            // Ajouter tous les produits à la liste de choix
            dialog.getItems().addAll(
                    filteredProduits.stream()
                            .map(p -> p.getId() + " - " + p.getNom())
                            .collect(Collectors.toList())
            );

            dialog.showAndWait().ifPresent(choix -> {
                // Extraire l'ID du produit du choix
                int produitId = Integer.parseInt(choix.split(" - ")[0]);

                // Trouver le produit correspondant
                produit selectedProduit = filteredProduits.stream()
                        .filter(p -> p.getId() == produitId)
                        .findFirst()
                        .orElse(null);

                if (selectedProduit != null) {
                    handleAddToCart(selectedProduit);
                }
            });
        });
    }

    private void handleAddToCart(produit produit) {
        try {
            // Check if user is logged in
            if (clientConnecteId <= 0) {
                showLoginRequiredDialog();
                return;
            }
            
            // Demander la quantité
            TextInputDialog quantityDialog = new TextInputDialog("1");
            quantityDialog.setTitle("Quantité");
            quantityDialog.setHeaderText("Ajouter au panier : " + produit.getNom());
            quantityDialog.setContentText("Entrez la quantité souhaitée :");

            quantityDialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);

                    if (quantity <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être supérieure à 0");
                        return;
                    }

                    if (quantity > produit.getStock()) {
                        showAlert(Alert.AlertType.ERROR, "Stock insuffisant",
                                "Il ne reste que " + produit.getStock() + " unités en stock");
                        return;
                    }

                    // Ajout au panier
                    commande nouvelleCommande = new commande();
                    nouvelleCommande.setProduitId(produit.getId());
                    nouvelleCommande.setQuantite(quantity);
                    nouvelleCommande.setClientId(clientConnecteId);

                    CommandeService commandeService = new CommandeService();
                    commandeService.ajouter(nouvelleCommande);

                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            quantity + " " + produit.getNom() + "(s) ajouté(s) au panier");

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un nombre valide");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout au panier: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout au panier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows a dialog informing the user they need to log in to add items to cart
     */
    private void showLoginRequiredDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connexion requise");
        alert.setHeaderText("Vous devez être connecté");
        alert.setContentText("Veuillez vous connecter pour ajouter des produits au panier.");
        
        ButtonType loginButton = new ButtonType("Se connecter");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        alert.showAndWait().ifPresent(type -> {
            if (type == loginButton) {
                try {
                    // Navigate to login page
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
                    Parent root = loader.load();
                    
                    Stage loginStage = new Stage();
                    loginStage.setTitle("Connexion");
                    loginStage.setScene(new Scene(root));
                    
                    // Close the current window (optional)
                    Stage currentStage = (Stage) searchField.getScene().getWindow();
                    currentStage.close();
                    
                    loginStage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Impossible d'ouvrir la page de connexion: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openFeedbackWindow(produit produit) {
        try {
            // Check if user is logged in
            if (clientConnecteId <= 0) {
                showLoginRequiredDialog();
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FeedbackProduitWindow.fxml"));
            FeedbackProduitController controller = new FeedbackProduitController();
            controller.setProduit(produit);
            controller.setUserId(clientConnecteId);
            loader.setController(controller);

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Avis - " + produit.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors de l'ouverture de la fenêtre des avis: " + e.getMessage());
        }
    }
}