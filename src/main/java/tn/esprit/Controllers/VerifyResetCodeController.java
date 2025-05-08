package tn.esprit.Controllers;

import tn.esprit.Services.AuthService;
import tn.esprit.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class VerifyResetCodeController {
    @FXML private TextField codeField;
    @FXML private Button submitButton;
    @FXML private Button backButton;

    private String email;
    private final AuthService authService = new AuthService();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleSubmit() {
        String code = codeField.getText().trim();

        if (email == null || email.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Email non défini. Veuillez recommencer le processus.", Alert.AlertType.ERROR);
            return;
        }

        if (code.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Veuillez entrer le code de réinitialisation", Alert.AlertType.ERROR);
            return;
        }

        try {
            boolean valid = authService.verifyResetCode(email, code);
            if (valid) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/ResetPassword.fxml"));
                Parent root = loader.load();
                ChangePasswordController controller = loader.getController();
                controller.setEmail(email);
                Scene scene = new Scene(root);
                Stage stage = (Stage) submitButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Change Password");
            } else {
                AlertUtils.showAlert("Erreur", "Code invalide ou expiré.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            AlertUtils.showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/ResetPasswordRequest.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Request Password Reset");
        } catch (IOException e) {
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de réinitialisation : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}