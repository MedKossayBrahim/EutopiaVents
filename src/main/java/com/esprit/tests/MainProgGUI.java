package com.esprit.tests;

import com.esprit.models.User;
import com.esprit.utils.SceneManager;
import com.esprit.utils.UserSession;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainProgGUI extends Application {
    private static SceneManager sceneManager;
    private User currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void start(Stage primaryStage) {

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
//        Parent root = loader.load();
//        Scene scene = new Scene(root);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Workshop PIDEV");
//        primaryStage.show();
        sceneManager = new SceneManager(primaryStage);
        currentUser = UserSession.loadUser();
        System.out.println(currentUser);

        try {
            if (currentUser == null) {
                sceneManager.switchScene("/login-view.fxml", null); // Start at Page1.fxml
            } else {
                sceneManager.switchScene("/otp-view.fxml", null); // Start at Page1.fxml
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("EutopiaVents");
        primaryStage.show();

    }
}
