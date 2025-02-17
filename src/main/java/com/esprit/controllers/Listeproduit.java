package com.esprit.controllers;

import com.esprit.models.commande;
import com.esprit.models.produit;
import com.esprit.services.CommandeService;
import com.esprit.services.ProduitService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;

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

    private final ProduitService produitService;

    // Ajout d'une variable pour stocker l'ID du client connecté
    private static int clientConnecteId;  // Vous devrez définir cette valeur lors de la connexion

    public Listeproduit() throws SQLException {
        produitService = new ProduitService();
    }

    @FXML
    public void initialize() {
        setupColumns();
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
                modifyBtn.setOnAction(event -> {
                    produit selectedProduit = getTableView().getItems().get(getIndex());
                    ouvrirModification(selectedProduit);
                });

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

    private void loadProduits() {
        produitTable.getItems().clear();
        produitTable.getItems().addAll(produitService.rechercher());
    }

    private void ouvrirModification(produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierProduit.fxml"));
            Parent root = loader.load();

            ModifierProduit controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
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