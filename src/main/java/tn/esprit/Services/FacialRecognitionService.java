package tn.esprit.Services;

import tn.esprit.Models.User;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_core.addWeighted;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class FacialRecognitionService {
    // Utiliser un répertoire utilisateur pour les données persistantes
    private final String FACE_DATA_DIR = System.getProperty("user.home") + File.separator +
            ".syncylinky" + File.separator + "face_data" + File.separator;
    private CascadeClassifier faceDetector;
    private FaceRecognizer faceRecognizer;
    private UserService userService;

    // Constantes pour améliorer la lisibilité et la maintenance
    private static final int FACE_IMAGE_SIZE = 200;
    private static final int REGISTER_FACE_COUNT = 8;    // Réduit pour accélérer la démo
    private static final int RECOGNITION_ATTEMPTS = 60;  // Augmenté pour donner plus de temps
    private static final double RECOGNITION_THRESHOLD = 70.0; // Valeur plus permissive pour caméras de faible qualité
    private static final double VERIFICATION_THRESHOLD = 80.0; // Valeur plus permissive

    // Paramètres pour les caméras de basse qualité
    private static final double SCALE_FACTOR = 1.05;     // Plus petit pour une détection plus précise
    private static final int MIN_NEIGHBORS = 2;         // Réduit pour caméras de basse qualité

    // Valeurs pour l'amélioration des images
    private static final double BRIGHTNESS_ALPHA = 1.2;  // Facteur d'amélioration de la luminosité
    private static final double BRIGHTNESS_BETA = 15;    // Valeur d'ajout pour la luminosité

    // Délais pour rendre les étapes plus visibles
    private static final int DETECTION_DELAY = 200;      // ms entre chaque frame
    private static final int CONFIRMATION_DELAY = 2000;  // ms pour afficher les confirmations

    // Taille minimale du visage requise
    private static final int MIN_FACE_SIZE = 80;         // Plus petit pour les caméras basse qualité

    public FacialRecognitionService() {
        userService = new UserService();
        initFaceDetector();
        initFaceRecognizer();
    }

    private void initFaceDetector() {
        // Initialiser le détecteur de visage Haar Cascade
        faceDetector = new CascadeClassifier();

        try {
            // Chemin dans les ressources
            String cascadeResourcePath = "/haarcascade_frontalface_default.xml";

            // Extraire le fichier depuis les ressources vers un fichier temporaire
            File tempFile = File.createTempFile("cascade", ".xml");
            tempFile.deleteOnExit();

            try (InputStream is = getClass().getResourceAsStream(cascadeResourcePath)) {
                if (is == null) {
                    System.err.println("Le fichier cascade n'a pas été trouvé dans les ressources: " + cascadeResourcePath);
                    return;
                }

                Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Charger depuis le fichier temporaire
            if (!faceDetector.load(tempFile.getAbsolutePath())) {
                System.err.println("Erreur lors du chargement du fichier cascade");
            } else {
                System.out.println("Fichier cascade chargé avec succès.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'extraction du fichier cascade: " + e.getMessage());
            e.printStackTrace();
        }

        // Créer le répertoire pour les visages s'il n'existe pas
        new File(FACE_DATA_DIR).mkdirs();
    }

    private void initFaceRecognizer() {
        // Initialiser le reconnaisseur de visage avec des paramètres optimisés pour caméras basse qualité
        faceRecognizer = LBPHFaceRecognizer.create(
                1,      // radius - rayon pour le voisinage local
                8,      // neighbors - nombre de points d'échantillonnage
                8,      // grid_x - nombre de cellules en x
                8,      // grid_y - nombre de cellules en y
                120.0   // threshold - valeur par défaut de seuil, augmentée pour caméras basse qualité
        );

        try {
            File modelFile = new File(FACE_DATA_DIR + "face_model.yml");
            if (modelFile.exists()) {
                faceRecognizer.read(modelFile.getAbsolutePath());
                System.out.println("Modèle de reconnaissance faciale chargé depuis: " + modelFile.getAbsolutePath());
            } else {
                // Entraîner avec les visages disponibles si le modèle n'existe pas
                trainRecognizer();
                System.out.println("Aucun modèle existant trouvé, création d'un nouveau modèle de reconnaissance");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du modèle de reconnaissance: " + e.getMessage());
        }
    }

    /**
     * Améliore la qualité d'une image pour compenser les caméras de faible qualité
     * @param image L'image à améliorer
     * @return L'image améliorée
     */
    private Mat enhanceImage(Mat image) {
        Mat enhanced = new Mat();

        // Améliorer la luminosité et le contraste
        image.convertTo(enhanced, -1, BRIGHTNESS_ALPHA, BRIGHTNESS_BETA);

        // Réduire le bruit
        GaussianBlur(enhanced, enhanced, new Size(3, 3), 0);

        return enhanced;
    }

    private void trainRecognizer() {
        List<Mat> faces = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        // Parcourir le répertoire des visages stockés pour l'entraînement
        File faceDir = new File(FACE_DATA_DIR);
        if (faceDir.exists() && faceDir.isDirectory()) {
            File[] userDirs = faceDir.listFiles();
            if (userDirs != null) {
                for (File userDir : userDirs) {
                    if (userDir.isDirectory()) {
                        try {
                            int userId = Integer.parseInt(userDir.getName());
                            File[] faceFiles = userDir.listFiles();
                            if (faceFiles != null) {
                                for (File faceFile : faceFiles) {
                                    if (faceFile.isFile() && faceFile.getName().endsWith(".jpg")) {
                                        Mat face = imread(faceFile.getAbsolutePath(), IMREAD_GRAYSCALE);
                                        if (!face.empty()) {
                                            // Vérifier que l'image est de la bonne taille
                                            if (face.rows() != FACE_IMAGE_SIZE || face.cols() != FACE_IMAGE_SIZE) {
                                                resize(face, face, new Size(FACE_IMAGE_SIZE, FACE_IMAGE_SIZE));
                                            }
                                            faces.add(face);
                                            labels.add(userId);
                                            System.out.println("Ajout du visage pour l'utilisateur " + userId + ": " + faceFile.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Ignorer les répertoires qui ne sont pas des nombres
                            System.out.println("Ignoré répertoire non numérique: " + userDir.getName());
                        }
                    }
                }
            }
        }

        if (!faces.isEmpty()) {
            System.out.println("Entraînement du modèle avec " + faces.size() + " visages");
            MatVector facesMat = new MatVector(faces.size());
            Mat labelsMat = new Mat(labels.size(), 1, CV_32SC1);
            IntPointer labelsPtr = new IntPointer(labelsMat.ptr());

            for (int i = 0; i < faces.size(); i++) {
                facesMat.put(i, faces.get(i));
                labelsPtr.put(i, labels.get(i));
            }

            faceRecognizer.train(facesMat, labelsMat);
            faceRecognizer.save(FACE_DATA_DIR + "face_model.yml");
            System.out.println("Modèle entraîné et sauvegardé avec succès");
        } else {
            System.out.println("Aucun visage trouvé pour l'entraînement");
        }
    }

    /**
     * Dessine une interface utilisateur améliorée sur l'image
     * @param image L'image sur laquelle dessiner l'interface
     * @param message Message principal à afficher
     * @param progress Message de progression
     * @param isSuccess Indique si une opération a réussi (pour changer la couleur)
     */
    private void drawEnhancedUI(Mat image, String message, String progress, boolean isSuccess) {
        // Dessiner un panneau semi-transparent en haut
        Mat overlay = image.clone();
        rectangle(overlay,
                new Rect(0, 0, image.cols(), 60),
                new Scalar(40, 40, 40, 0.8),
                -1, LINE_8, 0);

        // Fusionner l'overlay avec l'image originale
        addWeighted(overlay, 0.7, image, 0.3, 0, image);

        // Dessiner le titre principal
        putText(image, message,
                new Point(10, 30),
                FONT_HERSHEY_SIMPLEX, 0.9,
                isSuccess ? new Scalar(0, 255, 0, 1) : new Scalar(255, 255, 255, 1),
                2, LINE_AA, false);

        // Dessiner la barre de progression en bas
        rectangle(overlay,
                new Rect(0, image.rows() - 40, image.cols(), 40),
                new Scalar(40, 40, 40, 0.8),
                -1, LINE_8, 0);

        addWeighted(overlay, 0.7, image, 0.3, 0, image);

        putText(image, progress,
                new Point(10, image.rows() - 15),
                FONT_HERSHEY_SIMPLEX, 0.7,
                new Scalar(255, 255, 255, 1),
                1, LINE_AA, false);
    }

    /**
     * Dessine une bordure animée autour d'un rectangle
     * @param image L'image sur laquelle dessiner
     * @param rect Le rectangle à encadrer
     * @param frame Le numéro de frame pour l'animation
     */
    private void drawAnimatedBorder(Mat image, Rect rect, int frame) {
        // Couleurs qui changent en fonction du frame
        double hue = (frame % 100) * 3.6; // 0-360 degrés
        Scalar color = new Scalar(0, 0, 0, 1);

        // Animation simple des couleurs: rouge -> jaune -> vert -> cyan -> bleu -> magenta -> rouge
        if (hue < 60) {
            color = new Scalar(0, 255, 255, 1); // cyan
        } else if (hue < 120) {
            color = new Scalar(0, 255, 0, 1); // vert
        } else if (hue < 180) {
            color = new Scalar(255, 255, 0, 1); // jaune
        } else if (hue < 240) {
            color = new Scalar(255, 0, 0, 1); // bleu
        } else if (hue < 300) {
            color = new Scalar(255, 0, 255, 1); // magenta
        } else {
            color = new Scalar(0, 0, 255, 1); // rouge
        }

        // Dessiner le rectangle avec la couleur animée
        rectangle(image, rect, color, 3, LINE_8, 0);

        // Dessiner des guides d'alignement aux coins
        int guideLength = 15;

        // Coin supérieur gauche
        line(image, new Point(rect.x(), rect.y()),
                new Point(rect.x() + guideLength, rect.y()), color, 3, LINE_8, 0);
        line(image, new Point(rect.x(), rect.y()),
                new Point(rect.x(), rect.y() + guideLength), color, 3, LINE_8, 0);

        // Coin supérieur droit
        line(image, new Point(rect.x() + rect.width(), rect.y()),
                new Point(rect.x() + rect.width() - guideLength, rect.y()), color, 3, LINE_8, 0);
        line(image, new Point(rect.x() + rect.width(), rect.y()),
                new Point(rect.x() + rect.width(), rect.y() + guideLength), color, 3, LINE_8, 0);

        // Coin inférieur gauche
        line(image, new Point(rect.x(), rect.y() + rect.height()),
                new Point(rect.x() + guideLength, rect.y() + rect.height()), color, 3, LINE_8, 0);
        line(image, new Point(rect.x(), rect.y() + rect.height()),
                new Point(rect.x(), rect.y() + rect.height() - guideLength), color, 3, LINE_8, 0);

        // Coin inférieur droit
        line(image, new Point(rect.x() + rect.width(), rect.y() + rect.height()),
                new Point(rect.x() + rect.width() - guideLength, rect.y() + rect.height()), color, 3, LINE_8, 0);
        line(image, new Point(rect.x() + rect.width(), rect.y() + rect.height()),
                new Point(rect.x() + rect.width(), rect.y() + rect.height() - guideLength), color, 3, LINE_8, 0);
    }

    /**
     * Enregistre le visage d'un utilisateur avec une interface améliorée
     * @param user L'utilisateur dont on enregistre le visage
     * @return true si l'enregistrement a réussi, false sinon
     */
    public boolean registerFace(User user) {
        try {
            // Créer le répertoire pour l'utilisateur
            String userDir = FACE_DATA_DIR + user.getId() + "/";
            File userDirFile = new File(userDir);
            userDirFile.mkdirs();

            // Supprimer les anciennes images si l'utilisateur met à jour son visage
            if (userDirFile.exists() && userDirFile.isDirectory()) {
                File[] oldFiles = userDirFile.listFiles();
                if (oldFiles != null) {
                    for (File file : oldFiles) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                    System.out.println("Images précédentes supprimées pour l'utilisateur " + user.getId());
                }
            }

            // Configurer le grabber de caméra
            FrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.start();

            // Configurer la fenêtre d'affichage avec une taille plus grande
            int frameWidth = 800;
            int frameHeight = 600;

            grabber.setImageWidth(frameWidth);
            grabber.setImageHeight(frameHeight);

            CanvasFrame frame = new CanvasFrame("Enregistrement du visage - " + user.getName(),
                    CanvasFrame.getDefaultGamma() / grabber.getGamma());
            frame.setCanvasSize(frameWidth, frameHeight);
            frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

            int capturedFaces = 0;
            int frameCount = 0;
            final long startTime = System.currentTimeMillis();
            final long timeout = 60000; // 60 secondes maximum pour l'enregistrement

            // Afficher des instructions initiales
            System.out.println("Début de l'enregistrement du visage pour " + user.getName());
            System.out.println("PRÉSENTATION: Phase d'enregistrement facial");

            // Capturer plusieurs images du visage pour l'entraînement
            while (frame.isVisible() && capturedFaces < REGISTER_FACE_COUNT &&
                    (System.currentTimeMillis() - startTime) < timeout) {

                Frame capturedFrame = grabber.grab();
                Mat colorImage = converter.convert(capturedFrame);

                // Améliorer la qualité de l'image
                Mat enhancedImage = enhanceImage(colorImage);

                Mat grayImage = new Mat();
                cvtColor(enhancedImage, grayImage, COLOR_BGR2GRAY);
                equalizeHist(grayImage, grayImage); // Améliorer le contraste

                // Détecter les visages avec des paramètres adaptés aux caméras basse qualité
                RectVector faces = new RectVector();
                faceDetector.detectMultiScale(
                        grayImage,
                        faces,
                        SCALE_FACTOR,    // facteur d'échelle réduit
                        MIN_NEIGHBORS,   // minNeighbors réduit
                        0,               // flags (non utilisés)
                        new Size(MIN_FACE_SIZE, MIN_FACE_SIZE), // taille minimale du visage réduite
                        new Size(500, 500) // taille maximale du visage
                );

                // Augmenter le compteur de frames pour l'animation
                frameCount++;

                // Construire les messages d'instructions
                String mainMessage = "👤 Enregistrement facial de " + user.getName();
                String progressMessage = String.format("📸 Captures: %d/%d - Positionnez votre visage au centre",
                        capturedFaces, REGISTER_FACE_COUNT);

                // Dessiner l'interface utilisateur améliorée
                drawEnhancedUI(colorImage, mainMessage, progressMessage, false);

                if (faces.size() > 0) {
                    // Prendre le plus grand visage (probablement le plus proche)
                    Rect bestFace = getBestFace(faces);

                    // Dessiner une bordure animée autour du visage détecté
                    drawAnimatedBorder(colorImage, bestFace, frameCount);

                    // Si la taille est suffisante et le visage est bien centré
                    if (bestFace.width() >= MIN_FACE_SIZE && bestFace.height() >= MIN_FACE_SIZE) {
                        Mat face = new Mat(grayImage, bestFace);
                        resize(face, face, new Size(FACE_IMAGE_SIZE, FACE_IMAGE_SIZE));

                        // Améliorer la qualité avec une égalisation d'histogramme
                        equalizeHist(face, face);

                        // Vérifier le centrage du visage dans l'image pour une meilleure qualité
                        int centerX = colorImage.cols() / 2;
                        int centerY = colorImage.rows() / 2;
                        int faceX = bestFace.x() + bestFace.width() / 2;
                        int faceY = bestFace.y() + bestFace.height() / 2;

                        // Distance du centre (normalisée)
                        double centerDist = Math.sqrt(
                                Math.pow((faceX - centerX) / (double)colorImage.cols(), 2) +
                                        Math.pow((faceY - centerY) / (double)colorImage.rows(), 2)
                        );

                        // Afficher une indication visuelle du centrage
                        String centeringMsg;
                        Scalar centerColor;

                        if (centerDist < 0.15) {  // Bien centré
                            centeringMsg = "✅ Position idéale";
                            centerColor = new Scalar(0, 255, 0, 1);

                            // Sauvegarder l'image à intervalles pour éviter les doublons
                            if (frameCount % 5 == 0) { // Toutes les 5 frames
                                String filename = userDir + "face_" + capturedFaces + ".jpg";
                                imwrite(filename, face);
                                capturedFaces++;

                                // Message de confirmation pour chaque visage enregistré
                                System.out.println("PRÉSENTATION: Visage " + capturedFaces + "/" + REGISTER_FACE_COUNT +
                                        " capturé pour " + user.getName());

                                // Afficher un message de confirmation sur l'image
                                Mat captureOverlay = colorImage.clone();
                                rectangle(captureOverlay,
                                        new Rect(bestFace.x() - 10, bestFace.y() - 40, 150, 30),
                                        new Scalar(0, 128, 0, 0.7),
                                        -1, LINE_8, 0);

                                addWeighted(captureOverlay, 0.7, colorImage, 0.3, 0, colorImage);

                                putText(colorImage,
                                        "✓ Capture " + capturedFaces,
                                        new Point(bestFace.x(), bestFace.y() - 20),
                                        FONT_HERSHEY_SIMPLEX, 0.6,
                                        new Scalar(255, 255, 255, 1),
                                        1, LINE_AA, false);

                                // Ajouter une pause pour rendre la capture visible
                                frame.showImage(converter.convert(colorImage));
                                Thread.sleep(500); // Pause plus longue pour voir la confirmation
                            }
                        } else if (centerDist < 0.25) {  // Acceptable mais pas idéal
                            centeringMsg = "⚠️ Ajustez position";
                            centerColor = new Scalar(0, 255, 255, 1);
                        } else {  // Trop loin du centre
                            centeringMsg = "❌ Recentrez visage";
                            centerColor = new Scalar(0, 0, 255, 1);
                        }

                        // Dessinez un indicateur de centrage
                        putText(colorImage,
                                centeringMsg,
                                new Point(bestFace.x(), bestFace.y() + bestFace.height() + 20),
                                FONT_HERSHEY_SIMPLEX, 0.6,
                                centerColor,
                                1, LINE_AA, false);

                        // Ajouter des indicateurs visuels de direction si mal centré
                        if (centerDist >= 0.15) {
                            String directionMsg = "";

                            if (faceY < centerY - 50) directionMsg += "↓";
                            else if (faceY > centerY + 50) directionMsg += "↑";

                            if (faceX < centerX - 50) directionMsg += "→";
                            else if (faceX > centerX + 50) directionMsg += "←";

                            putText(colorImage,
                                    directionMsg,
                                    new Point(colorImage.cols() / 2 - 20, colorImage.rows() / 2 + 10),
                                    FONT_HERSHEY_SIMPLEX, 1.2,
                                    new Scalar(0, 200, 255, 1),
                                    2, LINE_AA, false);
                        }
                    }
                } else {
                    // Si aucun visage n'est détecté, afficher un message d'aide
                    putText(colorImage,
                            "❌ Aucun visage détecté - Rapprochez-vous",
                            new Point(colorImage.cols() / 2 - 200, colorImage.rows() / 2),
                            FONT_HERSHEY_SIMPLEX, 0.8,
                            new Scalar(0, 0, 255, 1),
                            2, LINE_AA, false);
                }

                // Afficher l'image avec les informations de progrès
                frame.showImage(converter.convert(colorImage));

                // Délai plus long pour rendre les étapes bien visibles
                Thread.sleep(DETECTION_DELAY);
            }

            // Si l'enregistrement est terminé avec succès, montrer un résumé
            if (capturedFaces >= REGISTER_FACE_COUNT) {
                Frame capturedFrame = grabber.grab();
                Mat colorImage = converter.convert(capturedFrame);

                // Message de succès
                String successMsg = "✅ Enregistrement Réussi!";
                String detailMsg = capturedFaces + " visages capturés pour " + user.getName();

                drawEnhancedUI(colorImage, successMsg, detailMsg, true);

                // Dessiner un grand symbole de succès
                circle(colorImage,
                        new Point(colorImage.cols() / 2, colorImage.rows() / 2),
                        100, new Scalar(0, 255, 0, 1), -1, LINE_8, 0);

                putText(colorImage,
                        "✓",
                        new Point(colorImage.cols() / 2 - 30, colorImage.rows() / 2 + 30),
                        FONT_HERSHEY_SIMPLEX, 3.0,
                        new Scalar(255, 255, 255, 1),
                        3, LINE_AA, false);

                frame.showImage(converter.convert(colorImage));
                Thread.sleep(CONFIRMATION_DELAY); // Afficher plus longtemps le message de succès
            }

            grabber.stop();
            frame.dispose();

            // Vérifier si nous avons capturé assez de visages
            if (capturedFaces < REGISTER_FACE_COUNT / 2) {
                System.out.println("PRÉSENTATION: Enregistrement annulé : pas assez d'images capturées pour " + user.getName());
                return false;
            }

            // Réentraîner le modèle avec les nouvelles images
            System.out.println("PRÉSENTATION: Entraînement du modèle de reconnaissance faciale");
            trainRecognizer();

            // Mettre à jour l'utilisateur pour indiquer qu'il a une identification faciale
            user.setHasFacialRecognition(true);
            userService.updateUser(user);

            // Message final de confirmation
            System.out.println("PRÉSENTATION: Visage enregistré avec succès pour " + user.getName() +
                    " (" + capturedFaces + " images capturées)");

            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement du visage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sélectionne le meilleur visage dans une liste de rectangles détectés
     * @param faces Collection de rectangles représentant des visages
     * @return Le rectangle du meilleur visage (généralement le plus grand)
     */
    private Rect getBestFace(RectVector faces) {
        Rect bestFace = faces.get(0);
        int maxArea = bestFace.width() * bestFace.height();

        for (int i = 1; i < faces.size(); i++) {
            Rect face = faces.get(i);
            int area = face.width() * face.height();
            if (area > maxArea) {
                maxArea = area;
                bestFace = face;
            }
        }

        return bestFace;
    }

    /**
     * Reconnaît un utilisateur par son visage avec une interface améliorée
     * @return L'utilisateur reconnu ou null si aucun utilisateur n'est reconnu
     * @throws Exception En cas d'erreur lors de la reconnaissance
     */
    public User recognizeFace() throws Exception {
        FrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // Configurer la fenêtre d'affichage avec une taille plus grande
        int frameWidth = 800;
        int frameHeight = 600;

        grabber.setImageWidth(frameWidth);
        grabber.setImageHeight(frameHeight);

        CanvasFrame frame = new CanvasFrame("Reconnaissance faciale",
                CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setCanvasSize(frameWidth, frameHeight);
        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        User recognizedUser = null;
        int attempts = 0;
        int frameCount = 0;
        int consecutiveMatches = 0; // Pour une reconnaissance plus stable
        User lastMatchedUser = null;

        System.out.println("PRÉSENTATION: Démarrage de la reconnaissance faciale...");

        while (frame.isVisible() && attempts < RECOGNITION_ATTEMPTS) {
            Frame capturedFrame = grabber.grab();
            Mat colorImage = converter.convert(capturedFrame);

            // Améliorer la qualité de l'image
            Mat enhancedImage = enhanceImage(colorImage);

            Mat grayImage = new Mat();
            cvtColor(enhancedImage, grayImage, COLOR_BGR2GRAY);
            equalizeHist(grayImage, grayImage);

            // Détecter les visages avec des paramètres optimisés
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(
                    grayImage,
                    faces,
                    SCALE_FACTOR,    // facteur d'échelle
                    MIN_NEIGHBORS,   // minNeighbors - plus élevé pour réduire les faux positifs
                    0,               // flags
                    new Size(MIN_FACE_SIZE, MIN_FACE_SIZE), // taille minimale
                    new Size(500, 500)    // taille maximale
            );

            // Augmenter le compteur de frames pour l'animation
            frameCount++;

            // Construire les messages d'interface
            String mainMessage = "🔍 Reconnaissance faciale en cours";
            String progressMessage = String.format("Tentative: %d/%d", attempts + 1, RECOGNITION_ATTEMPTS);

            // Dessiner l'interface utilisateur améliorée
            drawEnhancedUI(colorImage, mainMessage, progressMessage, false);

            if (!faces.empty()) {
                // Prendre le meilleur visage
                Rect bestFace = getBestFace(faces);

                // Dessiner une bordure animée autour du visage
                drawAnimatedBorder(colorImage, bestFace, frameCount);

                // Ignorer les visages trop petits pour une bonne précision
                if (bestFace.width() >= MIN_FACE_SIZE && bestFace.height() >= MIN_FACE_SIZE) {
                    // Extraire et prétraiter le visage pour la reconnaissance
                    Mat faceROI = new Mat(grayImage, bestFace);
                    resize(faceROI, faceROI, new Size(FACE_IMAGE_SIZE, FACE_IMAGE_SIZE));
                    equalizeHist(faceROI, faceROI); // Améliorer le contraste

                    // Prédire l'identité
                    IntPointer label = new IntPointer(1);
                    DoublePointer confidence = new DoublePointer(1);
                    faceRecognizer.predict(faceROI, label, confidence);

                    int predictedLabel = label.get(0);
                    double conf = confidence.get(0);

                    // Afficher le score de confiance
                    putText(colorImage,
                            String.format("Confiance: %.1f", conf),
                            new Point(bestFace.x(), bestFace.y() - 10),
                            FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 0, 255, 1), 2, LINE_AA, false);

                    // Si la confiance est assez haute (valeur plus basse = plus confiant)
                    if (conf < RECOGNITION_THRESHOLD) {
                        User user = userService.getUserById(predictedLabel);
                        if (user != null) {
                            // Vérifier si c'est le même utilisateur que la détection précédente
                            if (lastMatchedUser != null && lastMatchedUser.getId() == user.getId()) {
                                consecutiveMatches++;
                            } else {
                                consecutiveMatches = 1;
                                lastMatchedUser = user;
                            }

                            // Afficher le nom de l'utilisateur reconnu
                            putText(colorImage,
                                    user.getName() + (consecutiveMatches > 1 ? " ✓" + consecutiveMatches : ""),
                                    new Point(bestFace.x(), bestFace.y() - 30),
                                    FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0, 1), 2, LINE_AA, false);

                            // Si nous avons 3 détections consécutives du même utilisateur, c'est confirmé
                            if (consecutiveMatches >= 3) {
                                recognizedUser = user;

                                // Mettre à jour l'interface pour montrer la confirmation
                                mainMessage = "✅ Utilisateur reconnu!";
                                progressMessage = user.getName();
                                drawEnhancedUI(colorImage, mainMessage, progressMessage, true);

                                // Afficher confirmation visuelle
                                rectangle(colorImage, bestFace, new Scalar(0, 255, 0, 1), 3, LINE_8, 0);

                                // Dessiner un symbole de confirmation
                                circle(colorImage,
                                        new Point(colorImage.cols() / 2, colorImage.rows() / 2),
                                        100, new Scalar(0, 255, 0, 1), -1, LINE_8, 0);

                                putText(colorImage,
                                        "✓",
                                        new Point(colorImage.cols() / 2 - 30, colorImage.rows() / 2 + 30),
                                        FONT_HERSHEY_SIMPLEX, 3.0,
                                        new Scalar(255, 255, 255, 1),
                                        3, LINE_AA, false);

                                // Attendre un moment pour que l'utilisateur voie la confirmation
                                frame.showImage(converter.convert(colorImage));
                                Thread.sleep(CONFIRMATION_DELAY);
                                break;
                            }
                        }
                    }
                }
            } else {
                // Afficher un message d'aide si aucun visage n'est détecté
                putText(colorImage,
                        "❌ Aucun visage détecté - Rapprochez-vous",
                        new Point(colorImage.cols() / 2 - 200, colorImage.rows() / 2),
                        FONT_HERSHEY_SIMPLEX, 0.8,
                        new Scalar(0, 0, 255, 1),
                        2, LINE_AA, false);
            }

            // Afficher l'image avec les rectangles et les noms
            frame.showImage(converter.convert(colorImage));
            attempts++;

            // Petit délai pour réduire la charge CPU
            Thread.sleep(DETECTION_DELAY);
        }

        grabber.stop();
        frame.dispose();

        if (recognizedUser != null) {
            System.out.println("PRÉSENTATION: Utilisateur reconnu: " + recognizedUser.getName());
        } else {
            System.out.println("PRÉSENTATION: Aucun utilisateur reconnu après " + attempts + " tentatives");
        }

        return recognizedUser;
    }

    /**
     * Vérifie si le visage détecté correspond à l'utilisateur spécifié
     * @param user L'utilisateur à vérifier
     * @return true si le visage correspond à l'utilisateur, false sinon
     * @throws Exception En cas d'erreur lors de la vérification
     */
    public boolean verifyUserFace(User user) throws Exception {
        if (user == null) {
            System.err.println("Erreur: utilisateur null fourni pour la vérification faciale");
            return false;
        }

        FrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // Configurer la fenêtre d'affichage avec une taille plus grande
        int frameWidth = 800;
        int frameHeight = 600;

        grabber.setImageWidth(frameWidth);
        grabber.setImageHeight(frameHeight);

        CanvasFrame frame = new CanvasFrame("Vérification du visage - " + user.getName(),
                CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setCanvasSize(frameWidth, frameHeight);
        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        boolean isVerified = false;
        int attempts = 0;
        int frameCount = 0;
        int consecutiveMatches = 0;
        final int requiredMatches = 3; // Nombre de correspondances consécutives requises

        System.out.println("PRÉSENTATION: Démarrage de la vérification faciale pour " + user.getName());

        while (frame.isVisible() && attempts < RECOGNITION_ATTEMPTS && !isVerified) {
            Frame capturedFrame = grabber.grab();
            Mat colorImage = converter.convert(capturedFrame);

            // Améliorer la qualité de l'image
            Mat enhancedImage = enhanceImage(colorImage);

            Mat grayImage = new Mat();
            cvtColor(enhancedImage, grayImage, COLOR_BGR2GRAY);
            equalizeHist(grayImage, grayImage);

            // Détecter les visages
            RectVector faces = new RectVector();
            faceDetector.detectMultiScale(
                    grayImage,
                    faces,
                    SCALE_FACTOR,  // facteur d'échelle
                    MIN_NEIGHBORS,// minNeighbors
                    0,            // flags
                    new Size(MIN_FACE_SIZE, MIN_FACE_SIZE),  // taille minimale
                    new Size(500, 500) // taille maximale
            );

            // Augmenter le compteur de frames pour l'animation
            frameCount++;

            // Construire les messages d'interface
            String mainMessage = "🔍 Vérification de " + user.getName();
            String progressMessage = String.format("Tentative: %d/%d", attempts + 1, RECOGNITION_ATTEMPTS);

            // Dessiner l'interface utilisateur améliorée
            drawEnhancedUI(colorImage, mainMessage, progressMessage, false);

            if (!faces.empty()) {
                // Prendre le meilleur visage
                Rect bestFace = getBestFace(faces);

                // Dessiner une bordure animée autour du visage
                drawAnimatedBorder(colorImage, bestFace, frameCount);

                // Ignorer les visages trop petits
                if (bestFace.width() >= MIN_FACE_SIZE && bestFace.height() >= MIN_FACE_SIZE) {
                    Mat faceROI = new Mat(grayImage, bestFace);
                    resize(faceROI, faceROI, new Size(FACE_IMAGE_SIZE, FACE_IMAGE_SIZE));
                    equalizeHist(faceROI, faceROI);

                    IntPointer label = new IntPointer(1);
                    DoublePointer confidence = new DoublePointer(1);
                    faceRecognizer.predict(faceROI, label, confidence);

                    int predictedLabel = label.get(0);
                    double conf = confidence.get(0);

                    // Afficher le score de confiance
                    putText(colorImage,
                            String.format("Confiance: %.1f", conf),
                            new Point(bestFace.x(), bestFace.y() - 10),
                            FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 0, 255, 1), 2, LINE_AA, false);

                    // Vérifier si c'est l'utilisateur attendu avec une bonne confiance
                    if (predictedLabel == user.getId() && conf < VERIFICATION_THRESHOLD) {
                        consecutiveMatches++;

                        // Afficher le nom avec une indicateur de correspondance
                        putText(colorImage,
                                user.getName() + " ✓" + consecutiveMatches,
                                new Point(bestFace.x(), bestFace.y() - 30),
                                FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0, 1), 2, LINE_AA, false);

                        // Rectangle vert si correspondance
                        rectangle(colorImage, bestFace, new Scalar(0, 255, 0, 1), 2, LINE_8, 0);

                        // Si nous avons assez de correspondances consécutives
                        if (consecutiveMatches >= requiredMatches) {
                            isVerified = true;

                            // Mettre à jour l'interface pour montrer la confirmation
                            mainMessage = "✅ Vérification réussie!";
                            progressMessage = user.getName();
                            drawEnhancedUI(colorImage, mainMessage, progressMessage, true);

                            // Message de confirmation
                            rectangle(colorImage, bestFace, new Scalar(0, 255, 0, 1), 3, LINE_8, 0);

                            // Dessiner un symbole de confirmation
                            circle(colorImage,
                                    new Point(colorImage.cols() / 2, colorImage.rows() / 2),
                                    100, new Scalar(0, 255, 0, 1), -1, LINE_8, 0);

                            putText(colorImage,
                                    "✓",
                                    new Point(colorImage.cols() / 2 - 30, colorImage.rows() / 2 + 30),
                                    FONT_HERSHEY_SIMPLEX, 3.0,
                                    new Scalar(255, 255, 255, 1),
                                    3, LINE_AA, false);
                        }
                    } else {
                        // Réinitialiser le compteur de correspondances en cas d'échec
                        consecutiveMatches = 0;
                    }
                }
            } else {
                // Afficher un message d'aide si aucun visage n'est détecté
                putText(colorImage,
                        "❌ Aucun visage détecté - Rapprochez-vous",
                        new Point(colorImage.cols() / 2 - 200, colorImage.rows() / 2),
                        FONT_HERSHEY_SIMPLEX, 0.8,
                        new Scalar(0, 0, 255, 1),
                        2, LINE_AA, false);
            }

            frame.showImage(converter.convert(colorImage));
            attempts++;

            // Petit délai
            Thread.sleep(DETECTION_DELAY);

            // Si vérifié, attendre un moment pour afficher le message de confirmation
            if (isVerified) {
                Thread.sleep(CONFIRMATION_DELAY);
            }
        }

        grabber.stop();
        frame.dispose();

        if (isVerified) {
            System.out.println("PRÉSENTATION: Identité de " + user.getName() + " vérifiée avec succès");
        } else {
            System.out.println("PRÉSENTATION: Échec de la vérification pour " + user.getName() + " après " + attempts + " tentatives");
        }

        return isVerified;
    }
}