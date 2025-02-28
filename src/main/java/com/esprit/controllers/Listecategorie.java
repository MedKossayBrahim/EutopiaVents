package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.services.CategorieProduitService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class Listecategorie {

    @FXML
    private TableView<categorieproduit> categorieTable;
    @FXML
    private TableColumn<categorieproduit, Integer> idColumn;
    @FXML
    private TableColumn<categorieproduit, String> nomColumn;
    @FXML
    private TableColumn<categorieproduit, String> descriptionColumn; // Nouvelle colonne pour la description
    @FXML
    private TableColumn<categorieproduit, Void> actionsColumn;

    private final CategorieProduitService CategorieProduitService;

    public Listecategorie() throws SQLException {
        CategorieProduitService = new CategorieProduitService();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadCategories();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description")); // Configuration de la colonne description

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, modifyBtn, deleteBtn);

            {
                modifyBtn.setOnAction(event -> {
                    categorieproduit categorie = getTableView().getItems().get(getIndex());
                    ouvrirModification(categorie);
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
        });
    }

    private void loadCategories() {
        categorieTable.getItems().clear();
        categorieTable.getItems().addAll(CategorieProduitService.rechercher());
    }

    private void ouvrirModification(categorieproduit categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCategorieProduit.fxml"));
            Parent root = loader.load();

            ModifierCategorie controller = loader.getController();
            controller.setCategorie(categorie);

            Stage stage = new Stage();
            stage.setTitle("Modifier Catégorie");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
    }

    private void supprimercategorie(categorieproduit categorie) {
        CategorieProduitService.supprimer(categorie); // Assurez-vous d'avoir une méthode supprimer dans votre service
        loadCategories(); // Rechargez la liste après la suppression
    }

    @FXML
    private void handleDelete() {
        categorieproduit selectedCategorie = categorieTable.getSelectionModel().getSelectedItem();
        if (selectedCategorie != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer la catégorie");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                supprimercategorie(selectedCategorie);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une catégorie à supprimer.");
            alert.showAndWait();
        }
    }
}
