package tn.esprit.utils;

import tn.esprit.Models.Comment;
import tn.esprit.Models.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour valider les entrées utilisateur.
 */
public class Validator {
    private static final Logger logger = LoggerFactory.getLogger(Validator.class);
    private static final int POST_CONTENT_MIN_LENGTH = 5;
    private static final int POST_CONTENT_MAX_LENGTH = 255;
    private static final int POST_TITLE_MAX_LENGTH = 100;
    private static final int COMMENT_CONTENT_MIN_LENGTH = 3;
    private static final int COMMENT_CONTENT_MAX_LENGTH = 500;
    private static final Pattern INVALID_CHAR_PATTERN = Pattern.compile("[<>{}\\[\\];]");
    private static final String BANNED_WORDS_FILE = "banned-words.txt";
    private static final Set<String> BANNED_WORDS = loadBannedWords();

    /**
     * Charge la liste des mots interdits depuis le fichier banned-words.txt.
     * @return Un ensemble de mots interdits.
     */
    private static Set<String> loadBannedWords() {
        Set<String> bannedWords = new HashSet<>();
        try (InputStream inputStream = Validator.class.getClassLoader().getResourceAsStream(BANNED_WORDS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    bannedWords.add(trimmedLine.toLowerCase());
                }
            }
            logger.info("Chargé {} mots interdits depuis {}", bannedWords.size(), BANNED_WORDS_FILE);
        } catch (IOException | NullPointerException e) {
            logger.error("Erreur lors du chargement du fichier {}", BANNED_WORDS_FILE, e);
            throw new RuntimeException("Impossible de charger la liste des mots interdits", e);
        }
        return bannedWords;
    }

    /**
     * Valide une publication avant création ou modification.
     * @param post La publication à valider
     * @throws IllegalArgumentException Si la publication est invalide
     */
    public static void validatePost(Post post) {
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du post ne peut pas être vide.");
        }
        if (post.getContent().length() < POST_CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("Le contenu du post doit contenir au moins " + POST_CONTENT_MIN_LENGTH + " caractères.");
        }
        if (post.getContent().length() > POST_CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("Le contenu du post ne peut pas dépasser " + POST_CONTENT_MAX_LENGTH + " caractères.");
        }
        if (INVALID_CHAR_PATTERN.matcher(post.getContent()).find()) {
            throw new IllegalArgumentException("Le contenu du post contient des caractères non autorisés.");
        }
        if (post.getVisibility() == null || !post.getVisibility().matches("public|private")) {
            throw new IllegalArgumentException("La visibilité doit être 'public' ou 'private'.");
        }
        checkBannedWords(post.getContent(), "le contenu du post");
    }

    /**
     * Valide un commentaire avant création ou modification.
     * @param comment Le commentaire à valider
     * @throws IllegalArgumentException Si le commentaire est invalide
     */
    public static void validateComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide.");
        }
        if (comment.getContent().length() < COMMENT_CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("Le commentaire doit contenir au moins " + COMMENT_CONTENT_MIN_LENGTH + " caractères.");
        }
        if (comment.getContent().length() > COMMENT_CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("Le commentaire ne peut pas dépasser " + COMMENT_CONTENT_MAX_LENGTH + " caractères.");
        }
        if (INVALID_CHAR_PATTERN.matcher(comment.getContent()).find()) {
            throw new IllegalArgumentException("Le commentaire contient des caractères non autorisés.");
        }
        if (comment.getPostId() <= 0) {
            throw new IllegalArgumentException("L'ID du post doit être valide.");
        }
        checkBannedWords(comment.getContent(), "le commentaire");
    }

    /**
     * Vérifie si le contenu contient des mots interdits.
     * @param content Le contenu à vérifier
     * @param fieldName Le nom du champ pour le message d'erreur
     * @throws IllegalArgumentException Si un mot interdit est détecté
     */
    private static void checkBannedWords(String content, String fieldName) {
        if (content == null) {
            return;
        }
        String lowerContent = content.toLowerCase();
        for (String bannedWord : BANNED_WORDS) {
            if (lowerContent.contains(bannedWord)) {
                throw new IllegalArgumentException("Le contenu de " + fieldName + " contient un mot interdit : " + bannedWord);
            }
        }
    }
}