package tn.esprit.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.Models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.repositories.PostRepository;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.Validator;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;

    public PostService() {
        this.postRepository = new PostRepository();
    }

    public void createPost(Post post) throws SQLException {
        try {
            Validator.validatePost(post);
            postRepository.create(post);
            logger.info("Post {} created by user {}", post.getId(), post.getUserId());
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for post creation: {}", e.getMessage());
            throw e;
        } catch (SQLException e) {
            logger.error("Database error during post creation", e);
            throw e;
        }
    }

    public List<Post> getVisiblePosts(Integer currentUserId) throws SQLException {
        try {
            return postRepository.findAllVisible(currentUserId);
        } catch (SQLException e) {
            logger.error("Database error during fetching visible posts", e);
            throw e;
        }
    }

    public Post getPostById(int postId) throws SQLException {
        try {
            return postRepository.findById(postId);
        } catch (SQLException e) {
            logger.error("Database error during fetching post by ID: {}", postId, e);
            throw e;
        }
    }

    public void updatePost(Post post) throws SQLException {
        try {
            Validator.validatePost(post);
            postRepository.update(post);
            logger.info("Post {} updated by user {}", post.getId(), post.getUserId());
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for post update: {}", e.getMessage());
            throw e;
        } catch (SQLException e) {
            logger.error("Database error during post update", e);
            throw e;
        }
    }

    public void deletePost(int postId, Integer userId) throws SQLException {
        try {
            postRepository.delete(postId, userId);
            logger.info("Post {} deleted by user {}", postId, userId);
        } catch (SQLException e) {
            logger.error("Database error during post deletion: {}", postId, e);
            throw e;
        }
    }

    public List<Post> getUserPosts(Integer userId) throws SQLException {
        try {
            return postRepository.findByUserId(userId);
        } catch (SQLException e) {
            logger.error("Database error during fetching user posts: {}", userId, e);
            throw e;
        }
    }

    public List<Post> searchPosts(String keyword, int userId) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            logger.debug("Mot-clé vide, récupération des posts visibles pour l'utilisateur {}", userId);
            return getVisiblePosts(userId);
        }
        try {
            List<Post> posts = postRepository.findByKeyword(keyword, userId);
            logger.info("Recherche effectuée avec le mot-clé '{}' pour l'utilisateur {}, {} résultats trouvés", keyword, userId, posts.size());
            return posts;
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des posts avec le mot-clé '{}' pour l'utilisateur {}", keyword, userId, e);
            throw new SQLException("Impossible de rechercher les posts : " + e.getMessage(), e);
        }
    }

    public List<Post> getRecommendedPosts(int userId) throws SQLException {
        try {
            List<Post> posts = postRepository.findRecommendedPosts(userId);
            logger.debug("Récupéré {} posts recommandés pour l'utilisateur {}", posts.size(), userId);
            return posts;
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des posts recommandés pour l'utilisateur {}", userId, e);
            throw new SQLException("Impossible de récupérer les posts recommandés : " + e.getMessage(), e);
        }
    }
}