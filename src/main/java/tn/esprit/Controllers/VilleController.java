package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Models.Ville;
import tn.esprit.Services.DataUpdateService;
import tn.esprit.Services.LieuCulturelService;
import tn.esprit.Services.VilleService;
import tn.esprit.Services.WeatherService;
import javafx.application.Platform;
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
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class VilleController implements Initializable {
    
    @FXML private ListView<Ville> villeListView;
    @FXML private VBox villeDetailsBox;
    @FXML private VBox villesListBox;
    @FXML private Label villeNameLabel;
    @FXML private Label villeDescriptionLabel;
    @FXML private Label villePositionLabel;
    @FXML private FlowPane lieuxContainer;
    @FXML private TextField searchField;
    @FXML private TextField searchLieuField;
    
    private final VilleService villeService = new VilleService();
    private final LieuCulturelService lieuCulturelService = new LieuCulturelService();
    private final WeatherService weatherService = new WeatherService();
    
    private ObservableList<Ville> allVilles;
    private FilteredList<Ville> filteredVilles;
    private ObservableList<LieuCulturel> allLieux;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupVilleListView();
        setupSearch();
        loadVilles();
        
        // Configuration de la recherche de lieux culturels
        searchLieuField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLieuxCulturels(newValue);
        });
        
        // Configuration de la sélection de ville
        villeListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    showVilleDetails(newValue);
                }
            }
        );
        
        // S'abonner aux mises à jour
        DataUpdateService.getInstance().addListener(this::handleDataUpdate);
    }
    
    private void setupVilleListView() {
        villeListView.setCellFactory(lv -> new ListCell<Ville>() {
            private VBox container = new VBox(5);
            private Label nameLabel = new Label();
            private Label descLabel = new Label();
            private HBox weatherContainer = new HBox(10);
            private Label weatherLabel = new Label();
            private ImageView weatherIcon = new ImageView();

            {
                container.getChildren().addAll(nameLabel, descLabel, weatherContainer);
                weatherContainer.getChildren().addAll(weatherIcon, weatherLabel);
                weatherContainer.getStyleClass().add("weather-container");
                weatherLabel.getStyleClass().add("weather-info");
                weatherIcon.setFitHeight(20);
                weatherIcon.setFitWidth(20);
            }

            @Override
            protected void updateItem(Ville ville, boolean empty) {
                super.updateItem(ville, empty);
                if (empty || ville == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(ville.getNom());
                    descLabel.setText(ville.getDescription());
                    setGraphic(container);
                    
                    // Fetch weather data asynchronously
                    Thread weatherThread = new Thread(() -> {
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
                    });
                    weatherThread.setDaemon(true);
                    weatherThread.start();
                }
            }
        });
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
                        lieuxContainer.getChildren().add(lieuCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }
    
    private void loadVilles() {
        allVilles = villeService.getAllVilles();
        filteredVilles = new FilteredList<>(allVilles);
        villeListView.setItems(filteredVilles);
    }
    
    @FXML
    private void handleRetourListe() {
        villesListBox.setVisible(true);
        villesListBox.setManaged(true);
        villeDetailsBox.setVisible(false);
        villeDetailsBox.setManaged(false);
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
            villePositionLabel.setText("Position: " + ville.getPosition());
            
            allLieux = lieuCulturelService.getLieuxCulturelsByVille(ville.getId());
            filterLieuxCulturels(searchLieuField.getText());
        }
    }
    
    @FXML
    private void showAddDialog() {
        Ville newVille = new Ville();
        boolean okClicked = showVilleDialog(newVille, "Ajouter une ville");
        
        if (okClicked) {
            villeService.saveVille(newVille);
            loadVilles();
        }
    }
    
    @FXML
    private void handleEdit() {
        Ville selectedVille = villeListView.getSelectionModel().getSelectedItem();
        if (selectedVille != null) {
            showEditDialog(selectedVille);
        }
    }
    
    private void showEditDialog(Ville ville) {
        boolean okClicked = showVilleDialog(ville, "Modifier une ville");
        
        if (okClicked) {
            villeService.updateVille(ville);
            loadVilles();
        }
    }
    
    @FXML
    private void handleDelete() {
        Ville selectedVille = villeListView.getSelectionModel().getSelectedItem();
        if (selectedVille != null) {
            handleDelete(selectedVille);
        }
    }
    
    private void handleDelete(Ville ville) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la ville");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette ville ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            villeService.deleteVille(ville.getId());
            loadVilles();
            handleRetourListe();
        }
    }
    
    @FXML
    private void showAddLieuDialog() {
        try {
            // Create a new LieuCulturel with default values
            LieuCulturel newLieu = new LieuCulturel();
            newLieu.setId(null); // This is safe because the setter now handles null
            
            // Get the selected ville's ID
            Ville selectedVille = villeListView.getSelectionModel().getSelectedItem();
            if (selectedVille == null) {
                showError("Erreur", "Veuillez sélectionner une ville d'abord.");
                return;
            }
            newLieu.setVilleId(selectedVille.getId());
            
            // Show the dialog
            boolean okClicked = showLieuCulturelDialog(newLieu, "Ajouter un lieu culturel");
            
            if (okClicked) {
                try {
                    lieuCulturelService.saveLieuCulturel(newLieu);
                    showLieuxCulturels(selectedVille);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Erreur", "Erreur lors de l'ajout du lieu culturel.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue s'est produite.");
        }
    }
    
    private void showLieuxCulturels(Ville ville) {
        showVilleDetails(ville);
    }
    
    private boolean showVilleDialog(Ville ville, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/VilleDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(villeListView.getScene().getWindow());
            dialogStage.setScene(scene);
            
            VilleDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setVille(ville);
            
            dialogStage.showAndWait();
            return controller.isOkClicked();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage du dialogue.");
            return false;
        }
    }
    
    private boolean showLieuCulturelDialog(LieuCulturel lieu, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/LieuCulturelDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(villeListView.getScene().getWindow());
            dialogStage.setScene(scene);
            
            LieuCulturelDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setLieuCulturel(lieu);
            
            dialogStage.showAndWait();
            return controller.isOkClicked();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage du dialogue.");
            return false;
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void handleDataUpdate(String type) {
        switch (type) {
            case "VILLE_CREATE":
            case "VILLE_UPDATE":
            case "VILLE_DELETE":
                loadVilles();
                break;
            case "LIEU_CREATE":
            case "LIEU_UPDATE":
            case "LIEU_DELETE":
                Ville selectedVille = villeListView.getSelectionModel().getSelectedItem();
                if (selectedVille != null) {
                    showVilleDetails(selectedVille);
                }
                break;
        }
    }
} 