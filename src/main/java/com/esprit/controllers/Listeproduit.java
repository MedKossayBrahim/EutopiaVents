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

import java.io.ByteArrayInputStream;
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
            if (p.getImage() != null) {
                Image image = new Image(new ByteArrayInputStream(p.getImage())); // Convertir le tableau d'octets en Image
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
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredProduits.size());
        List<produit> pageItems = filteredProduits.subList(fromIndex, toIndex);

        produitsGrid.getChildren().clear();

        int row = 0;
        int col = 0;
        for (produit produit : pageItems) {
            produitsGrid.add(createProduitCard(produit), col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }

        return new VBox(produitsGrid);
    }

    private VBox createProduitCard(produit produit) {
        VBox card = new VBox(5);
        card.getStyleClass().add("produit-card");

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("image-container");
        ImageView imageView = new ImageView();
        if (produit.getImage() != null) {
            Image image = new Image(new ByteArrayInputStream(produit.getImage()));
            imageView.setImage(image);
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        // Informations produit
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");

        Label prixLabel = new Label(String.format("%.2f €", produit.getPrix()));
        prixLabel.getStyleClass().add("product-price");

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.getStyleClass().add("product-stock");

        // Boutons
        HBox buttonContainer = new HBox(5);
        buttonContainer.getStyleClass().add("button-container");
        buttonContainer.setAlignment(Pos.CENTER);

        Button modifyBtn = new Button("Modifier");
        modifyBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("danger-button");


        deleteBtn.setOnAction(e -> supprimerProduit(produit));

        buttonContainer.getChildren().addAll(modifyBtn, deleteBtn);

        card.getChildren().addAll(imageContainer, nomLabel, prixLabel, stockLabel, buttonContainer);
        return card;
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}