package tn.esprit.utils;

import tn.esprit.Models.User;
import tn.esprit.Services.UserService;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;
    private Map<String, Object> sessionAttributes;
    private User currentUser;
    private UserService userService;

    private SessionManager() {
        this.sessionAttributes = new HashMap<>();
        this.userService = new UserService();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        sessionAttributes.put("currentUser", user);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setAttribute(String key, Object value) {
        sessionAttributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return sessionAttributes.get(key);
    }

    public void removeAttribute(String key) {
        sessionAttributes.remove(key);
    }

    /**
     * Met à jour les informations de l'utilisateur dans la session et dans la base de données
     *
     * @param updatedUser L'utilisateur avec les informations mises à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateUser(User updatedUser) {
        if (currentUser != null && updatedUser.getId() == currentUser.getId()) {
            boolean success = userService.updateUser(updatedUser);
            if (success) {
                this.currentUser = updatedUser;
                sessionAttributes.put("currentUser", updatedUser);
            }
            return success;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
        this.sessionAttributes.clear();
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
}