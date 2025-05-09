
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
    private BorderPane FrontBorderpane;

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
            FrontBorderpane.setCenter(userAbonnementsPane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadCategoriesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/communityFront.fxml"));
            AnchorPane comunityfrontpane = loader.load();
            ComunityFrontController controller = loader.getController();
            controller.setFrontBorderpane(FrontBorderpane);
            FrontBorderpane.setCenter(comunityfrontpane);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}