
package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    public void loadCategoriesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/communityFront.fxml"));
            AnchorPane userAbonnementsPane = loader.load();
            FrontBorderpane.setCenter(userAbonnementsPane);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., show an error message)
        }
    }
}