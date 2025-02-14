package com.esprit.tests;

import com.esprit.models.User;
import com.esprit.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainProgGUI extends Application {
    private User currentUser = null;

    private static SceneManager sceneManager;


    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
//        Parent root = loader.load();
//        Scene scene = new Scene(root);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Workshop PIDEV");
//        primaryStage.show();
        sceneManager = new SceneManager(primaryStage);
        try {
            sceneManager.switchScene("/login-view.fxml",null); // Start at Page1.fxml
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("EutopiaVents");
        primaryStage.show();

    }
}
