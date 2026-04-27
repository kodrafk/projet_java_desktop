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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.CameraFaceHelper;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.LocalFaceEmbeddingService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDateTime;

/**
 * Face ID Enrollment — 3-step capture with professional overlay.
 * Same interface as login. No Python needed.
 */
public class FaceIdEnrollController {

    @FXML private ImageView   cameraView;
    @FXML private Label       stepTitleLabel;
    @FXML private Label       stepInstructionLabel;
    @FXML private Label       stepCounterLabel;
    @FXML private ProgressBar captureProgress;
    @FXML private Label       statusLabel;
    @FXML private Label       countdownLabel;
    @FXML private Button      startButton;
    @FXML private Button      cancelButton;
    @FXML private Circle      dot1, dot2, dot3;

    private static final String[][] STEPS = {
        {"Look Straight Ahead", "Center your face in the oval"},
        {"Turn Slightly Left",  "Turn your head slightly to the left"},
        {"Turn Slightly Right", "Turn your head slightly to the right"}
    };

    private int     currentStep = 0;
    private boolean capturing   = false;
    private final double[][] capturedEmbeddings = new double[3][];

    private final CameraFaceHelper        camera    = new CameraFaceHelper();
    private final LocalFaceEmbeddingService localEmb = new LocalFaceEmbeddingService();
    private final FaceEmbeddingService    cryptoSvc = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embRepo   = new FaceEmbeddingRepository();
    private final UserRepository          userRepo  = new UserRepository();

    private User     targetUser;
    private Runnable onEnrolled;
    private AnimationTimer previewTimer;

    public void setTargetUser(User u)     { this.targetUser = u; }
    public void setOnEnrolled(Runnable r) { this.onEnrolled = r; }

