package tn.esprit.Services;

import tn.esprit.dao.UserDAO;
import tn.esprit.Models.Categories;
import tn.esprit.Models.User;
import tn.esprit.utils.PasswordUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public int getTotalUserCount() {
        return userDAO.getTotalUserCount();
    }

    public int getActiveUserCount() {
        return userDAO.getActiveUserCount();
    }

    public int getBannedUserCount() {
        return userDAO.getBannedUserCount();
    }

    public int getAdminUserCount() {
        return userDAO.getCountByRole("ROLE_ADMIN");
    }

    public int getCountByRole(String role) {
        return userDAO.getCountByRole(role);
    }

    public int getNewUsersTodayCount() {
        return userDAO.getNewUsersCount(LocalDate.now());
    }

    public Map<String, Integer> getMonthlyRegistrations(int months) {
        return userDAO.getMonthlyRegistrations(months);
    }

    public Map<LocalDate, Map<String, Integer>> getDailyStats(LocalDate startDate, LocalDate endDate) {
        return userDAO.getDailyStats(startDate, endDate);
    }

    public Map<String, Double> getEngagementMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("loginFrequency", userDAO.getAverageLoginFrequency());
        metrics.put("sessionDuration", userDAO.getAverageSessionDuration());
        metrics.put("retentionRate", userDAO.getRetentionRate());
        return metrics;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public boolean addUser(User user) {
        return userDAO.addUser(user);
    }

    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public boolean deleteUser(int id) {
        return userDAO.deleteUser(id);
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public User getUserByResetToken(String resetToken) {
        return userDAO.getUserByResetToken(resetToken);
    }

    public Map<LocalDate, Integer> getDailyRegistrations(LocalDate startDate, LocalDate endDate) {
        return userDAO.getDailyRegistrations(startDate, endDate);
    }

    public boolean verifyCurrentPassword(int userId, String currentPassword) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        String storedPassword = userDAO.getUserPassword(userId);
        return PasswordUtils.verifyPassword(currentPassword, storedPassword);
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (!verifyCurrentPassword(userId, currentPassword)) {
            return false;
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }

        user.setPassword(PasswordUtils.hashPassword(newPassword));
        return userDAO.updateUser(user);
    }

    public boolean deactivateAccount(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }

        user.setActive(false);
        return userDAO.updateUser(user);
    }

    // New method to get all categories
    public List<Categories> getAllCategories() {
        return userDAO.getAllCategories();
    }
}