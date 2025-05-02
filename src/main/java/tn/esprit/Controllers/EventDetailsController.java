package tn.esprit.Controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import tn.esprit.Models.Events;
import tn.esprit.Services.EventService;
import tn.esprit.Services.EventUserService;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.json.JSONObject;

public class EventDetailsController {

    @FXML
    private ImageView coverImageView;

    @FXML
    private Label eventDateLabel;

    @FXML
    private Label eventTimeRangeLabel;

    @FXML
    private Label eventNameLabel;

    @FXML
    private Label eventTypeLabel;

    @FXML
    private Label timerDaysLabel;

    @FXML
    private Label timerHoursLabel;

    @FXML
    private Label timerMinutesLabel;

    @FXML
    private Label timerSecondsLabel;

    @FXML
    private Text eventDescriptionText;

    @FXML
    private Button interestedButton;

    @FXML
    private Button goingButton;

    @FXML
    private Label interestedCountLabel;

    @FXML
    private Label goingCountLabel;

    @FXML
    private VBox inviteFriendsVBox;

    @FXML
    private Label weatherTempLabel;

    @FXML
    private Label weatherWindLabel;

    @FXML
    private Label weatherPrecipLabel;

    private Events event;
    private final EventService eventService = new EventService();
    private final EventUserService eventUserService = new EventUserService();
    private Timeline timer;

    public void setEvent(Events event) {
        this.event = event;
        populateDetails();
        startTimer();
        loadStats();
        loadWeatherData();
    }

    private void populateDetails() {
        eventNameLabel.setText(event.getNom());
        eventDateLabel.setText(String.valueOf(event.getStarted_at().toLocalDate().getDayOfMonth()));
        eventTimeRangeLabel.setText(event.getStarted_at().toLocalDate().toString() + " - " + event.getFinish_at().toLocalDate().toString());
        eventTypeLabel.setText(event.getType());
        eventDescriptionText.setText(event.getDescription());

        String relativePath = event.getCover();
        if (relativePath != null && !relativePath.isEmpty()) {
            File file = new File("." + relativePath);
            if (file.exists()) {
                coverImageView.setImage(new Image(file.toURI().toString()));
            } else {
                coverImageView.setImage(new Image("/images/placeholder.jpg"));
            }
        } else {
            coverImageView.setImage(new Image("/images/placeholder.jpg"));
        }
    }

    private void startTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    private void updateTimer() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime finish = event.getFinish_at();
        long days = ChronoUnit.DAYS.between(now, finish);
        long hours = ChronoUnit.HOURS.between(now, finish) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, finish) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, finish) % 60;

        timerDaysLabel.setText(String.format("%02d", days));
        timerHoursLabel.setText(String.format("%02d", hours));
        timerMinutesLabel.setText(String.format("%02d", minutes));
        timerSecondsLabel.setText(String.format("%02d", seconds));

        if (days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0) {
            timer.stop();
        }
    }

    private void loadStats() {
        var stats = eventUserService.getEventStats().getOrDefault(event.getId(), new int[]{0, 0});
        interestedCountLabel.setText(String.valueOf(stats[1])); // Index 1 for Interested
        goingCountLabel.setText(String.valueOf(stats[0]));     // Index 0 for Going
    }

    private void loadWeatherData() {
        LocalDateTime eventDate = event.getStarted_at();
        LocalDateTime today = LocalDateTime.now();
        boolean isFutureEvent = eventDate.isAfter(today);

        try {
            String apiUrl;
            String latitude = "36.81897";
            String longitude = "10.16579";
            String dateStr = eventDate.toLocalDate().toString();

            if (isFutureEvent) {
                apiUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&daily=temperature_2m_max,wind_speed_10m_max,precipitation_sum&start_date=%s&end_date=%s",
                        latitude, longitude, dateStr, dateStr);
            } else {
                apiUrl = String.format("https://archive-api.open-meteo.com/v1/archive?latitude=%s&longitude=%s&daily=temperature_2m_max,wind_speed_10m_max,precipitation_sum&start_date=%s&end_date=%s",
                        latitude, longitude, dateStr, dateStr);
            }

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject daily = jsonResponse.getJSONObject("daily");

            double temp = daily.getJSONArray("temperature_2m_max").getDouble(0);
            double windSpeed = daily.getJSONArray("wind_speed_10m_max").getDouble(0);
            double precipitation = daily.getJSONArray("precipitation_sum").getDouble(0);

            weatherTempLabel.setText(String.format("%.1f °C", temp));
            weatherWindLabel.setText(String.format("%.1f km/h", windSpeed));
            weatherPrecipLabel.setText(String.format("%.1f mm", precipitation));

        } catch (Exception e) {
            weatherTempLabel.setText("N/A");
            weatherWindLabel.setText("N/A");
            weatherPrecipLabel.setText("N/A");
            System.err.println("Error fetching weather data: " + e.getMessage());
        }
    }

    @FXML
    private void handleInterested() {
        System.out.println("User marked as interested in event: " + event.getNom());
        loadStats();
    }

    @FXML
    private void handleGoing() {
        System.out.println("User marked as going to event: " + event.getNom());
        loadStats();
    }
}