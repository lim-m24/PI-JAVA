package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Models.Abonnements;
import tn.esprit.Models.Gamification;
import tn.esprit.Services.AbonnementService;
import tn.esprit.Services.GamificationService;
import tn.esprit.Services.PaymentService;
import tn.esprit.Services.QRCodeService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserAbonnementsController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private Button btnScanQR;

    @FXML
    private TextField qrResultField;


    private final AbonnementService abonnementService = new AbonnementService();
    private final GamificationService gamificationService = new GamificationService();
    private final PaymentService paymentService = new PaymentService();
    private final QRCodeService qrCodeService = new QRCodeService();
    private Abonnements paidAbonnement = null;

    @FXML
    public void initialize() {

        loadAbonnements();

        btnScanQR.setOnAction(event -> {
            try {
                ProcessBuilder builder = new ProcessBuilder("C:\\Program Files\\Python313\\python.exe", "D:\\projets\\Nouveau dossier\\PI-JAVA\\QR_codeReader.py");

                builder.redirectErrorStream(true);
                Process process = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    qrResultField.setText(output.toString());
                } else {
                    qrResultField.setText("Erreur de script.");
                }

            } catch (IOException | InterruptedException e) {
                qrResultField.setText("Erreur : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void loadAbonnements() {
        List<Abonnements> abonnements = abonnementService.readAll();
        flowPane.getChildren().clear();
        for (Abonnements abonnement : abonnements) {
            createAbonnementCard(abonnement);
        }
    }

    private void createAbonnementCard(Abonnements abonnement) {
        VBox cardLayout = new VBox(15);
        cardLayout.getStyleClass().add("abonnement-card-modern");
        cardLayout.setPrefWidth(280);
        cardLayout.setAlignment(Pos.CENTER);
        cardLayout.setPadding(new Insets(15));

        // Top blue gradient section
        StackPane topSection = new StackPane();
        topSection.setPrefHeight(cardLayout.getPrefWidth() * 0.4);
        topSection.setMaxWidth(Double.MAX_VALUE);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null,
                new Stop(0, javafx.scene.paint.Color.LIGHTBLUE),
                new Stop(1, javafx.scene.paint.Color.DODGERBLUE));
        topSection.setBackground(new Background(new BackgroundFill(gradient, new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));
        StackPane.setAlignment(topSection, Pos.TOP_CENTER);

        // Icon
        ImageView typeIcon = null;
        double iconSize = 50;
        if (abonnement.getType().equalsIgnoreCase("freemium")) {
            Image freemiumIcon = new Image(getClass().getResourceAsStream("/images/freemium.png"));
            typeIcon = new ImageView(freemiumIcon);
        } else if (abonnement.getType().equalsIgnoreCase("premium")) {
            Image premiumIcon = new Image(getClass().getResourceAsStream("/images/premium.png"));
            typeIcon = new ImageView(premiumIcon);
        }

        StackPane iconPlaceholder = new StackPane();
        iconPlaceholder.setPrefSize(iconSize, iconSize);
        iconPlaceholder.setAlignment(Pos.CENTER);
        if (typeIcon != null) {
            typeIcon.setFitHeight(iconSize);
            typeIcon.setFitWidth(iconSize);
            typeIcon.setPreserveRatio(true);
            iconPlaceholder.getChildren().add(typeIcon);
        } else {
            Circle placeholderCircle = new Circle(iconSize / 2, javafx.scene.paint.Color.LIGHTGRAY);
            Label placeholderLabel = new Label("?");
            placeholderLabel.setStyle("-fx-font-size: " + (iconSize / 2) + "px; -fx-font-weight: bold; -fx-text-fill: white;");
            iconPlaceholder.getChildren().addAll(placeholderCircle, placeholderLabel);
        }
        StackPane.setAlignment(iconPlaceholder, Pos.CENTER);
        topSection.getChildren().add(iconPlaceholder);

        // Abonnement Name
        Label nomLabel = new Label("Abonnement : " + abonnement.getNom());
        nomLabel.getStyleClass().add("card-title-modern");
        nomLabel.setAlignment(Pos.CENTER);
        nomLabel.setWrapText(true);

        // Abonnement Type
        Label typeLabel = new Label(abonnement.getType());
        typeLabel.getStyleClass().add("card-type-modern");
        typeLabel.setAlignment(Pos.CENTER);
        typeLabel.setWrapText(true);

        VBox contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(nomLabel, typeLabel);

        // QR Code
// Récupérer le nom et le prix de l'abonnement
        String nomAbonnement = abonnement.getNom();
        double prixAbonnement = abonnement.getPrix();

// Vérifier les données
        System.out.println("Nom: " + nomAbonnement + ", Prix: " + prixAbonnement);

// Appel au service QR
        Image qrCodeImage = qrCodeService.generateQRCode(nomAbonnement, 200, 200);


// Créer l'ImageView pour afficher le QR code
        ImageView qrCodeImageView = new ImageView(qrCodeImage);
        qrCodeImageView.setFitWidth(100);
        qrCodeImageView.setFitHeight(100);

// Créer un StackPane pour contenir le QR code
        StackPane qrCodePane = new StackPane(qrCodeImageView);
        qrCodePane.setAlignment(Pos.CENTER);
        qrCodePane.setPadding(new Insets(10, 0, 0, 0));





        // Pay Button
        Button payButton = new Button("Pay");
        payButton.getStyleClass().add("pay-button-modern");
        payButton.setOnAction(event -> handlePay(abonnement));

        // Expiry Date Label
        Label expiryLabel = new Label();
        LocalDate expiryDate = abonnementService.getExpiryDate(abonnement.getId());
        if (expiryDate != null) {
            expiryLabel.setText("Expires on: " + expiryDate.toString());
            expiryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #777;");
        }

        VBox bottomSection = new VBox(10);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.getChildren().addAll(qrCodePane, payButton, expiryLabel); // Ensured expiryLabel is added here

        cardLayout.getChildren().addAll(topSection, contentBox, bottomSection);

        cardLayout.setOnMouseClicked(event -> showGamificationPopup(abonnement));

        flowPane.getChildren().add(cardLayout);
    }
    private void showGamificationPopup(Abonnements abonnement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GamificationPopupView.fxml"));
            Parent root = loader.load();

            GamificationPopupController controller = loader.getController();
            controller.setTitle("Gamification for " + abonnement.getNom());

            List<Gamification> gamifications = gamificationService.readAll().stream()
                    .filter(g -> g.getTypeAbonnement() == abonnement.getId())
                    .collect(Collectors.toList());

            controller.setGamifications(gamifications);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Gamification Details");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load gamification details.");
        }
    }

    @FXML
    private void handlePay(Abonnements abonnement) {
        if (paidAbonnement == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaymentFormView.fxml"));
                Parent root = loader.load();

                PaymentFormController controller = loader.getController();
                controller.setAbonnement(abonnement);
                controller.setPaymentService(paymentService);
                controller.setUserAbonnementsController(this);

                Stage paymentStage = new Stage();
                paymentStage.initModality(Modality.APPLICATION_MODAL);
                paymentStage.setTitle("Payment Details");
                paymentStage.setScene(new Scene(root));
                controller.setDialogStage(paymentStage);
                paymentStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Could not load payment form.");
            }
        } else {
            showAlert("Payment Error", "You can only have one active abonnement.");
        }
    }

    public void markAbonnementAsPaid(Abonnements abonnement) {
        showGamification(abonnement);
        setCardToPaid(abonnement);
        paidAbonnement = abonnement;
        loadAbonnements();
    }

    private void showGamification(Abonnements abonnement) {
        List<Gamification> gamifications = gamificationService.readAll().stream()
                .filter(g -> g.getTypeAbonnement() == abonnement.getId())
                .collect(Collectors.toList());

        if (!gamifications.isEmpty()) {
            StringBuilder gamificationDetails = new StringBuilder("Gamification for " + abonnement.getNom() + ":\n");
            for (Gamification gamification : gamifications) {
                gamificationDetails.append("- ").append(gamification.getNom()).append(": ").append(gamification.getDescription()).append("\n");
            }
            showAlert("Payment Successful!", gamificationDetails.toString());
        } else {
            showAlert("Payment Successful!", "No gamification rules found for " + abonnement.getNom() + ".");
        }
    }

    private void setCardToPaid(Abonnements abonnement) {
        for (javafx.scene.Node node : flowPane.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                if (card.getStyleClass().contains("abonnement-card-modern")) {
                    // Find the label and button to update their state if needed
                    for (javafx.scene.Node child : card.getChildren()) {
                        if (child instanceof VBox contentBox) {
                            for (javafx.scene.Node contentChild : contentBox.getChildren()) {
                                if (contentChild instanceof Label nomLabel && nomLabel.getText().equals("Abonnement : " + abonnement.getNom())) {
                                    // Optionally change the appearance of the label
                                }
                                if (contentChild instanceof Button payButton && payButton.getText().equals("Pay")) {
                                    payButton.setDisable(true);
                                    payButton.setStyle("-fx-opacity: 0.7; -fx-cursor: default; -fx-background-color: #4db9b9; -fx-text-fill: white;");
                                }
                            }
                        }
                    }
                    card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 5); -fx-background-color: #f0fff0; -fx-border-color: #a9a9a9; -fx-border-radius: 8px;");
                    break;
                }
            }
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}