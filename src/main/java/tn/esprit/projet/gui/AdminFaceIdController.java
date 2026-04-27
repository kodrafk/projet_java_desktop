package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.AlertUtil;

import java.time.format.DateTimeFormatter;

/**
 * Admin panel: view, enroll, or revoke Face ID for any user.
 */
public class AdminFaceIdController {

    @FXML private Label  lblUserName;
    @FXML private Label  lblStatusIcon;
    @FXML private Label  lblStatus;
    @FXML private Label  lblEnrolledDate;
    @FXML private Label  lblMessage;
    @FXML private Button btnEnroll;
    @FXML private Button btnRevoke;

    private User targetUser;
    private Runnable onChanged;

    private final UserRepository          userRepo      = new UserRepository();
    private final FaceEmbeddingRepository embeddingRepo = new FaceEmbeddingRepository();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setTargetUser(User u) {
        this.targetUser = u;
        refresh();
    }

    public void setOnChanged(Runnable r) { this.onChanged = r; }

    private void refresh() {
        // Always reload fresh from DB
        User u = userRepo.findById(targetUser.getId());
        if (u != null) targetUser = u;

        if (lblUserName != null)
            lblUserName.setText(targetUser.getFullName() + " — " + targetUser.getEmail());

        boolean enrolled = targetUser.hasFaceId();

        if (lblStatusIcon != null) lblStatusIcon.setText(enrolled ? "✅" : "❌");

        if (lblStatus != null) {
            lblStatus.setText(enrolled ? "Face ID Enrolled" : "Face ID Not Enrolled");
            lblStatus.setStyle(enrolled
                    ? "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#16A34A;"
                    : "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#64748B;");
        }

        if (lblEnrolledDate != null) {
            if (enrolled && targetUser.getFaceIdEnrolledAt() != null)
                lblEnrolledDate.setText("Enrolled on: " + targetUser.getFaceIdEnrolledAt().format(FMT));
            else
                lblEnrolledDate.setText(enrolled ? "Enrollment date unknown" : "No face data stored");
        }

        if (btnEnroll != null)
            btnEnroll.setText(enrolled ? "🔄  Re-enroll Face ID" : "📷  Enroll Face ID");

        if (btnRevoke != null) {
            btnRevoke.setDisable(!enrolled);
            btnRevoke.setStyle(enrolled
                    ? "-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:hand;"
                    : "-fx-background-color:#E2E8F0;-fx-text-fill:#94A3B8;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:10;-fx-cursor:not-allowed;");
        }

        setMessage("");
    }

    @FXML
    private void handleEnroll() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_id_enroll.fxml"));
            Parent root = loader.load();
            FaceIdEnrollController ctrl = loader.getController();
            ctrl.setTargetUser(targetUser);
            ctrl.setOnEnrolled(() -> {
                // Reload user and refresh panel
                User fresh = userRepo.findById(targetUser.getId());
                if (fresh != null) targetUser = fresh;
                Platform.runLater(() -> {
                    refresh();
                    setMessage("✅ Face ID enrolled successfully for " + targetUser.getFirstName() + ".");
                    if (onChanged != null) onChanged.run();
                });
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnEnroll.getScene().getWindow());
            stage.setTitle("Face ID Enrollment — " + targetUser.getFullName());
            stage.setScene(new Scene(root, 560, 620));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("❌ Could not open camera: " + e.getMessage());
        }
    }

    @FXML
    private void handleRevoke() {
        if (!targetUser.hasFaceId()) {
            setMessage("This user has no Face ID enrolled.");
            return;
        }

        boolean confirmed = AlertUtil.confirm("Revoke Face ID",
            "Revoke Face ID for " + targetUser.getFullName() + "?\n\n" +
            "This will permanently delete their face data. They will need to re-enroll to use Face ID again.");
        if (confirmed) {
            userRepo.removeFaceDescriptor(targetUser.getId());
            embeddingRepo.removeByUserId(targetUser.getId());
            targetUser.setFaceDescriptor(null);
            targetUser.setFaceIdEnrolledAt(null);
            refresh();
            setMessage("✅ Face ID revoked for " + targetUser.getFirstName() + ".");
            if (onChanged != null) onChanged.run();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) lblStatus.getScene().getWindow()).close();
    }

    private void setMessage(String msg) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            boolean ok = msg.startsWith("✅");
            lblMessage.setStyle(ok
                    ? "-fx-font-size:12px;-fx-text-fill:#16A34A;-fx-font-weight:bold;"
                    : "-fx-font-size:12px;-fx-text-fill:#DC2626;");
        }
    }
}
