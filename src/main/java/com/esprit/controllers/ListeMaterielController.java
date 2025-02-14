package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.services.MaterielService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;

public class ListeMaterielController {
    @FXML
    private TableView<Materiel> materielTable;
    @FXML
    private TableColumn<Materiel, String> libelleColumn;
    @FXML
    private TableColumn<Materiel, String> descriptionColumn;
    @FXML
    private TableColumn<Materiel, Integer> quantiteColumn;
    @FXML
    private TableColumn<Materiel, Double> prixColumn;
    @FXML
    private TableColumn<Materiel, String> categorieColumn;
    @FXML
    private TableColumn<Materiel, String> imageUrlColumn;
    @FXML
    private TableColumn<Materiel, Void> actionsColumn;

    private final MaterielService materielService;

    public ListeMaterielController() {
        materielService = new MaterielService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadMateriels();
    }

    private void setupColumns() {
        // Configuration des colonnes
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorieId")); // Assurez-vous que "categorieId" est correct
        imageUrlColumn.setCellValueFactory(new PropertyValueFactory<>("Image_url"));

        // Rendre les colonnes éditables
        libelleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        quantiteColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        prixColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        imageUrlColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // Gérer les modifications
        libelleColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setLibelle(event.getNewValue());
            materielService.modifier(materiel);
        });

        descriptionColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setDescription(event.getNewValue());
            materielService.modifier(materiel);
        });

        quantiteColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setQuantite(event.getNewValue());
            materielService.modifier(materiel);
        });

        prixColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setPrix(event.getNewValue());
            materielService.modifier(materiel);
        });

        imageUrlColumn.setOnEditCommit(event -> {
            Materiel materiel = event.getRowValue();
            materiel.setImage_url(event.getNewValue());
            materielService.modifier(materiel);
        });

        // Configuration de la colonne des actions (supprimer uniquement)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5); // 5 est l'espacement entre les boutons

            {
                buttonsBox.getChildren().addAll(deleteBtn);

                deleteBtn.setOnAction(event -> {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    materielService.supprimer(materiel);
                    loadMateriels();
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

        // Activer l'édition sur double-clic
        materielTable.setEditable(true);
        materielTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !materielTable.getSelectionModel().isEmpty()) {
                TablePosition<Materiel, ?> pos = materielTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<Materiel, ?> col = pos.getTableColumn();
                if (col == libelleColumn || col == descriptionColumn || col == quantiteColumn || col == prixColumn || col == imageUrlColumn) {
                    materielTable.edit(row, col);
                }
            }
        });
    }

    private void loadMateriels() {
        materielTable.getItems().clear();
        materielTable.getItems().addAll(materielService.rechercher());
    }
}