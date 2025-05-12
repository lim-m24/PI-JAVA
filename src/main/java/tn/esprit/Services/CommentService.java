package tn.esprit.Services;

import tn.esprit.Models.Comment;
import tn.esprit.repositories.CommentRepository;
import tn.esprit.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour gérer les opérations CRUD sur les commentaires.
 */
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;

    /**
     * Constructeur par défaut qui initialise le repository des commentaires.
     */
    public CommentService() {
        this.commentRepository = new CommentRepository();
    }

    /**
     * Crée un nouveau commentaire pour un post donné.
     *
     * @param postId  L'identifiant du post associé.
     * @param userId  L'identifiant de l'utilisateur (peut être null pour un utilisateur anonyme).
     * @param content Le contenu du commentaire.
     * @throws IllegalArgumentException Si la validation du commentaire échoue.
     * @throws SQLException            Si une erreur de base de données survient.
     */
    public void createComment(int postId, Integer userId, String content) throws SQLException {
        try {
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            Validator.validateComment(comment);
            commentRepository.create(comment);
            logger.info("Commentaire créé pour le post {} par l'utilisateur {}", postId, userId != null ? userId : "anonyme");
        } catch (IllegalArgumentException e) {
            logger.error("Échec de la validation lors de la création du commentaire : {}", e.getMessage());
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors de la création du commentaire pour le post {}", postId, e);
            throw e;
        }
    }

    /**
     * Récupère la liste des commentaires associés à un post.
     *
     * @param postId L'identifiant du post.
     * @return Une liste de commentaires.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public List<Comment> getCommentsForPost(int postId) throws SQLException {
        try {
            List<Comment> comments = commentRepository.findByPostId(postId);
            logger.debug("Récupération de {} commentaires pour le post {}", comments.size(), postId);
            return comments;
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors de la récupération des commentaires pour le post {}", postId, e);
            throw e;
        }
    }

    /**
     * Compte le nombre de commentaires associés à un post.
     *
     * @param postId L'identifiant du post.
     * @return Le nombre de commentaires.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public int getCommentCount(int postId) throws SQLException {
        try {
            int count = commentRepository.countByPostId(postId);
            logger.debug("Nombre de commentaires pour le post {} : {}", postId, count);
            return count;
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors du comptage des commentaires pour le post {}", postId, e);
            throw e;
        }
    }

    /**
     * Met à jour le contenu d'un commentaire existant.
     *
     * @param comment Le commentaire à mettre à jour.
     * @throws IllegalArgumentException Si la validation du commentaire échoue.
     * @throws SQLException            Si une erreur de base de données survient.
     */
    public void updateComment(Comment comment) throws SQLException {
        try {
            Validator.validateComment(comment);
            commentRepository.update(comment);
            logger.info("Commentaire {} mis à jour pour le post {}", comment.getId(), comment.getPostId());
        } catch (IllegalArgumentException e) {
            logger.error("Échec de la validation lors de la mise à jour du commentaire {} : {}", comment.getId(), e.getMessage());
            throw e;
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors de la mise à jour du commentaire {}", comment.getId(), e);
            throw e;
        }
    }

    /**
     * Supprime un commentaire donné par son identifiant et l'identifiant de l'utilisateur.
     *
     * @param commentId L'identifiant du commentaire.
     * @param userId    L'identifiant de l'utilisateur.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public void deleteComment(int commentId, Integer userId) throws SQLException {
        try {
            commentRepository.delete(commentId, userId);
            logger.info("Commentaire {} supprimé par l'utilisateur {}", commentId, userId != null ? userId : "anonyme");
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors de la suppression du commentaire {}", commentId, e);
            throw e;
        }
    }
}