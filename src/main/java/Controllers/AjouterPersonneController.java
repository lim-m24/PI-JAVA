package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import models.Personne;
import services.PersonneService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterPersonneController {

    @FXML
    private TextField txtAge;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    void Ajouter(ActionEvent event) {
        PersonneService sc = new PersonneService();
        Personne p = new Personne(Integer.parseInt(txtAge.getText()),txtNom.getText(),txtPrenom.getText());
        try {
            sc.ajouter(p);
        } catch (SQLException e) {
            System.out.println(e.getMessage())  ;
        }

    }
    @FXML
    void Afficher(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPersonne.fxml"));
            Parent root = loader.load();
            txtPrenom.getScene().setRoot(root);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }

}