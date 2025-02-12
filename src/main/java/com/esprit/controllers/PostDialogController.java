package com.esprit.controllers;

import com.esprit.models.*;
import com.esprit.services.PostService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.sql.*;

public class PostDialogController {
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox pinnedCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Post post;
    private boolean isEdit;
    private Stage dialogStage;
    private boolean saveClicked = false;
    private PostService postService;
    private final int LOGGED_USER_ID = 11; // Current logged in user ID
    
    @FXML
    private void initialize() {
        // Add button handlers
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> {
            // Get the stage from any control (using saveButton here)
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });
        
        // Add validation listeners to enable/disable save button
        titleField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButton());
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButton());
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSaveButton());
        
        // Initially disable save button
        updateSaveButton();
    }
    
    private void updateSaveButton() {
        boolean isValid = !titleField.getText().trim().isEmpty() 
                         && !contentArea.getText().trim().isEmpty() 
                         && categoryComboBox.getValue() != null;
        saveButton.setDisable(!isValid);
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
    
    public void setPost(Post post) {
        this.post = post;
        this.isEdit = post != null;
        
        if (isEdit) {
            titleField.setText(post.getTitle());
            contentArea.setText(post.getContent());
            categoryComboBox.setValue(post.getCategory());
            pinnedCheckBox.setSelected(post.isPinned());
        }
    }
    
    public boolean isSaveClicked() {
        return saveClicked;
    }
    
    public Post getPost() {
        return post;
    }
    
    private String getUsernameFromDatabase(int userId) {
        String username = "Default User";
        String query = "SELECT userName FROM users WHERE userID = ?";
        
        try (Connection conn = postService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("userName");
                    System.out.println("Found username: " + username);
                } else {
                    System.out.println("No user found with ID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return username;
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput(true)) {
            return;
        }
        
        try {
            // Get the actual username from database
            String username = getUsernameFromDatabase(LOGGED_USER_ID);
            
            // Create new post using the constructor that includes author
            Post newPost = new Post(
                LOGGED_USER_ID,
                titleField.getText().trim(),
                contentArea.getText().trim(),
                username,  // Using actual username from database
                getCategoryId(categoryComboBox.getValue())
            );
            
            // Set additional fields
            newPost.setCreatedAt(LocalDateTime.now());
            newPost.setUpdatedAt(LocalDateTime.now());
            newPost.setPinned(pinnedCheckBox.isSelected());  // Set pinned value from checkbox
            
            // Debug print
            System.out.println("Saving post with details:");
            System.out.println("Author: " + newPost.getAuthor());
            System.out.println("User ID: " + newPost.getUserId());
            System.out.println("Category ID: " + newPost.getCategoryId());
            System.out.println("Title: " + newPost.getTitle());
            System.out.println("Is Pinned: " + newPost.isPinned());  // Debug print for pinned status
            
            postService.ajouter(newPost);
            
            saveClicked = true;
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save post");
            alert.setContentText("Error: " + e.getMessage());
            e.printStackTrace();
            alert.showAndWait();
        }
    }
    
    public void setPostService(PostService postService) {
        this.postService = postService;
        
        try {
            if (postService != null) {
                // Load only the categories that exist in your database
                categoryComboBox.getItems().addAll(
                    "Announcements",
                    "Warnings"
                );
            }
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInput(boolean showAlert) {
        String errorMessage = "";
        
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "Title is required\n";
        }
        
        if (contentArea.getText() == null || contentArea.getText().trim().isEmpty()) {
            errorMessage += "Content is required\n";
        }
        
        if (categoryComboBox.getValue() == null) {
            errorMessage += "Category is required\n";
        }
        
        if (!errorMessage.isEmpty() && showAlert) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
        
        return errorMessage.isEmpty();
    }
    
    private int getCategoryId(String categoryName) {
        return switch (categoryName) {
            case "Announcements" -> 10;
            case "Warnings" -> 11;
            default -> 10;
        };
    }
}