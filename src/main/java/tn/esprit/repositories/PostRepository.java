package tn.esprit.repositories;

import tn.esprit.config.DatabaseConfig;
import tn.esprit.Models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);

    public List<Post> findAllVisible(Integer currentUserId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT * FROM post 
            WHERE visibility = 'public' OR user_id = ?
            ORDER BY created_at DESC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, currentUserId, Types.INTEGER);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching visible posts for user {}", currentUserId, e);
            throw e;
        }
        return posts;
    }

    public void create(Post post) throws SQLException {
        String query = """
            INSERT INTO post (user_id, content, file, titre, visibility, created_at, update_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setObject(1, post.getUserId(), Types.INTEGER);
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getFile());
            pstmt.setString(4, post.getTitre());
            pstmt.setString(5, post.getVisibility());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        post.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating post", e);
            throw e;
        }
    }

    public Post findById(int postId) throws SQLException {
        String query = "SELECT * FROM post WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching post by ID: {}", postId, e);
            throw e;
        }
        return null;
    }

    public void update(Post post) throws SQLException {
        String query = """
            UPDATE post 
            SET content = ?, file = ?, titre = ?, visibility = ?, update_at = ? 
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, post.getContent());
            pstmt.setString(2, post.getFile());
            pstmt.setString(3, post.getTitre());
            pstmt.setString(4, post.getVisibility());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(6, post.getId());
            pstmt.setObject(7, post.getUserId(), Types.INTEGER);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating post: {}", post.getId(), e);
            throw e;
        }
    }

    public void delete(int postId, Integer userId) throws SQLException {
        String query = "DELETE FROM post WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.setObject(2, userId, Types.INTEGER);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting post: {}", postId, e);
            throw e;
        }
    }

    public List<Post> findByUserId(Integer userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, userId, Types.INTEGER);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching posts for user: {}", userId, e);
            throw e;
        }
        return posts;
    }

    public List<Post> findByKeyword(String keyword, Integer userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT * FROM post 
            WHERE (visibility = 'public' OR user_id = ?) 
            AND (titre LIKE ? OR content LIKE ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + keyword.trim() + "%";
            pstmt.setObject(1, userId, Types.INTEGER);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
            logger.debug("Récupéré {} posts pour la recherche '{}'", posts.size(), keyword);
            return posts;
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des posts avec le mot-clé '{}'", keyword, e);
            throw e;
        }
    }

    public List<Post> findRecommendedPosts(int userId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT p.*, 
                   COALESCE((SELECT COUNT(*) FROM reaction r WHERE r.post_id = p.id AND r.reaction_type = 'LIKE'), 0) as like_count,
                   COALESCE((SELECT COUNT(*) FROM comment c WHERE c.post_id = p.id), 0) as comment_count
            FROM post p
            LEFT JOIN categories pc ON p.id = pc.post_id
            WHERE (p.visibility = 'public' OR p.user_id = ? OR p.user_id IS NULL)
            AND pc.category_id IN (SELECT category_id FROM categories WHERE user_id = ?)
            ORDER BY (like_count + comment_count) DESC, p.created_at DESC
            LIMIT 10
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
            logger.debug("Récupéré {} posts recommandés pour l'utilisateur {}", posts.size(), userId);
            return posts;
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des posts recommandés pour l'utilisateur {}", userId, e);
            throw new SQLException("Impossible de récupérer les posts recommandés : " + e.getMessage(), e);
        }
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setUserId(rs.getObject("user_id", Integer.class));
        post.setContent(rs.getString("content"));
        post.setFile(rs.getString("file"));
        post.setTitre(rs.getString("titre"));
        post.setVisibility(rs.getString("visibility"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setUpdateAt(rs.getTimestamp("update_at").toLocalDateTime());
        return post;
    }
}