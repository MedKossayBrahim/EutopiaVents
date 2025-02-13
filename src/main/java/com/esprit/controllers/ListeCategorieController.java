package com.esprit.controllers;

import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ListeCategorieController {
    @FXML
    private TableView<Categorie> categorieTable;
    @FXML
    private TableColumn<Categorie, Integer> idColumn;
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifiercateg");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5);

            {
                buttonsBox.getChildren().addAll(modifyBtn, deleteBtn);
                
                modifyBtn.setOnAction(event -> {
                    Categorie categorie = getTableView().getItems().get(getIndex());
                    ouvrirModification(categorie);
                });

                deleteBtn.setOnAction(event -> {
                    Categorie categorie = getTableView().getItems().get(getIndex());
                    confirmerSuppression(categorie);
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
    }

    private void loadCategories() {
        categorieTable.getItems().clear();
        categorieTable.getItems().addAll(categorieService.rechercher());
    }

    private void ouvrirModification(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCategorie.fxml"));
            Parent root = loader.load();
            
            ModifierCategorieController controller = loader.getController();
            controller.setCategorie(categorie);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
    }

    private void confirmerSuppression(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categorieService.supprimer(categorie);
                loadCategories(); // Recharger la liste
            }
        });
    }
} 