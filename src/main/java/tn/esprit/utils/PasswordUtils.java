package tn.esprit.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Facteur de coût par défaut (2^13 itérations)
    private static final int COST_FACTOR = 13;

    /**
     * Hache un mot de passe en utilisant BCrypt.
     * @param password Le mot de passe à hacher
     * @return Le hachage BCrypt avec sel intégré (format $2y$...)
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être null");
        }

        // BCrypt génère automatiquement un sel et l'intègre dans le hachage final
        return BCrypt.hashpw(password, BCrypt.gensalt(COST_FACTOR));
    }

    /**
     * Vérifie si un mot de passe en clair correspond à un hachage BCrypt.
     * @param inputPassword Le mot de passe en clair à vérifier
     * @param storedHash Le hachage BCrypt stocké
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        if (inputPassword == null || storedHash == null) {
            return false;
        }

        try {
            // BCrypt.checkpw extrait automatiquement le sel du hachage stocké
            return BCrypt.checkpw(inputPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // En cas de format de hachage invalide
            return false;
        }
    }
}