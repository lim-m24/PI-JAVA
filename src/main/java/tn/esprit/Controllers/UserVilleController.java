package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Models.Ville;
import tn.esprit.Services.FavorisService;
import tn.esprit.Services.LieuCulturelService;
import tn.esprit.Services.VilleService;
import tn.esprit.Services.WeatherService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class UserVilleController implements Initializable {
    
    @FXML private ListView<Ville> villeListView;
    @FXML private VBox villeDetailsBox;
    @FXML private Label villeNameLabel;
    @FXML private Label villeDescriptionLabel;
    @FXML private FlowPane lieuxContainer;
    @FXML private Button toggleVilleFavoriteButton;
    @FXML private ToggleButton toggleFavoriteMode;
    @FXML private TextField searchVilleField;
    @FXML private TextField searchLieuField;
    @FXML
    private VBox villesListBox;
    @FXML private ImageView weatherIcon;
    @FXML private Label weatherInfo;
    @FXML private HBox weatherContainer;
    
    private final VilleService villeService = new VilleService();
    private final LieuCulturelService lieuCulturelService = new LieuCulturelService();
    private final FavorisService favorisService = new FavorisService();
    private final WeatherService weatherService = new WeatherService();
    
    private ObservableList<Ville> allVilles;
    private FilteredList<Ville> filteredVilles;
    private ObservableList<LieuCulturel> allLieux;
    private FilteredList<LieuCulturel> filteredLieux;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupVilleListView();
        
        // Initialiser les listes
        updateVillesList();
        
        // Configuration de la recherche de villes
        searchVilleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredVilles != null) {
                filteredVilles.setPredicate(createVillePredicate(newValue));
            }
        });
        
        // Configuration de la recherche de lieux culturels
        searchLieuField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLieuxCulturels(newValue);
        });
        
        // Configuration de la sélection de ville
        villeListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showVilleDetails(newValue);
                } else {
                    villeDetailsBox.setVisible(false);
                }
            }
        );
        
        // Configuration du mode favoris
        toggleFavoriteMode.selectedProperty().addListener(
            (observable, oldValue, newValue) -> updateVillesList()
        );
    }

    private void setupVilleListView() {
        villeListView.setCellFactory(lv -> new ListCell<Ville>() {
            private VBox container;
            private Label nomLabel;
            private Label descriptionLabel;
            private Label weatherLabel;
            private ImageView weatherIcon;
            private HBox weatherContainer;

            {
                container = new VBox(5);
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
                container.getStyleClass().add("ville-cell");
                container.setPadding(new javafx.geometry.Insets(10));
            }

            @Override
            protected void updateItem(Ville ville, boolean empty) {
                super.updateItem(ville, empty);
                if (empty || ville == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nomLabel.setText(ville.getNom() + (favorisService.isVilleFavorite(ville.getId()) ? " ★" : ""));
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
    
    private Predicate<Ville> createVillePredicate(String searchText) {
        return ville -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            String lowerCaseFilter = searchText.toLowerCase();
            
            return ville.getNom().toLowerCase().contains(lowerCaseFilter) ||
                   ville.getDescription().toLowerCase().contains(lowerCaseFilter);
        };
    }
    
    private void filterLieuxCulturels(String searchText) {
        lieuxContainer.getChildren().clear();
        
        if (allLieux != null) {
            allLieux.stream()
                .filter(lieu -> {
                    if (searchText == null || searchText.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = searchText.toLowerCase();
                    return lieu.getNom().toLowerCase().contains(lowerCaseFilter) ||
                           lieu.getDescription().toLowerCase().contains(lowerCaseFilter);
                })
                .forEach(lieu -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/LieuCard.fxml"));
                        VBox lieuCard = loader.load();
                        LieuCardController controller = loader.getController();
                        controller.setLieuCulturel(lieu);
                        controller.hideAdminButtons();
                        lieuxContainer.getChildren().add(lieuCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }
    
    @FXML
    private void handleRetourListe() {
        // Afficher la liste des villes
        villesListBox.setVisible(true);
        villesListBox.setManaged(true);
        // Cacher les détails de la ville
        villeDetailsBox.setVisible(false);
        villeDetailsBox.setManaged(false);
        // Désélectionner la ville actuelle
        villeListView.getSelectionModel().clearSelection();
    }

    private void showVilleDetails(Ville ville) {
        if (ville != null) {
            villesListBox.setVisible(false);
            villesListBox.setManaged(false);
            villeDetailsBox.setVisible(true);
            villeDetailsBox.setManaged(true);
            
            villeNameLabel.setText(ville.getNom());
            villeDescriptionLabel.setText(ville.getDescription());
            toggleVilleFavoriteButton.setText(favorisService.isVilleFavorite(ville.getId()) ? "★" : "☆");
            
            // Charger la météo de manière asynchrone
            new Thread(() -> {
                try {
                    WeatherService.WeatherData weather = weatherService.getWeatherData(ville.getNom());
                    Platform.runLater(() -> {
                        if (weather != null) {
                            weatherInfo.setText(String.format("%.1f°C - %s", 
                                weather.getTemperature(), 
                                weather.getDescription()));
                            weatherIcon.setImage(new Image(weather.getIconUrl()));
                            weatherContainer.setVisible(true);
                        } else {
                            weatherInfo.setText("Météo non disponible");
                            weatherIcon.setImage(null);
                            weatherContainer.setVisible(true);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        weatherInfo.setText("Erreur de chargement météo");
                        weatherIcon.setImage(null);
                        weatherContainer.setVisible(true);
                    });
                }
            }).start();
            
            // Charger les lieux culturels
            allLieux = lieuCulturelService.getLieuxCulturelsByVille(ville.getId());
            filterLieuxCulturels(searchLieuField.getText());
        }
    }
    
    @FXML
    private void showFavoriteVilles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/FavoriteVilles.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle("Villes Favorites");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage des villes favorites.");
        }
    }
    
    @FXML
    private void showFavoriteLieux() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/FavoriteLieuxView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle("Lieux Culturels Favoris");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage des lieux favoris.");
        }
    }
    
    @FXML
    private void toggleVilleFavorite() {
        Ville selectedVille = villeListView.getSelectionModel().getSelectedItem();
        if (selectedVille != null) {
            favorisService.toggleVilleFavorite(selectedVille.getId());
            toggleVilleFavoriteButton.setText(favorisService.isVilleFavorite(selectedVille.getId()) ? "★" : "☆");
            updateVillesList();
        }
    }
    
    private void updateVillesList() {
        // Récupérer la liste appropriée (toutes les villes ou favoris)
        allVilles = toggleFavoriteMode.isSelected() ? 
            FXCollections.observableArrayList(favorisService.getFavoriteVilles()) :
            villeService.getAllVilles();
        
        // Créer une nouvelle FilteredList
        filteredVilles = new FilteredList<>(allVilles);
        
        // Appliquer le filtre actuel
        String currentFilter = searchVilleField.getText();
        if (currentFilter != null && !currentFilter.isEmpty()) {
            filteredVilles.setPredicate(createVillePredicate(currentFilter));
        }
        
        // Mettre à jour la ListView
        villeListView.setItems(filteredVilles);
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 