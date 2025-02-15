package com.esprit.tests;

import com.esprit.controllers.ForumMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.net.URL;

public class Eutopia extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            URL url = getClass().getResource("/forum_main_page.fxml");
            if (url == null) {
                throw new IOException("Cannot find forum_main_page.fxml");
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            // Get the controller and set the application reference
            ForumMainController controller = loader.getController();
            controller.setApplication(this);

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Set up the scene with full screen dimensions
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            
            // Set stage size to match screen size
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            
            // Set minimum window size
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            primaryStage.setTitle("EutopiaVents");
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
