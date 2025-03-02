package com.esprit.controllers;

import com.esprit.models.*;
import com.esprit.services.PostService;
import com.esprit.services.CategoryService;
import com.esprit.services.ChatService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.ProfanityFilter;
import com.esprit.utils.TextUtils;
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
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GradientPaint;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import java.awt.BasicStroke;

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
            showStyledErrorDialog("Please correct the following issues:\n" + errorMessage, "Invalid Fields");
            return false;
        }
        
        return errorMessage.isEmpty();
    }

    private void showInfo(String message) {
        showStyledInfoDialog(message, "Success");
    }
    
    private void showError(String message) {
        showStyledErrorDialog(message, "Error");
    }
    
    private void showStyledInfoDialog(String message, String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Warm beige background - slightly softer
                g2.setColor(new Color(252, 248, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                
                // Add subtle gradient - enhanced for smoother appearance
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(245, 240, 235), 
                    0, getHeight(), new Color(248, 235, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 18, 18);
                
                // Add a subtle border - softer color
                g2.setColor(new Color(245, 148, 92, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(3, 3, getWidth()-6, getHeight()-6, 20, 20);
                
                g2.dispose();
            }
        };

        panel.setLayout(new BorderLayout(10, 15));
        panel.setBackground(new Color(252, 248, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel messageLabel = new JLabel("<html><body style='width: 280px; font-family: Segoe UI; color: #2D3250; line-height: 1.5;'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(messageLabel, BorderLayout.CENTER);
        
        // Create OK button with warm orange theme - enhanced gradient and smoother appearance
        JButton okButton = new JButton("OK");
        String buttonStyle = "background-color: linear-gradient(to bottom, #f7a76c, #f0945c); " +
                "color: white; " +
                "border: none; " +
                "padding: 10px 30px; " +
                "border-radius: 15px; " +
                "font-size: 14px; " +
                "font-family: 'Segoe UI'; " +
                "font-weight: bold; " +
                "cursor: pointer; " +
                "box-shadow: 0 3px 5px rgba(245,148,92,0.25);";
        okButton.putClientProperty("style", buttonStyle);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        
        // Add some padding above button
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        // Configure the option pane
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null
        );
        
        // Create and style the dialog
        JDialog dialog = optionPane.createDialog(title);
        dialog.setBackground(new Color(252, 248, 245));
        
        // Add button action
        okButton.addActionListener(e -> {
            dialog.dispose();
        });
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    private void showStyledErrorDialog(String message, String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Warm beige background - slightly softer
                g2.setColor(new Color(252, 248, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                
                // Add subtle gradient - enhanced for smoother appearance
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(245, 240, 235), 
                    0, getHeight(), new Color(248, 235, 220));
                g2.setPaint(gp);
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 18, 18);
                
                // Add a subtle border - softer color
                g2.setColor(new Color(245, 148, 92, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(3, 3, getWidth()-6, getHeight()-6, 20, 20);
                
                g2.dispose();
            }
        };

        panel.setLayout(new BorderLayout(10, 15));
        panel.setBackground(new Color(252, 248, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel messageLabel = new JLabel("<html><body style='width: 280px; font-family: Segoe UI; color: #2D3250; line-height: 1.5;'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(messageLabel, BorderLayout.CENTER);
        
        // Create OK button with warm orange theme - enhanced gradient and smoother appearance
        JButton okButton = new JButton("OK");
        String buttonStyle = "background-color: linear-gradient(to bottom, #f7a76c, #f0945c); " +
                "color: white; " +
                "border: none; " +
                "padding: 10px 30px; " +
                "border-radius: 15px; " +
                "font-size: 14px; " +
                "font-family: 'Segoe UI'; " +
                "font-weight: bold; " +
                "cursor: pointer; " +
                "box-shadow: 0 3px 5px rgba(245,148,92,0.25);";
        okButton.putClientProperty("style", buttonStyle);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        
        // Add some padding above button
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        // Configure the option pane
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null
        );
        
        // Create and style the dialog
        JDialog dialog = optionPane.createDialog(title);
        dialog.setBackground(new Color(252, 248, 245));
        
        // Add button action
        okButton.addActionListener(e -> {
            dialog.dispose();
        });
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    public void setupCategoryComboBox() {
        System.out.println("Setting up ComboBox");
        
        try {
            // Load existing categories using rechercher()
            List<Category> categories = categoryService.rechercher();
            for (Category category : categories) {
                categoryComboBox.getItems().add(category.getName());
            }
            
            // Check if current user is admin
            boolean isAdmin = Eutopia.getCurrentUser().getRole() == Role.Admin;
            
            // Make ComboBox editable only for admins
            categoryComboBox.setEditable(isAdmin);
            
            if (isAdmin) {
                // Add new category when Enter is pressed in ComboBox (admin only)
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
                    } else if (newCategoryName.isEmpty()) {
                        showError("Category name cannot be empty");
                    } else {
                        showError("Category already exists");
                    }
                });
            } else {
                // For non-admin users, show message if they try to type
                categoryComboBox.setOnKeyTyped(event -> {
                    showError("Only administrators can create new categories");
                    // Reset to previous valid selection
                    if (!categoryComboBox.getItems().contains(categoryComboBox.getValue())) {
                        categoryComboBox.setValue(null);
                    }
                });
            }
            
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
                "You are a forum post generator. Create a post based on this prompt: '%s'\n\n" +
                "Respond EXACTLY in this format:\n" +
                "<TITLE>\n" +
                "Your title here\n" +
                "</TITLE>\n" +
                "<CONTENT>\n" +
                "Introduction paragraph here\n\n" +
                "### First Main Point\n" +
                "Content for first point\n\n" +
                "### Second Main Point\n" +
                "Content for second point\n\n" +
                "Conclusion paragraph here\n" +
                "</CONTENT>",
                promptArea.getText().trim()
            );

            // Get AI response using ChatService
            ChatService.UserChatMessage response = chatService.processMessage(prompt);
            String aiResponse = response.getContent();

            // Parse title
            String title = extractBetweenTags(aiResponse, "TITLE");
            String content = extractBetweenTags(aiResponse, "CONTENT");

            if (title != null && content != null) {
                // Strip emojis and format
                title = TextUtils.stripEmojis(title.trim());
                content = formatContent(TextUtils.stripEmojis(content.trim()));

                // Update the fields
                titleField.setText(title);
                contentArea.setText(content);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Content generated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to parse AI response");
                System.out.println("AI Response received: " + aiResponse);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractBetweenTags(String text, String tag) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";
        
        int startIndex = text.indexOf(startTag);
        int endIndex = text.indexOf(endTag);
        
        if (startIndex != -1 && endIndex != -1) {
            return text.substring(startIndex + startTag.length(), endIndex).trim();
        }
        return null;
    }

    private String formatContent(String content) {
        // Add extra line breaks between sections for better readability
        content = content.replaceAll("###\\s+", "\n\n### ");
        
        // Add line breaks after paragraphs
        content = content.replaceAll("\\.\n", ".\n\n");
        
        // Ensure consistent spacing around bullet points
        content = content.replaceAll("(?m)^-\\s*", "\n- ");
        
        // Remove any excessive blank lines (more than 2)
        content = content.replaceAll("\\n{3,}", "\n\n");
        
        return content.trim();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}