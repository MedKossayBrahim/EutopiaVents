package com.esprit.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatBot {
    private List<Event> mockEvents;
    private List<Post> forumPosts;
    
    public ChatBot() {
        initializeMockEvents();
        initializeForumPosts();
    }
    
    private void initializeMockEvents() {
        mockEvents = new ArrayList<>();
        
        // Add some mock events
        mockEvents.add(new Event("Summer Music Festival", "music", LocalDate.now().plusDays(5), 75.0, "Central Park"));
        mockEvents.add(new Event("Rock Concert", "music", LocalDate.now().plusDays(2), 50.0, "Stadium Arena"));
        mockEvents.add(new Event("Art Exhibition", "art", LocalDate.now().plusDays(1), 0.0, "City Gallery"));
        mockEvents.add(new Event("Basketball Tournament", "sports", LocalDate.now().plusDays(3), 30.0, "Sports Complex"));
        mockEvents.add(new Event("Photography Workshop", "art", LocalDate.now().plusDays(7), 25.0, "Creative Hub"));
        mockEvents.add(new Event("Jazz Night", "music", LocalDate.now(), 40.0, "Jazz Club"));
        mockEvents.add(new Event("Soccer Match", "sports", LocalDate.now().plusDays(1), 45.0, "City Stadium"));
    }
    
    private void initializeForumPosts() {
        forumPosts = new ArrayList<>();
        // This will be populated from your actual forum data
    }
    
    public void updateForumPosts(List<Post> posts) {
        this.forumPosts = new ArrayList<>(posts);
    }
    
    public String processUserInput(String userInput) {
        userInput = userInput.toLowerCase();
        
        if (userInput.contains("recommend") || userInput.contains("suggest")) {
            if (userInput.contains("music") || userInput.contains("concert")) {
                return findEvents("music");
            } else if (userInput.contains("sport")) {
                return findEvents("sports");
            } else if (userInput.contains("art") || userInput.contains("exhibition")) {
                return findEvents("art");
            }
        }
        
        if (userInput.contains("today")) {
            return findEventsByDate(LocalDate.now());
        } else if (userInput.contains("tomorrow")) {
            return findEventsByDate(LocalDate.now().plusDays(1));
        } else if (userInput.contains("weekend")) {
            return findEventsForWeekend();
        }
        
        if (userInput.contains("free")) {
            return findEventsByPrice(0);
        } else if (userInput.contains("cheap") || userInput.contains("budget")) {
            return findEventsByPrice(30);
        }
        
        if (userInput.contains("forum") || userInput.contains("post")) {
            if (userInput.contains("total") || userInput.contains("count") || userInput.contains("how many")) {
                return getForumStats();
            }
            if (userInput.contains("pinned")) {
                if (userInput.contains("not") || userInput.contains("unpinned")) {
                    return getNonPinnedPostsInfo();
                }
                return getPinnedPostsInfo();
            }
            if (userInput.contains("all")) {
                return getAllPostsInfo();
            }
            if (userInput.contains("week") || userInput.contains("recent")) {
                return getLastWeekPosts();
            }
        }
        
        return "I can help you find events and forum information! Try asking me things like:\n" +
               "- Recommend music events\n" +
               "- What's happening this weekend?\n" +
               "- Show me free events\n" +
               "- Find art exhibitions\n" +
               "- How many posts are in the forum?\n" +
               "- Show me pinned posts\n" +
               "- Show me unpinned posts\n" +
               "- Show me all posts\n" +
               "- Show me posts from last week\n" +
               "- What are the recent posts?";
    }
    
    private String findEvents(String category) {
        StringBuilder response = new StringBuilder("Here are some " + category + " events I found:\n\n");
        boolean found = false;
        
        for (Event event : mockEvents) {
            if (event.getCategory().equals(category)) {
                response.append("📅 ").append(event.getName())
                       .append("\nDate: ").append(event.getDate())
                       .append("\nPrice: $").append(event.getPrice())
                       .append("\nLocation: ").append(event.getVenue())
                       .append("\n\n");
                found = true;
            }
        }
        
        return found ? response.toString() : "Sorry, I couldn't find any " + category + " events.";
    }
    
    private String findEventsByDate(LocalDate date) {
        StringBuilder response = new StringBuilder("Here are the events for " + date + ":\n\n");
        boolean found = false;
        
        for (Event event : mockEvents) {
            if (event.getDate().equals(date)) {
                response.append("📅 ").append(event.getName())
                       .append("\nPrice: $").append(event.getPrice())
                       .append("\nLocation: ").append(event.getVenue())
                       .append("\n\n");
                found = true;
            }
        }
        
        return found ? response.toString() : "No events found for this date.";
    }
    
    private String findEventsForWeekend() {
        LocalDate saturday = LocalDate.now();
        while (saturday.getDayOfWeek().getValue() != 6) {
            saturday = saturday.plusDays(1);
        }
        LocalDate sunday = saturday.plusDays(1);
        
        StringBuilder response = new StringBuilder("Here are the weekend events:\n\n");
        boolean found = false;
        
        for (Event event : mockEvents) {
            if (event.getDate().equals(saturday) || event.getDate().equals(sunday)) {
                response.append("📅 ").append(event.getName())
                       .append("\nDate: ").append(event.getDate())
                       .append("\nPrice: $").append(event.getPrice())
                       .append("\nLocation: ").append(event.getVenue())
                       .append("\n\n");
                found = true;
            }
        }
        
        return found ? response.toString() : "No events found for this weekend.";
    }
    
    private String findEventsByPrice(double maxPrice) {
        StringBuilder response = new StringBuilder("Here are events under $" + maxPrice + ":\n\n");
        boolean found = false;
        
        for (Event event : mockEvents) {
            if (event.getPrice() <= maxPrice) {
                response.append("📅 ").append(event.getName())
                       .append("\nDate: ").append(event.getDate())
                       .append("\nPrice: $").append(event.getPrice())
                       .append("\nLocation: ").append(event.getVenue())
                       .append("\n\n");
                found = true;
            }
        }
        
        return found ? response.toString() : "No events found within this price range.";
    }
    
    private String getForumStats() {
        if (forumPosts == null || forumPosts.isEmpty()) {
            return "The forum currently has no posts.";
        }
        
        long totalPosts = forumPosts.size();
        long pinnedPosts = forumPosts.stream()
                .filter(Post::isPinned)
                .count();
        
        return String.format("The forum has %d total posts, including %d pinned posts.", 
                           totalPosts, pinnedPosts);
    }
    
    private String getAllPostsInfo() {
        if (forumPosts == null || forumPosts.isEmpty()) {
            return "There are currently no posts in the forum.";
        }
        
        StringBuilder response = new StringBuilder("Here are all the posts:\n\n");
        for (Post post : forumPosts) {
            response.append(post.isPinned() ? "📌 " : "📝 ")
                   .append(post.getTitle())
                   .append("\nPosted by: ").append(post.getAuthor())
                   .append("\nStatus: ").append(post.isPinned() ? "Pinned" : "Not Pinned")
                   .append("\n\n");
        }
        
        return response.toString();
    }
    
    private String getNonPinnedPostsInfo() {
        if (forumPosts == null || forumPosts.isEmpty()) {
            return "There are currently no posts in the forum.";
        }
        
        List<Post> nonPinnedPosts = forumPosts.stream()
                .filter(post -> !post.isPinned())
                .collect(Collectors.toList());
        
        if (nonPinnedPosts.isEmpty()) {
            return "There are currently no unpinned posts.";
        }
        
        StringBuilder response = new StringBuilder("Here are the unpinned posts:\n\n");
        for (Post post : nonPinnedPosts) {
            response.append("📝 ").append(post.getTitle())
                   .append("\nPosted by: ").append(post.getAuthor())
                   .append("\n\n");
        }
        
        return response.toString();
    }
    
    private String getPinnedPostsInfo() {
        if (forumPosts == null || forumPosts.isEmpty()) {
            return "There are currently no posts in the forum.";
        }
        
        List<Post> pinnedPosts = forumPosts.stream()
                .filter(Post::isPinned)
                .collect(Collectors.toList());
        
        if (pinnedPosts.isEmpty()) {
            return "There are currently no pinned posts.";
        }
        
        StringBuilder response = new StringBuilder("Here are the pinned posts:\n\n");
        for (Post post : pinnedPosts) {
            response.append("📌 ").append(post.getTitle())
                   .append("\nPosted by: ").append(post.getAuthor())
                   .append("\n\n");
        }
        
        return response.toString();
    }

    private String getLastWeekPosts() {
        if (forumPosts == null || forumPosts.isEmpty()) {
            return "There are currently no posts in the forum.";
        }

        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        
        List<Post> recentPosts = forumPosts.stream()
                .filter(post -> post.getCreatedAt().isAfter(oneWeekAgo))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());

        if (recentPosts.isEmpty()) {
            return "There have been no new posts in the last week.";
        }

        StringBuilder response = new StringBuilder("Here are the posts from the last week:\n\n");
        for (Post post : recentPosts) {
            response.append(post.isPinned() ? "📌 " : "📝 ")
                   .append(post.getTitle())
                   .append("\nPosted by: ").append(post.getAuthor())
                   .append("\nPosted on: ").append(formatDateTime(post.getCreatedAt()))
                   .append("\nStatus: ").append(post.isPinned() ? "Pinned" : "Not Pinned")
                   .append("\n\n");
        }

        return response.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        // Format: "Monday, July 10, 2023 at 14:30"
        return dateTime.getDayOfWeek().toString().substring(0, 1) + 
               dateTime.getDayOfWeek().toString().substring(1).toLowerCase() +
               ", " + dateTime.getMonth().toString().substring(0, 1) +
               dateTime.getMonth().toString().substring(1).toLowerCase() +
               " " + dateTime.getDayOfMonth() +
               ", " + dateTime.getYear() +
               " at " + String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }
} 