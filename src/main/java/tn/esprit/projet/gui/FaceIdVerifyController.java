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

import java.util.List;

/**
 * Face ID Login — real-time camera with professional overlay.
 * Uses LocalFaceEmbeddingService (no Python needed).
 */
public class FaceIdVerifyController {

    @FXML private ImageView   cameraView;
    @FXML private Label       statusLabel;
    @FXML private Label       instructionLabel;
    @FXML private ProgressBar verifyProgress;
    @FXML private Button      captureButton;
    @FXML private Button      cancelButton;

    private final CameraFaceHelper        camera   = new CameraFaceHelper();
    private final LocalFaceEmbeddingService localEmb = new LocalFaceEmbeddingService();
    private final FaceEmbeddingService    cryptoSvc = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embRepo  = new FaceEmbeddingRepository();
    private final UserRepository          userRepo  = new UserRepository();

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
                    setStatus("✅ Look at the camera and click Verify", false);
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
                                if (r2) { setStatus("✅ Camera ready.", false); captureButton.setText("🔍  Verify Face"); captureButton.setOnAction(ev -> handleVerify()); startPreview(); }
                                else    { setStatus("❌ Camera still unavailable.", false); }
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
        setStatus("📸 Hold still...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.2);

        PauseTransition p = new PauseTransition(Duration.millis(500));
        p.setOnFinished(e -> doVerify());
        p.play();
    }

    private void doVerify() {
        setStatus("🤖 Analyzing face...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.4);

        byte[] jpeg = camera.grabJpeg();
        if (jpeg == null || jpeg.length == 0) {
            verifying = false;
            setStatus("❌ Could not capture image. Try again.", false);
            if (captureButton != null) captureButton.setDisable(false);
            if (verifyProgress != null) verifyProgress.setProgress(0);
            return;
        }

        new Thread(() -> {
            try {
                double[] liveEmb = localEmb.extractEmbedding(jpeg);

                Platform.runLater(() -> {
                    if (verifyProgress != null) verifyProgress.setProgress(0.7);
                    setStatus("🔍 Matching identity...", false);
                });

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

                Integer matchId = null;
                double  bestScore = 0.0;

                for (FaceEmbeddingRepository.UserEmbedding ue : all) {
                    try {
                        double[] stored = cryptoSvc.decrypt(ue.encryptedB64, ue.ivB64, ue.tagB64);
                        double   score  = localEmb.similarity(liveEmb, stored);
                        System.out.printf("[FaceID] User %d → %.4f%n", ue.userId, score);
                        if (score > bestScore) { bestScore = score; matchId = ue.userId; }
                    } catch (Exception ex) {
                        System.err.println("[FaceID] Decrypt error user " + ue.userId);
                    }
                }

                embRepo.logAttempt(matchId, null, bestScore >= 0.82, bestScore);

                final Integer fid = matchId;
                final double  fs  = bestScore;

                Platform.runLater(() -> {
                    if (verifyProgress != null) verifyProgress.setProgress(1.0);

                    if (fid != null && fs >= 0.82) {
                        User user = userRepo.findById(fid);
                        if (user == null || !user.isActive()) {
                            verifying = false;
                            setStatus("❌ Account not found or inactive.", false);
                            if (captureButton != null) captureButton.setDisable(false);
                            if (verifyProgress != null) verifyProgress.setProgress(0);
                            return;
                        }
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
                        setStatus("❌ Face not recognized. Try again or use password.", false);
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
        }).start();
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
