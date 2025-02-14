package com.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainProgGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Création d'une interface d'accueil
        VBox root = new VBox(20); // Espacement de 20 entre les éléments
        root.getStyleClass().add("welcome-container");

        // Création des boutons
        Button btnCategories = new Button("Gestion des Catégories");
        Button btnLieux = new Button("Gestion des Lieux");
        Button btnPhotos = new Button("Voir les Photos"); // Ajout du bouton Voir les Photos

        // Stylisation des boutons
        btnCategories.getStyleClass().add("welcome-button");
        btnLieux.getStyleClass().add("welcome-button");
        btnPhotos.getStyleClass().add("welcome-button"); // Appliquer le même style

        // Ajout des actions aux boutons
        btnCategories.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutCategorie.fxml"));
                Parent categorieRoot = loader.load();
                Scene categorieScene = new Scene(categorieRoot);
                Stage categorieStage = new Stage();
                categorieStage.setScene(categorieScene);
                categorieStage.setTitle("Gestion des Catégories");
                categorieStage.show();
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnLieux.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/LieuView.fxml"));
                Parent lieuRoot = loader.load();
                Scene lieuScene = new Scene(lieuRoot);
                Stage lieuStage = new Stage();
                lieuStage.setScene(lieuScene);
                lieuStage.setTitle("Gestion des Lieux");
                lieuStage.show();
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnPhotos.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PhotoView.fxml")); // Assurez-vous que ce fichier existe
                Parent photoRoot = loader.load();
                Scene photoScene = new Scene(photoRoot);
                Stage photoStage = new Stage();
                photoStage.setScene(photoScene);
                photoStage.setTitle("Voir les Photos");
                photoStage.show();
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Ajout des boutons à l'interface
        root.getChildren().addAll(btnCategories, btnLieux, btnPhotos); // Ajout du bouton Voir les Photos

        // Configuration de la scène principale
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/styles/categories.css").toExternalForm());

        // Configuration de la fenêtre principale
        primaryStage.setTitle("Gestion des Salles");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
