package tn.esprit.Services;

import tn.esprit.Models.Media;
import tn.esprit.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.UUID;

public class MediaService {
    private static final String UPLOAD_DIR = "uploads";
    private final Path uploadPath;

    public MediaService() {
        // Obtenir le chemin absolu du dossier uploads
        this.uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        
        // Créer le dossier uploads s'il n'existe pas
        try {
            Files.createDirectories(uploadPath);
            System.out.println("Dossier uploads créé/vérifié à : " + uploadPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la création du dossier uploads");
        }
    }

    public ObservableList<Media> getMediaByLieuId(Long lieuId) {
        ObservableList<Media> mediaList = FXCollections.observableArrayList();
        String query = "SELECT * FROM media WHERE lieux_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, lieuId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String storedLink = rs.getString("link");
                    // Nettoyer le chemin stocké
                    String cleanLink = storedLink.replace("uploads/uploads/", "uploads/");
                    
                    Media media = new Media(
                        rs.getLong("id"),
                        rs.getLong("lieux_id"),
                        cleanLink,
                        rs.getString("type")
                    );
                    mediaList.add(media);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des médias", e);
        }
        return mediaList;
    }

    public Media saveMedia(Long lieuId, File file) throws IOException {
        // Vérifier l'extension du fichier
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String type = (extension.equals(".mp4") || extension.equals(".avi") || extension.equals(".mov")) ? "video" : "image";
        
        // Générer un nouveau nom de fichier unique
        String newFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
        
        // Construire le chemin du fichier cible
        Path targetPath = uploadPath.resolve(newFileName);
        
        // Copier le fichier
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Fichier copié vers : " + targetPath);
        
        // Construire le chemin relatif pour la base de données
        String dbPath = UPLOAD_DIR + "/" + newFileName;
        
        // Sauvegarder dans la base de données
        String query = "INSERT INTO media (lieux_id, link, type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, lieuId);
            pstmt.setString(2, dbPath);
            pstmt.setString(3, type);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Media(rs.getLong(1), lieuId, dbPath, type);
                }
            }
        } catch (SQLException e) {
            // En cas d'erreur, supprimer le fichier copié
            Files.deleteIfExists(targetPath);
            throw new IOException("Erreur lors de la sauvegarde du média dans la base de données", e);
        }
        return null;
    }

    public boolean deleteMedia(Media media) {
        // Supprimer le fichier physique
        try {
            // Construire le chemin absolu du fichier à supprimer
            Path mediaPath = uploadPath.resolve(Paths.get(media.getLink()).getFileName());
            Files.deleteIfExists(mediaPath);
            System.out.println("Fichier supprimé : " + mediaPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Supprimer l'entrée de la base de données
        String query = "DELETE FROM media WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, media.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 