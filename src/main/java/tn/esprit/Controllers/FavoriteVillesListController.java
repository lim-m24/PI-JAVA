package tn.esprit.Controllers;

import tn.esprit.Models.Ville;
import tn.esprit.Services.VilleService;
import tn.esprit.Services.WeatherService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class FavoriteVillesListController implements Initializable {
    
    @FXML private ListView<Ville> villeListView;
    @FXML private TextField searchField;
    
    private final VilleService villeService = new VilleService();
    private final WeatherService weatherService = new WeatherService();
    private ObservableList<Ville> allVilles;
    private FilteredList<Ville> filteredVilles;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        setupSearch();
        loadFavoriteVilles();
    }
    
    private void setupListView() {
        villeListView.setCellFactory(lv -> new ListCell<Ville>() {
            private final VBox container;
            private final Label nomLabel;
            private final Label descriptionLabel;
            private final Label weatherLabel;
            private final ImageView weatherIcon;
            private final HBox weatherContainer;
            
            {
                container = new VBox(5);
                container.setPadding(new Insets(5, 0, 5, 0));
                
                nomLabel = new Label();
                nomLabel.getStyleClass().add("ville-nom");
                
                descriptionLabel = new Label();
                descriptionLabel.getStyleClass().add("ville-description");
                descriptionLabel.setWrapText(true);
                
                weatherLabel = new Label();
                weatherLabel.getStyleClass().add("weather-info");
                
                weatherIcon = new ImageView();
                weatherIcon.setFitHeight(30);
                weatherIcon.setFitWidth(30);
                
                weatherContainer = new HBox(10);
                weatherContainer.getChildren().addAll(weatherIcon, weatherLabel);
                
                container.getChildren().addAll(nomLabel, descriptionLabel, weatherContainer);
            }
            
            @Override
            protected void updateItem(Ville ville, boolean empty) {
                super.updateItem(ville, empty);
                
                if (empty || ville == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nomLabel.setText(ville.getNom());
                    descriptionLabel.setText(ville.getDescription());
                    
                    // Charger la météo de manière asynchrone
                    new Thread(() -> {
                        try {
                            WeatherService.WeatherData weather = weatherService.getWeatherData(ville.getNom());
                            Platform.runLater(() -> {
                                if (weather != null) {
                                    weatherLabel.setText(String.format("%.1f°C - %s", 
                                        weather.getTemperature(), 
                                        weather.getDescription()));
                                    weatherIcon.setImage(new Image(weather.getIconUrl()));
                                } else {
                                    weatherLabel.setText("Météo non disponible");
                                    weatherIcon.setImage(null);
                                }
                            });
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                weatherLabel.setText("Erreur de chargement météo");
                                weatherIcon.setImage(null);
                            });
                        }
                    }).start();
                    
                    setGraphic(container);
                }
            }
        });
        
        villeListView.getStyleClass().add("villes-list");
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredVilles != null) {
                filteredVilles.setPredicate(createVillePredicate(newValue));
            }
        });
    }
    
    private Predicate<Ville> createVillePredicate(String searchText) {
        return ville -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            String lowerCaseFilter = searchText.toLowerCase();
            return ville.getNom().toLowerCase().contains(lowerCaseFilter) ||
                   ville.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                   ville.getPosition().toLowerCase().contains(lowerCaseFilter);
        };
    }
    
    private void loadFavoriteVilles() {
        try {
            allVilles = FXCollections.observableArrayList(villeService.getFavoriteVilles());
            filteredVilles = new FilteredList<>(allVilles);
            villeListView.setItems(filteredVilles);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les villes favorites");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void refreshView() {
        loadFavoriteVilles();
    }
} 