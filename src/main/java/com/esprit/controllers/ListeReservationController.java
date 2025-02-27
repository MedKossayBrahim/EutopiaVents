package com.esprit.controllers;

import com.esprit.models.Reservation;
import com.esprit.services.ReservationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;

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
    private TableColumn<Reservation, Date> dateDebutColumn;
    @FXML
    private TableColumn<Reservation, Date> dateFinColumn;
    @FXML
    private TableColumn<Reservation, Void> actionsColumn;
    @FXML
    private TextField filterEventField; // Champ de filtre par événement
    @FXML
    private TextField filterMaterialField; // Champ de filtre par matériel
    @FXML
    private TableColumn<Reservation, String> userIdColumn;

    private final ReservationService reservationService;
    private ObservableList<Reservation> reservationsList; // Liste observable des réservations
    private FilteredList<Reservation> filteredReservations; // Liste filtrée

    public ListeReservationController() throws SQLException {
        reservationService = new ReservationService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadReservations();

        // Configurer les filtres
        setupFilters();
    }

    private void setupColumns() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        evenementIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(reservationService.getEventName(cellData.getValue().getEvenementId()))
        );

        userIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(reservationService.getUserName(cellData.getValue().getUserId()))
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

        dateDebutColumn.setCellFactory(column -> new TableCell<Reservation, Date>() {
            @Override
            public void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });

        dateFinColumn.setCellFactory(column -> new TableCell<Reservation, Date>() {
            @Override
            public void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
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
        reservationsList = FXCollections.observableArrayList(reservationService.rechercher());
        filteredReservations = new FilteredList<>(reservationsList, p -> true); // Initialiser la liste filtrée
        reservationTable.setItems(filteredReservations); // Lier la liste filtrée à la TableView
    }

    private void setupFilters() {
        // Ajouter un écouteur sur le champ de filtre par événement
        filterEventField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredReservations.setPredicate(reservation -> {
                // Si le champ de filtre par événement est vide, ignorer ce filtre
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convertir le texte saisi et le nom de l'événement en minuscules pour une recherche insensible à la casse
                String lowerCaseFilter = newValue.toLowerCase();
                String eventName = reservationService.getEventName(reservation.getEvenementId()).toLowerCase();

                // Vérifier si le nom de l'événement contient le texte saisi
                return eventName.contains(lowerCaseFilter);
            });
        });

        // Ajouter un écouteur sur le champ de filtre par matériel
        filterMaterialField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredReservations.setPredicate(reservation -> {
                // Si le champ de filtre par matériel est vide, ignorer ce filtre
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convertir le texte saisi et le nom du matériel en minuscules pour une recherche insensible à la casse
                String lowerCaseFilter = newValue.toLowerCase();
                String materialName = reservationService.getMaterialName(reservation.getMaterielId()).toLowerCase();

                // Vérifier si le nom du matériel contient le texte saisi
                return materialName.contains(lowerCaseFilter);
            });
        });
    }

}