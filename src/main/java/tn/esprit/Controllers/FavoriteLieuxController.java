package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Services.LieuCulturelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;

public class FavoriteLieuxController {

    @FXML
    private FlowPane lieuxContainer;
    @FXML
    private TextField searchField;

    private final LieuCulturelService lieuCulturelService;
    private ObservableList<LieuCulturel> allLieux;

    public FavoriteLieuxController() {
        this.lieuCulturelService = new LieuCulturelService();
    }

    @FXML
    public void initialize() {
        // Charger tous les lieux favoris
        allLieux = FXCollections.observableArrayList(lieuCulturelService.getFavoriteLieux());
        
        // Configurer la recherche dynamique
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLieux(newValue);
        });
        
        // Afficher tous les lieux initialement
        filterLieux("");
    }

    private void filterLieux(String searchText) {
        lieuxContainer.getChildren().clear();
        
        allLieux.stream()
            .filter(lieu -> matchesSearch(lieu, searchText))
            .forEach(lieu -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/LieuCard.fxml"));
                    Node lieuCard = loader.load();
                    LieuCardController controller = loader.getController();
                    controller.setLieuCulturel(lieu);
                    lieuxContainer.getChildren().add(lieuCard);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private boolean matchesSearch(LieuCulturel lieu, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }
        
        String lowerCaseFilter = searchText.toLowerCase();
        return lieu.getNom().toLowerCase().contains(lowerCaseFilter) ||
               lieu.getDescription().toLowerCase().contains(lowerCaseFilter);
    }

    public void refreshView() {
        allLieux = FXCollections.observableArrayList(lieuCulturelService.getFavoriteLieux());
        filterLieux(searchField.getText());
    }
} 