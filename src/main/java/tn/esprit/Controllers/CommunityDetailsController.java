package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.Models.Community;

import java.io.File;

public class CommunityDetailsController {

    @FXML
    private ImageView coverImageView;

    @FXML
    private Label communityNameLabel;

    @FXML
    private Label memberCountLabel;

    @FXML
    private Label memberCountSideLabel;

    @FXML
    private Text descriptionText;

    @FXML
    private Button leaveButton;

    @FXML
    private Button eventTab;

    @FXML
    private Button discussionTab;

    @FXML
    private Button membersTab;

    @FXML
    private VBox eventsVBox;

    private Community community;

    public void setCommunity(Community community) {
        this.community = community;
        populateDetails();
    }

    private void populateDetails() {
        // Set community details
        communityNameLabel.setText(community.getNom());
        memberCountLabel.setText("Public community • " + community.getNbr_membre() + " Members");
        memberCountSideLabel.setText(String.valueOf(community.getNbr_membre()));
        descriptionText.setText(community.getDescription());

        // Load cover image
        String relativePath = community.getCover();
        if (relativePath != null && !relativePath.isEmpty()) {
            File file = new File("." + relativePath);
            if (file.exists()) {
                coverImageView.setImage(new Image(file.toURI().toString()));
            } else {
                System.err.println("Image not found: " + file.getAbsolutePath());
                coverImageView.setImage(new Image("/images/placeholder.jpg"));
            }
        }

        // Add sample events (you can extend this to load real events)
        addEvent("Formation With M.Olivier", "25/10 10:40 - 27/10 10:40", "0 Interested • 0 Participated");
        addEvent("Workshop Mixte Présentiel", "29/10 12:35 - 27/10 07:40", "0 Interested • 0 Participated");
    }

    private void addEvent(String title, String date, String stats) {
        VBox eventCard = new VBox(5);
        eventCard.setStyle("-fx-background-color: #2C3E50; -fx-padding: 10; -fx-border-radius: 5;");

        ImageView eventImage = new ImageView(new Image("/images/placeholder.jpg"));
        eventImage.setFitWidth(150);
        eventImage.setFitHeight(100);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill: gray;");

        Label statsLabel = new Label(stats);
        statsLabel.setStyle("-fx-text-fill: gray;");

        eventCard.getChildren().addAll(eventImage, titleLabel, dateLabel, statsLabel);
        eventsVBox.getChildren().add(eventCard);
    }
}