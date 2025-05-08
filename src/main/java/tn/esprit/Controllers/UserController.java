package tn.esprit.Controllers;

import tn.esprit.Models.User;
import tn.esprit.Models.UserActivity;
import tn.esprit.Services.AuthService;
import tn.esprit.Services.UserActivityService;
import tn.esprit.Services.UserService;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.SessionTimeoutTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private StackPane contentPane;

    private final AuthService authService = new AuthService();

    // Champ pour le filtre de recherche
    @FXML private TextField searchField;

    // Champs pour les filtres
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label userCountLabel;

    // Champs pour la pagination
    @FXML private Pagination userPagination;
    @FXML private ComboBox<Integer> pageSizeComboBox;

    // Champs pour les actions en masse
    @FXML private ComboBox<String> bulkActionComboBox;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private Label selectedCountLabel;

    private final UserService userService = new UserService();
    private final UserActivityService userActivityService = new UserActivityService(); // Add this field
    private final ObservableList<User> users = FXCollections.observableArrayList();

    // Variables pour la pagination
    private int itemsPerPage = 10;
    private List<User> allUsers = new ArrayList<>();
    private SessionTimeoutTask sessionTimeoutTask;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        setupPagination();
        setupBulkActions();
        loadUsers();

    }

    private void setupTableColumns() {
        // Colonne Status
        statusColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new SimpleStringProperty(user.isBanned() ? "Banni" : "Actif");
        });

        // Colonne Actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button banBtn = new Button();

            {
                // Style de base
                editBtn.getStyleClass().add("button");
                deleteBtn.getStyleClass().add("button");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                banBtn.getStyleClass().add("button");

                // Actions
                editBtn.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
                banBtn.setOnAction(event -> handleToggleBan(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.isBanned()) {
                        banBtn.setText("Unban");
                        banBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                    } else {
                        banBtn.setText("Ban");
                        banBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                    }

                    HBox buttons = new HBox(5, editBtn, deleteBtn, banBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        // Configuration des filtres de rôle
        if (roleFilter != null) {
            roleFilter.getItems().addAll("All", "ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR");
            roleFilter.setValue("All");
            roleFilter.setOnAction(e -> applyFilters());
        }

        // Configuration des filtres de statut
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All", "Active", "Banned");
            statusFilter.setValue("All");
            statusFilter.setOnAction(e -> applyFilters());
        }

        // Configuration de la recherche
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void setupPagination() {
        // Configuration des options de taille de page
        if (pageSizeComboBox != null) {
            pageSizeComboBox.getItems().addAll(10, 25, 50, 100);
            pageSizeComboBox.setValue(10);
            pageSizeComboBox.setOnAction(e -> {
                itemsPerPage = pageSizeComboBox.getValue();
                updatePagination();
            });
        }

        // Configuration de la pagination
        if (userPagination != null) {
            userPagination.setPageFactory(this::createPage);
        }
    }

    private void setupBulkActions() {
        // Configuration des actions en masse disponibles
        if (bulkActionComboBox != null) {
            bulkActionComboBox.getItems().addAll("Ban Selected", "Unban Selected", "Delete Selected", "Change Role");
        }

        // Initialisation du compteur de sélection
        if (selectedCountLabel != null) {
            selectedCountLabel.setText("0 selected");
        }
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, users.size());

        ObservableList<User> pageItems = FXCollections.observableArrayList(
                users.subList(fromIndex, toIndex));
        usersTable.setItems(pageItems);

        return usersTable;
    }

    private void updatePagination() {
        if (userPagination != null) {
            int pageCount = (int) Math.ceil((double) users.size() / itemsPerPage);
            userPagination.setPageCount(pageCount);
            userPagination.setCurrentPageIndex(0);
            createPage(0);
        } else {
            usersTable.setItems(users);
        }
    }

    private void loadUsers() {
        allUsers = userService.getAllUsers();
        applyFilters();
    }

    private void applyFilters() {
        List<User> filteredUsers;

        // Si les filtres sont configurés, les appliquer
        if (searchField != null && roleFilter != null && statusFilter != null) {
            // Récupération des valeurs de filtre
            String searchText = searchField.getText().toLowerCase();
            String role = roleFilter.getValue();
            String status = statusFilter.getValue();

            // Filtrage des utilisateurs
            filteredUsers = allUsers.stream()
                    .filter(user -> {
                        // Filtre de recherche
                        boolean matchesSearch = searchText.isEmpty() ||
                                user.getName().toLowerCase().contains(searchText) ||
                                user.getEmail().toLowerCase().contains(searchText);

                        // Filtre de rôle
                        boolean matchesRole = "All".equals(role) ||
                                user.getRole().equals(role);

                        // Filtre de statut
                        boolean matchesStatus = "All".equals(status) ||
                                ("Active".equals(status) && !user.isBanned()) ||
                                ("Banned".equals(status) && user.isBanned());

                        return matchesSearch && matchesRole && matchesStatus;
                    })
                    .collect(Collectors.toList());

            // Mise à jour du compteur
            if (userCountLabel != null) {
                userCountLabel.setText("Total: " + filteredUsers.size() + " users");
            }
        } else {
            // Si les filtres ne sont pas configurés, afficher tous les utilisateurs
            filteredUsers = allUsers;
        }

        // Mise à jour de la table
        users.setAll(filteredUsers);

        // Mise à jour de la pagination
        updatePagination();
    }

    @FXML
    public void handleSearchUsers() {
        applyFilters();
    }

    @FXML
    public void handleFilterChange() {
        applyFilters();
    }

    @FXML
    public void handleResetFilters() {
        if (searchField != null) searchField.clear();
        if (roleFilter != null) roleFilter.setValue("All");
        if (statusFilter != null) statusFilter.setValue("All");
        applyFilters();
    }

    private void loadForm(User user) {
        try {
            // Load the FXML
            URL formUrl = getClass().getResource("/views/user/form.fxml");
            if (formUrl == null) {
                showAlert("Error", "Could not find form.fxml file", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(formUrl);
            Parent formView = loader.load();

            // Get the controller after loading
            UserFormController formController = loader.getController();
            formController.setParentController(this);
            formController.setUser(user);

            // Apply CSS and show
            Scene scene = new Scene(formView);
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(user == null ? "Add New User" : "Edit User");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/details.fxml"));
            loader.setController(new UserDetailsController(this, user));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditUser(User user) {
        loadForm(user);
    }

    @FXML
    public void handleAddUser() {
        loadForm(null);
    }

    @FXML
    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userService.deleteUser(user.getId())) {
                    users.remove(user);
                    allUsers.remove(user);
                    showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
                }
            }
        });
    }

    public boolean deleteUser(User user) {
        boolean success = userService.deleteUser(user.getId());
        if (success) {
            allUsers.remove(user);
            applyFilters();
        }
        return success;
    }

    public void refreshUsers() {
        loadUsers();
    }

    public void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleExportUsers() {
        try {
            // Create a file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Users");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            // Show save file dialog
            File file = fileChooser.showSaveDialog(usersTable.getScene().getWindow());

            if (file != null) {
                // Write to CSV
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.write("ID,Email,Name,Role,Status\n");

                    // Write data
                    for (User user : users) {
                        String status = user.isBanned() ? "Banned" : "Active";
                        writer.write(String.format("%d,%s,%s,%s,%s\n",
                                user.getId(),
                                escapeCSV(user.getEmail()),
                                escapeCSV(user.getName()),
                                escapeCSV(user.getRole()),
                                status));
                    }

                    showAlert("Success", "Users exported successfully to " + file.getName(),
                            Alert.AlertType.INFORMATION);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to export users: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Helper method to properly escape CSV values
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @FXML
    private void handleToggleBan(User user) {
        boolean newBanStatus = !user.isBanned();
        user.setBanned(newBanStatus);

        if (userService.updateUser(user)) {
            // Send email only if the user is being banned
            if (newBanStatus) {
                authService.sendBanNotificationEmail(user.getEmail());
            }

            // Solution optimale: rafraîchir seulement la ligne modifiée
            int index = usersTable.getItems().indexOf(user);
            if (index >= 0) {
                usersTable.getItems().set(index, user); // Force la mise à jour
                usersTable.refresh(); // Rafraîchit la table
            }

            String message = newBanStatus
                    ? "L'utilisateur a été banni avec succès."
                    : "L'utilisateur a été débanni avec succès.";
            showAlert("Succès", message, Alert.AlertType.INFORMATION);
        } else {
            showAlert("Erreur", "Échec de la mise à jour du statut de l'utilisateur.", Alert.AlertType.ERROR);
            user.setBanned(!newBanStatus); // Annule le changement
        }
    }

    @FXML
    public void handleExportToPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Users to PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(usersTable.getScene().getWindow());

            if (file != null) {
                // Ici, vous devrez implémenter l'export PDF
                // Par exemple avec la bibliothèque iText ou Apache PDFBox

                showAlert("Information", "Cette fonctionnalité nécessite une bibliothèque PDF. " +
                                "Veuillez implémenter l'export PDF avec iText ou Apache PDFBox.",
                        Alert.AlertType.INFORMATION);

                // Exemple de code à implémenter:
                // exportUsersToPdf(users, file);
            }
        } catch (Exception e) {
            showAlert("Error", "Échec de l'export PDF: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSelectAll() {
        boolean selectAll = selectAllCheckBox.isSelected();

        // Sélectionner ou désélectionner tous les utilisateurs visibles
        for (User user : usersTable.getItems()) {
            // Si vous ajoutez une propriété selected à votre classe User
            // user.setSelected(selectAll);
        }

        // Mettre à jour le compteur de sélection
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        // Compter les utilisateurs sélectionnés
        long selectedCount = usersTable.getItems().stream()
                // .filter(User::isSelected) // Si vous ajoutez cette propriété
                .count();

        if (selectedCountLabel != null) {
            selectedCountLabel.setText(selectedCount + " selected");
        }
    }

    @FXML
    public void handleBulkAction() {
        if (bulkActionComboBox == null) return;

        String selectedAction = bulkActionComboBox.getValue();
        if (selectedAction == null) {
            showAlert("Warning", "Please select an action", Alert.AlertType.WARNING);
            return;
        }

        // Récupérer les utilisateurs sélectionnés
        List<User> selectedUsers = usersTable.getSelectionModel().getSelectedItems(); // Use TableView's selection model

        if (selectedUsers.isEmpty()) {
            showAlert("Warning", "No users selected", Alert.AlertType.WARNING);
            return;
        }

        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText("Apply " + selectedAction);
        alert.setContentText("Are you sure you want to " + selectedAction.toLowerCase() +
                " for " + selectedUsers.size() + " selected users?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Appliquer l'action selon le choix
                switch (selectedAction) {
                    case "Ban Selected":
                        selectedUsers.forEach(user -> {
                            user.setBanned(true);
                            userService.updateUser(user);
                            authService.sendBanNotificationEmail(user.getEmail()); // Send email for each banned user
                        });
                        break;
                    case "Unban Selected":
                        selectedUsers.forEach(user -> {
                            user.setBanned(false);
                            userService.updateUser(user);
                        });
                        break;
                    case "Delete Selected":
                        selectedUsers.forEach(user -> userService.deleteUser(user.getId()));
                        break;
                    case "Change Role":
                        showRoleSelectionDialog(selectedUsers);
                        break;
                }

                // Rafraîchir la liste
                loadUsers();
                showAlert("Success", "Bulk action applied successfully", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showRoleSelectionDialog(List<User> users) {
        // Création d'une boîte de dialogue pour choisir le rôle
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Role");
        dialog.setHeaderText("Select new role for selected users");

        // Boutons
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Contenu de la boîte de dialogue
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ROLE_ADMIN", "ROLE_USER", "ROLE_MODERATOR");
        roleComboBox.setValue("ROLE_USER");

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("New role:"), roleComboBox);
        dialog.getDialogPane().setContent(content);

        // Convertisseur de résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return roleComboBox.getValue();
            }
            return null;
        });

        // Affichage et traitement du résultat
        dialog.showAndWait().ifPresent(role -> {
            users.forEach(user -> {
                user.setRole(role);
                userService.updateUser(user);
            });
        });
    }

    // Ajoutez cette méthode à UserController
    @FXML
    public void showUserStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/statistics.fxml"));
            Parent statsView = loader.load();

            // Passer les données statistiques
            UserStatsController controller = loader.getController();
            controller.setStats(calculateUserStats());
            controller.setRecentActivities(getRecentActivities()); // Implémentez cette méthode

            Stage stage = new Stage();
            stage.setScene(new Scene(statsView));
            stage.setTitle("User Statistics Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load statistics view: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private Map<String, Integer> calculateUserStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", (int) allUsers.stream().filter(u -> !u.isBanned()).count());
        stats.put("bannedUsers", (int) allUsers.stream().filter(User::isBanned).count());
        stats.put("adminUsers", (int) allUsers.stream().filter(u -> u.getRole().equals("ROLE_ADMIN")).count());
        return stats;
    }

    private List<UserActivity> getRecentActivities() {
        // Fetch the most recent activities from the database (e.g., last 10 activities)
        return userActivityService.getRecentActivities(10);
    }

    // Ajouter des méthodes pour gérer les sessions utilisateur
    public void startUserSession(User user) {
        SessionManager.getInstance().setCurrentUser(user);
        SessionManager.getInstance().setAttribute("userRole", user.getRole());
        SessionManager.getInstance().setAttribute("lastActivity", LocalDateTime.now());
    }

    public void endUserSession() {
        SessionManager.getInstance().logout();
    }

    public boolean isUserSessionActive() {
        return SessionManager.getInstance().isLoggedIn();
    }

    public void updateSessionActivity() {
        SessionManager.getInstance().setAttribute("lastActivity", LocalDateTime.now());
    }
}