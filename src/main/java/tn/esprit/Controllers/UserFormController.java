package tn.esprit.Controllers;

import tn.esprit.Models.Categories;
import tn.esprit.Models.User;
import tn.esprit.Services.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

public class UserFormController extends UserController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField firstnameField;
    @FXML private TextField usernameField;
    @FXML private DatePicker dateOBPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ListView<Categories> interestsListView;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;



    // Labels d'erreur pour chaque champ
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label nameErrorLabel;
    @FXML private Label firstnameErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label dateOBErrorLabel;
    @FXML private Label genderErrorLabel;
    @FXML private Label roleErrorLabel;
    @FXML private Label interestsErrorLabel;
    @FXML private ToggleButton showPasswordToggle;
    @FXML private TextField visiblePasswordField; // Déjà existant, modifiez si besoin
    @FXML private Button showPasswordButton;
    @FXML private Label passwordVisibilityIcon;


    private final UserService userService = new UserService();
    private User user;
    private UserController parentController;
    private ObservableList<Categories> allCategories = FXCollections.observableArrayList();

    // Regex pour validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].{8,}$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_INTERESTS = 2;
    private static final int MAX_INTERESTS = 4;
    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 100;

    public UserFormController() {
        // Constructeur par défaut nécessaire pour l'initialisation par JavaFX
    }

    public UserFormController(UserController parentController) {
        this.parentController = parentController;
    }

    public UserFormController(UserController parentController, User user) {
        this.parentController = parentController;
        this.user = user;
    }

    public void setParentController(UserController parentController) {
        this.parentController = parentController;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            populateForm();
        }
    }

    @FXML
    public void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/form.fxml"));
            // Créez le contrôleur avec les paramètres nécessaires
            UserFormController formController = new UserFormController(this, user);
            loader.setController(formController);

            Parent formView = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(formView));
            stage.setTitle(user == null ? "Add New User" : "Edit User");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load edit form:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void populateForm() {
        if (emailField != null) emailField.setText(user.getEmail());
        if (nameField != null) nameField.setText(user.getName());
        if (firstnameField != null) firstnameField.setText(user.getFirstname());
        if (usernameField != null) usernameField.setText(user.getUsername());
        if (dateOBPicker != null) dateOBPicker.setValue(user.getDateOB());
        if (genderComboBox != null) genderComboBox.setValue(user.getGender());
        if (roleComboBox != null) roleComboBox.setValue(user.getRole());

        // Ensure categories are loaded before selecting user's interests
        loadCategories();

        // Select user's interests if both the user interests and list view are available
        if (user.getInterests() != null && interestsListView != null) {
            for (Categories interest : user.getInterests()) {
                for (Categories category : allCategories) {
                    if (category.getId() == interest.getId()) {
                        interestsListView.getSelectionModel().select(category);
                    }
                }
            }
        }

        // Clear any previous error messages
        clearErrorMessages();
    }

    @FXML
    public void handleSave() {
        System.out.println("handleSave called");
        // Réinitialiser les messages d'erreur
        clearErrorMessages();

        boolean isValid = validateForm();

        if (!isValid) {
            // Ne pas poursuivre si le formulaire n'est pas valide
            return;
        }

        try {
            // Initialize user if null (new user)
            if (user == null) {
                user = new User();
                user.setId(0); // ID sera généré par la base de données
                user.setRole(roleComboBox.getValue() != null ? roleComboBox.getValue() : "ROLE_USER"); // Valeur par défaut
            }

            // Récupération des valeurs du formulaire
            user.setEmail(emailField.getText().trim());
            user.setName(nameField.getText().trim());
            user.setFirstname(firstnameField.getText().trim());
            user.setUsername(usernameField.getText().trim());

            // Gérer le mot de passe uniquement s'il a été modifié ou s'il s'agit d'un nouvel utilisateur
            if (!passwordField.getText().isEmpty()) {
                user.setPassword(passwordField.getText());
            } else if (user.getId() == 0) { // Nouveau utilisateur sans mot de passe
                showError(passwordErrorLabel, "Password is required for new users");
                return;
            }

            // Définir le rôle
            user.setRole(roleComboBox.getValue());

            // Définir la date de naissance
            user.setDateOB(dateOBPicker.getValue());

            // Définir le genre
            user.setGender(genderComboBox.getValue());

            // Récupérer les intérêts sélectionnés
            List<Categories> selectedInterests = interestsListView.getSelectionModel().getSelectedItems();
            user.setInterests(selectedInterests);

            // Enregistrement - déterminer si on crée ou met à jour
            boolean success;
            if (user.getId() == 0) { // Nouveau utilisateur
                success = userService.addUser(user);
            } else { // Mise à jour d'un utilisateur existant
                success = userService.updateUser(user);
            }

            // Fermer la fenêtre et rafraîchir les utilisateurs en cas de succès
            if (success) {
                if (parentController != null) {
                    parentController.refreshUsers();
                }
                // Fermer la fenêtre
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Erreur", "Échec de l'enregistrement", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation du nom
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError(nameErrorLabel, "Last name is required");
            isValid = false;
        } else if (nameField.getText().trim().length() < MIN_NAME_LENGTH ||
                nameField.getText().trim().length() > MAX_NAME_LENGTH) {
            showError(nameErrorLabel, "Last name must be between " + MIN_NAME_LENGTH +
                    " and " + MAX_NAME_LENGTH + " characters");
            isValid = false;
        }

        // Validation du prénom
        if (firstnameField.getText() == null || firstnameField.getText().trim().isEmpty()) {
            showError(firstnameErrorLabel, "First name is required");
            isValid = false;
        } else if (firstnameField.getText().trim().length() < MIN_NAME_LENGTH ||
                firstnameField.getText().trim().length() > MAX_NAME_LENGTH) {
            showError(firstnameErrorLabel, "First name must be between " + MIN_NAME_LENGTH +
                    " and " + MAX_NAME_LENGTH + " characters");
            isValid = false;
        }

        // Validation du nom d'utilisateur
        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            showError(usernameErrorLabel, "Username is required");
            isValid = false;
        } else if (usernameField.getText().trim().length() < 3 ||
                usernameField.getText().trim().length() > 30) {
            showError(usernameErrorLabel, "Username must be between 3 and 30 characters");
            isValid = false;
        }

        // Validation de l'email
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            showError(emailErrorLabel, "Email is required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            showError(emailErrorLabel, "Please enter a valid email address");
            isValid = false;
        }

        // Validation du mot de passe (uniquement pour les nouveaux utilisateurs ou si modifié)
        String password = passwordField.getText();
        if ((user == null || user.getId() == 0) && password.isEmpty()) {
            showError(passwordErrorLabel, "Password is required for new users");
            isValid = false;
        } else if (!password.isEmpty() && !PASSWORD_PATTERN.matcher(password).matches()) {
            showError(passwordErrorLabel, "Password must contain at least 8 characters including " +
                    "one uppercase letter, one lowercase letter, one number, and one special character");
            isValid = false;
        }

        // Validation de la date de naissance
        if (dateOBPicker.getValue() == null) {
            showError(dateOBErrorLabel, "Date of birth is required");
            isValid = false;
        } else {
            // Vérifier si la date est valide (âge raisonnable)
            LocalDate today = LocalDate.now();
            int age = Period.between(dateOBPicker.getValue(), today).getYears();
            if (age < MIN_AGE) {
                showError(dateOBErrorLabel, "User must be at least " + MIN_AGE + " years old");
                isValid = false;
            } else if (age > MAX_AGE) {
                showError(dateOBErrorLabel, "Invalid date of birth");
                isValid = false;
            }
        }

        // Validation du genre
        if (genderComboBox.getValue() == null || genderComboBox.getValue().isEmpty()) {
            showError(genderErrorLabel, "Gender selection is required");
            isValid = false;
        }

        // Validation du rôle
        if (roleComboBox.getValue() == null || roleComboBox.getValue().isEmpty()) {
            showError(roleErrorLabel, "Role selection is required");
            isValid = false;
        }

        // Validation des intérêts
        int selectedInterestsCount = interestsListView.getSelectionModel().getSelectedItems().size();
        if (selectedInterestsCount < MIN_INTERESTS) {
            showError(interestsErrorLabel, "Please select at least " + MIN_INTERESTS + " interests");
            isValid = false;
        } else if (selectedInterestsCount > MAX_INTERESTS) {
            showError(interestsErrorLabel, "You cannot select more than " + MAX_INTERESTS + " interests");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);
        }
    }

    private void clearErrorMessages() {
        // Clear all error labels
        if (emailErrorLabel != null) emailErrorLabel.setVisible(false);
        if (passwordErrorLabel != null) passwordErrorLabel.setVisible(false);
        if (nameErrorLabel != null) nameErrorLabel.setVisible(false);
        if (firstnameErrorLabel != null) firstnameErrorLabel.setVisible(false);
        if (usernameErrorLabel != null) usernameErrorLabel.setVisible(false);
        if (dateOBErrorLabel != null) dateOBErrorLabel.setVisible(false);
        if (genderErrorLabel != null) genderErrorLabel.setVisible(false);
        if (roleErrorLabel != null) roleErrorLabel.setVisible(false);
        if (interestsErrorLabel != null) interestsErrorLabel.setVisible(false);
    }

    @FXML
    public void handleCancel() {
        System.out.println("handleCancel called");
        // Close the form window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();

        if (parentController != null) {
            parentController.refreshUsers();
        }
    }


    private void setupPasswordVisibilityToggle() {
        System.out.println("Setting up password visibility toggle...");

        // Vérification que les composants existent
        if (passwordField == null) {
            System.err.println("passwordField est null!");
            return;
        }

        if (showPasswordButton == null) {
            System.err.println("showPasswordButton est null!");
            return;
        }

        if (passwordVisibilityIcon == null) {
            System.err.println("passwordVisibilityIcon est null!");
            return;
        }

        if (visiblePasswordField == null) {
            System.err.println("visiblePasswordField est null!");
            return;
        }

        // S'assurer que le TextField visible a les mêmes propriétés que le PasswordField
        visiblePasswordField.setPromptText(passwordField.getPromptText());
        visiblePasswordField.setPrefWidth(passwordField.getPrefWidth());
        visiblePasswordField.setPrefHeight(passwordField.getPrefHeight());

        // Synchroniser le contenu entre les deux champs
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Configurer l'action du bouton
        showPasswordButton.setOnAction(event -> {
            System.out.println("Password visibility toggle clicked!");

            // Inverser l'état de visibilité
            boolean makeVisible = !visiblePasswordField.isVisible();

            // Mettre à jour l'interface utilisateur
            visiblePasswordField.setVisible(makeVisible);
            visiblePasswordField.setManaged(makeVisible); // Important pour le layout
            passwordField.setVisible(!makeVisible);
            passwordField.setManaged(!makeVisible); // Important pour le layout

            // Mettre à jour l'icône
            passwordVisibilityIcon.setText(makeVisible ? "👁️" : "👁️‍🗨️");

            // Donner le focus au champ approprié et placer le curseur à la fin
            if (makeVisible) {
                visiblePasswordField.requestFocus();
                visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }

            System.out.println("Password is now " + (makeVisible ? "visible" : "hidden"));
        });

        // Configuration initiale: mot de passe caché
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        passwordVisibilityIcon.setText("👁️‍🗨️");

        System.out.println("Password visibility toggle setup complete.");
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing UserFormController...");

        // Initialize error labels
        initializeErrorLabels();

        setupPasswordVisibilityToggle();

        // Set up field validation listeners
        setupValidationListeners();
        setupPasswordVisibilityToggle();


        // Important: Lier explicitement les méthodes de gestionnaire d'événements aux boutons
        if (saveButton != null) {
            saveButton.setOnAction(event -> handleSave());
        } else {
            System.err.println("saveButton is null!");
        }

        if (cancelButton != null) {
            cancelButton.setOnAction(event -> handleCancel());
        } else {
            System.err.println("cancelButton is null!");
        }

        // Initialize ComboBox for Gender
        System.out.println("Setting up gender ComboBox...");
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                "Homme", "Femme", "Autre"
        );
        genderComboBox.setItems(genderOptions);

        // Initialize ComboBox for Role
        System.out.println("Setting up role ComboBox...");
        ObservableList<String> roleOptions = FXCollections.observableArrayList(
                "ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"
        );
        roleComboBox.setItems(roleOptions);

        // Setup ListView for interests/categories
        System.out.println("Setting up interests ListView...");
        setupInterestsListView();

        // Load form data if editing existing user
        if (user != null) {
            System.out.println("Populating form with existing user data...");
            populateForm();
        } else {
            // Charger les catégories immédiatement si nouvel utilisateur
            loadCategories();
        }
    }

    private void initializeErrorLabels() {
        // Create error labels if they don't exist in the FXML
        if (emailErrorLabel == null) {
            emailErrorLabel = createErrorLabel(emailField);
        }
        if (passwordErrorLabel == null) {
            passwordErrorLabel = createErrorLabel(passwordField);
        }
        if (nameErrorLabel == null) {
            nameErrorLabel = createErrorLabel(nameField);
        }
        if (firstnameErrorLabel == null) {
            firstnameErrorLabel = createErrorLabel(firstnameField);
        }
        if (usernameErrorLabel == null) {
            usernameErrorLabel = createErrorLabel(usernameField);
        }
        if (dateOBErrorLabel == null) {
            dateOBErrorLabel = createErrorLabel(dateOBPicker);
        }
        if (genderErrorLabel == null) {
            genderErrorLabel = createErrorLabel(genderComboBox);
        }
        if (roleErrorLabel == null) {
            roleErrorLabel = createErrorLabel(roleComboBox);
        }
        if (interestsErrorLabel == null) {
            interestsErrorLabel = createErrorLabel(interestsListView);
        }

        // Hide all error labels initially
        clearErrorMessages();
    }

    private Label createErrorLabel(Control field) {
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // Add label after the field in the parent container
        if (field != null && field.getParent() != null) {
            if (field.getParent() instanceof VBox) {
                VBox parent = (VBox) field.getParent();
                int fieldIndex = parent.getChildren().indexOf(field);
                if (fieldIndex >= 0 && fieldIndex < parent.getChildren().size()) {
                    parent.getChildren().add(fieldIndex + 1, errorLabel);
                }
            }
        }

        return errorLabel;
    }

    private void setupValidationListeners() {
        // Real-time validation on field focus loss
        if (emailField != null) {
            emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    if (emailField.getText().isEmpty()) {
                        showError(emailErrorLabel, "Email is required");
                    } else if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
                        showError(emailErrorLabel, "Please enter a valid email address");
                    } else {
                        emailErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (nameField != null) {
            nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    if (nameField.getText().isEmpty()) {
                        showError(nameErrorLabel, "Last name is required");
                    } else if (nameField.getText().length() < MIN_NAME_LENGTH ||
                            nameField.getText().length() > MAX_NAME_LENGTH) {
                        showError(nameErrorLabel, "Last name must be between " + MIN_NAME_LENGTH +
                                " and " + MAX_NAME_LENGTH + " characters");
                    } else {
                        nameErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (firstnameField != null) {
            firstnameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    if (firstnameField.getText().isEmpty()) {
                        showError(firstnameErrorLabel, "First name is required");
                    } else if (firstnameField.getText().length() < MIN_NAME_LENGTH ||
                            firstnameField.getText().length() > MAX_NAME_LENGTH) {
                        showError(firstnameErrorLabel, "First name must be between " + MIN_NAME_LENGTH +
                                " and " + MAX_NAME_LENGTH + " characters");
                    } else {
                        firstnameErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (usernameField != null) {
            usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) { // Focus lost
                    if (usernameField.getText().isEmpty()) {
                        showError(usernameErrorLabel, "Username is required");
                    } else if (usernameField.getText().length() < 3 ||
                            usernameField.getText().length() > 30) {
                        showError(usernameErrorLabel, "Username must be between 3 and 30 characters");
                    } else {
                        usernameErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (passwordField != null) {
            passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && !passwordField.getText().isEmpty()) { // Focus lost and not empty
                    if (!PASSWORD_PATTERN.matcher(passwordField.getText()).matches()) {
                        showError(passwordErrorLabel, "Password must contain at least 8 characters including " +
                                "one uppercase letter, one lowercase letter, one number, and one special character");
                    } else {
                        passwordErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (dateOBPicker != null) {
            dateOBPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    LocalDate today = LocalDate.now();
                    int age = Period.between(newVal, today).getYears();
                    if (age < MIN_AGE) {
                        showError(dateOBErrorLabel, "User must be at least " + MIN_AGE + " years old");
                    } else if (age > MAX_AGE) {
                        showError(dateOBErrorLabel, "Invalid date of birth");
                    } else {
                        dateOBErrorLabel.setVisible(false);
                    }
                }
            });
        }

        if (genderComboBox != null) {
            genderComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (genderErrorLabel != null) {
                    genderErrorLabel.setVisible(newVal == null || newVal.isEmpty());
                }
            });
        }

        if (roleComboBox != null) {
            roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (roleErrorLabel != null) {
                    roleErrorLabel.setVisible(newVal == null || newVal.isEmpty());
                }
            });
        }
    }

    private void setupInterestsListView() {
        // Vérifier si la ListView est null
        if (interestsListView == null) {
            System.err.println("Erreur critique: interestsListView est null!");
            return;
        }

        interestsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // CellFactory for display
        interestsListView.setCellFactory(lv -> new ListCell<Categories>() {
            @Override
            protected void updateItem(Categories item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        // Add listener to validate number of selected interests
        interestsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            int selectedCount = interestsListView.getSelectionModel().getSelectedItems().size();
            if (interestsErrorLabel != null) {
                if (selectedCount < MIN_INTERESTS) {
                    showError(interestsErrorLabel, "Please select at least " + MIN_INTERESTS + " interests");
                } else if (selectedCount > MAX_INTERESTS) {
                    showError(interestsErrorLabel, "You cannot select more than " + MAX_INTERESTS + " interests");
                } else {
                    interestsErrorLabel.setVisible(false);
                }
            }
        });
    }

    private void loadCategories() {
        if (!allCategories.isEmpty()) {
            System.out.println("Catégories déjà chargées, ignorant le rechargement");
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                System.out.println("Loading categories from service...");
                List<Categories> categories = userService.getAllCategories();

                System.out.println("Categories loaded (" + categories.size() + "):");
                for (Categories cat : categories) {
                    System.out.println(" - ID: " + cat.getId() +
                            ", Nom: " + cat.getNom() +
                            ", Description: " + cat.getDescription());
                }

                if (categories != null && !categories.isEmpty()) {
                    Platform.runLater(() -> {
                        allCategories.clear();
                        allCategories.addAll(categories);

                        if (interestsListView != null) {
                            interestsListView.setItems(allCategories);
                            interestsListView.refresh();

                            if (user != null && user.getInterests() != null) {
                                for (Categories interest : user.getInterests()) {
                                    for (Categories category : allCategories) {
                                        if (category.getId() == interest.getId()) {
                                            interestsListView.getSelectionModel().select(category);
                                        }
                                    }
                                }
                            }

                            System.out.println("ListView items count: " + interestsListView.getItems().size());
                        } else {
                            System.err.println("Erreur: interestsListView est null!");
                        }
                    });
                } else {
                    System.out.println("No categories found");
                    Platform.runLater(() -> {
                        showAlert("Warning", "No categories available", Alert.AlertType.WARNING);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading categories: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Failed to load categories: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}