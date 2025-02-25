package com.esprit.controllers;

import com.esprit.models.categorie_salle;
import com.esprit.services.CategorieServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;

import java.sql.SQLException;

public class AjouterCategorie {

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfDescription;
    @FXML
    private TableView<categorie_salle> categoriesTable;
    @FXML
    private TableColumn<categorie_salle, String> nomColumn; // Affiche le nom de chaque catégorie
    @FXML
    private TableColumn<categorie_salle, String> descriptionColumn;

    private CategorieServiceImpl categorieService;
    private ObservableList<categorie_salle> categoriesList;

    @FXML
    public void initialize() throws SQLException {
        // Initialisation du service
        categorieService = new CategorieServiceImpl();

        // Configuration des colonnes (liaison avec les propriétés du modèle)
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Rendre la TableView éditable pour l'édition directe
        categoriesTable.setEditable(true);
        nomColumn.setCellFactory(tc -> {
            TableCell<categorie_salle, String> cell = new TableCell<>() {
                private Text text = new Text();
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        text.setWrappingWidth(tc.getWidth() - 10);
                        setGraphic(text);
                    }
                }
            };
            return cell;
        });

        descriptionColumn.setCellFactory(tc -> {
            TableCell<categorie_salle, String> cell = new TableCell<>() {
                private Text text = new Text();
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        text.setWrappingWidth(tc.getWidth() - 10);
                        setGraphic(text);
                    }
                }
            };
            return cell;
        });

        // Make sure columns resize with the table
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Allow rows to grow as needed
        categoriesTable.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        // Gestion de l'édition sur la colonne "nom"
        nomColumn.setOnEditCommit(event -> {
            categorie_salle cat = event.getRowValue();
            String newNom = event.getNewValue().trim();
            if (newNom.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom ne peut être vide.");
                refreshCategoriesList();
                return;
            }
            // Vérifier l'unicité du nom en excluant la catégorie en cours
            boolean exists = categoriesList.stream()
                    .anyMatch(c -> c.getNom().equalsIgnoreCase(newNom) && c.getId() != cat.getId());
            if (exists) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce nom existe déjà !");
                refreshCategoriesList();
            } else {
                cat.setNom(newNom);
                try {
                    categorieService.modifier(cat);
                    refreshCategoriesList();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Nom modifié avec succès !");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
                    refreshCategoriesList();
                }
            }
        });

        // Gestion de l'édition sur la colonne "description"
        descriptionColumn.setOnEditCommit(event -> {
            categorie_salle cat = event.getRowValue();
            String newDesc = event.getNewValue().trim();
            if (newDesc.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La description ne peut être vide.");
                refreshCategoriesList();
                return;
            }
            cat.setDescription(newDesc);
            try {
                categorieService.modifier(cat);
                refreshCategoriesList();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Description modifiée avec succès !");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
                refreshCategoriesList();
            }
        });

        // Chargement initial des données depuis la base
        refreshCategoriesList();

        // Gestion du clic sur une ligne :
        // - Clic simple : remplit le formulaire d'ajout avec les détails de la catégorie sélectionnée.
        // - Double clic : navigue vers l'interface de détail (AfficheCategorie.fxml).
        categoriesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                categorie_salle selected = categoriesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tfNom.setText(selected.getNom());
                    tfDescription.setText(selected.getDescription());
                }
            } else if (event.getClickCount() == 2) {
                categorie_salle selected = categoriesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    navigateToAffichage(selected);
                }
            }
        });

        // Validation en temps réel pour le champ tfNom (vérifie l'unicité)
        tfNom.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNomUnique(newValue);
        });
    }

    // Recharge la TableView avec les catégories depuis la base de données
    private void refreshCategoriesList() {
        categoriesList = FXCollections.observableArrayList(categorieService.rechercher());
        categoriesTable.setItems(categoriesList);
    }

    // Validation en temps réel pour le champ tfNom
    private void validateNomUnique(String nom) {
        boolean exists = categoriesList.stream()
                .anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom.trim()));
        if (exists) {
            tfNom.setStyle("-fx-border-color: red;");
            showTooltip(tfNom, "Ce nom de catégorie existe déjà!");
        } else {
            tfNom.setStyle("");
            removeTooltip(tfNom);
        }
    }

    // Action du bouton "Ajouter" du formulaire
    @FXML
    void addCategorie(ActionEvent event) {
        if (validateInput()) {
            try {
                categorie_salle nouvelleCategorie = new categorie_salle(
                        tfNom.getText().trim(),
                        tfDescription.getText().trim()
                );
                categorieService.ajouter(nouvelleCategorie);
                refreshCategoriesList();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
            }
        }
    }

    // Action du bouton "Voir Categories" (exemple de navigation)
    @FXML
    void voirCategories() {
        if (!categoriesTable.getItems().isEmpty()) {
            navigateToAffichage(categoriesTable.getItems().get(0));
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune catégorie disponible.");
        }
    }

    // Vérifie que les champs du formulaire sont correctement remplis
    private boolean validateInput() {
        String nom = tfNom.getText().trim();
        String description = tfDescription.getText().trim();
        if (nom.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont obligatoires!");
            return false;
        }
        if (categoriesList.stream().anyMatch(cat -> cat.getNom().equalsIgnoreCase(nom))) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une catégorie avec ce nom existe déjà!");
            return false;
        }
        return true;
    }

    private void showTooltip(Control control, String message) {
        Tooltip tooltip = new Tooltip(message);
        Tooltip.install(control, tooltip);
    }

    private void removeTooltip(Control control) {
        Tooltip.uninstall(control, control.getTooltip());
    }

    // Réinitialise les champs du formulaire
    private void clearFields() {
        tfNom.clear();
        tfDescription.clear();
        tfNom.setStyle("");
        removeTooltip(tfNom);
    }

    // Navigation vers l'interface détaillée (AfficheCategorie.fxml) en passant la catégorie sélectionnée
    private void navigateToAffichage(categorie_salle categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficheCategorie.fxml"));
            Parent root = loader.load();
            // Récupération du contrôleur associé à la nouvelle vue
            com.esprit.controllers.AfficheCategorie ac = loader.getController();
            // Passage des détails de la catégorie à la vue détaillée
            ac.setCategorieDetails(categorie);
            // Remplacement de la scène actuelle par la nouvelle vue
            tfNom.getScene().setRoot(root);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la navigation: " + e.getMessage());
        }
    }

    // Affichage d'une alerte
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}