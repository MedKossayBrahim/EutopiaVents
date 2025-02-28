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
import javafx.util.Callback;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.Optional;

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

    private static final String CLIENT_ID = "AYXW7pyvL5zqRF0h2G6lJ9n4MDw-upNnBXta3mBUJN9Deitr_khAft_jU1SRxfINFhB9N5NIqSNm9RL3";
    private static final String CLIENT_SECRET = "EH_nYmrLLJgdUJSpmXff1lD0KDSvpYD_bTIokX5k0RTiTMeiFjEXdSf76jFWidTCqj4P2RznXAzKuTaX";
    private static final String MODE = "sandbox"; // Utiliser "live" pour la production

    public ReservationWindowController(int userId) {
        this.currentUserId = userId;
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

        // Demander à l'utilisateur s'il veut payer par PayPal ou non
        Alert paymentChoice = new Alert(Alert.AlertType.CONFIRMATION);
        paymentChoice.setTitle("Choix du paiement");
        paymentChoice.setHeaderText("Total à payer : " + String.format("%.2f", totalAmount) + " EUR");
        paymentChoice.setContentText("Voulez-vous payer maintenant via PayPal ?");

        ButtonType paypalButton = new ButtonType("Payer par PayPal");
        ButtonType laterButton = new ButtonType("Payer plus tard");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        paymentChoice.getButtonTypes().setAll(paypalButton, laterButton, cancelButton);

        Optional<ButtonType> result = paymentChoice.showAndWait();
        if (result.isPresent()) {
            if (result.get() == paypalButton) {
                // Paiement PayPal
                try {
                    Payment payment = createPayPalPayment(totalAmount);
                    if (payment.getState().equals("approved")) {
                        processReservations();
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Paiement effectué et réservation confirmée avec succès!");
                        ((Stage) confirmerButton.getScene().getWindow()).close();
                    }
                } catch (PayPalRESTException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de paiement",
                        "Le paiement n'a pas pu être effectué: " + e.getMessage());
                }
            } else if (result.get() == laterButton) {
                // Créer la réservation sans paiement
                processReservations();
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Réservation enregistrée avec succès! Le paiement devra être effectué ultérieurement.");
                ((Stage) confirmerButton.getScene().getWindow()).close();
            } else {
                // L'utilisateur a annulé
                return;
            }
        }
    }

    private Payment createPayPalPayment(double totalAmount) throws PayPalRESTException {
        APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);

        // Créer les détails du paiement
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setTotal(String.format(java.util.Locale.US, "%.2f", totalAmount));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Reservation de materiel");

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        // Configuration du paiement
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        // Redirection URLs
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("http://localhost:8080/cancel");
        redirectUrls.setReturnUrl("http://localhost:8080/success");
        payment.setRedirectUrls(redirectUrls);

        // Créer le paiement et obtenir l'approbation
        Payment createdPayment = payment.create(apiContext);

        // Rediriger vers PayPal
        String approvalUrl = getApprovalUrl(createdPayment);
        if (approvalUrl != null) {
            openPayPalInBrowser(approvalUrl);
            return waitForPaymentApproval(createdPayment.getId(), apiContext);
        }
        throw new PayPalRESTException("Impossible d'obtenir l'URL d'approbation");
    }

    private String getApprovalUrl(Payment payment) {
        List<Links> links = payment.getLinks();
        for (Links link : links) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                return link.getHref();
            }
        }
        return null;
    }

    private void openPayPalInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                "Impossible d'ouvrir le navigateur. Veuillez copier ce lien : " + url);
        }
    }

    private Payment waitForPaymentApproval(String paymentId, APIContext apiContext)
            throws PayPalRESTException {
        // Attendre la confirmation du paiement
        int maxAttempts = 60; // 1 minute d'attente maximum
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde
                Payment payment = Payment.get(apiContext, paymentId);
                if (payment.getState().equals("approved")) {
                    return payment;
                }
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PayPalRESTException("Interruption pendant l'attente du paiement");
            }
        }
        throw new PayPalRESTException("Délai d'attente dépassé pour le paiement");
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

    private void processReservations() {
        for (Map.Entry<Materiel, Integer> entry : reservations.entrySet()) {
            Materiel materiel = entry.getKey();
            int quantity = entry.getValue();

            try {
                java.sql.Date dateDebutSql = java.sql.Date.valueOf(dateDebutPicker.getValue());
                java.sql.Date dateFinSql = java.sql.Date.valueOf(dateFinPicker.getValue());

                Reservation reservation = new Reservation(
                    materiel.getId(),
                    quantity,
                    materiel.getPrix() * quantity,
                    dateDebutSql,
                    dateFinSql,
                    currentUserId
                );

                ReservationService reservationService = new ReservationService();
                reservationService.ajouter(reservation);
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                         "Une erreur est survenue lors de la réservation: " + e.getMessage());
                e.printStackTrace();
                return;
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