package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.Models.Categories;
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
    private BorderPane mainBorderPane;
    @FXML
    private Button CategoriesBTN;
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
            mainBorderPane.setRight(categoriesView);
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
