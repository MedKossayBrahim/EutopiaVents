package com.esprit.tests;

import com.esprit.models.User;
import com.esprit.utils.SceneManager;
import com.esprit.utils.UserSession;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Eutopia extends Application {

    private static SceneManager sceneManager;
    private static User currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        Eutopia.currentUser = currentUser;
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize SceneManager first
        sceneManager = new SceneManager(primaryStage);
        currentUser = UserSession.loadUser();
        try {
            if (currentUser == null) {
                sceneManager.switchScene("/login-view.fxml", null); // Start at Page1.fxml
            } else {
                sceneManager.switchScene("/events-view.fxml", null); // Start at Page1.fxml
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//            URL url = getClass().getResource("/login-view.fxml");
//            if (url == null) {
//                throw new IOException("Cannot find login-view.fxml");
//            }
//
//            // Load the FXML file
//            FXMLLoader loader = new FXMLLoader(url);
//            Parent root = loader.load();
//
//            // Get the controller
//            LoginView controller = loader.getController();
//
//            // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
//
//            // Set up the scene with full screen dimensions
//            Scene scene = new Scene(root);
//            primaryStage.setScene(scene);

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

    }
}
