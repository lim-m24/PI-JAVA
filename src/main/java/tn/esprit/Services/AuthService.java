package tn.esprit.Services;

import tn.esprit.dao.UserDAO;
import tn.esprit.Models.User;
import tn.esprit.utils.PasswordUtils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public boolean authenticate(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        return user != null && PasswordUtils.verifyPassword(password, user.getPassword());
    }

    public User getCurrentUser(String email) {
        return userDAO.getUserByEmail(email);
    }

    public String validateLoginConditions(User user) {
        if (user == null) {
            return "Identifiants invalides";
        }
        if (user.isBanned()) {
            return "Votre compte a été banni. Contactez un administrateur.";
        }
        if (!user.isActive()) {
            return "Votre compte est désactivé.";
        }
        if (!user.isVerified()) {
            return "Votre compte n'est pas encore vérifié. Veuillez vérifier votre email.";
        }
        return null;
    }

    public boolean register(User user) {
        if (userDAO.getUserByEmail(user.getEmail()) != null) {
            return false;
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerified(false); // Set user as unverified initially

        // Add user to database
        boolean userAdded = userDAO.addUser(user);

        if (userAdded) {
            // Send verification email
            return sendVerificationEmail(user.getEmail(), verificationToken);
        }
        return false;
    }

    public boolean emailExists(String email) {
        return userDAO.getUserByEmail(email) != null;
    }

    public boolean initiatePasswordReset(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        // Generate a 6-digit reset code
        String resetCode = String.format("%06d", new Random().nextInt(999999));
        user.setResetToken(resetCode);
        userDAO.updateUser(user);
        // Send email with reset code
        return sendResetCodeEmail(email, resetCode);
    }

    public boolean verifyResetCode(String email, String code) {
        User user = userDAO.getUserByEmail(email);
        if (user == null || user.getResetToken() == null) {
            return false;
        }
        return user.getResetToken().equals(code);
    }

    public boolean resetPassword(String email, String newPassword) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            return false;
        }
        user.setPassword(PasswordUtils.hashPassword(newPassword));
        user.setResetToken(null); // Clear reset token after use
        return userDAO.updateUser(user);
    }

    public boolean verifyEmail(String token) {
        User user = userDAO.getUserByVerificationToken(token);
        if (user == null) {
            return false;
        }
        user.setVerified(true);
        user.setVerificationToken(null); // Clear token after verification
        return userDAO.updateUser(user);
    }

    private boolean sendVerificationEmail(String email, String token) {
        final String username = "kayzeurdylan@gmail.com";
        final String password = "pkwijduelesvgaov";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Verify Your Syncylinky Account");
            String verificationLink = "http://localhost:8080/verify-email?token=" + token;
            message.setText("Please verify your email by clicking the following link:\n\n" + verificationLink);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending verification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendResetCodeEmail(String email, String code) {
        final String username = "kayzeurdylan@gmail.com";
        final String password = "pkwijduelesvgaov";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Syncylinky Password Reset Code");
            message.setText("Your password reset code is: " + code + "\n\nPlease enter this code to reset your password.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending reset code email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // In AuthService.java

    // In AuthService.java

    public boolean sendBanNotificationEmail(String email) {
        final String username = "kayzeurdylan@gmail.com";
        final String password = "pkwijduelesvgaov";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Syncylinky Account Banned");
            message.setText("Dear User,\n\nYour Syncylinky account has been banned. Please contact our support team at support@syncylinky.com for more information.\n\nBest regards,\nThe Syncylinky Team");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending ban notification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}