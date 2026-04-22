package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.EmailService;
import tn.esprit.projet.utils.Nav;

import java.time.LocalDateTime;
import java.util.Random;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label     messageLabel;
    @FXML private Button    sendButton;

    /** Pre-filled from the login screen's email field */
    static String prefillEmail;

    /** Passed to VerifyCodeController */
    static String pendingEmail;
    static String pendingDevCode;

    private final UserRepository repo         = new UserRepository();
    private final EmailService   emailService = new EmailService();

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);

        // Pre-fill email if the user had already typed it on the login screen
        if (prefillEmail != null && !prefillEmail.isBlank()) {
            emailField.setText(prefillEmail);
            prefillEmail = null; // consume it
        }
    }

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim();

        // ── Validate format ────────────────────────────────────────────────────
        if (email.isEmpty()) {
            show("Please enter your email address.", false);
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}$")) {
            show("Please enter a valid email address.", false);
            return;
        }

        // ── Check that the account actually exists ─────────────────────────────
        User user = repo.findByEmail(email);
        if (user == null) {
            show("No account found with this email address.\nPlease check and try again.", false);
            emailField.setStyle("-fx-background-color:#FFF5F5;-fx-border-color:#EF4444;"
                    + "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:12 14;-fx-font-size:13px;");
            return;
        }

        // Reset field style
        emailField.setStyle("-fx-background-color:#F0F7F0;-fx-border-color:#C8E6C9;"
                + "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:12 14;-fx-font-size:13px;");

        // ── Send the code ──────────────────────────────────────────────────────
        sendButton.setDisable(true);
        sendButton.setText("Sending...");
        show("Sending code to " + email + "...", true);
        pendingEmail = email;

        new Thread(() -> {
            String code = String.format("%06d", new Random().nextInt(1_000_000));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            repo.setVerificationCode(user.getId(), code, expiresAt);

            boolean sent = emailService.sendResetCode(email, code);

            Platform.runLater(() -> {
                sendButton.setDisable(false);
                sendButton.setText("Send Reset Code");

                if (sent) {
                    pendingDevCode = null;
                    show("✅ Code sent to " + email + ". Check your inbox.", true);
                } else {
                    // Email sending failed — show code as fallback
                    pendingDevCode = code;
                    show("⚠️ Email unavailable. Your code: " + code + " (valid 15 min)", true);
                }

                // Navigate to verify screen after 1.5 seconds
                new Thread(() -> {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        Nav.go(stage, "verify_code.fxml", "NutriLife — Verify Code");
                    });
                }).start();
            });
        }).start();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    private void show(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setStyle(success
                ? "-fx-text-fill:#16A34A;-fx-font-size:12px;"
                : "-fx-text-fill:#DC2626;-fx-font-size:12px;-fx-font-weight:bold;");
        messageLabel.setVisible(true);
    }
}
