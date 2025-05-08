package tn.esprit.Models;

import java.util.Objects;

public class UserCategory {
    private int userId;
    private int categoryId;

    // Constructeurs
    public UserCategory() {
    }

    public UserCategory(int userId, int categoryId) {
        this.userId = userId;
        this.categoryId = categoryId;
    }

    // Getters et Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    // Méthodes utilitaires
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCategory that = (UserCategory) o;
        return userId == that.userId && categoryId == that.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, categoryId);
    }

    @Override
    public String toString() {
        return "UserCategory{" +
                "userId=" + userId +
                ", categoryId=" + categoryId +
                '}';
    }
}