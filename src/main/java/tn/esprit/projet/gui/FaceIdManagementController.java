package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FaceIdManagementController {

    @FXML private Label  statusLabel;
    @FXML private Label  enrolledDateLabel;
    @FXML private Button enrollButton;
    @FXML private Button removeButton;

    private final UserRepository repo = new UserRepository();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        refresh();
    }

    public void refresh() {
        User u = repo.findById(Session.getCurrentUser().getId());
        if (u != null) Session.login(u);
        u = Session.getCurrentUser();

        boolean enrolled = u.hasFaceId();
        statusLabel.setText(enrolled ? "✅  Face ID is enrolled" : "❌  Face ID is not enrolled");
        statusLabel.setStyle(enrolled
                ? "-fx-text-fill:#16A34A;-fx-font-size:14px;-fx-font-weight:bold;"
                : "-fx-text-fill:#64748B;-fx-font-size:14px;");

        if (enrolledDateLabel != null) {
            if (enrolled && u.getFaceIdEnrolledAt() != null)
                enrolledDateLabel.setText("Enrolled on: " + u.getFaceIdEnrolledAt().format(FMT));
            else
                enrolledDateLabel.setText("");
        }

        enrollButton.setVisible(!enrolled);
        enrollButton.setManaged(!enrolled);
        removeButton.setVisible(enrolled);
        removeButton.setManaged(enrolled);
    }

    @FXML
    private void handleEnroll() {
        openCamera(Session.getCurrentUser());
    }

    @FXML
    private void handleRemove() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Face ID");
        alert.setContentText("Are you sure you want to remove your Face ID? You will need to re-enroll to use it again.");
        ButtonType yes    = new ButtonType("Yes, Remove", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel",      ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, cancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            User u = Session.getCurrentUser();
            repo.removeFaceDescriptor(u.getId());
            u.setFaceDescriptor(null);
            u.setFaceIdEnrolledAt(null);
            Session.login(u);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            Toasts.show(stage, "Face ID removed successfully!", Toasts.Type.INFO);
            refresh();
        }
    }

    private void openCamera(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_enroll.fxml"));
            Parent root = loader.load();
            FaceIdEnrollController ctrl = loader.getController();
            ctrl.setTargetUser(user);
            ctrl.setOnEnrolled(this::refresh);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Face ID — Enroll");
            stage.setScene(new Scene(root, 560, 620));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleClose() {
        ((Stage) statusLabel.getScene().getWindow()).close();
    }
}
