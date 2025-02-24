package com.esprit.controllers;

import com.esprit.models.categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import com.esprit.services.*;

public class AjoutCategorie {

    @FXML
    private TextField des;

    @FXML
    private TextField nom;

    @FXML
    void ajouter(ActionEvent event) {
        CategorieService ps = new CategorieService();
        ps.ajouter(new categorie(nom.getText(), des.getText()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("categ ajoutée");
        alert.show();
    }


}
