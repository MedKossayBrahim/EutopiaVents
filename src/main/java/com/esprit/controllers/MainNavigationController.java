package com.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainNavigationController {

    @FXML
    private void goToCategories(ActionEvent event) {
        loadView(event, "AjoutCategorie.fxml");
    }

    @FXML
    private void goToLieux(ActionEvent event) {
        loadView(event, "LieuView.fxml");
    }

    @FXML
    private void goToPhotos(ActionEvent event) {
        loadView(event, "photoView.fxml");
    }

    @FXML
    private void goToReservations(ActionEvent event) {
        loadView(event, "Reservation1View.fxml");
    }

    private void loadView(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately in your application
        }
    }
} 