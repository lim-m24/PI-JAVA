package tn.esprit.Controllers;

import tn.esprit.Models.User;
import tn.esprit.Services.UserService;
import tn.esprit.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class EditUserController {
    @FXML private TextField firstNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;

    private User user;
    private UserService userService;
    private FrontOfficeController parentController;

    public EditUserController(FrontOfficeController parentController, User user) {
        this.parentController = parentController;
        this.user = user;
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        if (user != null) {
            firstNameField.setText(user.getFirstname());
            emailField.setText(user.getEmail());
        }
    }

    @FXML
    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String email = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        if (firstName.isEmpty() || email.isEmpty() || currentPassword.isEmpty()) {
            showAlert("Error", "First name, email, and current password are required", Alert.AlertType.ERROR);
            return;
        }

        // Vérifier le mot de passe actuel
        if (!userService.verifyCurrentPassword(user.getId(), currentPassword)) {
            showAlert("Error", "Current password is incorrect", Alert.AlertType.ERROR);
            return;
        }

        // Créer un objet User mis à jour avec toutes les informations nécessaires
        User updatedUser = new User(
                user.getId(),
                email,
                user.getRole(),
                user.getPassword(), // Mot de passe initial, sera mis à jour si nécessaire
                user.getName(),
                firstName,
                user.getUsername(),
                user.getDateOB(),
                user.getGender(),
                user.isBanned(),
                user.isVerified()
        );
        updatedUser.setGoogleAuthenticatorEnabled(user.isGoogleAuthenticatorEnabled());
        updatedUser.setActive(user.isActive());
        updatedUser.setInterests(user.getInterests()); // Preserve interests

        // Si un nouveau mot de passe est fourni, le changer
        if (!newPassword.isEmpty()) {
            boolean passwordChanged = userService.changePassword(user.getId(), currentPassword, newPassword);
            if (!passwordChanged) {
                showAlert("Error", "Failed to change password", Alert.AlertType.ERROR);
                return;
            }
            // Pas besoin de setPassword ici, car changePassword a déjà mis à jour le mot de passe dans la base
        }

        // Mettre à jour les autres informations de l'utilisateur
        boolean updated = userService.updateUser(updatedUser);
        if (!updated) {
            showAlert("Error", "Failed to update user information", Alert.AlertType.ERROR);
            return;
        }

        // Mettre à jour la session
        SessionManager.getInstance().updateUser(updatedUser);

        // Notifier le parent (FrontOfficeController) pour mettre à jour l'affichage
        if (parentController != null) {
            parentController.updateUserInfo(updatedUser);
        }

        // Fermer la fenêtre
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleDeactivate() {
        boolean deactivated = userService.deactivateAccount(user.getId());
        if (deactivated) {
            showAlert("Success", "Account deactivated. You will be logged out.", Alert.AlertType.INFORMATION);
            SessionManager.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) firstNameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
            } catch (IOException e) {
                showAlert("Error", "Could not load login page: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Failed to deactivate account", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}