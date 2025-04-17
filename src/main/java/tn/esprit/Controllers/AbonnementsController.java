package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.Models.Abonnements;
import tn.esprit.Models.Categories;
import tn.esprit.Services.AbonnementService;

import java.util.List;

public class AbonnementsController {

    @FXML
    private TableColumn<Abonnements, Void> actionCol;

    @FXML
    private Button addButton;

    @FXML
    private TableColumn<Abonnements, String> avantagesCol;

    @FXML
    private TextField avantagesField;

    @FXML
    private TableColumn<Abonnements, Integer> idCol;

    @FXML
    private TableColumn<Abonnements, String> nomCol;

    @FXML
    private TextField nomField;

    @FXML
    private TableColumn<Abonnements, Double> prixCol;

    @FXML
    private TextField prixField;

    @FXML
    private TableView<Abonnements> tableviewAbonnement;

    @FXML
    private TableColumn<Abonnements, String> typeCol;

    @FXML
    private TextField typeField;

    @FXML
    private Button updateButton;

    private final AbonnementService AbonnementService = new AbonnementService();
    private Abonnements selectedAbonnement;
    private static final double ROW_HEIGHT = 50.0;

    @FXML
    void initialize() {
        List<Abonnements> abonnements = AbonnementService.readAll();
        ObservableList<Abonnements> observableList = FXCollections.observableList(abonnements);
        tableviewAbonnement.setItems(observableList);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        avantagesCol.setCellValueFactory(new PropertyValueFactory<>("avantages"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        addActionButtonsToTable();

        tableviewAbonnement.setFixedCellSize(ROW_HEIGHT);
        tableviewAbonnement.prefHeightProperty().bind(
                tableviewAbonnement.fixedCellSizeProperty().multiply(
                        FXCollections.observableList(abonnements).size() + 1.01
                )
        );
        tableviewAbonnement.minHeightProperty().bind(tableviewAbonnement.prefHeightProperty());
        tableviewAbonnement.maxHeightProperty().bind(tableviewAbonnement.prefHeightProperty());
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
                    Abonnements selectedAbonnement = getTableView().getItems().get(getIndex());
                    System.out.println("Delete clicked for: " + selectedAbonnement.getNom());
                    AbonnementService.DeleteByID(selectedAbonnement.getId());
                    initialize();
                });

                updateButton.setOnAction(event -> {
                    Abonnements selectedAbonnement = getTableView().getItems().get(getIndex());
                    System.out.println("Update clicked for: " + selectedAbonnement.getNom());
                    handleUpdate(selectedAbonnement);
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

    private void handleUpdate(Abonnements abonnement) {
        selectedAbonnement = abonnement;
        nomField.setText(abonnement.getNom());
        prixField.setText(String.valueOf(abonnement.getPrix()));
        avantagesField.setText(abonnement.getAvantages());
        typeField.setText(abonnement.getType());

        updateButton.setDisable(false);
        addButton.setDisable(true);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        Abonnements newAbonnement = new Abonnements(
                nomField.getText(),
                Double.parseDouble(prixField.getText()),
                avantagesField.getText(),
                typeField.getText()
        );
        AbonnementService.Add(newAbonnement);
        System.out.println("Category added: " + newAbonnement.getNom());

        initialize();
        clearForm();
    }

    @FXML
    void handleUpdateAction(ActionEvent event) {
        if (selectedAbonnement != null) {
            selectedAbonnement.setNom(nomField.getText());
            selectedAbonnement.setPrix(Double.parseDouble(prixField.getText()));
            selectedAbonnement.setAvantages(avantagesField.getText());
            selectedAbonnement.setType(typeField.getText());

            AbonnementService.Update(selectedAbonnement);
            updateButton.setDisable(true);
            addButton.setDisable(false);
            System.out.println("Abonnement updated: " + selectedAbonnement.getNom());

            initialize();
            clearForm();
        }
    }

    private void clearForm() {
        nomField.clear();
        prixField.clear();
        avantagesField.clear();
        typeField.clear();
        updateButton.setDisable(true);
    }
}