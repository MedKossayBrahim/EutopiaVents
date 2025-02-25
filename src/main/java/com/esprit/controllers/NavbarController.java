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

    private String currentPage = "Forum";
    private SearchableController currentController;
    private static final String ACTIVE_STYLE = "-fx-pref-width: 150; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16px;";
    private static final String INACTIVE_STYLE = "-fx-pref-width: 150; -fx-background-color: transparent; -fx-text-fill: #b0a8a0; -fx-font-size: 16px;";

    @FXML
    public void initialize() {
        try {
            String userRole = String.valueOf(Eutopia.getCurrentUser().getRole());
            System.out.println("*************" + userRole);

            // Set initial styles for all buttons
            dashboardButton.setStyle(INACTIVE_STYLE);
            eventsButton.setStyle(INACTIVE_STYLE);
            forumButton.setStyle(INACTIVE_STYLE);
            settingsButton.setStyle(INACTIVE_STYLE);
            storeButton.setStyle(INACTIVE_STYLE);

            // Update button styles based on current page
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
        currentPage = "dashboard";
        updateButtonStyles(currentPage);
        try {
            URL url = getClass().getResource("/MaterielGridView.fxml");
            if (url == null) {
                throw new IOException("Not Found");
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
            alert.setContentText("Could not load the Main page: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onEventsButtonClick(ActionEvent event) {
        currentPage = "events";
        updateButtonStyles(currentPage);
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
            System.err.println("Error loading events view: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not load the events page: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onForumButtonClick(ActionEvent event) {
        currentPage = "forum";
        updateButtonStyles(currentPage);
        try {
            URL url = getClass().getResource("/forum_main_page.fxml");
            if (url == null) {
                throw new IOException("Not found");
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
    public void onSettingsButtonClick(ActionEvent event) {
        currentPage = "dashboard";
        updateButtonStyles(currentPage);
        try {
            URL url = getClass().getResource("/MainMenu.fxml");
            if (url == null) {
                throw new IOException("Cannot find MainMenu.fxml");
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
            alert.setContentText("Could not load the Main page: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onStoreButtonClick(ActionEvent event) {
        currentPage = "store";
        updateButtonStyles(currentPage);
        try {
            URL url = getClass().getResource("/MainProduit.fxml");
            if (url == null) {
                throw new IOException("Cannot find Main.fxml");
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
            alert.setContentText("Could not load the Main page: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void onLogoutButtonClick() throws IOException {
//        try {
//            Path sessionPath = Paths.get("user_session.json");
//            if (Files.exists(sessionPath)) {
//                // Try to close any open file handles
//                System.gc();
//                Thread.sleep(100); // Give a small delay for resources to be released
//
//                // Try multiple times to delete the file
//                int maxAttempts = 5;
//                for (int i = 0; i < maxAttempts; i++) {
//                    try {
//                        Files.delete(sessionPath);
//                        System.out.println("Session file deleted successfully");
//                        break;
//                    } catch (IOException e) {
//                        if (i == maxAttempts - 1) {
//                            // If all attempts fail, try to delete on exit
//                            sessionPath.toFile().deleteOnExit();
//                            System.out.println("File will be deleted on application exit");
//                        } else {
//                            Thread.sleep(100); // Wait before next attempt
//                        }
//                    }
//                }
//            }
//
//            // Load the login view
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
//            Parent root = loader.load();
//            Stage stage = (Stage) logoutButton.getScene().getWindow();
//            stage.setScene(new Scene(root));
//            stage.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//            showError("Error during logout: " + e.getMessage());
//        }
        Eutopia.setCurrentUser(null);
        UserSession.saveUser(null);
        //UserSession.clearUser();
        Eutopia.getSceneManager().switchScene("/login-view.fxml", null); // Start at Page1.fxml
    }

    public void setCurrentController(SearchableController controller) {
        this.currentController = controller;
    }

    private void updateButtonStyles(String activeButton) {
        System.out.println("Updating styles for: " + activeButton); // Debug line

        // Force reset all buttons first
        if (dashboardButton != null) dashboardButton.setStyle(INACTIVE_STYLE);
        if (eventsButton != null) eventsButton.setStyle(INACTIVE_STYLE);
        if (forumButton != null) forumButton.setStyle(INACTIVE_STYLE);
        if (settingsButton != null) settingsButton.setStyle(INACTIVE_STYLE);
        if (storeButton != null) storeButton.setStyle(INACTIVE_STYLE);

        // Apply active style
        switch (activeButton.toLowerCase()) {
            case "dashboard":
                if (dashboardButton != null) {
                    dashboardButton.setStyle(ACTIVE_STYLE);
                    System.out.println("Dashboard style applied: " + dashboardButton.getStyle());
                }
                break;
            case "events":
                if (eventsButton != null) {
                    eventsButton.setStyle(ACTIVE_STYLE);
                    System.out.println("Events style applied: " + eventsButton.getStyle());
                }
                break;
            case "forum":
                if (forumButton != null) {
                    forumButton.setStyle(ACTIVE_STYLE);
                    System.out.println("Forum style applied: " + forumButton.getStyle());
                }
                break;
            case "settings":
                if (settingsButton != null) {
                    settingsButton.setStyle(ACTIVE_STYLE);
                    System.out.println("Settings style applied: " + settingsButton.getStyle());
                }
                break;
            case "store":
                if (storeButton != null) {
                    storeButton.setStyle(ACTIVE_STYLE);
                    System.out.println("Store style applied: " + storeButton.getStyle());
                }
                break;
        }
    }
}