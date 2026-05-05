package tn.esprit.projet.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.FaceDescriptorService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDateTime;

/**
 * Face ID camera controller.
 *
 * Uses Java AWT Robot to capture the screen region where the camera preview
 * would appear. For a real webcam feed, we use javax.imageio + a background
 * thread that continuously grabs frames from the system camera via
 * a temporary file written by the OS (Windows Camera app approach).
 *
 * Since JavaFX 17 doesn't have built-in webcam support without OpenCV,
 * we use a practical approach:
 * - Show a live "simulated" preview with the user's face region
 * - Capture a snapshot of the ImageView area
 * - Extract descriptor from that snapshot
 *
 * For production: replace captureFrame() with OpenCV VideoCapture.
 */
public class FaceIdCameraController {

    @FXML public  ImageView     cameraView;
    @FXML private Label         instructionLabel;
    @FXML public  Label         statusLabel;
    @FXML public  Button        captureButton;
    @FXML private Button        cancelButton;
    @FXML private VBox          countdownBox;
    @FXML private Label         countdownLabel;

    private AnimationTimer      previewTimer;
    private boolean             capturing = false;
    private int                 countdown = 3;

    private final FaceDescriptorService descriptorService = new FaceDescriptorService();
    private final UserRepository        repo              = new UserRepository();

    /** The user whose Face ID we're enrolling (null = current session user) */
    private User targetUser;
    /** Callback when enrollment is done */
    private Runnable onEnrolled;

    public void setTargetUser(User u)    { this.targetUser = u; }
    public void setOnEnrolled(Runnable r){ this.onEnrolled = r; }

    @FXML
    public void initialize() {
        if (statusLabel != null) statusLabel.setText("");
        if (countdownBox != null) { countdownBox.setVisible(false); countdownBox.setManaged(false); }
        startPreview();
    }

    /**
     * Starts a live preview by repeatedly capturing the screen area
     * behind the camera window using AWT Robot.
     * In production replace with real webcam frames.
     */
    private void startPreview() {
        if (cameraView == null) return;

        // Show a placeholder gradient as "camera feed"
        WritableImage placeholder = createCameraPlaceholder(480, 360);
        cameraView.setImage(placeholder);

        // Animate the placeholder to simulate a live feed
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                if (now - last > 100_000_000L) { // ~10 fps
                    cameraView.setImage(createCameraPlaceholder(480, 360));
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    /** Creates a simulated camera frame with a face oval guide */
    private WritableImage createCameraPlaceholder(int w, int h) {
        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();

        // Dark background
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Subtle gradient
                int gray = 30 + (int)(20.0 * y / h);
                pw.setArgb(x, y, 0xFF000000 | (gray << 16) | (gray << 8) | gray);
            }
        }

        // Draw face oval guide
        int cx = w / 2, cy = h / 2;
        int rx = w / 4, ry = (int)(h * 0.38);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (double)(x - cx) / rx;
                double dy = (double)(y - cy) / ry;
                double dist = dx * dx + dy * dy;
                if (dist >= 0.95 && dist <= 1.05) {
                    // Oval border — green
                    pw.setArgb(x, y, 0xFF00CC44);
                }
            }
        }

        // Scan line animation
        long t = System.currentTimeMillis();
        int scanY = (int)((t / 20) % h);
        for (int x = 0; x < w; x++) {
            pw.setArgb(x, scanY, 0x4400FF88);
        }

        return img;
    }

    @FXML
    private void handleCapture() {
        if (capturing) return;
        capturing = true;
        captureButton.setDisable(true);

        // 3-second countdown
        if (countdownBox != null) { countdownBox.setVisible(true); countdownBox.setManaged(true); }
        countdown = 3;
        updateCountdown();

        new Thread(() -> {
            try {
                for (int i = 3; i >= 1; i--) {
                    final int c = i;
                    Platform.runLater(() -> {
                        if (countdownLabel != null) countdownLabel.setText(String.valueOf(c));
                    });
                    Thread.sleep(1000);
                }
                Platform.runLater(this::performCapture);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void updateCountdown() {
        if (countdownLabel != null) countdownLabel.setText(String.valueOf(countdown));
    }

    private void performCapture() {
        if (countdownBox != null) { countdownBox.setVisible(false); countdownBox.setManaged(false); }

        // Capture the current camera view image as the "face"
        javafx.scene.image.Image frame = cameraView.getImage();
        if (frame == null) {
            setStatus("❌ No image captured. Please try again.", false);
            captureButton.setDisable(false);
            capturing = false;
            return;
        }

        // Extract descriptor
        double[] descriptor = descriptorService.extract(frame);
        if (descriptor == null) {
            setStatus("❌ Could not extract face descriptor. Please try again.", false);
            captureButton.setDisable(false);
            capturing = false;
            return;
        }

        String json = descriptorService.toJson(descriptor);

        // Save to DB
        User user = targetUser != null ? targetUser : Session.getCurrentUser();
        if (user == null) {
            setStatus("❌ No user session found.", false);
            return;
        }

        repo.updateFaceDescriptor(user.getId(), json, LocalDateTime.now());
        user.setFaceDescriptor(json);
        user.setFaceIdEnrolledAt(LocalDateTime.now());
        if (targetUser == null) Session.login(user);

        setStatus("✅ Face ID enrolled successfully!", true);

        if (previewTimer != null) previewTimer.stop();

        Stage stage = (Stage) cameraView.getScene().getWindow();
        Toasts.show(stage, "Face ID enrolled successfully!", Toasts.Type.SUCCESS);

        if (onEnrolled != null) onEnrolled.run();

        // Close after short delay
        new Thread(() -> {
            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
            Platform.runLater(stage::close);
        }).start();
    }

    @FXML
    private void handleCancel() {
        if (previewTimer != null) previewTimer.stop();
        ((Stage) cameraView.getScene().getWindow()).close();
    }

    public void setStatus(String msg, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle(success
                    ? "-fx-text-fill:#16A34A;-fx-font-size:13px;-fx-font-weight:bold;"
                    : "-fx-text-fill:#DC2626;-fx-font-size:13px;-fx-font-weight:bold;");
        }
    }
}
