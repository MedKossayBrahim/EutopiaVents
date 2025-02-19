package com.esprit.controllers;

import com.esprit.models.Reservation;
import com.esprit.services.ReservationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class ListeReservationController {
    @FXML
    private TableView<Reservation> reservationTable;
    @FXML
    private TableColumn<Reservation, String> evenementIdColumn;
    @FXML
    private TableColumn<Reservation, String> materielIdColumn;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


        evenementIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(reservationService.getEventName(cellData.getValue().getEvenementId()))
        );

        materielIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(reservationService.getMaterialName(cellData.getValue().getMaterielId()))
        );

        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        prixTotalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        quantiteColumn.setOnEditCommit(event -> {
            Reservation reservation = event.getRowValue();
            reservation.setQuantite(event.getNewValue());
            reservationService.modifier(reservation);
        });

        prixTotalColumn.setOnEditCommit(event -> {
            Reservation reservation = event.getRowValue();
            reservation.setPrixTotal(event.getNewValue());
            reservationService.modifier(reservation);
        });

        dateDebutColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDateTime().format(formatter));
                }
            }
        });

        dateFinColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDateTime().format(formatter));
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5);

            {
                buttonsBox.getChildren().add(deleteBtn);
                deleteBtn.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    reservationService.supprimer(reservation);
                    loadReservations();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });

        reservationTable.setEditable(true);
        reservationTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !reservationTable.getSelectionModel().isEmpty()) {
                TablePosition<Reservation, ?> pos = reservationTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<Reservation, ?> col = pos.getTableColumn();
                if (col == quantiteColumn || col == prixTotalColumn) {
                    reservationTable.edit(row, col);
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
