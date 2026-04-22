package tn.esprit.projet.gui;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.WebcamService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 3-step Face ID enrollment:
 * Step 1 — Look Straight Ahead
 * Step 2 — Turn Left
 * Step 3 — Turn Right
 *
 * Each step captures 5 frames over 3 seconds, keeps the middle frame (index 2).
 * Generates a 512-float embedding, encrypts with AES-256-GCM, saves to DB.
 */
public class FaceIdEnrollController {

    // ── FXML fields ────────────────────────────────────────────────────────────
    @FXML public  ImageView  cameraView;
    @FXML private Label      stepTitleLabel;
    @FXML private Label      stepInstructionLabel;
    @FXML private Label      stepCounterLabel;
    @FXML private ProgressBar captureProgress;
    @FXML private Label      statusLabel;
    @FXML private Button     startButton;
    @FXML private Button     cancelButton;
    @FXML private VBox       countdownBox;
    @FXML private Label      countdownLabel;
    // Step dots
    @FXML private javafx.scene.shape.Circle dot1;
    @FXML private javafx.scene.shape.Circle dot2;
    @FXML private javafx.scene.shape.Circle dot3;

    // ── Step definitions ───────────────────────────────────────────────────────
    private static final String[][] STEPS = {
        {"Look Straight Ahead", "Position your face in front of the camera"},
        {"Turn Left",           "Turn your head slightly to the left"},
        {"Turn Right",          "Turn your head slightly to the right"}
    };

    // ── State ──────────────────────────────────────────────────────────────────
    private int          currentStep  = 0;
    private boolean      running      = false;
    private AnimationTimer previewTimer;
    private final byte[][] capturedFrames = new byte[3][];

    // ── Services ───────────────────────────────────────────────────────────────
    private final FaceEmbeddingService    embeddingService = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embeddingRepo    = new FaceEmbeddingRepository();
    private final UserRepository          userRepo         = new UserRepository();
    private final WebcamService           webcam           = new WebcamService();

    private User     targetUser;
    private Runnable onEnrolled;

    public void setTargetUser(User u)     { this.targetUser = u; }
    public void setOnEnrolled(Runnable r) { this.onEnrolled = r; }

    // ── Init ───────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        if (countdownBox != null) { countdownBox.setVisible(false); countdownBox.setManaged(false); }
        if (captureProgress != null) captureProgress.setProgress(0);
        if (startButton != null) startButton.setDisable(true);
        updateStepUI();

