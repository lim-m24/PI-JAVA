package tn.esprit.Controllers;

import tn.esprit.Models.UserActivity;
import tn.esprit.Services.UserActivityService;
import tn.esprit.Services.UserService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserStatsController {
    private static final Logger logger = Logger.getLogger(UserStatsController.class.getName());

    private final UserService userService = new UserService();
    private final UserActivityService activityService = new UserActivityService();

    // Top metrics labels
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminUsersLabel;
    @FXML private Label newUsersTodayLabel;

    // Trend labels
    @FXML private Label totalUsersTrendLabel;
    @FXML private Label activeUsersTrendLabel;
    @FXML private Label bannedUsersTrendLabel;
    @FXML private Label adminUsersTrendLabel;
    @FXML private Label newUsersTrendLabel;

    // Charts
    @FXML private PieChart rolesChart;
    @FXML private PieChart statusChart;
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private AreaChart<String, Number> activityChart;
    @FXML private BarChart<String, Number> dailyComparisonChart;

    // User engagement metrics
    @FXML private ProgressBar loginFrequencyBar;
    @FXML private Label loginFrequencyLabel;
    @FXML private ProgressBar sessionDurationBar;
    @FXML private Label sessionDurationLabel;
    @FXML private ProgressBar retentionRateBar;
    @FXML private Label retentionRateLabel;

    // Activities table
    @FXML private TableView<UserActivity> activitiesTable;
    @FXML private ComboBox<String> actionFilterComboBox;

    // Period filter
    @FXML private ComboBox<String> periodFilter;

    private Map<String, Integer> stats;
    private Map<String, Integer> previousStats; // Pour calculer les tendances
    private ObservableList<UserActivity> allActivities = FXCollections.observableArrayList();
    private ObservableList<UserActivity> filteredActivities = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

    @FXML
    public void initialize() {
        try {
            setupFilters();
            setupAutoRefresh();
            styleCharts();
            setStats(fetchUserStats());
            setRecentActivities(fetchRecentActivities());
            refreshAllCharts(); // Rafraîchir les graphiques immédiatement
        } catch (Exception e) {
            showAlert("Initialization Error", "Failed to initialize statistics view: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            logger.log(Level.SEVERE, "Failed to initialize statistics view", e);
        }
    }

    private Map<String, Integer> fetchUserStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUserCount());
        stats.put("activeUsers", userService.getActiveUserCount());
        stats.put("bannedUsers", userService.getBannedUserCount());
        stats.put("adminUsers", userService.getAdminUserCount());
        stats.put("newUsersToday", userService.getNewUsersTodayCount());
        stats.put("moderatorUsers", userService.getCountByRole("ROLE_MODERATOR")); // Add this to UserService if needed
        return stats;
    }

    private List<UserActivity> fetchRecentActivities() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30); // Last 30 days
        return activityService.getActivitiesBetweenDates(start, end);
    }

    @FXML
    public void handleRefreshActivities() {
        setRecentActivities(fetchRecentActivities());
    }

    private void setupFilters() {
        // Configurer le filtre d'actions
        if (actionFilterComboBox != null) {
            actionFilterComboBox.getItems().addAll(
                    "All Actions", "LOGIN", "LOGOUT", "PROFILE_UPDATE", "PASSWORD_CHANGE",
                    "USER_CREATED", "USER_BANNED", "USER_DELETED"
            );
            actionFilterComboBox.setValue("All Actions");
            actionFilterComboBox.setOnAction(e -> filterActivities());
        }

        // Configurer le filtre de période
        if (periodFilter != null) {
            periodFilter.getItems().addAll("Last 7 days", "Last 30 days", "Last 3 months",
                    "Last 6 months", "Last year", "All time");
            periodFilter.setValue("Last 6 months"); // Add default value
            periodFilter.setOnAction(e -> refreshAllCharts());
        }
    }

    private void refreshAllCharts() {
        String selectedPeriod = periodFilter.getValue();
        // Refresh all charts based on selected period
        updateCharts();
    }

    private void styleCharts() {
        if (rolesChart != null) {
            rolesChart.setLabelLineLength(15);
            rolesChart.setLabelsVisible(true);
            rolesChart.setStyle("-fx-font-size: 12px; -fx-pie-color: #3182CE, #90CDF4, #E2E8F0;");
        }

        if (statusChart != null) {
            statusChart.setLabelLineLength(15);
            statusChart.setLabelsVisible(true);
            statusChart.setStyle("-fx-font-size: 12px;");
            // Appliquer des couleurs personnalisées pour chaque tranche
            statusChart.getData().forEach(data -> {
                if ("Active".equals(data.getName())) {
                    data.getNode().setStyle("-fx-pie-color: #38A169;");
                } else if ("Banned".equals(data.getName())) {
                    data.getNode().setStyle("-fx-pie-color: #E53E3E;");
                } else {
                    data.getNode().setStyle("-fx-pie-color: #ECC94B;");
                }
            });
        }

        if (registrationChart != null) {
            registrationChart.setStyle("-fx-font-size: 12px; -fx-background-color: #FFFFFF;");
        }

        if (activityChart != null) {
            activityChart.setStyle("-fx-font-size: 12px; -fx-background-color: #FFFFFF;");
        }

        if (dailyComparisonChart != null) {
            dailyComparisonChart.setStyle("-fx-font-size: 12px; -fx-background-color: #FFFFFF;");
        }
    }

    private void setupAutoRefresh() {
        // Actualisation automatique toutes les 5 minutes
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> refreshAllData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void setStats(Map<String, Integer> stats) {
        // Stocker les anciennes statistiques pour le calcul des tendances
        if (this.stats != null) {
            this.previousStats = new HashMap<>(this.stats);
        } else {
            this.previousStats = new HashMap<>();
        }

        this.stats = stats;
        updateStatsDisplay();
        updateCharts();
    }

    public void setRecentActivities(List<UserActivity> activities) {
        this.allActivities.setAll(activities);
        filterActivities();
    }

    @FXML
    public void handleExportStats() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Statistics");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File file = fileChooser.showSaveDialog(periodFilter.getScene().getWindow());

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    // Exporter les statistiques principales
                    writer.write("Metric,Value\n");
                    writer.write("Total Users," + stats.get("totalUsers") + "\n");
                    writer.write("Active Users," + stats.get("activeUsers") + "\n");
                    writer.write("Banned Users," + stats.get("bannedUsers") + "\n");
                    writer.write("Admin Users," + stats.get("adminUsers") + "\n");
                    writer.write("New Users Today," + stats.get("newUsersToday") + "\n\n");

                    // Exporter les données mensuelles
                    writer.write("Month,New Registrations\n");
                    Map<String, Integer> monthlyData = getMonthlyRegistrationData();
                    for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
                        writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                    }

                    // Exporter les activités récentes
                    writer.write("\nRecent Activities\n");
                    writer.write("User,Action,Date,Details\n");
                    for (UserActivity activity : filteredActivities) {
                        writer.write(escapeCSV(activity.getUserName()) + "," +
                                escapeCSV(activity.getAction()) + "," +
                                activity.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," +
                                escapeCSV(activity.getDetails()) + "\n");
                    }

                    showAlert("Succès", "Les statistiques ont été exportées avec succès.", Alert.AlertType.INFORMATION);
                }
            }
        } catch (IOException e) {
            showAlert("Erreur", "Échec de l'exportation : " + e.getMessage(), Alert.AlertType.ERROR);
            logger.log(Level.SEVERE, "Failed to export statistics", e);
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatsDisplay() {
        // Afficher les métriques principales
        totalUsersLabel.setText(String.valueOf(stats.get("totalUsers")));
        activeUsersLabel.setText(String.valueOf(stats.get("activeUsers")));
        bannedUsersLabel.setText(String.valueOf(stats.get("bannedUsers")));
        adminUsersLabel.setText(String.valueOf(stats.get("adminUsers")));
        newUsersTodayLabel.setText(String.valueOf(stats.get("newUsersToday")));

        // Mettre à jour les tendances
        updateTrendLabels();
    }

    private void updateTrendLabels() {
        if (previousStats.isEmpty()) return;

        updateTrendLabel(totalUsersTrendLabel, "totalUsers");
        updateTrendLabel(activeUsersTrendLabel, "activeUsers");
        updateTrendLabel(bannedUsersTrendLabel, "bannedUsers");
        updateTrendLabel(adminUsersTrendLabel, "adminUsers");
        updateTrendLabel(newUsersTrendLabel, "newUsersToday");
    }

    private void updateTrendLabel(Label label, String statKey) {
        int currentValue = stats.getOrDefault(statKey, 0);
        int previousValue = previousStats.getOrDefault(statKey, currentValue);

        if (previousValue == 0) return;

        double changePercent = ((double)(currentValue - previousValue) / previousValue) * 100;
        String trendText = String.format("%.1f%% %s", Math.abs(changePercent),
                changePercent >= 0 ? "↑" : "↓");

        label.setText(trendText);

        // Couleur en fonction de la tendance (vert pour positif, rouge pour négatif)
        // Mais pour certaines statistiques comme "bannedUsers", une baisse est positive
        boolean isPositiveTrend;
        if (statKey.equals("bannedUsers")) {
            isPositiveTrend = changePercent <= 0;
        } else {
            isPositiveTrend = changePercent >= 0;
        }

        label.setStyle("-fx-text-fill: " + (isPositiveTrend ? "green" : "red") + ";");
    }

    private void updateCharts() {
        updateRolesPieChart();
        updateStatusPieChart();
        updateRegistrationChart();
        updateActivityChart();
        updateDailyComparisonChart();
        updateEngagementMetrics();
    }

    private void updateRolesPieChart() {
        if (rolesChart == null) return;

        rolesChart.getData().clear();

        // Données pour le graphique camembert des rôles
        PieChart.Data adminSlice = new PieChart.Data("Admins", stats.get("adminUsers"));
        PieChart.Data moderatorSlice = new PieChart.Data("Moderators", stats.getOrDefault("moderatorUsers", (int)(stats.get("totalUsers") * 0.15)));
        PieChart.Data userSlice = new PieChart.Data("Regular Users",
                stats.get("totalUsers") - stats.get("adminUsers") - stats.getOrDefault("moderatorUsers", (int)(stats.get("totalUsers") * 0.15)));

        rolesChart.getData().addAll(adminSlice, moderatorSlice, userSlice);

        // Ajouter des tooltips
        for (PieChart.Data data : rolesChart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int)data.getPieValue() + " users");
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    private void updateStatusPieChart() {
        if (statusChart == null) return;

        statusChart.getData().clear();

        // Données pour le graphique camembert des statuts
        PieChart.Data activeSlice = new PieChart.Data("Active", stats.get("activeUsers"));
        PieChart.Data bannedSlice = new PieChart.Data("Banned", stats.get("bannedUsers"));
        PieChart.Data inactiveSlice = new PieChart.Data("Inactive",
                stats.get("totalUsers") - stats.get("activeUsers") - stats.get("bannedUsers"));

        statusChart.getData().addAll(activeSlice, bannedSlice, inactiveSlice);

        // Couleurs personnalisées
        activeSlice.getNode().setStyle("-fx-pie-color: #28a745;");
        bannedSlice.getNode().setStyle("-fx-pie-color: #dc3545;");
        inactiveSlice.getNode().setStyle("-fx-pie-color: #ffc107;");

        // Ajouter des tooltips
        for (PieChart.Data data : statusChart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int)data.getPieValue() + " users");
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    private void updateRegistrationChart() {
        if (registrationChart == null) return;

        registrationChart.getData().clear();

        Series<String, Number> registrationSeries = new Series<>();
        registrationSeries.setName("New Registrations");

        Map<String, Integer> monthlyData = getMonthlyRegistrationData();
        monthlyData.forEach((month, count) -> {
            registrationSeries.getData().add(new XYChart.Data<>(month, count));
        });

        registrationChart.getData().add(registrationSeries);
    }

    private void updateActivityChart() {
        if (activityChart == null) return;

        activityChart.getData().clear();

        // Agrégation des activités par date
        Map<LocalDate, Long> activityByDate = allActivities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getTimestamp().toLocalDate(),
                        Collectors.counting()
                ));

        Series<String, Number> loginSeries = new Series<>();
        loginSeries.setName("Logins");

        Series<String, Number> actionSeries = new Series<>();
        actionSeries.setName("Other Activities");

        // Récupérer les 15 derniers jours
        LocalDate today = LocalDate.now();
        for (int i = 14; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(dateFormatter);

            // Simuler la division entre logins et autres actions
            long totalActions = activityByDate.getOrDefault(date, 0L);
            long logins = totalActions > 0 ? (long) (totalActions * 0.6) : 0;
            long otherActions = totalActions - logins;

            loginSeries.getData().add(new XYChart.Data<>(dateStr, logins));
            actionSeries.getData().add(new XYChart.Data<>(dateStr, otherActions));
        }

        activityChart.getData().addAll(loginSeries, actionSeries);
    }

    private void updateDailyComparisonChart() {
        if (dailyComparisonChart == null) return;

        dailyComparisonChart.getData().clear();

        Series<String, Number> loginSeries = new Series<>();
        loginSeries.setName("Daily Logins");

        Series<String, Number> registrationSeries = new Series<>();
        registrationSeries.setName("New Registrations");

        // Get data for the last 7 days from activity service
        LocalDate today = LocalDate.now();
        Map<LocalDate, Integer> loginData = activityService.getDailyLoginCounts(today.minusDays(6), today);
        Map<LocalDate, Integer> registrationData = userService.getDailyRegistrations(today.minusDays(6), today);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(dateFormatter);

            int logins = loginData.getOrDefault(date, 0);
            int registrations = registrationData.getOrDefault(date, 0);

            loginSeries.getData().add(new XYChart.Data<>(dateStr, logins));
            registrationSeries.getData().add(new XYChart.Data<>(dateStr, registrations));
        }

        dailyComparisonChart.getData().addAll(loginSeries, registrationSeries);

        // Ajouter des tooltips
        for (Series<String, Number> series : dailyComparisonChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(series.getName() + ": " + data.getYValue());
                Tooltip.install(data.getNode(), tooltip);
            }
        }
    }

    private void filterActivities() {
        if (actionFilterComboBox == null || activitiesTable == null) return;

        String selectedAction = actionFilterComboBox.getValue();

        if ("All Actions".equals(selectedAction)) {
            filteredActivities.setAll(allActivities);
        } else {
            List<UserActivity> filtered = allActivities.stream()
                    .filter(activity -> activity.getAction().equals(selectedAction))
                    .collect(Collectors.toList());
            filteredActivities.setAll(filtered);
        }

        activitiesTable.setItems(filteredActivities);
    }

    private void refreshAllData() {
        // Update with real data instead of sample data
        setStats(fetchUserStats());
        setRecentActivities(fetchRecentActivities());
    }

    private Map<String, Integer> getMonthlyRegistrationData() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String selectedPeriod = periodFilter != null && periodFilter.getValue() != null ?
                periodFilter.getValue() : "Last 6 months";

        int monthsToShow = switch(selectedPeriod) {
            case "Last 7 days" -> 1;
            case "Last 30 days" -> 1;
            case "Last 3 months" -> 3;
            case "Last year" -> 12;
            case "All time" -> 24;
            default -> 6;
        };

        // Fetch real monthly registration data from UserService
        Map<String, Integer> monthlyData = userService.getMonthlyRegistrations(monthsToShow);
        LocalDate now = LocalDate.now();

        for (int i = monthsToShow - 1; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String month = date.format(monthFormatter);
            data.put(month, monthlyData.getOrDefault(date.getYear() + "-" + String.format("%02d", date.getMonthValue()), 0));
        }

        return data;
    }

    private void updateEngagementMetrics() {
        if (loginFrequencyBar == null || sessionDurationBar == null || retentionRateBar == null) return;

        Map<String, Double> metrics = userService.getEngagementMetrics();

        double loginFrequency = metrics.getOrDefault("loginFrequency", 0.0);
        loginFrequencyBar.setProgress(loginFrequency / 5.0); // Assuming 5 logins/week as max for scaling
        loginFrequencyLabel.setText(String.format("%.1f fois par semaine", loginFrequency));

        double sessionDuration = metrics.getOrDefault("sessionDuration", 0.0);
        sessionDurationBar.setProgress(sessionDuration / 60.0); // Assuming 60 minutes as max for scaling
        sessionDurationLabel.setText(String.format("%.0f minutes", sessionDuration));

        double retentionRate = metrics.getOrDefault("retentionRate", 0.0);
        retentionRateBar.setProgress(retentionRate / 100.0); // Percentage
        retentionRateLabel.setText(String.format("%.0f%% (30 derniers jours)", retentionRate));
    }
}