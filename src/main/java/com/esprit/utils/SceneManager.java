package com.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class SceneManager {
    private final Stage stage;
    private final Stack<Scene> history = new Stack<>();

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    /**
     * Switches to a new scene and optionally passes data.
     *
     * @param fxmlPath The path to the FXML file.
     * @param data     The data to pass to the new scene's controller (nullable).
     * @param <T>      The expected type of the controller.
     */
    public <T> void switchScene(String fxmlPath, Object data) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the controller of the new scene
        T controller = loader.getController();

        // Pass data if the controller implements DataReceiver
        if (controller instanceof DataReceiver) {
            ((DataReceiver) controller).setData(data);
        }

        Scene newScene = new Scene(root);

        // Push current scene to history before switching
        if (stage.getScene() != null) {
            history.push(stage.getScene());
        }

        stage.setScene(newScene);
        stage.show();
    }

    /**
     * Returns to the previous scene if available.
     */
    public void goBack() {
        if (!history.isEmpty()) {
            stage.setScene(history.pop());
        }
    }

    public Object getStylesheets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStylesheets'");
    }
}
