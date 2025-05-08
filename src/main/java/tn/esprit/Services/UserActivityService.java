package tn.esprit.Services;

import tn.esprit.dao.UserActivityDAO;
import tn.esprit.dao.UserActivityDAOImpl;
import tn.esprit.Models.UserActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserActivityService {
    private final UserActivityDAO activityDAO = new UserActivityDAOImpl(); // Use real DAO implementation

    public List<UserActivity> getRecentActivities(int limit) {
        return activityDAO.getRecentActivities(limit);
    }

    public List<UserActivity> getActivitiesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return activityDAO.getActivitiesBetweenDates(start, end);
    }

    public Map<LocalDate, Map<String, Long>> getActivityCountByDate(LocalDate startDate, LocalDate endDate) {
        List<UserActivity> activities = activityDAO.getActivitiesBetweenDates(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        return activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getTimestamp().toLocalDate(),
                        Collectors.groupingBy(
                                UserActivity::getAction,
                                Collectors.counting()
                        )
                ));
    }

    public boolean addActivity(UserActivity activity) {
        return activityDAO.addActivity(activity);
    }

    public List<UserActivity> getActivitiesByUserId(int userId, int limit) {
        return activityDAO.getActivitiesByUserId(userId, limit);
    }
    public Map<LocalDate, Integer> getDailyLoginCounts(LocalDate startDate, LocalDate endDate) {
        // Fetch activities between the given dates
        List<UserActivity> activities = activityDAO.getActivitiesBetweenDates(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        // Filter for LOGIN actions and group by date
        return activities.stream()
                .filter(activity -> "LOGIN".equals(activity.getAction()))
                .collect(Collectors.groupingBy(
                        activity -> activity.getTimestamp().toLocalDate(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
}