package com.esprit.models;

import java.time.LocalDateTime;

public class Like {
    private int id;
    private int postId;
    private int userId;
    private LocalDateTime createdAt;

    // Default constructor
    public Like() {
    }

    public Like(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Like{" +
                "postId=" + postId +
                ", likeCount=" + userId +  // using userId as likeCount
                '}';
    }
} 