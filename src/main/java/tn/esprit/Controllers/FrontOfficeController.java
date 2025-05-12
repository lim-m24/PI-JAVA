package tn.esprit.Controllers;

import tn.esprit.Models.Post;
import tn.esprit.Models.User;
import tn.esprit.Services.PostService;
import tn.esprit.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class FrontOfficeController {

    @FXML private Text welcomeMessage;
    @FXML private VBox userProfileBox;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Button notificationButton;
    @FXML private VBox feedBox;
    @FXML private TextField newPostField;

    private User currentUser;
    private boolean isNightMode = true;
    private ContextMenu userMenu;
    private CheckBox nightModeCheckBox;
    private PostService postService;
    private VBox newPostBox; // Pour stocker la zone de création de publication

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        postService = new PostService();

        if (currentUser != null) {
            updateUserInfo(currentUser);
            // Charger les préférences utilisateur depuis la session avec une valeur par défaut
            Object nightModeAttr = SessionManager.getInstance().getAttribute("nightMode");
            isNightMode = (nightModeAttr instanceof Boolean) ? (Boolean) nightModeAttr : true; // Default to true
            SessionManager.getInstance().setAttribute("nightMode", isNightMode); // Persist default value
            if (isNightMode) {
                applyNightMode();
            }
        }

        // Charger le menu contextuel
        userMenu = new ContextMenu();
        MenuItem upgradeItem = new MenuItem("Upgrade");
        upgradeItem.setOnAction(e -> navigateToUpgrade());
        upgradeItem.setStyle("-fx-background-color: #007BFF; -fx-text-fill: #FFFFFF;");

        MenuItem myAccountItem = new MenuItem("My Account");
        myAccountItem.setOnAction(e -> viewUserInfo());

        MenuItem nightModeItem = new MenuItem("Night Mode");
        nightModeCheckBox = new CheckBox();
        nightModeCheckBox.setSelected(isNightMode);
        nightModeCheckBox.setOnAction(e -> toggleNightMode());
        nightModeItem.setGraphic(nightModeCheckBox);

        MenuItem logoutItem = new MenuItem("Log Out");
        logoutItem.setOnAction(e -> handleLogout());
        logoutItem.setStyle("-fx-background-color: #DC3545; -fx-text-fill: #FFFFFF;");

        userMenu.getItems().addAll(upgradeItem, myAccountItem, nightModeItem, logoutItem);

        // Sauvegarder la zone de création de publication
        if (!feedBox.getChildren().isEmpty()) {
            newPostBox = (VBox) feedBox.getChildren().get(0);
        }
    }

    public void updateUserInfo(User user) {
        this.currentUser = user;
        welcomeMessage.setText("Hello, " + user.getFirstname() + "!");
        usernameLabel.setText(user.getFirstname());
        emailLabel.setText(user.getEmail());
    }



    @FXML
    private void toggleUserMenu(MouseEvent event) {
        if (userMenu.isShowing()) {
            userMenu.hide();
        } else {
            userMenu.show(userProfileBox, event.getScreenX(), event.getScreenY());
        }
    }

    @FXML
    private void navigateToMessages() {
        welcomeMessage.setText("Messages Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToCommunity() {
        welcomeMessage.setText("Community Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToVibe() {
        welcomeMessage.setText("Vibe Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToMyCommunity() {
        welcomeMessage.setText("My Community Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToPages() {
        welcomeMessage.setText("Pages Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToUpgrade() {
        welcomeMessage.setText("Upgrade Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void navigateToFeedback() {
        welcomeMessage.setText("Feedback Section");
        feedBox.getChildren().clear();
        if (newPostBox != null) {
            feedBox.getChildren().add(newPostBox);
        }
    }

    @FXML
    private void viewUserInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/EditUser.fxml"));
            loader.setController(new EditUserController(this, currentUser));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit My Information");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load edit user page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleNightMode() {
        isNightMode = nightModeCheckBox.isSelected();
        SessionManager.getInstance().setAttribute("nightMode", isNightMode);
        welcomeMessage.setText(isNightMode ? "Night Mode Enabled" : "Night Mode Disabled");
        applyNightMode();
    }

    @FXML
    private void showNotifications() {
        showAlert("Notifications", "Vous avez 0 notifications non lues.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userProfileBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showAlert("Error", "Could not load login page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyNightMode() {
        if (isNightMode) {
            // Appliquer le style nuit
            userProfileBox.setStyle("-fx-background-color: #34495E;");
            feedBox.setStyle("-fx-background-color: #1C2526;");
        } else {
            // Appliquer le style jour
            userProfileBox.setStyle("-fx-background-color: #E0E0E0;");
            feedBox.setStyle("-fx-background-color: #FFFFFF;");
        }
    }
}