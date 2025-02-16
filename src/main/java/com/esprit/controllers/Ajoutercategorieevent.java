package com.esprit.controllers;

import com.esprit.models.CategoriesEvent;
import com.esprit.services.CategoriesEventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class Ajoutercategorieevent implements Initializable {
    @FXML private TextField categ;
    @FXML private TableView<CategoriesEvent> categorieTable;
    @FXML private TableColumn<CategoriesEvent, String> nomColumn;
    @FXML private TableColumn<CategoriesEvent, Void> actionsColumn;

    private CategoriesEventService service = new CategoriesEventService();

    @FXML
    private BorderPane rootPane;

    public Ajoutercategorieevent() throws SQLException {
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

    private void loadPage(String page) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(page));
            Scene scene = rootPane.getScene();
            scene.setRoot(newPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        refreshTable();
    }

    private void setupTable() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(5, modifyBtn, deleteBtn);

            {
                modifyBtn.setOnAction(e -> {
                    CategoriesEvent categorie = getTableView().getItems().get(getIndex());
                    handleModify(categorie);
                });

                deleteBtn.setOnAction(e -> {
                    CategoriesEvent categorie = getTableView().getItems().get(getIndex());
                    service.supprimer(categorie);
                    refreshTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void ajouter() {
        CategoriesEvent newCategorie = new CategoriesEvent(categ.getText());
        service.ajouter(newCategorie);
        categ.clear();
        refreshTable();
    }

    private void handleModify(CategoriesEvent categorie) {
        TextInputDialog dialog = new TextInputDialog(categorie.getNom());
        dialog.setTitle("Modifier");
        dialog.setContentText("Nouveau nom:");

        dialog.showAndWait().ifPresent(newName -> {
            categorie.setNom(newName);
            service.modifier(categorie);
            refreshTable();
        });
    }

    private void refreshTable() {
        categorieTable.setItems(FXCollections.observableArrayList(service.rechercher()));
    }
}
