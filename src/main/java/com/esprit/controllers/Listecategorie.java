package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.services.CategorieProduitService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.SQLException;

public class Listecategorie {

    @FXML
    private TableView<categorieproduit> categorieTable;
    @FXML
    private TableColumn<categorieproduit, String> nomColumn;
    @FXML
    private TableColumn<categorieproduit, Void> actionsColumn;
    @FXML
    private TextField searchField;

    private final CategorieProduitService categorieProduitService;

    public Listecategorie() throws SQLException {
        categorieProduitService = new CategorieProduitService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadCategories();
        setupSearch();
    }

    private void setupColumns() {
        categorieTable.setEditable(true);

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nomColumn.setOnEditCommit(event -> {
            categorieproduit categorie = event.getRowValue();
            categorie.setNom(event.getNewValue());
        });

        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<categorieproduit, Void> call(final TableColumn<categorieproduit, Void> param) {
                return new TableCell<>() {
                    private final Button saveBtn = new Button("Sauvegarder");
                    private final Button deleteBtn = new Button("Supprimer");
                    private final HBox buttonsBox = new HBox(5, saveBtn, deleteBtn);

                    {
                        saveBtn.setOnAction(event -> {
                            categorieproduit categorie = getTableView().getItems().get(getIndex());
                            sauvegarderCategorie(categorie);
                        });

                        deleteBtn.setOnAction(event -> {
                            categorieproduit categorie = getTableView().getItems().get(getIndex());
                            supprimercategorie(categorie);
                        });

                        deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
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
                };
            }
        });
    }

    private void loadCategories() {
        categorieTable.getItems().clear();
        categorieTable.getItems().addAll(categorieProduitService.rechercher());
    }

    private void sauvegarderCategorie(categorieproduit categorie) {
        categorieProduitService.modifier(categorie);
        loadCategories();
    }

    private void supprimercategorie(categorieproduit categorie) {
        categorieProduitService.supprimer(categorie);
        loadCategories();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            categorieTable.getItems().setAll(categorieProduitService.rechercher());
        });
    }
}