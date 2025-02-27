package com.esprit.models;

public class Feedback {
    private int id;
    private int userId;
    private int materielId;
    private String contenu;
    private String userName; // Pour l'affichage
    private int rating;  // Ajout du rating

    public Feedback(int id, int userId, int materielId, String contenu, int rating) {
        this.id = id;
        this.userId = userId;
        this.materielId = materielId;
        this.contenu = contenu;
        this.rating = rating;
    }

    public Feedback(int userId, int materielId, String contenu) {
        this.userId = userId;
        this.materielId = materielId;
        this.contenu = contenu;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getMaterielId() { return materielId; }
    public void setMaterielId(int materielId) { this.materielId = materielId; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
} 