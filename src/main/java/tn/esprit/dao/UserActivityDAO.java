package tn.esprit.dao;

import tn.esprit.Models.UserActivity;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityDAO {
    /**
     * Gets the most recent activities
     * @param limit The maximum number of activities to return
     * @return A list of recent activities
     */
    List<UserActivity> getRecentActivities(int limit);

    /**
     * Gets activities between two dates
     * @param start The start date
     * @param end The end date
     * @return A list of activities between the specified dates
     */
    List<UserActivity> getActivitiesBetweenDates(LocalDateTime start, LocalDateTime end);

    /**
     * Adds a new activity
     * @param activity The activity to add
     * @return True if successful, false otherwise
     */
    boolean addActivity(UserActivity activity);

    /**
     * Gets activities for a specific user
     * @param userId The user ID
     * @param limit The maximum number of activities to return
     * @return A list of activities for the specified user
     */
    List<UserActivity> getActivitiesByUserId(int userId, int limit);
}