        // Open webcam in background
        setStatus("📷 Opening camera...", false);
        new Thread(() -> {
            boolean opened = webcam.open();
            Platform.runLater(() -> {
                if (opened) {
                    setStatus("✅ Camera ready. Click Start to begin.", false);
                    if (startButton != null) startButton.setDisable(false);
                    startLivePreview();
                } else {
                    setStatus("❌ Camera is in use by another app.\n1. Close Teams, Zoom, browser camera tabs\n2. Click Retry below", false);
                    if (startButton != null) {
                        startButton.setText("🔄  Retry Camera");
                        startButton.setDisable(false);
                        startButton.setOnAction(e -> retryCamera());
                    }
                    startPlaceholderPreview();
                }
            });
        }).start();
    }

    private void updateStepUI() {
        if (stepTitleLabel != null)
            stepTitleLabel.setText("Step " + (currentStep + 1) + " of 3 — " + STEPS[currentStep][0]);
        if (stepInstructionLabel != null)
            stepInstructionLabel.setText(STEPS[currentStep][1]);
        if (stepCounterLabel != null)
            stepCounterLabel.setText("Step " + (currentStep + 1) + " / 3");
        if (captureProgress != null)
            captureProgress.setProgress(currentStep / 3.0);
        if (statusLabel != null) statusLabel.setText("");
        // Update step dots
        String done    = "-fx-fill:#2E7D32;";
        String current = "-fx-fill:#4CAF50;";
        String pending = "-fx-fill:#C8E6C9;";
        if (dot1 != null) dot1.setStyle(currentStep > 0 ? done : current);
        if (dot2 != null) dot2.setStyle(currentStep > 1 ? done : currentStep == 1 ? current : pending);
        if (dot3 != null) dot3.setStyle(currentStep > 2 ? done : currentStep == 2 ? current : pending);
    }

    // ── Camera preview ─────────────────────────────────────────────────────────

    private void startLivePreview() {
        if (cameraView == null) return;
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 66_000_000L) { // ~15 fps
                    WritableImage frame = webcam.grabFrame();
                    if (frame != null) cameraView.setImage(frame);
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    private void startPlaceholderPreview() {
        if (cameraView == null) return;
        cameraView.setImage(buildFrame(0));
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 80_000_000L) {
                    cameraView.setImage(buildFrame(currentStep));
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    /** Builds a light-themed placeholder with face oval + step indicator */
    private WritableImage buildFrame(int step) {
        int W = 532, H = 360;
        WritableImage img = new WritableImage(W, H);
        PixelWriter pw = img.getPixelWriter();

        // Light grey background
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                int g = 228 + (int)(12.0 * y / H);
                pw.setArgb(x, y, 0xFF000000 | (g << 16) | (g << 8) | g);
            }

        // Face oval — shift based on step
        int cx = W / 2, cy = H / 2;
        if (step == 1) cx -= 50;
        if (step == 2) cx += 50;
        int rx = W / 4, ry = (int)(H * 0.40);

        int ovalColor = running ? 0xFF1B5E20 : 0xFF2E7D32;
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                double dx = (double)(x - cx) / rx, dy = (double)(y - cy) / ry;
                double d = dx * dx + dy * dy;
                if (d >= 0.93 && d <= 1.07) pw.setArgb(x, y, ovalColor);
            }

        // Subtle scan line
        int scanY = (int)((System.currentTimeMillis() / 25) % H);
        for (int x = 0; x < W; x++) pw.setArgb(x, scanY, 0x222E7D32);

        return img;
    }

    // ── Capture flow ───────────────────────────────────────────────────────────

    @FXML
    private void handleStart() {
        if (running) return;
        running = true;
        startButton.setDisable(true);
        startButton.setText("Capturing...");
        runStep(currentStep);
    }

    private void runStep(int step) {
        setStatus("⏳ Get ready...", false);

        // 1-second wait before capturing
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> captureFrames(step));
            } catch (InterruptedException ignored) {}
        }).start();
    }

    /** Capture 5 frames over 3 seconds (one every 600ms), keep middle frame (index 2) */
    private void captureFrames(int step) {
        List<byte[]> frames = new ArrayList<>();
        setStatus("📸 Capturing " + STEPS[step][0] + "...", false);

        // Show countdown
        if (countdownBox != null) { countdownBox.setVisible(true); countdownBox.setManaged(true); }

        final int[] frameIdx = {0};
        Timeline captureTimeline = new Timeline();

        for (int i = 0; i < 5; i++) {
            final int fi = i;
            captureTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(i * 600), e -> {
                if (countdownLabel != null) countdownLabel.setText(String.valueOf(5 - fi));
                byte[] frameBytes = captureCurrentFrame();
                frames.add(frameBytes);
                if (captureProgress != null)
                    captureProgress.setProgress((step + (fi + 1) / 5.0) / 3.0);
            }));
        }

        captureTimeline.setOnFinished(e -> {
            if (countdownBox != null) { countdownBox.setVisible(false); countdownBox.setManaged(false); }
            // Keep middle frame (index 2)
            byte[] middleFrame = frames.size() > 2 ? frames.get(2) : frames.get(frames.size() - 1);
            capturedFrames[step] = middleFrame;
            setStatus("✅ Step " + (step + 1) + " captured!", true);

            if (step < 2) {
                // Move to next step
                currentStep = step + 1;
                updateStepUI();
                new Thread(() -> {
                    try { Thread.sleep(800); }
                    catch (InterruptedException ignored) {}
                    Platform.runLater(() -> runStep(currentStep));
                }).start();
            } else {
                // All 3 steps done — generate embedding
                processEmbedding();
            }
        });

        captureTimeline.play();
    }

    /** Capture the current camera frame as JPEG bytes */
    private byte[] captureCurrentFrame() {
        if (webcam.isOpen()) {
            return webcam.grabFrameAsJpeg();
        }
        // Fallback: capture from ImageView (placeholder)
        try {
            javafx.scene.image.Image img = cameraView.getImage();
            if (img == null) return new byte[0];
            java.awt.image.BufferedImage bi = SwingFXUtils.fromFXImage(img, null);
            if (bi == null) return new byte[0];
            return webcam.toJpeg(bi, 0.8f);
        } catch (Exception e) { return new byte[0]; }
    }

    // ── Embedding generation + save ────────────────────────────────────────────

    private void processEmbedding() {
        setStatus("🔐 Generating secure embedding...", false);
        if (captureProgress != null) captureProgress.setProgress(0.9);

        new Thread(() -> {
            try {
                // Generate 512-float embedding
                double[] embedding = embeddingService.generateEmbedding(capturedFrames);

                // Encrypt with AES-256-GCM
                FaceEmbeddingService.EncryptedEmbedding enc = embeddingService.encrypt(embedding);

                // Get target user
                User user = targetUser != null ? targetUser : Session.getCurrentUser();
                if (user == null) {
                    Platform.runLater(() -> setStatus("❌ No user session found.", false));
                    return;
                }

                // Save encrypted embedding to face_embeddings table
                embeddingRepo.saveEmbedding(user.getId(), enc.encryptedB64, enc.ivB64, enc.tagB64);

                // Also save 128-float descriptor to user.face_descriptor (for backward compat)
                // Use first 128 values of the 512-float embedding
                double[] shortDescriptor = java.util.Arrays.copyOf(embedding, 128);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < shortDescriptor.length; i++) {
                    sb.append(String.format("%.6f", shortDescriptor[i]));
                    if (i < shortDescriptor.length - 1) sb.append(",");
                }
                sb.append("]");
                userRepo.updateFaceDescriptor(user.getId(), sb.toString(), LocalDateTime.now());
                user.setFaceDescriptor(sb.toString());
                user.setFaceIdEnrolledAt(LocalDateTime.now());
                if (targetUser == null) Session.login(user);

                Platform.runLater(() -> {
                    if (captureProgress != null) captureProgress.setProgress(1.0);
                    setStatus("✅ Face ID enrolled successfully!", true);
                    if (previewTimer != null) previewTimer.stop();
                    new Thread(webcam::close).start();

                    Stage stage = (Stage) cameraView.getScene().getWindow();
                    Toasts.show(stage, "Face ID enrolled successfully!", Toasts.Type.SUCCESS);

                    if (onEnrolled != null) onEnrolled.run();

                    new Thread(() -> {
                        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                        Platform.runLater(stage::close);
                    }).start();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("❌ Enrollment failed: " + e.getMessage(), false);
                    startButton.setDisable(false);
                    startButton.setText("Retry");
                    running = false;
                    currentStep = 0;
                    updateStepUI();
                });
            }
        }).start();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    public void setStatus(String msg, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle(success
                    ? "-fx-text-fill:#16A34A;-fx-font-size:13px;-fx-font-weight:bold;"
                    : "-fx-text-fill:#6B7280;-fx-font-size:12px;");
        }
    }

    @FXML
    private void handleCancel() {
        if (previewTimer != null) previewTimer.stop();
        new Thread(webcam::close).start();
        ((Stage) cameraView.getScene().getWindow()).close();
    }

    private void retryCamera() {
        if (startButton != null) {
            startButton.setDisable(true);
            startButton.setText("▶  Start Enrollment");
        }
        setStatus("📷 Retrying camera...", false);
        new Thread(() -> {
            webcam.close();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            boolean opened = webcam.open();
            Platform.runLater(() -> {
                if (opened) {
                    setStatus("✅ Camera ready. Click Start to begin.", false);
                    if (startButton != null) {
                        startButton.setDisable(false);
                        startButton.setText("▶  Start Enrollment");
                        startButton.setOnAction(e -> handleStart());
                    }
                    if (previewTimer != null) previewTimer.stop();
                    startLivePreview();
                } else {
                    setStatus("❌ Camera still in use. Close all apps using the camera and click Retry.", false);
                    if (startButton != null) {
                        startButton.setDisable(false);
                        startButton.setText("🔄  Retry Camera");
                    }
                }
            });
        }).start();
    }
}
