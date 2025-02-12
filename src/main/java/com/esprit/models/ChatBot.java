package com.esprit.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChatBot {
    private List<Event> mockEvents;
    
    public ChatBot() {
        initializeMockEvents();
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
        
        return "I can help you find events! Try asking me things like:\n" +
               "- Recommend music events\n" +
               "- What's happening this weekend?\n" +
               "- Show me free events\n" +
               "- Find art exhibitions";
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
} 