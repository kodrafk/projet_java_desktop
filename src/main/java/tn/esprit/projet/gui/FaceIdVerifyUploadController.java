package tn.esprit.projet.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.FaceEmbeddingRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.FaceEmbeddingService;
import tn.esprit.projet.utils.Session;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.List;

/**
 * Face ID verification via uploaded photo (no webcam required).
 * Alternative solution when webcam is unavailable.
 */
public class FaceIdVerifyUploadController {

    @FXML private ImageView imageView;
    @FXML private VBox placeholderBox;
    @FXML private Label statusLabel;
    @FXML private Label instructionLabel;
    @FXML private ProgressBar verifyProgress;
    @FXML private Button uploadButton;
    @FXML private Button verifyButton;
    @FXML private Button cancelButton;

    private final FaceEmbeddingService    embeddingService = new FaceEmbeddingService();
    private final FaceEmbeddingRepository embeddingRepo    = new FaceEmbeddingRepository();
    private final UserRepository          userRepo         = new UserRepository();

    private User     targetUser;
    private Runnable onSuccess;
    private Runnable onFailure;
    private File     selectedImageFile;
    private int      failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    public void setTargetUser(User u)    { this.targetUser = u; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }
    public void setOnFailure(Runnable r) { this.onFailure = r; }

    @FXML
    public void initialize() {
        embeddingRepo.ensureTableExists();
        if (verifyProgress != null) verifyProgress.setProgress(0);
        setStatus("📁 Choose a photo of your face to verify", false);
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Face Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) uploadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            loadImage(file);
        }
    }

    private void loadImage(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            
            if (placeholderBox != null) {
                placeholderBox.setVisible(false);
                placeholderBox.setManaged(false);
            }

            if (verifyButton != null) {
                verifyButton.setDisable(false);
            }

            setStatus("✅ Photo loaded. Click 'Verify Face' to authenticate.", false);

        } catch (Exception e) {
            setStatus("❌ Error loading image: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerify() {
        if (selectedImageFile == null) {
            setStatus("❌ Please select a photo first", false);
            return;
        }

        if (failedAttempts >= MAX_ATTEMPTS) {
            setStatus("❌ Too many failed attempts. Please use password login.", false);
            if (verifyButton != null) verifyButton.setDisable(true);
            return;
        }

        if (verifyButton != null) verifyButton.setDisable(true);
        if (uploadButton != null) uploadButton.setDisable(true);
        setStatus("🤖 Analyzing face with AI...", false);
        if (verifyProgress != null) verifyProgress.setProgress(0.2);

        new Thread(() -> {
            try {
                // Read image file
                BufferedImage bufferedImage = ImageIO.read(selectedImageFile);
                if (bufferedImage == null) {
                    throw new Exception("Could not read image file");
                }

                // Convert to JPEG bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Extract embedding
                String b64 = Base64.getEncoder().encodeToString(imageBytes);
                String req = "{\"command\":\"encode\",\"image\":\"" + b64 + "\"}";
                double[] liveEmbedding = embeddingService.callPythonForEmbedding(req);

                // L2 normalize
                double norm = 0;
                for (double v : liveEmbedding) norm += v * v;
                norm = Math.sqrt(norm);
                if (norm > 0) {
                    for (int i = 0; i < liveEmbedding.length; i++) {
                        liveEmbedding[i] /= norm;
                    }
                }

                final double[] finalLive = liveEmbedding;

                Platform.runLater(() -> {
                    if (verifyProgress != null) verifyProgress.setProgress(0.5);
                    setStatus("🔍 Comparing against enrolled users...", false);
                });

                // Get users to compare
                List<int[]> userIds = targetUser != null
                        ? List.of(new int[]{targetUser.getId()})
                        : embeddingRepo.findAllActiveUserIds();

                if (userIds.isEmpty()) {
                    Platform.runLater(() -> {
                        setStatus("❌ No Face ID users enrolled yet.", false);
                        if (verifyButton != null) verifyButton.setDisable(false);
                        if (uploadButton != null) uploadButton.setDisable(false);
                    });
                    return;
                }

                User   bestMatch   = null;
                double bestCosine  = -1;
                double secondBest  = -1;

                for (int[] row : userIds) {
                    String[] stored = embeddingRepo.findByUserId(row[0]);
                    if (stored == null) continue;

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

                final double THRESHOLD = 0.65;
                final double MARGIN    = 0.05;
                final boolean isMatch  = bestCosine >= THRESHOLD
                        && (secondBest < 0 || (bestCosine - secondBest) >= MARGIN);

                final User   matched = isMatch ? bestMatch : null;
                final double finalCos = bestCosine;
                final double simPct   = Math.max(0, finalCos) * 100;

                System.out.println("[FaceID Upload] Best match: " + (bestMatch != null ? bestMatch.getEmail() : "none"));
                System.out.println("[FaceID Upload] Similarity: " + String.format("%.2f%%", simPct));
                System.out.println("[FaceID Upload] Match: " + (isMatch ? "✅ YES" : "❌ NO"));

                embeddingRepo.logAttempt(
                        matched != null ? matched.getId() : null,
                        matched != null ? matched.getEmail() : null,
                        isMatch, finalCos);

                Platform.runLater(() -> handleResult(isMatch, matched, finalCos, simPct));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("❌ " + e.getMessage(), false);
                    if (verifyButton != null) verifyButton.setDisable(false);
                    if (uploadButton != null) uploadButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleResult(boolean match, User user, double cosine, double simPct) {
        if (verifyProgress != null) verifyProgress.setProgress(1.0);

        if (match && user != null) {
            embeddingRepo.updateLastUsed(user.getId());
            setStatus("✅ Welcome, " + user.getFirstName() + "!  (" +
                    String.format("%.1f%%", simPct) + " match)", true);
            Session.login(user);
            PauseTransition delay = new PauseTransition(Duration.millis(900));
            delay.setOnFinished(e -> {
                ((Stage) imageView.getScene().getWindow()).close();
                if (onSuccess != null) onSuccess.run();
            });
            delay.play();
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;
            setStatus("❌ Face not recognized. (" + String.format("%.1f%%", simPct) + " match)" +
                    (remaining > 0 ? "  " + remaining + " attempts left." : "  No more attempts."), false);
            
            if (verifyButton != null) verifyButton.setDisable(remaining <= 0);
            if (uploadButton != null) uploadButton.setDisable(remaining <= 0);
            
            if (remaining <= 0 && onFailure != null) onFailure.run();
        }
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
        ((Stage) imageView.getScene().getWindow()).close();
    }
}
