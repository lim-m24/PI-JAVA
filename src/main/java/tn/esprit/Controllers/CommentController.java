package tn.esprit.Controllers;

import tn.esprit.Models.Comment;
import tn.esprit.Services.CommentService;
import tn.esprit.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour le composant de commentaire.
 */
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @FXML private Label usernameLabel;
    @FXML private Label timestampLabel;
    @FXML private Label commentLabel;
    @FXML private Button menuButton;

    private Comment comment;
    private CommentService commentService;
    private Runnable onUpdate;

    /**
     * Initialise les données du commentaire et configure l'interface utilisateur.
     *
     * @param comment        Le commentaire à afficher.
     * @param commentService Le service pour gérer les opérations sur les commentaires.
     * @param onUpdate       Callback exécuté après une mise à jour ou suppression.
     */
    public void initializeData(Comment comment, CommentService commentService, Runnable onUpdate) {
        this.comment = comment;
        this.commentService = commentService;
        this.onUpdate = onUpdate;
        updateUI();
        logger.debug("Commentaire {} initialisé pour le post {}", comment.getId(), comment.getPostId());
    }

    private void updateUI() {
        usernameLabel.setText("Utilisateur #" + (comment.getUserId() != null ? comment.getUserId() : "Anonyme"));
        timestampLabel.setText(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        commentLabel.setText(comment.getContent());
        // Afficher le bouton de menu uniquement pour l'auteur du commentaire
        boolean isAuthor = comment.getUserId() != null && comment.getUserId().equals(SessionManager.getCurrentUserId());
        menuButton.setVisible(isAuthor);
        menuButton.setManaged(isAuthor);
        logger.debug("Bouton de menu {} pour le commentaire {} (utilisateur courant : {})",
                isAuthor ? "visible" : "caché", comment.getId(), SessionManager.getCurrentUserId());
    }

    @FXML
    private void showCommentOptionsMenu() {
        logger.debug("Affichage du menu contextuel pour le commentaire {}", comment.getId());
        ContextMenu menu = new ContextMenu();

        if (comment.getUserId() != null && comment.getUserId().equals(SessionManager.getCurrentUserId())) {
            MenuItem editItem = new MenuItem("Modifier");
            editItem.setOnAction(e -> {
                logger.debug("Option 'Modifier' sélectionnée pour le commentaire {}", comment.getId());
                handleEdit();
            });

            MenuItem deleteItem = new MenuItem("Supprimer");
            deleteItem.setOnAction(e -> {
                logger.debug("Option 'Supprimer' sélectionnée pour le commentaire {}", comment.getId());
                handleDelete();
            });

            menu.getItems().addAll(editItem, deleteItem);
        }

        MenuItem viewItem = new MenuItem("Afficher");
        viewItem.setOnAction(e -> {
            logger.debug("Option 'Afficher' sélectionnée pour le commentaire {}", comment.getId());
            showAlert("Info", "Détails du commentaire", "Contenu: " + comment.getContent(), Alert.AlertType.INFORMATION);
        });

        menu.getItems().add(viewItem);
        menu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
    private void handleEdit() {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifiez votre commentaire");

        dialog.showAndWait().ifPresent(newContent -> {
            logger.info("Tentative de modification du commentaire {} avec le nouveau contenu", comment.getId());
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws SQLException {
                    comment.setContent(newContent);
                    commentService.updateComment(comment);
                    return null;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                commentLabel.setText(newContent);
                showAlert("Succès", "Commentaire modifié", "Votre commentaire a été mis à jour.", Alert.AlertType.INFORMATION);
                if (onUpdate != null) {
                    onUpdate.run();
                }
                logger.info("Commentaire {} modifié avec succès", comment.getId());
            }));
            task.setOnFailed(event -> Platform.runLater(() -> {
                logger.error("Erreur lors de la modification du commentaire {}", comment.getId(), task.getException());
                showAlert("Erreur", "Impossible de modifier le commentaire", task.getException().getMessage(), Alert.AlertType.ERROR);
            }));
            new Thread(task).start();
        });
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce commentaire ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.info("Tentative de suppression du commentaire {}", comment.getId());
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws SQLException {
                        commentService.deleteComment(comment.getId(), SessionManager.getCurrentUserId());
                        return null;
                    }
                };

                task.setOnSucceeded(event -> Platform.runLater(() -> {
                    showAlert("Succès", "Commentaire supprimé", "Le commentaire a été supprimé.", Alert.AlertType.INFORMATION);
                    if (onUpdate != null) {
                        onUpdate.run();
                    }
                    logger.info("Commentaire {} supprimé avec succès", comment.getId());
                }));
                task.setOnFailed(event -> Platform.runLater(() -> {
                    logger.error("Erreur lors de la suppression du commentaire {}", comment.getId(), task.getException());
                    showAlert("Erreur", "Impossible de supprimer le commentaire", task.getException().getMessage(), Alert.AlertType.ERROR);
                }));
                new Thread(task).start();
            } else {
                logger.debug("Suppression du commentaire {} annulée", comment.getId());
            }
        });
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
            logger.warn("Fichier main.css introuvable dans /com/syncylinky/views/");
        }
        alert.showAndWait();
    }
}