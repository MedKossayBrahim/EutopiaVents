package com.esprit.controllers;

import com.esprit.models.ChatBot;
import com.esprit.models.Post;
import com.esprit.services.LikeService;
import com.esprit.services.PostService;
import com.esprit.tests.Eutopia;
import com.esprit.utils.DataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.io.File;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.esprit.services.ChatService;
import com.esprit.services.ChatService.UserChatMessage;
import javafx.application.Platform;

public class ForumMainController implements SearchableController, Initializable {

    @FXML
    private ListView<Post> latestUpdatesList;

    @FXML
    private ListView<Post> pinnedPostsList;

    @FXML
    private TextField searchField;

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox calendarContainer;
    private CalendarController calendarController;

    private Eutopia application; // Reference to the application
    private static int postCounter = 1; // Counter for post IDs

    private ArrayList<String> allLatestUpdates = new ArrayList<>(); // Use ArrayList for dynamic updates
    private final Map<String, Color> postBorderColors = new HashMap<>() {{
        put("Pink", Color.web("#FFB5C5"));
        put("Green", Color.web("#90EE90"));
        put("Orange", Color.web("#FFA07A"));
        put("Blue", Color.web("#87CEEB"));
        put("Violet", Color.web("#DDA0DD"));
        put("Beige", Color.web("#F5F5DC"));
    }};
    private Map<String, LocalDateTime> postTimestamps = new HashMap<>();

    // Change to use int instead of Long
    private Map<String, Integer> postIdMap = new HashMap<>();

    private final Random random = new Random();

    @FXML private VBox chatArea;
    @FXML private TextField userInput;
    private ChatBot chatBot;

    private final PostService postService = new PostService();

    @FXML
    private Label welcomeUsernameLabel;

    private List<Post> allPosts = new ArrayList<>();

    @FXML
    private ComboBox<String> searchFilterComboBox;
    private String currentSearchFilter = "Title";

    private ChatService chatService;

    private VBox messagesContainer; // Add this as a class field

    public void setApplication(Eutopia app) {
        this.application = app; // Set the application instance
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Initializing ForumMainController...");
            
            // Initialize your components
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
            }
            
            // Only set up ListViews if they exist
            if (latestUpdatesList != null) {
                setupListView(latestUpdatesList);
                loadLatestUpdates();
            } else {
                System.out.println("Warning: latestUpdatesList is null");
            }
            
            if (pinnedPostsList != null) {
                setupListView(pinnedPostsList);
                loadPinnedPosts();
            } else {
                System.out.println("Warning: pinnedPostsList is null");
            }
            
            // Initialize Calendar and Chat if needed
            initializeCalendar();
            initializeChat();
            
            // Set welcome message if label exists
            if (welcomeUsernameLabel != null) {
                welcomeUsernameLabel.setText("Welcome to Forum");
            }
            
