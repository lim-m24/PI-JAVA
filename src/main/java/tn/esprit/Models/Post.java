package tn.esprit.Models;

import java.time.LocalDateTime;

public class Post {
    private String author;
    private LocalDateTime timestamp;
    private String content;
    private int likes;
    private int comments;
    private int shares;

    public Post(String author, LocalDateTime timestamp, String content) {
        this.author = author;
        this.timestamp = timestamp;
        this.content = content;
        this.likes = 0;
        this.comments = 0;
        this.shares = 0;
    }

    // Getters et Setters
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public void like() {
        this.likes++;
    }

    public void addComment() {
        this.comments++;
    }

    public void share() {
        this.shares++;
    }
}