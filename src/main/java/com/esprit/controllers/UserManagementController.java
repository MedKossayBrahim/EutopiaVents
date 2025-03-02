package com.esprit.controllers;

import com.esprit.models.User;
import com.esprit.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TextField searchField;

    private final UserService userService;
    private FilteredList<User> filteredUsers;

    public UserManagementController() throws SQLException {
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupSearch();
        loadUsers();
    }

    private void setupTable() {
        // Create a cell factory for the status column
        TableColumn<User, Boolean> statusColumn = (TableColumn<User, Boolean>) usersTable.getColumns().get(4);
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final Button toggleButton = new Button();
            {
                toggleButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });
            }

            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                } else {
                    toggleButton.setText(active ? "Block" : "Unblock");
                    toggleButton.getStyleClass().clear();
                    toggleButton.getStyleClass().add(active ? "block-button" : "unblock-button");
                    setGraphic(toggleButton);
                }
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredUsers != null) {
                filteredUsers.setPredicate(user -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return user.getFullname().toLowerCase().contains(lowerCaseFilter) ||
                            user.getUserName().toLowerCase().contains(lowerCaseFilter) ||
                            user.getEmail().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAllNonAdminUsers();
            ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
            filteredUsers = new FilteredList<>(observableUsers);
            usersTable.setItems(filteredUsers);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void toggleUserStatus(User user) {
        try {
            boolean newStatus = !user.getActive();
            userService.updateUserStatus(user.getUserID(), newStatus);
            user.setActive(newStatus);
            usersTable.refresh();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "User " + (newStatus ? "unblocked" : "blocked") + " successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update user status: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 