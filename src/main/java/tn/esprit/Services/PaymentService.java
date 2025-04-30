package tn.esprit.Services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentService {

    private static final String STRIPE_TEST_SECRET_KEY = ""; // Replace with your actual test secret key
    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    public PaymentService() {
        Stripe.apiKey = STRIPE_TEST_SECRET_KEY;
    }

    public boolean processPayment(double amount, String description) {
        try {
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", (int) (amount * 100)); // Amount in cents
            chargeParams.put("currency", "eur"); // Or your desired currency
            chargeParams.put("source", "tok_visa"); // A test card token (will always succeed)
            chargeParams.put("description", description);

            Charge charge = Charge.create(chargeParams);
            return charge.getStatus().equals("succeeded");
        } catch (StripeException e) {
            LOGGER.log(Level.SEVERE, "Error processing Stripe payment: " + e.getMessage(), e);
            return false; // Indicate that the payment failed
        }
    }
}