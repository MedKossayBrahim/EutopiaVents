package com.esprit.controllers;

import com.esprit.models.commande;
import com.esprit.models.produit;
import com.esprit.models.User;
import com.esprit.services.CommandeService;
import com.esprit.services.ProduitService;
import com.esprit.tests.Eutopia;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

// Importation de Twilio
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

// Importation de Stripe
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

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
    
    @FXML
    private Button payerButton;

    private final CommandeService commandeService;

    // Remplacez par vos propres identifiants Twilio
    private static final String ACCOUNT_SID = "ACe2643dfd655324bd26444c686a116ab4";
    private static final String AUTH_TOKEN = "b73fdd08329a85786315b83d3e6c430e";
    private static final String TWILIO_PHONE_NUMBER = "+15855583471";
    
    // Clés Stripe
    private static final String STRIPE_SECRET_KEY = "sk_test_51QyaGqFjHGDIBML2p0Fm0oYajDQIelhtlozMKHoX8xgzVZ9hDw79fzegB9D8qmiVXQiHVWSPer3wYOSIWYGvZDoi00K1IfAtms";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51QyaGqFjHGDIBML2QoX8PK4TTtiK0kglXL3bwkJHThgR18N0A5EE31lD7Ca8t2hDOACDt9yi5XAMPDe1b8ZlnTuN00pcUYPJoQ";

    public paniers() throws SQLException {
        commandeService = new CommandeService();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Stripe.apiKey = STRIPE_SECRET_KEY;
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
            produitService = new ProduitService();
            produit prod = produitService.getOne(cmd.getProduitId());
            return new SimpleStringProperty(prod != null ? prod.getNom() : "Produit inconnu");
        });

        panierQuantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        panierPrixColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));

        // Configuration de la colonne des actions
        panierActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final Button editBtn = new Button("Modifier");
            private final Button payBtn = new Button("Payer");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn, payBtn);

            {
                deleteBtn.setOnAction(event -> {
                    commande commande = getTableView().getItems().get(getIndex());
                    supprimerDuPanier(commande);
                });

                editBtn.setOnAction(event -> {
                    commande commande = getTableView().getItems().get(getIndex());
                    modifierQuantite(commande);
                });
                
                payBtn.setOnAction(event -> {
                    commande commande = getTableView().getItems().get(getIndex());
                    handlePaiement(commande);
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
        totalLabel.setText(String.format("%.2f DT", total));
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
        dialog.setContentText("Numéro (format: +216XXXXXXXX):");

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
    
    // Méthode de paiement Stripe
    private void handlePaiement(commande commande) {
        try {
            User currentUser = Eutopia.getCurrentUser();
            if (currentUser == null) {
                showError("Erreur", "Vous devez être connecté pour effectuer un paiement.");
                return;
            }
            
            double prixTotal = 0;
            try {
                prixTotal = commande.getQuantite() * commande.getPrixTotal();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            
            long amount = Math.max(50L, (long) (prixTotal * 100));

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .setDescription(String.format("Commande de produit - Client: %s",
                            currentUser.getEmail()))
                    .setReceiptEmail(currentUser.getEmail())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Paiement Sécurisé");
            dialog.setHeaderText("Paiement pour votre commande");

            WebView webView = new WebView();
            webView.setPrefSize(500, 400);
            WebEngine engine = webView.getEngine();

            // Récupérer le nom du produit
            ProduitService produitService = new ProduitService();
            produit prod = produitService.getOne(commande.getProduitId());
            String nomProduit = prod != null ? prod.getNom() : "Produit";

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Paiement</title>
                    <script src="https://js.stripe.com/v3/"></script>
                    <style>
                        body { 
                            font-family: -apple-system, sans-serif; 
                            padding: 20px; 
                            background: #f8f9fa; 
                            color: #333;
                        }
                        .container { 
                            max-width: 450px; 
                            margin: 0 auto; 
                            background: white; 
                            padding: 30px; 
                            border-radius: 12px;
                            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                        }
                        .form-row {
                            margin-bottom: 25px;
                        }
                        .form-row label {
                            display: block;
                            margin-bottom: 8px;
                            font-weight: 500;
                            color: #2d3748;
                        }
                        #card-element {
                            padding: 15px;
                            border: 2px solid #e2e8f0;
                            border-radius: 8px;
                            background: white;
                            transition: border-color 0.2s ease;
                        }
                        #card-element:hover {
                            border-color: #cbd5e0;
                        }
                        #card-element.StripeElement--focus {
                            border-color: #4299e1;
                            box-shadow: 0 0 0 1px #4299e1;
                        }
                        button { 
                            background: #4CAF50; 
                            color: white; 
                            padding: 16px;
                            border: none; 
                            border-radius: 8px; 
                            width: 100%%; 
                            margin: 25px 0 15px; 
                            font-size: 16px;
                            font-weight: 600;
                            cursor: pointer;
                            transition: background-color 0.2s ease;
                        }
                        button:hover {
                            background: #43a047;
                        }
                        button:disabled {
                            background: #9e9e9e;
                            cursor: not-allowed;
                        }
                        .success { 
                            color: #2f855a; 
                            margin-top: 15px; 
                            text-align: center;
                            font-weight: 500;
                        }
                        
                        .amount {
                            font-size: 24px;
                            font-weight: 600;
                            text-align: center;
                            margin-bottom: 30px;
                            color: #2d3748;
                        }
                        .product-info {
                            text-align: center;
                            margin-bottom: 20px;
                            font-size: 18px;
                            color: #4a5568;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="product-info">%s x%d</div>
                        <div class="amount">%.2f DT</div>
                        <form id="payment-form">
                            <div class="form-row">
                                <label for="card-element">Informations de carte</label>
                                <div id="card-element"></div>
                            </div>
                            <button type="submit" id="submit-button">Payer maintenant</button>
                            <div id="success-message" class="success"></div>
                        </form>
                        
                    </div>

                    <script>
                        const stripe = Stripe('%s');
                        const elements = stripe.elements();
                        const card = elements.create('card', {
                            style: {
                                base: {
                                    fontSize: '16px',
                                    fontFamily: '-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif',
                                    color: '#2d3748',
                                    '::placeholder': {
                                        color: '#a0aec0'
                                    },
                                    padding: '12px'
                                }
                            }
                        });
                        card.mount('#card-element');

                        const form = document.getElementById('payment-form');
                        const submitButton = document.getElementById('submit-button');
                        const successDiv = document.getElementById('success-message');

                        form.addEventListener('submit', async (e) => {
                            e.preventDefault();
                            submitButton.disabled = true;
                            submitButton.textContent = 'Traitement en cours...';

                            const result = await stripe.confirmCardPayment('%s', {
                                payment_method: {
                                    card: card,
                                    billing_details: {
                                        email: '%s',
                                        name: '%s'
                                    }
                                }
                            });

                            if (result.paymentIntent.status === 'succeeded') {
                                successDiv.textContent = 'Paiement réussi!';
                                window.paymentSuccessful = true;
                            }
                        });
                    </script>
                </body>
                </html>
            """, nomProduit, commande.getQuantite(), prixTotal, STRIPE_PUBLIC_KEY, paymentIntent.getClientSecret(),
                    currentUser.getEmail(),
                    currentUser.getFullname());

            engine.loadContent(htmlContent);

            final boolean[] paymentSuccessful = {false};
            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    Timer timer = new Timer(true);
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    Boolean success = (Boolean) engine.executeScript("window.paymentSuccessful === true");
                                    if (Boolean.TRUE.equals(success)) {
                                        paymentSuccessful[0] = true;
                                        dialog.close();
                                        finalizePayment(commande);
                                        this.cancel();
                                    }
                                } catch (Exception e) {
                                    // Ignorer les erreurs de polling
                                }
                            });
                        }
                    }, 1000, 500);
                }
            });

            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(cancelButton);
            dialog.getDialogPane().setContent(webView);
            dialog.getDialogPane().setPrefSize(550, 600);

            dialog.showAndWait();

        } catch (Exception ex) {
            showError("Erreur de paiement", "Une erreur s'est produite lors du paiement.");
            ex.printStackTrace();
        }
    }
    
    private void finalizePayment(commande commande) {
        try {
            User currentUser = Eutopia.getCurrentUser();
            
            // Since there's no status field in the commande table, we'll handle payment completion differently
            // Option 1: Remove the item from the cart after successful payment
            commandeService.supprimer(commande);
            
            // Option 2: We could also create a new table for payment records if needed in the future
            
            // Rafraîchir le panier
            loadPanier();

            // Afficher une confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Paiement réussi");
            alert.setHeaderText("Transaction complétée");
            alert.setContentText("Votre paiement a été traité avec succès.");
            alert.show();
            
        } catch (Exception e) {
            showError("Erreur", "Impossible de finaliser le paiement.");
            e.printStackTrace();
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}