    @FXML
    public void initialize() {
        updateStepUI();
        embRepo.ensureTableExists();
        setStatus("📷 Opening camera...", false);
        if (startButton != null) startButton.setDisable(true);

        new Thread(() -> {
            boolean ok = camera.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready — align your face, then click Capture.", false);
                    if (startButton != null) startButton.setDisable(false);
                    startPreview();
                } else {
                    setStatus("❌ Camera unavailable. Click Retry.", false);
                    if (startButton != null) {
                        startButton.setText("🔄 Retry Camera");
                        startButton.setDisable(false);
                        startButton.setOnAction(e -> retryCamera());
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
                WritableImage frame = camera.grabWithOverlay(currentStep);
                if (frame != null && cameraView != null) cameraView.setImage(frame);
            }
        };
        previewTimer.start();
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

        String done = "-fx-fill:#2E7D32;", cur = "-fx-fill:#4CAF50;", pend = "-fx-fill:#C8E6C9;";
        if (dot1 != null) dot1.setStyle(currentStep > 0 ? done : cur);
        if (dot2 != null) dot2.setStyle(currentStep > 1 ? done : currentStep == 1 ? cur : pend);
        if (dot3 != null) dot3.setStyle(currentStep > 2 ? done : currentStep == 2 ? cur : pend);
    }

    @FXML
    private void handleCapture() {
        if (capturing) return;
        capturing = true;
        if (startButton != null) startButton.setDisable(true);
        setStatus("📸 Hold still...", false);
        runCountdown(3);
    }

    private void runCountdown(int n) {
        if (n <= 0) {
            if (countdownLabel != null) countdownLabel.setText("");
            doCapture();
            return;
        }
        if (countdownLabel != null) {
            countdownLabel.setText(String.valueOf(n));
            countdownLabel.setStyle("-fx-font-size:72px;-fx-font-weight:bold;-fx-text-fill:#4CAF50;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.7),10,0,0,2);");
        }
        PauseTransition p = new PauseTransition(Duration.seconds(1));
        p.setOnFinished(e -> runCountdown(n - 1));
        p.play();
    }

    private void doCapture() {
        setStatus("🤖 Analyzing face...", false);
        byte[] jpeg = camera.grabJpeg();

        if (jpeg == null || jpeg.length == 0) {
            setStatus("❌ Failed to capture. Try again.", false);
            capturing = false;
            if (startButton != null) startButton.setDisable(false);
            return;
        }

        new Thread(() -> {
            try {
                double[] emb = localEmb.extractEmbedding(jpeg);

                Platform.runLater(() -> {
                    capturedEmbeddings[currentStep] = emb;
                    capturing = false;
                    if (captureProgress != null) captureProgress.setProgress((currentStep + 1) / 3.0);
                    setStatus("✅ Step " + (currentStep + 1) + " captured!", true);

                    if (currentStep < 2) {
                        currentStep++;
                        updateStepUI();
                        PauseTransition delay = new PauseTransition(Duration.millis(700));
                        delay.setOnFinished(e -> {
                            if (startButton != null) startButton.setDisable(false);
                            setStatus("✅ Good! Now: " + STEPS[currentStep][1], false);
                        });
                        delay.play();
                    } else {
                        saveEmbedding();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    capturing = false;
                    setStatus("❌ " + e.getMessage(), false);
                    if (startButton != null) startButton.setDisable(false);
                });
            }
        }).start();
    }

    private void saveEmbedding() {
        setStatus("🔍 Verifying face uniqueness...", false);
        if (captureProgress != null) captureProgress.setProgress(0.85);

        new Thread(() -> {
            try {
                // Average 3 embeddings + L2 normalize
                int len = capturedEmbeddings[0].length;
                double[] avg = new double[len];
                for (double[] d : capturedEmbeddings) for (int i = 0; i < len; i++) avg[i] += d[i];
                for (int i = 0; i < len; i++) avg[i] /= 3.0;
                double norm = 0; for (double v : avg) norm += v * v; norm = Math.sqrt(norm);
                if (norm > 0) for (int i = 0; i < len; i++) avg[i] /= norm;

                User user = targetUser != null ? targetUser : Session.getCurrentUser();
                if (user == null) { Platform.runLater(() -> setStatus("❌ No user session.", false)); return; }

                // Security: check for duplicate face
                for (FaceEmbeddingRepository.UserEmbedding ue : embRepo.findAllActiveEmbeddings()) {
                    if (ue.userId == user.getId()) continue;
                    try {
                        double[] existing = cryptoSvc.decrypt(ue.encryptedB64, ue.ivB64, ue.tagB64);
                        if (localEmb.similarity(avg, existing) > 0.82) {
                            User eu = userRepo.findById(ue.userId);
                            String name = eu != null ? eu.getFirstName() + " " + eu.getLastName() : "User " + ue.userId;
                            Platform.runLater(() -> {
                                setStatus("❌ Security Alert: Face already registered!", false);
                                Stage s = (Stage) cameraView.getScene().getWindow();
                                Toasts.show(s, "⚠️ This face is already enrolled for: " + name, Toasts.Type.ERROR);
                                if (startButton != null) { startButton.setDisable(false); startButton.setText("Try Again"); }
                                currentStep = 0; updateStepUI();
                            });
                            return;
                        }
                    } catch (Exception ignored) {}
                }

                // Save encrypted embedding
                Platform.runLater(() -> { setStatus("🔐 Saving face data...", false); if (captureProgress != null) captureProgress.setProgress(0.95); });
                FaceEmbeddingService.EncryptedEmbedding enc = cryptoSvc.encrypt(avg);
                embRepo.saveEmbedding(user.getId(), enc.encryptedB64, enc.ivB64, enc.tagB64);

                // Update user face descriptor
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < avg.length; i++) { sb.append(String.format("%.8f", avg[i])); if (i < avg.length - 1) sb.append(","); }
                sb.append("]");
                userRepo.updateFaceDescriptor(user.getId(), sb.toString(), LocalDateTime.now());
                user.setFaceDescriptor(sb.toString());
                user.setFaceIdEnrolledAt(LocalDateTime.now());
                if (targetUser == null) Session.login(user);

                Platform.runLater(() -> {
                    if (captureProgress != null) captureProgress.setProgress(1.0);
                    setStatus("✅ Face ID enrolled successfully!", true);
                    stopCamera();
                    Stage s = (Stage) cameraView.getScene().getWindow();
                    Toasts.show(s, "Face ID enrolled!", Toasts.Type.SUCCESS);
                    if (onEnrolled != null) onEnrolled.run();
                    PauseTransition close = new PauseTransition(Duration.millis(1500));
                    close.setOnFinished(e -> s.close());
                    close.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("❌ " + e.getMessage(), false);
                    if (startButton != null) { startButton.setDisable(false); startButton.setText("Retry"); }
                    currentStep = 0; updateStepUI();
                });
            }
        }).start();
    }

    private void stopCamera() {
        if (previewTimer != null) { previewTimer.stop(); previewTimer = null; }
        new Thread(camera::close).start();
    }

    private void retryCamera() {
        if (startButton != null) startButton.setDisable(true);
        setStatus("📷 Retrying camera...", false);
        new Thread(() -> {
            camera.close();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            boolean ok = camera.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready.", false);
                    if (startButton != null) { startButton.setDisable(false); startButton.setText("▶  Capture"); startButton.setOnAction(e -> handleCapture()); }
                    startPreview();
                } else {
                    setStatus("❌ Camera still unavailable.", false);
                    if (startButton != null) startButton.setDisable(false);
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
