package tn.esprit.projet.gui;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.CameraFaceHelper;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.LocalFaceEmbeddingService;
import tn.esprit.projet.utils.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Face ID Login — real-time camera with professional overlay.
 *
 * Security improvements:
 *  - Face presence validation before matching (rejects hands, walls, objects)
 *  - 3-frame multi-capture: all 3 frames must match the same user
 *  - Strict threshold (0.93) — much harder to spoof
 *  - Clear error messages when no face is detected
 */
public class FaceIdVerifyController {

    @FXML private ImageView   cameraView;
    @FXML private Label       statusLabel;
    @FXML private Label       instructionLabel;
    @FXML private ProgressBar verifyProgress;
    @FXML private Button      captureButton;
    @FXML private Button      cancelButton;

    private final CameraFaceHelper          camera    = new CameraFaceHelper();
    private final LocalFaceEmbeddingService localEmb  = new LocalFaceEmbeddingService();
    private final FaceEmbeddingService      cryptoSvc = new FaceEmbeddingService();
    private final FaceEmbeddingRepository   embRepo   = new FaceEmbeddingRepository();
    private final UserRepository            userRepo  = new UserRepository();

    // Number of frames to capture and cross-validate
    private static final int CAPTURE_COUNT = 3;
    // All frames must score above this threshold
    private static final double MATCH_THRESHOLD = LocalFaceEmbeddingService.MATCH_THRESHOLD;
    // Minimum frames that must agree on the same user
    private static final int MIN_AGREEMENT = 3;

    private AnimationTimer previewTimer;
    private boolean verifying = false;

    private Runnable onSuccess;
    private Runnable onFailure;

    public void setOnSuccess(Runnable r) { this.onSuccess = r; }
    public void setOnFailure(Runnable r) { this.onFailure = r; }

