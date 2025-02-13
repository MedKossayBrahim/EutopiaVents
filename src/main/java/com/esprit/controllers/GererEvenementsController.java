package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GererEvenementsController implements Initializable {

    @FXML private TableView<Evenement> eventTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> descriptionColumn;
    @FXML private TableColumn<Evenement, String> dateDebutColumn;
    @FXML private TableColumn<Evenement, String> dateFinColumn;
    @FXML private TableColumn<Evenement, Integer> capaciteColumn;
    @FXML private TableColumn<Evenement, String> categorieIdColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> organisateurColumn;
    @FXML private TableColumn<Evenement, Double> prixColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

    private final EvenementService evenementService = new EvenementService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadEvents();
    }

    private void setupColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        categorieIdColumn.setCellValueFactory(new PropertyValueFactory<>("categorieNom"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        organisateurColumn.setCellValueFactory(new PropertyValueFactory<>("organisateurNom"));
        
        lieuColumn.setCellValueFactory(cellData -> {
            Evenement event = cellData.getValue();
            String lieuInfo = event.getLieuNom() != null ? 
                            event.getLieuNom() : 
                            event.getLieu_proprietaire();
            return new SimpleStringProperty(lieuInfo);
        });

        actionsColumn.setCellFactory(column -> {
            return new TableCell<>() {
                private final Button acceptButton = new Button("Accepter");
                private final Button refuserButton = new Button("Refuser");
                private final HBox buttonBox = new HBox(5, acceptButton, refuserButton);

                {
                    acceptButton.setOnAction(event -> {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        handleAccept(evenement);
                    });

                    refuserButton.setOnAction(event -> {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        handleRefuse(evenement);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Evenement evenement = getTableView().getItems().get(getIndex());
                        if ("en attente".equals(evenement.getStatut())) {
                            setGraphic(buttonBox);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
        });
    }

    private void loadEvents() {
        List<Evenement> events = evenementService.rechercher();
        eventTable.setItems(FXCollections.observableArrayList(events));
    }

    private void handleAccept(Evenement evenement) {
        evenement.setStatut("acceptée");
        evenementService.modifier(evenement);
        loadEvents();
    }

    private void handleRefuse(Evenement evenement) {
        evenement.setStatut("refusée");
        evenementService.modifier(evenement);
        loadEvents();
    }

    public void refreshTable() {
        loadEvents();
    }
} 