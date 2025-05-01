package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.Models.Categories;
import tn.esprit.Models.Community;
import tn.esprit.Services.CommunityService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryCommunitiesController {

    @FXML
    private Label categoryNameLabel;

    @FXML
    private GridPane communityGrid;

    private Categories category;
    private BorderPane FrontBorderpane; // Reference to the parent BorderPane
    private final CommunityService communityService = new CommunityService();

    // Setter to inject the category
    public void setCategory(Categories category) {
        this.category = category;
        categoryNameLabel.setText(category.getNom() + " Communities");
        loadCommunities();
    }

    // Setter to inject the BorderPane
    public void setFrontBorderpane(BorderPane frontBorderpane) {
        this.FrontBorderpane = frontBorderpane;
    }

    private void loadCommunities() {
        communityGrid.getChildren().clear();

        List<Community> allCommunities = communityService.readAll();

        // Filter communities by the selected category's id
        List<Community> communities = allCommunities.stream()
                .filter(community -> community.getId_categorie_id() == category.getId())
                .collect(Collectors.toList());

        if (communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("No communities available in this category.");
            noCommunitiesLabel.setStyle("-fx-text-fill: gray;");
            communityGrid.add(noCommunitiesLabel, 0, 0);
            return;
        }

        int col = 0, row = 0;
        for (Community community : communities) {
            VBox card = new VBox(5);
            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);
            String relativePath = community.getCover();

            if (relativePath != null && !relativePath.isEmpty()) {
                File file = new File("." + relativePath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    System.err.println("Image not found: " + file.getAbsolutePath());
                    imageView.setImage(new Image("/images/placeholder.jpg"));
                }
            }

            Label nameLabel = new Label(community.getNom());
            nameLabel.setStyle("-fx-text-fill: white;");
            Label detailsLabel = new Label(community.getId_categorie_id() + " • " + community.getNbr_membre() + " members");
            detailsLabel.setStyle("-fx-text-fill: gray;");

            Button joinButton = new Button("Join");
            joinButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;");
            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;");

            // Add action to the View button
            viewButton.setOnAction(event -> {
                if (FrontBorderpane == null) {
                    System.err.println("FrontBorderpane is not set in CategoryCommunitiesController.");
                    return;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommunityDetails.fxml"));
                    AnchorPane communityDetailsPane = loader.load();

                    CommunityDetailsController controller = loader.getController();
                    controller.setCommunity(community);
                    controller.setFrontBorderpane(FrontBorderpane);

                    FrontBorderpane.setCenter(communityDetailsPane);
                } catch (IOException e) {
                    System.err.println("Error loading CommunityDetails.fxml: " + e.getMessage());
                }
            });

            HBox buttons = new HBox(5, joinButton, viewButton);
            card.getChildren().addAll(imageView, nameLabel, detailsLabel, buttons);
            communityGrid.add(card, col, row);
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }
}