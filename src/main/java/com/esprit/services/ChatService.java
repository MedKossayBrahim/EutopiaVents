package com.esprit.services;

import com.esprit.utils.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.io.IOException;
import okhttp3.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import java.util.stream.Collectors;

public class ChatService {
    private static final String BOT_NAME = "Eventor";
    private java.sql.Connection connection;
    private Set<String> categories;
    private OpenAiService openAiService;
    private static final String OPENAI_API_KEY = "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";
    private List<com.theokanning.openai.completion.chat.ChatMessage> conversationHistory;

    public ChatService() {
        try {
            connection = DataSource.getInstance().getConnection();
            categories = loadCategories();
            conversationHistory = new ArrayList<>();
            
            // Initialize OpenAI service with your API key
            openAiService = new OpenAiService(OPENAI_API_KEY);
            
            // Add initial system prompt
            String systemPrompt = String.format(
                "You are an event and forum assistant with access to both event and forum data. " +
                "You can provide information about:\n" +
                "1. Events:\n" +
                "   - Event details (titles, dates, prices)\n" +
                "   - Event categories: %s\n" +
                "   - Event locations\n" +
                "   - Price ranges and statistics\n" +
                "2. Forum Posts:\n" +
                "   - Recent discussions\n" +
                "   - Popular topics\n" +
                "   - User activity\n" +
                "   - Post statistics\n\n" +
                "Please provide specific information from the database when answering questions. " +
                "You can discuss both events and forum posts, and provide relevant details from either.",
                String.join(", ", categories)
            );
            
            // Add system prompt to conversation history
            conversationHistory.add(new com.theokanning.openai.completion.chat.ChatMessage(
                ChatMessageRole.SYSTEM.value(), 
                systemPrompt
            ));
            
            // Add welcome message to conversation history
            String welcomeMessage = getWelcomeMessage();
            conversationHistory.add(new com.theokanning.openai.completion.chat.ChatMessage(
                ChatMessageRole.ASSISTANT.value(),
                welcomeMessage
            ));
            
        } catch (SQLException e) {
            System.err.println("Error initializing ChatService: " + e.getMessage());
        }
    }

