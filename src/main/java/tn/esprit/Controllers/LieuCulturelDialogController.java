package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Services.LieuCulturelService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LieuCulturelDialogController {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField link3dField;
    @FXML private ImageView imagePreview;
    @FXML private Label imagePathLabel;
    
    private Stage dialogStage;
    private LieuCulturel lieuCulturel;
    private boolean okClicked = false;
    private String selectedImagePath;
    private Long villeId;
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setVilleId(Long villeId) {
        this.villeId = villeId;
    }
    
    public void setLieuCulturel(LieuCulturel lieuCulturel) {
        this.lieuCulturel = lieuCulturel;
        
        if (lieuCulturel != null) {
            nomField.setText(lieuCulturel.getNom() != null ? lieuCulturel.getNom() : "");
            descriptionField.setText(lieuCulturel.getDescription() != null ? lieuCulturel.getDescription() : "");
            link3dField.setText(lieuCulturel.getLink3d() != null ? lieuCulturel.getLink3d() : "");
            
            if (lieuCulturel.getCover() != null && !lieuCulturel.getCover().isEmpty()) {
                File imageFile = new File(lieuCulturel.getCover());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                    imagePathLabel.setText(imageFile.getName());
                }
            }
        }
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // Créer le dossier d'upload s'il n'existe pas
                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                
                // Générer un nom de fichier unique
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadDir.resolve(fileName);
                
                // Copier le fichier
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Mettre à jour l'aperçu et le chemin
                Image image = new Image(targetPath.toUri().toString());
                imagePreview.setImage(image);
                imagePathLabel.setText(fileName);
                selectedImagePath = targetPath.toString();
                
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors du téléchargement de l'image : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            try {
                // Créer un nouvel objet LieuCulturel ou mettre à jour l'existant
                if (lieuCulturel == null) {
                    lieuCulturel = new LieuCulturel();
                    lieuCulturel.setVilleId(villeId);
                    lieuCulturel.setId(null); // S'assurer que l'ID est bien null pour l'insertion
                }
                
                // Mettre à jour l'objet LieuCulturel avec les nouvelles valeurs
                lieuCulturel.setNom(nomField.getText().trim());
                lieuCulturel.setDescription(descriptionField.getText().trim());
                lieuCulturel.setLink3d(link3dField.getText().trim());
                
                // Si une nouvelle image a été sélectionnée, mettre à jour le chemin
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    lieuCulturel.setCover(selectedImagePath);
                }
                
                // Sauvegarder les modifications dans la base de données
                LieuCulturelService lieuCulturelService = new LieuCulturelService();
                if (lieuCulturel.getId() == null || lieuCulturel.getId() == 0) {
                    lieuCulturel.setId(null); // S'assurer que l'ID est bien null pour l'insertion
                    lieuCulturelService.saveLieuCulturel(lieuCulturel);
                } else {
                    lieuCulturelService.updateLieuCulturel(lieuCulturel);
                }
                
                // Marquer comme OK et fermer la fenêtre
                okClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage += "Le nom est obligatoire!\n";
        }
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            errorMessage += "La description est obligatoire!\n";
        }
        if (link3dField.getText() == null || link3dField.getText().trim().isEmpty()) {
            errorMessage += "Le lien 3D est obligatoire!\n";
        }
        
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError("Champs invalides", errorMessage);
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
} 