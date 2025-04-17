package Controllers;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Personne;
import services.PersonneService;

import java.sql.SQLException;

public class AfficherPersonneController {

    @FXML
    private TableColumn<Personne, Integer> agoCol;

    @FXML
    private TableColumn<Personne, String> nomCol;

    @FXML
    private TableColumn<Personne, String> prenomCol;

    @FXML
    private TableView<Personne> tableview;

    @FXML
    void initialize() throws SQLException {
        PersonneService Service = new PersonneService();
        ObservableList<Personne> obs = FXCollections.observableList(Service.recuperer());
        tableview.setItems(obs);
        agoCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
    }

}
