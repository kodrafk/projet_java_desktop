package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;

public class ResetPasswordController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Label         errPassword;
    @FXML private Label         errConfirm;
    @FXML private Button        resetButton;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);

        // Real-time validation
        passwordField.focusedProperty().addListener((o, was, now) -> {
            if (!now) validatePassword();
        });
        confirmPasswordField.focusedProperty().addListener((o, was, now) -> {
            if (!now) validateConfirm();
        });
    }

    @FXML
    private void handleReset() {
        errorLabel.setVisible(false);

        boolean ok = validatePassword() & validateConfirm();
        if (!ok) return;

        if (VerifyCodeController.pendingUserId == 0) {
            showError("Session expired. Please start the reset process again.");
            return;
        }

        String hashed = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10));
        repo.updatePassword(VerifyCodeController.pendingUserId, hashed);
        repo.clearVerificationCode(VerifyCodeController.pendingUserId);

        // Clear all temp state
        ForgotPasswordController.pendingEmail   = null;
        ForgotPasswordController.pendingDevCode = null;
        VerifyCodeController.pendingUserId      = 0;

        // Navigate to login with success message
        Stage stage = (Stage) passwordField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    private boolean validatePassword() {
        String pwd = passwordField.getText();
        if (pwd.isEmpty()) {
            setErr(errPassword, "Password is required.");
            return false;
        }
        if (pwd.length() < 8) {
            setErr(errPassword, "Password must be at least 8 characters.");
            return false;
        }
        setErr(errPassword, null);
        return true;
    }

    private boolean validateConfirm() {
        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) {
            setErr(errConfirm, "Please confirm your password.");
            return false;
        }
        if (!confirm.equals(passwordField.getText())) {
            setErr(errConfirm, "Passwords do not match.");
            return false;
        }
        setErr(errConfirm, null);
        return true;
    }

    private void setErr(Label lbl, String msg) {
        if (lbl == null) return;
        if (msg != null) {
            lbl.setText(msg);
            lbl.setVisible(true);
        } else {
            lbl.setText("");
            lbl.setVisible(false);
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }
}
