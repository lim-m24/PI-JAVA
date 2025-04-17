package tn.esprit.Controllers;

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
    @FXML private TableView<Gamification> tableviewGamification;
    @FXML private TableColumn<Gamification, Integer> idCol;
    @FXML private TableColumn<Gamification, Integer> abonnementCol;
    @FXML private TableColumn<Gamification, String> nomCol, descriptionCol, typeCol;
    @FXML private TableColumn<Gamification, Integer> conditionCol;
    @FXML private TableColumn<Gamification, Void> actionCol;

    private final GamificationService gamificationService = new GamificationService();
    private final AbonnementService abonnementService = new AbonnementService();
    private Gamification selectedGamification;

    @FXML
    public void initialize() {
        loadComboAbonnements();
        loadGamifications();

        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        abonnementCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTypeAbonnement()).asObject());
        nomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        descriptionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        typeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        conditionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getConditionGamification()).asObject());

        tableviewGamification.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedGamification = newSelection;
                setFieldsFromSelection(newSelection);
                updateButton.setDisable(false);
            }
        });
        addActionButtonsToTable();
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
                    Gamification selectedGamification = getTableView().getItems().get(getIndex());
                    System.out.println("Delete clicked for: " + selectedGamification.getNom());
                    gamificationService.DeleteByID(selectedGamification.getId());
                    initialize();
                });

                updateButton.setOnAction(event -> {
                    Gamification selectedGamification = getTableView().getItems().get(getIndex());
                    System.out.println("Update clicked for: " + selectedGamification.getNom());
                    handleUpdate(selectedGamification);
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
    private void loadComboAbonnements() {
        List<Abonnements> abonnements = abonnementService.readAll();
        abonnementCombo.setItems(FXCollections.observableArrayList(abonnements));
    }

    private void loadGamifications() {
        ObservableList<Gamification> list = FXCollections.observableArrayList(gamificationService.readAll());
        tableviewGamification.setItems(list);
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
        Abonnements selectedAbonnement = abonnementCombo.getSelectionModel().getSelectedItem();
        if (selectedAbonnement == null) return;

        Gamification g = new Gamification(
                selectedAbonnement.getId(),
                nomField.getText(),
                descriptionField.getText(),
                typeField.getText(),
                Integer.parseInt(conditionField.getText())
        );

        gamificationService.Add(g);
        loadGamifications();
        clearFields();
    }

    @FXML
    private void handleUpdateAction() {
        if (selectedGamification == null) return;
        Abonnements selectedAbonnement = abonnementCombo.getSelectionModel().getSelectedItem();
        if (selectedAbonnement == null) return;

        selectedGamification.setTypeAbonnement(selectedAbonnement.getId());
        selectedGamification.setNom(nomField.getText());
        selectedGamification.setDescription(descriptionField.getText());
        selectedGamification.setType(typeField.getText());
        selectedGamification.setConditionGamification(Integer.parseInt(conditionField.getText()));

        gamificationService.Update(selectedGamification);
        loadGamifications();
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

}
