package com.esprit.controllers;

import com.esprit.models.commande;
import com.esprit.models.produit;
import com.esprit.services.CommandeService;
import com.esprit.services.ProduitService;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.List;

public class Listeproduit {

    @FXML
    private TableView<produit> produitTable;
    @FXML
    private TableColumn<produit, ImageView> imageColumn; // Colonne pour l'image
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

    // Ajout des nouveaux éléments UI
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> priceFilter;

    @FXML private GridPane produitsGrid;
    @FXML private Pagination pagination;

    private final ProduitService produitService;

    // Ajout d'une variable pour stocker l'ID du client connecté
    private static int clientConnecteId;  // Vous devrez définir cette valeur lors de la connexion

    private ObservableList<produit> allProduits;
    private ObservableList<produit> filteredProduits;

    private static final int ITEMS_PER_PAGE = 8;
    private static final String HTDOCS_PATH = "file:///C:/xampp/htdocs/images/";

    public Listeproduit() throws SQLException {
        produitService = new ProduitService();
    }

    @FXML
    public void initialize() {
        setupFilters();
        loadProduits();
        setupAjouterAuPanierButton();
    }

    // Méthode pour définir le client connecté
    public static void setClientConnecte(int clientId) {
        clientConnecteId = clientId;
    }

    private void setupColumns() {
        // Configuration de la colonne d'images
        imageColumn.setCellValueFactory(param -> {
            produit p = param.getValue();
            ImageView imageView = new ImageView();
            if (p.getImageUrl() != null) {
                Image image = new Image(p.getImageUrl()); // Convertir le tableau d'octets en Image
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
                    HBox hbox = new HBox(modifyBtn, deleteBtn);
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
        updatePagination();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredProduits.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        try {
            int fromIndex = pageIndex * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredProduits.size());
            List<produit> pageItems = filteredProduits.subList(fromIndex, toIndex);

            produitsGrid.getChildren().clear();
            produitsGrid.getRowConstraints().clear();
            produitsGrid.getColumnConstraints().clear();

            int row = 0;
            int col = 0;
            for (produit produit : pageItems) {
                VBox productCard = createProductCard(produit);
                produitsGrid.add(productCard, col, row);
                col++;
                if (col == 4) {
                    col = 0;
                    row++;
                }
            }

            return new VBox(produitsGrid);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la page: " + e.getMessage());
            return new VBox();
        }
    }

    private VBox createProductCard(produit product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(200);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        try {
            String imageUrl = product.getImageUrl();
            System.out.println("Loading image from URL: " + imageUrl);
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = new Image(imageUrl, 
                                     150, 150,
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

        Label nameLabel = new Label(product.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setWrapText(true);

        Text descriptionText = new Text(product.getDescription());
        descriptionText.setWrappingWidth(180);

        Label priceLabel = new Label(String.format("%.2f DT", product.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");

        Button addToCartBtn = new Button("Ajouter au panier");
        addToCartBtn.getStyleClass().add("button-primary");
        addToCartBtn.setOnAction(e -> handleAddToCart(product));

        card.getChildren().addAll(imageView, nameLabel, descriptionText, priceLabel, addToCartBtn);
        return card;
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
        produitService.supprimer(produit); // Assurez-vous d'avoir une méthode supprimer dans votre service
        loadProduits(); // Rechargez la liste après la suppression
    }

    private void setupAjouterAuPanierButton() {
        ajouterAuPanierBtn.setOnAction(event -> {
            // Vérifier si un client est connecté
            clientConnecteId = 11;

            produit selectedProduit = produitTable.getSelectionModel().getSelectedItem();

            if (selectedProduit == null) {
                showAlert(Alert.AlertType.WARNING, "Sélection requise",
                        "Veuillez sélectionner un produit dans la liste.");
                return;
            }

            TextInputDialog quantityDialog = new TextInputDialog("1");
            quantityDialog.setTitle("Quantité");
            quantityDialog.setHeaderText("Ajouter au panier : " + selectedProduit.getNom());
            quantityDialog.setContentText("Entrez la quantité souhaitée :");

            quantityDialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);

                    if (quantity <= 0) {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "La quantité doit être supérieure à 0");
                        return;
                    }

                    if (quantity > selectedProduit.getStock()) {
                        showAlert(Alert.AlertType.ERROR, "Stock insuffisant",
                                "Il ne reste que " + selectedProduit.getStock() + " unités en stock");
                        return;
                    }

                    // Créer et sauvegarder la commande avec l'ID du client
                    commande nouvelleCommande = new commande();
                    nouvelleCommande.setProduitId(selectedProduit.getId());
                    nouvelleCommande.setQuantite(quantity);
                    nouvelleCommande.setClientId(clientConnecteId);  // Ajout de l'ID du client

                    CommandeService commandeService = new CommandeService();
                    commandeService.ajouter(nouvelleCommande);

                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            quantity + " " + selectedProduit.getNom() + "(s) ajouté(s) au panier");

                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Veuillez entrer un nombre valide");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de l'ajout au panier: " + e.getMessage());
                }
            });
        });
    }

    private void handleAddToCart(produit produit) {
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté au panier");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}