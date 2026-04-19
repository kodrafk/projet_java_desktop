package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.PasswordUtil;
import tn.esprit.projet.utils.UserValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class UserFormController {

    @FXML private Label         lblTitle;
    @FXML private Label         lblPasswordHint;
    @FXML private TextField     fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private CheckBox      chkRoleUser;
    @FXML private CheckBox      chkRoleAdmin;
    @FXML private CheckBox      chkActive;
    @FXML private TextField     fieldFirstName;
    @FXML private TextField     fieldLastName;
    @FXML private ComboBox<Integer> cbBirthDay;
    @FXML private ComboBox<String>  cbBirthMonth;
    @FXML private ComboBox<Integer> cbBirthYear;
    @FXML private TextField     fieldWeight;
    @FXML private TextField     fieldHeight;
    @FXML private ImageView     photoPreview;
    @FXML private Label         photoInitials;
    @FXML private Button        btnChoosePhoto;
    @FXML private Button        btnSave;

    @FXML private Label errEmail;
    @FXML private Label errPassword;
    @FXML private Label errRoles;
    @FXML private Label errFirstName;
    @FXML private Label errLastName;
    @FXML private Label errBirthday;
    @FXML private Label errWeight;
    @FXML private Label errHeight;
    @FXML private Label errPhoto;

    private User     editUser;
    private File     selectedPhotoFile;
    private Runnable onSaved;
    private final UserDAO dao = new UserDAO();

    // ── Called by UserListController ───────────────────────────────────────────
    public void setUser(User user) {
        this.editUser = user;
        initBirthdayPicker();
        if (user != null) {
            lblTitle.setText("Edit User");
            lblPasswordHint.setText("Password (leave blank to keep current)");
            populate(user);
        } else {
            lblTitle.setText("Add User");
            lblPasswordHint.setText("Password *");
            chkRoleUser.setSelected(true);
            chkActive.setSelected(true);
        }
        setupRealtimeValidation();
        updateSaveButton();
    }

    public void setOnSaved(Runnable r) { this.onSaved = r; }

    // ── Birthday picker ────────────────────────────────────────────────────────
    private void initBirthdayPicker() {
        List<String> months = new ArrayList<>();
        for (Month m : Month.values())
            months.add(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        cbBirthMonth.setItems(FXCollections.observableArrayList(months));

        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 18; y >= currentYear - 100; y--) years.add(y);
        cbBirthYear.setItems(FXCollections.observableArrayList(years));

        cbBirthMonth.valueProperty().addListener((o, a, b) -> rebuildDays());
        cbBirthYear.valueProperty().addListener((o, a, b)  -> rebuildDays());
        rebuildDays();
    }

    private void rebuildDays() {
        Integer sel = cbBirthDay.getValue();
        int month = cbBirthMonth.getValue() != null
                ? cbBirthMonth.getItems().indexOf(cbBirthMonth.getValue()) + 1 : 1;
        int year  = cbBirthYear.getValue() != null
                ? cbBirthYear.getValue() : LocalDate.now().getYear();
        int max = YearMonth.of(year, month).lengthOfMonth();
        List<Integer> days = new ArrayList<>();
        for (int d = 1; d <= max; d++) days.add(d);
        cbBirthDay.setItems(FXCollections.observableArrayList(days));
        if (sel != null && sel <= max) cbBirthDay.setValue(sel);
        else cbBirthDay.setValue(null);
    }

    private LocalDate getBirthday() {
        if (cbBirthDay.getValue() == null || cbBirthMonth.getValue() == null || cbBirthYear.getValue() == null)
            return null;
        int month = cbBirthMonth.getItems().indexOf(cbBirthMonth.getValue()) + 1;
        return LocalDate.of(cbBirthYear.getValue(), month, cbBirthDay.getValue());
    }

    private void setBirthday(LocalDate date) {
        if (date == null) return;
        cbBirthYear.setValue(date.getYear());
        cbBirthMonth.setValue(date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        cbBirthDay.setValue(date.getDayOfMonth());
    }

    private static final String CB_ERR =
            "-fx-background-color: #FFF5F5; -fx-border-color: #EF4444; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;";
    private static final String CB_OK =
            "-fx-background-color: #F8FAFC; -fx-border-color: #C8E6C9; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand;";

    private boolean applyBirthday() {
        String err = UserValidator.validateBirthday(getBirthday());
        errBirthday.setText(err != null ? err : "");
        String s = err != null ? CB_ERR : CB_OK;
        cbBirthDay.setStyle(s); cbBirthMonth.setStyle(s); cbBirthYear.setStyle(s);
        return err == null;
    }

    // ── Real-time validation setup ─────────────────────────────────────────────
    private void setupRealtimeValidation() {
        fieldFirstName.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldFirstName, errFirstName,
                    UserValidator.validateName(fieldFirstName.getText(), "First name"));
            updateSaveButton();
        });
        fieldLastName.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldLastName, errLastName,
                    UserValidator.validateName(fieldLastName.getText(), "Last name"));
            updateSaveButton();
        });
        fieldEmail.focusedProperty().addListener((o, was, now) -> {
            if (!now) {
                String err = UserValidator.validateEmail(fieldEmail.getText());
                if (err == null)
                    err = dao.emailExists(fieldEmail.getText().trim(),
                            editUser != null ? editUser.getId() : 0)
                            ? "Email is already in use." : null;
                applyField(fieldEmail, errEmail, err);
            }
            updateSaveButton();
        });
        fieldPassword.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldPassword, errPassword,
                    UserValidator.validatePasswordOptional(fieldPassword.getText()));
            updateSaveButton();
        });
        cbBirthDay.valueProperty().addListener((o, a, b)   -> { applyBirthday(); updateSaveButton(); });
        cbBirthMonth.valueProperty().addListener((o, a, b) -> { applyBirthday(); updateSaveButton(); });
        cbBirthYear.valueProperty().addListener((o, a, b)  -> { applyBirthday(); updateSaveButton(); });
        fieldWeight.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldWeight, errWeight,
                    UserValidator.validateWeight(fieldWeight.getText()));
            updateSaveButton();
        });
        fieldHeight.focusedProperty().addListener((o, was, now) -> {
            if (!now) applyField(fieldHeight, errHeight,
                    UserValidator.validateHeight(fieldHeight.getText()));
            updateSaveButton();
        });
        chkRoleUser.selectedProperty().addListener((o, a, b) -> {
            validateRoles(); updateSaveButton();
        });
        chkRoleAdmin.selectedProperty().addListener((o, a, b) -> {
            validateRoles(); updateSaveButton();
        });

        // Text change listeners for button state
        fieldFirstName.textProperty().addListener((o, a, b) -> updateSaveButton());
        fieldLastName.textProperty().addListener((o, a, b) -> updateSaveButton());
        fieldEmail.textProperty().addListener((o, a, b) -> updateSaveButton());
        fieldWeight.textProperty().addListener((o, a, b) -> updateSaveButton());
        fieldHeight.textProperty().addListener((o, a, b) -> updateSaveButton());
    }

    // ── Pre-fill form for edit ─────────────────────────────────────────────────
    private void populate(User u) {
        fieldEmail.setText(nvl(u.getEmail()));
        chkActive.setSelected(u.isActive());
        chkRoleAdmin.setSelected("ROLE_ADMIN".equals(u.getRoles()));
        chkRoleUser.setSelected(!"ROLE_ADMIN".equals(u.getRoles()));
        fieldFirstName.setText(nvl(u.getFirstName()));
        fieldLastName.setText(nvl(u.getLastName()));
        setBirthday(u.getBirthday());
        fieldWeight.setText(u.getWeight() > 0 ? String.valueOf(u.getWeight()) : "");
        fieldHeight.setText(u.getHeight() > 0 ? String.valueOf(u.getHeight()) : "");

        if (u.getFirstName() != null && !u.getFirstName().isEmpty())
            photoInitials.setText(String.valueOf(u.getFirstName().charAt(0)).toUpperCase());

        if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                photoPreview.setImage(new Image(f.toURI().toString()));
                photoPreview.setVisible(true);
                photoInitials.setVisible(false);
            }
        }
    }

    // ── Photo chooser ──────────────────────────────────────────────────────────
    @FXML
    private void handleChoosePhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Profile Photo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File file = fc.showOpenDialog(btnChoosePhoto.getScene().getWindow());
        if (file == null) return;
        if (file.length() > 2L * 1024 * 1024) {
            errPhoto.setText("File exceeds 2 MB limit."); return;
        }
        errPhoto.setText("");
        selectedPhotoFile = file;
        photoPreview.setImage(new Image(file.toURI().toString()));
        photoPreview.setVisible(true);
        photoInitials.setVisible(false);
    }

    // ── Save ───────────────────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        boolean ok = true;

        ok &= applyField(fieldFirstName, errFirstName,
                UserValidator.validateName(fieldFirstName.getText(), "First name"));
        ok &= applyField(fieldLastName, errLastName,
                UserValidator.validateName(fieldLastName.getText(), "Last name"));

        String emailErr = UserValidator.validateEmail(fieldEmail.getText());
        if (emailErr == null && dao.emailExists(fieldEmail.getText().trim(),
                editUser != null ? editUser.getId() : 0))
            emailErr = "Email is already in use.";
        ok &= applyField(fieldEmail, errEmail, emailErr);

        // Password: required on create, optional on edit
        String pwdErr = (editUser == null)
                ? UserValidator.validatePassword(fieldPassword.getText())
                : UserValidator.validatePasswordOptional(fieldPassword.getText());
        ok &= applyField(fieldPassword, errPassword, pwdErr);

        ok &= validateRoles();
        ok &= applyBirthday();
        ok &= applyField(fieldWeight, errWeight,
                UserValidator.validateWeight(fieldWeight.getText()));
        ok &= applyField(fieldHeight, errHeight,
                UserValidator.validateHeight(fieldHeight.getText()));

        if (!ok) return;

        User u = editUser != null ? editUser : new User();
        u.setEmail(fieldEmail.getText().trim());
        u.setActive(chkActive.isSelected());
        u.setRoles(chkRoleAdmin.isSelected() ? "ROLE_ADMIN" : "ROLE_USER");
        String pwd = fieldPassword.getText();
        if (!pwd.isBlank()) u.setPassword(PasswordUtil.hashPassword(pwd));

        u.setFirstName(fieldFirstName.getText().trim());
        u.setLastName(fieldLastName.getText().trim());
        u.setBirthday(getBirthday());
        u.setWeight(Double.parseDouble(fieldWeight.getText().trim()));
        u.setHeight(Double.parseDouble(fieldHeight.getText().trim()));

        if (selectedPhotoFile != null) {
            String fn = savePhoto(selectedPhotoFile);
            if (fn != null) u.setPhotoFilename(fn);
        }

        boolean saved = (editUser != null) ? dao.update(u) : dao.create(u);
        if (saved) {
            if (onSaved != null) onSaved.run();
            closeStage();
        } else {
            AlertUtil.show(AlertUtil.Type.ERROR, "Save Failed",
                    "Could not save user. Please check your data and try again.\n\nError: " + dao.getLastError());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean applyField(Control field, Label errLabel, String error) {
        if (error != null) {
            errLabel.setText(error);
            field.setStyle(UserValidator.getErrorStyle());
            return false;
        } else {
            errLabel.setText("");
            field.setStyle(UserValidator.getOkStyle());
            return true;
        }
    }

    private boolean validateRoles() {
        boolean selected = chkRoleUser.isSelected() || chkRoleAdmin.isSelected();
        errRoles.setText(selected ? "" : "Select at least one role.");
        return selected;
    }

    private void updateSaveButton() {
        if (btnSave == null) return;
        boolean hasRequired =
                !fieldFirstName.getText().isBlank() &&
                !fieldLastName.getText().isBlank() &&
                !fieldEmail.getText().isBlank() &&
                !fieldWeight.getText().isBlank() &&
                !fieldHeight.getText().isBlank() &&
                getBirthday() != null &&
                (chkRoleUser.isSelected() || chkRoleAdmin.isSelected());
        btnSave.setDisable(!hasRequired);
    }

    private String savePhoto(File src) {
        try {
            Path dir = Paths.get("uploads/profiles");
            Files.createDirectories(dir);
            String ext = src.getName().substring(src.getName().lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Files.copy(src.toPath(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private String nvl(String s) { return s != null ? s : ""; }

    @FXML private void handleCancel() { closeStage(); }
    private void closeStage() { ((Stage) fieldEmail.getScene().getWindow()).close(); }
}
