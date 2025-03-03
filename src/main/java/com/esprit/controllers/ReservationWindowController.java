package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.models.Reservation;
import com.esprit.models.User;
import com.esprit.services.MaterielService;
import com.esprit.services.ReservationService;
import com.esprit.services.UserService;
import com.esprit.services.EmailServiceY;
import com.esprit.tests.Eutopia;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import netscape.javascript.JSObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class ReservationWindowController {
    @FXML
    private TableView<Materiel> materielsTable;
    @FXML
    private TableColumn<Materiel, Boolean> selectColumn;
    @FXML
    private TableColumn<Materiel, String> libelleColumn;
    @FXML
    private TableColumn<Materiel, String> descriptionColumn;
    @FXML
    private TableColumn<Materiel, String> prixColumn;
    @FXML
    private TableColumn<Materiel, String> stockColumn;
    @FXML
    private TableColumn<Materiel, String> quantiteColumn;
    @FXML
    private Button confirmerButton;
    @FXML
    private Button annulerButton;
    @FXML
    private TextField searchField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Label prixTotalLabel;

    private final MaterielService materielService = new MaterielService();
    private final Map<Materiel, SimpleBooleanProperty> selectionProperties = new HashMap<>();
    private final Map<Materiel, Spinner<Integer>> quantitySpinners = new HashMap<>();
    private final Map<Materiel, Integer> reservations = new HashMap<>();
    private ObservableList<Materiel> materiels;
    private FilteredList<Materiel> filteredMateriels;
    private int currentUserId;

    // Clés Stripe
    private static final String STRIPE_SECRET_KEY = "sk_test_51QxseJEozKp8FzlsT3iPfUCSswtp1cb2eBFOQTdQZ5lLLrPiZ8qoJ8Nm28FBiUqNM2xrLB3cW669cRE3z6kghV7T00lNVOHuxF";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51QxseJEozKp8FzlsD4etldXKYHMODlm3XlE7ZFjuudkzeQSJdd4QZ2WgRcJoUPv7Up3sfoWdNbnPj28ADypXEzOH00E1aI86nv";

    public ReservationWindowController(int userId) throws SQLException {
        this.currentUserId = userId;
        // Initialiser l'API Stripe avec la clé secrète
        try {
            Stripe.apiKey = STRIPE_SECRET_KEY;
        } catch (Exception e) {
            System.err.println("Erreur d'initialisation de Stripe: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadMateriels();
        setupSearch();
        setupButtons();
        setupDatePickers();
    }

    private void setupColumns() {
        // Configuration de la colonne de sélection
        selectColumn.setCellValueFactory(param -> {
            Materiel materiel = param.getValue();
            SimpleBooleanProperty booleanProp = selectionProperties.computeIfAbsent(materiel,
                    k -> new SimpleBooleanProperty(false));
            booleanProp.addListener((obs, oldVal, newVal) -> {
                Spinner<Integer> spinner = quantitySpinners.get(materiel);
                if (spinner != null) {
                    spinner.setDisable(!newVal);
                    if (!newVal) {
                        spinner.getValueFactory().setValue(0);
                    }
                }
                updatePrixTotal();
            });
            return booleanProp;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // Configuration des autres colonnes
        libelleColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLibelle()));
        descriptionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDescription()));
        prixColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPrix() + " TND"));
        stockColumn.setCellValueFactory(param -> new SimpleStringProperty(String.valueOf(param.getValue().getQuantite())));

        // Configuration de la colonne quantité avec Spinner
        quantiteColumn.setCellFactory(column -> new TableCell<>() {
            private Spinner<Integer> spinner;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    spinner = new Spinner<>(0, materiel.getQuantite(), 0);
                    spinner.setEditable(true);
                    spinner.setPrefWidth(80);

                    SimpleBooleanProperty booleanProp = selectionProperties.get(materiel);
                    spinner.setDisable(booleanProp == null || !booleanProp.get());

                    spinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrixTotal());
                    quantitySpinners.put(materiel, spinner);
                    setGraphic(spinner);
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredMateriels.setPredicate(materiel -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return materiel.getLibelle().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void loadMateriels() {
        materiels = FXCollections.observableArrayList(materielService.rechercher());
        filteredMateriels = new FilteredList<>(materiels, p -> true);
        materielsTable.setItems(filteredMateriels);
    }

    private void setupButtons() {
        confirmerButton.setOnAction(e -> handleConfirmation());
        annulerButton.setOnAction(e -> ((Stage) annulerButton.getScene().getWindow()).close());
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        dateDebutPicker.setValue(today);
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(dateDebutPicker.getValue()) < 0);
            }
        });

        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dateDebutPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date requise", "La date de début est obligatoire.");
                dateDebutPicker.setValue(today);
                return;
            }
            dateDebutPicker.setStyle("-fx-border-color: #999; -fx-border-width: 1px;");
            if (dateFinPicker.getValue() != null && dateFinPicker.getValue().compareTo(newVal) < 0) {
                dateFinPicker.setValue(newVal);
            }
            updatePrixTotal();
        });

        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date requise", "La date de fin est obligatoire.");
                dateFinPicker.setValue(dateDebutPicker.getValue());
                return;
            }
            if (newVal.isBefore(dateDebutPicker.getValue())) {
                dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date invalide", "La date de fin doit être égale ou postérieure à la date de début.");
                dateFinPicker.setValue(dateDebutPicker.getValue());
                return;
            }
            dateFinPicker.setStyle("-fx-border-color: #999; -fx-border-width: 1px;");
            updatePrixTotal();
        });
    }

    private void updatePrixTotal() {
        double prixTotal = 0.0;
        long nombreJours = 0;

        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            nombreJours = java.time.temporal.ChronoUnit.DAYS.between(dateDebutPicker.getValue(), dateFinPicker.getValue()) + 1;
        }

        for (Map.Entry<Materiel, SimpleBooleanProperty> entry : selectionProperties.entrySet()) {
            if (entry.getValue().get()) {
                Materiel materiel = entry.getKey();
                Spinner<Integer> spinner = quantitySpinners.get(materiel);
                if (spinner != null) {
                    int quantite = spinner.getValue();
                    prixTotal += materiel.getPrix() * quantite * nombreJours;
                }
            }
        }

        prixTotalLabel.setText(String.format("%.2f TND", prixTotal));
    }

    private double calculateTotalAmount() {
        double totalAmount = 0.0;
        long nombreJours = java.time.temporal.ChronoUnit.DAYS.between(
                dateDebutPicker.getValue(),
                dateFinPicker.getValue()
        ) + 1;

        for (Map.Entry<Materiel, SimpleBooleanProperty> entry : selectionProperties.entrySet()) {
            if (entry.getValue().get()) {
                Materiel materiel = entry.getKey();
                Spinner<Integer> spinner = quantitySpinners.get(materiel);
                if (spinner != null) {
                    int quantite = spinner.getValue();
                    totalAmount += materiel.getPrix() * quantite * nombreJours;
                }
            }
        }
        return totalAmount;
    }

    private void handleConfirmation() {
        if (dateDebutPicker.getValue() == null) {
            dateDebutPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.WARNING, "Date manquante", "Veuillez sélectionner une date de début.");
            return;
        }

        if (dateFinPicker.getValue() == null) {
            dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.WARNING, "Date manquante", "Veuillez sélectionner une date de fin.");
            return;
        }

        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (dateDebut.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "La date de début ne peut pas être dans le passé.");
            return;
        }

        if (dateFin.isBefore(dateDebut)) {
            showAlert(Alert.AlertType.ERROR, "Dates invalides", "La date de fin doit être égale ou postérieure à la date de début.");
            return;
        }

        reservations.clear();
        List<Materiel> selectedMateriels = new ArrayList<>();

        selectionProperties.forEach((materiel, selected) -> {
            if (selected.get()) {
                Spinner<Integer> spinner = quantitySpinners.get(materiel);
                int quantity = spinner.getValue();
                if (quantity > 0) {
                    selectedMateriels.add(materiel);
                    reservations.put(materiel, quantity);
                }
            }
        });

        if (selectedMateriels.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner au moins un matériel et spécifier une quantité.");
            return;
        }

        boolean quantitiesOk = reservations.entrySet().stream()
                .allMatch(entry -> entry.getValue() <= entry.getKey().getQuantite());

        if (!quantitiesOk) {
            showAlert(Alert.AlertType.ERROR, "Quantité invalide", "Une ou plusieurs quantités demandées dépassent le stock disponible.");
            return;
        }

        Reservation reservation = new Reservation();
        reservation.setPrixTotal(calculateTotalAmount());

        System.out.println("Handling payment for reservation with total price: " + reservation.getPrixTotal());

        handlePaiement(reservation);
    }

    private void handlePaiement(Reservation reservation) {
        try {
            User currentUser = Eutopia.getCurrentUser();
            long amount = Math.max(50L, (long) (reservation.getPrixTotal() * 100));

            System.out.println("Preparing payment for user: " + currentUser.getEmail() + " with amount: " + amount);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .setDescription(String.format("Réservation de matériel - Client: %s", currentUser.getEmail()))
                    .setReceiptEmail(currentUser.getEmail())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println("Payment Intent created: " + paymentIntent.getId());

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Paiement Sécurisé");
            dialog.setHeaderText("Paiement pour votre réservation");

            WebView webView = new WebView();
            webView.setPrefSize(500, 400);
            WebEngine engine = webView.getEngine();

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
                        .amount {
                            font-size: 24px;
                            font-weight: bold;
                            text-align: center;
                            margin-bottom: 20px;
                            color: #2d3748;
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
                        #card-element.invalid {
                            border-color: #e53e3e;
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
                            transition: all 0.2s ease;
                        }
                        button:disabled {
                            background: #9ca3af;
                            cursor: not-allowed;
                        }
                        button:hover:not(:disabled) {
                            background: #43a047;
                        }
                        .success { 
                            color: #2f855a; 
                            margin-top: 10px;
                            text-align: center;
                            font-weight: 500;
                        }
                        .error { 
                            color: #e53e3e; 
                            margin-top: 10px;
                            font-size: 14px;
                        }
                        .success-animation {
                            display: none;
                            text-align: center;
                            padding: 20px;
                            margin-top: 20px;
                        }
                        .success-animation.show {
                            display: block;
                            animation: fadeIn 0.5s ease-in;
                        }
                        .success-checkmark {
                            width: 80px;
                            height: 80px;
                            margin: 0 auto 20px;
                            border-radius: 50%%;
                            background: #4CAF50;
                            position: relative;
                        }
                        .success-checkmark:after {
                            content: '';
                            position: absolute;
                            top: 50%%;
                            left: 50%%;
                            transform: translate(-50%%, -60%%) rotate(45deg);
                            height: 40px;
                            width: 20px;
                            border-right: 4px solid white;
                            border-bottom: 4px solid white;
                        }
                        .success-text {
                            color: #2f855a;
                            font-size: 24px;
                            font-weight: bold;
                            margin: 10px 0;
                        }
                        .success-details {
                            color: #4a5568;
                            margin-top: 10px;
                        }
                        @keyframes fadeIn {
                            from { opacity: 0; transform: translateY(20px); }
                            to { opacity: 1; transform: translateY(0); }
                        }
                        #payment-form.success .form-row,
                        #payment-form.success button {
                            display: none;
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
            """, reservation.getPrixTotal(), STRIPE_PUBLIC_KEY, paymentIntent.getClientSecret(),
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
                                        finalizePayment(reservation, currentUser);
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
            System.err.println("Erreur de paiement: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors du paiement: " + ex.getMessage());
        }
    }

    private void finalizePayment(Reservation reservation, User currentUser) {
        try {
            ReservationService reservationService = new ReservationService();

            // Convertir LocalDate en java.util.Date
            java.util.Date dateDebut = java.sql.Date.valueOf(dateDebutPicker.getValue());
            java.util.Date dateFin = java.sql.Date.valueOf(dateFinPicker.getValue());

            // Pour chaque matériel sélectionné, créer une réservation distincte
            selectionProperties.forEach((materiel, selected) -> {
                if (selected.get()) {
                    Spinner<Integer> spinner = quantitySpinners.get(materiel);
                    if (spinner != null && spinner.getValue() > 0) {
                        // Créer une nouvelle réservation pour ce matériel
                        Reservation materielReservation = new Reservation();
                        materielReservation.setUserId(currentUser.getUserID());
                        materielReservation.setStatus("PAYÉ");
                        materielReservation.setDateDebut(dateDebut);
                        materielReservation.setDateFin(dateFin);

                        // Calculer le prix total pour ce matériel
                        long nombreJours = java.time.temporal.ChronoUnit.DAYS.between(
                                dateDebutPicker.getValue(),
                                dateFinPicker.getValue()
                        ) + 1;
                        double prixMateriel = materiel.getPrix() * spinner.getValue() * nombreJours;
                        materielReservation.setPrixTotal(prixMateriel);

                        // Ajouter le matériel et sa quantité
                        materielReservation.setMaterielId(materiel.getId());
                        materielReservation.setQuantite(spinner.getValue());

                        try {
                            // Sauvegarder la réservation
                            reservationService.ajouter(materielReservation);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'ajout de la réservation pour le matériel " + materiel.getLibelle() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });

            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Paiement réussi");
                alert.setHeaderText("Transaction complétée");
                alert.setContentText("Votre confirmation est en cours d'envoi à " + currentUser.getEmail());
                alert.show();

                new Thread(() -> {
                    try {
                        sendConfirmationEmail(currentUser, reservation);
                        javafx.application.Platform.runLater(() -> {
                            alert.close();
                            Alert emailSentAlert = new Alert(Alert.AlertType.INFORMATION);
                            emailSentAlert.setTitle("Email Envoyé");
                            emailSentAlert.setContentText("Votre confirmation a été envoyée à " + currentUser.getEmail());
                            emailSentAlert.showAndWait();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            alert.close();
                            showAlert(Alert.AlertType.ERROR, "Erreur d'envoi", "La réservation a été confirmée mais l'envoi de l'email a échoué.");
                        });
                    }
                }).start();
            });
        } catch (Exception e) {
            System.err.println("DEBUG: Error in finalizePayment: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la finalisation du paiement: " + e.getMessage());
        }
    }

    private void sendConfirmationEmail(User user, Reservation reservation) {
        try {
            System.out.println("Début de l'envoi de l'email de confirmation...");
            EmailServiceY emailService = new EmailServiceY();
            String subject = "Confirmation de votre réservation";

            // Construction du corps de l'email avec plus de détails
            StringBuilder body = new StringBuilder();
            body.append(String.format("Bonjour %s,\n\n", user.getFullname()));
            body.append("Votre réservation a été confirmée avec succès.\n\n");
            body.append("Détails de la réservation:\n");
            body.append(String.format("- Date de début: %s\n", dateDebutPicker.getValue()));
            body.append(String.format("- Date de fin: %s\n", dateFinPicker.getValue()));
            body.append(String.format("- Montant total: %.2f TND\n\n", reservation.getPrixTotal()));
            body.append("Articles réservés:\n");

            for (Map.Entry<Materiel, Integer> entry : reservations.entrySet()) {
                body.append(String.format("- %s x%d\n",
                        entry.getKey().getLibelle(),
                        entry.getValue()));
            }

            body.append("\nMerci de votre confiance!");

            System.out.println("Envoi de l'email à " + user.getEmail());
            emailService.sendEmail(user.getEmail(), subject, body.toString());
            System.out.println("Email de confirmation envoyé avec succès à " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}