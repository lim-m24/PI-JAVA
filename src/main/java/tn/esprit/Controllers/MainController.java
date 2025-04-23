package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.Models.Community;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CategorieService;
import tn.esprit.Services.CommunityService;

import java.util.List;

public class MainController {

    @FXML
    private HBox categorySlider; // Changed from GridPane to HBox

    @FXML
    private GridPane communityGrid;

    private final CommunityService communityService = new CommunityService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        // Load categories dynamically
        loadCategories();

        // Load communities dynamically
        loadCommunities();
    }

    private void loadCategories() {
        // Fetch categories from the database
        List<Categories> categories = categorieService.readAll();

        if (categories == null || categories.isEmpty()) {
            Label noCategoriesLabel = new Label("No categories available.");
            noCategoriesLabel.setStyle("-fx-text-fill: gray;");
            categorySlider.getChildren().add(noCategoriesLabel);
            return;
        }

        // Dynamically populate the HBox for categories
        for (Categories category : categories) {
            // Create a VBox for each category
            VBox categoryBox = new VBox(5);

            // Create and configure the ImageView
            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);
            try {
                String imageUrl = category.getCover();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    imageView.setImage(new Image(imageUrl));
                } else {
                    throw new Exception("Image URL is null or empty");
                }
            } catch (Exception e) {
                System.err.println("Error loading image for category " + category.getNom() + ": " + e.getMessage());
                // Fallback to a default image if the URL fails
                imageView.setImage(new Image("/images/placeholder.jpg"));
            }

            // Create and configure the Label
            Label label = new Label(category.getNom());
            label.setStyle("-fx-text-fill: white;");

            // Add the ImageView and Label to the VBox
            categoryBox.getChildren().addAll(imageView, label);

            // Add the VBox to the HBox (slider)
            categorySlider.getChildren().add(categoryBox);
        }
    }

    private void loadCommunities() {
        // Fetch communities from the database
        List<Community> communities = communityService.readAll();

        if (communities == null || communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("No communities available.");
            noCommunitiesLabel.setStyle("-fx-text-fill: gray;");
            communityGrid.add(noCommunitiesLabel, 0, 0);
            return;
        }

        // Dynamically populate the GridPane for communities
        int col = 0, row = 0;
        for (Community community : communities) {
            VBox card = new VBox(5);

            // Create and configure the ImageView
            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);
            try {
                String coverUrl = community.getCover();
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    imageView.setImage(new Image(coverUrl));
                } else {
                    throw new Exception("Cover URL is null or empty");
                }
            } catch (Exception e) {
                System.err.println("Error loading image for community " + community.getNom() + ": " + e.getMessage());
                // Fallback to a default image if the URL fails
                imageView.setImage(new Image("/images/placeholder.jpg"));
            }

            // Create and configure the Labels
            Label nameLabel = new Label(community.getNom());
            nameLabel.setStyle("-fx-text-fill: white;");

            // Assuming getId_categorie_id returns the category name or ID, adjust as needed
            Label detailsLabel = new Label(community.getId_categorie_id() + " • " + community.getNbr_membre() + " members");
            detailsLabel.setStyle("-fx-text-fill: gray;");

            // Create buttons
            Button joinButton = new Button("Join");
            joinButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;");

            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;");

            HBox buttons = new HBox(5, joinButton, viewButton);

            // Add components to the VBox
            card.getChildren().addAll(imageView, nameLabel, detailsLabel, buttons);

            // Add the VBox to the GridPane
            communityGrid.add(card, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }
}