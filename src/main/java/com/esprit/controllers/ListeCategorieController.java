package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
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

import java.io.IOException;
import java.sql.SQLException;

public class ListeCategorieController {
    @FXML
    private TableView<Categorie> categorieTable;
    @FXML
    private TableColumn<Categorie, String> nomColumn;
    @FXML
    private TableColumn<Categorie, Void> actionsColumn;
    @FXML
    private TextField searchField; // Zone de recherche

    private final CategorieService categorieService;
    private ObservableList<Categorie> categoriesList; // Liste observable des catégories
    private FilteredList<Categorie> filteredCategories; // Liste filtrée

    public ListeCategorieController() throws SQLException {
        categorieService = new CategorieService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadCategories();

        // Configurer la zone de recherche
        setupSearch();
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
        categoriesList = FXCollections.observableArrayList(categorieService.rechercher());
        filteredCategories = new FilteredList<>(categoriesList, p -> true); // Initialiser la liste filtrée
        categorieTable.setItems(filteredCategories); // Lier la liste filtrée à la TableView
    }

    private void setupSearch() {
        // Ajouter un écouteur sur le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCategories.setPredicate(categorie -> {
                // Si le champ de recherche est vide, afficher toutes les catégories
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Convertir le texte saisi et le nom de la catégorie en minuscules pour une recherche insensible à la casse
                String lowerCaseFilter = newValue.toLowerCase();
                String nomCategorie = categorie.getNom().toLowerCase();

                // Vérifier si le nom de la catégorie contient le texte saisi
                return nomCategorie.contains(lowerCaseFilter);
            });
        });
    }
    @FXML
    private void ajoutercateg() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ajoutercateg.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajout de Catégorie");
            stage.setScene(new Scene(root));
            stage.show();

            // Rafraîchir la liste après fermeture de la fenêtre d'ajout
            stage.setOnHidden(event -> loadCategories());

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre d'ajout : " + e.getMessage());
        }
    }

}