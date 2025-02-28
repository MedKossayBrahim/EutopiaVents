package com.esprit.controllers;

import com.esprit.models.commande;
import com.esprit.models.produit;
import com.esprit.services.CommandeService;
import com.esprit.services.ProduitService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;

public class paniers {
    @FXML
    private TableView<commande> panierTable;
    @FXML
    private TableColumn<commande, String> panierProduitColumn;
    @FXML
    private TableColumn<commande, Integer> panierQuantiteColumn;
    @FXML
    private TableColumn<commande, Double> panierPrixColumn;
    @FXML
    private TableColumn<commande, Void> panierActionsColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private Button validerPanierButton;

    private final CommandeService commandeService;

    public paniers() throws SQLException {
        commandeService = new CommandeService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadPanier();
        setupActions();
    }

    private void setupColumns() {
        panierProduitColumn.setCellValueFactory(cellData -> {
            commande cmd = cellData.getValue();
            ProduitService produitService = null;
            try {
                produitService = new ProduitService();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            produit prod = produitService.getOne(cmd.getProduitId());
            return new SimpleStringProperty(prod != null ? prod.getNom() : "Produit inconnu");
        });

        panierQuantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        panierPrixColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));

        // Configuration de la colonne des actions
        panierActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final Button editBtn = new Button("Modifier");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                deleteBtn.setOnAction(event -> {
                    commande commande = getTableView().getItems().get(getIndex());
                    supprimerDuPanier(commande);
                });

                editBtn.setOnAction(event -> {
                    commande commande = getTableView().getItems().get(getIndex());
                    modifierQuantite(commande);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadPanier() {
        panierTable.getItems().clear();
        panierTable.getItems().addAll(commandeService.rechercher());
        updateTotal();
    }

    private void setupActions() {
        validerPanierButton.setOnAction(event -> validerPanier());
    }

    private void supprimerDuPanier(commande commande) {
        commandeService.supprimer(commande);
        loadPanier();
    }

    private void modifierQuantite(commande commande) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(commande.getQuantite()));
        dialog.setTitle("Modifier la quantité");
        dialog.setHeaderText("Entrez la nouvelle quantité :");
        dialog.setContentText("Quantité :");

        dialog.showAndWait().ifPresent(quantity -> {
            try {
                int nouvelleQuantite = Integer.parseInt(quantity);
                if (nouvelleQuantite > 0) {
                    commande.setQuantite(nouvelleQuantite);
                    commandeService.modifier(commande);
                    loadPanier();
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Veuillez entrer un nombre valide");
                alert.showAndWait();
            }
        });
    }

    private void updateTotal() {
        double total = panierTable.getItems().stream()
                .mapToDouble(commande -> {
                    try {
                        return commande.getQuantite() * commande.getPrixTotal();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .sum();
        totalLabel.setText(String.format("%.2f €", total));
    }

    private void validerPanier() {
        if (panierTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Panier vide");
            alert.setContentText("Votre panier est vide");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Validation du panier");
        confirmation.setContentText("Voulez-vous valider votre panier ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Ici, ajoutez la logique pour finaliser la commande
                panierTable.getItems().clear();
                updateTotal();
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Votre commande a été validée avec succès !");
                success.showAndWait();
            }
        });
    }
}