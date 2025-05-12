package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Services.LieuCulturelService;
import tn.esprit.Services.PDFExportService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LieuCardController {
    @FXML private Label nomLabel;
    @FXML private Label descriptionLabel;
    @FXML private Hyperlink link3dLabel;
    @FXML private ImageView coverImageView;
    @FXML private Button favoriButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private Stage dialogStage;
    private LieuCulturel lieuCulturel;
    private final LieuCulturelService lieuCulturelService = new LieuCulturelService();
    private final PDFExportService pdfExportService = new PDFExportService();
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void initialize(Stage parentStage) {
        this.dialogStage = new Stage();
        dialogStage.initOwner(parentStage);
    }
    
    public void setLieuCulturel(LieuCulturel lieuCulturel) {
        this.lieuCulturel = lieuCulturel;
        
        nomLabel.setText(lieuCulturel.getNom());
        descriptionLabel.setText(lieuCulturel.getDescription());
        link3dLabel.setText(lieuCulturel.getLink3d());
        
        updateFavoriButton();
        
        if (lieuCulturel.getCover() != null && !lieuCulturel.getCover().isEmpty()) {
            File file = new File(lieuCulturel.getCover());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                coverImageView.setImage(image);
                coverImageView.setFitWidth(300);
                coverImageView.setFitHeight(200);
                coverImageView.setPreserveRatio(true);
            }
        }
    }
    
    @FXML
    private void handleDownloadPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.setInitialFileName(lieuCulturel.getNom().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
            try {
                pdfExportService.exportLieuCulturelToPDF(lieuCulturel.getId(), file.getAbsolutePath());
                
                // Ouvrir le PDF automatiquement
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Attention");
                        alert.setHeaderText(null);
                        alert.setContentText("Le PDF a été généré mais n'a pas pu être ouvert automatiquement.\nVous pouvez le trouver à l'emplacement : " + file.getAbsolutePath());
                        alert.showAndWait();
                        return;
                    }
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le PDF a été généré et ouvert avec succès !");
                alert.showAndWait();
                
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de la génération du PDF.");
                alert.showAndWait();
            }
        }
    }
    
    private void updateFavoriButton() {
        boolean isFavori = lieuCulturelService.isFavori(lieuCulturel.getId());
        favoriButton.setText(isFavori ? "★" : "☆");
    }
    
    @FXML
    private void handleToggleFavori() {
        lieuCulturelService.toggleFavori(lieuCulturel.getId());
        updateFavoriButton();
    }
    
    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleLink3d() {
        if (lieuCulturel.getLink3d() != null && !lieuCulturel.getLink3d().isEmpty()) {
            try {
                Desktop.getDesktop().browse(new java.net.URI(lieuCulturel.getLink3d()));
            } catch (Exception e) {
                e.printStackTrace();
                // Gérer l'erreur si nécessaire
            }
        }
    }
    
    @FXML
    private void handleViewMedia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/MediaView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage mediaStage = new Stage();
            mediaStage.setTitle("Médias de " + lieuCulturel.getNom());
            mediaStage.initModality(Modality.WINDOW_MODAL);
            mediaStage.initOwner(dialogStage);
            mediaStage.setScene(scene);
            
            MediaViewController controller = loader.getController();
            controller.setDialogStage(mediaStage);
            controller.initialize(lieuCulturel.getId(), lieuCulturel.getNom());
            
            mediaStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Gérer l'erreur si nécessaire
        }
    }
    
    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/LieuCulturelDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage editStage = new Stage();
            editStage.setTitle("Modifier un lieu culturel");
            editStage.initModality(Modality.WINDOW_MODAL);
            editStage.initOwner(coverImageView.getScene().getWindow());
            editStage.setScene(scene);
            
            LieuCulturelDialogController controller = loader.getController();
            controller.setDialogStage(editStage);
            controller.setVilleId(lieuCulturel.getVilleId());
            controller.setLieuCulturel(lieuCulturel);
            
            editStage.showAndWait();
            
            if (controller.isOkClicked()) {
                // Rafraîchir les données du lieu
                LieuCulturel updatedLieu = lieuCulturelService.getLieuById(lieuCulturel.getId());
                if (updatedLieu != null) {
                    setLieuCulturel(updatedLieu);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage du dialogue de modification.");
        }
    }
    
    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le lieu culturel");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce lieu culturel ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Supprimer l'image associée si elle existe
                if (lieuCulturel.getCover() != null && !lieuCulturel.getCover().isEmpty()) {
                    File imageFile = new File(lieuCulturel.getCover());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                // Supprimer le lieu de la base de données
                lieuCulturelService.deleteLieuCulturel(lieuCulturel.getId());
                
                // Fermer la carte
                if (dialogStage != null) {
                    dialogStage.close();
                }
                
                // Rafraîchir la vue en rechargeant les lieux culturels
                if (lieuCulturel.getVilleId() != null) {
                    lieuCulturelService.getLieuxCulturelsByVille(lieuCulturel.getVilleId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de la suppression du lieu culturel.");
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
    
    public void hideAdminButtons() {
        if (editButton != null) editButton.setVisible(false);
        if (deleteButton != null) deleteButton.setVisible(false);
    }
} 