package tn.esprit.Models;

import java.time.LocalDateTime;

public class Share {
    private int id;
    private int postId;
    private Integer sharedFromId; // Nullable
    private Integer userId; // Nullable, comme dans la table
    private LocalDateTime createAt; // Aligné avec la colonne 'create_at'

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

    public Integer getSharedFromId() {
        return sharedFromId;
    }

    public void setSharedFromId(Integer sharedFromId) {
        this.sharedFromId = sharedFromId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }
}