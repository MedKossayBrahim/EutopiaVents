package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.models.Reservation;
import com.esprit.services.MaterielService;
import com.esprit.services.ReservationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import javafx.beans.property.SimpleBooleanProperty;
import com.esprit.services.UserService;
import com.esprit.services.EmailService;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

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

    public ReservationWindowController(int userId) {
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
        libelleColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLibelle()));
        descriptionColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getDescription()));
        prixColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getPrix() + " TND"));
        stockColumn.setCellValueFactory(param ->
                new SimpleStringProperty(String.valueOf(param.getValue().getQuantite())));

        // Correction de la configuration de la colonne quantité avec Spinner
        quantiteColumn.setCellFactory(column -> new TableCell<>() {
            private Spinner<Integer> spinner;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    // Créer un nouveau spinner pour chaque cellule
                    spinner = new Spinner<>(0, materiel.getQuantite(), 0);
                    spinner.setEditable(true);
                    spinner.setPrefWidth(80);

                    // Vérifier si le matériel est sélectionné
                    SimpleBooleanProperty booleanProp = selectionProperties.get(materiel);
                    spinner.setDisable(booleanProp == null || !booleanProp.get());

                    // Ajouter le listener pour mettre à jour le prix total
                    spinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePrixTotal());

                    // Stocker le spinner dans la map
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
        // Définir la date du jour comme date minimale
        LocalDate today = LocalDate.now();

        // Configuration du DatePicker de début
        dateDebutPicker.setValue(today);
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Configuration du DatePicker de fin
        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(dateDebutPicker.getValue()) < 0);
            }
        });

        // Ajouter un style pour indiquer que les champs sont obligatoires
        dateDebutPicker.setStyle("-fx-border-color: #999; -fx-border-width: 1px;");
        dateFinPicker.setStyle("-fx-border-color: #999; -fx-border-width: 1px;");

        // Mettre à jour la date de fin minimale quand la date de début change
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Si la date de début est effacée, afficher une erreur
                dateDebutPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date requise",
                        "La date de début est obligatoire.");
                dateDebutPicker.setValue(today); // Remettre la date du jour
                return;
            }

            dateDebutPicker.setStyle("-fx-border-color: #999; -fx-border-width: 1px;");

            // Vérifier et ajuster la date de fin si nécessaire
            if (dateFinPicker.getValue() != null &&
                    dateFinPicker.getValue().compareTo(newVal) < 0) {
                dateFinPicker.setValue(newVal);
            }
            updatePrixTotal();
        });

        // Contrôle de la date de fin
        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                // Si la date de fin est effacée, afficher une erreur
                dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date requise",
                        "La date de fin est obligatoire.");
                dateFinPicker.setValue(dateDebutPicker.getValue()); // Mettre la même date que le début
                return;
            }

            if (newVal.isBefore(dateDebutPicker.getValue())) {
                dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                showAlert(Alert.AlertType.WARNING, "Date invalide",
                        "La date de fin doit être égale ou postérieure à la date de début.");
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

        // Calculer le nombre de jours si les dates sont sélectionnées
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            nombreJours = java.time.temporal.ChronoUnit.DAYS.between(
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue()
            ) + 1; // +1 pour inclure le jour de début
        }

        // Calculer le prix total en fonction des sélections
        for (Map.Entry<Materiel, SimpleBooleanProperty> entry : selectionProperties.entrySet()) {
            if (entry.getValue().get()) { // Si le matériel est sélectionné
                Materiel materiel = entry.getKey();
                Spinner<Integer> spinner = quantitySpinners.get(materiel);
                if (spinner != null) {
                    int quantite = spinner.getValue();
                    prixTotal += materiel.getPrix() * quantite * nombreJours;
                }
            }
        }

        // Mettre à jour le label
        prixTotalLabel.setText(String.format("%.2f TND", prixTotal));
    }

    private void handleConfirmation() {
        // Vérification stricte des dates
        if (dateDebutPicker.getValue() == null) {
            dateDebutPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.WARNING, "Date manquante",
                    "Veuillez sélectionner une date de début.");
            return;
        }

        if (dateFinPicker.getValue() == null) {
            dateFinPicker.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.WARNING, "Date manquante",
                    "Veuillez sélectionner une date de fin.");
            return;
        }

        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        // Vérification supplémentaire des dates
        if (dateDebut.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Date invalide",
                    "La date de début ne peut pas être dans le passé.");
            return;
        }

        if (dateFin.isBefore(dateDebut)) {
            showAlert(Alert.AlertType.ERROR, "Dates invalides",
                    "La date de fin doit être égale ou postérieure à la date de début.");
            return;
        }

        // Vider la map des réservations avant de la remplir
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
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner au moins un matériel et spécifier une quantité.");
            return;
        }

        // Vérification des quantités
        boolean quantitiesOk = reservations.entrySet().stream()
                .allMatch(entry -> entry.getValue() <= entry.getKey().getQuantite());

        if (!quantitiesOk) {
            showAlert(Alert.AlertType.ERROR, "Quantité invalide",
                    "Une ou plusieurs quantités demandées dépassent le stock disponible.");
            return;
        }

        // Calculer le montant total
        double totalAmount = calculateTotalAmount();
        // Conversion approximative de TND vers EUR (à ajuster selon le taux de change actuel)
        double euroAmount = totalAmount * 0.3; // Approximation: 1 TND ≈ 0.3 EUR

        // Créer une boîte de dialogue personnalisée avec deux boutons
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Options de paiement");
        dialog.setHeaderText("Total à payer : " + String.format("%.2f", totalAmount) + " TND" + 
                            " (≈ " + String.format("%.2f", euroAmount) + " EUR)");
        
        // Créer les boutons personnalisés
        ButtonType stripeButtonType = new ButtonType("Payer avec Stripe", ButtonBar.ButtonData.OK_DONE);
        ButtonType reserverButtonType = new ButtonType("Réserver sans paiement", ButtonBar.ButtonData.OTHER);
        ButtonType annulerButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(stripeButtonType, reserverButtonType, annulerButtonType);
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            if (result.get() == stripeButtonType) {
                // Paiement Stripe avec la nouvelle méthode WebView
                processStripePayment(totalAmount, euroAmount);
            } else if (result.get() == reserverButtonType) {
                // Créer la réservation sans paiement
                processReservations(false);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Réservation enregistrée avec succès! Le paiement devra être effectué ultérieurement.");
                ((Stage) confirmerButton.getScene().getWindow()).close();
            }
            // Si l'utilisateur a cliqué sur Annuler, ne rien faire
        }
    }

    /**
     * Traite un paiement avec Stripe en utilisant l'API WebView pour intégrer le formulaire de paiement
     * 
     * @param totalAmount Le montant total en TND
     * @param euroAmount Le montant converti en EUR
     */
    private void processStripePayment(double totalAmount, double euroAmount) {
        try {
            // Récupérer les informations de l'utilisateur via UserService
            UserService userService = new UserService();
            String[] userInfo = userService.getUserEmailAndName(currentUserId);
            if (userInfo == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur utilisateur", 
                        "Impossible de récupérer les informations de l'utilisateur.");
                return;
            }
            
            String userEmail = userInfo[0];
            String userName = userInfo[1];
            
            // Créer une intention de paiement avec Stripe
            long amountInCents = Math.max(50L, (long) (euroAmount * 100)); // Montant minimum de 50 cents
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .setDescription(String.format("Réservation de matériel - Client: %s", userEmail))
                    .setReceiptEmail(userEmail)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Utiliser la boîte de dialogue JavaFX standard au lieu de WebView
            Dialog<ButtonType> paymentDialog = new Dialog<>();
            paymentDialog.setTitle("Paiement Sécurisé");
            paymentDialog.setHeaderText("Paiement pour la réservation de matériel");
            
            // Créer la grille pour les champs
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Informations de paiement
            Label infoLabel = new Label("Montant à payer: " + String.format("%.2f EUR", euroAmount));
            infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(infoLabel, 0, 0, 2, 1);
            
            // Détails de la réservation
            String materielInfo = reservations.entrySet().stream()
                .map(entry -> entry.getKey().getLibelle() + " x" + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Matériel");
                
            String dateRange = dateDebutPicker.getValue().toString() + " au " + dateFinPicker.getValue().toString();
            
            Label reservationLabel = new Label("Réservation: " + materielInfo + "\nPériode: " + dateRange);
            reservationLabel.setStyle("-fx-font-size: 12px;");
            grid.add(reservationLabel, 0, 1, 2, 1);
            
            // Champs pour la carte
            Label cardNumberLabel = new Label("Numéro de carte:");
            TextField cardNumberField = new TextField();
            cardNumberField.setPromptText("1234 5678 9012 3456");
            
            Label expiryLabel = new Label("Date d'expiration (MM/AA):");
            TextField expiryField = new TextField();
            expiryField.setPromptText("MM/AA");
            
            Label cvcLabel = new Label("CVC:");
            TextField cvcField = new TextField();
            cvcField.setPromptText("123");
            
            Label nameLabel = new Label("Nom sur la carte:");
            TextField nameField = new TextField(userName);
            
            // Ajouter les champs à la grille
            grid.add(cardNumberLabel, 0, 3);
            grid.add(cardNumberField, 1, 3);
            grid.add(expiryLabel, 0, 4);
            grid.add(expiryField, 1, 4);
            grid.add(cvcLabel, 0, 5);
            grid.add(cvcField, 1, 5);
            grid.add(nameLabel, 0, 6);
            grid.add(nameField, 1, 6);
            
            paymentDialog.getDialogPane().setContent(grid);
            
            // Boutons
            ButtonType payButtonType = new ButtonType("Payer", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            paymentDialog.getDialogPane().getButtonTypes().addAll(payButtonType, cancelButtonType);
            
            // Afficher la boîte de dialogue et attendre la réponse
            Optional<ButtonType> result = paymentDialog.showAndWait();
            
            // Simuler un paiement réussi si l'utilisateur clique sur "Payer"
            if (result.isPresent() && result.get() == payButtonType) {
                String cardNumber = cardNumberField.getText();
                String expiry = expiryField.getText();
                String cvc = cvcField.getText();
                
                // Validation de base
                if (cardNumber.isEmpty() || expiry.isEmpty() || cvc.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Champs manquants", 
                           "Veuillez remplir tous les champs.");
                    return;
                }
                
                // Afficher un indicateur de progression
                ProgressDialog progressDialog = new ProgressDialog("Traitement du paiement");
                progressDialog.activateProgress();
                
                // Simulation de traitement de paiement (2 secondes)
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            progressDialog.closeProgress();
                            
                            // Finaliser la réservation
                            processReservations(true);
                            showAlert(Alert.AlertType.INFORMATION, "Paiement réussi",
                                    "Votre paiement a été traité avec succès et votre réservation est confirmée.");
                            
                            // Envoyer un email de confirmation
                            try {
                                EmailService emailService = new EmailService();
                                String materielDesc = reservations.entrySet().stream()
                                        .map(entry -> entry.getKey().getLibelle() + " x" + entry.getValue())
                                        .reduce((a, b) -> a + ", " + b)
                                        .orElse("Matériel");
                                
                                boolean emailSent = emailService.sendReservationConfirmation(
                                        userEmail,
                                        userName,
                                        materielDesc,
                                        reservations.values().stream().mapToInt(Integer::intValue).sum(),
                                        dateDebutPicker.getValue().toString(),
                                        dateFinPicker.getValue().toString(),
                                        totalAmount,
                                        true
                                );
                                
                                if (emailSent) {
                                    System.out.println("Email de confirmation envoyé à " + userEmail);
                                } else {
                                    System.err.println("Échec de l'envoi de l'email à " + userEmail);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                            }
                            
                            ((Stage) confirmerButton.getScene().getWindow()).close();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                      "Une erreur s'est produite lors du traitement du paiement: " + e.getMessage());
        }
    }

    /**
     * Une classe utilitaire pour afficher une boîte de dialogue de progression
     */
    private class ProgressDialog {
        private final Stage dialogStage;
        private final ProgressIndicator progressIndicator;
        
        public ProgressDialog(String title) {
            dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(title);
            dialogStage.setResizable(false);
            
            progressIndicator = new ProgressIndicator();
            progressIndicator.setProgress(-1); // Indéterminé
            
            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(20));
            vbox.getChildren().add(progressIndicator);
            
            Scene scene = new Scene(vbox, 200, 150);
            dialogStage.setScene(scene);
        }
        
        public void activateProgress() {
            dialogStage.show();
        }
        
        public void closeProgress() {
            dialogStage.close();
        }
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

    private void processReservations(boolean isPaid) {
        boolean allSuccess = true;
        String materielName = "";
        int totalQuantity = 0;
        double totalPrice = 0;
        
        for (Map.Entry<Materiel, Integer> entry : reservations.entrySet()) {
            Materiel materiel = entry.getKey();
            int quantity = entry.getValue();
            
            // Garder les informations pour l'email
            materielName = materiel.getLibelle();
            totalQuantity += quantity;
            totalPrice += materiel.getPrix() * quantity;

            try {
                // Conversion de LocalDate vers java.util.Date pour correspondre au constructeur de Reservation
                java.util.Date dateDebutUtil = java.util.Date.from(dateDebutPicker.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                java.util.Date dateFinUtil = java.util.Date.from(dateFinPicker.getValue().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant());
                
                // Log pour débogage
                System.out.println("Date début (LocalDate): " + dateDebutPicker.getValue());
                System.out.println("Date fin (LocalDate): " + dateFinPicker.getValue());
                System.out.println("Date début (java.util.Date): " + dateDebutUtil);
                System.out.println("Date fin (java.util.Date): " + dateFinUtil);

                Reservation reservation = new Reservation(
                        materiel.getId(),
                        quantity,
                        materiel.getPrix() * quantity,
                        dateDebutUtil,
                        dateFinUtil,
                        currentUserId
                );

                ReservationService reservationService = new ReservationService();
                reservationService.ajouter(reservation);

            } catch (Exception e) {
                allSuccess = false;
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la réservation: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        
        // Si toutes les réservations ont été ajoutées avec succès, envoyer un email de confirmation
        if (allSuccess) {
            try {
                // Récupérer l'email et le nom de l'utilisateur
                UserService userService = new UserService();
                String[] userInfo = userService.getUserEmailAndName(currentUserId);
                
                if (userInfo != null) {
                    String userEmail = userInfo[0];
                    String userName = userInfo[1];
                    
                    // Formater les dates pour l'email
                    String dateDebutStr = dateDebutPicker.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String dateFinStr = dateFinPicker.getValue().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    
                    // Envoyer l'email de confirmation
                    EmailService emailService = new EmailService();
                    boolean emailSent = emailService.sendReservationConfirmation(
                            userEmail, 
                            userName, 
                            materielName, 
                            totalQuantity, 
                            dateDebutStr, 
                            dateFinStr, 
                            totalPrice, 
                            isPaid
                    );
                    
                    if (emailSent) {
                        System.out.println("Email de confirmation envoyé à " + userEmail);
                    } else {
                        System.err.println("Échec de l'envoi de l'email à " + userEmail);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 