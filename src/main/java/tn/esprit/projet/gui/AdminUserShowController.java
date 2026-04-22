package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class AdminUserShowController {

    @FXML private ImageView photoView;
    @FXML private Label     lblAvatarInitials;
    @FXML private Label     lblFullName;
    @FXML private Label     lblEmail;
    @FXML private Label     lblRole;
    @FXML private Label     lblStatus;
    @FXML private Label     lblBirthday;
    @FXML private Label     lblAge;
    @FXML private Label     lblWeight;
    @FXML private Label     lblHeight;
    @FXML private Label     lblBmi;
    @FXML private Label     lblCreatedAt;
    @FXML private Label     lblWelcomeMessage;
    @FXML private Label     lblFaceId;
    @FXML private Label     lblGoogleId;

    private User user;
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setUser(User u) {
        this.user = u;
        populate();
    }

    private void populate() {
        User u = user;
        set(lblFullName, u.getFullName());
        set(lblEmail, u.getEmail());
        set(lblRole, u.isAdmin() ? "Administrator" : "User");

        if (lblStatus != null) {
            lblStatus.setText(u.isActive() ? "Active" : "Inactive");
            lblStatus.setStyle(u.isActive()
                    ? "-fx-text-fill:#16A34A;-fx-font-weight:bold;"
                    : "-fx-text-fill:#DC2626;-fx-font-weight:bold;");
        }

        set(lblBirthday, u.getBirthday() != null ? u.getBirthday().format(D_FMT) : "—");
        set(lblAge, u.getAge() > 0 ? u.getAge() + " years old" : "—");
        set(lblWeight, u.getWeight() > 0 ? u.getWeight() + " kg" : "—");
        set(lblHeight, u.getHeight() > 0 ? u.getHeight() + " cm" : "—");
        if (u.getWeight() > 0 && u.getHeight() > 0)
            set(lblBmi, String.format("%.2f (%s)", u.getBmi(), u.getBmiCategory()));
        else set(lblBmi, "—");

        set(lblCreatedAt, u.getCreatedAt() != null ? u.getCreatedAt().format(DT_FMT) : "—");
        set(lblWelcomeMessage, u.getWelcomeMessage() != null ? u.getWelcomeMessage() : "—");

        if (lblFaceId != null) {
            if (u.hasFaceId() && u.getFaceIdEnrolledAt() != null)
                lblFaceId.setText("Enrolled on " + u.getFaceIdEnrolledAt().format(DT_FMT));
            else lblFaceId.setText("Not enrolled");
        }

        set(lblGoogleId, u.getGoogleId() != null ? "Connected" : "Not connected");

        if (lblAvatarInitials != null)
            lblAvatarInitials.setText(u.getFirstName() != null && !u.getFirstName().isEmpty()
                    ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?");

        if (photoView != null && u.getPhotoFilename() != null) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                photoView.setImage(new Image(f.toURI().toString()));
                if (lblAvatarInitials != null) lblAvatarInitials.setVisible(false);
            }
        }
    }

    @FXML private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_edit.fxml"));
            Parent root = loader.load();
            AdminUserEditController ctrl = loader.getController();
            ctrl.setUser(user);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit — " + user.getFullName());
            stage.setScene(new Scene(root, 620, 680));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBack() {
        ((Stage) lblFullName.getScene().getWindow()).close();
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : "—"); }
}
