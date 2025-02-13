package com.esprit.controllers;
import com.esprit.models.Categorie;
import com.esprit.services.CategorieService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class Ajoutercateg {

    @FXML
    private TextField tfcategorie;

    @FXML
    void addcateg(ActionEvent event) {
        CategorieService cs = new CategorieService();
        cs.ajouter(new Categorie(tfcategorie.getText()));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("categorie  ajoutée");
        alert.show();
    }

}
