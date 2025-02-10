package com.esprit.models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private String username;

    public Comment(int id, int postId, int userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.username = "Anonymous";
    }

    public Comment(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public Comment(int postId, int userId, String content) {
        this.postId=postId;
        this.userId=userId;
        this.content = content;
    }

    public Comment(String content) {
        this.content=content;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "Comment{" +
                "content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}