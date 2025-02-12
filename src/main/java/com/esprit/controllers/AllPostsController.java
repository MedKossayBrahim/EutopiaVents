package com.esprit.controllers;

import com.esprit.controllers.*;
import com.esprit.tests.Eutopia;
import com.esprit.services.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AllPostsController extends ForumMainController {
    private Eutopia application;
    @FXML
    private ListView<String> postsListView;
    @FXML
    private TextField searchField;
    @FXML
    private VBox calendarContainer;
    private CalendarController calendarController;
    
    private Map<String, Integer> postIdMap = new HashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eutopia_db";
    private static final String USER = "root";
    private static final String PASS = "";
    private final Random random = new Random();
    private final String[] pastelColors = {
            "#FFB3BA", "#BAFFC9", "#BAE1FF", "#FFFFBA",
            "#FFB3F7", "#B3FFE5", "#B3BEFF", "#FFE5B3"
    };
    
    private ArrayList<String> originalPosts = new ArrayList<>();
    private Map<String, LocalDateTime> postTimestamps = new HashMap<>();
    private static final int POSTS_PER_PAGE = 50;
    private int currentPage = 1;
    private int totalPages;
    @FXML private Pagination pagination;

    public void setPosts(ArrayList<String> posts, String category) {
        originalPosts = new ArrayList<>(posts);
        postTimestamps = new HashMap<>(); // Clear existing timestamps
        
        // Get timestamps from database
        String sql = "SELECT title, created_at FROM posts";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String title = rs.getString("title");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                for (String post : posts) {
                    if (post.startsWith(title + ":")) {
                        postTimestamps.put(post, createdAt);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        postsListView.getItems().clear();
        postsListView.getItems().addAll(posts);
        loadPosts();
        setupListView();
    }

    public void setApplication(Eutopia application) {
        this.application = application;
    }
    @FXML
    public void initialize() {
        loadPosts();
        setupListView();
        // Add listener to searchField
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            onSearchKeyReleased();
        });

        // Initialize calendar
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/calendar.fxml"));
            VBox calendarView = loader.load();
            calendarController = loader.getController();
            calendarController.setMainController(this);
            calendarContainer.getChildren().add(calendarView);
        } catch (IOException e) {
            System.err.println("Could not load calendar.fxml");
            e.printStackTrace();
        }
    }

    private void loadPosts() {
        String sql = "SELECT id, title, created_at FROM posts";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                postIdMap.put(title, id);
                postTimestamps.put(title, createdAt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getPostIdFromTitle(String title) {
        return postIdMap.getOrDefault(title, 0);
    }

    private int getCurrentUserId() {
        return 1; // Temporary solution without user authentication
    }

    private void setupListView() {
        postsListView.setStyle("-fx-background-color: transparent; " +
                              "-fx-background-insets: 0; " +
                              "-fx-padding: 0; " +
                              "-fx-border-width: 0;");

        postsListView.setCellFactory(lv -> new ListCell<String>() {
            private final Button viewButton = new Button("View");
            private final Button likeButton = new Button("♡");
            private final HBox postBox = new HBox(10);

            {
                postBox.prefWidthProperty().bind(lv.widthProperty().subtract(20));
                
                // Style the like button
                likeButton.setStyle("-fx-background-color: transparent; " +
                                  "-fx-text-fill: #FF69B4; " +
                                  "-fx-font-size: 18px; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-padding: 5 10; " +
                                  "-fx-min-width: 40px;");

                // Style the view button
                viewButton.setStyle("-fx-background-color: white; " +
                                  "-fx-text-fill: #666666; " +
                                  "-fx-border-color: #dee2e6; " +
                                  "-fx-border-radius: 15; " +
                                  "-fx-background-radius: 15; " +
                                  "-fx-padding: 5 15; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-font-size: 10px;");

                // Add hover effect for view button
                viewButton.setOnMouseEntered(e -> viewButton.setStyle("-fx-background-color: #f8f9fa; " +
                "-fx-text-fill: #666666; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 15; " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 5 15; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 10px;"));

viewButton.setOnMouseExited(e -> viewButton.setStyle("-fx-background-color: white; " +
               "-fx-text-fill: #666666; " +
               "-fx-border-color: #dee2e6; " +
               "-fx-border-radius: 15; " +
               "-fx-background-radius: 15; " +
               "-fx-padding: 5 15; " +
               "-fx-cursor: hand; " +
               "-fx-font-size: 10px;"));

                // Add view button click handler
                viewButton.setOnAction(e -> {
                    String item = getItem();
                    if (item != null) {
                        try {
                            // Load the post view FXML
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/post_view.fxml"));
                            Parent root = loader.load();
                            
                            // Get the controller and set the post data
                            PostViewController controller = loader.getController();
                            
                            // Parse the post data
                            String[] parts = item.split(":", 2);
                            String title = parts[0].trim();
                            String content = parts.length > 1 ? parts[1].trim() : "";
                            int postId = getPostIdFromTitle(title);
                            
                            // Set the post data in the controller
                            controller.setPostData(postId, title, content, postTimestamps.get(item));
                            
                            // Show in the same scene
                            Scene scene = postBox.getScene();
                            scene.setRoot(root);
                            
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Could not load post view.");
                            alert.showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Get post ID and check like state
                    String[] parts = item.split(":", 2);
                    String title = parts[0].trim();
                    int postId = getPostIdFromTitle(title);
                    int userId = getCurrentUserId();
                    
                    // Check if post is liked
                    try {
                        LikeService likeService = new LikeService();
                        boolean isLiked = likeService.isPostLikedByUser(postId, userId);
                        likeButton.setText(isLiked ? "♥" : "♡");
                        
                        // Update like button click handler with current state
                        likeButton.setOnAction(e -> {
                            try {
                                if (!isLiked) {
                                    likeService.ajouter(postId, userId);
                                    likeButton.setText("♥");
                                } else {
                                    likeService.supprimer(postId, userId);
                                    likeButton.setText("♡");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText(null);
                                alert.setContentText("Could not process like action.");
                                alert.showAndWait();
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    postBox.getChildren().clear();
                    postBox.setAlignment(Pos.CENTER_LEFT);
                    postBox.setPrefHeight(USE_COMPUTED_SIZE);
                    postBox.setMinHeight(USE_COMPUTED_SIZE);
                    postBox.setMaxHeight(USE_COMPUTED_SIZE);
                    
                    String randomPastelColor = pastelColors[random.nextInt(pastelColors.length)];
                    postBox.setStyle("-fx-background-color: white; " +
                                   "-fx-background-radius: 15; " +
                                   "-fx-padding: 15; " +
                                   "-fx-border-color: " + randomPastelColor + "; " +
                                   "-fx-border-width: 2; " +
                                   "-fx-border-radius: 15; " +
                                   "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");

                    String content = parts.length > 1 ? parts[1].trim() : "";

                    VBox contentBox = new VBox(8);
                    contentBox.prefWidthProperty().bind(postBox.widthProperty().subtract(150));
                    contentBox.setMinHeight(USE_COMPUTED_SIZE);

                    Label titleLabel = new Label(title);
                    titleLabel.setWrapText(true);
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

                    Label contentLabel = new Label(content);
                    contentLabel.setWrapText(true);
                    contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

                    LocalDateTime timestamp = postTimestamps.get(item);
                    Label timeLabel = new Label(formatTimestamp(timestamp));
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

                    contentBox.getChildren().addAll(titleLabel, contentLabel, timeLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    HBox buttonBox = new HBox(5);
                    buttonBox.setAlignment(Pos.CENTER_RIGHT);
                    buttonBox.getChildren().addAll(viewButton, likeButton);

                    postBox.getChildren().addAll(contentBox, spacer, buttonBox);
                    
                    // Set cell height to computed height
                    setGraphic(postBox);
                    setPrefHeight(USE_COMPUTED_SIZE);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 10 0;");
                }
            }
        });
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(timestamp, now);
        long hours = ChronoUnit.HOURS.between(timestamp, now);
        long days = ChronoUnit.DAYS.between(timestamp, now);

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        if (days < 7) return days + " day" + (days == 1 ? "" : "s") + " ago";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
        return timestamp.format(formatter);
    }

    @FXML
    protected void activateSearch() {
        if (!searchField.isFocused()) {
            searchField.requestFocus();
        }
    }

    @FXML
    private void onSearchKeyReleased() {
        String searchText = searchField.getText().toLowerCase();
        handleSearch(searchText);
    }

    @Override
    public void handleSearch(String searchText) {
        ArrayList<String> filteredPosts = new ArrayList<>();
        
        if (searchText.isEmpty()) {
            filteredPosts = originalPosts;
        } else {
            for (String post : originalPosts) {
                if (post.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredPosts.add(post);
                }
            }
        }
        
        postsListView.setItems(FXCollections.observableArrayList(filteredPosts));
    }

    @Override
    public void handleRefresh() {
        // Refresh the posts list
        postsListView.getItems().clear();
        postsListView.getItems().addAll(originalPosts);
    }

    @Override
    public void filterPostsByDate(LocalDate startDate, LocalDate endDate) {
        System.out.println("Filtering posts for dates: " + startDate + " to " + endDate);

        postsListView.getItems().clear();

        if (startDate == null && endDate == null) {
            // No filter, show all posts
            postsListView.getItems().addAll(originalPosts);
            return;
        }

        for (String post : originalPosts) {
            LocalDateTime postTime = postTimestamps.get(post);
            System.out.println("Post: " + post + " Time: " + postTime);

            if (postTime != null) {
                LocalDate postDate = postTime.toLocalDate();

                boolean matchesFilter = false;
                if (endDate == null) {
                    // Single date selection
                    matchesFilter = postDate.equals(startDate);
                } else {
                    // Date range selection
                    matchesFilter = !postDate.isBefore(startDate) && !postDate.isAfter(endDate);
                }

                if (matchesFilter) {
                    postsListView.getItems().add(post);
                    System.out.println("Added matching post: " + post);
                }
            }
        }
    }
}