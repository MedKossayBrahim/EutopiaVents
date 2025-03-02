package com.esprit.controllers;

import com.esprit.models.categorie_salle;
import com.esprit.services.CategorieServiceImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AfficheCategorie {
    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfDescription;
    //Stocke la catégorie en cours de modification
    private categorie_salle currentCategorie;

    private CategorieServiceImpl categorieService;



    @FXML
    public void initialize() throws SQLException {
        categorieService = new CategorieServiceImpl();
    }

    public void setCategorieDetails(categorie_salle categorie) {
        this.currentCategorie = categorie;
        tfNom.setText(categorie.getNom());
        tfDescription.setText(categorie.getDescription());
    }

    @FXML
    private void modifierCategorie() {
        if (validateInput()) {
            try {
                String nouveauNom = tfNom.getText().trim();
                // Vérifier si le nouveau nom existe déjà (sauf pour la catégorie actuelle)
                boolean nomExiste = categorieService.rechercher().stream()
                        .anyMatch(cat -> cat.getNom().equalsIgnoreCase(nouveauNom)
                                && cat.getId() != currentCategorie.getId());

                if (nomExiste) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Une catégorie avec ce nom existe déjà!");
                    return;
                }

                currentCategorie.setNom(nouveauNom);
                currentCategorie.setDescription(tfDescription.getText().trim());

                categorieService.modifier(currentCategorie);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Catégorie modifiée avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la modification: " + e.getMessage());
            }
        }
    }

    @FXML
    private void supprimerCategorie() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cette catégorie ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                categorieService.supprimer(currentCategorie);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Catégorie supprimée avec succès!");
                retourAjout();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    /**
     * Helper method to navigate to a new view while preserving the navbar
     * @param fxmlPath The path to the FXML file to load
     * @param navbarSection The section to highlight in the navbar
     */
    private void navigateWithNavbar(String fxmlPath, String navbarSection) {
        try {
            // Get the current scene's root
            javafx.scene.Parent currentRoot = tfNom.getScene().getRoot();
            
            // Find the navbar in the current scene
            javafx.scene.layout.VBox navbar = null;
            if (currentRoot instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox container = (javafx.scene.layout.HBox) currentRoot;
                for (javafx.scene.Node node : container.getChildren()) {
                    if (node instanceof javafx.scene.layout.VBox && node.getId() != null && node.getId().equals("navbar")) {
                        navbar = (javafx.scene.layout.VBox) node;
                        break;
                    }
                }
            }
            
            // Load the new view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            
            // Create a container with navbar and content
            javafx.scene.layout.HBox container = new javafx.scene.layout.HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            
            if (navbar != null) {
                // If navbar was found, reuse it
                container.getChildren().addAll(navbar, content);
                
                // Update the navbar to show the active section
                NavbarController navController = (NavbarController) navbar.getProperties().get("controller");
                if (navController != null) {
                    navController.updateButtonStyles(navbarSection);
                }
            } else {
                // If navbar wasn't found, load view directly
                container.getChildren().add(content);
            }
            
            javafx.scene.layout.HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
            
            // Set the new scene
            tfNom.getScene().setRoot(container);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page demandée: " + e.getMessage());
        }
    }

    @FXML
    private void retourAjout() {
        navigateWithNavbar("/AjoutCategorie.fxml", "settings");
    }

    @FXML
    private void goToLieu() {
        navigateWithNavbar("/LieuView.fxml", "settings");
    }

    private boolean validateInput() {
        String nom = tfNom.getText().trim();
        String description = tfDescription.getText().trim();

        if (nom.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Tous les champs sont obligatoires!");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}