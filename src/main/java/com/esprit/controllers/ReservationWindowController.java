package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.services.MaterielService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

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
    private ObservableList<Materiel> materiels;
    private FilteredList<Materiel> filteredMateriels;

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

        List<Materiel> selectedMateriels = new ArrayList<>();
        Map<Materiel, Integer> reservations = new HashMap<>();

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

        // Ajouter le prix total dans le message de confirmation
        StringBuilder details = new StringBuilder("Articles sélectionnés:\n");
        details.append("Du: ").append(dateDebut).append("\n");
        details.append("Au: ").append(dateFin).append("\n");
        details.append("Prix Total: ").append(prixTotalLabel.getText()).append("\n\n");
        
        reservations.forEach((materiel, quantity) -> 
            details.append("- ").append(materiel.getLibelle())
                   .append(" (Quantité: ").append(quantity)
                   .append(", Prix unitaire: ").append(materiel.getPrix()).append(" TND)\n"));

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de réservation");
        confirmation.setHeaderText("Voulez-vous confirmer cette réservation ?");
        confirmation.setContentText(details.toString());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Passer les dates à la méthode de création de réservation
                // reservationService.creerReservation(materiel, quantity, dateDebut, dateFin);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                         "Votre réservation a été effectuée avec succès!");
                ((Stage) confirmerButton.getScene().getWindow()).close();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 