package tn.esprit.Controllers;

import tn.esprit.Models.LieuCulturel;
import tn.esprit.Services.LieuCulturelService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class LieuxCulturelsListController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private TableView<LieuCulturel> lieuTable;
    @FXML private TableColumn<LieuCulturel, Long> idColumn;
    @FXML private TableColumn<LieuCulturel, String> nomColumn;
    @FXML private TableColumn<LieuCulturel, String> descriptionColumn;
    @FXML private TableColumn<LieuCulturel, String> link3dColumn;
    @FXML private TableColumn<LieuCulturel, Void> actionsColumn;
    
    private final LieuCulturelService lieuCulturelService = new LieuCulturelService();
    private Stage dialogStage;
    private Long villeId;
    private String villeName;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeColumns();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void loadLieuxCulturels(Long villeId, String villeName) {
        this.villeId = villeId;
        this.villeName = villeName;
        if (titleLabel != null) {
            titleLabel.setText("Lieux culturels de " + villeName);
        }
        refreshLieuList();
    }
    
    private void initializeColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        link3dColumn.setCellValueFactory(new PropertyValueFactory<>("link3d"));
        
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button viewBtn = new Button("Voir");
            private final Button favoriBtn = new Button("☆");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn, viewBtn, favoriBtn);
            
            {
                editBtn.getStyleClass().add("button-edit");
                deleteBtn.getStyleClass().add("button-delete");
                viewBtn.getStyleClass().add("button-view");
                favoriBtn.getStyleClass().add("button-favori");
                
                editBtn.setOnAction(event -> {
                    LieuCulturel lieu = getTableView().getItems().get(getIndex());
                    showEditDialog(lieu);
                });
                
                deleteBtn.setOnAction(event -> {
                    LieuCulturel lieu = getTableView().getItems().get(getIndex());
                    handleDelete(lieu);
                });
                
                viewBtn.setOnAction(event -> {
                    LieuCulturel lieu = getTableView().getItems().get(getIndex());
                    showLieuCard(lieu);
                });

                favoriBtn.setOnAction(event -> {
                    LieuCulturel lieu = getTableView().getItems().get(getIndex());
                    toggleFavori(lieu);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    LieuCulturel lieu = getTableView().getItems().get(getIndex());
                    favoriBtn.setText(lieuCulturelService.isFavori(lieu.getId()) ? "★" : "☆");
                }
                setGraphic(empty ? null : buttons);
            }
        });
    }
    
    private void refreshLieuList() {
        System.out.println("Début du rafraîchissement de la liste des lieux culturels");
        try {
            ObservableList<LieuCulturel> lieux = lieuCulturelService.getLieuxCulturelsByVille(villeId);
            if (lieux != null) {
                System.out.println("Nombre de lieux culturels chargés : " + lieux.size());
                lieuTable.setItems(lieux);
                lieuTable.refresh();
                System.out.println("Liste rafraîchie avec succès");
            } else {
                System.out.println("La liste des lieux culturels est null");
                showError("Erreur", "Impossible de charger la liste des lieux culturels");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du rafraîchissement de la liste : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de rafraîchir la liste des lieux culturels : " + e.getMessage());
        }
    }
    
    @FXML
    private void showAddDialog() {
        LieuCulturel newLieu = new LieuCulturel();
        newLieu.setVilleId(villeId);
        boolean okClicked = showLieuCulturelDialog(newLieu, "Ajouter un lieu culturel");
        
        if (okClicked) {
            try {
                lieuCulturelService.saveLieuCulturel(newLieu);
                refreshLieuList();
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Une erreur est survenue lors de l'ajout du lieu culturel : " + e.getMessage());
            }
        }
    }
    
    private void showEditDialog(LieuCulturel lieu) {
        System.out.println("Début de la modification du lieu culturel : " + lieu.getId());
        
        if (lieu == null) {
            showError("Erreur", "Le lieu culturel ne peut pas être null");
            return;
        }

        // Sauvegarder les valeurs actuelles pour les restaurer en cas d'erreur
        LieuCulturel backupLieu = new LieuCulturel(
            lieu.getId(),
            lieu.getVilleId(),
            lieu.getNom(),
            lieu.getDescription(),
            lieu.getLink3d(),
            lieu.getCover()
        );

        System.out.println("Valeurs avant modification :");
        System.out.println("Nom : " + backupLieu.getNom());
        System.out.println("Description : " + backupLieu.getDescription());
        System.out.println("Link3D : " + backupLieu.getLink3d());
        System.out.println("Cover : " + backupLieu.getCover());

        boolean okClicked = showLieuCulturelDialog(lieu, "Modifier un lieu culturel");
        
        if (okClicked) {
            try {
                System.out.println("Sauvegarde des modifications...");
                
                // Sauvegarder les modifications
                lieuCulturelService.updateLieuCulturel(lieu);
                
                System.out.println("Rafraîchissement de la liste...");
                // Rafraîchir la liste
                refreshLieuList();
                
                System.out.println("Affichage du message de succès...");
                // Afficher un message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Le lieu culturel a été modifié avec succès.");
                successAlert.showAndWait();
                
                System.out.println("Modification terminée avec succès");
            } catch (Exception e) {
                System.out.println("Erreur lors de la modification : " + e.getMessage());
                e.printStackTrace();
                
                // En cas d'erreur, restaurer les valeurs originales
                lieu.setNom(backupLieu.getNom());
                lieu.setDescription(backupLieu.getDescription());
                lieu.setLink3d(backupLieu.getLink3d());
                lieu.setCover(backupLieu.getCover());
                
                showError("Erreur", "Une erreur est survenue lors de la modification : " + e.getMessage());
            }
        }
    }
    
    private boolean showLieuCulturelDialog(LieuCulturel lieu, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/gestion/view/LieuCulturelDialog.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(this.dialogStage);
            dialogStage.setScene(scene);
            
            LieuCulturelDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setVilleId(villeId);
            controller.setLieuCulturel(lieu);
            
            dialogStage.showAndWait();
            
            return controller.isOkClicked();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage du dialogue.");
            return false;
        }
    }
    
    private void handleDelete(LieuCulturel lieu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le lieu culturel");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce lieu culturel ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (lieu.getCover() != null && !lieu.getCover().isEmpty()) {
                File imageFile = new File(lieu.getCover());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }
            
            lieuCulturelService.deleteLieuCulturel(lieu.getId());
            refreshLieuList();
        }
    }
    
    private void showLieuCard(LieuCulturel lieu) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/gestion/view/MediaView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage mediaStage = new Stage();
            mediaStage.setTitle("Médias de " + lieu.getNom());
            mediaStage.initModality(Modality.WINDOW_MODAL);
            mediaStage.initOwner(dialogStage);
            mediaStage.setScene(scene);
            
            MediaViewController controller = loader.getController();
            controller.setDialogStage(mediaStage);
            controller.initialize(lieu.getId(), lieu.getNom());
            
            mediaStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage des médias.");
        }
    }
    
    private void toggleFavori(LieuCulturel lieu) {
        lieuCulturelService.toggleFavori(lieu.getId());
        refreshLieuList();
    }
    
    @FXML
    private void showFavoriteLieux() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/view/FavoriteLieuxView.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage stage = new Stage();
            stage.setTitle("Lieux Culturels Favoris");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dialogStage);
            stage.setScene(scene);
            
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de l'affichage des favoris.");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 