package com.esprit.controllers;

import com.esprit.models.Materiel;
import com.esprit.services.MaterielService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

public class ListeMaterielController {
    @FXML
    private TableView<Materiel> materielTable;
    @FXML
    private TableColumn<Materiel, String> libelleColumn;
    @FXML
    private TableColumn<Materiel, Integer> quantiteColumn;
    @FXML
    private TableColumn<Materiel, Double> prixColumn;
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
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        
        // Configuration de la colonne des actions (bouton modifier)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5); // 5 est l'espacement entre les boutons

            {
                buttonsBox.getChildren().addAll(modifyBtn, deleteBtn);
                
                modifyBtn.setOnAction(event -> {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    ouvrirModification(materiel);
                });

                deleteBtn.setOnAction(event -> {
                    Materiel materiel = getTableView().getItems().get(getIndex());
                    confirmerSuppression(materiel);
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

    private void loadMateriels() {
        materielTable.getItems().clear();
        materielTable.getItems().addAll(materielService.rechercher());
    }

    private void ouvrirModification(Materiel materiel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMateriel.fxml"));
            Parent root = loader.load();
            
            ModifierMaterielController controller = loader.getController();
            controller.setMateriel(materiel);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier Matériel");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de la modification: " + e.getMessage());
        }
    }

    private void confirmerSuppression(Materiel materiel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le matériel");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce matériel ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                materielService.supprimer(materiel);
                loadMateriels(); // Recharger la liste
            }
        });
    }
} 