package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;
import tn.esprit.projet.utils.UserValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class UserProfileController {

    // View labels
    @FXML private Label     lblAvatarInitials;
    @FXML private ImageView imgPhoto;
    @FXML private Label     lblFullName;
    @FXML private Label     lblEmail;
    @FXML private Label     lblRoleBadge;
    @FXML private Label     lblStatusBadge;
    @FXML private Label     lblMemberSince;
    @FXML private Label     lblAge;
    @FXML private Label     lblBmi;
    @FXML private Label     lblBmiCat;
    @FXML private Label     lblWeight;
    @FXML private Label     lblHeight;
    @FXML private Label     lblBirthday;
    @FXML private Label     lblPhone;
    @FXML private Label     lblPhoneVerified;
    @FXML private Label     lblWelcomeMsg;

    // Edit fields (only present in edit mode — null in view mode)
    @FXML private TextField  fieldFirstName;
    @FXML private TextField  fieldLastName;
    @FXML private TextField  fieldWeight;
    @FXML private TextField  fieldHeight;
    @FXML private TextField  fieldPhone;
    @FXML private TextArea   fieldWelcomeMessage;
    @FXML private Button     btnChoosePhoto;
    @FXML private Label      errFirstName;
    @FXML private Label      errLastName;
    @FXML private Label      errWeight;
    @FXML private Label      errHeight;
    @FXML private Label      errPhoto;

    private File selectedPhotoFile;
    private final UserDAO dao = new UserDAO();

    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        User u = SessionManager.getCurrentUser();
        if (u == null) return;

        // Avatar
        String init = u.getFirstName() != null && !u.getFirstName().isEmpty()
                ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?";
        if (lblAvatarInitials != null) lblAvatarInitials.setText(init);
        if (imgPhoto != null && u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                imgPhoto.setImage(new Image(f.toURI().toString()));
                imgPhoto.setVisible(true);
                if (lblAvatarInitials != null) lblAvatarInitials.setVisible(false);
            }
        }

        set(lblFullName, u.getFullName().isBlank() ? "—" : u.getFullName());
        set(lblEmail, nvl(u.getEmail()));

        // Role badge
        if (lblRoleBadge != null) {
            lblRoleBadge.setText(u.isAdmin() ? "Admin" : "User");
            lblRoleBadge.setStyle(u.isAdmin()
                    ? "-fx-background-color:#EFF6FF;-fx-text-fill:#1D4ED8;-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;"
                    : "-fx-background-color:#DCFCE7;-fx-text-fill:#166534;-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;");
        }

        // Status badge
        if (lblStatusBadge != null) {
            lblStatusBadge.setText(u.isActive() ? "Active" : "Inactive");
            lblStatusBadge.setStyle(u.isActive()
                    ? "-fx-background-color:#DCFCE7;-fx-text-fill:#166534;-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;"
                    : "-fx-background-color:#FEE2E2;-fx-text-fill:#991B1B;-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;");
        }

        if (lblMemberSince != null && u.getCreatedAt() != null)
            lblMemberSince.setText("Member since " + u.getCreatedAt().format(DT_FMT));

        // Stats
        set(lblAge,    u.getAge() > 0 ? String.valueOf(u.getAge()) : "—");
        set(lblWeight, u.getWeight() > 0 ? String.format("%.1f", u.getWeight()) : "—");
        set(lblHeight, u.getHeight() > 0 ? String.format("%.0f", u.getHeight()) : "—");

        if (u.getWeight() > 0 && u.getHeight() > 0) {
            double bmi = u.getBmi();
            set(lblBmi, String.format("%.1f", bmi));
            set(lblBmiCat, bmiCategory(bmi));
        } else {
            set(lblBmi, "—"); set(lblBmiCat, "—");
        }

        set(lblBirthday, u.getBirthday() != null ? u.getBirthday().format(D_FMT) : "—");
        set(lblPhone, u.getPhoneNumber() != null && !u.getPhoneNumber().isBlank()
                ? u.getPhoneNumber() : "—");

        if (lblPhoneVerified != null) {
            if (u.getPhoneNumber() != null && !u.getPhoneNumber().isBlank()) {
                lblPhoneVerified.setText(u.isPhoneVerified() ? "✓ Verified" : "✗ Not verified");
                lblPhoneVerified.setStyle(u.isPhoneVerified()
                        ? "-fx-text-fill: #16A34A; -fx-font-size: 11px; -fx-font-weight: bold;"
                        : "-fx-text-fill: #DC2626; -fx-font-size: 11px;");
            } else {
                lblPhoneVerified.setText("");
            }
        }

        set(lblWelcomeMsg, u.getWelcomeMessage() != null && !u.getWelcomeMessage().isBlank()
                ? "\"" + u.getWelcomeMessage() + "\"" : "—");

        // Edit fields (if in edit mode)
        if (fieldFirstName != null) fieldFirstName.setText(nvl(u.getFirstName()));
        if (fieldLastName  != null) fieldLastName.setText(nvl(u.getLastName()));
        if (fieldWeight    != null) fieldWeight.setText(u.getWeight() > 0 ? String.valueOf(u.getWeight()) : "");
        if (fieldHeight    != null) fieldHeight.setText(u.getHeight() > 0 ? String.valueOf(u.getHeight()) : "");
        if (fieldPhone     != null) fieldPhone.setText(nvl(u.getPhoneNumber()));
        if (fieldWelcomeMessage != null) fieldWelcomeMessage.setText(nvl(u.getWelcomeMessage()));
    }

    // ── Action buttons ─────────────────────────────────────────────────────────

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_profile.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root, 560, 620));
            stage.setResizable(false);
            stage.showAndWait();
            loadProfile(); // refresh after edit
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void handleChangePassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/change_password.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Change Password");
            stage.setScene(new Scene(root, 420, 340));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML
    private void handleDeactivate() {
        if (!AlertUtil.confirm("Deactivate Account",
                "Are you sure you want to deactivate your account?\n\nYou will be logged out and cannot log in until an administrator reactivates it."))
            return;
        User u = SessionManager.getCurrentUser();
        if (u != null) {
            dao.toggleActive(u.getId(), false);
            SessionManager.logout();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Stage stage = (Stage) lblFullName.getScene().getWindow();
                stage.setScene(new Scene(root, 1100, 720));
                stage.setTitle("NutriLife - Login");
                stage.setMaximized(false);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    @FXML
    private void handleChoosePhoto() {
        if (btnChoosePhoto == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Profile Photo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
        File file = fc.showOpenDialog(btnChoosePhoto.getScene().getWindow());
        if (file == null) return;
        if (file.length() > 2L * 1024 * 1024) {
            if (errPhoto != null) errPhoto.setText("File exceeds 2 MB limit."); return;
        }
        if (errPhoto != null) errPhoto.setText("");
        selectedPhotoFile = file;
        if (imgPhoto != null) {
            imgPhoto.setImage(new Image(file.toURI().toString()));
            imgPhoto.setVisible(true);
            if (lblAvatarInitials != null) lblAvatarInitials.setVisible(false);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateEdit()) return;
        User u = SessionManager.getCurrentUser();
        u.setFirstName(fieldFirstName.getText().trim());
        u.setLastName(fieldLastName.getText().trim());
        if (!fieldWeight.getText().isBlank()) u.setWeight(Float.parseFloat(fieldWeight.getText().trim()));
        if (!fieldHeight.getText().isBlank()) u.setHeight(Float.parseFloat(fieldHeight.getText().trim()));
        u.setPhoneNumber(fieldPhone.getText().trim().isBlank() ? null : fieldPhone.getText().trim());
        if (fieldWelcomeMessage != null) u.setWelcomeMessage(fieldWelcomeMessage.getText().trim());

        if (selectedPhotoFile != null) {
            String fn = savePhoto(selectedPhotoFile);
            if (fn != null) u.setPhotoFilename(fn);
        }
        u.setPassword(null); // don't change password here

        if (dao.update(u)) {
            Stage owner = (Stage) fieldFirstName.getScene().getWindow();
            Toast.show(owner, "Profile updated successfully.", Toast.Type.SUCCESS);
            closeStage();
        } else {
            if (errFirstName != null) errFirstName.setText("Save failed. Please try again.");
        }
    }

    private boolean validateEdit() {
        boolean ok = true;
        if (fieldFirstName != null) {
            String err = UserValidator.validateName(fieldFirstName.getText(), "First name");
            if (err != null) { if (errFirstName != null) errFirstName.setText(err); ok = false; }
            else { if (errFirstName != null) errFirstName.setText(""); }
        }
        if (fieldLastName != null) {
            String err = UserValidator.validateName(fieldLastName.getText(), "Last name");
            if (err != null) { if (errLastName != null) errLastName.setText(err); ok = false; }
            else { if (errLastName != null) errLastName.setText(""); }
        }
        if (fieldWeight != null && !fieldWeight.getText().isBlank()) {
            String err = UserValidator.validateWeight(fieldWeight.getText());
            if (err != null) { if (errWeight != null) errWeight.setText(err); ok = false; }
            else { if (errWeight != null) errWeight.setText(""); }
        }
        if (fieldHeight != null && !fieldHeight.getText().isBlank()) {
            String err = UserValidator.validateHeight(fieldHeight.getText());
            if (err != null) { if (errHeight != null) errHeight.setText(err); ok = false; }
            else { if (errHeight != null) errHeight.setText(""); }
        }
        return ok;
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

    private String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }
    private String nvl(String s) { return s != null ? s : ""; }

    @FXML private void handleClose() { closeStage(); }
    private void closeStage() {
        Stage s = (Stage) (lblFullName != null ? lblFullName : fieldFirstName).getScene().getWindow();
        s.close();
    }
}
