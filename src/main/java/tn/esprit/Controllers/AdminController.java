package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.SplitPane;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.Models.Post;
import tn.esprit.Models.User;
import tn.esprit.Services.PostService;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.Models.Post;
import tn.esprit.Services.CategorieService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

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
    private Button EventBTN;
    @FXML
    private Button UserBTN;
    @FXML
    private Button btnVilles;
    @FXML
    private Button homepage;

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
    private void loadUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/list.fxml"));
            Node userView = loader.load();
            mainBorderPane.setCenter(userView);
        } catch (IOException e) {
            e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadAllEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllEvents.fxml"));
            Node eventView = loader.load();
            mainBorderPane.setCenter(eventView);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadVilles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/VilleView.fxml"));
            Node villeView = loader.load();
            mainBorderPane.setCenter(villeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadHomepage(ActionEvent event) {
        try {
            // Load the communityFront.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/FrontOffice.fxml"));
            Parent communityRoot = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(communityRoot);
            scene.getStylesheets().add(getClass().getResource("/style2.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true);

            stage.show();
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
