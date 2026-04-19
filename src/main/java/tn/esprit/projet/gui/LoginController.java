package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

import java.util.Random;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox      rememberMeCheckbox;
    @FXML private Label         captchaLabel;
    @FXML private TextField     captchaField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;
    @FXML private Button        faceIdButton;
    @FXML private Button        googleButton;

    private int captchaA, captchaB;
    private final UserRepository       repo = new UserRepository();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        generateCaptcha();
    }

    private void generateCaptcha() {
        Random rng = new Random();
        captchaA = rng.nextInt(9) + 1;
        captchaB = rng.nextInt(9) + 1;
        if (captchaLabel != null)
            captchaLabel.setText("What is " + captchaA + " + " + captchaB + "?");
        if (captchaField != null) captchaField.clear();
    }

    // ── Password login ─────────────────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        hideError();

        // Captcha
        String capText = captchaField != null ? captchaField.getText().trim() : "";
        try {
            if (Integer.parseInt(capText) != captchaA + captchaB) {
                showError("Incorrect captcha answer.");
                generateCaptcha();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Incorrect captcha answer.");
            generateCaptcha();
            return;
        }

        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty())    { showError("Email is required.");    return; }
        if (password.isEmpty()) { showError("Password is required."); return; }

        User user = repo.findByEmail(email);
        if (user == null) { showError("Invalid email or password."); generateCaptcha(); return; }
        if (!user.isActive()) { showError("Your user account is deactivated."); return; }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            showError("Invalid email or password.");
            generateCaptcha();
            return;
        }

        Session.login(user);
        navigateAfterLogin(user);
    }

    // ── Face ID login ──────────────────────────────────────────────────────────

    @FXML
    private void handleFaceIdLogin() {
        hideError();
        // Open camera directly — no email needed.
        // The verify controller will scan ALL enrolled users and find the match.
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_verify.fxml"));
            Parent root = loader.load();
            FaceIdVerifyController ctrl = loader.getController();
            ctrl.setTargetUser(null); // null = scan all enrolled users
            ctrl.setOnSuccess(() -> {
                // Session is already set inside FaceIdVerifyController on match
                if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
            });
            ctrl.setOnFailure(() -> showError("Too many failed Face ID attempts. Please use password login."));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Face ID — Login");
            stage.setScene(new Scene(root, 520, 520));
            stage.setResizable(false);
            stage.showAndWait();

            // Check if login succeeded after window closed
            if (Session.isLoggedIn()) navigateAfterLogin(Session.getCurrentUser());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open camera: " + e.getMessage());
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
            stage.setTitle("Sign in with Google");
            stage.setScene(new Scene(root, 520, 620));
            stage.setResizable(false);
            stage.showAndWait();

            // If login succeeded, Session will be set by GoogleAuthController
            if (Session.isLoggedIn()) {
                navigateAfterLogin(Session.getCurrentUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Google login: " + e.getMessage());
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    @FXML
    private void handleForgotPassword() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "forgot_password.fxml", "NutriLife - Forgot Password");
    }

    @FXML
    private void handleGoRegister() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "register.fxml", "NutriLife - Register");
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
