package tn.esprit.Services;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Models.Ville;
import tn.esprit.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavorisService {
    
    private final LieuCulturelService lieuCulturelService;

    public FavorisService() {
        this.lieuCulturelService = new LieuCulturelService();
    }

    public ObservableList<Ville> getFavoriteVilles() {
        ObservableList<Ville> favoriteVilles = FXCollections.observableArrayList();
        String query = "SELECT * FROM ville WHERE est_favori = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ville ville = new Ville(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("position")
                );
                favoriteVilles.add(ville);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des villes favorites", e);
        }
        return favoriteVilles;
    }
    
    public List<LieuCulturel> getFavoriteLieux() throws SQLException {
        List<LieuCulturel> favoriteLieux = new ArrayList<>();
        String query = "SELECT * FROM lieu_culturels WHERE est_favori = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                LieuCulturel lieu = new LieuCulturel(
                    rs.getLong("id"),
                    rs.getLong("ville_id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("link3_d"),
                    rs.getString("cover")
                );
                favoriteLieux.add(lieu);
            }
        }
        return favoriteLieux;
    }
    
    public boolean isVilleFavorite(Long villeId) {
        String query = "SELECT est_favori FROM ville WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, villeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("est_favori");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean isLieuFavorite(Long lieuId) {
        String query = "SELECT est_favori FROM lieu_culturels WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, lieuId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("est_favori");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void toggleVilleFavorite(Long villeId) {
        String query = "UPDATE ville SET est_favori = NOT est_favori WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, villeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la modification du favori", e);
        }
    }
    
    public void toggleLieuFavorite(Long lieuId) {
        String query = "UPDATE lieu_culturels SET est_favori = NOT est_favori WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, lieuId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la modification du favori", e);
        }
    }
} 