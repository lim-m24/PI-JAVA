package tn.esprit.Controllers;

import tn.esprit.Models.Categories;
import tn.esprit.Models.User;
import tn.esprit.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class UserDetailsController {
    @FXML private Label idLabel;
    @FXML private Label emailLabel;
    @FXML private Label nameLabel;
    @FXML private Label firstnameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label dateOBLabel;
    @FXML private Label genderLabel;
    @FXML private Label roleLabel;
    @FXML private ListView<String> interestsListView;
    @FXML private Label bannedLabel;
    @FXML private Label verifiedLabel;

    private final User user;
    private final UserController parentController;

    public UserDetailsController(UserController parentController, User user) {
        this.parentController = parentController;
        this.user = user;
    }

    @FXML
    public void initialize() {
        // Afficher les détails de l'utilisateur
        idLabel.setText(String.valueOf(user.getId()));
        emailLabel.setText(user.getEmail());
        nameLabel.setText(user.getName());
        firstnameLabel.setText(user.getFirstname());
        usernameLabel.setText(user.getUsername());
        dateOBLabel.setText(user.getDateOB().toString());
        genderLabel.setText(user.getGender());
        roleLabel.setText(user.getRole());

        // Afficher les centres d'intérêt
        for (Categories interest : user.getInterests()) {
            interestsListView.getItems().add(interest.getNom());
        }

        // Afficher le statut
        bannedLabel.setText(user.isBanned() ? "Banned" : "Not banned");
        bannedLabel.setStyle(user.isBanned() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        verifiedLabel.setText(user.isVerified() ? "Verified" : "Not verified");
        verifiedLabel.setStyle(user.isVerified() ? "-fx-text-fill: green;" : "-fx-text-fill: orange;");
    }

    @FXML
    private void handleEdit() {
        parentController.handleEditUser(user);
    }

    @FXML
    private void handleBack() {
        parentController.refreshUsers();
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (parentController.deleteUser(user)) {
                    AlertUtils.showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                    handleBack();
                } else {
                    AlertUtils.showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
                }
            }
        });
    }
}