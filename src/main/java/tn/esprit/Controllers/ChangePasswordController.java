package tn.esprit.Controllers;

import tn.esprit.Services.AuthService;
import tn.esprit.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangePasswordController {
    @FXML private TextField resetTokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button submitButton;

    private String email;
    private final AuthService authService = new AuthService();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() {
        String token = resetTokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtils.showAlert("Erreur", "Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
            return;
        }

        try {
            boolean success = authService.resetPassword(token, newPassword);
            if (success) {
                AlertUtils.showAlert("Succès", "Votre mot de passe a été réinitialisé avec succès. Veuillez vous connecter.", Alert.AlertType.INFORMATION);
                handleBackToLogin();
            } else {
                AlertUtils.showAlert("Erreur", "Token invalide ou expiré.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            AlertUtils.showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (IOException e) {
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}