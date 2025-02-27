package com.esprit.controllers;

import com.esprit.services.*;
import com.esprit.models.*;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataSource;
import com.esprit.utils.ProfanityFilter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.esprit.components.AutocompleteTextField;

public class PostViewController {
    @FXML private Label postTitleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Text contentText;
    @FXML private Label likesCount;
    @FXML private Button likeButton;
    @FXML private VBox commentsContainer;
    @FXML private AutocompleteTextField commentField;
    @FXML private Button deletePostButton;
    @FXML private Button backButton;

    private int postId;
    private final LikeService likeService = new LikeService();
    private final CommentService commentService = new CommentService();
    private boolean isLiked = false;
    private PostService postService = new PostService();

    @FXML
    public void initialize() {
        likeButton.setOnAction(event -> handleLikeAction());
        
        // Configure AutocompleteTextField
        commentField.setPromptText("Write a comment...");
        commentField.applyTextFieldStyle("-fx-background-radius: 25; " +
                "-fx-padding: 12 20; " +
                "-fx-font-family: 'Segoe UI'; " +
                "-fx-prompt-text-fill: #999; " +
                "-fx-background-color: #f0eae4; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 1); " +
                "-fx-font-size: 13px;");
                
        // Initialize the suggestions pane
        commentField.initializeSuggestions();
                
        // Handle comment submission
        commentField.setOnAction(event -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                handleAddComment();
            }
        });

        // Setup delete post button with image
        try {
            ImageView deleteIcon = new ImageView(getClass().getResource("/Images/delete.png").toExternalForm());
            deleteIcon.setFitHeight(16);
            deleteIcon.setFitWidth(16);

            deletePostButton.setGraphic(deleteIcon);
            deletePostButton.setStyle("-fx-background-color: transparent;");

            deletePostButton.setOnAction(event -> handleDeletePost());
        } catch (Exception e) {
            e.printStackTrace();
            deletePostButton.setText("🗑️");
        }

        // Simplify delete post button
        deletePostButton.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #8B0000; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;");

        // Add hover effect
        deletePostButton.setOnMouseEntered(e ->
                deletePostButton.setStyle("-fx-background-color: #ffeeee; " +
                        "-fx-text-fill: #8B0000; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"));
        deletePostButton.setOnMouseExited(e ->
                deletePostButton.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #8B0000; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"));
    }

    public void setPostData(int postId, String title, String content, LocalDateTime timestamp) {
        this.postId = postId;
        postTitleLabel.setText(title);
        contentText.setText(content);

        // Format the date
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateLabel.setText(timestamp.format(formatter));
        }

        // Get author from database
        try {
            String query = "SELECT u.userName FROM posts p " +
                    "LEFT JOIN users u ON p.user_id = u.userID " +
                    "WHERE p.id = ?";

            try (Connection conn = DataSource.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, postId);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    String authorName = rs.getString("userName");
                    authorLabel.setText(authorName != null ? authorName : "Anonymous");
                } else {
                    authorLabel.setText("Anonymous");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            authorLabel.setText("Anonymous");
        }

        // Check if post is already liked
        try {
            isLiked = likeService.isPostLikedByUser(postId, getCurrentUserId());
            updateLikeButton();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get likes count
        updateLikesCount();

        // Load comments after postId is set
        loadComments();

        // Show delete button for post owner OR admin
        try {
            if (postService.isPostOwner(postId, getCurrentUserId()) || isCurrentUserAdmin()) {
                deletePostButton.setVisible(true);
            } else {
                deletePostButton.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            deletePostButton.setVisible(false);
        }
    }

    private void handleLikeAction() {
        try {
            int userId = getCurrentUserId();
            if (!isLiked) {
                likeService.ajouter(postId, userId);
                isLiked = true;
            } else {
                likeService.supprimer(postId, userId);
                isLiked = false;
            }
            updateLikeButton();
            updateLikesCount();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Could not process like action.");
        }
    }

    @FXML
    private void handleAddComment() {
        String content = commentField.getText().trim();
        if (!content.isEmpty()) {
            // First validate for profanity
            if (ProfanityFilter.containsProfanity(content)) {
                showError("Comment contains inappropriate language");
                return;
            }

            try {
                // Filter profanity before adding comment
                String filteredContent = ProfanityFilter.filter(content);
                
                Comment comment = new Comment();
                comment.setPostId(postId);
                comment.setUserId(getCurrentUserId());
                comment.setContent(filteredContent);

                commentService.ajouter(comment);
                commentField.clear();
                loadComments();

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Could not add comment: " + e.getMessage());
            }
        }
    }

    private void loadComments() {
        try {
            List<Comment> comments = commentService.getCommentsForPost(postId);
            commentsContainer.getChildren().clear();
            for (Comment comment : comments) {
                addCommentToView(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Could not load comments.");
        }
    }

    private void addCommentToView(Comment comment) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 10;");

        // Header with username and delete button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label(comment.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        // Show delete button for comment owner OR admin
        if (comment.getUserId() == getCurrentUserId() || isCurrentUserAdmin()) {
            ImageView deleteIcon = new ImageView(getClass().getResource("/Images/delete.png").toExternalForm());
            deleteIcon.setFitHeight(16);
            deleteIcon.setFitWidth(16);

            Button deleteButton = new Button();
            deleteButton.setGraphic(deleteIcon);
            deleteButton.setStyle("-fx-background-color: transparent;");

            deleteButton.setOnAction(e -> {
                handleDeleteComment(comment.getId(), commentBox);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            headerBox.getChildren().addAll(usernameLabel, spacer, deleteButton);
        } else {
            headerBox.getChildren().add(usernameLabel);
        }

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #333333;");

        // Add double-click editing for comment owner
        if (comment.getUserId() == getCurrentUserId()) {
            contentLabel.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    TextField editField = new TextField(comment.getContent());
                    editField.setStyle("-fx-background-color: white; -fx-padding: 5;");

                    int contentIndex = commentBox.getChildren().indexOf(contentLabel);
                    commentBox.getChildren().set(contentIndex, editField);
                    editField.requestFocus();

                    editField.setOnAction(e -> {
                        String newContent = editField.getText().trim();
                        if (!newContent.isEmpty() && !newContent.equals(comment.getContent())) {
                            // First validate for profanity
                            if (ProfanityFilter.containsProfanity(newContent)) {
                                showError("Comment contains inappropriate language");
                                commentBox.getChildren().set(contentIndex, contentLabel);
                                return;
                            }

                            try {
                                // Filter profanity in edited comment
                                String filteredContent = ProfanityFilter.filter(newContent);
                                comment.setContent(filteredContent);
                                commentService.modifier(comment);
                                contentLabel.setText(filteredContent);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                showError("Could not update comment: " + ex.getMessage());
                            }
                        }
                        commentBox.getChildren().set(contentIndex, contentLabel);
                    });

                    // Handle focus loss
                    editField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commentBox.getChildren().set(contentIndex, contentLabel);
                        }
                    });
                }
            });
        }

        Label timeLabel = new Label(formatTimestamp(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

        commentBox.getChildren().addAll(headerBox, contentLabel, timeLabel);
        commentsContainer.getChildren().add(0, commentBox);
    }

    private void handleDeleteComment(int commentId, VBox commentBox) {
        // Create custom styled dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Comment");
        dialog.setHeaderText(null);
        
        // Style the dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #f0eae4; " +
            "-fx-padding: 20; " +
            "-fx-background-radius: 15;"
        );
        
        // Create content
        Label messageLabel = new Label("Are you sure you want to delete this comment?");
        messageLabel.setStyle(
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666666;"
        );
        dialogPane.setContent(messageLabel);
        
        // Create and style buttons
        ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(deleteButton, cancelButton);
        
        // Style the buttons
        dialogPane.lookupButton(deleteButton).setStyle(
            "-fx-background-color: #ff6b6b; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand;"
        );
        
        dialogPane.lookupButton(cancelButton).setStyle(
            "-fx-background-color: #e0dad4; " +
            "-fx-text-fill: #666666; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand;"
        );
        
        // Add hover effects
        dialogPane.lookupButton(deleteButton).setOnMouseEntered(e -> 
            ((Button) dialogPane.lookupButton(deleteButton)).setStyle(
                "-fx-background-color: #ff5252; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;"
            )
        );
        
        dialogPane.lookupButton(deleteButton).setOnMouseExited(e -> 
            ((Button) dialogPane.lookupButton(deleteButton)).setStyle(
                "-fx-background-color: #ff6b6b; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;"
            )
        );

        dialogPane.lookupButton(cancelButton).setOnMouseEntered(e -> 
            ((Button) dialogPane.lookupButton(cancelButton)).setStyle(
                "-fx-background-color: #d4cec8; " +
                "-fx-text-fill: #666666; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;"
            )
        );
        
        dialogPane.lookupButton(cancelButton).setOnMouseExited(e -> 
            ((Button) dialogPane.lookupButton(cancelButton)).setStyle(
                "-fx-background-color: #e0dad4; " +
                "-fx-text-fill: #666666; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;"
            )
        );

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == deleteButton) {
                try {
                    commentService.supprimer(commentId, getCurrentUserId());
                    commentsContainer.getChildren().remove(commentBox);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Could not delete comment.");
                }
            }
        });
    }

    private void handleDeletePost() {
        // Create custom styled dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Post");
        dialog.setHeaderText(null);
        
        // Style the dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #f0eae4; " +
            "-fx-padding: 20; " +
            "-fx-background-radius: 15;"
        );
        
        // Create content with warning icon
        HBox contentBox = new HBox(10);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        
        // Try to load warning icon, use text fallback if not found
        try {
            ImageView warningIcon = new ImageView(getClass().getResource("/Images/warning.png").toExternalForm());
            warningIcon.setFitHeight(24);
            warningIcon.setFitWidth(24);
            contentBox.getChildren().add(warningIcon);
        } catch (Exception e) {
            // If icon fails to load, use a text symbol instead
            Label warningSymbol = new Label("⚠");
            warningSymbol.setStyle(
                "-fx-text-fill: #ff6b6b; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold;"
            );
            contentBox.getChildren().add(warningSymbol);
        }
        
        VBox messageBox = new VBox(5);
        Label titleLabel = new Label("Delete Post");
        titleLabel.setStyle(
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #ff6b6b;"
        );
        
        Label messageLabel = new Label(
            "Are you sure you want to delete this post?\nThis action cannot be undone."
        );
        messageLabel.setStyle(
            "-fx-font-family: 'Segoe UI'; " +
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #666666;"
        );
        
        messageBox.getChildren().addAll(titleLabel, messageLabel);
        contentBox.getChildren().add(messageBox);
        dialogPane.setContent(contentBox);
        
        // Create and style buttons
        ButtonType deleteButton = new ButtonType("Delete Post", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(deleteButton, cancelButton);
        
        // Style the buttons
        Button deleteBtn = (Button) dialogPane.lookupButton(deleteButton);
        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
        
        String deleteButtonStyle = 
            "-fx-background-color: #ff6b6b; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand; " +
            "-fx-font-family: 'Segoe UI';";
            
        String cancelButtonStyle = 
            "-fx-background-color: #e0dad4; " +
            "-fx-text-fill: #666666; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand; " +
            "-fx-font-family: 'Segoe UI';";
        
        deleteBtn.setStyle(deleteButtonStyle);
        cancelBtn.setStyle(cancelButtonStyle);
        
        // Add hover effects
        deleteBtn.setOnMouseEntered(e -> 
            deleteBtn.setStyle(deleteButtonStyle.replace("#ff6b6b", "#ff5252"))
        );
        
        deleteBtn.setOnMouseExited(e -> 
            deleteBtn.setStyle(deleteButtonStyle)
        );
        
        cancelBtn.setOnMouseEntered(e -> 
            cancelBtn.setStyle(cancelButtonStyle.replace("#e0dad4", "#d4cec8"))
        );
        
        cancelBtn.setOnMouseExited(e -> 
            cancelBtn.setStyle(cancelButtonStyle)
        );

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == deleteButton) {
                try {
                    postService.supprimer(postId, getCurrentUserId());
                    goBack();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Could not delete post: " + e.getMessage());
                }
            }
        });
    }

    private void updateLikeButton() {
        likeButton.setText(isLiked ? "♥" : "♡");
        likeButton.setStyle("-fx-background-color: transparent; " +
                "-fx-font-size: 20px; " +
                "-fx-text-fill: " + (isLiked ? "#FF69B4" : "#b0a8a0") + ";");
    }

    private void updateLikesCount() {
        try {
            int likes = likeService.getLikesCount(postId);
            likesCount.setText(likes + " likes");
        } catch (SQLException e) {
            e.printStackTrace();
            likesCount.setText("0 likes");
        }
    }

    private int getCurrentUserId() {
        return Eutopia.getCurrentUser().getUserID();
//        try {
//            // Get the path to user_session.json
//            Path sessionPath = Paths.get("user_session.json");
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

    private String formatTimestamp(LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return timestamp.format(formatter);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void goBack() {
        try {
            URL url = getClass().getResource("/forum_main_page.fxml");
            if (url == null) {
                throw new IOException("Cannot find forum_main_page.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = deletePostButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not return to main view: " + e.getMessage());
        }
    }

    private int showStyledConfirmDialog(String message, String title) {
        // Create custom buttons with styled look
        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");

        // Style the buttons
        String buttonStyle = "background-color: #007bff; " +
                "color: white; " +
                "border: none; " +
                "padding: 8px 20px; " +
                "border-radius: 5px; " +
                "font-size: 14px; " +
                "cursor: pointer;";

        yesButton.putClientProperty("style", buttonStyle);
        noButton.putClientProperty("style", buttonStyle.replace("#007bff", "#6c757d"));

        // Create the panel with rounded corners
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };

        // Style the panel
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and style the message label
        JLabel messageLabel = new JLabel("<html><body style='width: 200px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(messageLabel, BorderLayout.CENTER);

        // Create button panel with spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

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
        dialog.setBackground(Color.WHITE);

        // Add button actions
        yesButton.addActionListener(e -> {
            optionPane.setValue(JOptionPane.YES_OPTION);
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            optionPane.setValue(JOptionPane.NO_OPTION);
            dialog.dispose();
        });

        // Show dialog and return result
        dialog.setVisible(true);

        Object value = optionPane.getValue();
        return (value == null || !(value instanceof Integer)) ?
                JOptionPane.CLOSED_OPTION : (Integer) value;
    }

    private void showStyledErrorDialog(String message, String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };

        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel("<html><body style='width: 200px'>" + message + "</body></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(messageLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                null,
                panel,
                title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // Add method to check if current user is admin
    private boolean isCurrentUserAdmin() {
        if (Eutopia.getCurrentUser().getRole() == Role.Admin){
            return true;
        }
        return false;
//        try {
//            Path sessionPath = Paths.get("user_session.json");
//            JSONParser parser = new JSONParser();
//            JSONObject sessionData = (JSONObject) parser.parse(new FileReader(sessionPath.toFile()));
//            String role = (String) sessionData.get("role");
//            return "Admin".equalsIgnoreCase(role);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }
} 