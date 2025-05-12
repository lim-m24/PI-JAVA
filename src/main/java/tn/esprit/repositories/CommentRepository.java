package tn.esprit.repositories;

import tn.esprit.config.DatabaseConfig;
import tn.esprit.Models.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour gérer les opérations sur la table des commentaires dans la base de données.
 */
public class CommentRepository {

    private static final Logger logger = LoggerFactory.getLogger(CommentRepository.class);

    /**
     * Crée un nouveau commentaire dans la base de données.
     *
     * @param comment Le commentaire à créer.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public void create(Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (post_id, user_id, content, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        logger.debug("Exécution de la requête SQL : {}", sql);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, comment.getPostId());
            stmt.setObject(2, comment.getUserId(), Types.INTEGER);
            stmt.setString(3, comment.getContent());
            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setTimestamp(5, Timestamp.valueOf(now)); // Définit updated_at égal à created_at
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du commentaire pour le post {}", comment.getPostId(), e);
            throw e;
        }
    }

    /**
     * Récupère tous les commentaires associés à un post donné.
     *
     * @param postId L'identifiant du post.
     * @return Une liste de commentaires.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public List<Comment> findByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setPostId(rs.getInt("post_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setContent(rs.getString("content"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des commentaires pour le post {}", postId, e);
            throw e;
        }
        return comments;
    }

    /**
     * Compte le nombre de commentaires associés à un post.
     *
     * @param postId L'identifiant du post.
     * @return Le nombre de commentaires.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public int countByPostId(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comment WHERE post_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des commentaires pour le post {}", postId, e);
            throw e;
        }
    }

    /**
     * Met à jour un commentaire existant dans la base de données.
     *
     * @param comment Le commentaire à mettre à jour.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public void update(Comment comment) throws SQLException {
        String sql = "UPDATE comment SET content = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getId());
            stmt.setObject(3, comment.getUserId(), Types.INTEGER);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Échec de la mise à jour : commentaire non trouvé ou non autorisé");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du commentaire {}", comment.getId(), e);
            throw e;
        }
    }

    /**
     * Supprime un commentaire de la base de données.
     *
     * @param commentId L'identifiant du commentaire.
     * @param userId    L'identifiant de l'utilisateur.
     * @throws SQLException Si une erreur de base de données survient.
     */
    public void delete(int commentId, Integer userId) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            stmt.setObject(2, userId, Types.INTEGER);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Échec de la suppression : commentaire non trouvé ou non autorisé");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du commentaire {}", commentId, e);
            throw e;
        }
    }
}