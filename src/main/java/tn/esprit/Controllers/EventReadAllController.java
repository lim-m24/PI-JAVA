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
import tn.esprit.Models.Events;
import tn.esprit.Models.Community;
import tn.esprit.Models.Events;
import tn.esprit.Services.EventService;
import tn.esprit.Services.CommunityService;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventReadAllController {

    @FXML
    private TableColumn<Events, String> nomCol;
    @FXML
    private TableColumn<Events, String> descriptionCol;
    @FXML
    private TableColumn<Events, String> startedAtCol;
    @FXML
    private TableColumn<Events, String> finishAtCol;
    @FXML
    private TableColumn<Events, String> lieuCol;
    @FXML
    private TableColumn<Events, String> typeCol;
    @FXML
    private TableColumn<Events, String> coverCol;
    @FXML
    private TableColumn<Events, String> linkCol;
    @FXML
    private TableColumn<Events, String> accesCol;
    @FXML
    private TableColumn<Events, Integer> communityIdCol;
    @FXML
    private TableColumn<Events, Void> actionCol;
    @FXML
    private TableView<Events> tableviewEvent;

    @FXML
    private TextField nomField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField startedAtField;
    @FXML
    private TextField finishAtField;
    @FXML
    private TextField lieuField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField coverField;
    @FXML
    private TextField linkField;
    @FXML
    private TextField accesField;
    @FXML
    private ComboBox<Community> communityComboBox;
    @FXML
    private Button updateButton;
    @FXML
    private Button addButton;

    private final EventService eventService = new EventService();
    private final CommunityService communityService = new CommunityService();
    private Events selectedEvent;
    private static final double ROW_HEIGHT = 60.0;
    private final Map<Integer, String> communityNameMap = new HashMap<>();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @FXML
    private DatePicker startedAtDatePicker;

    @FXML
    private TextField startedAtTimeField;

    @FXML
    private DatePicker finishAtDatePicker;

    @FXML
    private TextField finishAtTimeField;

    private LocalDateTime startedAt;
    private LocalDateTime finishAt;

    @FXML
    void initialize() {
        startedAtDatePicker.setValue(LocalDate.now());
        finishAtDatePicker.setValue(LocalDate.now().plusDays(1));

        startedAtDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateStartedAt());
        startedAtTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateStartedAt());
        finishAtDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateFinishAt());
        finishAtTimeField.textProperty().addListener((obs, oldVal, newVal) -> updateFinishAt());

        List<Events> events = eventService.readAll();
        ObservableList<Events> observableList = FXCollections.observableList(events);
        tableviewEvent.setItems(observableList);

        // Populate community name map for display in the table
        for (Community community : communityService.readAll()) {
            communityNameMap.put(community.getId(), community.getNom());
        }

        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        startedAtCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStarted_at().format(dateTimeFormatter)));
        finishAtCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFinish_at().format(dateTimeFormatter)));
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        coverCol.setCellValueFactory(new PropertyValueFactory<>("cover"));
        coverCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String fileName, boolean empty) {
                super.updateItem(fileName, empty);

                if (empty || fileName == null || fileName.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    File file = new File("." + fileName); // Remove prepended "uploads/"
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString(), 50, 50, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        linkCol.setCellValueFactory(new PropertyValueFactory<>("link"));
        accesCol.setCellValueFactory(new PropertyValueFactory<>("acces"));
        communityIdCol.setCellValueFactory(new PropertyValueFactory<>("id_community_id"));
        communityIdCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer communityId, boolean empty) {
                super.updateItem(communityId, empty);
                if (empty || communityId == null) {
                    setText(null);
                } else {
                    setText(communityNameMap.getOrDefault(communityId, "Unknown"));
                }
            }
        });

        loadCommunities();

        addActionButtonsToTable();
        tableviewEvent.setFixedCellSize(ROW_HEIGHT);
        tableviewEvent.prefHeightProperty().bind(
                tableviewEvent.fixedCellSizeProperty().multiply(
                        FXCollections.observableList(events).size() + 1.01
                )
        );
        tableviewEvent.minHeightProperty().bind(tableviewEvent.prefHeightProperty());
        tableviewEvent.maxHeightProperty().bind(tableviewEvent.prefHeightProperty());
    }

    private void updateStartedAt() {
        try {
            LocalDate date = startedAtDatePicker.getValue();
            LocalTime time = LocalTime.parse(startedAtTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            startedAt = LocalDateTime.of(date, time);
            System.out.println("Started At: " + startedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } catch (Exception e) {
            System.err.println("Invalid start date/time format: " + e.getMessage());
        }
    }

    private void updateFinishAt() {
        try {
            LocalDate date = finishAtDatePicker.getValue();
            LocalTime time = LocalTime.parse(finishAtTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            finishAt = LocalDateTime.of(date, time);
            System.out.println("Finish At: " + finishAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } catch (Exception e) {
            System.err.println("Invalid finish date/time format: " + e.getMessage());
        }
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishAt() {
        return finishAt;
    }

    private void loadCommunities() {
        List<Community> communities = communityService.readAll();
        ObservableList<Community> communityList = FXCollections.observableList(communities);
        communityComboBox.setItems(communityList);
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
                    Events selectedEvent = getTableView().getItems().get(getIndex());
                    System.out.println("Delete clicked for: " + selectedEvent.getNom());
                    eventService.DeleteByID(selectedEvent.getId());
                    initialize();
                });

                updateButton.setOnAction(event -> {
                    Events selectedEvent = getTableView().getItems().get(getIndex());
                    System.out.println("Update clicked for: " + selectedEvent.getNom());
                    handleUpdate(selectedEvent);
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

    private void handleUpdate(Events event) {
        selectedEvent = event;

        Community selectedCommunity = communityComboBox.getItems()
                .stream()
                .filter(c -> c.getId() == event.getId_community_id())
                .findFirst()
                .orElse(null);

        communityComboBox.getSelectionModel().select(selectedCommunity);

        nomField.setText(event.getNom());
        descriptionField.setText(event.getDescription());
        startedAtField.setText(event.getStarted_at().format(dateTimeFormatter));
        finishAtField.setText(event.getFinish_at().format(dateTimeFormatter));
        lieuField.setText(event.getLieu());
        typeField.setText(event.getType());
        coverField.setText(event.getCover());
        linkField.setText(event.getLink());
        accesField.setText(event.getAcces());

        updateButton.setDisable(false);
        addButton.setDisable(true);
    }

    @FXML
    private void handleUpdateAction() {
        if (selectedEvent == null || !validateForm()) {
            showAlert("Please select an event and fill in all fields correctly.");
            return;
        }

        try {
            selectedEvent.setNom(nomField.getText());
            selectedEvent.setDescription(descriptionField.getText());
            selectedEvent.setStarted_at(LocalDateTime.parse(startedAtField.getText(), dateTimeFormatter));
            selectedEvent.setFinish_at(LocalDateTime.parse(finishAtField.getText(), dateTimeFormatter));
            selectedEvent.setLieu(lieuField.getText());
            selectedEvent.setType(typeField.getText());
            selectedEvent.setCover(coverField.getText());
            selectedEvent.setLink(linkField.getText());
            selectedEvent.setAcces(accesField.getText());
            selectedEvent.setId_community_id(communityComboBox.getValue().getId());

            eventService.Update(selectedEvent);
            updateButton.setDisable(true);
            addButton.setDisable(false);
            System.out.println("Event updated: " + selectedEvent.getNom());

            initialize();
            clearForm();
        } catch (DateTimeParseException e) {
            showAlert("Invalid date format. Please use yyyy-MM-dd HH:mm.");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            showAlert("Please fill in all fields correctly.");
            return;
        }

        Community selectedCommunity = communityComboBox.getSelectionModel().getSelectedItem();

        if (selectedCommunity == null) {
            showAlert("Please select a community.");
            return;
        }

        try {
            Events newEvent = new Events(
                    selectedCommunity.getId(),
                    nomField.getText().trim(),
                    descriptionField.getText().trim(),
                    LocalDateTime.parse(startedAtField.getText(), dateTimeFormatter),
                    LocalDateTime.parse(finishAtField.getText(), dateTimeFormatter),
                    lieuField.getText().trim(),
                    typeField.getText().trim(),
                    coverField.getText().trim(),
                    linkField.getText().trim(),
                    accesField.getText().trim()
            );

            eventService.Add(newEvent);
            System.out.println("Event added: " + newEvent.getNom());

            initialize();
            clearForm();
        } catch (DateTimeParseException e) {
            showAlert("Invalid date format. Please use yyyy-MM-dd HH:mm.");
        }
    }

    private void clearForm() {
        nomField.clear();
        descriptionField.clear();
        startedAtField.clear();
        finishAtField.clear();
        lieuField.clear();
        typeField.clear();
        coverField.clear();
        linkField.clear();
        accesField.clear();
        communityComboBox.getSelectionModel().clearSelection();
        updateButton.setDisable(true);
        addButton.setDisable(false);
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

        try {
            LocalDateTime.parse(startedAtField.getText(), dateTimeFormatter);
            startedAtField.setStyle("");
        } catch (DateTimeParseException e) {
            startedAtField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        try {
            LocalDateTime.parse(finishAtField.getText(), dateTimeFormatter);
            finishAtField.setStyle("");
        } catch (DateTimeParseException e) {
            finishAtField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (lieuField.getText().trim().isEmpty()) {
            lieuField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            lieuField.setStyle("");
        }

        if (typeField.getText().trim().isEmpty()) {
            typeField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            typeField.setStyle("");
        }

        if (coverField.getText().trim().isEmpty()) {
            coverField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            coverField.setStyle("");
        }

        if (accesField.getText().trim().isEmpty()) {
            accesField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            accesField.setStyle("");
        }

        if (communityComboBox.getValue() == null) {
            communityComboBox.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            communityComboBox.setStyle("");
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

    private void showAlert2(Alert.AlertType alertType, String title, String content) {
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
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Ensure uploads directory exists
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdir();
                }

                // Create unique filename to avoid conflict
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String uniqueName = selectedFile.getName().replace(extension, "") + "-" + java.util.UUID.randomUUID() + extension;
                File destFile = new File(uploadsDir, uniqueName);

                // Copy file to uploads/
                java.nio.file.Files.copy(
                        selectedFile.toPath(),
                        destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                // Set the field with full /uploads/ path (as required in DB)
                coverField.setText("/uploads/" + uniqueName);
                showAlert2(Alert.AlertType.INFORMATION, "Success", "Image uploaded and path set.");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert2(Alert.AlertType.ERROR, "Error", "Failed to upload image: " + e.getMessage());
            }
        }
    }
}