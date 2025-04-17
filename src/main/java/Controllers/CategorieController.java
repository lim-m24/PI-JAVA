package Controllers;

import models.Categories;
import utils.MyDabase;
import services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieController {
    @FXML
    private TableView<Categories> tableview;
    @FXML
    private TableColumn<Categories, Integer> idCol;
    @FXML
    private TableColumn<Categories, String> nomCol;
    @FXML
    private TableColumn<Categories, String> descriptionCol;
    @FXML
    private TableColumn<Categories, String> coverCol;
    @FXML
    private TableColumn<Categories, String> dateCol;

    private final CategorieService service = new CategorieService();

    @FXML
    void initialize() throws SQLException{
        ObservableList<Categories> obsList = FXCollections.observableArrayList(service.readAll());
        tableview.setItems(obsList);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        coverCol.setCellValueFactory(new PropertyValueFactory<>("cover"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date_creation"));
    }

    private void refreshTable() {
        ObservableList<Categories> obsList = FXCollections.observableArrayList(service.readAll());
        tableview.setItems(obsList);
    }


}
