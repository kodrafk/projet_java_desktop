package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Validator;

public class ResetPasswordController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Button        resetButton;

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    @FXML
    private void handleReset() {
        if (errorLabel != null) errorLabel.setVisible(false);

        String pwd     = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        String pwdErr = Validator.password(pwd);
        if (pwdErr != null) { show(pwdErr); return; }

        String confirmErr = Validator.confirmPassword(pwd, confirm);
        if (confirmErr != null) { show(confirmErr); return; }

        String hashed = BCrypt.hashpw(pwd, BCrypt.gensalt(10));
        repo.updatePassword(VerifyCodeController.pendingUserId, hashed);
        repo.clearVerificationCode(VerifyCodeController.pendingUserId);

        // Clear temp state
        ForgotPasswordController.pendingEmail = null;
        VerifyCodeController.pendingUserId = 0;

        Stage stage = (Stage) passwordField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    private void show(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }
}
