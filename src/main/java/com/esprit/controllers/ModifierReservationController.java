package com.esprit.controllers;

import com.esprit.models.Reservation;
import com.esprit.services.ReservationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifierReservationController {
    @FXML
    private TextField evenementIdField;
    @FXML
    private TextField materielIdField;
    @FXML
    private TextField quantiteField;
    @FXML
    private Label prixTotalLabel;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;

    private final ReservationService reservationService;
    private Reservation currentReservation;

    public ModifierReservationController() throws SQLException {
        reservationService = new ReservationService();
    }

    public void setReservation(Reservation reservation) {
        this.currentReservation = reservation;
        displayReservation();
    }

    private void displayReservation() {
        if (currentReservation != null) {
            evenementIdField.setText(String.valueOf(currentReservation.getEvenementId()));
            materielIdField.setText(String.valueOf(currentReservation.getMaterielId()));
            quantiteField.setText(String.valueOf(currentReservation.getQuantite()));
            prixTotalLabel.setText(String.format("%.2f", currentReservation.getPrixTotal()));
            
            // Conversion des Date en LocalDate pour les DatePicker
            if (currentReservation.getDateDebut() != null) {
                dateDebutPicker.setValue(currentReservation.getDateDebut().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
            if (currentReservation.getDateFin() != null) {
                dateFinPicker.setValue(currentReservation.getDateFin().toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            }
        }
    }

    @FXML
    private void handleSauvegarder() {
        try {
            currentReservation.setQuantite(Integer.parseInt(quantiteField.getText()));
            
            // Mise à jour de la réservation
            reservationService.modifier(currentReservation);
            
            // Fermer la fenêtre après la sauvegarde
            closeWindow();
            
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) quantiteField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 