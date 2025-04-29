package tn.esprit.Controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.Models.Abonnements;
import tn.esprit.Models.Gamification;
import tn.esprit.Services.AbonnementService;
import tn.esprit.Services.GamificationService;

import java.util.List;

public class GamificationController {

    @FXML private ComboBox<Abonnements> abonnementCombo;
    @FXML private TextField nomField, descriptionField, typeField, conditionField;
    @FXML private Button addButton, updateButton;
    @FXML private TableView<GamificationWrapper> tableviewGamification; // Use GamificationWrapper here
    @FXML private TableColumn<GamificationWrapper, String> abonnementCol; // Column now holds String (Abonnement Name)
    @FXML private TableColumn<GamificationWrapper, String> nomCol, descriptionCol, typeCol;
    @FXML private TableColumn<GamificationWrapper, Integer> conditionCol;
    @FXML private TableColumn<GamificationWrapper, Void> actionCol;

    private final GamificationService gamificationService = new GamificationService();
    private final AbonnementService abonnementService = new AbonnementService();
    private Gamification selectedGamification;

    @FXML
    public void initialize() {
        loadComboAbonnements();
        loadGamificationsWithAbonnementName();

        abonnementCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAbonnementName()));
        nomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGamification().getNom()));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGamification().getDescription()));
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGamification().getType()));
        conditionCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getGamification().getConditionGamification()).asObject());

        tableviewGamification.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedGamification = newSelection.getGamification();
                setFieldsFromSelection(selectedGamification);
                updateButton.setDisable(false);
            }
        });
        addActionButtonsToTable();
    }

    private void loadComboAbonnements() {
        List<Abonnements> abonnements = abonnementService.readAll();
        abonnementCombo.setItems(FXCollections.observableArrayList(abonnements));
    }

    private void loadGamificationsWithAbonnementName() {
        List<Gamification> gamifications = gamificationService.readAll();
        ObservableList<GamificationWrapper> wrapperList = FXCollections.observableArrayList();
        for (Gamification g : gamifications) {
            Abonnements abonnement = abonnementService.readAll().stream()
                    .filter(a -> a.getId() == g.getTypeAbonnement())
                    .findFirst()
                    .orElse(null);
            wrapperList.add(new GamificationWrapper(g, abonnement != null ? abonnement.getNom() : "N/A"));
        }
        tableviewGamification.setItems(wrapperList); // Now setting the correct type
    }

    private void setFieldsFromSelection(Gamification g) {
        nomField.setText(g.getNom());
        descriptionField.setText(g.getDescription());
        typeField.setText(g.getType());
        conditionField.setText(String.valueOf(g.getConditionGamification()));
        abonnementCombo.getSelectionModel().select(
                abonnementCombo.getItems().stream().filter(a -> a.getId() == g.getTypeAbonnement()).findFirst().orElse(null)
        );
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            showAlert("Please fill in all fields correctly.");
            return;
        }

        Abonnements selectedAbonnement = abonnementCombo.getSelectionModel().getSelectedItem();
        if (selectedAbonnement == null) {
            showAlert("Please select an abonnement.");
            return;
        }

        Gamification g = new Gamification(
                selectedAbonnement.getId(),
                nomField.getText(),
                descriptionField.getText(),
                typeField.getText(),
                Integer.parseInt(conditionField.getText())
        );

        gamificationService.Add(g);
        loadGamificationsWithAbonnementName(); // Reload data to update the table
        clearFields();
    }


    @FXML
    private void handleUpdateAction() {
        if (selectedGamification == null) {
            showAlert("Please select a gamification to update.");
            return;
        }

        if (!validateForm()) {
            showAlert("Please fill in all fields correctly.");
            return;
        }

        Abonnements selectedAbonnement = abonnementCombo.getSelectionModel().getSelectedItem();
        if (selectedAbonnement == null) {
            showAlert("Please select an abonnement.");
            return;
        }

        selectedGamification.setTypeAbonnement(selectedAbonnement.getId());
        selectedGamification.setNom(nomField.getText());
        selectedGamification.setDescription(descriptionField.getText());
        selectedGamification.setType(typeField.getText());
        selectedGamification.setConditionGamification(Integer.parseInt(conditionField.getText()));

        gamificationService.Update(selectedGamification);
        loadGamificationsWithAbonnementName(); // Reload data to update the table
        clearFields();
    }


    private void clearFields() {
        abonnementCombo.getSelectionModel().clearSelection();
        nomField.clear();
        descriptionField.clear();
        typeField.clear();
        conditionField.clear();
        updateButton.setDisable(true);
        selectedGamification = null;
    }
    private void handleUpdate(Gamification gamification) {
        selectedGamification = gamification;

        Abonnements abonnement = abonnementCombo.getItems()
                .stream()
                .filter(a -> a.getId() == gamification.getTypeAbonnement())
                .findFirst()
                .orElse(null);
        abonnementCombo.getSelectionModel().select(abonnement);

        nomField.setText(gamification.getNom());
        descriptionField.setText(gamification.getDescription());
        typeField.setText(gamification.getType());
        conditionField.setText(String.valueOf(gamification.getConditionGamification()));

        updateButton.setDisable(false);
        addButton.setDisable(true);
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

        if (typeField.getText().trim().isEmpty()) {
            typeField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            typeField.setStyle("");
        }

        try {
            Integer.parseInt(conditionField.getText());
            conditionField.setStyle("");
        } catch (NumberFormatException e) {
            conditionField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (abonnementCombo.getValue() == null) {
            abonnementCombo.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            abonnementCombo.setStyle("");
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

    // Helper class to wrap Gamification and Abonnement Name
    public static class GamificationWrapper {
        private final Gamification gamification;
        private final String abonnementName;

        public GamificationWrapper(Gamification gamification, String abonnementName) {
            this.gamification = gamification;
            this.abonnementName = abonnementName;
        }

        public Gamification getGamification() {
            return gamification;
        }

        public String getAbonnementName() {
            return abonnementName;
        }
    }

    private void addActionButtonsToTable() {
        actionCol.setCellFactory(param -> new TableCell<GamificationWrapper, Void>() { // Use GamificationWrapper here
            private final Button deleteButton = new Button("Delete");
            private final Button updateButton = new Button("Update");
            private final HBox hBox = new HBox(10, updateButton, deleteButton);

            {
                hBox.setPadding(new Insets(5));
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");
                updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");

                deleteButton.setOnAction(event -> {
                    GamificationWrapper selectedWrapper = getTableView().getItems().get(getIndex());
                    Gamification selectedGamification = selectedWrapper.getGamification();
                    System.out.println("Delete clicked for: " + selectedGamification.getNom());
                    gamificationService.DeleteByID(selectedGamification.getId());
                    loadGamificationsWithAbonnementName();
                });

                updateButton.setOnAction(event -> {
                    GamificationWrapper selectedWrapper = getTableView().getItems().get(getIndex());
                    handleUpdate(selectedWrapper.getGamification());
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
}