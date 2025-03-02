package com.esprit.controllers;

import com.esprit.services.ParticipantService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RequestList implements Initializable {
    @FXML
    private TableView<Map<String, Object>> userRequestsTable;

    private final ParticipantService participantService = new ParticipantService();

    public RequestList() throws SQLException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadRequests();
    }

    private void setupTable() {
        // Create a cell factory for the actions column
        TableColumn<Map<String, Object>, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(200);
        
        Callback<TableColumn<Map<String, Object>, Void>, TableCell<Map<String, Object>, Void>> cellFactory = 
            new Callback<>() {
                @Override
                public TableCell<Map<String, Object>, Void> call(final TableColumn<Map<String, Object>, Void> param) {
                    return new TableCell<>() {
                        private final Button acceptBtn = new Button("Accept");
                        private final Button denyBtn = new Button("Deny");
                        private final HBox buttons = new HBox(10, acceptBtn, denyBtn);

                        {
                            acceptBtn.getStyleClass().add("accept-button");
                            denyBtn.getStyleClass().add("deny-button");
                            buttons.setAlignment(Pos.CENTER);

                            acceptBtn.setOnAction(event -> handleAccept(getTableRow().getItem()));
                            denyBtn.setOnAction(event -> handleDeny(getTableRow().getItem()));
                        }

                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(buttons);
                            }
                        }
                    };
                }
            };

        actionsColumn.setCellFactory(cellFactory);
        userRequestsTable.getColumns().add(actionsColumn);
    }

    private void loadRequests() {
        List<Map<String, Object>> userRequests = participantService.getUserRequests();
        ObservableList<Map<String, Object>> observableList = FXCollections.observableArrayList(userRequests);
        userRequestsTable.setItems(observableList);
    }

    private void handleAccept(Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userID");
            participantService.updateRoleToOrganisateur(userId);
            participantService.deleteRequest(userId);
            loadRequests(); // Refresh the table
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "User has been promoted to Organisateur successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to accept request: " + e.getMessage());
        }
    }

    private void handleDeny(Map<String, Object> request) {
        try {
            int userId = (Integer) request.get("userID");
            participantService.deleteRequest(userId);
            loadRequests(); // Refresh the table
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Request has been denied and removed.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to deny request: " + e.getMessage());
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