            System.out.println("ForumMainController initialization complete");
            
        } catch (Exception e) {
            System.err.println("Error initializing ForumMainController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void setupListView(ListView<Post> listView) {
        if (listView == null) {
            System.out.println("Warning: ListView is null, skipping setup");
            return;
        }

        System.out.println("Setting up ListView");

        listView.setFixedCellSize(160);
        listView.setStyle("-fx-background-color: transparent; "
                + "-fx-background-insets: 0; "
                + "-fx-padding: 0;");

        // Hide scrollbars
        listView.setMouseTransparent(false);
        listView.setFocusTraversable(false);

        // Add CSS to hide scrollbars
        listView.getStylesheets().add("data:text/css,"
                + ".list-view .scroll-bar:vertical { -fx-scale-x: 0; }"
                + ".list-view .scroll-bar:horizontal { -fx-scale-y: 0; }"
                + ".list-view { -fx-background-insets: 0; padding: 0; }");

        listView.setCellFactory(lv -> new ListCell<Post>() {
            private final VBox contentBox = new VBox(8);
            private final HBox headerBox = new HBox(10);
            private final HBox bottomBox = new HBox(10);
            private final Label titleLabel = new Label();
            private final Label authorLabel = new Label();
            private final Label contentLabel = new Label();
            private final Label timeLabel = new Label();
            private final Button viewButton = new Button("View Post");
            private final Region spacer = new Region();

            {
                // Style labels with bigger fonts
                titleLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 20; -fx-font-family: 'System Bold';");
                authorLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14; -fx-font-weight: 600;");
                contentLabel.setStyle("-fx-font-size: 15;");
                contentLabel.setWrapText(true);
                contentLabel.setMaxWidth(600); // Increase max width for content
                timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11;");

                // Add title and author to header box with more spacing
                headerBox.setSpacing(15); // Increase spacing between title and author
                headerBox.getChildren().addAll(titleLabel, authorLabel);

                // Style view button
                viewButton.setStyle("-fx-background-color: white; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 16; " +
                        "-fx-font-size: 13; " +
                        "-fx-cursor: hand;");

                // Add hover effect
                viewButton.setOnMouseEntered(e -> 
                    viewButton.setStyle("-fx-background-color: #f8f9fa; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 16; " +
                        "-fx-font-size: 13; " +
                        "-fx-cursor: hand;"));
                viewButton.setOnMouseExited(e -> 
                    viewButton.setStyle("-fx-background-color: white; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-border-color: #dee2e6; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 8 16; " +
                        "-fx-font-size: 13; " +
                        "-fx-cursor: hand;"));

                // Configure bottom box with spacer for right alignment
                HBox.setHgrow(spacer, Priority.ALWAYS);
                bottomBox.setAlignment(Pos.CENTER_LEFT);
                bottomBox.setPadding(new Insets(10, 0, 0, 0)); // Add top padding to bottom box
                bottomBox.getChildren().addAll(timeLabel, spacer, viewButton);

                // Add all components to the content box with increased padding
                contentBox.getChildren().addAll(headerBox, contentLabel, bottomBox);
                contentBox.setPadding(new Insets(20, 30, 20, 30)); // Increase horizontal padding
                
                // Set minimum width for content box
                contentBox.setMinWidth(700); // Increase minimum width
                contentBox.setPrefWidth(700); // Set preferred width
            }

            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);

                if (empty || post == null) {
                    setGraphic(null);
                } else {
                    // Get random border color
                    List<Color> colors = new ArrayList<>(postBorderColors.values());
                    Color borderColor = colors.get(random.nextInt(colors.size()));

                    // Setup the layout with random border color
                    contentBox.setStyle(String.format(
                            "-fx-background-color: white; " +
                                    "-fx-background-radius: 15; " +
                                    "-fx-padding: 20; " +
                                    "-fx-border-color: %s; " +
                                    "-fx-border-width: 2; " +
                                    "-fx-border-radius: 15; " +
                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);",
                            borderColor.toString().replace("0x", "#")
                    ));

                    titleLabel.setText(post.getTitle());
                    authorLabel.setText("by " + post.getAuthor());
                    contentLabel.setText(post.getContent());
                    timeLabel.setText(formatTimestamp(post.getCreatedAt()));

                    // Add view button click handler
                    viewButton.setOnAction(event -> {
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
                            scene.setRoot(root);

                        } catch (IOException e) {
                            System.err.println("Error loading post view: " + e.getMessage());
                            e.printStackTrace();
                            showError("Could not load post view: " + e.getMessage());
                        }
                    });

                    setGraphic(contentBox);
                }
            }
        });
    }

    @FXML
    private void onAddPostClick() {
        try {
            URL url = getClass().getResource("/post-dialog.fxml");
            if (url == null) {
                throw new IOException("Cannot find post-dialog.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            PostDialogController controller = loader.getController();
            controller.setPostService(postService);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Post");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setMinWidth(400);
            dialogStage.setMinHeight(300);
            dialogStage.initOwner((Stage) latestUpdatesList.getScene().getWindow());
            dialogStage.showAndWait();

            loadLatestUpdates();
            loadPinnedPosts();

        } catch (IOException e) {
            System.err.println("Error loading post dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load post dialog: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void addPost(String title, String content) {
        System.out.println("Adding post - Title: " + title + ", Content: " + content);
        String fullPost = title + ": " + content;
        allLatestUpdates.add(0, fullPost);
        postTimestamps.put(fullPost, LocalDateTime.now());

        // Update the ListView
        displayLimitedLatestUpdates(3);
    }

    private void loadLatestUpdates() {
        try {
            // Check if ListView is initialized
            if (latestUpdatesList == null) {
                System.out.println("Warning: latestUpdatesList is null, skipping load");
                return;
            }

            // Fetch posts from database
            List<Post> allPosts = postService.getAllPosts();
            System.out.println("Fetched " + allPosts.size() + " posts from database");

            // Store all posts for filtering
            this.allPosts = new ArrayList<>(allPosts);

            // Store timestamps for ALL posts, not just latest ones
            postTimestamps.clear(); // Clear existing timestamps
            for (Post post : allPosts) {
                postTimestamps.put(post.getTitle(), post.getCreatedAt());
                System.out.println("Stored timestamp for post: " + post.getTitle() +
                        " Date: " + post.getCreatedAt().toLocalDate());
            }

            // Sort and limit for display
            List<Post> latestPosts = allPosts.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(3)
                    .collect(Collectors.toList());

            // Update display
            latestUpdatesList.getItems().clear();
            latestUpdatesList.getItems().addAll(latestPosts);
            latestUpdatesList.refresh();

        } catch (SQLException e) {
            System.err.println("Error loading latest updates: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load latest updates: " + e.getMessage());
        }
    }

    private void loadPinnedPosts() {
        try {
            // Get pinned posts directly from service
            List<Post> pinnedPosts = postService.getPinnedPosts();  // Use the dedicated method

            // Sort by creation time and limit to 3 posts
            List<Post> limitedPinnedPosts = pinnedPosts.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(3)
                    .collect(Collectors.toList());

            // Update UI with pinned posts
            pinnedPostsList.getItems().clear();
            pinnedPostsList.getItems().addAll(limitedPinnedPosts);

            // Debug print
            System.out.println("Loaded " + limitedPinnedPosts.size() + " pinned posts");

            // Force refresh
            pinnedPostsList.refresh();

        } catch (SQLException e) {
            System.err.println("Error loading pinned posts: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load pinned posts: " + e.getMessage());
        }
    }

    // Make sure PostService has this method to get pinned posts
    public List<Post> getPinnedPosts() throws SQLException {
        String sql = "SELECT p.*, u.username FROM posts p " +
                "LEFT JOIN users u ON p.user_id = u.userID " +
                "WHERE p.is_pinned = 1 " +
                "ORDER BY p.created_at DESC";

        List<Post> pinnedPosts = new ArrayList<>();
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                post.setAuthor(rs.getString("username"));
                post.setPinned(rs.getBoolean("is_pinned"));
                pinnedPosts.add(post);
            }
        }
        return pinnedPosts;
    }

    @FXML
    public void handleSearch(String searchText) {
        try {
            System.out.println("Searching for: " + searchText + " by " + currentSearchFilter);
            
            // Clear current items
            latestUpdatesList.getItems().clear();
            
            if (searchText == null || searchText.trim().isEmpty()) {
                // If search is empty, show latest posts
                List<Post> latestPosts = postService.getAllPosts().stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(3)
                    .collect(Collectors.toList());
                    
                latestUpdatesList.getItems().addAll(latestPosts);
                System.out.println("Search cleared - showing latest posts");
                return;
            }
            
            // Convert search text to lowercase for case-insensitive search
            String searchLower = searchText.toLowerCase().trim();
            
            // Get all posts with their related data
            List<Post> allPosts = postService.getAllPosts();
            
            // Filter posts based on selected filter
            List<Post> filteredPosts = allPosts.stream()
                .filter(post -> {
                    try {
                        switch (currentSearchFilter) {
                            case "Title":
                                return post.getTitle().toLowerCase().contains(searchLower);
                            case "Author":
                                return post.getAuthor().toLowerCase().contains(searchLower);
                            case "Category":
                                // Get category name from database using category_id
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
            
            // Update ListView with filtered results
            latestUpdatesList.getItems().addAll(filteredPosts);
            
            System.out.println("Found " + filteredPosts.size() + " matching posts");
            
        } catch (SQLException e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            showError("Search failed: " + e.getMessage());
        }
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

    private void displayLimitedLatestUpdates(int limit) {
        try {
            List<Post> allPosts = postService.getAllPosts();

            // Sort posts by creation time (newest first) and take only the latest 3
            List<Post> limitedPosts = allPosts.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(3)
                    .collect(Collectors.toList());

            latestUpdatesList.getItems().clear();
            latestUpdatesList.getItems().addAll(limitedPosts);
            latestUpdatesList.refresh();

            System.out.println("Displaying " + limitedPosts.size() + " latest posts");

        } catch (SQLException e) {
            System.err.println("Error loading limited updates: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load updates: " + e.getMessage());
        }
    }

    @FXML
    protected void onViewAllPostsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/all-posts-page.fxml"));
            Parent root = loader.load();

            AllPostsController controller = loader.getController();
            controller.setApplication(application);

            // Convert Post objects to strings for display
            ArrayList<String> postsList = new ArrayList<>();
            for (Post post : pinnedPostsList.getItems()) {
                postsList.add(post.getTitle() + ": " + post.getContent());
            }
            controller.setPosts(postsList, "Latest Updates");

            // Get the current scene and set the new content
            Scene scene = pinnedPostsList.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load all posts view");
        }
    }

    public void filterPostsByDate(LocalDate startDate, LocalDate endDate) {
        System.out.println("\nFiltering posts for date(s):");
        System.out.println("Start date: " + startDate);
        System.out.println("End date: " + endDate);

        try {
            if (startDate == null && endDate == null) {
                System.out.println("Clearing filter - resetting to latest posts");
                // First clear the list
                latestUpdatesList.getItems().clear();

                // Get all posts and sort by creation date
                List<Post> sortedPosts = postService.getAllPosts().stream()
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .limit(3)
                        .collect(Collectors.toList());

                // Update the ListView with the latest 3 posts
                latestUpdatesList.getItems().addAll(sortedPosts);
                latestUpdatesList.refresh();

                System.out.println("Reset complete - showing " + sortedPosts.size() + " latest posts");
                return;
            }

            latestUpdatesList.getItems().clear();
            List<Post> filteredPosts = new ArrayList<>();

            for (Post post : allPosts) {
                LocalDateTime postDateTime = post.getCreatedAt();
                LocalDate postDate = postDateTime.toLocalDate();

                System.out.println("\nChecking post: " + post.getTitle());
                System.out.println("Post date: " + postDate);

                boolean matchesFilter = false;
                if (endDate == null) {
                    // Single date selection
                    matchesFilter = postDate.isEqual(startDate);
                    System.out.println("Single date comparison: " + matchesFilter);
                } else {
                    // Date range selection
                    matchesFilter = !postDate.isBefore(startDate) && !postDate.isAfter(endDate);
                    System.out.println("Date range comparison: " + matchesFilter);
                }

                if (matchesFilter) {
                    filteredPosts.add(post);
                    System.out.println("Added matching post: " + post.getTitle());
                }
            }

            // Sort filtered posts by date (newest first)
            filteredPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

            // Update the ListView
            latestUpdatesList.getItems().addAll(filteredPosts);
            latestUpdatesList.refresh();

            System.out.println("Total matching posts: " + filteredPosts.size());

        } catch (Exception e) {
            System.err.println("Error filtering posts: " + e.getMessage());
            e.printStackTrace();
            showError("Could not filter posts: " + e.getMessage());
        }
    }

    private void resetToLatestPosts() {
        try {
            System.out.println("Resetting to latest posts");

            // Clear the current list
            latestUpdatesList.getItems().clear();

            // Get fresh data from database
            List<Post> allPosts = postService.getAllPosts();

            // Sort by date and get latest 3
            List<Post> latestPosts = allPosts.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .limit(3)
                    .collect(Collectors.toList());

            // Update the ListView
            latestUpdatesList.getItems().addAll(latestPosts);
            latestUpdatesList.refresh();

            System.out.println("Reset complete - showing " + latestPosts.size() + " latest posts");

        } catch (SQLException e) {
            System.err.println("Error resetting posts: " + e.getMessage());
            e.printStackTrace();
            showError("Could not reset to latest posts: " + e.getMessage());
        }
    }

    // Add NoSelectionModel inner class
    private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.observableArrayList();
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.observableArrayList();
        }

        @Override
        public void selectIndices(int index, int... indices) {}

        @Override
        public void selectAll() {}

        @Override
        public void selectFirst() {}

        @Override
        public void selectLast() {}

        @Override
        public void clearAndSelect(int index) {}

        @Override
        public void select(int index) {}

        @Override
        public void select(T obj) {}

        @Override
        public void clearSelection(int index) {}

        @Override
        public void clearSelection() {}

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {}

        @Override
        public void selectNext() {}
    }

    private void addPostWithDate(String title, String content, LocalDateTime timestamp) {
        try {
            Post newPost = new Post();
            newPost.setTitle(title);
            newPost.setContent(content);
            newPost.setCreatedAt(timestamp);
            newPost.setAuthor("System"); // Default author for sample posts

            // Save to database
            postService.ajouter(newPost);

            // Add to local list
            String postString = title + ": " + content;
            allLatestUpdates.add(postString);

            // Refresh the display
            loadLatestUpdates();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error adding post");
        }
    }

    // If you're adding posts through a form or other method, make sure to use this:
    public void addNewPost(String title, String content) {
        String post = title + ": " + content;
        allLatestUpdates.add(0, post);
        postTimestamps.put(post, LocalDateTime.now());

        // Get random color from postBorderColors instead of borderColors
        List<Color> colors = new ArrayList<>(postBorderColors.values());
        Color borderColor = colors.get(random.nextInt(colors.size()));
        postBorderColors.put(post, borderColor);

        // Refresh the display
        displayLimitedLatestUpdates(3);
    }

    private void styleViewAllButton() {
        Button viewAllUpdatesButton = (Button) rootPane.lookup("#viewAllUpdatesButton");
        Button viewAllPinnedButton = (Button) rootPane.lookup("#viewAllPinnedButton");

        String normalStyle = "-fx-background-color: white; " +
                "-fx-text-fill: #666666; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 16; " +
                "-fx-font-size: 13; " +
                "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: #f8f9fa; " +
                "-fx-text-fill: #666666; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-radius: 20; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 16; " +
                "-fx-font-size: 13; " +
                "-fx-cursor: hand;";

        // Style both buttons
        for (Button button : new Button[]{viewAllUpdatesButton, viewAllPinnedButton}) {
            if (button != null) {
                button.setStyle(normalStyle);

                // Add hover effect
                button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
                button.setOnMouseExited(e -> button.setStyle(normalStyle));
            }
        }
    }

    @FXML
    private void onViewAllUpdatesClick() {
        try {
            URL url = getClass().getResource("/all-posts-page.fxml");
            if (url == null) {
                throw new IOException("Cannot find all-posts-page.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AllPostsController controller = loader.getController();
            controller.setApplication(application);

            Scene scene = latestUpdatesList.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load all posts page: " + e.getMessage());
        }
    }

    @FXML
    private void onViewAllPinnedClick() {
        try {
            URL url = getClass().getResource("/all-posts-page.fxml");
            if (url == null) {
                throw new IOException("Cannot find all-posts-page.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            AllPostsController controller = loader.getController();
            controller.setApplication(application);

            List<Post> allPinnedPosts = postService.getPinnedPosts().stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());

            ArrayList<String> postsList = new ArrayList<>();
            for (Post post : allPinnedPosts) {
                postsList.add(post.getTitle() + ": " + post.getContent());
            }
            controller.setPosts(postsList, "Pinned Posts");

            Scene scene = pinnedPostsList.getScene();
            scene.setRoot(root);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showError("Could not load all posts page: " + e.getMessage());
        }
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(timestamp, now);
        long hours = ChronoUnit.HOURS.between(timestamp, now);
        long days = ChronoUnit.DAYS.between(timestamp, now);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
            return timestamp.format(formatter);
        }
    }

    // Modify your method where you load posts to store the IDs
    private void loadPosts() {
        String sql = "SELECT id, title FROM posts";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                postIdMap.put(title, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getPostIdFromTitle(String title) {
        // Get the actual post ID from the map
        return postIdMap.getOrDefault(title, 0);  // Return 0 instead of null
    }

    private int getCurrentUserId() {
        return 1;  // Just use a dummy user ID
    }

    @FXML
    private void handleShowAllPosts() {
        try {
            URL url = getClass().getResource("/all-posts-page.fxml");
            if (url == null) {
                throw new IOException("Cannot find all-posts-page.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("All Posts");
            stage.setScene(new Scene(root));

            AllPostsController controller = loader.getController();
            controller.setApplication(application);

            // Convert Post objects to strings
            ArrayList<String> postsList = new ArrayList<>();
            for (Post post : pinnedPostsList.getItems()) {
                postsList.add(post.getTitle() + ": " + post.getContent());
            }
            controller.setPosts(postsList, "Latest Updates");

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load all posts view");
        }
    }

    private String getUsernameById(int userId) {
        String sql = "SELECT userName FROM users WHERE userID = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("userName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Guest";
    }

    private void addPostWithDate(String title, String content, String author, String category) {
        Post post = new Post(title, content, author);
        post.setCategoryId(getCategoryIdByName(category));

        try {
            postService.ajouter(post);
            loadLatestUpdates();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating post: " + e.getMessage());
        }
    }

    // Add this helper method to get category ID from name
    private Integer getCategoryIdByName(String categoryName) {
        String sql = "SELECT id FROM categoriesposts WHERE name = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void initializeCalendar() {
        try {
            if (calendarContainer == null) {
                System.err.println("Calendar container is null!");
                return;
            }

            URL url = getClass().getResource("/calendar.fxml");
            if (url == null) {
                throw new IOException("Cannot find calendar.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent calendarView = loader.load();
            
            final CalendarController controller = loader.getController();
            this.calendarController = controller;

            controller.setOnDateSelected((startDate, endDate) -> {
                filterPostsByDate(startDate, endDate);
            });

            calendarContainer.getChildren().clear();
            calendarContainer.getChildren().add(calendarView);

            // Make sure the container is visible
            calendarContainer.setVisible(true);
            calendarContainer.setManaged(true);

            // Set calendar container styling
            calendarContainer.setStyle("-fx-background-color: #faf6f3; " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 15; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

            // Adjust size for the calendar container
            calendarContainer.setMinHeight(350);
            calendarContainer.setPrefHeight(350);
            calendarContainer.setMaxHeight(350);

            System.out.println("Calendar initialized successfully");

        } catch (Exception e) {
            System.err.println("Error loading calendar: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load calendar: " + e.getMessage());
        }
    }

    private void initializeChat() {
        try {
            if (chatArea == null) {
                System.out.println("Chat area is null, skipping chat initialization");
                return;
            }

            // Initialize chat service first
            chatService = new ChatService();
            System.out.println("Chat service initialized");

            // Clear existing chat messages
            chatArea.getChildren().clear();

            // Create title label
            Label titleLabel = new Label("Event Assistant");
            titleLabel.setStyle("-fx-font-size: 18px; " +
                              "-fx-font-weight: bold; " +
                              "-fx-text-fill: #2c3e50; " +
                              "-fx-padding: 0 0 10 0;"); // Add some padding below the title
            titleLabel.setAlignment(Pos.CENTER);
            titleLabel.setPrefWidth(400); // Match chat area width

            // Style the chat area with transparent background
            chatArea.setStyle("-fx-background-color: transparent; " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 15; " +
                    "-fx-spacing: 10;");
            chatArea.setPrefWidth(400);
            chatArea.setMaxWidth(400);
            chatArea.setPrefHeight(450);
            chatArea.setMinHeight(450);
            chatArea.setMaxHeight(450);

            // Create a VBox to hold title and chat area
            VBox chatContainer = new VBox(5); // 5px spacing between elements
            chatContainer.setAlignment(Pos.TOP_CENTER);
            chatContainer.getChildren().add(titleLabel);

            // Create messages container with scroll capability
            ScrollPane scrollPane = new ScrollPane();
            messagesContainer = new VBox(10);
            messagesContainer.setStyle("-fx-padding: 10; -fx-background-color: transparent;");
            
            scrollPane.setContent(messagesContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setPrefWidth(400);
            scrollPane.setMaxWidth(400);
            scrollPane.setPrefHeight(380);
            scrollPane.setMaxHeight(380);
            scrollPane.setStyle("-fx-background: transparent; " +
                              "-fx-background-color: transparent; " +
                              "-fx-control-inner-background: transparent;");
            
            // Create input area with adjusted spacing
            HBox inputArea = new HBox(10);
            inputArea.setAlignment(Pos.CENTER);
            inputArea.setPadding(new Insets(10, 0, 0, 0));
            inputArea.setMaxWidth(400);
            inputArea.setPrefHeight(40);
            inputArea.setStyle("-fx-background-color: transparent;");
            
            // Create text input with reduced width
            TextField messageInput = new TextField();
            messageInput.setPromptText("Type a message...");
            messageInput.setPrefWidth(250);  // Reduced from 300
            messageInput.setMaxWidth(250);   // Reduced from 300
            messageInput.setStyle("-fx-background-radius: 20; " +
                    "-fx-padding: 8 15; " +
                    "-fx-font-size: 13px;");
            
            // Create send button with fixed width
            Button sendButton = new Button("Send");
            sendButton.setPrefWidth(80);     // Fixed width for button
            sendButton.setMaxWidth(80);      // Fixed width for button
            sendButton.setStyle("-fx-background-color: #007bff; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 20; " +
                    "-fx-padding: 8 15; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 13px;");
            
            // Add hover effect to send button
            sendButton.setOnMouseEntered(e -> 
                sendButton.setStyle(sendButton.getStyle().replace("#007bff", "#0056b3")));
            sendButton.setOnMouseExited(e -> 
                sendButton.setStyle(sendButton.getStyle().replace("#0056b3", "#007bff")));
            
            // Add send functionality
            EventHandler<ActionEvent> sendMessage = e -> {
                String message = messageInput.getText().trim();
                if (!message.isEmpty()) {
                    System.out.println("Sending message: " + message);
                    
                    // Add user message to chat
                    UserChatMessage userChatMessage = new UserChatMessage(
                        "User", // Replace with actual username when available
                        message,
                        LocalDateTime.now()
                    );
                    addMessageToChat(userChatMessage);
                    
                    // Process message and get bot response
                    try {
                        UserChatMessage botResponse = chatService.processMessage(message);
                        System.out.println("Bot response received: " + botResponse.getContent());
                        addMessageToChat(botResponse);
                    } catch (Exception ex) {
                        System.err.println("Error processing message: " + ex.getMessage());
                        ex.printStackTrace();
                        UserChatMessage errorMessage = new UserChatMessage(
                            "System",
                            "Sorry, there was an error processing your message.",
                            LocalDateTime.now()
                        );
                        addMessageToChat(errorMessage);
                    }
                    
                    messageInput.clear();
                    
                    // Scroll to bottom after new message
                    Platform.runLater(() -> {
                        scrollPane.setVvalue(1.0);
                    });
                }
            };
            
            // Connect send button and enter key to send message
            sendButton.setOnAction(sendMessage);
            messageInput.setOnAction(sendMessage);
            
            // Set input field to take remaining space
            HBox.setHgrow(messageInput, javafx.scene.layout.Priority.ALWAYS);
            
            // Add components to input area
            inputArea.getChildren().addAll(messageInput, sendButton);
            
            // Add all components to chat container instead of chat area directly
            chatContainer.getChildren().addAll(scrollPane, inputArea);
            
            // Add chat container to chat area
            chatArea.getChildren().add(chatContainer);

            // Add welcome message
            UserChatMessage welcomeMessage = new UserChatMessage(
                "Eventor",
                chatService.getWelcomeMessage(),
                LocalDateTime.now()
            );
            addMessageToChat(welcomeMessage);

            System.out.println("Chat interface initialized successfully");

        } catch (Exception e) {
            System.err.println("Error loading chat interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadChatMessages() {
        if (chatArea == null || chatService == null) {
            return;
        }

        List<UserChatMessage> messages = chatService.getRecentMessages();
        for (UserChatMessage message : messages) {
            addMessageToChat(message);
        }
    }


    private void addMessageToChat(UserChatMessage message) {
        if (messagesContainer == null) {
            System.err.println("Messages container is null");
            return;
        }
        
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setStyle("-fx-background-color: white; " +
                           "-fx-background-radius: 15; " +
                           "-fx-border-radius: 15; " +
                           "-fx-border-color: #e1e1e1; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        
        VBox contentBox = new VBox(4);
        
        // Username and timestamp in one line
        HBox headerBox = new HBox(8);
        Label userLabel = new Label(message.getUsername());
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label timeLabel = new Label(formatTimestamp(message.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(userLabel, timeLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        // Message content
        Label messageLabel = new Label(message.getContent());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #34495e; -fx-font-size: 13px;");
        
        contentBox.getChildren().addAll(headerBox, messageLabel);
        messageBox.getChildren().add(contentBox);
        
        // Add some spacing between messages
        VBox.setMargin(messageBox, new Insets(0, 0, 8, 0));
        
        // Add message to container
        messagesContainer.getChildren().add(messageBox);
    }
} 