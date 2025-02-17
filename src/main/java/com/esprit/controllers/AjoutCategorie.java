package com.esprit.controllers;

import com.esprit.models.categorieproduit;
import com.esprit.services.CategorieProduitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AjoutCategorie {

    @FXML
    private TextField des;

    @FXML
    private TextField nom;

    @FXML
    void ajouter(ActionEvent event) throws SQLException {
        CategorieProduitService ps = new CategorieProduitService();
        ps.ajouter(new categorieproduit(nom.getText(), des.getText()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("categ ajoutée");
        alert.show();
    }


}
