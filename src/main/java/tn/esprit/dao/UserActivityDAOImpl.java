package tn.esprit.dao;

import tn.esprit.Models.UserActivity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserActivityDAOImpl implements UserActivityDAO {
    @Override
    public List<UserActivity> getRecentActivities(int limit) {
        List<UserActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM user_activity ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UserActivity activity = mapResultSetToUserActivity(rs);
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    @Override
    public List<UserActivity> getActivitiesBetweenDates(LocalDateTime start, LocalDateTime end) {
        List<UserActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM user_activity WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UserActivity activity = mapResultSetToUserActivity(rs);
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    private UserActivity mapResultSetToUserActivity(ResultSet rs) throws SQLException {
        UserActivity activity = new UserActivity();
        activity.setId(rs.getInt("id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setAction(rs.getString("action"));
        activity.setDetails(rs.getString("details"));
        activity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        return activity;
    }

    public boolean addActivity(UserActivity activity) {
        String sql = "INSERT INTO user_activity (user_id, action, details, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, activity.getUserId());
            pstmt.setString(2, activity.getAction());
            pstmt.setString(3, activity.getDetails());
            pstmt.setTimestamp(4, Timestamp.valueOf(activity.getTimestamp()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserActivity> getActivitiesByUserId(int userId, int limit) {
        List<UserActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM user_activity WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UserActivity activity = mapResultSetToUserActivity(rs);
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }
}