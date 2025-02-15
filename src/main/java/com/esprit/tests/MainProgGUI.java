package com.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainProgGUI extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showMainMenu();
    }

    public static void showMainMenu() throws Exception {
        VBox root = new VBox(20);
        root.getStyleClass().add("welcome-container");

        Button[] buttons = {
                createNavButton("Gestion des Catégories", "/AjoutCategorie.fxml"),
                createNavButton("Gestion des Lieux", "/LieuView.fxml"),
                createNavButton("Gestion des Photos", "/PhotoView.fxml"),
                createNavButton("Gestion des Réservations", "/Reservation1View.fxml")
        };

        root.getChildren().addAll(buttons);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(MainProgGUI.class.getResource("/styles/theme.css").toExternalForm());

        primaryStage.setTitle("Gestion des Salles");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Button createNavButton(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setOnAction(e -> loadFXML(fxmlPath));
        return btn;
    }

    public static void loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainProgGUI.class.getResource(fxmlPath));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}