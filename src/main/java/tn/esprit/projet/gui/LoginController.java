package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.RecaptchaService;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox      rememberMeCheckbox;
    @FXML private Label         captchaLabel;
    @FXML private WebView       recaptchaView;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;
    @FXML private Button        faceIdButton;
    @FXML private Button        googleButton;
    @FXML private Button        signUpButton;
    @FXML private Button        forgotPasswordButton;

    private final UserRepository   repo            = new UserRepository();
    private final RecaptchaService recaptchaService = new RecaptchaService();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        
        // reCAPTCHA disabled
        if (recaptchaView != null) {
            recaptchaView.setVisible(false);
            recaptchaView.setManaged(false);
        }
        if (captchaLabel != null) {
            captchaLabel.setVisible(false);
            captchaLabel.setManaged(false);
        }
    }

    private void loadRecaptcha() {
        if (recaptchaView == null) return;
        try {
            String url = getClass().getResource("/html/recaptcha.html").toExternalForm();
            recaptchaView.getEngine().load(url);
            recaptchaView.getEngine().locationProperty().addListener((obs, old, newLoc) -> {
                if (newLoc != null && newLoc.contains("#verified:")) {
                    if (captchaLabel != null) {
                        captchaLabel.setText("✅ Verified!");
                        captchaLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;-fx-font-weight:bold;");
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Could not load: " + e.getMessage());
        }
    }

    private String getRecaptchaToken() {
        if (recaptchaView == null) return null;
        try {
            Object result = recaptchaView.getEngine().executeScript("getToken()");
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
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
            stage.initOwner((Stage) emailField.getScene().getWindow());
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

    @FXML
    private void handleFaceIdUpload() {
        hideError();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_verify_upload.fxml"));
            Parent root = loader.load();
            FaceIdVerifyUploadController ctrl = loader.getController();
            ctrl.setTargetUser(null);
            ctrl.setOnSuccess(() -> {
                if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
            });
            ctrl.setOnFailure(() -> showError("Invalid credentials."));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Face ID - Upload Photo");
            stage.setScene(new Scene(root, 560, 600));
            stage.setResizable(false);
            stage.showAndWait();

            if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load upload interface: " + e.getMessage());
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
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Google Login");
            stage.setScene(new Scene(root, 520, 620));
            stage.setResizable(false);
            stage.showAndWait();

            if (Session.isLoggedIn()) {
                navigateAfterLogin(Session.getCurrentUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Google login: " + e.getMessage());
        }
    }

    // ── Password login ─────────────────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        hideError();

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty())    { showError("Please enter your email.");    return; }
        if (password.isEmpty()) { showError("Please enter your password."); return; }

        User user = repo.findByEmail(email);
        if (user == null) { showError("Invalid email or password."); return; }
        if (!user.isActive()) { showError("Your account is inactive."); return; }
        
        boolean passwordValid = false;
        try {
            passwordValid = BCrypt.checkpw(password, user.getPassword());
        } catch (IllegalArgumentException e) {
            passwordValid = password.equals(user.getPassword());
        }
        
        if (!passwordValid) {
            showError("Invalid email or password.");
            return;
        }

        Session.login(user);
        navigateAfterLogin(user);
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    @FXML
    private void handleForgotPassword() {
        ForgotPasswordController.prefillEmail = emailField.getText().trim();
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "forgot_password.fxml", "Forgot Password");
    }

    @FXML
    private void handleGoRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "register.fxml", "Register");
    }

    private void navigateAfterLogin(User user) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        if (user.isAdmin()) {
            Nav.go(stage, "admin_dashboard.fxml", "NutriLife - Admin", 1320, 780, true);
        } else {
            Nav.go(stage, "home.fxml", "NutriLife - Home", 1280, 760, true);
        }
    }

    private void showError(String msg) {
        if (errorLabel != null) { errorLabel.setText(msg); errorLabel.setVisible(true); }
    }

    private void hideError() {
        if (errorLabel != null) { errorLabel.setVisible(false); errorLabel.setText(""); }
    }
}
