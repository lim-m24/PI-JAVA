package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.Models.Categories;
import tn.esprit.Services.CategorieService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
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
    private TableColumn<Categories, Integer> idCol;
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
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        coverCol.setCellValueFactory(new PropertyValueFactory<>("cover"));
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
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

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
        if (selectedCategory != null) {
            selectedCategory.setNom(nomField.getText());
            selectedCategory.setDescription(descriptionField.getText());
            selectedCategory.setCover(coverField.getText());

            LocalDateTime creationDate = selectedCategory.getDate_creation();
            selectedCategory.setDate_creation(creationDate);

            CategorieService.Update(selectedCategory);
            updateButton.setDisable(true);
            addButton.setDisable(false);
            System.out.println("Category updated: " + selectedCategory.getNom());

            initialize();
            clearForm();
        }
    }

    @FXML
    private void handleAdd() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        Categories newCategory = new Categories(
                nomField.getText(),
                descriptionField.getText(),
                coverField.getText(),
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
}
