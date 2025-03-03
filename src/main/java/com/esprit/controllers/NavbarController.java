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
    @FXML
    private Button requestsButton;
    @FXML
    private Button userManagementButton;
    @FXML
    private Button profileButton;

    private String currentPage = "events";
    private SearchableController currentController;
    private static final String ACTIVE_STYLE = "-fx-pref-width: 150; " +
            "-fx-background-color: #e6ddd4; " + // Light warm background
            "-fx-text-fill: #7d6d61; " + // Darker warm text
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " + // Rounded corners
            "-fx-padding: 8 16; " + // Padding for better appearance
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"; // Subtle shadow

    private static final String BUTTON_STYLE = "-fx-pref-width: 150; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #b0a8a0; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 8; " + // Matching radius for consistency
            "-fx-padding: 8 16;"; // Matching padding for consistency

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        updateButtonStyles(page);
    }

    /**
     * Sets the forum button as active. This method should be called when the forum page is loaded.
     */
    public void setForumButtonActive() {
        setCurrentPage("forum");
        System.out.println("Forum button set to active");
    }

    @FXML
    public void initialize() {
        try {
            String userRole = String.valueOf(Eutopia.getCurrentUser().getRole());
            System.out.println("Current user role: " + userRole);

            // Make sure buttons are initialized before trying to modify them
            if (requestsButton != null && userManagementButton != null) {
                // Check for ADMIN role (try both uppercase and actual case)
                boolean isAdmin = userRole.equalsIgnoreCase("ADMIN");
                System.out.println("Is user admin? " + isAdmin);

                // Set visibility and managed properties
                requestsButton.setVisible(isAdmin);
                userManagementButton.setVisible(isAdmin);
                requestsButton.setManaged(isAdmin);
                userManagementButton.setManaged(isAdmin);

                System.out.println("Requests button visible: " + requestsButton.isVisible());
                System.out.println("User Management button visible: " + userManagementButton.isVisible());
            } else {
                System.out.println("Admin buttons not properly initialized in FXML");
            }

            // Set initial styles based on current page
            updateButtonStyles(currentPage);

            // Create an array of all navigation buttons for easier management
            Button[] navButtons = { profileButton, eventsButton, dashboardButton, settingsButton,
                    forumButton, storeButton, requestsButton, userManagementButton };

            // Add hover effects to all navigation buttons
            for (Button button : navButtons) {
                if (button != null) {
                    // When mouse enters the button
                    button.setOnMouseEntered(e -> {
                        // Skip if this is the active button
                        if (!button.getStyle().contains("#e6ddd4")) {
                            button.setStyle("-fx-pref-width: 150; " +
                                    "-fx-background-color: #f0ebe7; " + // Lighter background for hover
                                    "-fx-text-fill: #7d6d61; " + // Darker text for better contrast
                                    "-fx-font-size: 16px; " +
                                    "-fx-background-radius: 8; " +
                                    "-fx-padding: 8 16;");
                        }
                    });

                    // When mouse exits the button
                    button.setOnMouseExited(e -> {
                        // If this is not the active button, reset to default style
                        if (!button.getStyle().contains("#e6ddd4")) {
                            button.setStyle(BUTTON_STYLE);
                        }
                    });
                }
            }

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

    @FXML
    public void onRequestsButtonClick(ActionEvent event) {
        setCurrentPage("requests");
        try {
            URL url = getClass().getResource("/Request_List.fxml");
            if (url == null) {
                throw new IOException("Cannot find Request_List.fxml");
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
            handleNavigationError(e, "requests");
        }
    }

    @FXML
    public void onUserManagementButtonClick(ActionEvent event) {
        setCurrentPage("users");
        try {
            URL url = getClass().getResource("/UserManagement.fxml");
            if (url == null) {
                throw new IOException("Cannot find UserManagement.fxml");
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
            handleNavigationError(e, "user management");
        }
    }

    @FXML
    public void onProfileButtonClick(ActionEvent event) {
        setCurrentPage("profile");
        try {
            URL url = getClass().getResource("/editProfile.fxml");
            if (url == null) {
                throw new IOException("Cannot find editProfile.fxml");
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
            handleNavigationError(e, "profile");
        }
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
            URL url = getClass().getResource("/listeproduit.fxml");
            if (url == null) {
                throw new IOException("Cannot find listeproduit.fxml");
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
        // Update the current page
        this.currentPage = activeButton;

        // Reset all buttons to default style
        if (profileButton != null)
            profileButton.setStyle(BUTTON_STYLE);
        if (dashboardButton != null)
            dashboardButton.setStyle(BUTTON_STYLE);
        if (eventsButton != null)
            eventsButton.setStyle(BUTTON_STYLE);
        if (forumButton != null)
            forumButton.setStyle(BUTTON_STYLE);
        if (settingsButton != null)
            settingsButton.setStyle(BUTTON_STYLE);
        if (storeButton != null)
            storeButton.setStyle(BUTTON_STYLE);
        if (requestsButton != null)
            requestsButton.setStyle(BUTTON_STYLE);
        if (userManagementButton != null)
            userManagementButton.setStyle(BUTTON_STYLE);

        // Apply active style to the selected button
        switch (activeButton.toLowerCase()) {
            case "dashboard":
                if (dashboardButton != null)
                    dashboardButton.setStyle(ACTIVE_STYLE);
                break;
            case "events":
                if (eventsButton != null)
                    eventsButton.setStyle(ACTIVE_STYLE);
                break;
            case "forum":
                if (forumButton != null)
                    forumButton.setStyle(ACTIVE_STYLE);
                break;
            case "settings":
                if (settingsButton != null)
                    settingsButton.setStyle(ACTIVE_STYLE);
                break;
            case "store":
                if (storeButton != null)
                    storeButton.setStyle(ACTIVE_STYLE);
                break;
            case "requests":
                if (requestsButton != null)
                    requestsButton.setStyle(ACTIVE_STYLE);
                break;
            case "users":
                if (userManagementButton != null)
                    userManagementButton.setStyle(ACTIVE_STYLE);
                break;
            case "profile":
                if (profileButton != null)
                    profileButton.setStyle(ACTIVE_STYLE);
                break;
        }
    }
}