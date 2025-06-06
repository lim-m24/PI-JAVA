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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

public class AbonnementsController {

    @FXML
    private TableColumn<Abonnements, Void> actionCol;

    @FXML
    private Button addButton;
    @FXML
    private TextField dureeField;


    @FXML
    private TableColumn<Abonnements, String> avantagesCol;

    @FXML
    private TextField avantagesField;

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
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");
                updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 10 2 10; -fx-font-size: 12px;");

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
        if (!validateForm()) {
            showAlert("Please fill in all fields correctly.");
            return;
        }

        Abonnements newAbonnement = new Abonnements(
                nomField.getText(),
                Double.parseDouble(prixField.getText()),
                avantagesField.getText(),
                typeField.getText()
        );

        AbonnementService.Add(newAbonnement);
        System.out.println("Abonnement added: " + newAbonnement.getNom());

        // Get the newly added ID (assuming it's the last one in the list)
        int id = AbonnementService.readAll()
                .stream()
                .mapToInt(Abonnements::getId)
                .max()
                .orElse(-1); // fallback

        try {
            int days = Integer.parseInt(dureeField.getText());
            LocalDate expiryDate = LocalDate.now().plusDays(days);
            saveExpiryDate(id, expiryDate);
        } catch (NumberFormatException e) {
            showAlert("Durée must be a valid number.");
        }

        initialize();
        clearForm();
    }
    private void saveExpiryDate(int id, LocalDate expiryDate) {
        try {
            File file = new File("src/abonnement_expiries.properties");
            Properties props = new Properties();

            // Load existing if exists
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    props.load(in);
                }
            }

            props.setProperty(String.valueOf(id), expiryDate.toString());

            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, "Abonnement Expiry Dates");
            }

            System.out.println("Expiry saved: " + id + "=" + expiryDate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void handleUpdateAction(ActionEvent event) {
        if (selectedAbonnement == null || !validateForm()) {
            showAlert("Please select an abonnement and fill in all fields correctly.");
            return;
        }

        selectedAbonnement.setNom(nomField.getText());
        selectedAbonnement.setPrix(Double.parseDouble(prixField.getText()));
        selectedAbonnement.setAvantages(avantagesField.getText());
        selectedAbonnement.setType(typeField.getText());

        AbonnementService.Update(selectedAbonnement);
        System.out.println("Abonnement updated: " + selectedAbonnement.getNom());

        updateButton.setDisable(true);
        addButton.setDisable(false);
        initialize();
        clearForm();
    }


    private void clearForm() {
        nomField.clear();
        prixField.clear();
        avantagesField.clear();
        typeField.clear();
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

        try {
            Double.parseDouble(prixField.getText());
            prixField.setStyle("");
        } catch (NumberFormatException e) {
            prixField.setStyle("-fx-border-color: red;");
            isValid = false;
        }

        if (avantagesField.getText().trim().isEmpty()) {
            avantagesField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            avantagesField.setStyle("");
        }

        if (typeField.getText().trim().isEmpty()) {
            typeField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else {
            typeField.setStyle("");
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

}