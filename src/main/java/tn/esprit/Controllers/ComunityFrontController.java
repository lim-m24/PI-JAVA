package tn.esprit.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.esprit.Models.Categories;
import tn.esprit.Models.Community;
import tn.esprit.Services.CategorieService;
import tn.esprit.Services.CommunityService;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ComunityFrontController {
    @FXML
    private HBox categorySlider;

    @FXML
    private GridPane communityGrid;

    @FXML
    private ScrollPane categoryScrollPane;

    private BorderPane FrontBorderpane;

    @FXML
    private Button popularButton;

    private final CommunityService communityService = new CommunityService();
    private final CategorieService categorieService = new CategorieService();

    public void setFrontBorderpane(BorderPane frontBorderpane) {
        this.FrontBorderpane = frontBorderpane;
    }

    @FXML
    public void initialize() {
        loadCategories();
        loadCommunities(false);

        categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        categoryScrollPane.setCursor(Cursor.HAND);
        categoryScrollPane.setPannable(true);
        categoryScrollPane.setOnMousePressed(e -> categoryScrollPane.setCursor(Cursor.CLOSED_HAND));
        categoryScrollPane.setOnMouseReleased(e -> categoryScrollPane.setCursor(Cursor.HAND));

        popularButton.setOnAction(event -> loadCommunities(true));
    }

    private void loadCategories() {
        categorySlider.setPrefWidth(Region.USE_COMPUTED_SIZE);
        categorySlider.setMinWidth(Region.USE_PREF_SIZE);

        List<Categories> categories = categorieService.readAll();

        if (categories == null || categories.isEmpty()) {
            Label noCategoriesLabel = new Label("No categories available.");
            noCategoriesLabel.setStyle("-fx-text-fill: gray;");
            categorySlider.getChildren().add(noCategoriesLabel);
            return;
        }

        double totalWidth = 0;
        for (Categories category : categories) {
            VBox categoryBox = new VBox(5);
            categoryBox.setStyle("-fx-background-color: #2C3E50; -fx-padding: 10; -fx-alignment: center;");

            FadeTransition fade = new FadeTransition(Duration.millis(400), categoryBox);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setFitWidth(150);
            String relativePath = category.getCover();

            if (relativePath != null && !relativePath.isEmpty()) {
                File file = new File("." + relativePath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    System.err.println("Image not found: " + file.getAbsolutePath());
                    imageView.setImage(new Image("/images/placeholder.jpg"));
                }
            }

            Label label = new Label(category.getNom());
            label.setStyle("-fx-text-fill: white;");
            categoryBox.getChildren().addAll(imageView, label);

            // Make the category clickable
            categoryBox.setOnMouseClicked(event -> {
                if (FrontBorderpane == null) {
                    System.err.println("FrontBorderpane is not set in ComunityFrontController.");
                    return;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategoryCommunities.fxml"));
                    AnchorPane categoryCommunitiesPane = loader.load();

                    CategoryCommunitiesController controller = loader.getController();
                    controller.setCategory(category);
                    controller.setFrontBorderpane(FrontBorderpane);

                    FrontBorderpane.setCenter(categoryCommunitiesPane);
                } catch (IOException e) {
                    System.err.println("Error loading CategoryCommunities.fxml: " + e.getMessage());
                }
            });

            categorySlider.getChildren().add(categoryBox);
            totalWidth += 150;
        }

        categorySlider.setPrefWidth(totalWidth);
    }

    private void loadCommunities(boolean sortByPopularity) {
        communityGrid.getChildren().clear();

        List<Community> communities = communityService.readAll();

        if (communities == null || communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("No communities available.");
            noCommunitiesLabel.setStyle("-fx-text-fill: gray;");
            communityGrid.add(noCommunitiesLabel, 0, 0);
            return;
        }

        if (sortByPopularity) {
            communities = communities.stream()
                    .sorted(Comparator.comparingInt(Community::getNbr_membre).reversed())
                    .collect(Collectors.toList());
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

            viewButton.setOnAction(event -> {
                if (FrontBorderpane == null) {
                    System.err.println("FrontBorderpane is not set in ComunityFrontController.");
                    return;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommunityDetails.fxml"));
                    BorderPane communityDetailsPane = loader.load();

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