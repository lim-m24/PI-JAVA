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

public class ResetPasswordRequestController {
    @FXML private TextField emailField;
    @FXML private Button submitButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleSubmit() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            AlertUtils.showAlert("Erreur", "Veuillez entrer votre adresse email", Alert.AlertType.ERROR);
            return;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            AlertUtils.showAlert("Erreur", "Format d'email invalide", Alert.AlertType.ERROR);
            return;
        }

        try {
            boolean success = authService.initiatePasswordReset(email);
            if (success) {
                AlertUtils.showAlert("Succès", "Un code de réinitialisation a été envoyé à votre email.", Alert.AlertType.INFORMATION);
                // Navigate to VerifyResetCode.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/VerifyResetCode.fxml"));
                Parent root = loader.load();
                VerifyResetCodeController controller = loader.getController();
                controller.setEmail(email);
                Scene scene = new Scene(root);
                Stage stage = (Stage) submitButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Verify Reset Code");
            } else {
                AlertUtils.showAlert("Erreur", "Aucun compte trouvé avec cet email.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            AlertUtils.showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            // Utiliser emailField pour obtenir la fenêtre, car backButton n'existe pas
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (IOException e) {
            AlertUtils.showAlert("Erreur", "Impossible de charger la page de connexion : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}