package com.esprit.controllers;

import com.esprit.models.Reservation;
import com.esprit.services.ReservationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
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

    public ListeReservationController() throws SQLException {
        reservationService = new ReservationService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadReservations();
    }

    private void setupColumns() {
        // Configuration des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        evenementIdColumn.setCellValueFactory(new PropertyValueFactory<>("evenementId"));
        materielIdColumn.setCellValueFactory(new PropertyValueFactory<>("materielId"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        // Rendre les colonnes éditables (sauf les colonnes de date)
        evenementIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        materielIdColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        prixTotalColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));

        // Gérer les modifications (sauf les colonnes de date)
        evenementIdColumn.setOnEditCommit(event -> {
            Reservation reservation = event.getRowValue();
            reservation.setEvenementId(event.getNewValue());
            reservationService.modifier(reservation);
        });

        materielIdColumn.setOnEditCommit(event -> {
            Reservation reservation = event.getRowValue();
            reservation.setMaterielId(event.getNewValue());
            reservationService.modifier(reservation);
        });

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

        // Configuration de la colonne des actions (supprimer uniquement)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5); // 5 est l'espacement entre les boutons

            {
                buttonsBox.getChildren().addAll(deleteBtn);

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

        // Activer l'édition sur double-clic (sauf les colonnes de date)
        reservationTable.setEditable(true);
        reservationTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !reservationTable.getSelectionModel().isEmpty()) {
                TablePosition<Reservation, ?> pos = reservationTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<Reservation, ?> col = pos.getTableColumn();
                if (col == evenementIdColumn || col == materielIdColumn || col == quantiteColumn || col == prixTotalColumn) {
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