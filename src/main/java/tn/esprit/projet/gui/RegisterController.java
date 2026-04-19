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
import tn.esprit.projet.utils.Validator;

import java.time.LocalDate;
import java.util.Random;

public class RegisterController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private DatePicker    birthdayPicker;
    @FXML private TextField     weightField;
    @FXML private TextField     heightField;
    @FXML private Label         captchaLabel;
    @FXML private TextField     captchaField;
    @FXML private Button        registerButton;

    // Inline error labels
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errConfirm;
    @FXML private Label errFirstName;
    @FXML private Label errLastName;
    @FXML private Label errBirthday;
    @FXML private Label errWeight;
    @FXML private Label errHeight;
    @FXML private Label errCaptcha;
    @FXML private Label errorLabel;

    private int captchaA, captchaB;
    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        generateCaptcha();

        // Pre-fill from Google if data is available
        prefillFromGoogle();

        // Real-time inline validation
        emailField.focusedProperty().addListener((o, was, now) -> { if (!now) validateEmail(); });
        passwordField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(passwordField, errPassword, Validator.password(passwordField.getText()));
        });
        confirmPasswordField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(confirmPasswordField, errConfirm,
                    Validator.confirmPassword(passwordField.getText(), confirmPasswordField.getText()));
        });
        firstNameField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        });
        lastNameField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));
        });
        birthdayPicker.valueProperty().addListener((o, a, b) -> validateBirthday());
        weightField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        });
        heightField.focusedProperty().addListener((o, was, now) -> {
            if (!now) Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));
        });
    }

    /** Pre-fill fields if coming from Google OAuth */
    private void prefillFromGoogle() {
        GoogleAuthController.GoogleTempData g = null;
        if (GoogleAuthController.GoogleTempData.email != null) {
            if (emailField    != null) emailField.setText(GoogleAuthController.GoogleTempData.email);
            if (firstNameField != null) firstNameField.setText(nvl(GoogleAuthController.GoogleTempData.firstName));
            if (lastNameField  != null) lastNameField.setText(nvl(GoogleAuthController.GoogleTempData.lastName));
            // Lock email field since it came from Google
            if (emailField != null) emailField.setEditable(false);
            // Show info banner
            if (errorLabel != null) {
                errorLabel.setText("✅ Google account connected! Complete your profile to finish registration.");
                errorLabel.setStyle("-fx-text-fill:#16A34A;-fx-font-size:12px;-fx-font-weight:bold;");
                errorLabel.setVisible(true);
            }
        }
    }

    private void generateCaptcha() {
        Random rng = new Random();
        captchaA = rng.nextInt(9) + 1;
        captchaB = rng.nextInt(9) + 1;
        if (captchaLabel != null) captchaLabel.setText("What is " + captchaA + " + " + captchaB + "?");
        if (captchaField != null) captchaField.clear();
    }

    private boolean validateEmail() {
        String err = Validator.email(emailField.getText());
        if (err == null && repo.emailExistsExcluding(emailField.getText().trim(), 0))
            err = "This email is already registered.";
        return Validator.apply(emailField, errEmail, err);
    }

    private boolean validateBirthday() {
        String err = Validator.birthday(birthdayPicker.getValue());
        if (errBirthday != null) errBirthday.setText(err != null ? err : "");
        return err == null;
    }

    // ── Main register ──────────────────────────────────────────────────────────

    @FXML
    private void handleRegister() {
        boolean ok = true;
        ok &= validateEmail();
        ok &= Validator.apply(passwordField, errPassword, Validator.password(passwordField.getText()));
        ok &= Validator.apply(confirmPasswordField, errConfirm,
                Validator.confirmPassword(passwordField.getText(), confirmPasswordField.getText()));
        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));
        ok &= validateBirthday();
        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));

        // Captcha
        try {
            if (Integer.parseInt(captchaField.getText().trim()) != captchaA + captchaB) {
                if (errCaptcha != null) errCaptcha.setText("Incorrect captcha answer.");
                generateCaptcha();
                ok = false;
            } else {
                if (errCaptcha != null) errCaptcha.setText("");
            }
        } catch (NumberFormatException e) {
            if (errCaptcha != null) errCaptcha.setText("Incorrect captcha answer.");
            generateCaptcha();
            ok = false;
        }

        if (!ok) return;

        User u = new User();
        u.setEmail(emailField.getText().trim());
        u.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10)));
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setBirthday(birthdayPicker.getValue());
        u.setWeight(Double.parseDouble(weightField.getText().trim()));
        u.setHeight(Double.parseDouble(heightField.getText().trim()));
        u.setRole("ROLE_USER");
        u.setActive(true);

        // Attach Google ID if came from Google OAuth
        if (GoogleAuthController.GoogleTempData.googleId != null) {
            u.setGoogleId(GoogleAuthController.GoogleTempData.googleId);
            // Clear temp data
            GoogleAuthController.GoogleTempData.email     = null;
            GoogleAuthController.GoogleTempData.googleId  = null;
            GoogleAuthController.GoogleTempData.firstName = null;
            GoogleAuthController.GoogleTempData.lastName  = null;
        }

        repo.save(u);

        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    // ── Google Sign Up ─────────────────────────────────────────────────────────

    @FXML
    private void handleGoogleSignUp() {
        try {
            GoogleAuthController.fromRegister = true;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/google_auth.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Sign up with Google");
            stage.setScene(new Scene(root, 520, 620));
            stage.setResizable(false);
            stage.showAndWait();

            // After Google auth, pre-fill was set in GoogleTempData — re-run prefill
            prefillFromGoogle();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open Google sign-up: " + e.getMessage());
        }
    }

    // ── Face ID Sign Up ────────────────────────────────────────────────────────

    @FXML
    private void handleFaceIdSignUp() {
        // Face ID during registration: user must first fill in their details,
        // then enroll their face. We save the account first, then open camera.
        boolean ok = true;
        ok &= validateEmail();
        ok &= Validator.apply(passwordField, errPassword, Validator.password(passwordField.getText()));
        ok &= Validator.apply(confirmPasswordField, errConfirm,
                Validator.confirmPassword(passwordField.getText(), confirmPasswordField.getText()));
        ok &= Validator.apply(firstNameField, errFirstName, Validator.name(firstNameField.getText(), "First name"));
        ok &= Validator.apply(lastNameField, errLastName, Validator.name(lastNameField.getText(), "Last name"));
        ok &= validateBirthday();
        ok &= Validator.apply(weightField, errWeight, Validator.weight(weightField.getText()));
        ok &= Validator.apply(heightField, errHeight, Validator.height(heightField.getText()));

        if (!ok) {
            showError("Please fill in all required fields before enrolling Face ID.");
            return;
        }

        // Create the user first
        User u = new User();
        u.setEmail(emailField.getText().trim());
        u.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt(10)));
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setBirthday(birthdayPicker.getValue());
        u.setWeight(Double.parseDouble(weightField.getText().trim()));
        u.setHeight(Double.parseDouble(heightField.getText().trim()));
        u.setRole("ROLE_USER");
        u.setActive(true);
        repo.save(u);

        // Set session so FaceIdEnrollController can save the descriptor
        Session.login(u);

        // Open camera for enrollment
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_enroll.fxml"));
            Parent root = loader.load();
            FaceIdEnrollController ctrl = loader.getController();
            ctrl.setTargetUser(u);
            ctrl.setOnEnrolled(() -> {
                Stage stage = (Stage) emailField.getScene().getWindow();
                Nav.go(stage, "login.fxml", "NutriLife - Login");
            });
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Face ID — Enroll");
            stage.setScene(new Scene(root, 560, 620));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open camera: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login");
    }

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setStyle("-fx-text-fill:#DC2626;-fx-font-size:12px;-fx-font-weight:bold;");
            errorLabel.setVisible(true);
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
