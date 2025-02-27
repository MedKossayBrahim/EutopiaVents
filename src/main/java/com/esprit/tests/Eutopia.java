package com.esprit.tests;

import com.esprit.controllers.*;
import com.esprit.utils.SceneManager;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import com.esprit.models.*;
import com.esprit.utils.*;

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
        // Initialize SceneManager
        sceneManager = new SceneManager(primaryStage);
        currentUser = UserSession.loadUser();

        try {
            if (currentUser == null) {
                sceneManager.switchScene("/login-view.fxml", null);
            } else {
                sceneManager.switchScene("/MaterielGridView.fxml", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ensure stage is maximized
        primaryStage.setMaximized(true);

        // Set stage to full screen dimensions
        centerStageOnScreen(primaryStage);

        primaryStage.setTitle("EutopiaVents");
        primaryStage.show();
    }

    /**
     * Centers the stage on the screen and ensures it's full size.
     */
    private void centerStageOnScreen(Stage stage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }
}
