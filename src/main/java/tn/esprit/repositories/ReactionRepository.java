package tn.esprit.repositories;

import tn.esprit.config.DatabaseConfig;
import tn.esprit.Models.Reaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class ReactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReactionRepository.class);

    public void toggleReaction(Reaction reaction) throws SQLException {
        Optional<Reaction> existing = findExistingReaction(reaction);
        if (existing.isPresent()) {
            delete(existing.get().getId());
        } else {
            add(reaction);
        }
    }

    private void add(Reaction reaction) throws SQLException {
        String query = """
            INSERT INTO reaction (post_id, comment_id, user_id, type) 
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, reaction.getPostId(), Types.INTEGER);
            pstmt.setObject(2, reaction.getCommentId(), Types.INTEGER);
            pstmt.setObject(3, reaction.getUserId(), Types.INTEGER);
            pstmt.setString(4, reaction.getType());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding reaction", e);
            throw e;
        }
    }

    public void delete(int reactionId) throws SQLException {
        String query = "DELETE FROM reaction WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting reaction: {}", reactionId, e);
            throw e;
        }
    }

    public int countByPostAndType(int postId, String type) throws SQLException {
        String query = "SELECT COUNT(*) FROM reaction WHERE post_id = ? AND type = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting reactions for post: {}, type: {}", postId, type, e);
            throw e;
        }
        return 0;
    }

    public boolean existsByUserAndPost(int userId, int postId) throws SQLException {
        String query = "SELECT 1 FROM reaction WHERE user_id = ? AND post_id = ? LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking reaction existence for user: {}, post: {}", userId, postId, e);
            throw e;
        }
    }

    private Optional<Reaction> findExistingReaction(Reaction reaction) throws SQLException {
        String query = reaction.getPostId() != null
                ? "SELECT * FROM reaction WHERE post_id = ? AND user_id = ?"
                : "SELECT * FROM reaction WHERE comment_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reaction.getPostId() != null ? reaction.getPostId() : reaction.getCommentId());
            pstmt.setObject(2, reaction.getUserId(), Types.INTEGER);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReaction(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding existing reaction", e);
            throw e;
        }
        return Optional.empty();
    }

    private Reaction mapResultSetToReaction(ResultSet rs) throws SQLException {
        Reaction reaction = new Reaction();
        reaction.setId(rs.getInt("id"));
        reaction.setPostId(rs.getObject("post_id", Integer.class));
        reaction.setCommentId(rs.getObject("comment_id", Integer.class));
        reaction.setUserId(rs.getObject("user_id", Integer.class));
        reaction.setType(rs.getString("type"));
        return reaction;
    }
}