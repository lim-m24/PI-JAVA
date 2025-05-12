package tn.esprit.Services;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Models.Media;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExportService {
    
    private final MediaService mediaService;
    private final LieuCulturelService lieuCulturelService;
    private final Path uploadPath;
    
    public PDFExportService() {
        this.mediaService = new MediaService();
        this.lieuCulturelService = new LieuCulturelService();
        this.uploadPath = Paths.get("uploads").toAbsolutePath();
    }
    
    public void exportLieuCulturelToPDF(Long lieuId, String outputPath) throws IOException {
        LieuCulturel lieu = lieuCulturelService.getLieuById(lieuId);
        List<Media> medias = mediaService.getMediaByLieuId(lieuId);
        
        System.out.println("Génération du PDF pour le lieu : " + lieu.getNom());
        System.out.println("Nombre de médias trouvés : " + medias.size());
        
        try (PDDocument document = new PDDocument()) {
            // Page de titre
            PDPage titlePage = new PDPage(PDRectangle.A4);
            document.addPage(titlePage);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, titlePage)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(lieu.getNom());
                contentStream.endText();
                
                // Description
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                String[] descriptionLines = lieu.getDescription().split("(?<=\\G.{80})");
                float leading = 15;
                for (String line : descriptionLines) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -leading);
                }
                contentStream.endText();
                
                // Lien 3D
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("Lien 3D: " + lieu.getLink3d());
                contentStream.endText();
                
                // Date de génération
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Généré le " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                contentStream.endText();
            }
            
            // Pages des médias
            if (!medias.isEmpty()) {
                PDPage mediaPage = new PDPage(PDRectangle.A4);
                document.addPage(mediaPage);
                PDPageContentStream mediaStream = new PDPageContentStream(document, mediaPage);
                
                float yPosition = 750;
                float pageWidth = mediaPage.getMediaBox().getWidth();
                float margin = 50;
                float contentWidth = pageWidth - 2 * margin;
                
                // Titre de la section médias
                mediaStream.beginText();
                mediaStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                mediaStream.newLineAtOffset(margin, yPosition);
                mediaStream.showText("Médias associés");
                mediaStream.endText();
                yPosition -= 30;
                
                for (Media media : medias) {
                    // Obtenir le nom du fichier à partir du chemin stocké
                    String fileName = Paths.get(media.getLink()).getFileName().toString();
                    // Construire le chemin absolu du fichier
                    Path mediaPath = uploadPath.resolve(fileName);
                    File mediaFile = mediaPath.toFile();
                    
                    if (!mediaFile.exists()) {
                        System.err.println("Fichier média non trouvé: " + mediaFile.getAbsolutePath());
                        continue;
                    }
                    
                    String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                    boolean isImage = extension.matches("\\.(jpg|jpeg|png|gif)$");
                    
                    if (isImage) {
                        // Vérifier s'il faut créer une nouvelle page
                        if (yPosition < 250) {
                            mediaStream.close();
                            mediaPage = new PDPage(PDRectangle.A4);
                            document.addPage(mediaPage);
                            mediaStream = new PDPageContentStream(document, mediaPage);
                            yPosition = 750;
                        }
                        
                        try {
                            PDImageXObject image = PDImageXObject.createFromFile(mediaFile.getAbsolutePath(), document);
                            
                            // Calculer les dimensions de l'image
                            float maxWidth = contentWidth;
                            float maxHeight = 300;
                            float scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
                            float width = image.getWidth() * scale;
                            float height = image.getHeight() * scale;
                            
                            // Centrer l'image horizontalement
                            float xPosition = margin + (contentWidth - width) / 2;
                            
                            // Dessiner l'image
                            mediaStream.drawImage(image, xPosition, yPosition - height, width, height);
                            
                            // Ajouter une légende
                            mediaStream.beginText();
                            mediaStream.setFont(PDType1Font.HELVETICA, 10);
                            mediaStream.newLineAtOffset(margin, yPosition - height - 15);
                            mediaStream.showText(fileName);
                            mediaStream.endText();
                            
                            yPosition -= (height + 40); // Espace pour la légende et la marge
                            
                        } catch (IOException e) {
                            System.err.println("Erreur lors du chargement de l'image: " + mediaFile.getAbsolutePath());
                            e.printStackTrace();
                        }
                    } else {
                        // Pour les vidéos, afficher un message avec un lien
                        mediaStream.beginText();
                        mediaStream.setFont(PDType1Font.HELVETICA, 12);
                        mediaStream.newLineAtOffset(margin, yPosition);
                        mediaStream.showText("🎥 Vidéo : " + fileName);
                        mediaStream.endText();
                        yPosition -= 20;
                    }
                }
                mediaStream.close();
            }
            
            // Sauvegarde du document
            System.out.println("Sauvegarde du PDF à : " + outputPath);
            document.save(outputPath);
            System.out.println("PDF généré avec succès !");
        }
    }
} 