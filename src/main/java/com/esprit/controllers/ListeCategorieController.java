package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ListeCategorieController {
    @FXML
    private TableView<Categorie> categorieTable;
    @FXML
    private TableColumn<Categorie, String> nomColumn;
    @FXML
    private TableColumn<Categorie, Void> actionsColumn;

    private final CategorieService categorieService;

    public ListeCategorieController() {
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadCategories();
    }

    private void setupColumns() {

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Rendre la colonne "nom" éditable
        nomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nomColumn.setOnEditCommit(event -> {
            Categorie categorie = event.getRowValue();
            categorie.setNom(event.getNewValue());
            categorieService.modifier(categorie);
        });

        // Configurer la colonne des actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5);

            {
                buttonsBox.getChildren().addAll(deleteBtn);

                deleteBtn.setOnAction(event -> {
                    Categorie categorie = getTableView().getItems().get(getIndex());
                    categorieService.supprimer(categorie);
                    loadCategories();
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
        categorieTable.setEditable(true);
        categorieTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !categorieTable.getSelectionModel().isEmpty()) {
                TablePosition<Categorie, ?> pos = categorieTable.getSelectionModel().getSelectedCells().get(0);
                int row = pos.getRow();
                TableColumn<Categorie, ?> col = pos.getTableColumn();
                if (col == nomColumn) {
                    categorieTable.edit(row, col);
                }
            }
        });
    }

    private void loadCategories() {
        categorieTable.getItems().clear();
        categorieTable.getItems().addAll(categorieService.rechercher());
    }
}