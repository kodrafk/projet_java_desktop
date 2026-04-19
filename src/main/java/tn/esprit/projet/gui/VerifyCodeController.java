package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;

import java.time.LocalDateTime;

public class VerifyCodeController {

    @FXML private Label     emailLabel;
    @FXML private Label     devCodeLabel;
    @FXML private TextField codeField;
    @FXML private Label     errorLabel;
    @FXML private Button    verifyButton;

    static int pendingUserId;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);

        String email = ForgotPasswordController.pendingEmail;
        if (emailLabel != null && email != null)
            emailLabel.setText("Code sent to: " + email);

        // Show dev code if available
        String code = ForgotPasswordController.pendingDevCode;
        if (devCodeLabel != null) {
            if (code != null && !code.isBlank()) {
                devCodeLabel.setText("🔑 Dev mode — your code: " + code + " (expires in 15 min)");
                devCodeLabel.setVisible(true);
                devCodeLabel.setManaged(true);
            } else {
                devCodeLabel.setVisible(false);
                devCodeLabel.setManaged(false);
            }
        }
    }

    @FXML
    private void handleVerify() {
        if (errorLabel != null) errorLabel.setVisible(false);
        String code = codeField.getText().trim();

        if (code.isEmpty() || code.length() != 6) {
            show("Invalid verification code.");
            return;
        }

        String email = ForgotPasswordController.pendingEmail;
        if (email == null) { show("Session expired. Please start over."); return; }

        User user = repo.findByVerificationCode(email, code);
        if (user == null) { show("Invalid verification code."); return; }

        if (user.getVerificationCodeExpiresAt() != null &&
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            show("Verification code has expired. Please request a new one.");
            Stage stage = (Stage) codeField.getScene().getWindow();
            Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
            return;
        }

        pendingUserId = user.getId();
        Stage stage = (Stage) codeField.getScene().getWindow();
        Nav.go(stage, "reset_password.fxml", "NutriLife - Reset Password");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
    }

    private void show(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }
}
