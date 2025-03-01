package com.esprit.controllers;

import com.esprit.models.CategoriesEvent;
import com.esprit.models.User;
import com.esprit.services.CategoriesEventService;
import com.esprit.tests.Eutopia;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.geometry.Pos;

public class Ajoutercategorieevent implements Initializable {
    @FXML private TextField categ;
    @FXML private TableView<CategoriesEvent> categorieTable;
    @FXML private TableColumn<CategoriesEvent, String> nomColumn;
    @FXML private TableColumn<CategoriesEvent, Void> actionsColumn;

    private CategoriesEventService service = new CategoriesEventService();

    @FXML
    private BorderPane rootPane;
    @FXML
    private Button btnAjouterCateg;
    @FXML
    private Button btnAjouterEvenement;
    @FXML
    private Button btnModifierEvenement;
    @FXML
    private Button btnGererEvenements;

    @FXML
    private void goToAjouterCateg() {
        loadPage("/AjouterCateg.fxml");
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
        setupTableStyle();
        User currentUser = Eutopia.getCurrentUser();
        if (currentUser != null) {
            // Vérifier le type de service en fonction du rôle de l'utilisateur
            switch (currentUser.getRole()) {
                case Admin:
                    // Admin peut voir tous les boutons
                    break;

                case Organisateur:
                    // Organisateur peut voir tous les boutons sauf GererEvenements et AjouterCateg
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                case Participant:
                    // Participant ne peut voir aucun bouton de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;

                default:
                    // Par défaut, cacher tous les boutons de gestion
                    btnAjouterCateg.setVisible(false);
                    btnAjouterCateg.setManaged(false);
                    btnAjouterEvenement.setVisible(false);
                    btnAjouterEvenement.setManaged(false);
                    btnModifierEvenement.setVisible(false);
                    btnModifierEvenement.setManaged(false);
                    btnGererEvenements.setVisible(false);
                    btnGererEvenements.setManaged(false);
                    break;
            }
        }
    }

    private void setupTableStyle() {
        // Add row hover effect
        categorieTable.setRowFactory(tv -> {
            TableRow<CategoriesEvent> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent;");

            row.setOnMouseEntered(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f8f9fa;");
                }
            });
            row.setOnMouseExited(event -> {
                if (!row.isEmpty()) {
                    row.setStyle("");
                }
            });
            return row;
        });
    }

    private void setupTable() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(8, modifyBtn, deleteBtn);

            {
                // Modern button styling
                String modifyBtnStyle = "-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13; -fx-padding: 8 16;";
                String deleteBtnStyle = "-fx-background-color: #dc3545; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13; -fx-padding: 8 16;";

                modifyBtn.setStyle(modifyBtnStyle);
                deleteBtn.setStyle(deleteBtnStyle);
                box.setAlignment(Pos.CENTER);

                // Enhanced hover effects
                modifyBtn.setOnMouseEntered(e ->
                        modifyBtn.setStyle(modifyBtnStyle + "-fx-background-color: #138496; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 0);"));
                modifyBtn.setOnMouseExited(e ->
                        modifyBtn.setStyle(modifyBtnStyle));

                deleteBtn.setOnMouseEntered(e ->
                        deleteBtn.setStyle(deleteBtnStyle + "-fx-background-color: #c82333; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 0);"));
                deleteBtn.setOnMouseExited(e ->
                        deleteBtn.setStyle(deleteBtnStyle));

                // Add button actions
                modifyBtn.setOnAction(e -> {
                    CategoriesEvent categorie = getTableView().getItems().get(getIndex());
                    handleModify(categorie);
                });

                deleteBtn.setOnAction(e -> {
                    CategoriesEvent categorie = getTableView().getItems().get(getIndex());
                    supprimer(categorie);
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
        String categoryName = categ.getText().trim();

        // Vérification si le champ est vide
        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la catégorie ne peut pas être vide.");
            return;
        }

        // Vérification de la longueur maximale (par exemple, 50 caractères)
        if (categoryName.length() > 50) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la catégorie ne peut pas dépasser 50 caractères.");
            return;
        }

        CategoriesEvent newCategorie = new CategoriesEvent(categoryName);
        if (service.isCategoryExists(newCategorie.getNom())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La catégorie existe déjà.");
            return;
        }

        service.ajouter(newCategorie);
        categ.clear();
        refreshTable();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès.");
    }

    private void handleModify(CategoriesEvent categorie) {
        TextInputDialog dialog = new TextInputDialog(categorie.getNom());
        dialog.setTitle("Modifier");
        dialog.setContentText("Nouveau nom:");

        dialog.showAndWait().ifPresent(newName -> {
            // Vérification si le champ est vide
            if (newName.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la catégorie ne peut pas être vide.");
                return;
            }

            // Vérification de la longueur maximale (par exemple, 50 caractères)
            if (newName.length() > 50) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom de la catégorie ne peut pas dépasser 50 caractères.");
                return;
            }

            // Si les validations passent, modifier la catégorie
            categorie.setNom(newName);
            service.modifier(categorie);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie modifiée avec succès.");
            refreshTable();
        });
    }

    @FXML
    private void supprimer(CategoriesEvent categorie) {
        if (service.isCategoryLinkedToEvent(categorie.getId())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La catégorie ne peut pas être supprimée car elle est liée à un événement.");
            return;
        }

        service.supprimer(categorie);
        refreshTable();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée avec succès.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshTable() {
        categorieTable.setItems(FXCollections.observableArrayList(service.rechercher()));
    }
}
