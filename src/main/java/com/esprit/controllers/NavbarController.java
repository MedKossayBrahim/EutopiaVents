package com.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class NavbarController {
    @FXML
    private Button dashboardButton;
    @FXML
    private Button eventsButton;
    @FXML
    private Button forumButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private VBox navbar;
    @FXML
    private TextField searchField;
    @FXML
    private HBox searchContainer;
    @FXML
    private Button refreshPostsButton;

    private String currentPage = "Forum";
    private SearchableController currentController;
    private static final String ACTIVE_STYLE = "-fx-pref-width: 150; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16px;";
    private static final String INACTIVE_STYLE = "-fx-pref-width: 150; -fx-background-color: transparent; -fx-text-fill: #b0a8a0; -fx-font-size: 16px;";

    @FXML
    public void initialize() {
        updateButtonStyles("forum");
        if (searchField != null) {
            searchField.setDisable(true);

            searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    searchField.setDisable(true);
                    searchField.setStyle("-fx-background-color: white;");
                }
            });

            searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    searchContainer.setStyle(searchContainer.getStyle() +
                            "; -fx-effect: dropshadow(three-pass-box, #007bff22, 8, 0, 0, 0);");
                } else {
                    searchContainer.setStyle(searchContainer.getStyle().replace(
                            "; -fx-effect: dropshadow(three-pass-box, #007bff22, 8, 0, 0, 0)", ""));
                }
            });
        }
    }

    @FXML
    public void onDashboardButtonClick() {
        updateButtonStyles("dashboard");
        navigateToPage("dashboard.fxml");
    }

    @FXML
    public void onEventsButtonClick() {
        updateButtonStyles("events");
        navigateToPage("events.fxml");
    }

    @FXML
    public void onForumButtonClick(ActionEvent event) {
        try {
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");

            // Define the path to the FXML file
            String fxmlPath = projectRoot + "/src/main/ressources/forum_main_page.fxml";
            File fxmlFile = new File(fxmlPath);

            URL url;
            if (!fxmlFile.exists()) {
                System.err.println("FXML file not found at: " + fxmlPath);
                // Try loading from resources
                url = getClass().getResource("/forum_main_page.fxml");
                if (url == null) {
                    throw new IOException("Cannot find forum_main_page.fxml");
                }
            } else {
                url = fxmlFile.toURI().toURL();
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = ((Button) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Error loading forum main page: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not load the forum page: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onSettingsButtonClick() {
        updateButtonStyles("settings");
        navigateToPage("settings.fxml");
    }

    @FXML
    public void onLogoutButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentController(SearchableController controller) {
        this.currentController = controller;
    }

    @FXML
    private void onSearchButtonClick() throws SQLException {
        if (searchField.isDisable()) {
            searchField.setDisable(false);
            searchField.setStyle("-fx-background-color: white;");
            searchField.requestFocus();
        } else {
            searchField.setDisable(true);
            searchField.clear();
            if (currentController != null) {
                currentController.handleSearch("");  // Clear search
            }
        }
    }

    @FXML
    private void onSearchKeyReleased() throws SQLException {
        if (!searchField.isDisable() && currentController != null) {
            String searchText = searchField.getText().toLowerCase();
            currentController.handleSearch(searchText);
        }
    }

    @FXML
    private void activateSearch() {
        if (!searchField.isFocused()) {
            searchField.requestFocus();
        }
    }

    private void navigateToPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/" + fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SearchableController) {
                setCurrentController((SearchableController) controller);
            }

            dashboardButton.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStyles(String activeButton) {
        // Reset all buttons to inactive style
        resetButtonStyle(dashboardButton);
        resetButtonStyle(eventsButton);
        resetButtonStyle(forumButton);
        resetButtonStyle(settingsButton);

        // Set active style for the clicked button
        switch (activeButton) {
            case "dashboard":
                dashboardButton.setStyle(ACTIVE_STYLE);
                break;
            case "events":
                eventsButton.setStyle(ACTIVE_STYLE);
                break;
            case "forum":
                forumButton.setStyle(ACTIVE_STYLE);
                break;
            case "settings":
                settingsButton.setStyle(ACTIVE_STYLE);
                break;
        }
    }

    private void resetButtonStyle(Button button) {
        if (button != null) {
            button.setStyle(INACTIVE_STYLE);
        }
    }
}