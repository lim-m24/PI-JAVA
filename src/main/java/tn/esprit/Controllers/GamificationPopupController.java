package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.Models.Gamification;

import java.util.List;

public class GamificationPopupController {

    @FXML
    private Label titleLabel;

    @FXML
    private VBox gamificationList;

    @FXML
    private Label noGamificationLabel;

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setGamifications(List<Gamification> gamifications) {
        gamificationList.getChildren().clear();
        if (gamifications.isEmpty()) {
            noGamificationLabel.setVisible(true);
        } else {
            noGamificationLabel.setVisible(false);
            for (Gamification gamification : gamifications) {
                Label gamificationLabel = new Label("- " + gamification.getNom() + ": " + gamification.getDescription());
                gamificationLabel.getStyleClass().add("gamification-item"); // Apply the CSS class
                gamificationList.getChildren().add(gamificationLabel);
            }
        }
    }
}