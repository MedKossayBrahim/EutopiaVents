package com.esprit.controllers;

import com.esprit.models.Evenement;
import com.esprit.services.EvenementService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.ScrollPane;

import java.sql.SQLException;
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
    @FXML private ScrollPane scrollPane;

    private EvenementService evenementService = new EvenementService();
    private ObservableList<Evenement> evenementList = FXCollections.observableArrayList();

    public ModifierEvenementController() throws SQLException {
    }

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

        // Gérer les modifications sans appliquer immédiatement
        titreColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setTitre(event.getNewValue());
        });

        descriptionColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDescription(event.getNewValue());
        });

        dateDebutColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDateDebut(event.getNewValue());
        });

        dateFinColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setDateFin(event.getNewValue());
        });

        capaciteColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setCapacite(event.getNewValue());
        });

        prixColumn.setOnEditCommit(event -> {
            Evenement selectedEvent = event.getRowValue();
            selectedEvent.setPrix(event.getNewValue());
        });

        // Ajouter le bouton Modifier dans chaque ligne
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");

            {
                modifyBtn.setOnAction(e -> {
                    Evenement selectedEvent = getTableView().getItems().get(getIndex());
                    // Appliquer les modifications
                    modifierEvenement(selectedEvent);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyBtn);
            }
        });
    }

    private void modifierEvenement(Evenement evenement) {
        try {
            evenementService.modifier(evenement);
            System.out.println("Événement modifié avec succès : " + evenement);
            loadEvents(); // Rafraîchir la table pour afficher les modifications
        } catch (Exception e) {
            System.err.println("Erreur lors de la modification de l'événement : " + e.getMessage());
        }
    }

    private void loadEvents() {
        List<Evenement> events = evenementService.rechercher();
        evenementList.setAll(events);
        eventTable.setItems(evenementList);
    }

    @FXML
    private void handleRetour() {
        loadPage("/events-view.fxml"); // Retour à la page des événements
    }

    private void loadPage(String page) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(page));
            Scene scene = scrollPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCategEvent.fxml");
    }

    @FXML
    private void goToAjouterEvenement() {
        loadPage("/AjouterEvenement.fxml");
    }

    @FXML
    private void goToModifierEvenement() {
        loadPage("/ModifierEvenement.fxml");
    }

    @FXML
    private void goToEventsView() {
        loadPage("/events-view.fxml");
    }

    @FXML
    private void goToGererEvenements() {
        loadPage("/GererEvenements.fxml");
    }
}
