package tn.esprit.Models;

import tn.esprit.Services.UserService;

import java.time.LocalDateTime;

public class UserActivity {
    private int id;
    private int userId; // Changed to userId to match the database schema
    private String action;
    private LocalDateTime timestamp;
    private String details;

    // Transient field for userName (not stored in the database)
    private transient String userName;

    public UserActivity() {
    }

    public UserActivity(int id, int userId, String action, LocalDateTime timestamp, String details) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.timestamp = timestamp;
        this.details = details;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Method to dynamically fetch userName when needed
    public String getUserName() {
        if (userName == null && userId > 0) {
            UserService userService = new UserService();
            User user = userService.getUserById(userId);
            userName = user != null ? user.getName() : "Unknown User";
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}