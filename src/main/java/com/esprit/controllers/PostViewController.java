package com.esprit.controllers;

import com.esprit.services.*;
import com.esprit.models.*;
import com.esprit.utils.DataSource;
import com.esprit.utils.ProfanityFilter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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


public class PostViewController {
    @FXML private Label postTitleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Text contentText;
    @FXML private Label likesCount;
    @FXML private Button likeButton;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentField;
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
        commentField.setOnAction(event -> handleAddComment());

        // Set up delete button with image
        try {
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");
            
            // Define the path to the image file
            String imagePath = projectRoot + "/src/main/ressources/Images/delete.png";
            File imageFile = new File(imagePath);
            
            if (!imageFile.exists()) {
                System.err.println("Image file not found at: " + imagePath);
                // Try loading from resources
                URL resource = getClass().getResource("/Images/delete.png");
                if (resource != null) {
                    imageFile = new File(resource.toURI());
                } else {
                    throw new IOException("Cannot find delete.png");
                }
            }

            Image deleteImage = new Image(imageFile.toURI().toString());
            ImageView deleteIcon = new ImageView(deleteImage);
            deleteIcon.setFitHeight(20);
            deleteIcon.setFitWidth(20);
            deletePostButton.setGraphic(deleteIcon);
            deletePostButton.setText(""); // Remove text, show only icon
            deletePostButton.setStyle("-fx-background-color: transparent;"); // Make button background transparent
            
            // Add hover effect
            deletePostButton.setOnMouseEntered(e -> deletePostButton.setStyle("-fx-background-color: #f8f9fa;"));
            deletePostButton.setOnMouseExited(e -> deletePostButton.setStyle("-fx-background-color: transparent;"));
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load delete icon: " + e.getMessage());
            deletePostButton.setText("Delete"); // Fallback to text if image fails to load
        }
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
        
        // Show delete button only for post owner
        try {
            if (postService.isPostOwner(postId, getCurrentUserId())) {
                deletePostButton.setVisible(true);
                
                // Get the project root directory
                String projectRoot = System.getProperty("user.dir");
                
                // Define the path to the image file
                String imagePath = projectRoot + "/src/main/ressources/Images/delete.png";
                File imageFile = new File(imagePath);
                
                if (!imageFile.exists()) {
                    System.err.println("Image file not found at: " + imagePath);
                    // Try loading from resources
                    URL resource = getClass().getResource("/Images/delete.png");
                    if (resource != null) {
                        imageFile = new File(resource.toURI());
                    } else {
                        throw new IOException("Cannot find delete.png");
                    }
                }

                Image deleteImage = new Image(imageFile.toURI().toString());
                ImageView deleteIcon = new ImageView(deleteImage);
                deleteIcon.setFitHeight(20);
                deleteIcon.setFitWidth(20);
                deletePostButton.setGraphic(deleteIcon);
                deletePostButton.setText(""); // Remove text, show only icon
                deletePostButton.setStyle("-fx-background-color: transparent;"); // Make button background transparent
                
                // Add hover effect
                deletePostButton.setOnMouseEntered(e -> deletePostButton.setStyle("-fx-background-color: #f8f9fa;"));
                deletePostButton.setOnMouseExited(e -> deletePostButton.setStyle("-fx-background-color: transparent;"));
                
                deletePostButton.setOnAction(event -> handleDeletePost());
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
            try {
                // Check for profanity before adding comment
                if (ProfanityFilter.containsProfanity(content)) {
                    showError("Please keep comments appropriate.");
                    return;
                }
                
                Comment comment = new Comment();
                comment.setPostId(postId);
                comment.setUserId(getCurrentUserId());
                comment.setContent(content);
                
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

        // Only show delete button for comments made by current user
        if (comment.getUserId() == getCurrentUserId()) {
            Button deleteButton = new Button();
            
            try {
                // Get the project root directory
                String projectRoot = System.getProperty("user.dir");
                
                // Define the path to the image file
                String imagePath = projectRoot + "/src/main/ressources/Images/delete.png";
                File imageFile = new File(imagePath);
                
                if (!imageFile.exists()) {
                    System.err.println("Image file not found at: " + imagePath);
                    // Try loading from resources
                    URL resource = getClass().getResource("/Images/delete.png");
                    if (resource != null) {
                        imageFile = new File(resource.toURI());
                    } else {
                        throw new IOException("Cannot find delete.png");
                    }
                }

                Image deleteImage = new Image(imageFile.toURI().toString());
                ImageView deleteIcon = new ImageView(deleteImage);
                deleteIcon.setFitHeight(16);
                deleteIcon.setFitWidth(16);
                deleteButton.setGraphic(deleteIcon);
                
                deleteButton.setStyle("-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 2 5;");
                
                deleteButton.setOnMouseEntered(e -> {
                    deleteButton.setStyle("-fx-background-color: #ffeeee; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 2 5; " +
                                        "-fx-background-radius: 3;");
                    deleteIcon.setScaleX(1.1);
                    deleteIcon.setScaleY(1.1);
                });
                
                deleteButton.setOnMouseExited(e -> {
                    deleteButton.setStyle("-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 2 5;");
                    deleteIcon.setScaleX(1.0);
                    deleteIcon.setScaleY(1.0);
                });

                deleteButton.setOnAction(e -> handleDeleteComment(comment.getId(), commentBox));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                headerBox.getChildren().addAll(usernameLabel, spacer, deleteButton);
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to text button if image fails to load
                Button fallbackButton = new Button("🗑️");
                fallbackButton.setStyle("-fx-background-color: transparent; " +
                                      "-fx-text-fill: #8B0000; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-cursor: hand; " +
                                      "-fx-padding: 2 5;");
                fallbackButton.setOnAction(e2 -> handleDeleteComment(comment.getId(), commentBox));
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                headerBox.getChildren().addAll(usernameLabel, spacer, fallbackButton);
            }
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
                    // Create a text field with current content
                    TextField editField = new TextField(comment.getContent());
                    editField.setStyle("-fx-background-color: white; -fx-padding: 5;");
                    
                    // Replace the label with the text field
                    int contentIndex = commentBox.getChildren().indexOf(contentLabel);
                    commentBox.getChildren().set(contentIndex, editField);
                    editField.requestFocus();
                    
                    // Handle edit completion
                    editField.setOnAction(e -> {
                        String newContent = editField.getText().trim();
                        if (!newContent.isEmpty() && !newContent.equals(comment.getContent())) {
                            try {
                                comment.setContent(newContent);
                                commentService.modifier(comment); // Pass the entire comment object
                                contentLabel.setText(newContent);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                showError("Could not update comment: " + ex.getMessage());
                            }
                        }
                        // Restore the label
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
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Comment");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete this comment?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commentService.supprimer(commentId, getCurrentUserId());
                commentsContainer.getChildren().remove(commentBox);
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Could not delete comment.");
            }
        }
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
        // For testing, let's verify if this userID exists in your database
        try {
            String query = "SELECT userID FROM users LIMIT 1";
            try (Connection conn = DataSource.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement(query)) {
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return rs.getInt("userID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("No valid user found in the database");
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
        alert.showAndWait();
    }

    private void handleDeletePost() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Post");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete this post? This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postService.supprimer(postId, getCurrentUserId());
                goBack();
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Could not delete post.");
            }
        }
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
} 