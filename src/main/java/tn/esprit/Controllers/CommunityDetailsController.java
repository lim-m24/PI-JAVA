package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tn.esprit.Models.Community;
import tn.esprit.Models.Events;
import tn.esprit.Services.CommunityService;
import tn.esprit.Services.EventService;
import tn.esprit.Services.EventUserService;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.AnchorPane;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.io.image.ImageDataFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class CommunityDetailsController {

    @FXML
    private ImageView coverImageView;

    @FXML
    private Label communityNameLabel;

    @FXML
    private Label memberCountLabel;

    @FXML
    private Label memberCountSideLabel;

    @FXML
    private Text descriptionText;

    @FXML
    private Button leaveButton;

    @FXML
    private Button menuButton;

    @FXML
    private Button eventTab;

    @FXML
    private Button discussionTab;

    @FXML
    private Button membersTab;

    @FXML
    private GridPane eventsGrid;

    @FXML
    private VBox suggestedCommunitiesVBox;

    private Community community;
    private final CommunityService communityService = new CommunityService();
    private final EventService eventService = new EventService();
    private final EventUserService eventUserService = new EventUserService();
    private BorderPane FrontBorderpane;

    public void setFrontBorderpane(BorderPane frontBorderpane) {
        this.FrontBorderpane = frontBorderpane;
    }

    public void setCommunity(Community community) {
        this.community = community;
        populateDetails();
        loadSuggestedCommunities();
        initializeMenuButton();
    }

    private void populateDetails() {
        communityNameLabel.setText(community.getNom());
        memberCountLabel.setText("Public community • " + community.getNbr_membre() + " Members");
        memberCountSideLabel.setText(String.valueOf(community.getNbr_membre()));
        descriptionText.setText(community.getDescription());

        String relativePath = community.getCover();
        if (relativePath != null && !relativePath.isEmpty()) {
            File file = new File("." + relativePath);
            if (file.exists()) {
                coverImageView.setImage(new Image(file.toURI().toString()));
            } else {
                System.err.println("Image not found: " + file.getAbsolutePath());
                coverImageView.setImage(new Image("/images/placeholder.jpg"));
            }
        }

        // Load events for the selected community
        List<Events> events = eventService.readAll().stream()
                .filter(event -> event.getId_community_id() == community.getId())
                .collect(Collectors.toList());

        eventsGrid.getChildren().clear(); // Clear existing events
        for (Events event : events) {
            String dateRange = event.getStarted_at().toLocalDate().toString() + " " +
                    event.getStarted_at().toLocalTime().toString() + " - " +
                    event.getFinish_at().toLocalDate().toString() + " " +
                    event.getFinish_at().toLocalTime().toString();
            addEvent(event, dateRange);
        }
    }

    private void addEvent(Events event, String dateRange) {
        VBox eventCard = new VBox(10);
        eventCard.setStyle("-fx-background-color: #2C3E50; -fx-padding: 10; -fx-border-radius: 5; -fx-cursor: hand;");
        eventCard.setOnMouseClicked(e -> showEventDetails(event));

        // Event Cover Image
        ImageView eventImage = new ImageView();
        eventImage.setFitWidth(200);
        eventImage.setFitHeight(150);
        String coverPath = event.getCover();
        if (coverPath != null && !coverPath.isEmpty()) {
            File file = new File("." + coverPath);
            if (file.exists()) {
                eventImage.setImage(new Image(file.toURI().toString()));
            } else {
                eventImage.setImage(new Image("/images/placeholder.jpg"));
            }
        } else {
            eventImage.setImage(new Image("/images/placeholder.jpg"));
        }

        // Event Title
        Label titleLabel = new Label(event.getNom());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");

        // Date Label
        Label dateLabel = new Label(dateRange);
        dateLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12;");

        // Buttons
        HBox buttonBox = new HBox(10);
        Button interestedButton = new Button("Interested");
        interestedButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-padding: 5 10;");
        interestedButton.setOnAction(e -> handleInterested(event));

        Button goingButton = new Button("Going");
        goingButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-padding: 5 10;");
        goingButton.setOnAction(e -> handleGoing(event));

        buttonBox.getChildren().addAll(interestedButton, goingButton);

        eventCard.getChildren().addAll(eventImage, titleLabel, dateLabel, buttonBox);

        int row = eventsGrid.getChildren().size() / 3;
        int col = eventsGrid.getChildren().size() % 3;
        eventsGrid.add(eventCard, col, row);
    }

    private void showEventDetails(Events event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            BorderPane eventDetailsPane = loader.load();

            EventDetailsController controller = loader.getController();
            controller.setEvent(event);

            if (FrontBorderpane != null) {
                FrontBorderpane.setCenter(eventDetailsPane);
            } else {
                System.err.println("FrontBorderpane is not set in CommunityDetailsController.");
            }
        } catch (IOException e) {
            System.err.println("Error loading EventDetails.fxml: " + e.getMessage());
        }
    }

    private void handleInterested(Events event) {
        // Logic to mark user as interested
        System.out.println("User marked as interested in event: " + event.getNom());
        // Add to event_user table with type "Interested" using EventUserService
        // Example: eventUserService.Add(new EventUser(currentUserId, event.getId(), "Interested"));
        // Refresh UI if needed
    }

    private void handleGoing(Events event) {
        // Logic to mark user as going
        System.out.println("User marked as going to event: " + event.getNom());
        // Add to event_user table with type "Participate" using EventUserService
        // Example: eventUserService.Add(new EventUser(currentUserId, event.getId(), "Participate"));
        // Refresh UI if needed
    }

    private void loadSuggestedCommunities() {
        List<Community> allCommunities = communityService.readAll();

        List<Community> suggestedCommunities = allCommunities.stream()
                .filter(c -> c.getId_categorie_id() == community.getId_categorie_id())
                .filter(c -> c.getId() != community.getId())
                .limit(3)
                .collect(Collectors.toList());

        suggestedCommunitiesVBox.getChildren().removeIf(node -> !(node instanceof HBox && ((HBox) node).getChildren().stream()
                .anyMatch(child -> child instanceof Label && "Suggested communities".equals(((Label) child).getText()))));

        for (Community suggested : suggestedCommunities) {
            HBox communityBox = new HBox(10);
            communityBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);

            String relativePath = suggested.getCover();
            if (relativePath != null && !relativePath.isEmpty()) {
                File file = new File("." + relativePath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image("/images/placeholder.jpg"));
                }
            } else {
                imageView.setImage(new Image("/images/placeholder.jpg"));
            }

            VBox details = new VBox(5);
            Label nameLabel = new Label(suggested.getNom());
            nameLabel.setStyle("-fx-text-fill: white;");

            Label memberLabel = new Label(suggested.getNbr_membre() + " Members");
            memberLabel.setStyle("-fx-text-fill: gray;");

            Button viewButton = new Button("View");
            viewButton.setStyle("-fx-text-fill: white; -fx-background-color: transparent; -fx-border-color: white;");

            viewButton.setOnAction(event -> {
                if (FrontBorderpane == null) {
                    System.err.println("FrontBorderpane is not set in CommunityDetailsController.");
                    return;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommunityDetails.fxml"));
                    AnchorPane communityDetailsPane = loader.load();

                    CommunityDetailsController controller = loader.getController();
                    controller.setCommunity(suggested);
                    controller.setFrontBorderpane(FrontBorderpane);

                    FrontBorderpane.setCenter(communityDetailsPane);
                } catch (IOException e) {
                    System.err.println("Error loading CommunityDetails.fxml: " + e.getMessage());
                }
            });

            details.getChildren().addAll(nameLabel, memberLabel, viewButton);
            communityBox.getChildren().addAll(imageView, details);

            suggestedCommunitiesVBox.getChildren().add(communityBox);
        }

        if (suggestedCommunities.isEmpty()) {
            Label noSuggestionsLabel = new Label("No suggested communities available.");
            noSuggestionsLabel.setStyle("-fx-text-fill: gray;");
            suggestedCommunitiesVBox.getChildren().add(noSuggestionsLabel);
        }
    }

    private void initializeMenuButton() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem shareItem = new MenuItem("Share");
        shareItem.setOnAction(event -> {
            showQRCodePopup();
        });

        MenuItem copyLinkItem = new MenuItem("Copy Link");
        copyLinkItem.setOnAction(event -> {
            System.out.println("Copy link for community: " + community.getNom());
        });

        contextMenu.getItems().addAll(shareItem, copyLinkItem);

        contextMenu.setStyle("-fx-background-color: #2C3E50; -fx-border-radius: 5;");

        for (MenuItem item : contextMenu.getItems()) {
            item.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #2C3E50; -fx-padding: 5 10 5 10;");
        }

        menuButton.setOnAction(event -> {
            contextMenu.show(menuButton,
                    menuButton.getScene().getWindow().getX() + menuButton.localToScene(menuButton.getBoundsInLocal()).getMinX(),
                    menuButton.getScene().getWindow().getY() + menuButton.localToScene(menuButton.getBoundsInLocal()).getMaxY());
        });
    }

    private void showQRCodePopup() {
        try {
            String url = "www.syncylinky.tn/community/" + community.getId();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200);

            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Image qrImage = new Image(bais);

            Stage popupStage = new Stage();
            popupStage.setTitle("Share Community - QR Code");

            ImageView qrImageView = new ImageView(qrImage);
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);

            // Add Download Button
            Button downloadButton = new Button("Download as PDF");
            downloadButton.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 5;");
            downloadButton.setOnAction(e -> downloadQRCodeAsPDF(bufferedImage));

            StackPane popupLayout = new StackPane();
            popupLayout.getChildren().addAll(qrImageView, downloadButton);
            StackPane.setAlignment(downloadButton, javafx.geometry.Pos.BOTTOM_CENTER);
            popupLayout.setStyle("-fx-background-color: #1A2A44; -fx-padding: 20;");

            Scene popupScene = new Scene(popupLayout, 240, 280); // Increased height to accommodate button
            popupStage.setScene(popupScene);

            popupStage.setX(menuButton.getScene().getWindow().getX() + (menuButton.getScene().getWindow().getWidth() - popupStage.getWidth()) / 2);
            popupStage.setY(menuButton.getScene().getWindow().getY() + (menuButton.getScene().getWindow().getHeight() - popupStage.getHeight()) / 2);

            popupStage.show();
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
        }
    }

    private void downloadQRCodeAsPDF(BufferedImage qrImage) {
        try {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save QR Code as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(new Stage());

            if (file != null) {
                PdfWriter writer = new PdfWriter(new FileOutputStream(file));
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Convert BufferedImage to PdfImage
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(qrImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                com.itextpdf.layout.element.Image pdfImage = new com.itextpdf.layout.element.Image(ImageDataFactory.create(imageBytes));

                // Add image to PDF (centered)
                pdfImage.setWidth(200);
                pdfImage.setHeight(200);
                document.add(pdfImage);

                document.close();
                System.out.println("QR Code saved as PDF: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error saving QR code as PDF: " + e.getMessage());
        }
    }
}