package tn.esprit.projet.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.WebcamService;
import tn.esprit.projet.utils.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Face ID verification — opens the REAL webcam immediately.
 *
 * Two modes:
 * - targetUser != null → verify against that specific user
 * - targetUser == null → scan ALL enrolled users (login mode, no email needed)
 */
public class FaceIdVerifyController {

    @FXML public  ImageView   cameraView;
    @FXML private Label       statusLabel;
    @FXML private Label       instructionLabel;
    @FXML private ProgressBar verifyProgress;
    @FXML private Button      captureButton;
    @FXML private Button      cancelButton;
    @FXML private Label       countdownLabel;

    private AnimationTimer    previewTimer;
    private final WebcamService webcam = new WebcamService();

    private User     targetUser;
    private Runnable onSuccess;
    private Runnable onFailure;

    private final FaceEmbeddingService    embeddingService = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embeddingRepo    = new FaceEmbeddingRepository();
    private final UserRepository          userRepo         = new UserRepository();

    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    public void setTargetUser(User u)    { this.targetUser = u; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }
    public void setOnFailure(Runnable r) { this.onFailure = r; }

    // ── Init ───────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        if (verifyProgress != null) verifyProgress.setProgress(0);
        if (instructionLabel != null)
            instructionLabel.setText(targetUser == null
                    ? "Position your face — we'll identify you automatically"
                    : "Look directly at the camera");

        // Open webcam in background, then start preview
        setStatus("📷 Opening camera...", false);
        if (captureButton != null) captureButton.setDisable(true);

        new Thread(() -> {
            boolean opened = webcam.open();
            Platform.runLater(() -> {
                if (opened) {
                    setStatus("✅ Camera ready. Click Verify when ready.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    startLivePreview();
                } else {
                    setStatus("❌ Camera is in use by another app.\n1. Close Teams, Zoom, browser camera tabs\n2. Click Retry below", false);
                    if (captureButton != null) {
                        captureButton.setText("🔄  Retry Camera");
                        captureButton.setDisable(false);
                        captureButton.setOnAction(e -> retryCamera());
                    }
                    startPlaceholderPreview();
                }
            });
        }).start();
    }

    // ── Live webcam preview ────────────────────────────────────────────────────

    private void startLivePreview() {
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 66_000_000L) { // ~15 fps
                    WritableImage frame = webcam.grabFrame();
                    if (frame != null && cameraView != null) cameraView.setImage(frame);
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    /** Fallback animated placeholder if camera unavailable */
    private void startPlaceholderPreview() {
        if (cameraView != null) cameraView.setImage(buildPlaceholder());
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 80_000_000L) {
                    if (cameraView != null) cameraView.setImage(buildPlaceholder());
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    private WritableImage buildPlaceholder() {
        int W = 480, H = 360;
        WritableImage img = new WritableImage(W, H);
        PixelWriter pw = img.getPixelWriter();
        // Light grey background matching the app theme
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                int g = 230 + (int)(10.0 * y / H);
                pw.setArgb(x, y, 0xFF000000 | (g << 16) | (g << 8) | g);
            }
        // Green oval guide
        int cx = W / 2, cy = H / 2, rx = W / 4, ry = (int)(H * 0.40);
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                double dx = (double)(x - cx) / rx, dy = (double)(y - cy) / ry;
                double d = dx * dx + dy * dy;
                if (d >= 0.93 && d <= 1.07) pw.setArgb(x, y, 0xFF2E7D32);
            }
        // Subtle scan line
        int scanY = (int)((System.currentTimeMillis() / 25) % H);
        for (int x = 0; x < W; x++) pw.setArgb(x, scanY, 0x332E7D32);
        return img;
    }

    // ── Capture + verify ───────────────────────────────────────────────────────

    @FXML
    private void handleCapture() {
        if (failedAttempts >= MAX_ATTEMPTS) {
            setStatus("❌ Too many failed attempts. Please use password login.", false);
            if (captureButton != null) captureButton.setDisable(true);
            return;
        }

        if (captureButton != null) captureButton.setDisable(true);
        setStatus("📸 Capturing 5 frames...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.1);

        // Capture 5 frames over 3 seconds in a background thread
        new Thread(() -> {
            List<byte[]> frames = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                final int fi = i;
                // Grab frame
                byte[] frameBytes = webcam.isOpen()
                        ? webcam.grabFrameAsJpeg()
                        : new byte[0];
                frames.add(frameBytes);

                Platform.runLater(() -> {
                    if (countdownLabel != null) countdownLabel.setText(String.valueOf(5 - fi));
                    if (verifyProgress != null) verifyProgress.setProgress(0.1 + (fi + 1) * 0.1);
                });

                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> {
                if (countdownLabel != null) countdownLabel.setText("");
            });

            // Keep middle frame (index 2)
            byte[] middleFrame = frames.size() > 2 ? frames.get(2) : frames.get(frames.size() - 1);
            verifyEmbedding(middleFrame);
        }).start();
    }

    // ── Embedding verification ─────────────────────────────────────────────────

