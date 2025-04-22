package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CategorieService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

public class CategoriesReadAllController {

    @FXML
    private TableColumn<Categories, String> coverCol;
    @FXML
    private TableColumn<Categories, String> dateCol;
    @FXML
    private TableColumn<Categories, String> descriptionCol;
    @FXML
    private TableColumn<Categories, String> nomCol;
    @FXML
    private TableView<Categories> tableviewCategories;
    @FXML
    private TableColumn<Categories, Void> actionCol;

    @FXML
    private TextField nomField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField coverField;
    @FXML
    private Button updateButton;
    @FXML
    private Button addButton;



    private final CategorieService CategorieService = new CategorieService();
    private Categories selectedCategory;
    private static final double ROW_HEIGHT = 50.0;

    @FXML
    void initialize() {
        List<Categories> categories = CategorieService.readAll();
        ObservableList<Categories> observableList = FXCollections.observableList(categories);
        tableviewCategories.setItems(observableList);
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        coverCol.setCellValueFactory(new PropertyValueFactory<>("cover"));

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
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_creation"));

        addActionButtonsToTable();

        tableviewCategories.setFixedCellSize(ROW_HEIGHT);
        tableviewCategories.prefHeightProperty().bind(
                tableviewCategories.fixedCellSizeProperty().multiply(
                        FXCollections.observableList(categories).size() + 1.01
                )
        );
        tableviewCategories.minHeightProperty().bind(tableviewCategories.prefHeightProperty());
        tableviewCategories.maxHeightProperty().bind(tableviewCategories.prefHeightProperty());
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
                    Categories selectedCategory = getTableView().getItems().get(getIndex());
                    System.out.println("Delete clicked for: " + selectedCategory.getNom());
                    CategorieService.DeleteByID(selectedCategory.getId());
                    initialize();
                });

                updateButton.setOnAction(event -> {
                    Categories selectedCategory = getTableView().getItems().get(getIndex());
                    System.out.println("Update clicked for: " + selectedCategory.getNom());
                    handleUpdate(selectedCategory);
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
    private void handleUpdate(Categories category) {
        selectedCategory = category;
        nomField.setText(category.getNom());
        descriptionField.setText(category.getDescription());
        coverField.setText(category.getCover());

        updateButton.setDisable(false);
        addButton.setDisable(true);
    }

    @FXML
    private void handleUpdateAction() {
        if (selectedCategory == null || !validateForm()) return;

        selectedCategory.setNom(nomField.getText().trim());
        selectedCategory.setDescription(descriptionField.getText().trim());
        selectedCategory.setCover(coverField.getText().trim());

        CategorieService.Update(selectedCategory);
        updateButton.setDisable(true);
        addButton.setDisable(false);
        System.out.println("Category updated: " + selectedCategory.getNom());

        initialize();
        clearForm();
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) return;

        LocalDateTime currentDateTime = LocalDateTime.now();
        Categories newCategory = new Categories(
                nomField.getText().trim(),
                descriptionField.getText().trim(),
                coverField.getText().trim(),
                currentDateTime
        );
        CategorieService.Add(newCategory);
        System.out.println("Category added: " + newCategory.getNom());

        initialize();
        clearForm();
    }

    private void clearForm() {
        nomField.clear();
        descriptionField.clear();
        coverField.clear();
        updateButton.setDisable(true);
    }

    private boolean validateForm() {
        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();
        String cover = coverField.getText().trim();

        if (nom.isEmpty() || description.isEmpty() || cover.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields must be filled.");
            return false;
        }

        if (nom.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Nom must be at least 3 characters.");
            return false;
        }

        if (description.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Description must be at least 10 characters.");
            return false;
        }

        if (cover.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Cover field must not be empty.");
            return false;
        }

        return true;
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
