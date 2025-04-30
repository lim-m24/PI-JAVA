
package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.esprit.Models.Community;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CategorieService;
import tn.esprit.Services.CommunityService;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    private HBox categorySlider;

    @FXML
    private GridPane communityGrid;

    @FXML
    private BorderPane FrontBorderpane; // Reference to the root BorderPane

    private final CommunityService communityService = new CommunityService();
    private final CategorieService categorieService = new CategorieService();
    @FXML
    private Button viewButton;

    @FXML
    public void initialize() {
        loadCategories();
        loadCommunities();
        viewButton.setOnAction(event -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CommunityDetails.fxml"));
                Parent root = fxmlLoader.load();

                Stage stage = new Stage();
                stage.setTitle("Community Details");
                stage.setScene(new Scene(root));
                stage.show();

                // Optionnel : si tu veux passer des données (nom, description...)
                CommunityDetailsController controller = fxmlLoader.getController();
                controller.setCommunityData("Oriental", "Dance Oriental.", 4);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadCategories() {
        List<Categories> categories = categorieService.readAll();

        if (categories == null || categories.isEmpty()) {
            Label noCategoriesLabel = new Label("No categories available.");
            noCategoriesLabel.setStyle("-fx-text-fill: gray;");
            categorySlider.getChildren().add(noCategoriesLabel);
            return;
        }

        for (Categories category : categories) {
            VBox categoryBox = new VBox(5);
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
                imageView.setImage(new Image("/images/placeholder.jpg"));
            }
            Label label = new Label(category.getNom());
            label.setStyle("-fx-text-fill: white;");
            categoryBox.getChildren().addAll(imageView, label);
            categorySlider.getChildren().add(categoryBox);
        }
    }

    private void loadCommunities() {
        List<Community> communities = communityService.readAll();

        if (communities == null || communities.isEmpty()) {
            Label noCommunitiesLabel = new Label("No communities available.");
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
            try {
                String coverUrl = community.getCover();
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    imageView.setImage(new Image(coverUrl));
                } else {
                    throw new Exception("Cover URL is null or empty");
                }
            } catch (Exception e) {
                System.err.println("Error loading image for community " + community.getNom() + ": " + e.getMessage());
                imageView.setImage(new Image("/images/placeholder.jpg"));
            }
            Label nameLabel = new Label(community.getNom());
            nameLabel.setStyle("-fx-text-fill: white;");
            Label detailsLabel = new Label(community.getId_categorie_id() + " • " + community.getNbr_membre() + " members");
            detailsLabel.setStyle("-fx-text-fill: gray;");
            Button joinButton = new Button("Join");
            joinButton.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;");
            Button viewButton = new Button("View");

            viewButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;");
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

    @FXML
    public void loadUserAbonnementsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserAbonnementsView.fxml"));
            AnchorPane userAbonnementsPane = loader.load();
            // Set the loaded pane in the center of the BorderPane
            FrontBorderpane.setCenter(userAbonnementsPane);

            // You might want to get the controller of the loaded view to pass data
            // UserAbonnementsController controller = loader.getController();
            // controller.setData(someData);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., show an error message)
        }
    }
}