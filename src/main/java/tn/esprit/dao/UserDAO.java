package tn.esprit.dao;

import tn.esprit.Models.Categories;
import tn.esprit.Models.User;
import tn.esprit.utils.PasswordUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getString("username"),
                        rs.getDate("date_ob").toLocalDate(),
                        rs.getString("gender"),
                        rs.getBoolean("banned"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationToken(rs.getString("verification_token"));
                loadUserInterests(conn, user);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private void loadUserInterests(Connection conn, User user) throws SQLException {
        String sql = "SELECT c.id, c.nom, c.description, c.cover, c.date_creation " +
                "FROM user_categories uc " +
                "JOIN categories c ON uc.categories_id = c.id " +
                "WHERE uc.user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("date_creation");
                LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;
                Categories category = new Categories(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("cover"),
                        dateCreation
                );
                user.addInterest(category);
            }
        }
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        User user = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getString("username"),
                        rs.getDate("date_ob").toLocalDate(),
                        rs.getString("gender"),
                        rs.getBoolean("banned"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationToken(rs.getString("verification_token"));
                loadUserInterests(conn, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public boolean addUser(User user) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO user (email, role, password, name, firstname, username, date_ob, gender, banned, is_verified, is_google_authenticator_enabled, is_active, verification_token) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, user.getRole());
                pstmt.setString(3, PasswordUtils.hashPassword(user.getPassword()));
                pstmt.setString(4, user.getName());
                pstmt.setString(5, user.getFirstname());
                pstmt.setString(6, user.getUsername());
                pstmt.setDate(7, Date.valueOf(user.getDateOB()));
                pstmt.setString(8, user.getGender());
                pstmt.setBoolean(9, user.isBanned());
                pstmt.setBoolean(10, user.isVerified());
                pstmt.setBoolean(11, user.isGoogleAuthenticatorEnabled());
                pstmt.setBoolean(12, true);
                pstmt.setString(13, user.getVerificationToken());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            if (user.getInterests() != null && !user.getInterests().isEmpty()) {
                for (Categories category : user.getInterests()) {
                    if (!categoryExists(conn, category.getId())) {
                        conn.rollback();
                        return false;
                    }
                }
                saveUserInterests(conn, user);
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean categoryExists(Connection conn, int categoryId) throws SQLException {
        String sql = "SELECT id FROM categories WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // In UserDAO.java
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET email = ?, role = ?, password = ?, name = ?, firstname = ?, " +
                "username = ?, date_ob = ?, gender = ?, banned = ?, is_verified = ?, " +
                "is_google_authenticator_enabled = ?, is_active = ?, verification_token = ?, reset_token = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getEmail());
                pstmt.setString(2, user.getRole());
                pstmt.setString(3, user.getPassword());
                pstmt.setString(4, user.getName());
                pstmt.setString(5, user.getFirstname());
                pstmt.setString(6, user.getUsername());
                pstmt.setDate(7, Date.valueOf(user.getDateOB()));
                pstmt.setString(8, user.getGender());
                pstmt.setBoolean(9, user.isBanned());
                pstmt.setBoolean(10, user.isVerified());
                pstmt.setBoolean(11, user.isGoogleAuthenticatorEnabled());
                pstmt.setBoolean(12, user.isActive());
                pstmt.setString(13, user.getVerificationToken());
                pstmt.setString(14, user.getResetToken()); // Ensure this is set
                pstmt.setInt(15, user.getId());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    deleteUserInterests(conn, user.getId());
                    saveUserInterests(conn, user);
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int id) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            deleteUserInterests(conn, id);

            String deleteDependenciesSQL = "DELETE FROM membre_comunity WHERE id_user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteDependenciesSQL)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            String deleteUserSQL = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSQL)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUserInterests(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO user_categories (user_id, categories_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Categories category : user.getInterests()) {
                pstmt.setInt(1, user.getId());
                pstmt.setInt(2, category.getId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deleteUserInterests(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM user_categories WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        User user = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getString("username"),
                        rs.getDate("date_ob").toLocalDate(),
                        rs.getString("gender"),
                        rs.getBoolean("banned"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationToken(rs.getString("verification_token"));
                loadUserInterests(conn, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public User getUserByResetToken(String resetToken) {
        String sql = "SELECT * FROM user WHERE reset_token = ?";
        User user = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resetToken);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getString("username"),
                        rs.getDate("date_ob").toLocalDate(),
                        rs.getString("gender"),
                        rs.getBoolean("banned"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationToken(rs.getString("verification_token"));
                loadUserInterests(conn, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public User getUserByVerificationToken(String token) {
        String sql = "SELECT * FROM user WHERE verification_token = ?";
        User user = null;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("firstname"),
                        rs.getString("username"),
                        rs.getDate("date_ob").toLocalDate(),
                        rs.getString("gender"),
                        rs.getBoolean("banned"),
                        rs.getBoolean("is_verified")
                );
                user.setVerificationToken(rs.getString("verification_token"));
                loadUserInterests(conn, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM user";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveUserCount() {
        String sql = "SELECT COUNT(*) FROM user WHERE is_active = true";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getBannedUserCount() {
        String sql = "SELECT COUNT(*) FROM user WHERE banned = true";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCountByRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getNewUsersCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM user WHERE DATE(created_at) = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getMonthlyRegistrations(int months) {
        Map<String, Integer> registrations = new HashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
                "FROM user " +
                "WHERE created_at >= DATE_SUB(CURRENT_DATE(), INTERVAL ? MONTH) " +
                "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                "ORDER BY month ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, months);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                registrations.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return registrations;
    }

    public Map<LocalDate, Map<String, Integer>> getDailyStats(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Map<String, Integer>> stats = new HashMap<>();

        String sql = "SELECT DATE(created_at) as date, " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active, " +
                "SUM(CASE WHEN banned = true THEN 1 ELSE 0 END) as banned " +
                "FROM user " +
                "WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Integer> dailyStats = new HashMap<>();
                LocalDate date = rs.getDate("date").toLocalDate();
                dailyStats.put("total", rs.getInt("total"));
                dailyStats.put("active", rs.getInt("active"));
                dailyStats.put("banned", rs.getInt("banned"));

                stats.put(date, dailyStats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public double getAverageLoginFrequency() {
        String sql = "SELECT AVG(login_count) FROM user_stats";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getAverageSessionDuration() {
        String sql = "SELECT AVG(session_duration) FROM user_sessions";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getRetentionRate() {
        String sql = "SELECT (COUNT(CASE WHEN last_login_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) THEN 1 END) * 100.0 / COUNT(*)) " +
                "FROM user WHERE created_at <= DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY)";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Map<LocalDate, Integer> getDailyRegistrations(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Integer> dailyRegistrations = new HashMap<>();
        String sql = "SELECT DATE(created_at) AS reg_date, COUNT(*) AS count " +
                "FROM user " +
                "WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY reg_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("reg_date").toLocalDate();
                int count = rs.getInt("count");
                dailyRegistrations.put(date, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dailyRegistrations;
    }

    public String getUserPassword(int userId) {
        String sql = "SELECT password FROM user WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Categories> getAllCategories() {
        List<Categories> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("date_creation");
                LocalDateTime dateCreation = timestamp != null ? timestamp.toLocalDateTime() : null;
                Categories category = new Categories(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("cover"),
                        dateCreation
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }
}