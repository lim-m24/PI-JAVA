package tn.esprit.Controllers;

import tn.esprit.Models.Post;
import tn.esprit.Models.User;
import tn.esprit.Services.*;
import tn.esprit.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @FXML private VBox feedContainer;
    @FXML private VBox recommendedContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button recommendedButton;
    @FXML private Button backButton;
    @FXML private Button notificationButton;
    @FXML private Label notificationCountLabel;
    @FXML private VBox userProfileBox;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;

    private final PostService postService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final ShareService shareService;

    // Historique des états du feed
    private final List<FeedState> feedHistory = new ArrayList<>();
    private boolean isNavigatingBack = false;

    // Classe interne pour représenter un état du feed
    private static class FeedState {
        enum Type { MAIN_FEED, SEARCH, USER_POSTS }
        final Type type;
        final String keyword; // null pour MAIN_FEED et USER_POSTS

        FeedState(Type type, String keyword) {
            this.type = type;
            this.keyword = keyword;
        }
    }

    public FeedController() {
        this.postService = new PostService();
        this.commentService = new CommentService();
        this.reactionService = new ReactionService();
        this.shareService = new ShareService();
    }

    @FXML
    public void initialize() {
        try {
            logger.info("Initialisation de FeedController");
            // Désactiver le bouton Retour au démarrage
            backButton.setDisable(true);
            // Charger les informations de l'utilisateur
            updateUserProfile(null);
            // Charger le feed et les publications recommandées
            loadFeed();
            loadRecommendedPosts();
            logger.debug("FeedController initialisé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de FeedController", e);
            showAlert("Erreur", "Initialisation échouée", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void updateUserProfile(User updatedUser) {
        try {
            Integer userId = SessionManager.getCurrentUserId();
            if (userId == null) {
                logger.warn("Aucun utilisateur connecté pour mettre à jour le profil");
                return;
            }
            User user = updatedUser;
            if (user == null) {
                // Récupérer l'utilisateur depuis UserService si aucun utilisateur n'est fourni
                UserService userService = new UserService();
                user = userService.getUserById(userId);
            }
            if (user != null) {
                usernameLabel.setText(user.getUsername() != null ? user.getUsername() : "Utilisateur");
                emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
                roleLabel.setText(user.getRole() != null ? user.getRole() : "");
                logger.debug("Profil utilisateur mis à jour pour userId={}: username={}, email={}, role={}",
                        userId, user.getUsername(), user.getEmail(), user.getRole());
            } else {
                logger.warn("Utilisateur introuvable pour userId={}", userId);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du profil utilisateur", e);
            showAlert("Erreur", "Échec de la mise à jour du profil", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        logger.debug("Recherche avec la requête : {}", keyword);
        if (!isNavigatingBack) {
            feedHistory.add(new FeedState(FeedState.Type.SEARCH, keyword));
            backButton.setDisable(feedHistory.size() <= 1);
        }
        loadFeed(keyword);
    }

    @FXML
    private void handleShowRecommended() {
        logger.debug("Chargement des publications recommandées");
        loadRecommendedPosts();
    }

    @FXML
    private void handleViewUserPosts() {
        logger.debug("Affichage des publications de l'utilisateur");
        if (!isNavigatingBack) {
            feedHistory.add(new FeedState(FeedState.Type.USER_POSTS, null));
            backButton.setDisable(feedHistory.size() <= 1);
        }
        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() throws SQLException {
                return postService.getUserPosts(SessionManager.getCurrentUserId());
            }
        };

        loadingIndicator.setVisible(true);
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                feedContainer.getChildren().clear();
                List<Post> posts = task.getValue();
                if (posts.isEmpty()) {
                    Label emptyLabel = new Label("Aucune publication à afficher");
                    emptyLabel.getStyleClass().add("empty-label");
                    feedContainer.getChildren().add(emptyLabel);
                } else {
                    for (Post post : posts) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/post-component.fxml"));
                            VBox postNode = loader.load();
                            PostController controller = loader.getController();
                            controller.initializeData(post, postService, commentService, reactionService, shareService);
                            feedContainer.getChildren().add(postNode);
                        } catch (IOException e) {
                            logger.error("Erreur lors du chargement du post {}", post.getId(), e);
                            showAlert("Erreur", "Impossible de charger le post", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            logger.error("Erreur lors du chargement des publications", task.getException());
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showAlert("Erreur", "Impossible de charger les publications", task.getException().getMessage(), Alert.AlertType.ERROR);
            });
        });

        new Thread(task).start();
    }

    @FXML
    private void handleBack() {
        if (feedHistory.size() <= 1) {
            logger.debug("Aucun état précédent pour revenir en arrière");
            return;
        }
        // Supprimer l'état actuel
        feedHistory.remove(feedHistory.size() - 1);
        // Récupérer l'état précédent
        FeedState previousState = feedHistory.get(feedHistory.size() - 1);
        isNavigatingBack = true;
        try {
            logger.debug("Retour à l'état précédent : type={}", previousState.type);
            switch (previousState.type) {
                case MAIN_FEED:
                    loadFeed();
                    break;
                case SEARCH:
                    searchField.setText(previousState.keyword);
                    loadFeed(previousState.keyword);
                    break;
                case USER_POSTS:
                    handleViewUserPosts();
                    break;
            }
        } finally {
            isNavigatingBack = false;
        }
        backButton.setDisable(feedHistory.size() <= 1);
    }

    @FXML
    private void handleNewPost() {
        logger.debug("Ouverture du dialogue pour un nouveau post");
        Dialog<Post> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Post");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");
        TextArea contentField = new TextArea();
        contentField.setPromptText("Quoi de neuf ?");
        ChoiceBox<String> visibilityChoice = new ChoiceBox<>();
        visibilityChoice.getItems().addAll("public", "private");
        visibilityChoice.setValue("public");
        Button fileButton = new Button("Choisir une image");
        Label fileLabel = new Label("Aucun fichier sélectionné");
        File[] selectedFile = new File[1];

        fileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
            }
        });

        GridPane grid = new GridPane();
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Contenu:"), 0, 1);
        grid.add(contentField, 1, 1);
        grid.add(new Label("Visibilité:"), 0, 2);
        grid.add(visibilityChoice, 1, 2);
        grid.add(new Label("Image:"), 0, 3);
        grid.add(fileButton, 1, 3);
        grid.add(fileLabel, 1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String content = contentField.getText().trim();
                if (content.isEmpty()) {
                    showAlert("Erreur", "Contenu vide", "Le contenu du post ne peut pas être vide.", Alert.AlertType.WARNING);
                    return null;
                }
                Post post = new Post();
                post.setUserId(SessionManager.getCurrentUserId());
                post.setTitre(titleField.getText());
                post.setContent(content);
                post.setVisibility(visibilityChoice.getValue());
                post.setCreatedAt(java.time.LocalDateTime.now());
                post.setUpdateAt(java.time.LocalDateTime.now());
                if (selectedFile[0] != null) {
                    try {
                        Path uploadDir = Path.of("uploads");
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectory(uploadDir);
                        }
                        Path targetPath = uploadDir.resolve(selectedFile[0].getName());
                        Files.copy(selectedFile[0].toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        post.setFile(targetPath.toString());
                    } catch (IOException e) {
                        logger.error("Erreur lors de la copie du fichier", e);
                        showAlert("Erreur", "Impossible de charger l'image", e.getMessage(), Alert.AlertType.ERROR);
                        return null;
                    }
                }
                return post;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(post -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    postService.createPost(post);
                    return null;
                }
            };

            loadingIndicator.setVisible(true);
            task.setOnSucceeded(event -> Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showAlert("Succès", "Post créé", "Votre publication a été ajoutée.", Alert.AlertType.INFORMATION);
                loadFeed();
                loadRecommendedPosts();
            }));
            task.setOnFailed(event -> Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                logger.error("Erreur lors de la création du post", task.getException());
                String errorMessage = task.getException() instanceof IllegalArgumentException
                        ? task.getException().getMessage()
                        : "Échec de la création : " + task.getException().getMessage();
                showAlert("Erreur", "Échec de la création", errorMessage, Alert.AlertType.ERROR);
            }));
            new Thread(task).start();
        });
    }

    @FXML
    private void navigateToFeed() {
        logger.debug("Navigation vers le Feed");
        loadFeed();
    }

    @FXML
    private void navigateToMessages() {
        logger.debug("Navigation vers Messages (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Messages n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToCommunity() {
        logger.debug("Navigation vers Community (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Community n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToVibe() {
        logger.debug("Navigation vers Vibe (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Vibe n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToMyCommunity() {
        logger.debug("Navigation vers My Community (non implémenté)");
        showAlert("Information", "Non implémenté", "La section My Community n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToPages() {
        logger.debug("Navigation vers Pages (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Pages n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void viewUserInfo() {
        logger.debug("Affichage des informations de l'utilisateur");
        showAlert("Information", "Profil utilisateur", "Affichage du profil utilisateur (à implémenter).", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToUpgrade() {
        logger.debug("Navigation vers Upgrade (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Upgrade n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void navigateToFeedback() {
        logger.debug("Navigation vers Feedback (non implémenté)");
        showAlert("Information", "Non implémenté", "La section Feedback n'est pas encore disponible.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void toggleUserMenu(MouseEvent event) {
        logger.debug("Clic sur le menu utilisateur");
        showAlert("Information", "Menu utilisateur", "Menu utilisateur cliqué (à implémenter).", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showNotifications() {
        logger.debug("Affichage des notifications");
        showAlert("Information", "Notifications", "Aucune notification pour le moment (à implémenter).", Alert.AlertType.INFORMATION);
    }

    private void loadFeed() {
        loadFeed(null);
    }

    private void loadFeed(String keyword) {
        if (!isNavigatingBack && keyword == null) {
            feedHistory.add(new FeedState(FeedState.Type.MAIN_FEED, null));
            backButton.setDisable(feedHistory.size() <= 1);
        }
        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() throws SQLException {
                int userId = SessionManager.getCurrentUserId();
                return keyword == null || keyword.isEmpty() ? postService.getVisiblePosts(userId) : postService.searchPosts(keyword, userId);
            }
        };

        loadingIndicator.setVisible(true);
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                feedContainer.getChildren().clear();
                List<Post> posts = task.getValue();
                if (posts.isEmpty()) {
                    Label emptyLabel = new Label(keyword == null ? "Aucune publication à afficher" : "Aucun post trouvé pour '" + keyword + "'");
                    emptyLabel.getStyleClass().add("empty-label");
                    feedContainer.getChildren().add(emptyLabel);
                } else {
                    for (Post post : posts) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/post-component.fxml"));
                            VBox postNode = loader.load();
                            PostController controller = loader.getController();
                            controller.initializeData(post, postService, commentService, reactionService, shareService);
                            feedContainer.getChildren().add(postNode);
                        } catch (IOException e) {
                            logger.error("Erreur lors du chargement du post {}", post.getId(), e);
                            showAlert("Erreur", "Impossible de charger le post", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            logger.error("Erreur lors du chargement du feed", task.getException());
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showAlert("Erreur", "Impossible de charger le feed", task.getException().getMessage(), Alert.AlertType.ERROR);
            });
        });

        new Thread(task).start();
    }

    private void loadRecommendedPosts() {
        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() throws SQLException {
                return postService.getRecommendedPosts(SessionManager.getCurrentUserId());
            }
        };

        loadingIndicator.setVisible(true);
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                recommendedContainer.getChildren().clear();
                List<Post> posts = task.getValue();
                if (posts.isEmpty()) {
                    Label emptyLabel = new Label("Aucune publication recommandée");
                    emptyLabel.getStyleClass().add("empty-label");
                    recommendedContainer.getChildren().add(emptyLabel);
                } else {
                    Label headerLabel = new Label("Publications recommandées");
                    headerLabel.getStyleClass().add("recommendation-header");
                    recommendedContainer.getChildren().add(headerLabel);
                    for (Post post : posts) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/post-component.fxml"));
                            VBox postNode = loader.load();
                            PostController controller = loader.getController();
                            controller.initializeData(post, postService, commentService, reactionService, shareService);
                            recommendedContainer.getChildren().add(postNode);
                        } catch (IOException e) {
                            logger.error("Erreur lors du chargement du post recommandé {}", post.getId(), e);
                            showAlert("Erreur", "Impossible de charger le post recommandé", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            logger.error("Erreur lors du chargement des posts recommandés", task.getException());
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                showAlert("Erreur", "Impossible de charger les posts recommandés", task.getException().getMessage(), Alert.AlertType.ERROR);
            });
        });

        new Thread(task).start();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/views/main.css").toExternalForm());
        } catch (Exception e) {
            logger.warn("Impossible de charger main.css pour l'alerte", e);
        }
        alert.showAndWait();
    }
}