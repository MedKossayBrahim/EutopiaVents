package com.esprit.controllers;

import com.esprit.models.reservation1;
import com.esprit.services.ReservationServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class Reservation1 implements Initializable {

    @FXML private ComboBox<String> lieuComboBox;
    @FXML private ComboBox<String> eventComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> heureDebutCombo;
    @FXML private ComboBox<String> heureFinCombo;
    @FXML private Label lieuCapaciteLabel;
    @FXML private Label eventDetailsLabel;
    @FXML private TableView<reservation1> reservationsTable;
    @FXML private TableColumn<reservation1, String> lieuColumn;
    @FXML private TableColumn<reservation1, String> eventColumn;
    @FXML private TableColumn<reservation1, LocalDateTime> dateDebutColumn;
    @FXML private TableColumn<reservation1, LocalDateTime> dateFinColumn;

    private ReservationServiceImpl reservationService;
    private Map<String, Integer> lieuxMap = new HashMap<>();
    private Map<String, Integer> eventsMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            reservationService = new ReservationServiceImpl();
            initializeTimeComboBoxes();
            loadLieuxAndEvents();
            setupTableColumns();
            loadReservations();
            setupListeners();
        } catch (RuntimeException e) {
            System.err.println("Erreur d'initialisation : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur d'initialisation : " + e.getMessage());
        }
    }

    private void initializeTimeComboBoxes() {
        ObservableList<String> heures = FXCollections.observableArrayList();
        IntStream.rangeClosed(0, 23).forEach(hour -> {
            String hourStr = String.format("%02d:00", hour);
            heures.add(hourStr);
        });
        heureDebutCombo.setItems(heures);
        heureFinCombo.setItems(heures);
    }

    private void loadLieuxAndEvents() {
        try {
            // Chargement des lieux
            ObservableList<String> lieux = FXCollections.observableArrayList();
            for (Map<String, Object> lieu : reservationService.getAllLieux()) {
                String nomLieu = (String) lieu.get("nom");
                Integer idLieu = ((Number) lieu.get("id")).intValue();
                lieux.add(nomLieu);
                lieuxMap.put(nomLieu, idLieu);
            }
            lieuComboBox.setItems(lieux);

            // Chargement des événements
            ObservableList<String> events = FXCollections.observableArrayList();
            for (Map<String, Object> event : reservationService.getAllEvenements()) {
                String nomEvent = (String) event.get("titre");
                Integer idEvent = ((Number) event.get("id")).intValue();
                events.add(nomEvent);
                eventsMap.put(nomEvent, idEvent);
            }
            eventComboBox.setItems(events);
        } catch (RuntimeException e) {
            System.err.println("Erreur lors du chargement des lieux et événements : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des lieux et événements : " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        try {
            lieuColumn.setCellValueFactory(cellData -> {
                int idLieu = cellData.getValue().getIdLieu();
                String nomLieu = reservationService.getNomLieuById(idLieu);
                return new SimpleStringProperty(nomLieu);
            });
            eventColumn.setCellValueFactory(cellData -> {
                int idEvenement = cellData.getValue().getIdEvenement();
                String titreEvent = reservationService.getTitreEventById(idEvenement);
                return new SimpleStringProperty(titreEvent);
            });
            dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

            // Formateur pour les colonnes de date
            dateDebutColumn.setCellFactory(column -> new TableCell<reservation1, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            });

            dateFinColumn.setCellFactory(column -> new TableCell<reservation1, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des colonnes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadReservations() {
        try {
            ObservableList<reservation1> reservations = FXCollections.observableArrayList(
                    reservationService.rechercher()
            );
            reservationsTable.setItems(reservations);
        } catch (RuntimeException e) {
            System.err.println("Erreur lors du chargement des réservations : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            if (validateInputs()) {
                reservation1 newReservation = createReservationFromInputs();
                reservationService.ajouter(newReservation);
                loadReservations();
                clearInputs();
                showSuccessAlert("Succès", "La réservation a été ajoutée avec succès.");
            }
        } catch (RuntimeException e) {
            showDetailedAlert("Erreur", "Erreur lors de l'ajout de la réservation", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        try {
            reservation1 selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null && validateInputs()) {
                reservation1 updatedReservation = createReservationFromInputs();
                updatedReservation.setId(selectedReservation.getId());
                reservationService.modifier(updatedReservation);
                loadReservations();
                clearInputs();
                showSuccessAlert("Succès", "La réservation a été modifiée avec succès.");
            }
        } catch (RuntimeException e) {
            showDetailedAlert("Erreur", "Erreur lors de la modification de la réservation", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        try {
            reservation1 selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Supprimer la réservation");
                confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    reservationService.supprimer(selectedReservation);
                    loadReservations();
                    clearInputs();
                    showSuccessAlert("Succès", "La réservation a été supprimée avec succès.");
                }
            }
        } catch (RuntimeException e) {
            showDetailedAlert("Erreur", "Erreur lors de la suppression de la réservation", e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        clearInputs();
    }

    private boolean validateInputs() {
        if (lieuComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un lieu.");
            return false;
        }
        if (eventComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un événement.");
            return false;
        }
        if (dateDebutPicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de début.");
            return false;
        }
        if (dateFinPicker.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de fin.");
            return false;
        }
        if (heureDebutCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une heure de début.");
            return false;
        }
        if (heureFinCombo.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une heure de fin.");
            return false;
        }
        return true;
    }

    private reservation1 createReservationFromInputs() {
        LocalDateTime dateDebut = LocalDateTime.of(
                dateDebutPicker.getValue(),
                LocalTime.parse(heureDebutCombo.getValue() + ":00")
        );
        LocalDateTime dateFin = LocalDateTime.of(
                dateFinPicker.getValue(),
                LocalTime.parse(heureFinCombo.getValue() + ":00")
        );

        return new reservation1(
                0,
                lieuxMap.get(lieuComboBox.getValue()),
                eventsMap.get(eventComboBox.getValue()),
                dateDebut,
                dateFin
        );
    }

    private void clearInputs() {
        lieuComboBox.setValue(null);
        eventComboBox.setValue(null);
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        heureDebutCombo.setValue(null);
        heureFinCombo.setValue(null);
        lieuCapaciteLabel.setVisible(false);
        eventDetailsLabel.setVisible(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Create expandable Exception.
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showDetailedAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable Exception.
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }


    private void setupListeners() {
        lieuComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    for (Map<String, Object> lieu : reservationService.getAllLieux()) {
                        if (lieu.get("nom").equals(newVal)) {
                            Number capaciteNum = (Number) lieu.get("capacite");
                            int capacite = capaciteNum.intValue();
                            lieuCapaciteLabel.setText("Capacité: " + capacite + " personnes");
                            lieuCapaciteLabel.setVisible(true);

                            eventComboBox.getItems().clear();
                            for (Map<String, Object> event : reservationService.getAllEvenements()) {
                                Number eventCapaciteNum = (Number) event.get("capacite");
                                int eventCapacite = eventCapaciteNum.intValue();
                                if (eventCapacite <= capacite) {
                                    eventComboBox.getItems().add((String) event.get("titre"));
                                }
                            }
                            break;
                        }
                    }
                } catch (RuntimeException e) {
                    System.err.println("Erreur lors de la mise à jour des informations du lieu : " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur lors de la mise à jour des informations du lieu : " + e.getMessage());
                }
            } else {
                lieuCapaciteLabel.setVisible(false);
            }
        });

        eventComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    for (Map<String, Object> event : reservationService.getAllEvenements()) {
                        if (event.get("titre").equals(newVal)) {
                            LocalDateTime dateDebut = (LocalDateTime) event.get("date_debut");
                            LocalDateTime dateFin = (LocalDateTime) event.get("date_fin");
                            Number capaciteNum = (Number) event.get("capacite");
                            int capacite = capaciteNum.intValue();

                            String details = String.format("Du %s au %s - %d participants",
                                    dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                    dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                    capacite);
                            eventDetailsLabel.setText(details);
                            eventDetailsLabel.setVisible(true);

                            dateDebutPicker.setValue(dateDebut.toLocalDate());
                            dateFinPicker.setValue(dateFin.toLocalDate());
                            heureDebutCombo.setValue(String.format("%02d:00", dateDebut.getHour()));
                            heureFinCombo.setValue(String.format("%02d:00", dateFin.getHour()));

                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la mise à jour des détails de l'événement : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                eventDetailsLabel.setVisible(false);
            }
        });

        setupDateTimeValidation();
        setupTableSelection();
    }

    private void setupDateTimeValidation() {
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateFinPicker.getValue() != null) {
                validateDates();
            }
        });

        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateDebutPicker.getValue() != null) {
                validateDates();
            }
        });

        heureDebutCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && heureFinCombo.getValue() != null) {
                validateHours();
            }
        });

        heureFinCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && heureDebutCombo.getValue() != null) {
                validateHours();
            }
        });
    }

    private void setupTableSelection() {
        reservationsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        try {
                            for (Map<String, Object> lieu : reservationService.getAllLieux()) {
                                if (((Number)lieu.get("id")).intValue() == newSelection.getIdLieu()) {
                                    lieuComboBox.setValue((String)lieu.get("nom"));
                                    break;
                                }
                            }

                            for (Map<String, Object> event : reservationService.getAllEvenements()) {
                                if (((Number)event.get("id")).intValue() == newSelection.getIdEvenement()) {
                                    eventComboBox.setValue((String)event.get("titre"));
                                    break;
                                }
                            }

                            dateDebutPicker.setValue(newSelection.getDateDebut().toLocalDate());
                            dateFinPicker.setValue(newSelection.getDateFin().toLocalDate());
                            heureDebutCombo.setValue(String.format("%02d:00", newSelection.getDateDebut().getHour()));
                            heureFinCombo.setValue(String.format("%02d:00", newSelection.getDateFin().getHour()));
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la sélection dans la table : " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void validateDates() {
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début doit être antérieure à la date de fin");
            dateFinPicker.setValue(dateDebut);
        }
    }

    private void validateHours() {
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null &&
                heureDebutCombo.getValue() != null && heureFinCombo.getValue() != null) {

            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            LocalTime heureDebut = LocalTime.parse(heureDebutCombo.getValue() + ":00");
            LocalTime heureFin = LocalTime.parse(heureFinCombo.getValue() + ":00");

            if (dateDebut.equals(dateFin) && heureDebut.isAfter(heureFin)) {
                showAlert("Erreur", "L'heure de début doit être antérieure à l'heure de fin");
                heureFinCombo.setValue(heureDebutCombo.getValue());
            }
        }
    }
    @FXML
    private void goToPhotoView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/photoView.fxml"));
            lieuComboBox.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}