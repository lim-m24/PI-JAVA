package tn.esprit.Controllers;

import tn.esprit.Models.User;
import tn.esprit.Services.AuthService;
import tn.esprit.Services.FacialRecognitionService;
import tn.esprit.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordBtn;
    @FXML private Button faceRecognitionBtn;

    private boolean passwordVisible = false;
    private final AuthService authService = new AuthService();
    private final FacialRecognitionService facialRecognitionService = new FacialRecognitionService();

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisible) {
                visiblePasswordField.setText(newVal);
            }
        });

        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisible) {
                passwordField.setText(newVal);
            }
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
            visiblePasswordField.requestFocus();
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordBtn.setText("👁️");
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (authService.authenticate(email, password)) {
                User user = authService.getCurrentUser(email);
                processSuccessfulLogin(user);
            } else {
                showAlert("Échec de connexion", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFaceLogin() {
        showAlert("Information", "Démarrage de la reconnaissance faciale...", Alert.AlertType.INFORMATION);

        CompletableFuture.supplyAsync(() -> {
            try {
                return facialRecognitionService.recognizeFace();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(user -> {
            Platform.runLater(() -> {
                if (user != null) {
                    processSuccessfulLogin(user);
                } else {
                    showAlert("Échec de la reconnaissance", "Impossible de vous reconnaître. Veuillez réessayer ou utiliser la connexion traditionnelle.", Alert.AlertType.ERROR);
                }
            });
        });
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/ResetPasswordRequest.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Request Password Reset");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page de réinitialisation : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegisterLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.setMaximized(true);
            stage.show();
            ((Stage) emailField.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'inscription : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void processSuccessfulLogin(User user) {
        SessionManager.getInstance().setCurrentUser(user);
        SessionManager.getInstance().setAttribute("lastLogin", LocalDateTime.now());

        Integer loginCount = (Integer) SessionManager.getInstance().getAttribute("loginCount");
        if (loginCount == null) {
            loginCount = 0;
        }
        SessionManager.getInstance().setAttribute("loginCount", loginCount + 1);

        String errorMessage = authService.validateLoginConditions(user);
        if (errorMessage != null) {
            displayErrorInUI(errorMessage);
            return;
        }

        try {
            FXMLLoader loader;
            String fxmlPath;
            String title;
            Parent root;

            if ("ROLE_USER".equals(user.getRole())) {
                fxmlPath = "/views/user/FrontOffice.fxml";
                title = "SyncYLinkY Front Office";
            } else {
                fxmlPath = "/views/main.fxml";
                title = "SyncYLinkY Admin";
            }

            loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Impossible de trouver le fichier FXML à : " + fxmlPath);
            }
            loader.setLocation(fxmlUrl);
            root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement de l'interface : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void registerFace() {
        String email = emailField.getText();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir email et mot de passe pour enregistrer votre visage", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (authService.authenticate(email, password)) {
                User user = authService.getCurrentUser(email);
                showAlert("Information", "Préparation de l'enregistrement du visage. Veuillez regarder la caméra.", Alert.AlertType.INFORMATION);

                CompletableFuture.supplyAsync(() -> {
                    try {
                        return facialRecognitionService.registerFace(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }).thenAccept(success -> {
                    Platform.runLater(() -> {
                        if (success) {
                            showAlert("Succès", "Votre visage a été enregistré avec succès. Vous pouvez maintenant vous connecter par reconnaissance faciale.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Erreur", "Échec de l'enregistrement du visage. Veuillez réessayer.", Alert.AlertType.ERROR);
                        }
                    });
                });
            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect. Authentification requise pour enregistrer votre visage.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void displayErrorInUI(String errorMessage) {
        HBox errorBox = new HBox();
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setStyle("-fx-background-color: #1e2532; -fx-padding: 15px; -fx-background-radius: 5px;");
        errorBox.setPrefWidth(500);
        errorBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label(errorMessage);
        errorLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        errorBox.getChildren().add(errorLabel);

        Pane parent = (Pane) emailField.getParent().getParent();
        parent.getChildren().removeIf(node ->
                node instanceof HBox &&
                        node.getStyle() != null &&
                        node.getStyle().contains("-fx-background-color: #1e2532")
        );

        parent.getChildren().add(2, errorBox);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}