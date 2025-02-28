package com.esprit.models;

import java.time.LocalDate;

public class Event {
    private String name;
    private String category;
    private LocalDate date;
    private double price;
    private String venue;
    
    public Event(String name, String category, LocalDate date, double price, String venue) {
        this.name = name;
        this.category = category;
        this.date = date;
        this.price = price;
        this.venue = venue;
    }
    
    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public double getPrice() { return price; }
    public String getVenue() { return venue; }
} 