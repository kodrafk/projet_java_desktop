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
    @FXML private WebView       recaptchaView;
    @FXML private Button        registerButton;

    // Captcha fields
    @FXML private Label         captchaLabel;
    @FXML private TextField     captchaField;
    private int captchaA;
    private int captchaB;

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

    private final UserRepository   repo             = new UserRepository();
    private final RecaptchaService recaptchaService = new RecaptchaService();

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setVisible(false);
        // reCAPTCHA temporarily disabled
        if (recaptchaView != null) { recaptchaView.setVisible(false); recaptchaView.setManaged(false); }
        if (captchaLabel != null) { captchaLabel.setVisible(false); captchaLabel.setManaged(false); }
        if (captchaField != null) { captchaField.setVisible(false); captchaField.setManaged(false); }
        if (errCaptcha   != null) { errCaptcha.setVisible(false);   errCaptcha.setManaged(false); }
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

    private void loadRecaptcha() {
        if (recaptchaView == null) return;
        try {
            String url = getClass().getResource("/html/recaptcha.html").toExternalForm();
            recaptchaView.getEngine().load(url);
        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Could not load: " + e.getMessage());
        }
    }

    private String getRecaptchaToken() {
        if (recaptchaView == null) return null;
        try {
            Object result = recaptchaView.getEngine().executeScript("getToken()");
            return result != null ? result.toString() : null;
        } catch (Exception e) { return null; }
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

        // reCAPTCHA temporarily disabled
        // String token = getRecaptchaToken();
        // if (token == null || token.isBlank()) {
        //     if (errCaptcha != null) errCaptcha.setText("Please complete the reCAPTCHA verification.");
        //     ok = false;
        // } else if (!recaptchaService.verify(token)) {
        //     if (errCaptcha != null) errCaptcha.setText("reCAPTCHA failed. Please try again.");
        //     loadRecaptcha();
        //     ok = false;
        // } else {
        //     if (errCaptcha != null) errCaptcha.setText("");
        // }

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
        // Validate all fields first
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

        // Prepare user data (but DON'T save yet!)
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

        // Open Face ID registration (will verify uniqueness BEFORE creating account)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_register.fxml"));
            Parent root = loader.load();
            FaceIdRegisterController ctrl = loader.getController();
            ctrl.setPendingUser(u);
            ctrl.setOnSuccess(() -> {
                System.out.println("[Register] Account created with Face ID");
            });
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner((Stage) emailField.getScene().getWindow());
            stage.setTitle("Face ID Registration — Verify Uniqueness");
            stage.setScene(new Scene(root, 520, 520));
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
