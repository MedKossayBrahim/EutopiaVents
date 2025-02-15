package com.esprit.tests;

import com.esprit.controllers.ForumMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.io.File;
import java.net.URL;

public class Eutopia extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");
            System.out.println("Project Root: " + projectRoot);
            
            // Define the path to the FXML file
            String fxmlPath = projectRoot + "/src/main/ressources/forum_main_page.fxml";
            File fxmlFile = new File(fxmlPath);
            
            // Debug information
            System.out.println("FXML Path: " + fxmlPath);
            System.out.println("File exists: " + fxmlFile.exists());
            System.out.println("File absolute path: " + fxmlFile.getAbsolutePath());
            
            if (!fxmlFile.exists()) {
                throw new IllegalStateException("FXML file not found at: " + fxmlPath);
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
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
            System.err.println("Stack trace:");
            e.printStackTrace();
            
            // Additional debugging information
            File projectDir = new File(System.getProperty("user.dir"));
            System.out.println("\nListing contents of project directory:");
            for (File file : projectDir.listFiles()) {
                System.out.println(file.getName());
            }
            
            File resourcesDir = new File(System.getProperty("user.dir") + "/src/main/ressources");
            if (resourcesDir.exists()) {
                System.out.println("\nListing contents of resources directory:");
                for (File file : resourcesDir.listFiles()) {
                    System.out.println(file.getName());
                }
            } else {
                System.out.println("\nResources directory not found at: " + resourcesDir.getAbsolutePath());
            }
        }
    }
}
