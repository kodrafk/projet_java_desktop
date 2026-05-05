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

    // Passed to ResetPasswordController
    static int pendingUserId;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Show which email the code was sent to
        String email = ForgotPasswordController.pendingEmail;
        if (emailLabel != null && email != null)
            emailLabel.setText("Code sent to: " + email);

        // Show code in UI only if email sending failed (fallback)
        String code = ForgotPasswordController.pendingDevCode;
        if (devCodeLabel != null) {
            if (code != null && !code.isBlank()) {
                devCodeLabel.setText("⚠️ Email unavailable — your code: " + code);
                devCodeLabel.setVisible(true);
                devCodeLabel.setManaged(true);
            } else {
                devCodeLabel.setVisible(false);
                devCodeLabel.setManaged(false);
            }
        }

        // Limit code field to 6 digits only
        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) codeField.setText(newVal.replaceAll("[^\\d]", ""));
            if (codeField.getText().length() > 6) codeField.setText(codeField.getText().substring(0, 6));
        });
    }

    @FXML
    private void handleVerify() {
        errorLabel.setVisible(false);
        String code = codeField.getText().trim();

        if (code.length() != 6) {
            showError("Please enter the 6-digit code.");
            return;
        }

        String email = ForgotPasswordController.pendingEmail;
        if (email == null || email.isBlank()) {
            showError("Session expired. Please start over.");
            return;
        }

        User user = repo.findByVerificationCode(email, code);

        if (user == null) {
            showError("Incorrect code. Please check your email and try again.");
            codeField.clear();
            codeField.requestFocus();
            return;
        }

        if (user.getVerificationCodeExpiresAt() != null
                && user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            showError("This code has expired. Please request a new one.");
            Stage stage = (Stage) codeField.getScene().getWindow();
            Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
            return;
        }

        // Code is valid — pass user ID to next screen
        pendingUserId = user.getId();
        Stage stage = (Stage) codeField.getScene().getWindow();
        Nav.go(stage, "reset_password.fxml", "NutriLife - New Password");
    }

    @FXML
    private void handleResend() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) codeField.getScene().getWindow();
        Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
