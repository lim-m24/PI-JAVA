package tn.esprit.Controllers;

import tn.esprit.Services.AuthService;
import tn.esprit.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class VerifyEmailController {
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();
    private String token;

    public void setToken(String token) {
        this.token = token;
        verifyEmail();
    }

    private void verifyEmail() {
        if (authService.verifyEmail(token)) {
            AlertUtils.showAlert("Success", "Email verified successfully! You can now log in.", Alert.AlertType.INFORMATION);
        } else {
            AlertUtils.showAlert("Error", "Invalid or expired verification token.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (IOException e) {
            AlertUtils.showAlert("Error", "Could not load login page", Alert.AlertType.ERROR);
        }
    }
}