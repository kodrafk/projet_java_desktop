package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfileController {

    @FXML private ImageView photoView;
    @FXML private Label     lblFullName;
    @FXML private Label     lblEmail;
    @FXML private Label     lblBirthday;
    @FXML private Label     lblAge;
    @FXML private Label     lblWeight;
    @FXML private Label     lblHeight;
    @FXML private Label     lblBmi;
    @FXML private Label     lblWelcomeMessage;
    @FXML private Label     lblStatus;
    @FXML private Label     lblMemberSince;
    @FXML private Label     lblFaceId;
    @FXML private Label     lblRole;
    @FXML private Label     lblAvatarInitials;

    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTFULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository repo = new UserRepository();

    @FXML
    public void initialize() {
        if (!Session.isLoggedIn()) {
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
            return;
        }
        loadProfile();
    }

    public void loadProfile() {
        // Refresh from DB
        User u = repo.findById(Session.getCurrentUser().getId());
        if (u != null) Session.login(u);
        u = Session.getCurrentUser();

        set(lblFullName, u.getFullName());
        set(lblEmail, u.getEmail());
        set(lblBirthday, u.getBirthday() != null ? u.getBirthday().format(D_FMT) : "—");
        set(lblAge, u.getAge() > 0 ? u.getAge() + " years old" : "—");
        set(lblWeight, u.getWeight() > 0 ? u.getWeight() + " kg" : "—");
        set(lblHeight, u.getHeight() > 0 ? u.getHeight() + " cm" : "—");

        if (u.getWeight() > 0 && u.getHeight() > 0) {
            set(lblBmi, String.format("%.2f (%s)", u.getBmi(), u.getBmiCategory()));
        } else {
            set(lblBmi, "—");
        }

        set(lblWelcomeMessage, u.getWelcomeMessage() != null && !u.getWelcomeMessage().isBlank()
                ? u.getWelcomeMessage() : "");

        if (lblStatus != null) {
            lblStatus.setText(u.isActive() ? "Active" : "Inactive");
            lblStatus.setStyle(u.isActive()
                    ? "-fx-text-fill:#16A34A;-fx-font-weight:bold;"
                    : "-fx-text-fill:#DC2626;-fx-font-weight:bold;");
        }

        set(lblMemberSince, u.getCreatedAt() != null ? u.getCreatedAt().format(DT_FMT) : "—");

        if (lblFaceId != null) {
            if (u.hasFaceId() && u.getFaceIdEnrolledAt() != null) {
                lblFaceId.setText("Enrolled (since " + u.getFaceIdEnrolledAt().format(DTFULL) + ")");
                lblFaceId.setStyle("-fx-text-fill:#16A34A;");
            } else {
                lblFaceId.setText("Not enrolled");
                lblFaceId.setStyle("-fx-text-fill:#64748B;");
            }
        }

        set(lblRole, u.isAdmin() ? "Administrator" : "User");

        // Avatar
        if (lblAvatarInitials != null)
            lblAvatarInitials.setText(u.getFirstName() != null && !u.getFirstName().isEmpty()
                    ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?");

        if (photoView != null) {
            if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
                File f = new File("uploads/profiles/" + u.getPhotoFilename());
                if (f.exists()) {
                    photoView.setImage(new Image(f.toURI().toString()));
                    if (lblAvatarInitials != null) lblAvatarInitials.setVisible(false);
                }
            }
        }
    }

    @FXML private void handleEditProfile() {
        openModal("edit_profile.fxml", "Edit Profile", 560, 640);
    }

    @FXML private void handleChangePassword() {
        openModal("change_password.fxml", "Change Password", 460, 360);
    }

    @FXML private void handleManageFaceId() {
        openModal("face_id_management.fxml", "Manage Face ID", 480, 360);
    }

    @FXML private void handleWelcomeMessage() {
        openModal("welcome_message.fxml", "Welcome Message", 480, 340);
    }

    @FXML private void handleMyBadges() {
        openModal("badges.fxml", "My Badges", 700, 620);
    }

    @FXML private void handleDeactivate() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deactivate Account");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Your account will be deactivated. You will be logged out immediately and will not be able to log in until an administrator reactivates your account.");
        ButtonType yes = new ButtonType("Yes, Deactivate", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, cancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            repo.setActive(Session.getCurrentUser().getId(), false);
            Session.logout();
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
        }
    }

    private void openModal(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
            loadProfile();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : "—"); }
}
