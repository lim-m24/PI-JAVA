package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Services.PaymentService;
import tn.esprit.Models.Abonnements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentFormController {

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryField;

    @FXML
    private TextField cvcField;

    @FXML
    private TextField cardholderNameField;

    @FXML
    private Label amountToPayLabel; // Added this

    private Abonnements abonnementToPay;
    private PaymentService paymentService;
    private UserAbonnementsController userAbonnementsController;
    private Stage dialogStage;

    public void setAbonnement(Abonnements abonnement) {
        this.abonnementToPay = abonnement;
        if (amountToPayLabel != null) {
            amountToPayLabel.setText("Amount to pay: " + String.format("%.2f", abonnement.getPrix()) + " TND");
        }
    }

    public void setPaymentService(PaymentService service) {
        this.paymentService = service;
    }

    public void setUserAbonnementsController(UserAbonnementsController controller) {
        this.userAbonnementsController = controller;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void handlePayButtonAction() {
        String cardNumber = cardNumberField.getText();
        String expiry = expiryField.getText();
        String cvc = cvcField.getText();
        String cardholderName = cardholderNameField.getText();

        if (isValidTestCardNumber(cardNumber) && isValidExpiry(expiry) && isValidCVC(cvc)) {
            boolean paymentSuccessful = paymentService.processPayment(abonnementToPay.getPrix(), "Payment for " + abonnementToPay.getNom() + " (Cardholder: " + cardholderName + ")");
            if (paymentSuccessful) {
                showAlert("Payment Successful!", "Your simulated payment was processed successfully.");
                userAbonnementsController.markAbonnementAsPaid(abonnementToPay);
                dialogStage.close();
            } else {
                showAlert("Payment Failed", "Simulated payment failed.");
                dialogStage.close();
            }
        } else {
            showAlert("Error", "Invalid card details. Please use Stripe test card information.");
        }
    }

    private boolean isValidTestCardNumber(String cardNumber) {
        Pattern pattern = Pattern.compile("^(4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})$");
        Matcher matcher = pattern.matcher(cardNumber.replaceAll("\\s+", ""));
        return matcher.matches();
    }

    private boolean isValidExpiry(String expiry) {
        Pattern pattern = Pattern.compile("^(0[1-9]|1[0-2])\\/([0-9]{2})$");
        Matcher matcher = pattern.matcher(expiry);
        if (!matcher.matches()) {
            return false;
        }

        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt("20" + parts[1]);
            int currentYear = java.time.Year.now().getValue();
            int currentMonth = java.time.LocalDate.now().getMonthValue();

            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean isValidCVC(String cvc) {
        Pattern pattern = Pattern.compile("^[0-9]{3,4}$");
        Matcher matcher = pattern.matcher(cvc);
        return matcher.matches();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}