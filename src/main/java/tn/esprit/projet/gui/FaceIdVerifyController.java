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
import tn.esprit.projet.services.FaceCameraOverlay;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.WebcamService;
import tn.esprit.projet.utils.Session;

import java.util.Base64;
import java.util.List;

/**
 * Face ID verification — sarxos webcam + Python DeepFace (ArcFace).
 * Shows face-guide oval. Cosine similarity on L2-normalized embeddings.
 */
public class FaceIdVerifyController {

    @FXML private ImageView   cameraView;
    @FXML private Label       statusLabel;
    @FXML private Label       instructionLabel;
    @FXML private ProgressBar verifyProgress;
    @FXML private Button      captureButton;
    @FXML private Button      cancelButton;

    private AnimationTimer previewTimer;
    private boolean        verifying = false;

    private final WebcamService           webcam           = new WebcamService();
    private final FaceEmbeddingService    embeddingService = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embeddingRepo    = new FaceEmbeddingRepository();
    private final UserRepository          userRepo         = new UserRepository();

    private User     targetUser;
    private Runnable onSuccess;
    private Runnable onFailure;
    private int      failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    public void setTargetUser(User u)    { this.targetUser = u; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }
    public void setOnFailure(Runnable r) { this.onFailure = r; }

    @FXML
    public void initialize() {
        // Ensure DB tables exist
        embeddingRepo.ensureTableExists();
        if (verifyProgress != null) verifyProgress.setProgress(0);
        if (captureButton != null) captureButton.setDisable(true);
        if (instructionLabel != null)
            instructionLabel.setText("Center your face in the oval, then click Verify");

        setStatus("📷 Opening camera...", false);
        new Thread(() -> {
            boolean ok = webcam.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready — align your face in the oval.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                    startPreview();
                } else {
                    setStatus("❌ Camera unavailable. Close other apps using the camera.", false);
                    if (captureButton != null) {
                        captureButton.setText("🔄 Retry");
                        captureButton.setDisable(false);
                        captureButton.setOnAction(e -> retryCamera());
                    }
                }
            });
        }).start();
    }

    private void startPreview() {
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 50_000_000L) {
                    WritableImage raw = webcam.grabFrame();
                    if (raw != null && cameraView != null) {
                        WritableImage overlaid = FaceCameraOverlay.draw(raw, !verifying, 0);
                        cameraView.setImage(overlaid);
                    }
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    @FXML
    private void handleCapture() {
        if (verifying) return;
        if (failedAttempts >= MAX_ATTEMPTS) {
            setStatus("❌ Too many failed attempts. Please use password login.", false);
            if (captureButton != null) captureButton.setDisable(true);
            return;
        }

        verifying = true;
        if (captureButton != null) captureButton.setDisable(true);
        setStatus("🤖 Analyzing face with AI...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.2);

        new Thread(() -> {
            try {
                // Capture 3 frames and average embeddings for robustness
                double[] liveEmbedding = null;
                int successCount = 0;
                for (int attempt = 0; attempt < 3; attempt++) {
                    byte[] frameJpeg = webcam.grabFrameAsJpeg();
                    if (frameJpeg == null || frameJpeg.length == 0) continue;
                    try {
                        String b64 = Base64.getEncoder().encodeToString(frameJpeg);
                        String req = "{\"command\":\"encode\",\"image\":\"" + b64 + "\"}";
                        double[] emb = embeddingService.callPythonForEmbedding(req);
                        if (liveEmbedding == null) {
                            liveEmbedding = emb.clone();
                        } else {
                            for (int i = 0; i < Math.min(liveEmbedding.length, emb.length); i++)
                                liveEmbedding[i] += emb[i];
                        }
                        successCount++;
                        if (attempt < 2) Thread.sleep(200);
                    } catch (Exception ignored) {}
                }
                if (liveEmbedding == null) throw new Exception("Could not capture any frame.");
                // Average and L2-normalize
                if (successCount > 1) {
                    for (int i = 0; i < liveEmbedding.length; i++) liveEmbedding[i] /= successCount;
                    double norm = 0;
                    for (double v : liveEmbedding) norm += v * v;
                    norm = Math.sqrt(norm);
                    if (norm > 0) for (int i = 0; i < liveEmbedding.length; i++) liveEmbedding[i] /= norm;
                }
                final double[] finalLive = liveEmbedding;

                Platform.runLater(() -> {
                    if (verifyProgress != null) verifyProgress.setProgress(0.5);
                    setStatus("🔍 Comparing against enrolled users...", false);
                });

                List<int[]> userIds = targetUser != null
                        ? List.of(new int[]{targetUser.getId()})
                        : embeddingRepo.findAllActiveUserIds();

                if (userIds.isEmpty()) {
                    Platform.runLater(() -> {
                        verifying = false;
                        setStatus("❌ No Face ID users enrolled yet.", false);
                        if (captureButton != null) captureButton.setDisable(false);
                    });
                    return;
                }

                User   bestMatch   = null;
                double bestCosine  = -1;
                double secondBest  = -1;

                for (int[] row : userIds) {
                    String[] stored = embeddingRepo.findByUserId(row[0]);
                    if (stored == null) continue;

                    // Extra safety: verify user still has face_descriptor in user table
                    User candidate = userRepo.findById(row[0]);
                    if (candidate == null || !candidate.isActive() || !candidate.hasFaceId()) continue;

                    double[] storedEmb = embeddingService.decrypt(stored[0], stored[1], stored[2]);

                    int len = Math.min(storedEmb.length, finalLive.length);
                    double dot = 0, na = 0, nb = 0;
                    for (int i = 0; i < len; i++) {
                        dot += storedEmb[i] * finalLive[i];
                        na  += storedEmb[i] * storedEmb[i];
                        nb  += finalLive[i] * finalLive[i];
                    }
                    double cosine = (na > 0 && nb > 0) ? dot / (Math.sqrt(na) * Math.sqrt(nb)) : 0;

                    if (cosine > bestCosine) {
                        secondBest = bestCosine;
                        bestCosine = cosine;
                        bestMatch  = candidate;
                    } else if (cosine > secondBest) {
                        secondBest = cosine;
                    }
                }

                // Adjusted threshold for better recognition
                // Lower threshold = more lenient (easier to match)
                // Higher threshold = stricter (harder to match)
                final double THRESHOLD = 0.65;  // Reduced from 0.82 for better recognition
                final double MARGIN    = 0.05;  // Reduced from 0.08
                final boolean isMatch  = bestCosine >= THRESHOLD
                        && (secondBest < 0 || (bestCosine - secondBest) >= MARGIN);

                final User   matched = isMatch ? bestMatch : null;
                final double finalCos = bestCosine;
                final double simPct   = Math.max(0, finalCos) * 100;

                // Debug logging
                System.out.println("[FaceID] Best match: " + (bestMatch != null ? bestMatch.getEmail() : "none"));
                System.out.println("[FaceID] Similarity: " + String.format("%.2f%%", simPct));
                System.out.println("[FaceID] Threshold: 65%");
                System.out.println("[FaceID] Match: " + (isMatch ? "✅ YES" : "❌ NO"));

                embeddingRepo.logAttempt(
                        matched != null ? matched.getId() : null,
                        matched != null ? matched.getEmail() : null,
                        isMatch, finalCos);

                Platform.runLater(() -> handleResult(isMatch, matched, finalCos, simPct));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    verifying = false;
                    setStatus("❌ " + e.getMessage(), false);
                    if (captureButton != null) captureButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleResult(boolean match, User user, double cosine, double simPct) {
        verifying = false;
        if (verifyProgress != null) verifyProgress.setProgress(1.0);

        if (match && user != null) {
            embeddingRepo.updateLastUsed(user.getId());
            setStatus("✅ Welcome, " + user.getFirstName() + "!  (" +
                    String.format("%.1f%%", simPct) + " match)", true);
            stopCamera();
            Session.login(user);
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> {
                ((Stage) cameraView.getScene().getWindow()).close();
                if (onSuccess != null) onSuccess.run();
            });
            delay.play();
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;
            setStatus("❌ Face not recognized. (" + String.format("%.1f%%", simPct) + " match)" +
                    (remaining > 0 ? "  " + remaining + " attempts left." : "  No more attempts."), false);
            if (captureButton != null) captureButton.setDisable(remaining <= 0);
            if (remaining <= 0 && onFailure != null) onFailure.run();
        }
    }

    private void stopCamera() {
        if (previewTimer != null) { previewTimer.stop(); previewTimer = null; }
        new Thread(webcam::close).start();
    }

    private void retryCamera() {
        if (captureButton != null) captureButton.setDisable(true);
        setStatus("📷 Retrying camera...", false);
        new Thread(() -> {
            webcam.close();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            boolean ok = webcam.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready — align your face in the oval.", false);
                    if (captureButton != null) {
                        captureButton.setDisable(false);
                        captureButton.setText("🔍  Verify Face");
                        captureButton.setOnAction(e -> handleCapture());
                    }
                    startPreview();
                } else {
                    setStatus("❌ Camera still unavailable.", false);
                    if (captureButton != null) captureButton.setDisable(false);
                }
            });
        }).start();
    }

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
        stopCamera();
        ((Stage) cameraView.getScene().getWindow()).close();
    }
}
