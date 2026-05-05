package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.PasswordUtil;
import tn.esprit.projet.utils.UserValidator;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegisterController {

    @FXML private TextField     fieldFirstName;
    @FXML private TextField     fieldLastName;
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirmPassword;
    @FXML private TextField     fieldPasswordVisible;
    @FXML private TextField     fieldConfirmVisible;
    @FXML private Button        btnTogglePwd;
    @FXML private Button        btnToggleConfirm;

    private boolean pwdVisible     = false;
    private boolean confirmVisible = false;
    @FXML private TextField     fieldWeight;
    @FXML private TextField     fieldHeight;
    @FXML private TextField     fieldPhone;
    @FXML private Button        btnSubmit;

    // Birthday combo boxes
    @FXML private ComboBox<Integer> cbBirthDay;
    @FXML private ComboBox<String>  cbBirthMonth;
    @FXML private ComboBox<Integer> cbBirthYear;

    @FXML private Label errFirstName;
    @FXML private Label errLastName;
    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errConfirmPassword;
    @FXML private Label errBirthday;
    @FXML private Label errWeight;
    @FXML private Label errHeight;
    @FXML private Label errPhone;
    @FXML private Label errGeneral;

    private final UserDAO dao = new UserDAO();

    private static final String CB_STYLE_OK =
            "-fx-background-color: #F0F7F0; -fx-border-color: #C8E6C9; " +
            "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13px; -fx-cursor: hand;";
    private static final String CB_STYLE_ERR =
            "-fx-background-color: #FFF5F5; -fx-border-color: #EF4444; " +
            "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        // Hide visible fields initially
        if (fieldPasswordVisible != null) { fieldPasswordVisible.setVisible(false); fieldPasswordVisible.setManaged(false); }
        if (fieldConfirmVisible  != null) { fieldConfirmVisible.setVisible(false);  fieldConfirmVisible.setManaged(false); }
        initBirthdayPicker();
        setupRealtimeValidation();
        updateSubmitButton();
    }

    @FXML private void handleTogglePassword() {
        pwdVisible = !pwdVisible;
        if (pwdVisible) {
            fieldPasswordVisible.setText(fieldPassword.getText());
            fieldPasswordVisible.setVisible(true); fieldPasswordVisible.setManaged(true);
            fieldPassword.setVisible(false); fieldPassword.setManaged(false);
            if (btnTogglePwd != null) btnTogglePwd.setText("🙈");
        } else {
            fieldPassword.setText(fieldPasswordVisible.getText());
            fieldPassword.setVisible(true); fieldPassword.setManaged(true);
            fieldPasswordVisible.setVisible(false); fieldPasswordVisible.setManaged(false);
            if (btnTogglePwd != null) btnTogglePwd.setText("👁");
        }
    }

    @FXML private void handleToggleConfirm() {
        confirmVisible = !confirmVisible;
        if (confirmVisible) {
            fieldConfirmVisible.setText(fieldConfirmPassword.getText());
            fieldConfirmVisible.setVisible(true); fieldConfirmVisible.setManaged(true);
            fieldConfirmPassword.setVisible(false); fieldConfirmPassword.setManaged(false);
            if (btnToggleConfirm != null) btnToggleConfirm.setText("🙈");
        } else {
            fieldConfirmPassword.setText(fieldConfirmVisible.getText());
            fieldConfirmPassword.setVisible(true); fieldConfirmPassword.setManaged(true);
            fieldConfirmVisible.setVisible(false); fieldConfirmVisible.setManaged(false);
            if (btnToggleConfirm != null) btnToggleConfirm.setText("👁");
        }
    }

    private String getPwd()     { return pwdVisible     ? fieldPasswordVisible.getText() : fieldPassword.getText(); }
    private String getConfirm() { return confirmVisible ? fieldConfirmVisible.getText()  : fieldConfirmPassword.getText(); }

    // ── Birthday picker setup ──────────────────────────────────────────────────
    private void initBirthdayPicker() {
        // Months
        List<String> months = new ArrayList<>();
        for (Month m : Month.values())
            months.add(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        cbBirthMonth.setItems(FXCollections.observableArrayList(months));

        // Years: max 18 years ago → 100 years ago
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 18; y >= currentYear - 100; y--) years.add(y);
        cbBirthYear.setItems(FXCollections.observableArrayList(years));

        // Days: rebuild when month/year changes
        cbBirthMonth.valueProperty().addListener((o, a, b) -> rebuildDays());
        cbBirthYear.valueProperty().addListener((o, a, b)  -> rebuildDays());
        rebuildDays();

        // Validate on change
        cbBirthDay.valueProperty().addListener((o, a, b)   -> { validateBirthday(); updateSubmitButton(); });
        cbBirthMonth.valueProperty().addListener((o, a, b) -> { validateBirthday(); updateSubmitButton(); });
        cbBirthYear.valueProperty().addListener((o, a, b)  -> { validateBirthday(); updateSubmitButton(); });
    }

    private void rebuildDays() {
        Integer selectedDay = cbBirthDay.getValue();
        int month = cbBirthMonth.getValue() != null
                ? cbBirthMonth.getItems().indexOf(cbBirthMonth.getValue()) + 1 : 1;
        int year  = cbBirthYear.getValue() != null
                ? cbBirthYear.getValue() : LocalDate.now().getYear();
        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        List<Integer> days = new ArrayList<>();
        for (int d = 1; d <= maxDay; d++) days.add(d);
        cbBirthDay.setItems(FXCollections.observableArrayList(days));
        if (selectedDay != null && selectedDay <= maxDay) cbBirthDay.setValue(selectedDay);
        else cbBirthDay.setValue(null);
    }

    private LocalDate getBirthday() {
        if (cbBirthDay.getValue() == null || cbBirthMonth.getValue() == null || cbBirthYear.getValue() == null)
            return null;
        int month = cbBirthMonth.getItems().indexOf(cbBirthMonth.getValue()) + 1;
        return LocalDate.of(cbBirthYear.getValue(), month, cbBirthDay.getValue());
    }

    private void validateBirthday() {
        String err = UserValidator.validateBirthday(getBirthday());
        errBirthday.setText(err != null ? err : "");
        String style = err != null ? CB_STYLE_ERR : CB_STYLE_OK;
        cbBirthDay.setStyle(style);
        cbBirthMonth.setStyle(style);
        cbBirthYear.setStyle(style);
    }

    // ── Real-time validation ───────────────────────────────────────────────────
    private void setupRealtimeValidation() {
        fieldFirstName.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldFirstName, errFirstName,
                    UserValidator.validateName(fieldFirstName.getText(), "First name"));
            updateSubmitButton();
        });
        fieldLastName.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldLastName, errLastName,
                    UserValidator.validateName(fieldLastName.getText(), "Last name"));
            updateSubmitButton();
        });
        fieldEmail.focusedProperty().addListener((o, was, now) -> {
            if (!now) {
                String err = UserValidator.validateEmail(fieldEmail.getText());
                if (err == null && dao.emailExists(fieldEmail.getText().trim(), 0))
                    err = "Email is already in use.";
                applyField(fieldEmail, errEmail, err);
            }
            updateSubmitButton();
        });
        fieldPassword.focusedProperty().addListener((o, was, now) -> {
            if (!now) {
                applyField(fieldPassword, errPassword,
                        UserValidator.validatePassword(getPwd()));
                if (!getConfirm().isBlank())
                    applyField(fieldConfirmPassword, errConfirmPassword,
                            UserValidator.validateConfirmPassword(getPwd(), getConfirm()));
            }
            updateSubmitButton();
        });
        fieldConfirmPassword.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldConfirmPassword, errConfirmPassword,
                    UserValidator.validateConfirmPassword(getPwd(), getConfirm()));
            updateSubmitButton();
        });
        fieldWeight.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldWeight, errWeight,
                    UserValidator.validateWeight(fieldWeight.getText()));
            updateSubmitButton();
        });
        fieldHeight.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldHeight, errHeight,
                    UserValidator.validateHeight(fieldHeight.getText()));
            updateSubmitButton();
        });
        fieldPhone.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldPhone, errPhone,
                    UserValidator.validatePhone(fieldPhone.getText()));
        });

        fieldFirstName.textProperty().addListener((o, a, b) -> updateSubmitButton());
        fieldLastName.textProperty().addListener((o, a, b)  -> updateSubmitButton());
        fieldEmail.textProperty().addListener((o, a, b)     -> updateSubmitButton());
        fieldPassword.textProperty().addListener((o, a, b)  -> updateSubmitButton());
        fieldConfirmPassword.textProperty().addListener((o, a, b) -> updateSubmitButton());
        fieldWeight.textProperty().addListener((o, a, b)    -> updateSubmitButton());
        fieldHeight.textProperty().addListener((o, a, b)    -> updateSubmitButton());
    }

    // ── Submit ─────────────────────────────────────────────────────────────────
    @FXML
    private void handleRegister() {
        boolean ok = true;

        ok &= applyField(fieldFirstName, errFirstName,
                UserValidator.validateName(fieldFirstName.getText(), "First name"));
        ok &= applyField(fieldLastName, errLastName,
                UserValidator.validateName(fieldLastName.getText(), "Last name"));

        String emailErr = UserValidator.validateEmail(fieldEmail.getText());
        if (emailErr == null && dao.emailExists(fieldEmail.getText().trim(), 0))
            emailErr = "Email is already in use.";
        ok &= applyField(fieldEmail, errEmail, emailErr);

        ok &= applyField(fieldPassword, errPassword,
                UserValidator.validatePassword(getPwd()));
        ok &= applyField(fieldConfirmPassword, errConfirmPassword,
                UserValidator.validateConfirmPassword(getPwd(), getConfirm()));

        // Birthday
        String bdErr = UserValidator.validateBirthday(getBirthday());
        errBirthday.setText(bdErr != null ? bdErr : "");
        String bdStyle = bdErr != null ? CB_STYLE_ERR : CB_STYLE_OK;
        cbBirthDay.setStyle(bdStyle); cbBirthMonth.setStyle(bdStyle); cbBirthYear.setStyle(bdStyle);
        if (bdErr != null) ok = false;

        ok &= applyField(fieldWeight, errWeight,
                UserValidator.validateWeight(fieldWeight.getText()));
        ok &= applyField(fieldHeight, errHeight,
                UserValidator.validateHeight(fieldHeight.getText()));
        ok &= applyField(fieldPhone, errPhone,
                UserValidator.validatePhone(fieldPhone.getText()));

        if (!ok) return;

        User u = new User();
        u.setFirstName(fieldFirstName.getText().trim());
        u.setLastName(fieldLastName.getText().trim());
        u.setEmail(fieldEmail.getText().trim());
        u.setPassword(PasswordUtil.hashPassword(getPwd()));
        u.setRoles("ROLE_USER");
        u.setActive(true);
        u.setBirthday(getBirthday());
        u.setWeight(Float.parseFloat(fieldWeight.getText().trim()));
        u.setHeight(Float.parseFloat(fieldHeight.getText().trim()));
        String phone = fieldPhone.getText().trim();
        u.setPhoneNumber(phone.isBlank() ? null : phone);

        if (!dao.create(u)) {
            AlertUtil.show(AlertUtil.Type.ERROR, "Registration Failed",
                    "Could not create your account.\n\nError: " + dao.getLastError());
            return;
        }

        AlertUtil.show(AlertUtil.Type.SUCCESS, "Account Created",
                "Your account has been created successfully!\nPlease sign in to continue.");
        SessionManager.setCurrentUser(u);
        try {
            // Redirect to login with success — per spec
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) fieldEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("NutriLife - Login");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void handleGoLogin(javafx.event.ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) fieldEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("NutriLife - Login");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private boolean applyField(Control field, Label errLabel, String error) {
        if (error != null) {
            errLabel.setText(error);
            field.setStyle(UserValidator.getErrorStyle());
            return false;
        }
        errLabel.setText("");
        field.setStyle(UserValidator.getOkStyle());
        return true;
    }

    private void updateSubmitButton() {
        if (btnSubmit == null) return;
        boolean ready =
                !fieldFirstName.getText().isBlank() &&
                !fieldLastName.getText().isBlank() &&
                !fieldEmail.getText().isBlank() &&
                !fieldPassword.getText().isBlank() &&
                !fieldConfirmPassword.getText().isBlank() &&
                cbBirthDay.getValue() != null &&
                cbBirthMonth.getValue() != null &&
                cbBirthYear.getValue() != null &&
                !fieldWeight.getText().isBlank() &&
                !fieldHeight.getText().isBlank();
        btnSubmit.setDisable(!ready);
    }
}