    @FXML
    public void initialize() {
        embRepo.ensureTableExists();
        setStatus("📷 Opening camera...", false);
        if (captureButton != null) captureButton.setDisable(true);
        if (verifyProgress != null) verifyProgress.setProgress(0);

        new Thread(() -> {
            boolean ok = camera.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Look directly at the camera and click Verify", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    startPreview();
                } else {
                    setStatus("❌ Camera unavailable. Click Retry.", false);
                    if (captureButton != null) {
                        captureButton.setText("🔄 Retry");
                        captureButton.setDisable(false);
                        captureButton.setOnAction(e -> new Thread(() -> {
                            boolean r2 = camera.open();
                            Platform.runLater(() -> {
                                if (r2) {
                                    setStatus("✅ Camera ready.", false);
                                    captureButton.setText("🔍  Verify Face");
                                    captureButton.setOnAction(ev -> handleVerify());
                                    startPreview();
                                } else {
                                    setStatus("❌ Camera still unavailable.", false);
                                }
                            });
                        }).start());
                    }
                }
            });
        }).start();
    }

    private void startPreview() {
        previewTimer = new AnimationTimer() {
            private long last = 0;
            @Override public void handle(long now) {
                if (now - last < 40_000_000L) return;
                last = now;
                WritableImage frame = camera.grabWithOverlay(0);
                if (frame != null && cameraView != null) cameraView.setImage(frame);
            }
        };
        previewTimer.start();
    }

    @FXML
    private void handleVerify() {
        if (verifying || !camera.isOpen()) return;
        verifying = true;
        if (captureButton != null) captureButton.setDisable(true);
        setStatus("📸 Hold still — capturing 3 frames...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.1);

        // Small delay to let user settle
        PauseTransition p = new PauseTransition(Duration.millis(600));
        p.setOnFinished(e -> doMultiCapture());
        p.play();
    }

    /**
     * Capture CAPTURE_COUNT frames with small delays between them.
     * All frames must agree on the same user above the threshold.
     */
    private void doMultiCapture() {
        setStatus("🤖 Analyzing face — please stay still...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.2);

        new Thread(() -> {
            List<byte[]> frames = new ArrayList<>();

            // Capture 3 frames with 300ms between each
            for (int i = 0; i < CAPTURE_COUNT; i++) {
                byte[] jpeg = camera.grabJpeg();
                if (jpeg != null && jpeg.length > 0) {
                    frames.add(jpeg);
                }
                if (i < CAPTURE_COUNT - 1) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
                final int captured = frames.size();
                Platform.runLater(() -> {
                    if (verifyProgress != null)
                        verifyProgress.setProgress(0.2 + 0.3 * captured / CAPTURE_COUNT);
                    setStatus("🤖 Captured " + captured + "/" + CAPTURE_COUNT + " frames...", false);
                });
            }

            if (frames.isEmpty()) {
                Platform.runLater(() -> {
                    verifying = false;
                    setStatus("❌ Could not capture image. Try again.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    if (verifyProgress != null) verifyProgress.setProgress(0);
                });
                return;
            }

            Platform.runLater(() -> {
                if (verifyProgress != null) verifyProgress.setProgress(0.5);
                setStatus("🔍 Verifying identity...", false);
            });

            doVerifyFrames(frames);
        }).start();
    }

    private void doVerifyFrames(List<byte[]> frames) {
        try {
            List<FaceEmbeddingRepository.UserEmbedding> all = embRepo.findAllActiveEmbeddings();

            if (all.isEmpty()) {
                Platform.runLater(() -> {
                    verifying = false;
                    setStatus("❌ No Face ID accounts found. Register first.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    if (verifyProgress != null) verifyProgress.setProgress(0);
                });
                return;
            }

            // Extract embeddings from all frames
            List<double[]> liveEmbeddings = new ArrayList<>();
            for (byte[] jpeg : frames) {
                try {
                    double[] emb = localEmb.extractEmbedding(jpeg);
                    liveEmbeddings.add(emb);
                } catch (LocalFaceEmbeddingService.FaceNotDetectedException e) {
                    System.out.println("[FaceID] Frame rejected: " + e.getMessage());
                    // Don't add — this frame had no face
                } catch (Exception e) {
                    System.err.println("[FaceID] Frame error: " + e.getMessage());
                }
            }

            if (liveEmbeddings.isEmpty()) {
                Platform.runLater(() -> {
                    verifying = false;
                    setStatus("❌ No face detected. Look directly at the camera.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    if (verifyProgress != null) verifyProgress.setProgress(0);
                });
                return;
            }

            System.out.printf("[FaceID] %d/%d frames had valid faces%n", liveEmbeddings.size(), frames.size());

            Platform.runLater(() -> {
                if (verifyProgress != null) verifyProgress.setProgress(0.7);
            });

            // For each enrolled user, count how many frames match
            Integer bestUserId = null;
            double  bestAvgScore = 0.0;
            int     bestAgreement = 0;

            for (FaceEmbeddingRepository.UserEmbedding ue : all) {
                try {
                    double[] stored = cryptoSvc.decrypt(ue.encryptedB64, ue.ivB64, ue.tagB64);

                    int agreement = 0;
                    double totalScore = 0.0;

                    for (double[] live : liveEmbeddings) {
                        double score = localEmb.similarity(live, stored);
                        System.out.printf("[FaceID] User %d frame score: %.4f (threshold=%.2f)%n",
                                ue.userId, score, MATCH_THRESHOLD);
                        totalScore += score;
                        if (score >= MATCH_THRESHOLD) agreement++;
                    }

                    double avgScore = totalScore / liveEmbeddings.size();
                    System.out.printf("[FaceID] User %d → avg=%.4f, agreement=%d/%d%n",
                            ue.userId, avgScore, agreement, liveEmbeddings.size());

                    if (agreement > bestAgreement ||
                        (agreement == bestAgreement && avgScore > bestAvgScore)) {
                        bestAgreement = agreement;
                        bestAvgScore  = avgScore;
                        bestUserId    = ue.userId;
                    }
                } catch (Exception ex) {
                    System.err.println("[FaceID] Decrypt error user " + ue.userId + ": " + ex.getMessage());
                }
            }

            // Log the attempt
            boolean success = bestUserId != null
                    && bestAgreement >= MIN_AGREEMENT
                    && bestAvgScore >= MATCH_THRESHOLD;
            embRepo.logAttempt(bestUserId, null, success, bestAvgScore);

            final Integer finalUserId  = bestUserId;
            final double  finalScore   = bestAvgScore;
            final int     finalAgree   = bestAgreement;

            Platform.runLater(() -> {
                if (verifyProgress != null) verifyProgress.setProgress(1.0);

                if (success) {
                    User user = userRepo.findById(finalUserId);
                    if (user == null || !user.isActive()) {
                        verifying = false;
                        setStatus("❌ Account not found or inactive.", false);
                        if (captureButton != null) captureButton.setDisable(false);
                        if (verifyProgress != null) verifyProgress.setProgress(0);
                        return;
                    }
                    System.out.printf("[FaceID] ✅ Login: %s (score=%.4f, agreement=%d/%d)%n",
                            user.getEmail(), finalScore, finalAgree, liveEmbeddings.size());
                    setStatus("✅ Welcome, " + user.getFirstName() + "!", true);
                    Session.login(user);
                    PauseTransition close = new PauseTransition(Duration.millis(1200));
                    close.setOnFinished(ev -> {
                        stopCamera();
                        ((Stage) cameraView.getScene().getWindow()).close();
                        if (onSuccess != null) onSuccess.run();
                    });
                    close.play();
                } else {
                    verifying = false;
                    String msg;
                    if (finalUserId == null) {
                        msg = "❌ Face not recognized. Try again or use password.";
                    } else if (finalAgree < MIN_AGREEMENT) {
                        msg = String.format("❌ Inconsistent match (%d/%d frames). Try again.", finalAgree, liveEmbeddings.size());
                    } else {
                        msg = String.format("❌ Score too low (%.0f%%). Try again.", finalScore * 100);
                    }
                    setStatus(msg, false);
                    if (captureButton != null) captureButton.setDisable(false);
                    if (verifyProgress != null) verifyProgress.setProgress(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                verifying = false;
                setStatus("❌ Error: " + e.getMessage(), false);
                if (captureButton != null) captureButton.setDisable(false);
                if (verifyProgress != null) verifyProgress.setProgress(0);
            });
        }
    }

    private void stopCamera() {
        if (previewTimer != null) { previewTimer.stop(); previewTimer = null; }
        new Thread(camera::close).start();
    }

    private void setStatus(String msg, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(msg);
            statusLabel.setStyle(success
                ? "-fx-text-fill:#16A34A;-fx-font-size:13px;-fx-font-weight:bold;"
                : "-fx-text-fill:#6B7280;-fx-font-size:12px;");
        }
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        ((Stage) cameraView.getScene().getWindow()).close();
        if (onFailure != null) onFailure.run();
    }
}
