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

    @FXML
    private void retourAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutCategorie.fxml"));
            Parent root = loader.load();
            tfNom.getScene().setRoot(root); //change l’interface actuelle en affichant AjoutCategorie.fxml
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du retour: " + e.getMessage());
        }
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
    @FXML
    private void goToLieu() {
        try {
            // Charge le fichier FXML de l'interface Lieu
            Parent root = FXMLLoader.load(getClass().getResource("/LieuView.fxml"));
            // Remplace la racine de la scène actuelle par la nouvelle vue
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}