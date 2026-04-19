package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;

import java.time.LocalDateTime;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label     messageLabel;

    // Shared state for next screens
    static String pendingEmail;
    static String pendingDevCode;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (messageLabel != null) messageLabel.setVisible(false);
    }

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            show("Please enter your email address.", false);
            return;
        }

        User user = repo.findByEmail(email);
        pendingEmail = email;

        if (user != null) {
            String code = String.format("%06d", new Random().nextInt(999999));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            repo.setVerificationCode(user.getId(), code, expiresAt);
            pendingDevCode = code;
            show("Code generated! Check the next screen for your code (dev mode).", true);
        } else {
            pendingDevCode = null;
            show("If an account exists with that email, a code has been sent.", true);
        }

        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "verify_code.fxml", "NutriLife - Verify Code");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    private void show(String msg, boolean success) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(success
                    ? "-fx-text-fill:#16A34A;-fx-font-size:12px;"
                    : "-fx-text-fill:#DC2626;-fx-font-size:12px;");
            messageLabel.setVisible(true);
        }
    }
}