    private Set<String> loadCategories() throws SQLException {
        Set<String> uniqueCategories = new HashSet<>();
        String query = "SELECT DISTINCT nom FROM categoriesevent WHERE nom IS NOT NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String category = rs.getString("nom");
                if (category != null && !category.trim().isEmpty()) {
                    uniqueCategories.add(category);
                }
            }
        }
        return uniqueCategories;
    }

    private Integer getCategoryId(String categoryName) throws SQLException {
        String query = "SELECT id FROM categoriesevent WHERE LOWER(nom) = LOWER(?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return null;
    }

    public List<UserChatMessage> getRecentMessages() {
        List<UserChatMessage> messages = new ArrayList<>();
        
        // Always include welcome message as first message if it exists
        if (!conversationHistory.isEmpty()) {
            // Skip the first message (system prompt) and start from index 1
            for (int i = 1; i < conversationHistory.size(); i++) {
                com.theokanning.openai.completion.chat.ChatMessage msg = conversationHistory.get(i);
                if (!msg.getRole().equals(ChatMessageRole.SYSTEM.value())) {
                    String username;
                    switch (msg.getRole()) {
                        case "user":
                            username = "You";
                            break;
                        case "assistant":
                            username = BOT_NAME + " (AI)";
                            break;
                        default:
                            continue;
                    }
                    
                    messages.add(new UserChatMessage(
                        username,
                        msg.getContent(),
                        LocalDateTime.now()
                    ));
                }
            }
        }
        
        // If still empty (first time), add welcome message
        if (messages.isEmpty()) {
            String welcomeMessage = getWelcomeMessage();
            messages.add(new UserChatMessage(BOT_NAME + " (AI)", welcomeMessage, LocalDateTime.now()));
            
            // Add to conversation history if not already there
            if (conversationHistory.stream().noneMatch(msg -> 
                    msg.getRole().equals(ChatMessageRole.ASSISTANT.value()) && 
                    msg.getContent().equals(welcomeMessage))) {
                conversationHistory.add(new com.theokanning.openai.completion.chat.ChatMessage(
                    ChatMessageRole.ASSISTANT.value(),
                    welcomeMessage
                ));
            }
        }
        
        System.out.println("Returning " + messages.size() + " messages from history");
        return messages;
    }

    public UserChatMessage processMessage(String userMessage) {
        try {
            System.out.println("\n====== Processing User Message ======");
            System.out.println("User Input: " + userMessage);
            
            // All messages go directly to AI processing
            return processWithAI(userMessage);

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            return new UserChatMessage(BOT_NAME + " (AI)", 
                "Sorry, there was an error processing your request.", 
                LocalDateTime.now());
        }
    }

    private UserChatMessage processWithAI(String userMessage) {
        try {
            System.out.println("\n====== Processing User Message ======");
            System.out.println("User Input: " + userMessage);
            
            // Get current database context but limit it
            String databaseContext = getLimitedDatabaseContext();
            System.out.println("\nDatabase Context Provided to AI:");
            System.out.println(databaseContext);
            
            // Manage conversation history size
            if (conversationHistory.size() > 10) {
                // Keep system message and last 9 messages
                List<com.theokanning.openai.completion.chat.ChatMessage> trimmedHistory = new ArrayList<>();
                trimmedHistory.add(conversationHistory.get(0)); // Keep system message
                trimmedHistory.addAll(conversationHistory.subList(
                    conversationHistory.size() - 9, 
                    conversationHistory.size()
                ));
                conversationHistory = trimmedHistory;
            }

            // Add context and user message to conversation history
            conversationHistory.add(new com.theokanning.openai.completion.chat.ChatMessage(
                ChatMessageRole.SYSTEM.value(),
                "Current database information:\n" + databaseContext + "\n" +
                "You are an event and forum assistant. Help users by providing specific information about events and forum posts."
            ));

            conversationHistory.add(new com.theokanning.openai.completion.chat.ChatMessage(
                ChatMessageRole.USER.value(), 
                userMessage
            ));

            // Create chat completion request with increased max tokens
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo-16k") // Using the 16k model for larger context
                .messages(conversationHistory)
                .temperature(0.7)
                .maxTokens(1000)
                .build();

            // Get AI response
            com.theokanning.openai.completion.chat.ChatMessage aiResponse = 
                openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage();

            System.out.println("\n====== AI Response ======");
            System.out.println(aiResponse.getContent());
            System.out.println("==========================\n");

            // Add AI response to conversation history
            conversationHistory.add(aiResponse);

            return new UserChatMessage(BOT_NAME + " (AI)", aiResponse.getContent(), LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("Error processing with AI: " + e.getMessage());
            e.printStackTrace();
            return new UserChatMessage(BOT_NAME + " (AI)", 
                "I'm having trouble processing your request due to the amount of information. Let me try to help you with a more focused response.", 
                LocalDateTime.now());
        }
    }

    private String getLimitedDatabaseContext() {
        StringBuilder context = new StringBuilder();
        try {
            // Get only the most recent events (limit to 5) with both start and end dates
            String eventQuery = "SELECT e.titre, e.prix, e.date_debut, e.date_fin, l.nom as location, c.nom as category " +
                              "FROM events e " +
                              "JOIN lieu l ON e.lieu_id = l.id " +
                              "JOIN categoriesevent c ON e.categorie_id = c.id " +
                              "ORDER BY e.date_debut ASC LIMIT 5";
            
            context.append("Recent Events:\n");
            try (PreparedStatement stmt = connection.prepareStatement(eventQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    context.append(String.format("- %s (Category: %s)\n" +
                        "  Location: %s\n" +
                        "  Price: %.2f TND\n" +
                        "  Starts: %s\n" +
                        "  Ends: %s\n",
                        rs.getString("titre"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getDouble("prix"),
                        rs.getDate("date_debut"),
                        rs.getDate("date_fin")
                    ));
                }
            }

            // Get only the most recent posts (limit to 5)
            String recentPostsQuery = "SELECT title, author, created_at " +
                                    "FROM posts " +
                                    "ORDER BY created_at DESC LIMIT 5";

            context.append("\nRecent Forum Posts:\n");
            try (PreparedStatement stmt = connection.prepareStatement(recentPostsQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    context.append(String.format(
                        "- %s by %s (%s)\n",
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }

            // Get basic statistics only
            String statsQuery = "SELECT COUNT(*) as total_posts, COUNT(DISTINCT author) as unique_authors " +
                              "FROM posts";

            context.append("\nForum Statistics:\n");
            try (PreparedStatement stmt = connection.prepareStatement(statsQuery);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    context.append(String.format(
                        "Total Posts: %d, Unique Authors: %d\n",
                        rs.getInt("total_posts"),
                        rs.getInt("unique_authors")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting database context: " + e.getMessage());
            context.append("Error accessing some information.");
        }

        return context.toString();
    }

    private UserChatMessage handleLocationQuery() {
        try {
            String sql = "SELECT e.titre, l.nom as location, c.nom as category " +
                        "FROM events e " +
                        "JOIN lieu l ON e.lieu_id = l.id " +
                        "JOIN categoriesevent c ON e.categorie_id = c.id " +
                        "ORDER BY e.date_debut ASC";
            
            StringBuilder response = new StringBuilder();
            response.append("Here are the event locations:\n\n");
            
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String title = rs.getString("titre");
                    String location = rs.getString("location");
                    String category = rs.getString("category");
                    
                    response.append(String.format("Event: %s\n", title));
                    response.append(String.format("Location: %s\n", location));
                    response.append(String.format("Category: %s\n", category));
                    response.append("------------------------\n");
                }
            }
            
            if (response.toString().equals("Here are the event locations:\n\n")) {
                return new UserChatMessage(BOT_NAME,
                    "I couldn't find any event location information at the moment. " +
                    "Would you like to see all available events instead?",
                    LocalDateTime.now());
            }
            
            return new UserChatMessage(BOT_NAME, response.toString(), LocalDateTime.now());
            
        } catch (SQLException e) {
            System.err.println("Error querying event locations: " + e.getMessage());
            return new UserChatMessage(BOT_NAME,
                "I'm having trouble finding location information. Would you like to see all available events instead?",
                LocalDateTime.now());
        }
    }

    private UserChatMessage handlePriceQuery(String query) {
        try {
            // Query to get events sorted by price
            String sql = "SELECT e.titre, e.prix, c.nom as category " +
                        "FROM events e " +
                        "JOIN categoriesevent c ON e.categorie_id = c.id " +
                        "WHERE e.prix IS NOT NULL " +
                        "ORDER BY e.prix ASC " +
                        "LIMIT 5";
            
            StringBuilder response = new StringBuilder();
            response.append("Here are some of the most affordable events:\n\n");
            
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String title = rs.getString("titre");
                    double price = rs.getDouble("prix");
                    String category = rs.getString("category");
                    
                    response.append(String.format("Event: %s\n", title));
                    response.append(String.format("Price: %.2f TND\n", price));
                    response.append(String.format("Category: %s\n", category));
                    response.append("------------------------\n");
                }
            }
            
            if (response.toString().equals("Here are some of the most affordable events:\n\n")) {
                return new UserChatMessage(BOT_NAME,
                    "I couldn't find any events with price information at the moment. " +
                    "Would you like to see all available events in a specific category instead?",
                    LocalDateTime.now());
            }
            
            return new UserChatMessage(BOT_NAME, response.toString(), LocalDateTime.now());
            
        } catch (SQLException e) {
            System.err.println("Error querying events by price: " + e.getMessage());
            return new UserChatMessage(BOT_NAME,
                "I'm having trouble finding price information. Would you like to see all available events instead?",
                LocalDateTime.now());
        }
    }

    private UserChatMessage processEventQuery(String category) {
        try {
            Integer categoryId = getCategoryId(category);
            if (categoryId == null) {
                return new UserChatMessage(BOT_NAME, "Error finding category ID.", LocalDateTime.now());
            }

            // Existing event query logic
            String query = "SELECT titre, prix, date_debut, capacite FROM events " +
                         "WHERE categorie_id = ? " +
                         "ORDER BY date_debut ASC";
            
            List<String> events = new ArrayList<>();
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, categoryId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String title = rs.getString("titre");
                    double price = rs.getDouble("prix");
                    Date date = rs.getDate("date_debut");
                    int capacity = rs.getInt("capacite");
                    
                    events.add(String.format(
                        "Event: %s\n" +
                        "When: %s\n" +
                        "Price: %.2f TND\n" +
                        "Capacity: %d\n" +
                        "------------------------\n",
                        title,
                        date != null ? date.toString() : "Date TBA",
                        price,
                        capacity
                    ));
                }
            }
            
            if (events.isEmpty()) {
                return new UserChatMessage(BOT_NAME, 
                    "No events found for category: " + category, 
                    LocalDateTime.now());
            }
            
            StringBuilder response = new StringBuilder();
            response.append("Here are the events for category '")
                   .append(category)
                   .append("':\n\n");
            events.forEach(response::append);
            
            return new UserChatMessage(BOT_NAME, response.toString(), LocalDateTime.now());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return new UserChatMessage(BOT_NAME, 
                "Sorry, there was an error retrieving the events.", 
                LocalDateTime.now());
        }
    }

    public String getWelcomeMessage() {
        StringBuilder welcomeMessage = new StringBuilder();
        welcomeMessage.append("Hello! I'm your event and forum assistant. I can help you with:\n\n");
        welcomeMessage.append("1. Finding events by category, location, or price\n");
        welcomeMessage.append("2. Browsing forum discussions and posts\n");
        welcomeMessage.append("3. Getting information about event venues\n");
        welcomeMessage.append("4. Checking forum statistics and active users\n\n");
        welcomeMessage.append("What would you like to know about?\n");
        
        return welcomeMessage.toString();
    }

    // Inner class for chat messages
    public static class UserChatMessage {
        private String username;
        private String content;
        private LocalDateTime timestamp;

        public UserChatMessage(String username, String content, LocalDateTime timestamp) {
            this.username = username;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getUsername() { return username; }
        public String getContent() { return content; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
} 