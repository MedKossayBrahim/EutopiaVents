package com.esprit.models;

import java.time.LocalDateTime;

public class Like {
    private int id;
    private int postId;
    private int userId;
    private LocalDateTime createdAt;
    private int likeCount; // For aggregation purposes

    // Default constructor
    public Like() {
    }

    public Like(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for like count aggregation - renamed parameter for clarity
    public Like(int postId, int totalLikes, boolean isAggregation) {
        this.postId = postId;
        this.likeCount = totalLikes;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", likeCount=" + likeCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Like like = (Like) o;
        return id == like.id &&
               postId == like.postId &&
               userId == like.userId;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + postId;
        result = 31 * result + userId;
        return result;
    }
} 