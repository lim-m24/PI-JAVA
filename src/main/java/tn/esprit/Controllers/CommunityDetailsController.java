package tn.esprit.Controllers;

import javafx.fxml.FXML;
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
import tn.esprit.Services.CommunityService;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

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

        addEvent("Formation With M.Olivier", "25/10 10:40 - 27/10 10:40", "0 Interested • 0 Participated");
        addEvent("Workshop Mixte Présentiel", "29/10 12:35 - 27/10 07:40", "0 Interested • 0 Participated");
    }

    private void addEvent(String title, String date, String stats) {
        VBox eventCard = new VBox(5);
        eventCard.setStyle("-fx-background-color: #2C3E50; -fx-padding: 10; -fx-border-radius: 5;");

        ImageView eventImage = new ImageView(new Image("/images/placeholder.jpg"));
        eventImage.setFitWidth(150);
        eventImage.setFitHeight(100);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-text-fill: gray;");

        Label statsLabel = new Label(stats);
        statsLabel.setStyle("-fx-text-fill: gray;");

        eventCard.getChildren().addAll(eventImage, titleLabel, dateLabel, statsLabel);

        int row = eventsGrid.getChildren().size() / 3;
        int col = eventsGrid.getChildren().size() % 3;
        eventsGrid.add(eventCard, col, row);
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
                    BorderPane communityDetailsPane = loader.load();

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

        // Apply styles to the ContextMenu
        contextMenu.setStyle("-fx-background-color: #2C3E50; -fx-border-radius: 5;");

        // Style each MenuItem and handle hover effects
        for (MenuItem item : contextMenu.getItems()) {
            // Default style
            item.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #2C3E50; -fx-padding: 5 10 5 10;");

        }

        // Show the context menu on left-click
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
            ImageIO.write(bufferedImage, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            Image qrImage = new Image(bais);

            Stage popupStage = new Stage();
            popupStage.setTitle("Share Community - QR Code");

            ImageView qrImageView = new ImageView(qrImage);
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);

            StackPane popupLayout = new StackPane(qrImageView);
            popupLayout.setStyle("-fx-background-color: #1A2A44; -fx-padding: 20;");

            Scene popupScene = new Scene(popupLayout, 240, 240);
            popupStage.setScene(popupScene);

            popupStage.setX(menuButton.getScene().getWindow().getX() + (menuButton.getScene().getWindow().getWidth() - popupStage.getWidth()) / 2);
            popupStage.setY(menuButton.getScene().getWindow().getY() + (menuButton.getScene().getWindow().getHeight() - popupStage.getHeight()) / 2);

            popupStage.show();
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
        }
    }
}