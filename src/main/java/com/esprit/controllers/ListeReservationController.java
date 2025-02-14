package com.esprit.controllers;

import com.esprit.models.Reservation;
import com.esprit.services.ReservationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Timestamp;

public class ListeReservationController {
    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, Integer> idColumn;
    @FXML
    private TableColumn<Reservation, Integer> evenementIdColumn;
    @FXML
    private TableColumn<Reservation, Integer> materielIdColumn;
    @FXML
    private TableColumn<Reservation, Integer> quantiteColumn;
    @FXML
    private TableColumn<Reservation, Double> prixTotalColumn;
    @FXML
    private TableColumn<Reservation, Timestamp> dateDebutColumn;
    @FXML
    private TableColumn<Reservation, Timestamp> dateFinColumn;
    @FXML
    private TableColumn<Reservation, Void> actionsColumn;

    private final ReservationService reservationService;

    public ListeReservationController() {
        reservationService = new ReservationService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadReservations();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        evenementIdColumn.setCellValueFactory(new PropertyValueFactory<>("evenementId"));
        materielIdColumn.setCellValueFactory(new PropertyValueFactory<>("materielId"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");

            {
                modifyBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    ouvrirModification(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(modifyBtn);
                }
            }
        });
    }

    private void loadReservations() {
        reservationTable.getItems().clear();
        reservationTable.getItems().addAll(reservationService.rechercher());
    }

    private void ouvrirModification(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierReservation.fxml"));
            Parent root = loader.load();
            
            ModifierReservationController controller = loader.getController();
            controller.setReservation(reservation);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier Réservation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
    }
} 