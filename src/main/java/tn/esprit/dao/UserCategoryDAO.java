package tn.esprit.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserCategoryDAO {

    /**
     * Enregistre une catégorie d'intérêt pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param categoryId ID de la catégorie
     * @return true si l'opération a réussi, false sinon
     */
    public boolean saveUserCategory(int userId, int categoryId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "INSERT INTO user_categories (user_id, category_id) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error saving user category: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Supprime toutes les catégories d'intérêt d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return true si l'opération a réussi, false sinon
     */
    public boolean deleteUserCategories(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "DELETE FROM user_categories WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting user categories: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifie si un utilisateur a sélectionné une catégorie spécifique
     * @param userId ID de l'utilisateur
     * @param categoryId ID de la catégorie
     * @return true si l'utilisateur a cette catégorie parmi ses intérêts, false sinon
     */
    public boolean hasUserCategory(int userId, int categoryId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "SELECT COUNT(*) FROM user_categories WHERE user_id = ? AND category_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error checking user category: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}