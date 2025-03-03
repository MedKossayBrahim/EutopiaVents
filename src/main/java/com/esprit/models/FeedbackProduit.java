package com.esprit.models;

public class FeedbackProduit {
    private int id;
    private int userId;
    private int produitId;
    private String comment;
    private String userName; // For display
    private int rating;  // 1-5 rating

    public FeedbackProduit(int id, int userId, int produitId, String comment, int rating) {
        this.id = id;
        this.userId = userId;
        this.produitId = produitId;
        this.comment = comment;
        this.rating = rating;
    }

    public FeedbackProduit(int userId, int produitId, String comment) {
        this.userId = userId;
        this.produitId = produitId;
        this.comment = comment;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProduitId() { return produitId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
} 