package com.esprit.controllers;

import com.esprit.models.ChatBot;
import com.esprit.models.Post;
import com.esprit.models.User;
import com.esprit.tests.Eutopia;
import com.esprit.services.*;
import com.esprit.utils.DataSource;
import com.esprit.services.ChatService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AllPostsController extends ForumMainController {
    private Eutopia application;
    @FXML
    private ListView<Post> postsListView;
    @FXML
    private TextField searchField;
    @FXML
    private VBox calendarContainer;
    private CalendarController calendarController;
    
    private Map<String, Integer> postIdMap = new HashMap<>();
    private final Random random = new Random();
    private final String[] pastelColors = {
            "#FFB3BA", "#BAFFC9", "#BAE1FF", "#FFFFBA",
            "#FFB3F7", "#B3FFE5", "#B3BEFF", "#FFE5B3"
    };
    
    private ArrayList<Post> originalPosts = new ArrayList<>();
    private Map<String, LocalDateTime> postTimestamps = new HashMap<>();
    private static final int POSTS_PER_PAGE = 25;
    private int currentPage = 0;
    private int totalPages;
    @FXML private Pagination pagination;

    @FXML
    private ComboBox<String> searchFilterComboBox;
    private String currentSearchFilter = "Title";

    public void setApplication(Eutopia application) {
        this.application = application;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Initializing AllPostsController...");



            // Initialize search filter combo box
            if (searchFilterComboBox != null) {
                ObservableList<String> filterOptions = FXCollections.observableArrayList(
                    "Title", "Author", "Category"
                );
                searchFilterComboBox.setItems(filterOptions);
                searchFilterComboBox.setValue("Title");
                
                searchFilterComboBox.setOnAction(event -> {
                    currentSearchFilter = searchFilterComboBox.getValue();
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        handleSearch(searchField.getText());
                    }
                });
            }

            // Set up search field
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    handleSearch(newValue);
                });
                styleSearchField();
            }

            // Initialize ListView and Pagination
            if (postsListView != null && pagination != null) {
                setupListView();
                loadPosts();
                
                // Set up pagination
                int pageCount = (int) Math.ceil((double) originalPosts.size() / POSTS_PER_PAGE);
                pagination.setPageCount(pageCount);
                pagination.setCurrentPageIndex(0);
                pagination.setMaxPageIndicatorCount(7);
                
                // Add page change listener
                pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                    currentPage = newIndex.intValue();
                    updatePageContent();
                });
                
                // Initial page load
                updatePageContent();
            }
            
            // Initialize Calendar
            if (calendarContainer != null) {
                initializeCalendar();
            }

            System.out.println("AllPostsController initialization complete");
            
        } catch (Exception e) {
            System.err.println("Error initializing AllPostsController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPosts() throws SQLException {
        PostService postService = new PostService();
        List<Post> posts = postService.getAllPosts();
        originalPosts = new ArrayList<>(posts);

        // Clear existing maps
        postIdMap.clear();
        postTimestamps.clear();

        // Populate maps and ListView
        for (Post post : posts) {
            postIdMap.put(post.getTitle(), post.getId());
            postTimestamps.put(post.getTitle(), post.getCreatedAt());
        }

        postsListView.getItems().clear();
        postsListView.getItems().addAll(posts);

    }

    private void setupListView() {
        postsListView.setStyle("-fx-background-color: transparent; " +
                              "-fx-background-insets: 0; " +
                              "-fx-padding: 0; " +
                              "-fx-border-width: 0;");

        postsListView.setCellFactory(lv -> new ListCell<Post>() {
            private final Button viewButton = new Button("View");
            private final Button likeButton = new Button("♡");
            private final HBox postBox = new HBox(10);
            private final LikeService likeService = new LikeService();
            private boolean isLiked = false;

            {
                postBox.prefWidthProperty().bind(lv.widthProperty().subtract(20));
                
                // Style buttons
                styleButtons();
                
                // Add view button click handler
                setupViewButton();
                
                // Add like button click handler with current user ID
                likeButton.setOnAction(event -> {
                    Post post = getItem();
                    if (post != null) {
                        try {
                            int userId = getCurrentUserId(); // Now using the current user's ID
                            if (!isLiked) {
                                likeService.ajouter(post.getId(), userId);
                                isLiked = true;
                                likeButton.setText("♥");
                                likeButton.setStyle(likeButton.getStyle() + "-fx-text-fill: #FF69B4;");
                            } else {
                                likeService.supprimer(post.getId(), userId);
                                isLiked = false;
                                likeButton.setText("♡");
                                likeButton.setStyle(likeButton.getStyle() + "-fx-text-fill: #b0a8a0;");
                            }
                            // Update like count in the UI
                            updateLikeCount(post);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showError("Could not process like action.");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setupPostCell(post);
                    // Check if post is liked by current user
                    try {
                        int userId = getCurrentUserId(); // Using current user's ID
                        isLiked = likeService.isPostLikedByUser(post.getId(), userId);
                        updateLikeButton();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void updateLikeButton() {
                likeButton.setText(isLiked ? "♥" : "♡");
                likeButton.setStyle(likeButton.getStyle() + 
                                  "-fx-text-fill: " + (isLiked ? "#FF69B4" : "#b0a8a0") + ";");
            }

            private void updateLikeCount(Post post) throws SQLException {
                int likes = likeService.getLikesCount(post.getId());
                // Find and update the likes label in the post cell
                postBox.getChildren().stream()
                    .filter(node -> node instanceof VBox)
                    .map(node -> (VBox) node)
                    .flatMap(vbox -> vbox.getChildren().stream())
                    .filter(node -> node instanceof Label && ((Label) node).getText().contains("likes"))
                    .findFirst()
                    .ifPresent(label -> ((Label) label).setText(likes + " likes"));
            }

            private void styleButtons() {
                // Style like button
                likeButton.setStyle("-fx-background-color: transparent; " +
                                  "-fx-text-fill: #b0a8a0; " +
                                  "-fx-font-size: 18px; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-padding: 5 10; " +
                                  "-fx-min-width: 40px;");

                // Style view button
                viewButton.setStyle("-fx-background-color: white; " +
                                  "-fx-text-fill: #666666; " +
                                  "-fx-border-color: #dee2e6; " +
                                  "-fx-border-radius: 15; " +
                                  "-fx-background-radius: 15; " +
                                  "-fx-padding: 5 15; " +
                                  "-fx-cursor: hand; " +
                                  "-fx-font-size: 10px;");

                // Add hover effects
                addButtonHoverEffects();
            }

            private void setupPostCell(Post post) {
                // Setup cell content
                postBox.getChildren().clear();
                postBox.setAlignment(Pos.CENTER_LEFT);
                
                String randomPastelColor = pastelColors[random.nextInt(pastelColors.length)];
                postBox.setStyle("-fx-background-color: white; " +
                               "-fx-background-radius: 15; " +
                               "-fx-padding: 15; " +
                               "-fx-border-color: " + randomPastelColor + "; " +
                               "-fx-border-width: 2; " +
                               "-fx-border-radius: 15; " +
                               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);");

                // Create and style content
                VBox contentBox = createContentBox(post);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox buttonBox = new HBox(5, viewButton, likeButton);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);

                postBox.getChildren().addAll(contentBox, spacer, buttonBox);
                setGraphic(postBox);
            }

            private VBox createContentBox(Post post) {
                VBox contentBox = new VBox(8);
                contentBox.prefWidthProperty().bind(postBox.widthProperty().subtract(150));

                Label titleLabel = new Label(post.getTitle());
                titleLabel.setWrapText(true);
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333333;");

                Label contentLabel = new Label(post.getContent());
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

                Label timeLabel = new Label(formatTimestamp(post.getCreatedAt()));
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

                contentBox.getChildren().addAll(titleLabel, contentLabel, timeLabel);
                return contentBox;
            }

            private void setupViewButton() {
                viewButton.setOnAction(event -> {
                    Post post = getItem();
                    if (post != null) {
                        try {
                            URL url = getClass().getResource("/post_view.fxml");
                            if (url == null) {
                                throw new IOException("Cannot find post_view.fxml");
                            }

                            FXMLLoader loader = new FXMLLoader(url);
                            Parent root = loader.load();
                            
                            PostViewController controller = loader.getController();
                            controller.setPostData(
                                post.getId(),
                                post.getTitle(),
                                post.getContent(),
                                post.getCreatedAt()
                            );
                            
                            Scene scene = viewButton.getScene();
                            
                            // Find the navbar in the scene hierarchy and set forum button as active
                            Parent currentRoot = scene.getRoot();
                            if (currentRoot instanceof BorderPane) {
                                BorderPane bp = (BorderPane) currentRoot;
                                if (bp.getLeft() instanceof VBox) {
                                    VBox navbar = (VBox) bp.getLeft();
                                    for (javafx.scene.Node child : navbar.getChildren()) {
                                        if (child instanceof Parent) {
                                            Parent parent = (Parent) child;
                                            Object navController = parent.getUserData();
                                            if (navController instanceof NavbarController) {
                                                NavbarController navbarController = (NavbarController) navController;
                                                navbarController.setForumButtonActive();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            scene.setRoot(root);
                            
                        } catch (IOException e) {
                            System.err.println("Error loading post view: " + e.getMessage());
                            e.printStackTrace();
                            showError("Could not load post details: " + e.getMessage());
                        }
                    }
                });
            }

            private void addButtonHoverEffects() {
                // Hover effect for view button
                viewButton.setOnMouseEntered(e -> 
                    viewButton.setStyle(viewButton.getStyle() + 
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 2);"
                    )
                );
                
                viewButton.setOnMouseExited(e -> 
                    viewButton.setStyle(viewButton.getStyle() + 
                        "-fx-background-color: white; " +
                        "-fx-effect: none;"
                    )
                );

                // Hover effect for like button
                likeButton.setOnMouseEntered(e -> 
                    likeButton.setStyle(likeButton.getStyle() + 
                        "-fx-text-fill: #ff1493;"
                    )
                );
                
                likeButton.setOnMouseExited(e -> 
                    likeButton.setStyle(likeButton.getStyle() + 
                        "-fx-text-fill: #FF69B4;"
                    )
                );
            }
        });
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    @Override
    public void handleSearch(String searchText) {
        System.out.println("Searching for: " + searchText + " by " + currentSearchFilter);

        if (searchText == null || searchText.trim().isEmpty()) {
            // If search is empty, show all posts with pagination
            updatePageContent();
            System.out.println("Search cleared - showing all posts");
            return;
        }

        // Convert search text to lowercase for case-insensitive search
        String searchLower = searchText.toLowerCase().trim();

        // Filter posts based on selected filter
        List<Post> filteredPosts = originalPosts.stream()
            .filter(post -> {
                try {
                    switch (currentSearchFilter) {
                        case "Title":
                            return post.getTitle().toLowerCase().contains(searchLower);
                        case "Author":
                            return post.getAuthor().toLowerCase().contains(searchLower);
                        case "Category":
                            String categoryName = getCategoryName(post.getCategoryId());
                            return categoryName != null &&
                                   categoryName.toLowerCase().contains(searchLower);
                        default:
                            return false;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            })
            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
            .collect(Collectors.toList());

        // Update pagination for filtered results
        int pageCount = (int) Math.ceil((double) filteredPosts.size() / POSTS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);

        // Show first page of filtered results
        postsListView.setItems(FXCollections.observableArrayList(
            filteredPosts.subList(0, Math.min(POSTS_PER_PAGE, filteredPosts.size()))
        ));

        System.out.println("Found " + filteredPosts.size() + " matching posts");

    }

    private String getCategoryName(Integer categoryId) throws SQLException {
        if (categoryId == null) return null;
        
        String sql = "SELECT name FROM categoriesposts WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }

    @FXML
    private void onSearchKeyReleased(KeyEvent event) {
        // Get the search text
        String searchText = searchField.getText();
        handleSearch(searchText);
    }

    @FXML
    private void activateSearch(MouseEvent event) {
        // Focus on search field when search icon is clicked
        searchField.requestFocus();
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

        for (Post post : originalPosts) {
            LocalDateTime postTime = postTimestamps.get(post.getTitle());
            System.out.println("Post: " + post.getTitle() + " Time: " + postTime);

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
                    System.out.println("Added matching post: " + post.getTitle());
                }
            }
        }
    }

    public void setPosts(ArrayList<String> posts, String title) {
        if (postsListView == null) {
            System.err.println("Error: postsListView is null in setPosts");
            return;
        }

        try {
            // Convert string posts to Post objects
            List<Post> postObjects = new ArrayList<>();
            for (String postString : posts) {
                String[] parts = postString.split(": ", 2);
                if (parts.length == 2) {
                    Post post = new Post();
                    post.setTitle(parts[0]);
                    post.setContent(parts[1]);
                    post.setCreatedAt(LocalDateTime.now()); // You might want to modify this based on your needs
                    postObjects.add(post);
                }
            }

            // Update the ListView
            originalPosts = new ArrayList<>(postObjects);
            postsListView.getItems().clear();
            postsListView.getItems().addAll(postObjects);
            
            // Update maps
            postIdMap.clear();
            postTimestamps.clear();
            for (Post post : postObjects) {
                postIdMap.put(post.getTitle(), post.getId());
                postTimestamps.put(post.getTitle(), post.getCreatedAt());
            }

        } catch (Exception e) {
            System.err.println("Error setting posts: " + e.getMessage());
            e.printStackTrace();
            showError("Could not set posts: " + e.getMessage());
        }
    }


    private void styleSearchField() {
        searchField.setPromptText("Search posts...");
        searchField.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 15; " +
                "-fx-font-size: 13;");

        // Add focus effects
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                searchField.setStyle(searchField.getStyle() + 
                    "-fx-border-color: #007bff; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,123,255,0.25), 4, 0, 0, 0);");
            } else {
                searchField.setStyle(searchField.getStyle() + 
                    "-fx-border-color: #dee2e6; " +
                    "-fx-effect: none;");
            }
        });
    }

    private void updatePageContent() {
        int fromIndex = currentPage * POSTS_PER_PAGE;
        int toIndex = Math.min(fromIndex + POSTS_PER_PAGE, originalPosts.size());
        
        if (fromIndex > originalPosts.size()) {
            postsListView.setItems(FXCollections.observableArrayList());
            return;
        }
        
        List<Post> pageContent = originalPosts.subList(fromIndex, toIndex);
        postsListView.setItems(FXCollections.observableArrayList(pageContent));
        
        System.out.println("Showing posts " + (fromIndex + 1) + " to " + toIndex + 
                          " of " + originalPosts.size());
    }

    @Override
    protected void initializeCalendar() {
        try {
            URL url = getClass().getResource("/calendar.fxml");
            if (url == null) {
                throw new IOException("Cannot find calendar.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent calendarRoot = loader.load();
            calendarController = loader.getController();
            calendarController.setMainController(this);
            calendarContainer.getChildren().add(calendarRoot);

        } catch (IOException e) {
            System.err.println("Error loading calendar: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load calendar: " + e.getMessage());
        }
    }
}