    private void verifyEmbedding(byte[] frameBytes) {
        Platform.runLater(() -> {
            setStatus("🔐 Verifying...", false);
            if (verifyProgress != null) verifyProgress.setProgress(0.7);
        });

        new Thread(() -> {
            try {
                double[] liveEmbedding = embeddingService.generateEmbedding(
                        new byte[][]{frameBytes, frameBytes, frameBytes});

                if (targetUser != null) {
                    verifyAgainstUser(targetUser, liveEmbedding);
                } else {
                    scanAllUsers(liveEmbedding);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("❌ Verification error: " + e.getMessage(), false);
                    if (captureButton != null) captureButton.setDisable(false);
                });
            }
        }).start();
    }

    private void verifyAgainstUser(User user, double[] liveEmbedding) {
        try {
            String[] stored = embeddingRepo.findByUserId(user.getId());
            if (stored == null) {
                embeddingRepo.logAttempt(user.getId(), user.getEmail(), false, null);
                Platform.runLater(() -> {
                    setStatus("❌ No Face ID enrolled for this account.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                });
                return;
            }
            double[] storedEmbedding = embeddingService.decrypt(stored[0], stored[1], stored[2]);
            double similarity = embeddingService.cosineSimilarity(storedEmbedding, liveEmbedding);
            boolean match = similarity >= 0.75;
            embeddingRepo.logAttempt(user.getId(), user.getEmail(), match, similarity);
            Platform.runLater(() -> handleResult(match, user, similarity));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                setStatus("❌ Error: " + e.getMessage(), false);
                if (captureButton != null) captureButton.setDisable(false);
            });
        }
    }

    private void scanAllUsers(double[] liveEmbedding) {
        Platform.runLater(() -> setStatus("🔍 Scanning enrolled users...", false));
        try {
            List<int[]> userIds = embeddingRepo.findAllActiveUserIds();
            if (userIds.isEmpty()) {
                Platform.runLater(() -> {
                    setStatus("❌ No Face ID users enrolled yet.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                });
                return;
            }

            User bestMatch = null;
            double bestSimilarity = 0;

            for (int[] row : userIds) {
                try {
                    String[] stored = embeddingRepo.findByUserId(row[0]);
                    if (stored == null) continue;
                    double[] storedEmbedding = embeddingService.decrypt(stored[0], stored[1], stored[2]);
                    double similarity = embeddingService.cosineSimilarity(storedEmbedding, liveEmbedding);
                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        User candidate = userRepo.findById(row[0]);
                        if (candidate != null && candidate.isActive()) bestMatch = candidate;
                    }
                } catch (Exception ignored) {}
            }

            final User matchedUser = bestMatch;
            final double finalSimilarity = bestSimilarity;
            final boolean matched = finalSimilarity >= 0.75;

            embeddingRepo.logAttempt(
                    matchedUser != null ? matchedUser.getId() : null,
                    matchedUser != null ? matchedUser.getEmail() : null,
                    matched, finalSimilarity);

            Platform.runLater(() -> handleResult(matched, matchedUser, finalSimilarity));
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                setStatus("❌ Scan error: " + e.getMessage(), false);
                if (captureButton != null) captureButton.setDisable(false);
            });
        }
    }

    private void handleResult(boolean match, User user, double similarity) {
        if (verifyProgress != null) verifyProgress.setProgress(1.0);

        if (match && user != null) {
            embeddingRepo.updateLastUsed(user.getId());
            setStatus("✅ Welcome, " + user.getFirstName() + "!  (" +
                    String.format("%.1f%%", similarity * 100) + " match)", true);
            stopCamera();
            Session.login(user);
            new Thread(() -> {
                try { Thread.sleep(900); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    ((Stage) cameraView.getScene().getWindow()).close();
                    if (onSuccess != null) onSuccess.run();
                });
            }).start();
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;
            setStatus("❌ Face not recognized. (" + String.format("%.1f%%", similarity * 100) + ")" +
                    (remaining > 0 ? "  " + remaining + " attempts left." : "  No more attempts."), false);
            if (captureButton != null) captureButton.setDisable(remaining <= 0);
            if (remaining <= 0 && onFailure != null) onFailure.run();
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────────────────

    private void stopCamera() {
        if (previewTimer != null) { previewTimer.stop(); previewTimer = null; }
        new Thread(webcam::close).start();
    }

    private void retryCamera() {
        if (captureButton != null) {
            captureButton.setDisable(true);
            captureButton.setText("🔍  Verify Face");
        }
        setStatus("📷 Retrying camera...", false);
        new Thread(() -> {
            webcam.close();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            boolean opened = webcam.open();
            Platform.runLater(() -> {
                if (opened) {
                    setStatus("✅ Camera ready. Click Verify when ready.", false);
                    if (captureButton != null) {
                        captureButton.setDisable(false);
                        captureButton.setText("🔍  Verify Face");
                        captureButton.setOnAction(e -> handleCapture());
                    }
                    if (previewTimer != null) previewTimer.stop();
                    startLivePreview();
                } else {
                    setStatus("❌ Camera still in use. Close all apps using the camera and click Retry.", false);
                    if (captureButton != null) {
                        captureButton.setDisable(false);
                        captureButton.setText("🔄  Retry Camera");
                    }
                }
            });
        }).start();
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        ((Stage) cameraView.getScene().getWindow()).close();
    }

    public void setStatus(String msg, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle(success
                    ? "-fx-text-fill:#16A34A;-fx-font-size:13px;-fx-font-weight:bold;"
                    : "-fx-text-fill:#6B7280;-fx-font-size:12px;");
        }
    }
}
