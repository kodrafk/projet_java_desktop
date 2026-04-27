package tn.esprit.projet.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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
import tn.esprit.projet.services.FaceCameraOverlay;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.services.WebcamService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Face ID enrollment — sarxos webcam + Python DeepFace (ArcFace).
 * Shows face-guide oval overlay. 3 steps: straight → left → right.
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
        {"Turn Left",           "Turn your head slightly to the left"},
        {"Turn Right",          "Turn your head slightly to the right"}
    };

    private int     currentStep = 0;
    private boolean capturing   = false;
    private final double[][] capturedEmbeddings = new double[3][];

    private final FaceEmbeddingService    embeddingService = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embeddingRepo    = new FaceEmbeddingRepository();
    private final UserRepository          userRepo         = new UserRepository();
    private final WebcamService           webcam           = new WebcamService();
    private final ObjectMapper            mapper           = new ObjectMapper();

    private User     targetUser;
    private Runnable onEnrolled;
    private AnimationTimer previewTimer;

    public void setTargetUser(User u)     { this.targetUser = u; }
    public void setOnEnrolled(Runnable r) { this.onEnrolled = r; }

    @FXML
    public void initialize() {
        updateStepUI();
        // Ensure DB tables exist
        embeddingRepo.ensureTableExists();
        setStatus("📷 Opening camera...", false);
        if (startButton != null) startButton.setDisable(true);

        new Thread(() -> {
            boolean ok = webcam.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready — align your face in the oval, then click Capture.", false);
                    if (startButton != null) startButton.setDisable(false);
                    startPreview();
                } else {
                    setStatus("❌ Camera unavailable. Close Teams/Zoom/browser and click Retry.", false);
                    if (startButton != null) {
                        startButton.setText("🔄 Retry Camera");
                        startButton.setDisable(false);
                        startButton.setOnAction(e -> retryCamera());
                    }
                }
            });
        }).start();
    }

    // ── Live preview with face-guide overlay ──────────────────────────────────

    private void startPreview() {
        previewTimer = new AnimationTimer() {
            long last = 0;
            @Override public void handle(long now) {
                if (now - last > 50_000_000L) { // ~20 fps
                    WritableImage raw = webcam.grabFrame();
                    if (raw != null && cameraView != null) {
                        WritableImage overlaid = FaceCameraOverlay.draw(raw, !capturing, currentStep);
                        cameraView.setImage(overlaid);
                    }
                    last = now;
                }
            }
        };
        previewTimer.start();
    }

    // ── Step UI ───────────────────────────────────────────────────────────────

    private void updateStepUI() {
        if (stepTitleLabel != null)
            stepTitleLabel.setText("Step " + (currentStep + 1) + " of 3 — " + STEPS[currentStep][0]);
        if (stepInstructionLabel != null)
            stepInstructionLabel.setText(STEPS[currentStep][1]);
        if (stepCounterLabel != null)
            stepCounterLabel.setText("Step " + (currentStep + 1) + " / 3");
        if (captureProgress != null)
            captureProgress.setProgress(currentStep / 3.0);

        String done    = "-fx-fill:#2E7D32;";
        String current = "-fx-fill:#4CAF50;";
        String pending = "-fx-fill:#C8E6C9;";
        if (dot1 != null) dot1.setStyle(currentStep > 0 ? done : current);
        if (dot2 != null) dot2.setStyle(currentStep > 1 ? done : currentStep == 1 ? current : pending);
        if (dot3 != null) dot3.setStyle(currentStep > 2 ? done : currentStep == 2 ? current : pending);
    }

    // ── Capture ───────────────────────────────────────────────────────────────

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
        byte[] jpeg = webcam.grabFrameAsJpeg();

        if (jpeg == null || jpeg.length == 0) {
            setStatus("❌ Failed to capture. Try again.", false);
            capturing = false;
            if (startButton != null) startButton.setDisable(false);
            return;
        }

        new Thread(() -> {
            try {
                String b64 = Base64.getEncoder().encodeToString(jpeg);
                String req = "{\"command\":\"encode\",\"image\":\"" + b64 + "\"}";
                double[] embedding = embeddingService.callPythonForEmbedding(req);

                Platform.runLater(() -> {
                    capturedEmbeddings[currentStep] = embedding;
                    capturing = false;

                    if (captureProgress != null)
                        captureProgress.setProgress((currentStep + 1) / 3.0);

                    setStatus("✅ Step " + (currentStep + 1) + " captured!", true);

                    if (currentStep < 2) {
                        currentStep++;
                        updateStepUI();
                        PauseTransition delay = new PauseTransition(Duration.millis(800));
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

    // ── Save ──────────────────────────────────────────────────────────────────

    private void saveEmbedding() {
        setStatus("🔐 Checking for duplicates...", false);
        if (captureProgress != null) captureProgress.setProgress(0.85);

        new Thread(() -> {
            try {
                // Average the 3 embeddings for robustness
                int len = capturedEmbeddings[0].length;
                double[] avg = new double[len];
                for (double[] d : capturedEmbeddings)
                    for (int i = 0; i < len; i++) avg[i] += d[i];
                for (int i = 0; i < len; i++) avg[i] /= 3.0;

                // L2 normalize
                double norm = 0;
                for (double v : avg) norm += v * v;
                norm = Math.sqrt(norm);
                if (norm > 0) for (int i = 0; i < len; i++) avg[i] /= norm;

                User user = targetUser != null ? targetUser : Session.getCurrentUser();
                if (user == null) {
                    Platform.runLater(() -> setStatus("❌ No user session.", false));
                    return;
                }

                // ── SECURITY CHECK: Verify this face is not already enrolled by another user ──
                Platform.runLater(() -> setStatus("🔍 Verifying face uniqueness...", false));
                
                java.util.List<FaceEmbeddingRepository.UserEmbedding> allEmbeddings = embeddingRepo.findAllActiveEmbeddings();
                
                for (FaceEmbeddingRepository.UserEmbedding ue : allEmbeddings) {
                    // Skip if it's the same user (re-enrollment is allowed)
                    if (ue.userId == user.getId()) continue;
                    
                    try {
                        // Decrypt existing embedding
                        double[] existingEmb = embeddingService.decrypt(ue.encryptedB64, ue.ivB64, ue.tagB64);
                        
                        // Calculate cosine similarity
                        double similarity = cosineSimilarity(avg, existingEmb);
                        
                        // If similarity is too high (threshold 0.6), it's the same person
                        if (similarity > 0.6) {
                            User existingUser = userRepo.findById(ue.userId);
                            String existingUserName = existingUser != null 
                                ? existingUser.getFirstName() + " " + existingUser.getLastName()
                                : "User ID " + ue.userId;
                            
                            Platform.runLater(() -> {
                                setStatus("❌ Security Alert: This face is already registered!", false);
                                Stage stage = (Stage) cameraView.getScene().getWindow();
                                Toasts.show(stage, 
                                    "⚠️ This face is already enrolled for another account (" + existingUserName + "). " +
                                    "Each face can only be used for one account.", 
                                    Toasts.Type.ERROR);
                                
                                // Reset to allow retry
                                if (startButton != null) { 
                                    startButton.setDisable(false); 
                                    startButton.setText("Try Again"); 
                                }
                                currentStep = 0;
                                updateStepUI();
                            });
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("[Security] Could not decrypt embedding for user " + ue.userId + ": " + e.getMessage());
                    }
                }

                // ── No duplicate found, proceed with enrollment ──
                Platform.runLater(() -> setStatus("🔐 Encrypting and saving face data...", false));
                if (captureProgress != null) Platform.runLater(() -> captureProgress.setProgress(0.95));

                FaceEmbeddingService.EncryptedEmbedding enc = embeddingService.encrypt(avg);
                embeddingRepo.saveEmbedding(user.getId(), enc.encryptedB64, enc.ivB64, enc.tagB64);

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < avg.length; i++) {
                    sb.append(String.format("%.8f", avg[i]));
                    if (i < avg.length - 1) sb.append(",");
                }
                sb.append("]");
                userRepo.updateFaceDescriptor(user.getId(), sb.toString(), LocalDateTime.now());
                user.setFaceDescriptor(sb.toString());
                user.setFaceIdEnrolledAt(LocalDateTime.now());
                if (targetUser == null) Session.login(user);

                Platform.runLater(() -> {
                    if (captureProgress != null) captureProgress.setProgress(1.0);
                    setStatus("✅ Face ID enrolled successfully!", true);
                    stopCamera();
                    Stage stage = (Stage) cameraView.getScene().getWindow();
                    Toasts.show(stage, "Face ID enrolled!", Toasts.Type.SUCCESS);
                    if (onEnrolled != null) onEnrolled.run();
                    PauseTransition close = new PauseTransition(Duration.millis(1500));
                    close.setOnFinished(e -> stage.close());
                    close.play();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("❌ Save failed: " + e.getMessage(), false);
                    if (startButton != null) { startButton.setDisable(false); startButton.setText("Retry"); }
                    currentStep = 0;
                    updateStepUI();
                });
            }
        }).start();
    }
    
    // ── Calculate cosine similarity between two embeddings ────────────────────
    
    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (normA * normB);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void stopCamera() {
        if (previewTimer != null) { previewTimer.stop(); previewTimer = null; }
        new Thread(webcam::close).start();
    }

    private void retryCamera() {
        if (startButton != null) startButton.setDisable(true);
        setStatus("📷 Retrying camera...", false);
        new Thread(() -> {
            webcam.close();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            boolean ok = webcam.open();
            Platform.runLater(() -> {
                if (ok) {
                    setStatus("✅ Camera ready — align your face in the oval.", false);
                    if (startButton != null) {
                        startButton.setDisable(false);
                        startButton.setText("▶  Capture");
                        startButton.setOnAction(e -> handleCapture());
                    }
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
