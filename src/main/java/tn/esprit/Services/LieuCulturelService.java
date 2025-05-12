package tn.esprit.Services;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class LieuCulturelService {
    
    public ObservableList<LieuCulturel> getLieuxCulturelsByVille(Long villeId) {
        ObservableList<LieuCulturel> lieux = FXCollections.observableArrayList();
        String query = "SELECT id, ville_id, nom, description, link3_d, cover FROM lieu_culturels WHERE ville_id = ?";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(query)) {
            pstmt.setLong(1, villeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LieuCulturel lieu = new LieuCulturel(
                        rs.getLong("id") != 0 ? rs.getLong("id") : null,
                        rs.getLong("ville_id") != 0 ? rs.getLong("ville_id") : null,
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("link3_d"),
                        rs.getString("cover")
                    );
                    lieux.add(lieu);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des lieux culturels", e);
        }
        
        return lieux;
    }
    
    public void saveLieuCulturel(LieuCulturel lieu) throws SQLException {
        String query = "INSERT INTO lieu_culturels (ville_id, nom, description, link3_d, cover) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, lieu.getVilleId());
            pstmt.setString(2, lieu.getNom());
            pstmt.setString(3, lieu.getDescription());
            pstmt.setString(4, lieu.getLink3d());
            pstmt.setString(5, lieu.getCover());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    lieu.setId(generatedKeys.getLong(1));
                }
            }
            DataUpdateService.getInstance().notifyUpdate("LIEU_CREATE");
        }
    }
    
    public void updateLieuCulturel(LieuCulturel lieu) {
        System.out.println("Début de la mise à jour du lieu culturel : " + lieu.getId());
        
        if (lieu == null) {
            throw new RuntimeException("Le lieu culturel ne peut pas être null");
        }

        if (lieu.getId() == null || lieu.getId() == 0) {
            throw new RuntimeException("L'ID du lieu culturel est invalide");
        }

        // Vérifier que le lieu existe dans la base de données
        LieuCulturel existingLieu = getLieuById(lieu.getId());
        if (existingLieu == null) {
            throw new RuntimeException("Le lieu culturel n'existe pas dans la base de données");
        }

        System.out.println("Anciennes valeurs :");
        System.out.println("Nom : " + existingLieu.getNom());
        System.out.println("Description : " + existingLieu.getDescription());
        System.out.println("Link3D : " + existingLieu.getLink3d());
        System.out.println("Cover : " + existingLieu.getCover());

        String query = "UPDATE lieu_culturels SET nom = ?, description = ?, link3_d = ?, cover = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Définir les paramètres avec des valeurs par défaut si null
            String nom = lieu.getNom() != null ? lieu.getNom().trim() : "";
            String description = lieu.getDescription() != null ? lieu.getDescription().trim() : "";
            String link3d = lieu.getLink3d() != null ? lieu.getLink3d().trim() : "";
            String cover = lieu.getCover() != null ? lieu.getCover().trim() : "";
            
            pstmt.setString(1, nom);
            pstmt.setString(2, description);
            pstmt.setString(3, link3d);
            pstmt.setString(4, cover);
            pstmt.setLong(5, lieu.getId());
            
            System.out.println("Nouvelles valeurs à sauvegarder :");
            System.out.println("Nom : " + nom);
            System.out.println("Description : " + description);
            System.out.println("Link3D : " + link3d);
            System.out.println("Cover : " + cover);
            
            // Exécuter la mise à jour
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Nombre de lignes affectées : " + rowsAffected);
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Aucun lieu culturel n'a été mis à jour");
            }
            
            // Vérifier que la mise à jour a bien été effectuée
            LieuCulturel updatedLieu = getLieuById(lieu.getId());
            if (updatedLieu == null) {
                throw new RuntimeException("Impossible de récupérer le lieu mis à jour");
            }
            
            System.out.println("Valeurs après mise à jour :");
            System.out.println("Nom : " + updatedLieu.getNom());
            System.out.println("Description : " + updatedLieu.getDescription());
            System.out.println("Link3D : " + updatedLieu.getLink3d());
            System.out.println("Cover : " + updatedLieu.getCover());
            
            // Vérifier que les valeurs ont été correctement mises à jour
            if (!updatedLieu.getNom().equals(nom) ||
                !updatedLieu.getDescription().equals(description) ||
                !updatedLieu.getLink3d().equals(link3d) ||
                !updatedLieu.getCover().equals(cover)) {
                throw new RuntimeException("La mise à jour n'a pas été correctement effectuée");
            }
            
            // Notifier les observateurs du changement
            DataUpdateService.getInstance().notifyUpdate("LIEU_UPDATE");
            System.out.println("Mise à jour terminée avec succès");
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du lieu culturel : " + e.getMessage(), e);
        }
    }
    
    public void deleteLieuCulturel(Long id) {
        // D'abord, supprimer tous les médias associés
        String deleteMediaQuery = "DELETE FROM media WHERE lieux_id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(deleteMediaQuery)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression des médias associés", e);
        }

        // Ensuite, supprimer le lieu culturel
        String deleteLieuQuery = "DELETE FROM lieu_culturels WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(deleteLieuQuery)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            DataUpdateService.getInstance().notifyUpdate("LIEU_DELETE");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du lieu culturel", e);
        }
    }

    public LieuCulturel getLieuById(Long id) {
        String query = "SELECT * FROM lieu_culturels WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new LieuCulturel(
                        rs.getLong("id"),
                        rs.getLong("ville_id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("link3_d"),
                        rs.getString("cover")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération du lieu culturel", e);
        }
        return null;
    }

    public ObservableList<LieuCulturel> getFavoriteLieux() {
        ObservableList<LieuCulturel> lieux = FXCollections.observableArrayList();
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
                lieux.add(lieu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des lieux favoris", e);
        }
        return lieux;
    }

    public boolean isFavori(Long lieuId) {
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
            throw new RuntimeException("Erreur lors de la vérification du favori", e);
        }
        return false;
    }

    public void toggleFavori(Long lieuId) {
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