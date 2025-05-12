package tn.esprit.Services;

import tn.esprit.Models.Ville;
import tn.esprit.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VilleService {
    
    public ObservableList<Ville> getAllVilles() {
        ObservableList<Ville> villes = FXCollections.observableArrayList();
        String query = "SELECT * FROM ville";
        
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ville ville = new Ville(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("position")
                );
                villes.add(ville);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des villes", e);
        }
        
        return villes;
    }
    
    public void saveVille(Ville ville) {
        String query = "INSERT INTO ville (nom, description, position) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ville.getNom());
            pstmt.setString(2, ville.getDescription());
            pstmt.setString(3, ville.getPosition());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ville.setId(generatedKeys.getLong(1));
                }
            }
            DataUpdateService.getInstance().notifyUpdate("VILLE_CREATE");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout de la ville", e);
        }
    }
    
    public void updateVille(Ville ville) {
        String query = "UPDATE ville SET nom = ?, description = ?, position = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(query)) {
            pstmt.setString(1, ville.getNom());
            pstmt.setString(2, ville.getDescription());
            pstmt.setString(3, ville.getPosition());
            pstmt.setLong(4, ville.getId());
            
            pstmt.executeUpdate();
            DataUpdateService.getInstance().notifyUpdate("VILLE_UPDATE");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de la ville", e);
        }
    }
    
    public void deleteVille(Long id) {
        // D'abord, supprimer tous les lieux culturels associés
        String deleteLieuxQuery = "DELETE FROM lieu_culturels WHERE ville_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(deleteLieuxQuery)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression des lieux culturels associés", e);
        }

        // Ensuite, supprimer la ville
        String deleteVilleQuery = "DELETE FROM ville WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(deleteVilleQuery)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            DataUpdateService.getInstance().notifyUpdate("VILLE_DELETE");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de la ville", e);
        }
    }

    public List<Ville> getFavoriteVilles() throws SQLException {
        List<Ville> favoriteVilles = new ArrayList<>();
        String query = "SELECT * FROM ville WHERE est_favori = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Ville ville = new Ville(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("position")
                );
                favoriteVilles.add(ville);
            }
        }
        return favoriteVilles;
    }
} 