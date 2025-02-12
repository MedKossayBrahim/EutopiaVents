package com.esprit.models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
    private String username;

    public Comment() {
    }

    public Comment(int postId, int userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Comment(int id, int postId, int userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Comment comment = (Comment) o;
        return id == comment.id &&
               postId == comment.postId &&
               userId == comment.userId &&
               content.equals(comment.content) &&
               (username == null ? comment.username == null : username.equals(comment.username));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + postId;
        result = 31 * result + userId;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}