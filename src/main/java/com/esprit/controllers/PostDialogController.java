package com.esprit.controllers;

import com.esprit.models.*;
import com.esprit.services.PostService;
import com.esprit.services.CategoryService;
import com.esprit.services.ChatService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.ProfanityFilter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.List;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PostDialogController {
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox pinnedCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private CheckBox eventor;
    @FXML private TextArea promptArea;
    @FXML private Button generateButton;
    
    private Post post;
    private boolean isEdit;
    private Stage dialogStage;
    private boolean saveClicked = false;
    private PostService postService;
    private CategoryService categoryService = new CategoryService();
    private ChatService chatService = new ChatService();
    
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
        
        // Add handler for generate button
        generateButton.setOnAction(event -> handleGenerate());
        
        // Disable/Enable prompt area and generate button based on eventor checkbox
        eventor.selectedProperty().addListener((obs, oldVal, newVal) -> {
            promptArea.setDisable(!newVal);
            generateButton.setDisable(!newVal);
        });
        
        // Initially disable save button
        updateSaveButton();
        
        // Initially disable AI components
        promptArea.setDisable(true);
        generateButton.setDisable(true);
    }
    
    private void updateSaveButton() {
        boolean isValid = !titleField.getText().trim().isEmpty() 
                         && !contentArea.getText().trim().isEmpty() 
                         && categoryComboBox.getValue() != null 
                         && !categoryComboBox.getValue().trim().isEmpty();
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
            // Filter profanity in title and content
            String filteredTitle = ProfanityFilter.filter(titleField.getText().trim());
            String filteredContent = ProfanityFilter.filter(contentArea.getText().trim());
            
            // First, handle the category
            String categoryName = categoryComboBox.getValue();
            int categoryId;
            
            // Check if this is a new category
            List<Category> existingCategories = categoryService.rechercher();
            boolean categoryExists = existingCategories.stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(categoryName));
            
            if (!categoryExists) {
                // Add new category
                System.out.println("Adding new category: " + categoryName);
                Category newCategory = new Category(categoryName, "Added from post creation");
                categoryService.ajouter(newCategory);
                
                // Get the newly added category's ID
                categoryId = categoryService.rechercher().stream()
                        .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                        .findFirst()
                        .map(Category::getId)
                        .orElseThrow(() -> new SQLException("Failed to get new category ID"));
            } else {
                // Get existing category ID
                categoryId = existingCategories.stream()
                        .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                        .findFirst()
                        .map(Category::getId)
                        .orElseThrow(() -> new SQLException("Category not found"));
            }
            
            // Get the actual username from database
            String username = getUsernameFromDatabase(getCurrentUserId());
            
            // Create new post with filtered content
            Post newPost = new Post(
                getCurrentUserId(),
                filteredTitle,
                filteredContent,
                username,
                categoryId
            );
            
            // Set additional fields
            newPost.setCreatedAt(LocalDateTime.now());
            newPost.setUpdatedAt(LocalDateTime.now());
            newPost.setPinned(pinnedCheckBox.isSelected());
            
            // Debug print
            System.out.println("Saving post with details:");
            System.out.println("Author: " + newPost.getAuthor());
            System.out.println("User ID: " + newPost.getUserId());
            System.out.println("Category ID: " + newPost.getCategoryId());
            System.out.println("Title: " + newPost.getTitle());
            System.out.println("Is Pinned: " + newPost.isPinned());
            
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
        setupCategoryComboBox();
    }
    
    private boolean validateInput(boolean showAlert) {
        String errorMessage = "";
        
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage += "Title is required\n";
        }
        
        if (contentArea.getText() == null || contentArea.getText().trim().isEmpty()) {
            errorMessage += "Content is required\n";
        }
        
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            errorMessage += "Category is required\n";
        }
        
        // Check for profanity before saving
        if (ProfanityFilter.containsProfanity(titleField.getText()) || 
            ProfanityFilter.containsProfanity(contentArea.getText())) {
            errorMessage += "Post contains inappropriate language\n";
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void setupCategoryComboBox() {
        System.out.println("Setting up ComboBox");
        
        try {
            // Load existing categories using rechercher()
            List<Category> categories = categoryService.rechercher();
            for (Category category : categories) {
                categoryComboBox.getItems().add(category.getName());
            }
            
            // Make ComboBox editable
            categoryComboBox.setEditable(true);
            
            // Add new category when Enter is pressed in ComboBox
            categoryComboBox.getEditor().setOnAction(e -> {
                String newCategoryName = categoryComboBox.getEditor().getText().trim();
                if (!newCategoryName.isEmpty() && !categoryComboBox.getItems().contains(newCategoryName)) {
                    try {
                        System.out.println("Attempting to add new category: " + newCategoryName);
                        
                        // Create and add new category
                        Category newCategory = new Category(newCategoryName, "New category description");
                        System.out.println("Created category object: " + newCategory.getName());
                        
                        categoryService.ajouter(newCategory);
                        
                        // Add to ComboBox and select it
                        categoryComboBox.getItems().add(newCategoryName);
                        categoryComboBox.setValue(newCategoryName);
                        
                        showInfo("New category added successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        System.err.println("Full error: " + ex.getMessage());
                        showError("Could not add new category: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Category name empty or already exists: " + newCategoryName);
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getCurrentUserId() {
        return Eutopia.getCurrentUser().getUserID();
//        try {
//            // Get the path to user_session.json
  //         Path sessionPath = Paths.get("user_session.json");
//
//            // Parse the JSON file
//            JSONParser parser = new JSONParser();
//            JSONObject sessionData = (JSONObject) parser.parse(new FileReader(sessionPath.toFile()));
//
//            // Get the userID from the session
//            Long userID = (Long) sessionData.get("userID");
//            return userID.intValue();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Could not get current user ID from session");
//        }
    }

    @FXML
    private void handleGenerate() {
        if (promptArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a prompt for content generation");
            return;
        }

        try {
            String prompt = String.format(
                "Based on this prompt: '%s', generate a forum post with the following format:\n" +
                "TITLE: A clear, concise title (max 100 characters)\n" +
                "CONTENT: Detailed, well-structured content (max 1000 characters)\n\n" +
                "Make sure the content is appropriate and relevant to the forum context.",
                promptArea.getText().trim()
            );

            // Get AI response using ChatService
            ChatService.UserChatMessage response = chatService.processMessage(prompt);
            String aiResponse = response.getContent();

            // Parse AI response to extract title and content
            String[] parts = aiResponse.split("CONTENT:", 2);
            if (parts.length == 2) {
                String titlePart = parts[0].replace("TITLE:", "").trim();
                String contentPart = parts[1].trim();

                // Update the fields
                titleField.setText(titlePart);
                contentArea.setText(contentPart);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Content generated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to parse AI response");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}