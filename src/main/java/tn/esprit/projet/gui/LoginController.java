package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.utils.PasswordUtil;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.UserValidator;

public class LoginController {

    // ── Integration-style fields ───────────────────────────────────────────────
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private TextField     fieldPasswordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private Label         errEmail;
    @FXML private Label         errPassword;
    @FXML private Label         errGeneral;

    // ── gestion_user-style fields (optional, may be null) ─────────────────────
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        faceIdButton;
    @FXML private Button        googleButton;
    @FXML private Button        forgotPasswordButton;
    @FXML private WebView       recaptchaView;
    @FXML private Label         captchaLabel;

    private boolean passwordVisible = false;
    private final UserDAO userDAO = new UserDAO();

    // reCAPTCHA service (optional)
    private tn.esprit.projet.services.RecaptchaService recaptchaService;

    @FXML
    public void initialize() {
        // Try to init reCAPTCHA service
        try {
            recaptchaService = new tn.esprit.projet.services.RecaptchaService();
        } catch (Exception e) {
            System.err.println("[Login] reCAPTCHA service unavailable: " + e.getMessage());
        }

        // Setup password toggle (integration style)
        if (fieldPasswordVisible != null) {
            fieldPasswordVisible.textProperty().addListener((o, a, b) -> {
                if (passwordVisible) fieldPassword.setText(b);
            });
            if (fieldPassword != null) {
                fieldPassword.textProperty().addListener((o, a, b) -> {
                    if (!passwordVisible) fieldPasswordVisible.setText(b);
                });
            }
            fieldPasswordVisible.setVisible(false);
            fieldPasswordVisible.setManaged(false);
        }

        // Setup reCAPTCHA (gestion_user style)
        if (recaptchaView != null) {
            recaptchaView.setVisible(true);
            recaptchaView.setManaged(true);
            loadRecaptcha();
        }
        if (captchaLabel != null) {
            captchaLabel.setVisible(true);
            captchaLabel.setManaged(true);
        }
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    private void loadRecaptcha() {
        try {
            tn.esprit.projet.utils.RecaptchaLoader.load(recaptchaView, captchaLabel);
        } catch (Exception e) {
            System.err.println("[Login] reCAPTCHA load failed: " + e.getMessage());
        }
    }

    private String getRecaptchaToken() {
        try {
            return tn.esprit.projet.utils.RecaptchaLoader.getToken(recaptchaView);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Toggle password visibility ─────────────────────────────────────────────
    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            if (fieldPasswordVisible != null) {
                fieldPasswordVisible.setText(fieldPassword.getText());
                fieldPasswordVisible.setVisible(true);
                fieldPasswordVisible.setManaged(true);
            }
            if (fieldPassword != null) {
                fieldPassword.setVisible(false);
                fieldPassword.setManaged(false);
            }
            if (btnTogglePassword != null) btnTogglePassword.setText("🙈");
        } else {
            if (fieldPassword != null) {
                fieldPassword.setText(fieldPasswordVisible != null ? fieldPasswordVisible.getText() : "");
                fieldPassword.setVisible(true);
                fieldPassword.setManaged(true);
            }
            if (fieldPasswordVisible != null) {
                fieldPasswordVisible.setVisible(false);
                fieldPasswordVisible.setManaged(false);
            }
            if (btnTogglePassword != null) btnTogglePassword.setText("👁");
        }
    }

    private String getEmailValue() {
        if (emailField != null && !emailField.getText().isBlank()) return emailField.getText().trim();
        if (fieldEmail != null) return fieldEmail.getText().trim();
        return "";
    }

    private String getPasswordValue() {
        if (passwordField != null && !passwordField.getText().isBlank()) return passwordField.getText();
        if (passwordVisible && fieldPasswordVisible != null) return fieldPasswordVisible.getText();
        if (fieldPassword != null) return fieldPassword.getText();
        return "";
    }

    private Stage getStage() {
        if (emailField != null && emailField.getScene() != null)
            return (Stage) emailField.getScene().getWindow();
        if (fieldEmail != null && fieldEmail.getScene() != null)
            return (Stage) fieldEmail.getScene().getWindow();
        return null;
    }

    // ── Face ID login ──────────────────────────────────────────────────────────
    @FXML
    private void handleFaceIdLogin() {
        hideError();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_verify.fxml"));
            Parent root = loader.load();
            FaceIdVerifyController ctrl = loader.getController();
            ctrl.setOnSuccess(() -> {
                if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
            });
            ctrl.setOnFailure(() -> showError("Face ID authentication failed. Please use password."));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getStage());
            stage.setTitle("Face ID Authentication");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Face ID: " + e.getMessage());
        }
    }

    // ── Google login ───────────────────────────────────────────────────────────
    @FXML
    private void handleGoogleLogin() {
        hideError();
        try {
            GoogleAuthController.fromRegister = false;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/google_auth.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getStage());
            stage.setTitle("Google Login");
            stage.setScene(new Scene(root, 520, 620));
            stage.setResizable(false);
            stage.showAndWait();

            if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Google login: " + e.getMessage());
        }
    }

    // ── Forgot password ────────────────────────────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        try {
            ForgotPasswordController.prefillEmail = getEmailValue();
            Stage stage = getStage();
            if (stage != null) {
                tn.esprit.projet.utils.Nav.go(stage, "forgot_password.fxml", "Forgot Password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Main login ─────────────────────────────────────────────────────────────
    @FXML
    private void handleLogin() {
        clearErrors();

        String email    = getEmailValue();
        String password = getPasswordValue();
        boolean ok      = true;

        // Validate email
        String emailErr = UserValidator.validateEmail(email);
        if (emailErr != null) {
            if (errEmail != null) errEmail.setText(emailErr);
            if (fieldEmail != null) fieldEmail.setStyle(UserValidator.getErrorStyle());
            ok = false;
        }
        // Validate password
        if (password.isBlank()) {
            if (errPassword != null) errPassword.setText("Password is required.");
            if (fieldPassword != null) fieldPassword.setStyle(UserValidator.getErrorStyle());
            ok = false;
        }
        if (!ok) return;

        // reCAPTCHA check (only if recaptchaView is present)
        if (recaptchaView != null && recaptchaService != null) {
            String token = getRecaptchaToken();
            if (token == null || token.isBlank()) {
                showError("Please complete the Security Verification (reCAPTCHA).");
                return;
            }
            if (!recaptchaService.verify(token)) {
                showError("Security verification failed. Please try again.");
                loadRecaptcha();
                return;
            }
        }

        User user = userDAO.findByEmail(email);
        if (user == null || !PasswordUtil.checkPassword(password, user.getPassword())) {
            showError("Invalid email or password.");
            return;
        }
        if (!user.isActive()) {
            showError("Your account has been deactivated. Contact an administrator.");
            return;
        }

        // Set session (both Session and SessionManager for compatibility)
        Session.login(user);
        SessionManager.setCurrentUser(user);

        try {
            EthicalPointsManager.reset();
            EthicalPointsManager.loadFromDatabase();
        } catch (Exception e) {
            System.err.println("[Login] EthicalPoints load failed: " + e.getMessage());
        }

        navigateAfterLogin(user);
    }

    @FXML
    private void handleGoRegister() {
        try {
            Stage stage = getStage();
            if (stage != null) {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
                stage.setScene(new Scene(root, 1100, 720));
                stage.setTitle("NutriLife - Register");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void navigateAfterLogin(User user) {
        Stage stage = getStage();
        if (stage == null) return;
        if (user.isAdmin()) {
            navigate(stage, "/fxml/admin_layout.fxml", "NutriLife - Admin Panel", 1320, 780, true);
        } else {
            navigate(stage, "/fxml/main_layout.fxml", "NutriLife - Dashboard", 1280, 760, true);
        }
    }

    private void navigate(Stage stage, String fxml, String title, int w, int h, boolean maximize) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.setMaximized(maximize);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showError(String msg) {
        if (errGeneral != null) errGeneral.setText(msg);
        if (errorLabel != null) { errorLabel.setText(msg); errorLabel.setVisible(true); }
    }

    private void hideError() {
        if (errorLabel != null) { errorLabel.setVisible(false); errorLabel.setText(""); }
    }

    private void clearErrors() {
        if (errEmail   != null) errEmail.setText("");
        if (errPassword != null) errPassword.setText("");
        if (errGeneral  != null) errGeneral.setText("");
        if (errorLabel  != null) { errorLabel.setText(""); errorLabel.setVisible(false); }
        if (fieldEmail  != null) fieldEmail.setStyle(UserValidator.getNeutralStyle());
        if (fieldPassword != null) fieldPassword.setStyle(UserValidator.getNeutralStyle());
        if (fieldPasswordVisible != null) fieldPasswordVisible.setStyle(UserValidator.getNeutralStyle());
    }
}
