package tn.esprit.Controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.stage.Stage;
import tn.esprit.Models.Post;
import tn.esprit.Models.User;
import tn.esprit.Services.PostService;
import tn.esprit.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import tn.esprit.Models.Community;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CategorieService;
import tn.esprit.Services.CommunityService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    @FXML
    private StackPane contentPane;
    private PostService postService;

    @FXML
    private TableView<CommunityWaitList> communityWaitListTable;

    @FXML
    private TableColumn<CommunityWaitList, String> userColumn;

    @FXML
    private TableColumn<CommunityWaitList, String> communityNameColumn;

    @FXML
    private TableColumn<CommunityWaitList, HBox> actionColumn;

    @FXML
    private HBox categorySlider;

    @FXML
    private GridPane communityGrid;

    @FXML
    private BorderPane FrontBorderpane;
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button viewButton;

    private User currentUser;
    private final CommunityService communityService = new CommunityService();
    private final CategorieService categorieService = new CategorieService();

    // Déclarez stage comme un champ de la classe
    private Stage stage;

    // Méthode pour initialiser le stage (par exemple, depuis votre classe principale ou un événement)
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    // Sample data model for community wait list
    public static class CommunityWaitList {
        private String username;
        private String communityName;
        private HBox actions;

        public CommunityWaitList(String username, String communityName) {
            this.username = username;
            this.communityName = communityName;

            // Create action buttons for each row
            Button approveBtn = new Button();
            approveBtn.getStyleClass().add("action-icon");
            approveBtn.setText("✓");

            Button viewBtn = new Button();
            viewBtn.getStyleClass().add("action-icon");
            viewBtn.setText("👁");

            Button rejectBtn = new Button();
            rejectBtn.getStyleClass().add("action-icon");
            rejectBtn.setText("✕");

            this.actions = new HBox(10, approveBtn, viewBtn, rejectBtn);
        }

        public String getUsername() {
            return username;
        }

        public String getCommunityName() {
            return communityName;
        }

        public HBox getActions() {
            return actions;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize community wait list table if it exists in the view
        if (communityWaitListTable != null) {
            userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            communityNameColumn.setCellValueFactory(new PropertyValueFactory<>("communityName"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));

            // Populate with sample data
            ObservableList<CommunityWaitList> waitList = FXCollections.observableArrayList(
                    new CommunityWaitList("Padel Connection", ""),
                    new CommunityWaitList("DJ's", ""),
                    new CommunityWaitList("theatre comedie", ""),
                    new CommunityWaitList("Ensemble tour du monde", ""),
                    new CommunityWaitList("FootBall", ""),
                    new CommunityWaitList("Street workout", ""),
                    new CommunityWaitList("Oriental", "")
            );

            communityWaitListTable.setItems(waitList);
        }
    }

    @FXML
    public void loadUserAbonnementsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserAbonnementsView.fxml"));
            AnchorPane userAbonnementsPane = loader.load();
            FrontBorderpane.setCenter(userAbonnementsPane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load user abonnements view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    @FXML
    private void navigateToFeed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fill.fxml"));
            Node communityView = loader.load();
            mainBorderPane.setCenter(communityView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadComunnity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AllCommunity.fxml"));
            Node communityView = loader.load();
            mainBorderPane.setCenter(communityView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadCategoriesView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/communityFront.fxml"));
            AnchorPane comunityfrontpane = loader.load();
            ComunityFrontController controller = loader.getController();
            controller.setFrontBorderpane(FrontBorderpane);
            FrontBorderpane.setCenter(comunityfrontpane);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load categories view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleApprove(String username, String communityName) {
        showAlert("Approve", "Approved community: " + username, Alert.AlertType.INFORMATION);
    }

    private void handleView(String username, String communityName) {
        showAlert("View", "Viewing community: " + username, Alert.AlertType.INFORMATION);
    }

    private void handleReject(String username, String communityName) {
        showAlert("Reject", "Rejected community: " + username, Alert.AlertType.INFORMATION);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Vérification du rôle
        if ("ROLE_USER".equals(user.getRole())) {
            showAlert("Accès refusé", "Vous n'avez pas les droits nécessaires", Alert.AlertType.ERROR);
            // Fermer l'application ou revenir à la page de login
            handleLogout();
        }
    }

    @FXML
    private void handleUsersMenuClick() {
        try {
            // Charger le ResourceBundle pour l'internationalisation
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages");

            // Créer un FXMLLoader et définir le ResourceBundle
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/list.fxml"));
            loader.setResources(bundle);

            // Charger la vue
            Parent usersView = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(usersView);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load users view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Charger le ResourceBundle pour la vue de connexion (si nécessaire)
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages");

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/login.fxml"));
            loader.setResources(bundle);
            Parent loginView = loader.load();

            // Définir la scène de connexion
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.setTitle("SyncYLinkY - Login");

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de se déconnecter: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardClick() {
        // Cette méthode est juste pour montrer une action du bouton, car le dashboard
        // est déjà inclus comme vue par défaut dans le FXML
        System.out.println("Dashboard clicked");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}