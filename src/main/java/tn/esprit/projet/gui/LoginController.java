package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.utils.PasswordUtil;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.UserValidator;

public class LoginController {

    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private TextField     fieldPasswordVisible;   // plain text overlay
    @FXML private Button        btnTogglePassword;
    @FXML private Label         errEmail;
    @FXML private Label         errPassword;
    @FXML private Label         errGeneral;

    private boolean passwordVisible = false;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Keep both fields in sync
        fieldPasswordVisible.textProperty().bindBidirectional(
                new javafx.beans.property.SimpleStringProperty() {
                    @Override public String get() { return fieldPassword.getText(); }
                    @Override public void set(String v) { fieldPassword.setText(v); }
                });
        // Simpler: just sync manually
        fieldPasswordVisible.textProperty().addListener((o, a, b) -> {
            if (passwordVisible) fieldPassword.setText(b);
        });
        fieldPassword.textProperty().addListener((o, a, b) -> {
            if (!passwordVisible) fieldPasswordVisible.setText(b);
        });
        fieldPasswordVisible.setVisible(false);
        fieldPasswordVisible.setManaged(false);
    }

    @FXML
    private void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            fieldPasswordVisible.setText(fieldPassword.getText());
            fieldPasswordVisible.setVisible(true);
            fieldPasswordVisible.setManaged(true);
            fieldPassword.setVisible(false);
            fieldPassword.setManaged(false);
            btnTogglePassword.setText("🙈");
        } else {
            fieldPassword.setText(fieldPasswordVisible.getText());
            fieldPassword.setVisible(true);
            fieldPassword.setManaged(true);
            fieldPasswordVisible.setVisible(false);
            fieldPasswordVisible.setManaged(false);
            btnTogglePassword.setText("👁");
        }
    }

    private String getPasswordText() {
        return passwordVisible ? fieldPasswordVisible.getText() : fieldPassword.getText();
    }

    @FXML
    private void handleLogin() {
        clearErrors();

        String email    = fieldEmail.getText().trim();
        String password = getPasswordText();
        boolean ok      = true;

        String emailErr = UserValidator.validateEmail(email);
        if (emailErr != null) {
            errEmail.setText(emailErr);
            fieldEmail.setStyle(UserValidator.getErrorStyle());
            ok = false;
        }
        if (password.isBlank()) {
            errPassword.setText("Password is required.");
            fieldPassword.setStyle(UserValidator.getErrorStyle());
            fieldPasswordVisible.setStyle(UserValidator.getErrorStyle());
            ok = false;
        }
        if (!ok) return;

        User user = userDAO.findByEmail(email);
        if (user == null || !PasswordUtil.checkPassword(password, user.getPassword())) {
            errGeneral.setText("Invalid email or password.");
            return;
        }
        if (!user.isActive()) {
            errGeneral.setText("Your account has been deactivated. Contact an administrator.");
            return;
        }

        SessionManager.setCurrentUser(user);
        EthicalPointsManager.reset(); // reset le cache pour ce nouveau user
        EthicalPointsManager.loadFromDatabase(); // charger depuis BDD
        
        if ("ROLE_ADMIN".equals(user.getRoles())) {
            navigate("/fxml/admin_layout.fxml", "NutriLife - Admin Panel", 1320, 780, true);
        } else {
            navigate("/fxml/main_layout.fxml", "NutriLife - Dashboard", 1280, 760, true);
        }
    }

    @FXML
    private void handleGoRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) fieldEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("NutriLife - Register");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void navigate(String fxml, String title, int w, int h, boolean maximize) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) fieldEmail.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.setMaximized(maximize);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void clearErrors() {
        errEmail.setText(""); errPassword.setText(""); errGeneral.setText("");
        fieldEmail.setStyle(UserValidator.getNeutralStyle());
        fieldPassword.setStyle(UserValidator.getNeutralStyle());
        fieldPasswordVisible.setStyle(UserValidator.getNeutralStyle());
    }
}
