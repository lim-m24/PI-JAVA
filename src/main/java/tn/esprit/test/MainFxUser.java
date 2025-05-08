package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainFxUser extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Correction du chemin avec le slash initial et vérification
        System.out.println("Tentative de chargement du FXML...");
        URL fxmlUrl = getClass().getResource("/views/auth/Login.fxml");
        if (fxmlUrl == null) {
            System.err.println("ERREUR: Fichier FXML introuvable !");
            System.err.println("Recherché à: /views/auth/Login.fxml");
            throw new RuntimeException("Fichier FXML introuvable");
        }
        System.out.println("FXML trouvé à: " + fxmlUrl);

        Parent root = FXMLLoader.load(fxmlUrl);
        primaryStage.setTitle("SyncYLinkY - Connexion");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
