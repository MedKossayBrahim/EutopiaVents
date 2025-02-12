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
import java.util.ResourceBundle;
import java.io.File;
import java.util.stream.Collectors;

public class ForumMainController implements SearchableController, Initializable {

    @FXML
    private ListView<Post> latestUpdatesList;

    @FXML
    private ListView<Post> pinnedPostsList;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addButton;

    @FXML
    private Button refreshPostsButton;

    @FXML
    private BorderPane rootPane;

    @FXML
    private HBox searchContainer;

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

    // Add these constants for database connection
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eutopia_db";
    private static final String USER = "root";
    private static final String PASS = "";

    @FXML
    private Label welcomeUsernameLabel;

    private List<Post> allPosts = new ArrayList<>();

    @FXML
    private ComboBox<String> searchFilterComboBox;
    private String currentSearchFilter = "Title";

    public void setApplication(Eutopia app) {
        this.application = app; // Set the application instance
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Set up ListViews
            setupListView(latestUpdatesList);
            setupListView(pinnedPostsList);
            
            // Initialize Calendar and Chat
            initializeCalendar();
            initializeChat();
            
            // Set simple welcome message
            if (welcomeUsernameLabel != null) {
                welcomeUsernameLabel.setText("Welcome to Forum");
            }
            
            // Initialize search filter ComboBox
            if (searchFilterComboBox != null) {
                ObservableList<String> filterOptions = FXCollections.observableArrayList(
                    "Title", "Author", "Category"
                );
                searchFilterComboBox.setItems(filterOptions);
                searchFilterComboBox.setValue("Title"); // Set default value
                
                searchFilterComboBox.setOnAction(event -> {
                    currentSearchFilter = searchFilterComboBox.getValue();
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        handleSearch(searchField.getText());
                    }
                    System.out.println("Search filter changed to: " + currentSearchFilter);
                });
            }
            
            // Initialize your components
            loadLatestUpdates();
            loadPinnedPosts();
            
            // Set up search field
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    handleSearch(newValue);
                });
            }
            
            // Debug prints
            System.out.println("Initialize called");
            System.out.println("LatestUpdatesList items: " + latestUpdatesList.getItems().size());
            System.out.println("PinnedPostsList items: " + pinnedPostsList.getItems().size());
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing ForumMainController: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        System.out.println("FXML initialize called");

        // Set up ListViews
        setupListView(latestUpdatesList);
        setupListView(pinnedPostsList);

        // Initialize lists
        loadLatestUpdates();
        loadPinnedPosts();
        displayLimitedLatestUpdates(3);

        // Debug print
        System.out.println("Posts loaded: " + latestUpdatesList.getItems().size());

        // Style the view all button
        styleViewAllButton();
    }

    void setupListView(ListView<Post> listView) {
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
            private final VBox contentBox = new VBox(8); // Increased spacing between elements
            private final HBox headerBox = new HBox(10);
            private final Label titleLabel = new Label();
            private final Label authorLabel = new Label();
            private final Label contentLabel = new Label();
            private final Label timeLabel = new Label();

            {
                // Style labels with bigger fonts
                titleLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 20; -fx-font-family: 'System Bold';");
                authorLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14; -fx-font-weight: 600;");
                contentLabel.setStyle("-fx-font-size: 15;");
                contentLabel.setWrapText(true);
                timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 11;");

                // Add spacing in header
                headerBox.setSpacing(15);
                headerBox.getChildren().addAll(titleLabel, authorLabel);

                // Add all components to the content box
                contentBox.getChildren().addAll(headerBox, contentLabel, timeLabel);

                // Add padding to content box
                contentBox.setPadding(new Insets(5, 0, 5, 0));
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
                                    "-fx-padding: 20; " + // Increased padding
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
                    setGraphic(contentBox);
                }
            }
        });
    }

    @FXML
    private void onAddPostClick() {
        try {
            // Get the project root directory
            String projectRoot = System.getProperty("user.dir");

            // Define the path to the FXML file
            String fxmlPath = projectRoot + "/src/main/ressources/post-dialog.fxml";
            File fxmlFile = new File(fxmlPath);

            if (!fxmlFile.exists()) {
                System.err.println("FXML file not found at: " + fxmlPath);
                // Try loading from resources
                URL resource = getClass().getResource("/post-dialog.fxml");
                if (resource != null) {
                    fxmlFile = new File(resource.toURI());
                } else {
                    throw new IOException("Cannot find post-dialog.fxml");
                }
            }

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Parent root = loader.load();

            // Get the controller and pass any necessary data
            PostDialogController controller = loader.getController();
            controller.setPostService(postService);

            // Create new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Post");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Set up the scene
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Set minimum window size
            dialogStage.setMinWidth(400);
            dialogStage.setMinHeight(300);

            // Center on parent window
            Stage parentStage = (Stage) latestUpdatesList.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            // Show the dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the posts list after dialog closes
            loadLatestUpdates();
            loadPinnedPosts();

        } catch (Exception e) {
            System.err.println("Error loading post dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load post dialog. Error: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/all-posts-page.fxml"));
            Parent root = loader.load();

            AllPostsController controller = loader.getController();
            controller.setApplication(application);

            // Convert Post objects to strings for display
            ArrayList<String> postsList = new ArrayList<>();
            for (Post post : latestUpdatesList.getItems()) {
                postsList.add(post.getTitle() + ": " + post.getContent());
            }
            controller.setPosts(postsList, "Latest Updates");

            // Get the current scene and set the new content
            Scene scene = latestUpdatesList.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load all posts page.");
        }
    }

    @FXML
    private void onViewAllPinnedClick() {
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
            controller.setPosts(postsList, "Pinned Posts");

            // Get the current scene and set the new content
            Scene scene = pinnedPostsList.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load all posts page.");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/allPosts.fxml"));
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
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
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

    private void initializeCalendar() {
        try {
            if (calendarContainer == null) {
                System.err.println("Calendar container is null!");
                return;
            }

            File file = new File("src/main/ressources/calendar.fxml");
            if (!file.exists()) {
                throw new IOException("Could not find calendar.fxml at: " + file.getAbsolutePath());
            }

            URL fxmlUrl = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent calendarView = loader.load();
            calendarController = loader.getController();

            // Set up calendar selection listener
            calendarController.setOnDateSelected((startDate, endDate) -> {
                filterPostsByDate(startDate, endDate);
            });

            // Add the calendar to the container
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
            File file = new File("src/main/ressources/chat_interface.fxml");
            if (!file.exists()) {
                throw new IOException("Could not find chat_interface.fxml at: " + file.getAbsolutePath());
            }

            URL fxmlUrl = file.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent chatView = loader.load();

            // Update the Event Assistant label style to black
            Label eventAssistantLabel = (Label) chatView.lookup("Label");
            if (eventAssistantLabel != null) {
                eventAssistantLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: black;");
            }

            // Add the chat to the container
            chatArea.getChildren().clear();
            chatArea.getChildren().add(chatView);

            System.out.println("Chat interface initialized successfully");

        } catch (Exception e) {
            System.err.println("Error loading chat interface: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load chat interface: " + e.getMessage());
        }
    }

    @Override
    public void handleRefresh() {
        // Load latest posts
        loadLatestUpdates();
        // Load pinned posts
        loadPinnedPosts();
        // Clear search field
        if (searchField != null) {
            searchField.clear();
        }
        System.out.println("Forum view refreshed");
    }
} 