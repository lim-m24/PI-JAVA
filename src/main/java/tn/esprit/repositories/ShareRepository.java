package tn.esprit.repositories;

import tn.esprit.config.DatabaseConfig;
import tn.esprit.Models.Share;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShareRepository {
    private static final Logger logger = LoggerFactory.getLogger(ShareRepository.class);

    public void sharePost(Share share) throws SQLException {
        String checkQuery = "SELECT 1 FROM share WHERE post_id = ? AND user_id = ? LIMIT 1";
        String insertQuery = """
            INSERT INTO share (post_id, user_id, shared_from_id, create_at) 
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, share.getPostId());
            checkStmt.setObject(2, share.getUserId(), Types.INTEGER);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    logger.warn("Partage déjà existant pour le post {} par l'utilisateur {}", share.getPostId(), share.getUserId());
                    throw new SQLException("Vous avez déjà partagé ce post.");
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, share.getPostId());
                insertStmt.setObject(2, share.getUserId(), Types.INTEGER);
                insertStmt.setObject(3, share.getSharedFromId(), Types.INTEGER);
                insertStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                insertStmt.executeUpdate();

                try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        share.setId(rs.getInt(1));
                    }
                }
                logger.info("Post {} partagé par l'utilisateur {}", share.getPostId(), share.getUserId());
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du partage du post {}", share.getPostId(), e);
            throw e;
        }
    }

    public List<Share> findByPostId(int postId) throws SQLException {
        List<Share> shares = new ArrayList<>();
        String query = "SELECT * FROM share WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(mapResultSetToShare(rs));
                }
            }
            logger.debug("Récupéré {} partages pour le post {}", shares.size(), postId);
            return shares;
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des partages pour le post {}", postId, e);
            throw e;
        }
    }

    public int countByPostId(int postId) throws SQLException {
        String query = "SELECT COUNT(*) FROM share WHERE post_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des partages pour le post {}", postId, e);
            throw e;
        }
    }

    public void unshare(int shareId, Integer userId) throws SQLException {
        String query = "DELETE FROM share WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, shareId);
            pstmt.setObject(2, userId, Types.INTEGER);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Partage {} supprimé pour l'utilisateur {}", shareId, userId);
            } else {
                logger.warn("Aucun partage trouvé pour l'ID {} et l'utilisateur {}", shareId, userId);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du partage {}", shareId, e);
            throw e;
        }
    }

    private Share mapResultSetToShare(ResultSet rs) throws SQLException {
        Share share = new Share();
        share.setId(rs.getInt("id"));
        share.setPostId(rs.getInt("post_id"));
        share.setUserId(rs.getObject("user_id", Integer.class));
        share.setSharedFromId(rs.getObject("shared_from_id", Integer.class));
        share.setCreateAt(rs.getTimestamp("create_at").toLocalDateTime());
        return share;
    }
}