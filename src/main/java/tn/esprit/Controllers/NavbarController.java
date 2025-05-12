package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class NavbarController implements Initializable {
    
    @FXML
    private ImageView logoImage;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            File logoFile = new File("src/main/resources/uplods/eab72c4c-0453-4abb-a658-2114fa65bf5c.jpg");
            if (logoFile.exists()) {
                Image logo = new Image(logoFile.toURI().toString());
                logoImage.setImage(logo);
            } else {
                System.err.println("Logo file not found: " + logoFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading logo: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHome() {
        // Navigation vers l'accueil
    }
    
    @FXML
    private void handleVilles() {
        // Navigation vers la liste des villes
    }
    
    @FXML
    private void handleFavoris() {
        // Navigation vers les favoris
    }
} 