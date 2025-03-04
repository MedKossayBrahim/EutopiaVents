package com.esprit.controllers;

import com.esprit.models.Lieu;
import com.esprit.models.Role;
import com.esprit.models.User;
import com.esprit.models.reservation1;
import com.esprit.services.LieuServiceImpl;
import com.esprit.services.ReservationServiceImpl;
import com.esprit.utils.UserSession;
import com.esprit.tests.Eutopia;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class Reservation1 implements Initializable {

    @FXML private ComboBox<String> lieuComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> heureDebutCombo;
    @FXML private ComboBox<String> heureFinCombo;
    @FXML private Label lieuCapaciteLabel;
    @FXML private TableView<reservation1> reservationsTable;
    @FXML private TableColumn<reservation1, String> lieuColumn;
    @FXML private TableColumn<reservation1, LocalDateTime> dateDebutColumn;
    @FXML private TableColumn<reservation1, LocalDateTime> dateFinColumn;
    @FXML private TableColumn<reservation1, String> typeReservationColumn;
    @FXML private TableColumn<reservation1, String> prixColumn;
    @FXML private Label userInfoLabel;
    
    @FXML private Button ajouterBtn;
    @FXML private Button modifierBtn;
    @FXML private Button supprimerBtn;
    @FXML private Button annulerBtn;

    private ReservationServiceImpl reservationService;
    private LieuServiceImpl lieuService;
    private Map<String, Integer> lieuxMap = new HashMap<>();
    private User currentUser;

    /**
     * Configures the visibility and state of buttons based on the user's role
     */
    public void configureButtonsByRole() {
        try {
            // Get the current user using Eutopia.getCurrentUser()
            User user = Eutopia.getCurrentUser();
            if (user == null) {
                return;
            }
            
            // Get the role and configure buttons based on role
            Role userRole = user.getRole();
            
            // Check roles using equals method for proper enum comparison
            if (Role.Admin.equals(userRole)) {
                // Admins can do everything
                ajouterBtn.setDisable(false);
                modifierBtn.setDisable(false);
                supprimerBtn.setDisable(false);
            } else if (Role.Organisateur.equals(userRole)) {
                // Organisateurs can add and modify but not delete
                ajouterBtn.setDisable(false);
                modifierBtn.setDisable(false);
                supprimerBtn.setDisable(true);
            } else {
                // Regular users (Participants) can only add
                ajouterBtn.setDisable(false);
                modifierBtn.setDisable(true);
                supprimerBtn.setDisable(true);
            }
        } catch (Exception e) {
            System.err.println("Error configuring buttons by role: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Use Eutopia.getCurrentUser() to get the current user
            currentUser = Eutopia.getCurrentUser();
            if (currentUser == null) {
                showAlert("Erreur", "Vous devez être connecté pour accéder à cette fonctionnalité.");
                return;
            }

            if (userInfoLabel != null) {
                userInfoLabel.setText("Connecté en tant que: " + currentUser.getUserName());
            }

            reservationService = new ReservationServiceImpl();
            lieuService = new LieuServiceImpl();
            initializeTimeComboBoxes();
            loadLieux();
            setupTableColumns();
            loadUserReservations();
            setupListeners();
            configureButtonsByRole();
        } catch (RuntimeException | SQLException e) {
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

    private void loadLieux() {
        try {
            ObservableList<String> lieux = FXCollections.observableArrayList();
            for (Map<String, Object> lieu : reservationService.getAllLieux()) {
                String nomLieu = (String) lieu.get("nom");
                Integer idLieu = ((Number) lieu.get("id")).intValue();
                lieux.add(nomLieu);
                lieuxMap.put(nomLieu, idLieu);
            }
            lieuComboBox.setItems(lieux);
        } catch (RuntimeException e) {
            System.err.println("Erreur lors du chargement des lieux : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des lieux : " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        try {
            lieuColumn.setCellValueFactory(cellData -> {
                int idLieu = cellData.getValue().getIdLieu();
                String nomLieu = reservationService.getNomLieuById(idLieu);
                return new SimpleStringProperty(nomLieu);
            });
            dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
            typeReservationColumn.setCellValueFactory(new PropertyValueFactory<>("typeReservation"));
            prixColumn.setCellValueFactory(cellData -> {
                double prix = calculerPrixReservation(cellData.getValue());
                return new SimpleStringProperty(String.format("%.2f DT", prix));
            });

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateDebutColumn.setCellFactory(column -> new TableCell<reservation1, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(formatter));
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
                        setText(item.format(formatter));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des colonnes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserReservations() {
        try {
            if (currentUser != null) {
                List<reservation1> userReservations = reservationService.rechercherReservationsUtilisateur(currentUser.getUserID());
                ObservableList<reservation1> reservations = FXCollections.observableArrayList(userReservations);
                reservationsTable.setItems(reservations);
            } else {
                ObservableList<reservation1> reservations = FXCollections.observableArrayList(
                        reservationService.rechercher()
                );
                reservationsTable.setItems(reservations);
            }
        } catch (RuntimeException e) {
            System.err.println("Erreur lors du chargement des réservations : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            if (currentUser == null) {
                showAlert("Erreur", "Vous devez être connecté pour effectuer une réservation.");
                return;
            }

            if (validateInputs()) {
                reservation1 newReservation = createReservationFromInputs();
                newReservation.setUserID(currentUser.getUserID());

                try {
                    if (!reservationService.checkEventAvailability(
                            newReservation.getIdLieu(),
                            newReservation.getDateDebut(),
                            newReservation.getDateFin())) {
                        showAlert("Erreur", "Le lieu est déjà réservé pour un événement pendant cette période.");
                        return;
                    }

                    reservationService.ajouter(newReservation);
                    loadUserReservations();
                    clearInputs();
                    showSuccessAlert("Succès", "La réservation a été ajoutée avec succès.");
                } catch (RuntimeException e) {
                    showDetailedAlert("Erreur", "Erreur lors de l'ajout de la réservation", e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            showDetailedAlert("Erreur", "Erreur lors de l'ajout de la réservation", e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        try {
            if (currentUser == null) {
                showAlert("Erreur", "Vous devez être connecté pour modifier une réservation.");
                return;
            }

            reservation1 selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                if (selectedReservation.getUserID() != currentUser.getUserID()) {
                    showAlert("Erreur", "Vous ne pouvez modifier que vos propres réservations.");
                    return;
                }

                if (validateInputs()) {
                    reservation1 updatedReservation = createReservationFromInputs();
                    updatedReservation.setId(selectedReservation.getId());
                    updatedReservation.setUserID(currentUser.getUserID());

                    try {
                        if (!reservationService.checkEventAvailability(
                                updatedReservation.getIdLieu(),
                                updatedReservation.getDateDebut(),
                                updatedReservation.getDateFin())) {
                            showAlert("Erreur", "Le lieu est déjà réservé pour un événement pendant cette période.");
                            return;
                        }

                        reservationService.modifier(updatedReservation);
                        loadUserReservations();
                        clearInputs();
                        showSuccessAlert("Succès", "La réservation a été modifiée avec succès.");
                    } catch (RuntimeException e) {
                        showDetailedAlert("Erreur", "Erreur lors de la modification de la réservation", e.getMessage());
                    }
                }
            } else {
                showAlert("Erreur", "Veuillez sélectionner une réservation à modifier.");
            }
        } catch (RuntimeException e) {
            showDetailedAlert("Erreur", "Erreur lors de la modification de la réservation", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        try {
            if (currentUser == null) {
                showAlert("Erreur", "Vous devez être connecté pour supprimer une réservation.");
                return;
            }

            reservation1 selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                if (selectedReservation.getUserID() != currentUser.getUserID()) {
                    showAlert("Erreur", "Vous ne pouvez supprimer que vos propres réservations.");
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirmation");
                confirmAlert.setHeaderText("Supprimer la réservation");
                confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    reservationService.supprimer(selectedReservation);
                    loadUserReservations();
                    clearInputs();
                    showSuccessAlert("Succès", "La réservation a été supprimée avec succès.");
                }
            } else {
                showAlert("Erreur", "Veuillez sélectionner une réservation à supprimer.");
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

        reservation1 newReservation = new reservation1(
                0,
                lieuxMap.get(lieuComboBox.getValue()),
                0,  // idEvenement is always 0 for direct rentals
                dateDebut,
                dateFin
        );
        newReservation.setTypeReservation("location");
        if (currentUser != null) {
            newReservation.setUserID(currentUser.getUserID());
        }
        return newReservation;
    }

    private void clearInputs() {
        lieuComboBox.setValue(null);
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        heureDebutCombo.setValue(null);
        heureFinCombo.setValue(null);
        lieuCapaciteLabel.setVisible(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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

        setupDateTimeValidation();
        setupTableSelection();
        configureButtonsByRole();
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

    private double calculerPrixReservation(reservation1 reservation) {
        try {
            int lieuId = reservation.getIdLieu();
            Lieu lieu = lieuService.getLieuById(lieuId);
            if (lieu == null) return 0.0;

            double prixLieu = lieu.getPrix();
            if (prixLieu <= 0) return 0.0;

            LocalDateTime debut = reservation.getDateDebut();
            LocalDateTime fin = reservation.getDateFin();

            long jours = Math.max(1, ChronoUnit.DAYS.between(debut, fin));

            if (jours == 1 && debut.toLocalDate().equals(fin.toLocalDate())) {
                long heures = ChronoUnit.HOURS.between(debut, fin);
                if (heures < 24) {
                    return (prixLieu * heures) / 24.0;
                } else {
                    return prixLieu;
                }
            } else {
                return prixLieu * jours;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}