package tn.esprit.Services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherService {
    private static final String API_KEY = "4cea4532ce3947ec1d4492b90d84c2c0"; // Remplacer par votre clé API OpenWeatherMap
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    public static class WeatherData {
        private final double temperature;
        private final String description;
        private final String iconUrl;

        public WeatherData(double temperature, String description, String iconCode) {
            this.temperature = temperature;
            this.description = description;
            this.iconUrl = String.format("http://openweathermap.org/img/w/%s.png", iconCode);
        }

        public double getTemperature() { return temperature; }
        public String getDescription() { return description; }
        public String getIconUrl() { return iconUrl; }
    }

    public WeatherData getWeatherData(String cityName) throws Exception {
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String urlString = String.format("%s?q=%s&appid=%s&units=metric&lang=fr", BASE_URL, encodedCityName, API_KEY);
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            JSONObject weatherData = new JSONObject(response.toString());
            double temperature = weatherData.getJSONObject("main").getDouble("temp");
            String description = weatherData.getJSONArray("weather")
                    .getJSONObject(0).getString("description");
            String iconCode = weatherData.getJSONArray("weather")
                    .getJSONObject(0).getString("icon");

            return new WeatherData(temperature, description, iconCode);
        } finally {
            connection.disconnect();
        }
    }

    public static class WeatherInfo {
        private final double temperature;
        private final int humidity;
        private final String description;
        private final String icon;

        public WeatherInfo(double temperature, int humidity, String description, String icon) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.description = description;
            this.icon = icon;
        }

        public double getTemperature() { return temperature; }
        public int getHumidity() { return humidity; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public WeatherInfo parseWeatherInfo(JSONObject weatherData) {
        double temperature = weatherData.getJSONObject("main").getDouble("temp");
        int humidity = weatherData.getJSONObject("main").getInt("humidity");
        String description = weatherData.getJSONArray("weather")
                .getJSONObject(0).getString("description");
        String icon = weatherData.getJSONArray("weather")
                .getJSONObject(0).getString("icon");

        return new WeatherInfo(temperature, humidity, description, icon);
    }
} 