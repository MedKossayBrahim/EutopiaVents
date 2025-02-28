package com.esprit.controllers;

import com.esprit.tests.Eutopia;
import com.esprit.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URL;

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
    private Button storeButton;
    @FXML
    private VBox navbar;
    @FXML
    private TextField searchField;
    @FXML
    private HBox searchContainer;
    @FXML
    private Button refreshPostsButton;

    private String currentPage = "events";
    private SearchableController currentController;
    private static final String ACTIVE_STYLE = "-fx-pref-width: 150; " +
            "-fx-background-color: #e6ddd4; " + // Light warm background
            "-fx-text-fill: #7d6d61; " +        // Darker warm text
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +       // Rounded corners
            "-fx-padding: 8 16; " +              // Padding for better appearance
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"; // Subtle shadow

    private static final String BUTTON_STYLE = "-fx-pref-width: 150; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #b0a8a0; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 8; " +       // Matching radius for consistency
            "-fx-padding: 8 16;";                // Matching padding for consistency

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        updateButtonStyles(page);
    }

    @FXML
    public void initialize() {
        try {
            String userRole = String.valueOf(Eutopia.getCurrentUser().getRole());
            System.out.println("*************" + userRole);

            // Set initial styles based on current page
            updateButtonStyles(currentPage);

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
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading user session: " + e.getMessage());
        }
    }

    @FXML
    public void onDashboardButtonClick(ActionEvent event) {
        setCurrentPage("dashboard");
        navigateToDashboard(event);
    }

    @FXML
    public void onEventsButtonClick(ActionEvent event) {
        setCurrentPage("events");
        navigateToEvents(event);
    }

    @FXML
    public void onForumButtonClick(ActionEvent event) {
        setCurrentPage("forum");
        navigateToForum(event);
    }

    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
        setCurrentPage("settings");
        navigateToSettings(event);
    }

    @FXML
    public void onStoreButtonClick(ActionEvent event) {
        setCurrentPage("store");
        navigateToStore(event);
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            URL url = getClass().getResource("/MaterielGridView.fxml");
            if (url == null) {
                throw new IOException("Not Found");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Scene scene = ((Button) event.getSource()).getScene();
            VBox existingNavbar = navbar;
            
            HBox container = new HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            container.getChildren().addAll(existingNavbar, root);
            
            HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
            
            scene.setRoot(container);
        } catch (IOException e) {
            handleNavigationError(e, "Main");
        }
    }

    private void navigateToEvents(ActionEvent event) {
        try {
            URL url = getClass().getResource("/events-view.fxml");
            if (url == null) {
                throw new IOException("Not found");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Scene scene = ((Button) event.getSource()).getScene();
            scene.setRoot(root);
            
        } catch (IOException e) {
            handleNavigationError(e, "events");
        }
    }

    private void navigateToForum(ActionEvent event) {
        try {
            URL url = getClass().getResource("/forum_main_page.fxml");
            if (url == null) {
                throw new IOException("Not found");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Scene scene = ((Button) event.getSource()).getScene();
            VBox existingNavbar = navbar;
            
            HBox container = new HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            container.getChildren().addAll(existingNavbar, root);
            
            HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
            
            scene.setRoot(container);
        } catch (IOException e) {
            handleNavigationError(e, "forum");
        }
    }

    private void navigateToSettings(ActionEvent event) {
        try {
            URL url = getClass().getResource("/MainMenu.fxml");
            if (url == null) {
                throw new IOException("Cannot find MainMenu.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Scene scene = ((Button) event.getSource()).getScene();
            VBox existingNavbar = navbar;
            
            HBox container = new HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            container.getChildren().addAll(existingNavbar, root);
            
            HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
            
            scene.setRoot(container);
        } catch (IOException e) {
            handleNavigationError(e, "settings");
        }
    }

    private void navigateToStore(ActionEvent event) {
        try {
            URL url = getClass().getResource("/MainProduit.fxml");
            if (url == null) {
                throw new IOException("Cannot find MainProduit.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            
            Scene scene = ((Button) event.getSource()).getScene();
            VBox existingNavbar = navbar;
            
            HBox container = new HBox();
            container.setSpacing(0);
            container.setStyle("-fx-background-color: white;");
            container.getChildren().addAll(existingNavbar, root);
            
            HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
            
            scene.setRoot(container);
        } catch (IOException e) {
            handleNavigationError(e, "store");
        }
    }

    private void handleNavigationError(IOException e, String page) {
        System.err.println("Error loading " + page + " page: " + e.getMessage());
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Navigation Error");
        alert.setContentText("Could not load the " + page + " page: " + e.getMessage());
        alert.showAndWait();
    }

    @FXML
    public void onLogoutButtonClick() throws IOException {
        Eutopia.setCurrentUser(null);
        UserSession.saveUser(null);
        Eutopia.getSceneManager().switchScene("/login-view.fxml", null);
    }

    public void setCurrentController(SearchableController controller) {
        this.currentController = controller;
    }

    public void updateButtonStyles(String activeButton) {
        // Only update styles if the page has changed
        if (!activeButton.equals(currentPage)) {
            return;
        }

        // Reset all buttons to default style
        if (dashboardButton != null) dashboardButton.setStyle(BUTTON_STYLE);
        if (eventsButton != null) eventsButton.setStyle(BUTTON_STYLE);
        if (forumButton != null) forumButton.setStyle(BUTTON_STYLE);
        if (settingsButton != null) settingsButton.setStyle(BUTTON_STYLE);
        if (storeButton != null) storeButton.setStyle(BUTTON_STYLE);

        // Apply active style to the selected button
        switch (activeButton.toLowerCase()) {
            case "dashboard":
                if (dashboardButton != null) dashboardButton.setStyle(ACTIVE_STYLE);
                break;
            case "events":
                if (eventsButton != null) eventsButton.setStyle(ACTIVE_STYLE);
                break;
            case "forum":
                if (forumButton != null) forumButton.setStyle(ACTIVE_STYLE);
                break;
            case "settings":
                if (settingsButton != null) settingsButton.setStyle(ACTIVE_STYLE);
                break;
            case "store":
                if (storeButton != null) storeButton.setStyle(ACTIVE_STYLE);
                break;
        }
    }
}