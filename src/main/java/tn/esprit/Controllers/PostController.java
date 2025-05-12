package tn.esprit.Controllers;

import tn.esprit.Models.Comment;
import tn.esprit.Models.Post;
import tn.esprit.Services.CommentService;
import tn.esprit.Services.PostService;
import tn.esprit.Services.ReactionService;
import tn.esprit.Services.ShareService;
import tn.esprit.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @FXML private Label authorLabel;
    @FXML private Label timestampLabel;
    @FXML private Text contentLabel;
    @FXML private ImageView postImageView;
    @FXML private VBox commentsContainer;
    @FXML private ImageView reactionIcon;
    @FXML private Label likeCountLabel;
    @FXML private Label commentCountLabel;
    @FXML private Label shareCountLabel;
    @FXML private Button likeButton;
    @FXML private Button commentButton;
    @FXML private Button shareButton;
    @FXML private Button menuButton;

    private Post post;
    private PostService postService;
    private CommentService commentService;
    private ReactionService reactionService;
    private ShareService shareService;

    @FXML
    public void initializeData(Post post, PostService postService, CommentService commentService,
                               ReactionService reactionService, ShareService shareService) {
        this.post = post;
        this.postService = postService;
        this.commentService = commentService;
        this.reactionService = reactionService;
        this.shareService = shareService;
        updateUI();
        loadComments();
        loadReactions();
    }

    private void updateUI() {
        try {
            authorLabel.setText("Utilisateur #" + (post.getUserId() != null ? post.getUserId() : "Anonyme"));
            timestampLabel.setText(post.getCreatedAt() != null
                    ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Inconnu");
            String titre = post.getTitre() != null ? post.getTitre() : "";
            String content = post.getContent() != null ? post.getContent() : "";
            contentLabel.setText(titre + (titre.isEmpty() ? "" : "\n") + content);

            if (post.getFile() != null && !post.getFile().isEmpty()) {
                try {
                    postImageView.setImage(new Image("file:" + post.getFile()));
                    postImageView.setVisible(true);
                } catch (Exception e) {
                    logger.error("Erreur lors du chargement de l'image pour le post {}", post.getId(), e);
                    postImageView.setVisible(false);
                }
            } else {
                postImageView.setVisible(false);
            }

            shareCountLabel.setText("0 Partages");
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'UI pour le post {}", post.getId(), e);
            showAlert("Erreur", "Mise à jour de l'interface", "Impossible de mettre à jour l'affichage du post.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLike() {
        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    reactionService.toggleReaction(post.getId(), userId, "like");
                    return null;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(this::loadReactions));
            task.setOnFailed(event -> Platform.runLater(() -> {
                logger.error("Erreur lors de l'enregistrement de la réaction pour le post {}", post.getId(), task.getException());
                showAlert("Erreur", "Impossible d'enregistrer la réaction", task.getException().getMessage(), Alert.AlertType.ERROR);
            }));
            new Thread(task).start();
        } catch (IllegalStateException e) {
            showAlert("Erreur", "Connexion requise", "Vous devez être connecté pour aimer un post.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleShare() {
        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    shareService.sharePost(post.getId(), userId, null);
                    return null;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                showAlert("Succès", "Post partagé", "Le post a été partagé avec succès.", Alert.AlertType.INFORMATION);
                loadReactions();
            }));
            task.setOnFailed(event -> Platform.runLater(() -> {
                logger.error("Erreur lors du partage du post {}", post.getId(), task.getException());
                showAlert("Erreur", "Impossible de partager le post", task.getException().getMessage(), Alert.AlertType.ERROR);
            }));
            new Thread(task).start();
        } catch (IllegalStateException e) {
            showAlert("Erreur", "Connexion requise", "Vous devez être connecté pour partager un post.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void showCommentDialog() {
        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nouveau Commentaire");
            dialog.setHeaderText("Ajouter un commentaire");
            dialog.setContentText("Votre commentaire:");

            dialog.showAndWait().ifPresent(commentText -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws SQLException {
                        commentService.createComment(post.getId(), userId, commentText);
                        return null;
                    }
                };

                task.setOnSucceeded(event -> Platform.runLater(() -> {
                    showAlert("Succès", "Commentaire ajouté", "Votre commentaire a été publié.", Alert.AlertType.INFORMATION);
                    loadComments();
                }));
                task.setOnFailed(event -> Platform.runLater(() -> {
                    logger.error("Erreur lors de l'ajout du commentaire pour le post {}", post.getId(), task.getException());
                    String errorMessage = task.getException() instanceof IllegalArgumentException
                            ? task.getException().getMessage()
                            : "Une erreur est survenue lors de l'ajout du commentaire.";
                    showAlert("Erreur", "Impossible d'ajouter le commentaire", errorMessage, Alert.AlertType.ERROR);
                }));
                new Thread(task).start();
            });
        } catch (IllegalStateException e) {
            showAlert("Erreur", "Connexion requise", "Vous devez être connecté pour commenter un post.", Alert.AlertType.WARNING);
        }
    }

    private void loadComments() {
        Task<List<Comment>> task = new Task<>() {
            @Override
            protected List<Comment> call() throws SQLException {
                return commentService.getCommentsForPost(post.getId());
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            commentsContainer.getChildren().clear();
            List<Comment> comments = task.getValue();
            for (Comment comment : comments) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/syncylinky/views/components/comment-component.fxml"));
                    HBox commentNode = loader.load();
                    CommentController controller = loader.getController();
                    controller.initializeData(comment, commentService, this::loadComments);
                    commentsContainer.getChildren().add(commentNode);
                } catch (IOException e) {
                    logger.error("Erreur lors du chargement du commentaire {} pour le post {}", comment.getId(), post.getId(), e);
                    showAlert("Erreur", "Impossible de charger le commentaire", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            logger.error("Erreur lors du chargement des commentaires pour le post {}", post.getId(), task.getException());
            showAlert("Erreur", "Impossible de charger les commentaires", task.getException().getMessage(), Alert.AlertType.ERROR);
        }));

        new Thread(task).start();
    }

    private void loadReactions() {
        Task<ReactionData> task = new Task<>() {
            @Override
            protected ReactionData call() throws SQLException {
                int likeCount = reactionService.getReactionCount(post.getId(), "like");
                boolean userLiked = false;
                try {
                    int userId = SessionManager.getInstance().getCurrentUserId();
                    userLiked = reactionService.hasUserReacted(post.getId(), userId);
                } catch (IllegalStateException e) {
                    logger.debug("No user logged in, skipping user reaction check for post {}", post.getId());
                }
                int commentCount = commentService.getCommentCount(post.getId());
                int shareCount = shareService.getShareCount(post.getId());
                return new ReactionData(likeCount, userLiked, commentCount, shareCount);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            ReactionData data = task.getValue();
            likeCountLabel.setText(data.likeCount + " J'aime");
            commentCountLabel.setText(data.commentCount + " Commentaires");
            shareCountLabel.setText(data.shareCount + " Partages");
            likeButton.setStyle(data.userLiked ? "-fx-text-fill: #1877f2;" : "");
            updateReactionIcon(data.likeCount > 0);
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            logger.error("Erreur lors du chargement des réactions pour le post {}", post.getId(), task.getException());
            showAlert("Erreur", "Impossible de charger les réactions", task.getException().getMessage(), Alert.AlertType.ERROR);
        }));

        new Thread(task).start();
    }

    @FXML
    private void showPostOptionsMenu() {
        ContextMenu menu = new ContextMenu();
        try {
            int userId = SessionManager.getInstance().getCurrentUserId();
            if (post.getUserId() != null && post.getUserId().equals(userId)) {
                MenuItem editItem = new MenuItem("Modifier");
                editItem.setOnAction(e -> editPost());

                MenuItem deleteItem = new MenuItem("Supprimer");
                deleteItem.setOnAction(e -> deletePost());

                menu.getItems().addAll(editItem, deleteItem);
            }
        } catch (IllegalStateException e) {
            logger.debug("No user logged in, skipping edit/delete options for post {}", post.getId());
        }

        MenuItem viewItem = new MenuItem("Afficher");
        viewItem.setOnAction(e -> showAlert("Info", "Détails du post",
                "Titre: " + (post.getTitre() != null ? post.getTitre() : "") +
                        "\nContenu: " + (post.getContent() != null ? post.getContent() : ""), Alert.AlertType.INFORMATION));

        menu.getItems().add(viewItem);
        menu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void editPost() {
        TextInputDialog dialog = new TextInputDialog(post.getContent() != null ? post.getContent() : "");
        dialog.setTitle("Modifier le post");
        dialog.setHeaderText("Modifiez votre publication");

        dialog.showAndWait().ifPresent(newContent -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    post.setContent(newContent);
                    postService.updatePost(post);
                    return null;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                showAlert("Succès", "Post modifié", "Votre publication a été mise à jour.", Alert.AlertType.INFORMATION);
                updateUI();
            }));
            task.setOnFailed(event -> Platform.runLater(() -> {
                logger.error("Erreur lors de la modification du post {}", post.getId(), task.getException());
                String errorMessage = task.getException() instanceof IllegalArgumentException
                        ? task.getException().getMessage()
                        : "Une erreur est survenue lors de la modification du post.";
                showAlert("Erreur", "Impossible de modifier le post", errorMessage, Alert.AlertType.ERROR);
            }));
            new Thread(task).start();
        });
    }

    private void deletePost() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce post ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws SQLException {
                        int userId = SessionManager.getInstance().getCurrentUserId();
                        postService.deletePost(post.getId(), userId);
                        return null;
                    }
                };

                task.setOnSucceeded(event -> Platform.runLater(() -> {
                    showAlert("Succès", "Post supprimé", "La publication a été supprimée.", Alert.AlertType.INFORMATION);
                    VBox parent = (VBox) authorLabel.getParent().getParent();
                    parent.getChildren().remove(authorLabel.getParent());
                }));
                task.setOnFailed(event -> Platform.runLater(() -> {
                    logger.error("Erreur lors de la suppression du post {}", post.getId(), task.getException());
                    showAlert("Erreur", "Impossible de supprimer le post", task.getException().getMessage(), Alert.AlertType.ERROR);
                }));
                new Thread(task).start();
            }
        });
    }

    private void updateReactionIcon(boolean hasLikes) {
        if (reactionIcon != null) {
            String iconPath = hasLikes ? "/com/syncylinky/images/like-filled.png" : "/com/syncylinky/images/like-empty.png";
            try {
                Image image = new Image(getClass().getResourceAsStream(iconPath));
                reactionIcon.setImage(image);
            } catch (Exception e) {
                logger.error("Erreur lors du chargement de l'icône de réaction pour le post {}", post.getId(), e);
            }
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        URL cssUrl = getClass().getResource("/com/syncylinky/css/main.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        } else {
            logger.warn("Fichier main.css introuvable dans /com/syncylinky/css/");
        }
        alert.showAndWait();
    }

    private static class ReactionData {
        final int likeCount;
        final boolean userLiked;
        final int commentCount;
        final int shareCount;

        ReactionData(int likeCount, boolean userLiked, int commentCount, int shareCount) {
            this.likeCount = likeCount;
            this.userLiked = userLiked;
            this.commentCount = commentCount;
            this.shareCount = shareCount;
        }
    }
}