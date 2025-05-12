package tn.esprit.Controllers;

import tn.esprit.Models.Media;
import tn.esprit.Services.MediaService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MediaViewController {
    
    @FXML private Label titleLabel;
    @FXML private FlowPane imagesContainer;
    @FXML private FlowPane videosContainer;
    
    private Stage dialogStage;
    private Long lieuId;
    private String lieuName;
    private final MediaService mediaService = new MediaService();
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void initialize(Long lieuId, String lieuName) {
        this.lieuId = lieuId;
        this.lieuName = lieuName;
        titleLabel.setText("Médias de " + lieuName);
        loadMedia();
    }
    
    private void loadMedia() {
        imagesContainer.getChildren().clear();
        videosContainer.getChildren().clear();
        
        for (Media media : mediaService.getMediaByLieuId(lieuId)) {
            try {
                // Construire le chemin absolu du fichier
                Path mediaPath = Paths.get(media.getLink());
                File mediaFile = mediaPath.toFile();
                
                if (!mediaFile.exists()) {
                    System.err.println("Fichier non trouvé: " + mediaFile.getAbsolutePath());
                    continue;
                }
                
                if (media.getType().equals("image")) {
                    addImageToContainer(media, mediaFile);
                } else if (media.getType().equals("video")) {
                    addVideoToContainer(media, mediaFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur lors du chargement du média: " + media.getLink());
            }
        }
    }
    
    private void addImageToContainer(Media media, File imageFile) {
        try {
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            
            VBox imageBox = new VBox(5);
            imageBox.getStyleClass().add("media-box");
            
            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("button-delete");
            deleteButton.setOnAction(e -> handleDeleteMedia(media));
            
            imageBox.getChildren().addAll(imageView, deleteButton);
            imagesContainer.getChildren().add(imageBox);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger l'image: " + imageFile.getName());
        }
    }
    
    private void addVideoToContainer(Media media, File videoFile) {
        try {
            VBox videoBox = new VBox(5);
            videoBox.getStyleClass().add("media-box");
            
            javafx.scene.media.Media videoMedia = new javafx.scene.media.Media(videoFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(videoMedia);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(200);
            mediaView.setFitHeight(150);
            
            Button playButton = new Button("Lecture");
            playButton.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playButton.setText("Lecture");
                } else {
                    mediaPlayer.play();
                    playButton.setText("Pause");
                }
            });
            
            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("button-delete");
            deleteButton.setOnAction(e -> handleDeleteMedia(media));
            
            videoBox.getChildren().addAll(mediaView, playButton, deleteButton);
            videosContainer.getChildren().add(videoBox);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la vidéo: " + videoFile.getName());
        }
    }
    
    @FXML
    private void handleAddMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un média");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                Media savedMedia = mediaService.saveMedia(lieuId, selectedFile);
                if (savedMedia != null) {
                    loadMedia(); // Recharger tous les médias
                }
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors du téléchargement du fichier.");
            }
        }
    }
    
    private void handleDeleteMedia(Media media) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le média");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce média ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (mediaService.deleteMedia(media)) {
                loadMedia();
            } else {
                showError("Erreur", "Impossible de supprimer le média.");
            }
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 