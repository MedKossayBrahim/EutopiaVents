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

// Importation de Twilio
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

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

    @FXML
    private Button confirmerSmsButton;

    private final CommandeService commandeService;

    // Remplacez par vos propres identifiants Twilio
    private static final String ACCOUNT_SID = "ACe2643dfd655324bd26444c686a116ab4";
    private static final String AUTH_TOKEN = "b73fdd08329a85786315b83d3e6c430e";
    private static final String TWILIO_PHONE_NUMBER = "+15855583471";

    public paniers() throws SQLException {
        commandeService = new CommandeService();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
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
        confirmerSmsButton.setOnAction(event -> confirmerParSms());
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
                envoyerSMSConfirmation("+21627534166"); // Remplacez par le numéro du client
                panierTable.getItems().clear();
                updateTotal();
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Votre commande a été validée avec succès ! Un SMS de confirmation a été envoyé.");
                success.showAndWait();
            }
        });
    }

    private void confirmerParSms() {
        if (panierTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Panier vide");
            alert.setContentText("Votre panier est vide");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("+216");
        dialog.setTitle("Confirmation par SMS");
        dialog.setHeaderText("Entrez votre numéro de téléphone");
        dialog.setContentText("Numéro (format: +21651707908):");

        dialog.showAndWait().ifPresent(numero -> {
            if (numero.matches("\\+\\d{8,15}")) {
                try {
                    // Envoyer le SMS de confirmation
                    envoyerSMSConfirmation(numero);

                    // Vider le panier après confirmation
                    for (commande cmd : panierTable.getItems()) {
                        commandeService.supprimer(cmd);
                    }

                    panierTable.getItems().clear();
                    updateTotal();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setContentText("Votre commande a été confirmée ! Un SMS a été envoyé au " + numero);
                    success.showAndWait();
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setContentText("Erreur lors de l'envoi du SMS: " + e.getMessage());
                    error.showAndWait();
                }
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Format invalide");
                error.setContentText("Le format du numéro de téléphone est invalide. Utilisez le format +216XXXXXXXX");
                error.showAndWait();
            }
        });
    }

    private void envoyerSMSConfirmation(String numeroClient) {
        String messageTexte = "Votre commande a été confirmée avec succès. Merci pour votre achat !";
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(numeroClient),
                new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                messageTexte
        ).create();
        System.out.println("SMS envoyé: " + message.getSid());
    }
}