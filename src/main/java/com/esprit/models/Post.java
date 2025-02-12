package com.esprit.models;

import java.time.LocalDateTime;

public class Post {
    private int id;
    private String title;
    private String content;
    private String author;
    private int userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPinned;
    private int categoryId;
    private String category;

    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Post(String title, String content, String author) {
        this.title=title;
        this.content=content;
        this.author=author;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // Constructor with fields
    public Post(int userId, String title, String content, String author, int categoryId) {
        this.userId=userId;
        this.title = title;
        this.content = content;
        this.author = author;
        this.categoryId = categoryId;
        this.isPinned = false;
    }

    public Post(int id, String title, String content) {
        this.id=id;
        this.title = title;
        this.content = content;
        this.isPinned = false;
    }

    public Post(int id) {
        this.id=id;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategory() { 
        return category; 
    }
    
    public void setCategory(String category) { 
        this.category = category; 
    }

    @Override
    public String toString() {
        return "Post{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Post post = (Post) o;
        return id == post.id &&
               userId == post.userId &&
               categoryId == post.categoryId &&
               isPinned == post.isPinned &&
               title.equals(post.title) &&
               content.equals(post.content) &&
               (author == null ? post.author == null : author.equals(post.author)) &&
               (category == null ? post.category == null : category.equals(post.category));
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id;
        result = 31 * result + userId;
        result = 31 * result + categoryId;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (isPinned ? 1 : 0);
        return result;
    }
}