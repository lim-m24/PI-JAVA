package tn.esprit.Controllers;

import tn.esprit.Models.Ville;
import tn.esprit.Services.VilleService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class FavoriteVillesController implements Initializable {
    
    @FXML
    private ListView<Ville> favoriteVillesListView;
    
    private final VilleService villeService = new VilleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupListView();
        loadFavoriteVilles();
    }

    private void setupListView() {
        favoriteVillesListView.setCellFactory(param -> new ListCell<Ville>() {
            @Override
            protected void updateItem(Ville ville, boolean empty) {
                super.updateItem(ville, empty);
                
                if (empty || ville == null) {
                    setText(null);
                } else {
                    setText(ville.getNom());
                }
            }
        });
    }

    public void loadFavoriteVilles() {
        try {
            favoriteVillesListView.getItems().clear();
            favoriteVillesListView.getItems().addAll(villeService.getFavoriteVilles());
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: Add proper error handling
        }
    }

    public void refreshView() {
        loadFavoriteVilles();
    }
} 