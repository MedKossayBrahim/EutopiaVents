package com.esprit.controllers;

import com.esprit.services.*;
import com.esprit.models.*;
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

import java.io.IOException;
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
        
        // Get author from database (using dummy data for now)
        authorLabel.setText("Anonymous");
        
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
                
                // Load delete icon
                try {
                    Image deleteIcon = new Image(getClass().getResourceAsStream("/com/example/demo1/images/delete.png"));
                    ImageView deleteImageView = new ImageView(deleteIcon);
                    deleteImageView.setFitHeight(16);
                    deleteImageView.setFitWidth(16);
                    deletePostButton.setGraphic(deleteImageView);
                } catch (Exception e) {
                    deletePostButton.setText("🗑️");
                }
                
                deletePostButton.setStyle("-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 2 5;");
                
                deletePostButton.setOnMouseEntered(e -> 
                    deletePostButton.setStyle("-fx-background-color: #ffeeee; " +
                                            "-fx-cursor: hand; " +
                                            "-fx-padding: 2 5; " +
                                            "-fx-background-radius: 3;"));
                
                deletePostButton.setOnMouseExited(e -> 
                    deletePostButton.setStyle("-fx-background-color: transparent; " +
                                            "-fx-cursor: hand; " +
                                            "-fx-padding: 2 5;"));
                
                deletePostButton.setOnAction(event -> handleDeletePost());
            } else {
                deletePostButton.setVisible(false);
            }
        } catch (SQLException e) {
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
    
    private void handleAddComment() {
        String content = commentField.getText().trim();
        if (!content.isEmpty()) {
            try {
                Comment comment = new Comment(postId, getCurrentUserId(), content);
                commentService.ajouter(comment);
                addCommentToView(comment);
                commentField.clear();
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Could not add comment.");
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
                Image deleteIcon = new Image(getClass().getResourceAsStream("/com/example/demo1/images/delete.png"));
                ImageView deleteImageView = new ImageView(deleteIcon);
                deleteImageView.setFitHeight(16);
                deleteImageView.setFitWidth(16);
                deleteButton.setGraphic(deleteImageView);
                
                deleteButton.setStyle("-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 2 5;");
                
                deleteButton.setOnMouseEntered(e -> {
                    deleteButton.setStyle("-fx-background-color: #ffeeee; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 2 5; " +
                                        "-fx-background-radius: 3;");
                    deleteImageView.setScaleX(1.1);
                    deleteImageView.setScaleY(1.1);
                });
                
                deleteButton.setOnMouseExited(e -> {
                    deleteButton.setStyle("-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 2 5;");
                    deleteImageView.setScaleX(1.0);
                    deleteImageView.setScaleY(1.0);
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
        return 1; // Temporary solution without user authentication
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/forum_main_page.fxml"));
            Parent root = loader.load();
            Scene scene = deletePostButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not return to main view.");
        }
    }
} 