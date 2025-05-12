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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MediaListController {
    
    @FXML private Label titleLabel;
    @FXML private FlowPane mediaContainer;
    
    private final MediaService mediaService = new MediaService();
    private Stage dialogStage;
    private Long lieuId;
    private String lieuName;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void loadMedia(Long lieuId, String lieuName) {
        this.lieuId = lieuId;
        this.lieuName = lieuName;
        titleLabel.setText("Médias de " + lieuName);
        refreshMediaList();
    }
    
    private void refreshMediaList() {
        mediaContainer.getChildren().clear();
        
        for (Media media : mediaService.getMediaByLieuId(lieuId)) {
            VBox mediaBox = createMediaBox(media);
            mediaContainer.getChildren().add(mediaBox);
        }
    }
    
    private VBox createMediaBox(Media media) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-padding: 5;");
        
        if (media.getType().startsWith("image/")) {
            ImageView imageView = new ImageView(new Image(new File(media.getLink()).toURI().toString()));
            imageView.setFitWidth(200);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);
            box.getChildren().add(imageView);
        } else if (media.getType().startsWith("video/")) {
            Label videoLabel = new Label("Vidéo");
            videoLabel.setStyle("-fx-font-size: 16px;");
            box.getChildren().add(videoLabel);
        }
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("button-delete");
        deleteButton.setOnAction(e -> handleDelete(media));
        
        box.getChildren().add(deleteButton);
        return box;
    }
    
    @FXML
    private void showAddDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un média");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                String mimeType = Files.probeContentType(selectedFile.toPath());
                if (mimeType != null && (mimeType.startsWith("image/") || mimeType.startsWith("video/"))) {
                    Media savedMedia = mediaService.saveMedia(lieuId, selectedFile);
                    if (savedMedia != null) {
                        refreshMediaList();
                    }
                } else {
                    showError("Type de fichier non supporté", "Veuillez sélectionner une image ou une vidéo.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors du téléchargement du fichier.");
            }
        }
    }
    
    private void handleDelete(Media media) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le média");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce média ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (mediaService.deleteMedia(media)) {
                refreshMediaList();
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