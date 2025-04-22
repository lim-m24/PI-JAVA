package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.SplitPane;



import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.Services.CategorieService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class AdminController {
    private final CategorieService service=new CategorieService();


    @FXML
    private ImageView logoImageView;
    @FXML
    private ImageView logoImageView2;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button CategoriesBTN;
    @FXML
    private Button btnAbonnements;
    @FXML
    private Button btnGamifications;
    @FXML
    private Button CommunityBTN;
    @FXML
    private Text welcomeText;

    @FXML
    void initialize() throws SQLException {
        welcomeText.setText("Welcome to Syncylinky!");
        CategoriesBTN.setOnAction(event -> loadAllCategories());
        try {
            InputStream logoStream = getClass().getResourceAsStream("/images/logo TYPO blue.png");
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                logoImageView.setImage(logo);
                logoImageView2.setImage(logo);
            } else {
                System.out.println("Logo image not found");
            }
        } catch (Exception e) {
            System.out.println("Error loading logo: " + e.getMessage());
        }
    }
    @FXML
    private void loadAllCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllCategories.fxml"));
            Node categoriesView = loader.load();
            mainBorderPane.setCenter(categoriesView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadAllCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllCommunity.fxml"));
            Node communityView = loader.load();
            mainBorderPane.setCenter(communityView);
            mainBorderPane.setRight(communityView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadAbonnements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Abonnements.fxml"));
            Node abonnementsView = loader.load();
            mainBorderPane.setCenter(abonnementsView);
            mainBorderPane.setRight(abonnementsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadGamifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Gamifications.fxml"));
            Node gamificationView = loader.load();
            mainBorderPane.setCenter(gamificationView);
            mainBorderPane.setRight(gamificationView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private VBox sidebar;
    @FXML private Button btnUsers;
    @FXML private VBox usersSubmenu;
    @FXML private Button btnCommunities;
    @FXML private VBox communitiesSubmenu;

    @FXML
    private void toggleUsersSubmenu() {
        boolean isVisible = usersSubmenu.isVisible();
        usersSubmenu.setVisible(!isVisible);
        usersSubmenu.setManaged(!isVisible);
    }

    @FXML
    private void toggleCommunitiesSubmenu() {
        boolean isVisible = communitiesSubmenu.isVisible();
        communitiesSubmenu.setVisible(!isVisible);
        communitiesSubmenu.setManaged(!isVisible);
    }

}
