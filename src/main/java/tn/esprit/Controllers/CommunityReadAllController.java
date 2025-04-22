package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.Models.Community;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CommunityService;
import tn.esprit.Services.CategorieService;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityReadAllController {

    @FXML
    private TableColumn<Community, String> coverCol;
    @FXML
    private TableColumn<Community, String> createdAtCol;
    @FXML
    private TableColumn<Community, String> descriptionCol;
    @FXML
    private TableColumn<Community, String> nomCol;
    @FXML
    private TableView<Community> tableviewCommunity;
    @FXML
    private TableColumn<Community, Void> actionCol;
    @FXML
    private TableColumn<Community, Integer> nbrCol;
    @FXML
    private TableColumn<Community, Byte> statutCol;

    @FXML
    private TextField nomField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField coverField;
    @FXML
    private TextField nbrMembreField;
    @FXML
    private ComboBox<Categories> categoryComboBox;
    @FXML
    private Button updateButton;
    @FXML
    private Button addButton;
    @FXML
    private TableColumn<Community, String> categoryCol;

    private final CommunityService communityService = new CommunityService();
    private final CategorieService categoryService = new CategorieService();
    private Community selectedCommunity;
    private static final double ROW_HEIGHT = 60.0;
    private final Map<Integer, String> categoryNameMap = new HashMap<>();

    @FXML
    void initialize() {
        List<Community> communities = communityService.readAll();
        ObservableList<Community> observableList = FXCollections.observableList(communities);
        tableviewCommunity.setItems(observableList);

        CategorieService categorieService = new CategorieService();
        for (Categories cat : categorieService.readAll()) {
            categoryNameMap.put(cat.getId(), cat.getNom());
        }

        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        coverCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);

                if (empty || imageUrl == null || imageUrl.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(imageUrl, 50, 50, true, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }

        });
        nbrCol.setCellValueFactory(new PropertyValueFactory<>("nbr_membre"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(categoryNameMap.getOrDefault(cellData.getValue().getId_categorie_id(), "Unknown"))
        );
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("created_at"));

        loadCategories();

        addActionButtonsToTable();
    }

    private void loadCategories() {
        List<Categories> categories = categoryService.readAll();  // Fetch categories
        ObservableList<Categories> categoryList = FXCollections.observableList(categories);
        categoryComboBox.setItems(categoryList);
    }

    private void addActionButtonsToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final Button updateButton = new Button("Update");
            private final HBox hBox = new HBox(10, updateButton, deleteButton);

            {
                hBox.setPadding(new Insets(5));
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");
                updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");

                deleteButton.setOnAction(event -> {
                    Community selectedCommunity = getTableView().getItems().get(getIndex());
                    System.out.println("Delete clicked for: " + selectedCommunity.getNom());
                    communityService.DeleteByID(selectedCommunity.getId());
                    initialize();
                });

                updateButton.setOnAction(event -> {
                    Community selectedCommunity = getTableView().getItems().get(getIndex());
                    System.out.println("Update clicked for: " + selectedCommunity.getNom());
                    handleUpdate(selectedCommunity);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
                }
            }
        });
    }

    private void handleUpdate(Community community) {
        selectedCommunity = community;

        Categories selectedCategory = categoryComboBox.getItems()
                .stream()
                .filter(c -> c.getId() == community.getId_categorie_id())
                .findFirst()
                .orElse(null);

        categoryComboBox.getSelectionModel().select(selectedCategory);

        nomField.setText(community.getNom());
        descriptionField.setText(community.getDescription());
        coverField.setText(community.getCover());
        nbrMembreField.setText(String.valueOf(community.getNbr_membre()));

        updateButton.setDisable(false);
        addButton.setDisable(true);
    }


    @FXML
    private void handleUpdateAction() {
        if (selectedCommunity == null || !validateForm()) {
            showAlert("Please select a community and fill in all fields correctly.");
            return;
        }

        selectedCommunity.setNom(nomField.getText());
        selectedCommunity.setDescription(descriptionField.getText());
        selectedCommunity.setCover(coverField.getText());
        selectedCommunity.setNbr_membre(Integer.parseInt(nbrMembreField.getText()));

        selectedCommunity.setId_categorie_id(categoryComboBox.getValue().getId());

        communityService.Update(selectedCommunity);
        updateButton.setDisable(true);
        addButton.setDisable(false);
        System.out.println("Community updated: " + selectedCommunity.getNom());

        initialize();
        clearForm();
    }


    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            showAlert("Please fill in all fields correctly.");
            return;
        }

        Categories selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert("Please select a category.");
            return;
        }

        LocalDateTime currentDateTime = LocalDateTime.now();

        Community newCommunity = new Community(
                selectedCategory.getId(),
                nomField.getText(),
                descriptionField.getText(),
                coverField.getText(),
                currentDateTime,
                Integer.parseInt(nbrMembreField.getText()),
                (byte) 1
        );

        communityService.Add(newCommunity);
        System.out.println("Community added: " + newCommunity.getNom());

        initialize();
        clearForm();
    }



    private void clearForm() {
        nomField.clear();
        descriptionField.clear();
        coverField.clear();
        nbrMembreField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        updateButton.setDisable(true);
    }
    private boolean validateForm() {
        boolean isValid = true;

        if (nomField.getText().trim().isEmpty()) {
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            nomField.setStyle("");
        }

        if (descriptionField.getText().trim().isEmpty()) {
            descriptionField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            descriptionField.setStyle("");
        }

        if (coverField.getText().trim().isEmpty()) {
            coverField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            coverField.setStyle("");
        }

        try {
            Integer.parseInt(nbrMembreField.getText());
            nbrMembreField.setStyle("");
        } catch (NumberFormatException e) {
            nbrMembreField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (categoryComboBox.getValue() == null) {
            categoryComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            categoryComboBox.setStyle("");
        }

        return isValid;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Form Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            String imageUrl = file.toURI().toString();
            coverField.setText(imageUrl);
        }
    }

}
