package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class UserViewController {

    @FXML private Label     lblFullName;
    @FXML private Label     lblEmail;
    @FXML private Label     lblStatusBadge;
    @FXML private Label     lblRolesBadge;
    @FXML private Label     lblAvatarInitials;
    @FXML private ImageView imgPhoto;
    @FXML private Label     lblAge;
    @FXML private Label     lblBmi;
    @FXML private Label     lblBmiCategory;
    @FXML private Label     lblWeight;
    @FXML private Label     lblHeight;
    @FXML private Label     lblBirthday;
    @FXML private Label     lblPhone;
    @FXML private Label     lblCreatedAt;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setUser(User u) {
        lblFullName.setText(u.getFullName().isBlank() ? "—" : u.getFullName());
        lblEmail.setText(nvl(u.getEmail()));

        String init = u.getFirstName() != null && !u.getFirstName().isEmpty()
                ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?";
        lblAvatarInitials.setText(init);
        if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                imgPhoto.setImage(new Image(f.toURI().toString()));
                imgPhoto.setVisible(true);
                lblAvatarInitials.setVisible(false);
            }
        }

        if (u.isActive()) {
            lblStatusBadge.setText("Active");
            lblStatusBadge.setStyle("-fx-background-color:#DCFCE7;-fx-text-fill:#166534;" +
                    "-fx-background-radius:6;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;");
        } else {
            lblStatusBadge.setText("Inactive");
            lblStatusBadge.setStyle("-fx-background-color:#FEE2E2;-fx-text-fill:#991B1B;" +
                    "-fx-background-radius:6;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;");
        }
        lblRolesBadge.setText(u.getRoles() != null ? u.getRoles() : "No roles");

        lblAge.setText(u.getAge() > 0 ? String.valueOf(u.getAge()) : "—");
        lblWeight.setText(u.getWeight() > 0 ? String.format("%.1f", u.getWeight()) : "—");
        lblHeight.setText(u.getHeight() > 0 ? String.format("%.0f", u.getHeight()) : "—");

        if (u.getWeight() > 0 && u.getHeight() > 0) {
            double bmi = u.getBmi();
            lblBmi.setText(String.format("%.1f", bmi));
            lblBmiCategory.setText(bmiCategory(bmi));
        } else {
            lblBmi.setText("—"); lblBmiCategory.setText("—");
        }

        lblBirthday.setText(u.getBirthday() != null ? u.getBirthday().format(D_FMT) : "—");
        lblPhone.setText("—");
        lblCreatedAt.setText(u.getCreatedAt() != null ? u.getCreatedAt().format(DT_FMT) : "—");
    }

    private String bmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private String nvl(String s) { return s != null ? s : "—"; }

    @FXML
    private void handleClose() {
        ((Stage) lblFullName.getScene().getWindow()).close();
    }
}
