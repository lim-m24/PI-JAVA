package tn.esprit.Controllers;

import tn.esprit.Models.Ville;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class VilleDialogController {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private TextField positionField;
    @FXML private Label nomErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label positionErrorLabel;
    
    private Stage dialogStage;
    private Ville ville;
    private boolean okClicked = false;
    
    @FXML
    private void initialize() {
        // Ajouter des écouteurs pour la validation en temps réel
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateNom(newValue);
        });
        
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateDescription(newValue);
        });
        
        positionField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePosition(newValue);
        });
        
        // Initialiser les labels d'erreur
        nomErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);
        positionErrorLabel.setVisible(false);
        
        // Ajouter des tooltips
        nomField.setTooltip(new Tooltip("Entrez le nom de la ville (3-50 caractères)"));
        descriptionField.setTooltip(new Tooltip("Décrivez la ville (10-500 caractères)"));
        positionField.setTooltip(new Tooltip("Format: latitude,longitude (ex: 48.8566,2.3522)"));
    }
    
    private boolean validateNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            nomErrorLabel.setText("Le nom est obligatoire");
            nomErrorLabel.setVisible(true);
            nomField.setStyle("-fx-border-color: red;");
            return false;
        }
        if (nom.trim().length() < 3) {
            nomErrorLabel.setText("Le nom doit contenir au moins 3 caractères");
            nomErrorLabel.setVisible(true);
            nomField.setStyle("-fx-border-color: red;");
            return false;
        }
        if (nom.trim().length() > 50) {
            nomErrorLabel.setText("Le nom ne peut pas dépasser 50 caractères");
            nomErrorLabel.setVisible(true);
            nomField.setStyle("-fx-border-color: red;");
            return false;
        }
        nomErrorLabel.setVisible(false);
        nomField.setStyle("");
        return true;
    }
    
    private boolean validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            descriptionErrorLabel.setText("La description est obligatoire");
            descriptionErrorLabel.setVisible(true);
            descriptionField.setStyle("-fx-border-color: red;");
            return false;
        }
        if (description.trim().length() < 10) {
            descriptionErrorLabel.setText("La description doit contenir au moins 10 caractères");
            descriptionErrorLabel.setVisible(true);
            descriptionField.setStyle("-fx-border-color: red;");
            return false;
        }
        if (description.trim().length() > 500) {
            descriptionErrorLabel.setText("La description ne peut pas dépasser 500 caractères");
            descriptionErrorLabel.setVisible(true);
            descriptionField.setStyle("-fx-border-color: red;");
            return false;
        }
        descriptionErrorLabel.setVisible(false);
        descriptionField.setStyle("");
        return true;
    }
    
    private boolean validatePosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            positionErrorLabel.setText("La position est obligatoire");
            positionErrorLabel.setVisible(true);
            positionField.setStyle("-fx-border-color: red;");
            return false;
        }
        
        // Validation du format latitude,longitude
        String[] coordinates = position.split(",");
        if (coordinates.length != 2) {
            positionErrorLabel.setText("Format invalide. Utilisez: latitude,longitude");
            positionErrorLabel.setVisible(true);
            positionField.setStyle("-fx-border-color: red;");
            return false;
        }
        
        try {
            double lat = Double.parseDouble(coordinates[0].trim());
            double lon = Double.parseDouble(coordinates[1].trim());
            
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                positionErrorLabel.setText("Coordonnées hors limites");
                positionErrorLabel.setVisible(true);
                positionField.setStyle("-fx-border-color: red;");
                return false;
            }
        } catch (NumberFormatException e) {
            positionErrorLabel.setText("Les coordonnées doivent être des nombres");
            positionErrorLabel.setVisible(true);
            positionField.setStyle("-fx-border-color: red;");
            return false;
        }
        
        positionErrorLabel.setVisible(false);
        positionField.setStyle("");
        return true;
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setVille(Ville ville) {
        this.ville = ville;
        
        nomField.setText(ville.getNom());
        descriptionField.setText(ville.getDescription());
        positionField.setText(ville.getPosition());
        
        // Valider les champs existants
        validateNom(ville.getNom());
        validateDescription(ville.getDescription());
        validatePosition(ville.getPosition());
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            ville.setNom(nomField.getText().trim());
            ville.setDescription(descriptionField.getText().trim());
            ville.setPosition(positionField.getText().trim());
            
            okClicked = true;
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        boolean isValid = true;
        
        isValid &= validateNom(nomField.getText());
        isValid &= validateDescription(descriptionField.getText());
        isValid &= validatePosition(positionField.getText());
        
        return isValid;
    }
} 