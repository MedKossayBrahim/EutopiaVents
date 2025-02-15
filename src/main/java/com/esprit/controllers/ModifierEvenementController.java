package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.List;

public class ModifierEvenementController {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> descriptionColumn;
    @FXML private TableColumn<Evenement, String> dateDebutColumn;
    @FXML private TableColumn<Evenement, String> dateFinColumn;
    @FXML private TableColumn<Evenement, Integer> capaciteColumn;
    @FXML private TableColumn<Evenement, Double> prixColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

    private EvenementService evenementService = new EvenementService();
    private ObservableList<Evenement> evenementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadEvents();
    }

    private void setupTable() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Rendre les colonnes éditables
        titreColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        dateDebutColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        dateFinColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        capaciteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        prixColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        // Gérer les modifications
        titreColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setTitre(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        descriptionColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDescription(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        dateDebutColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDateDebut(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        dateFinColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDateFin(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        capaciteColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setCapacite(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        prixColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setPrix(event.getNewValue());
            evenementService.modifier(selectedEvent);
            loadEvents();
        });

        // Ajouter le bouton Modifier dans chaque ligne
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");

            {
                modifyBtn.setOnAction(e -> {
                    Evenement selectedEvent = getTableView().getItems().get(getIndex());
                    // Logique pour confirmer les modifications si nécessaire
                    evenementService.modifier(selectedEvent);
                    loadEvents(); // Rafraîchir la table
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyBtn);
            }
        });
    }

    private void loadEvents() {
        List<Evenement> events = evenementService.rechercher();
        evenementList.setAll(events);
        eventTable.setItems(evenementList);
    }

    @FXML
    private void handleRetour() {
        // Logique pour retourner à la page précédente
    }
}
