package com.esprit.controllers;

import com.esprit.models.Reservations;
import com.esprit.models.Evenement;
import com.esprit.models.User;
import com.esprit.services.ReservationsService;
import com.esprit.services.EvenementService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.net.URL;
import java.util.ResourceBundle;
import com.esprit.services.EmailService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import java.util.Timer;
import java.util.TimerTask;

public class PanierController implements Initializable {

    @FXML private VBox reservationsContainer;
    @FXML private Label totalLabel;
    @FXML private Button retourButton;

    private ReservationsService reservationsService = new ReservationsService();
    private EvenementService evenementService = new EvenementService();
    private EmailService emailService = new EmailService();

    private static final String STRIPE_SECRET_KEY = "sk_test_51QwmhtQKdiXHPvYQwJdPEWTkpkgcggyDOztY0l1lrRGatINKkvmpEnU4ts2OFo0pY6FxlKVBcp1OGdLc7QTdnrrs00VvUzplT6";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51QwmhtQKdiXHPvYQ5hgxvOX1EHFdJvqk3V8lknUFTzKIMHEYAHbNNsDkFAdhuGOKtdesQg8UCPhlK1EzkJ9yIkuK00qk4gUFEA";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser == null) {
            showNoUserMessage();
            return;
        }
        loadReservations(currentUser.getUserID());
    }

    private void showNoUserMessage() {
        reservationsContainer.getChildren().clear();
        Label messageLabel = new Label("Veuillez vous connecter pour voir votre panier");
        messageLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
        reservationsContainer.getChildren().add(messageLabel);
        totalLabel.setText("0.00 TND");
    }

    private void loadReservations(int userId) {
        reservationsContainer.getChildren().clear();
        double totalPanier = 0;

        var reservations = reservationsService.rechercherParUtilisateur(userId).stream()
                .filter(r -> "en_attente".equals(r.getStatut()))
                .toList();

        if (reservations.isEmpty()) {
            Label emptyLabel = new Label("Votre panier est vide");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");
            reservationsContainer.getChildren().add(emptyLabel);
        } else {
            for (Reservations reservation : reservations) {
                Evenement event = evenementService.rechercherParId(reservation.getEvenementId());
                VBox card = createReservationCard(reservation, event);
                reservationsContainer.getChildren().add(card);
                totalPanier += reservation.getPrixTotal();
            }
        }

        totalLabel.setText(String.format("%.2f TND", totalPanier));
    }

    private VBox createReservationCard(Reservations reservation, Evenement event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5;");
        card.setPadding(new Insets(10));

        // Titre et informations
        Label titreLabel = new Label(event.getTitre());
        titreLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        String lieu = event.getLieuId() != 0 ? event.getLieuNom() : event.getLieu_proprietaire();
        Label infoLabel = new Label(String.format("Date: %s\nLieu: %s", event.getDateDebut(), lieu));

        // Prix et quantité
        HBox prixQuantiteBox = new HBox(20);
        prixQuantiteBox.setAlignment(Pos.CENTER_LEFT);

        Label prixLabel = new Label(String.format("Prix unitaire: %.2f TND", event.getPrix()));

        Spinner<Integer> quantiteSpinner = new Spinner<>(1, 100, reservation.getQuantite());
        quantiteSpinner.setMaxWidth(100);
        quantiteSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            reservation.setQuantite(newValue);
            reservation.setPrixTotal(event.getPrix() * newValue);
            reservationsService.modifier(reservation);
            loadReservations(reservation.getUtilisateurId());
        });

        Label totalLabel = new Label(String.format("Total: %.2f TND", reservation.getPrixTotal()));
        prixQuantiteBox.getChildren().addAll(prixLabel, new Label("Quantité:"), quantiteSpinner, totalLabel);

        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button confirmerBtn = new Button("Payer");
        confirmerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        confirmerBtn.setOnAction(e -> handlePaiement(reservation, event));

        Button annulerBtn = new Button("Annuler");
        annulerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        annulerBtn.setOnAction(e -> {
            reservation.setStatut("annulé");
            reservationsService.modifier(reservation);
            loadReservations(reservation.getUtilisateurId());
        });

        Label statutLabel = new Label("Statut: " + reservation.getStatut());
        statutLabel.setStyle("-fx-font-style: italic;");

        buttonsBox.getChildren().addAll(confirmerBtn, annulerBtn);
        card.getChildren().addAll(titreLabel, infoLabel, prixQuantiteBox, statutLabel, buttonsBox);

        return card;
    }

    @FXML
    private void handleRetour() {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource("/events-view.fxml"));
            retourButton.getScene().setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger la page /events-view.fxml");
        }
    }

    private void handlePaiement(Reservations reservation, Evenement event) {
        try {
            User currentUser = Eutopia.getCurrentUser();
            long amount = Math.max(50L, (long) (reservation.getPrixTotal() * 100));
            
            // Créer l'intention de paiement
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .setDescription("Billet pour " + event.getTitre() + " - Client: " + currentUser.getEmail())
                    .setReceiptEmail(currentUser.getEmail())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Créer la boîte de dialogue de paiement
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Paiement Sécurisé");
            dialog.setHeaderText("Paiement pour " + event.getTitre());

            WebView webView = new WebView();
            webView.setPrefSize(500, 400);
            WebEngine engine = webView.getEngine();

            // Contenu HTML pour le formulaire de paiement
            String htmlContent = createPaymentFormHtml(
                    reservation.getPrixTotal(), 
                    STRIPE_PUBLIC_KEY, 
                    paymentIntent.getClientSecret(), 
                    currentUser.getEmail(), 
                    currentUser.getNom() + " " + currentUser.getPrenom()
            );

            engine.loadContent(htmlContent);

            // Surveiller le succès du paiement
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
                                        dialog.close();
                                        finalizePayment(reservation, event, currentUser);
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
        }
    }

    private String createPaymentFormHtml(double amount, String publicKey, String clientSecret, String email, String name) {
        return String.format("""
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
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="amount">%.2f TND</div>
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
        """, amount, publicKey, clientSecret, email, name);
    }

    private void finalizePayment(Reservations reservation, Evenement event, User currentUser) {
        try {
            reservationsService.confirmerAchat(reservation.getId(), reservation.getQuantite());
            
            javafx.application.Platform.runLater(() -> {
                loadReservations(currentUser.getUserID());
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Paiement réussi");
                alert.setHeaderText("Transaction complétée");
                alert.setContentText("Votre billet est en cours d'envoi à " + currentUser.getEmail());
                alert.show();

                new Thread(() -> {
                    try {
                        emailService.envoyerBillet(currentUser.getEmail(), reservation, event);
                        javafx.application.Platform.runLater(() -> {
                            alert.close();
                            showInfoDialog("Email Envoyé", "Votre billet a été envoyé à " + currentUser.getEmail());
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            alert.close();
                            showError("Erreur d'envoi", "L'achat a été confirmé mais l'envoi du billet a échoué.");
                        });
                    }
                }).start();
            });
        } catch (Exception e) {
            showError("Erreur", "Impossible de finaliser le paiement.");
        }
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}