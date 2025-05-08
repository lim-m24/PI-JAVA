package tn.esprit.Controllers;

import tn.esprit.dao.CategoryDAO;
import tn.esprit.Models.Categories;
import tn.esprit.Models.User;
import tn.esprit.Services.AuthService;
import tn.esprit.utils.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterController {
    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> dayCombo;
    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private RadioButton hommeRadio;
    @FXML private RadioButton femmeRadio;
    @FXML private RadioButton autreRadio;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button registerButton;
    @FXML private FlowPane interestsPane;

    // Error fields
    @FXML private Text lastNameError;
    @FXML private Text firstNameError;
    @FXML private Text emailError;
    @FXML private Text genderError;
    @FXML private Text birthDateError;
    @FXML private Text passwordError;
    @FXML private Text confirmPasswordError;
    @FXML private Text interestsError;
    @FXML private Text termsError;

    private final AuthService authService = new AuthService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ToggleGroup genderToggleGroup = new ToggleGroup();
    private final Map<Integer, Boolean> selectedInterests = new HashMap<>();

    @FXML
    public void initialize() {
        // Setup gender radio buttons
        hommeRadio.setToggleGroup(genderToggleGroup);
        femmeRadio.setToggleGroup(genderToggleGroup);
        autreRadio.setToggleGroup(genderToggleGroup);
        hommeRadio.setSelected(true);

        // Set user data for gender radio buttons
        hommeRadio.setUserData("Homme");
        femmeRadio.setUserData("Femme");
        autreRadio.setUserData("Autre");

        // Initialize date dropdowns
        for (int i = 1; i <= 31; i++) dayCombo.getItems().add(String.valueOf(i));
        monthCombo.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December");
        for (int i = LocalDate.now().getYear() - 100; i <= LocalDate.now().getYear(); i++) {
            yearCombo.getItems().add(String.valueOf(i));
        }

        // Set default values
        dayCombo.setValue("1");
        monthCombo.setValue("January");
        yearCombo.setValue(String.valueOf(LocalDate.now().getYear() - 20));

        // Real-time validation
        setupFieldValidations();

        // Configure interests pane
        interestsPane.setPrefWidth(350);
        interestsPane.setVgap(20);
        interestsPane.setHgap(20);

        // Load interests categories
        loadInterestsCategories();

        // Initialize error messages to empty
        clearErrorMessages();
    }

    private void clearErrorMessages() {
        lastNameError.setText("");
        firstNameError.setText("");
        emailError.setText("");
        genderError.setText("");
        birthDateError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        interestsError.setText("");
        termsError.setText("");
    }

    private void loadInterestsCategories() {
        List<Categories> categories = categoryDAO.getAllCategories();

        for (Categories category : categories) {
            try {
                // Créer un conteneur pour chaque catégorie
                VBox categoryBox = new VBox();
                categoryBox.setAlignment(javafx.geometry.Pos.CENTER);
                categoryBox.setSpacing(10);
                categoryBox.setPadding(new Insets(15));
                categoryBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10; -fx-cursor: hand;");
                categoryBox.setPrefWidth(120);
                categoryBox.setPrefHeight(150);

                // Créer un conteneur pour l'image avec coins arrondis
                ImageView imageView = new ImageView();
                Image image = new Image(category.getCover(), 80, 80, true, true);
                imageView.setImage(image);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);

                // Créer un rectangle pour le clip de l'image
                Rectangle clip = new Rectangle(80, 80);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                imageView.setClip(clip);

                // Ajouter une ombre portée
                DropShadow dropShadow = new DropShadow();
                dropShadow.setRadius(5.0);
                dropShadow.setOffsetX(0);
                dropShadow.setOffsetY(0);
                dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
                imageView.setEffect(dropShadow);

                // Ajouter le nom de la catégorie
                Text categoryName = new Text(category.getNom());
                categoryName.setFill(Color.WHITE);
                categoryName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                // Ajouter l'image et le texte au conteneur
                categoryBox.getChildren().addAll(imageView, categoryName);

                // Ajouter un gestionnaire d'événements pour la sélection
                categoryBox.setOnMouseClicked(event -> {
                    boolean isSelected = selectedInterests.getOrDefault(category.getId(), false);
                    selectedInterests.put(category.getId(), !isSelected);

                    // Changer l'apparence en fonction de la sélection
                    if (!isSelected) {
                        // Sélectionné
                        categoryBox.setStyle("-fx-background-color: rgba(48, 79, 254, 0.7); -fx-background-radius: 10; -fx-cursor: hand;");
                        dropShadow.setColor(Color.rgb(0, 114, 255, 0.8));
                        imageView.setEffect(dropShadow);

                        // Reset interests error if some are selected
                        interestsError.setText("");
                    } else {
                        // Non sélectionné
                        categoryBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10; -fx-cursor: hand;");
                        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
                        imageView.setEffect(dropShadow);
                    }

                    validateForm();
                });

                // Ajouter le conteneur au FlowPane
                interestsPane.getChildren().add(categoryBox);

            } catch (Exception e) {
                System.err.println("Error loading category image: " + e.getMessage());
            }
        }
    }

    private void setupFieldValidations() {
        // Validation du nom
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                lastNameField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
                lastNameError.setText("Le nom est requis");
            } else {
                lastNameField.setStyle("-fx-background-color: #333743; -fx-text-fill: white;");
                lastNameError.setText("");
            }
            validateForm();
        });

        // Validation du prénom
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                firstNameField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
                firstNameError.setText("Le prénom est requis");
            } else {
                firstNameField.setStyle("-fx-background-color: #333743; -fx-text-fill: white;");
                firstNameError.setText("");
            }
            validateForm();
        });

        // Email validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                emailField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
                if (newVal.isEmpty()) {
                    emailError.setText("L'email est requis");
                } else {
                    emailError.setText("Format d'email invalide");
                }
            } else {
                emailField.setStyle("-fx-background-color: #333743; -fx-text-fill: white;");
                emailError.setText("");
            }
            validateForm();
        });

        // Password validation
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            validateForm();
        });

        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePassword();
            validateForm();
        });

        // Terms checkbox validation
        termsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                termsError.setText("Vous devez accepter les conditions d'utilisation");
            } else {
                termsError.setText("");
            }
            validateForm();
        });
    }

    private void validatePassword() {
        String password = passwordField.getText();
        boolean isValid = password.length() >= 8 &&
                password.matches(".*[A-Z].*") && // At least one uppercase
                password.matches(".*[a-z].*") && // At least one lowercase
                password.matches(".*\\d.*") &&   // At least one digit
                password.matches(".*[@$!%*?&].*"); // At least one special char

        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
            passwordError.setText("Le mot de passe est requis");
        } else if (!isValid) {
            passwordField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
            passwordError.setText("Le mot de passe ne respecte pas les critères");
        } else {
            passwordField.setStyle("-fx-background-color: #333743; -fx-text-fill: white;");
            passwordError.setText("");
        }

        // Confirmer mot de passe
        String confirmPassword = confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            confirmPasswordField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
            confirmPasswordError.setText("Veuillez confirmer le mot de passe");
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordField.setStyle("-fx-border-color: #FF3B30; -fx-border-width: 1px; -fx-background-color: #333743; -fx-text-fill: white;");
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
        } else {
            confirmPasswordField.setStyle("-fx-background-color: #333743; -fx-text-fill: white;");
            confirmPasswordError.setText("");
        }
    }

    private void validateForm() {
        // Vérification des intérêts (au moins un sélectionné)
        boolean hasInterests = selectedInterests.containsValue(true);
        if (!hasInterests) {
            interestsError.setText("Veuillez sélectionner au moins un centre d'intérêt");
        } else {
            interestsError.setText("");
        }

        boolean isValid = !lastNameField.getText().trim().isEmpty() &&
                !firstNameField.getText().trim().isEmpty() &&
                emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") &&
                passwordField.getText().length() >= 8 &&
                passwordField.getText().matches(".*[A-Z].*") &&
                passwordField.getText().matches(".*[a-z].*") &&
                passwordField.getText().matches(".*\\d.*") &&
                passwordField.getText().matches(".*[@$!%*?&].*") &&
                passwordField.getText().equals(confirmPasswordField.getText()) &&
                hasInterests &&
                termsCheckBox.isSelected();

        registerButton.setDisable(!isValid);
    }

    private boolean validateBeforeSubmit() {
        boolean isValid = true;

        // Last name validation
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Le nom est requis");
            isValid = false;
        }

        // First name validation
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Le prénom est requis");
            isValid = false;
        }

        // Email validation
        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailError.setText("Format d'email invalide");
            isValid = false;
        }

        // Password validation
        String password = passwordField.getText();
        boolean passwordValid = password.length() >= 8 &&
                password.matches(".*[A-Z].*") && // At least one uppercase
                password.matches(".*[a-z].*") && // At least one lowercase
                password.matches(".*\\d.*") &&   // At least one digit
                password.matches(".*[@$!%*?&].*"); // At least one special char

        if (!passwordValid) {
            passwordError.setText("Le mot de passe ne respecte pas les critères");
            isValid = false;
        }

        // Confirm password validation
        if (!confirmPasswordField.getText().equals(password)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            isValid = false;
        }

        // Interests validation
        boolean hasInterests = selectedInterests.containsValue(true);
        if (!hasInterests) {
            interestsError.setText("Veuillez sélectionner au moins un centre d'intérêt");
            isValid = false;
        }

        // Terms validation
        if (!termsCheckBox.isSelected()) {
            termsError.setText("Vous devez accepter les conditions d'utilisation");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void handleRegister() {
        try {
            // Vérification finale avant soumission
            if (!validateBeforeSubmit()) {
                return;
            }

            // Create birth date
            LocalDate birthDate = LocalDate.of(
                    Integer.parseInt(yearCombo.getValue()),
                    monthCombo.getSelectionModel().getSelectedIndex() + 1,
                    Integer.parseInt(dayCombo.getValue())
            );

            User newUser = new User(
                    0, // id will be generated
                    emailField.getText(),
                    "ROLE_USER", // default role
                    passwordField.getText(),
                    lastNameField.getText(),
                    firstNameField.getText(),
                    generateUsername(firstNameField.getText(), lastNameField.getText()),
                    birthDate,
                    ((RadioButton)genderToggleGroup.getSelectedToggle()).getUserData().toString(),
                    false, // banned
                    false  // verified
            );

            if (authService.register(newUser)) {
                // Sauvegarder les centres d'intérêt sélectionnés
                saveUserInterests(newUser.getId());

                AlertUtils.showAlert("Success", "Registration successful! Please check your email to verify your account.", Alert.AlertType.INFORMATION);
                openLoginScreen();
            } else {
                AlertUtils.showAlert("Error", "Registration failed. Email may already exist or email verification could not be sent.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            AlertUtils.showAlert("Error", "Please fill all fields correctly: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void saveUserInterests(int userId) {
        for (Map.Entry<Integer, Boolean> entry : selectedInterests.entrySet()) {
            if (entry.getValue()) {
                int categoryId = entry.getKey();
                // Utiliser un DAO approprié pour enregistrer l'intérêt
                // Par exemple: userCategoryDAO.saveUserCategory(userId, categoryId);
            }
        }
    }

    private String generateUsername(String firstname, String lastname) {
        return (firstname.substring(0, 1) + lastname).toLowerCase();
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/auth/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        } catch (IOException e) {
            AlertUtils.showAlert("Error", "Could not load login page", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLoginLink() {
        openLoginScreen();
    }
}