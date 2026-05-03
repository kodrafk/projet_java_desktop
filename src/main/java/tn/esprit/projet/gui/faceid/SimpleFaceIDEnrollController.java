package tn.esprit.projet.gui.faceid;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.services.faceid.ProfessionalCameraService;
import tn.esprit.projet.services.faceid.SimpleFaceRecognitionService;

/**
 * Simple Face ID Enrollment Controller
 * Uses Python server for face recognition
 */
public class SimpleFaceIDEnrollController {

    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private Label instructionLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button captureButton;
    @FXML private Button cancelButton;

    private ProfessionalCameraService cameraService;
    private SimpleFaceRecognitionService faceRecognitionService;
    private AnimationTimer cameraTimer;
    
    private int userId;
    private Runnable onSuccess;
    private Runnable onCancel;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }

    @FXML
    public void initialize() {
        // Initialize services
        cameraService = new ProfessionalCameraService();
        faceRecognitionService = new SimpleFaceRecognitionService();

        // Check if Python server is running
        if (!faceRecognitionService.isServerRunning()) {
            Platform.runLater(() -> {
                statusLabel.setText("ERROR: Python server not running!");
                statusLabel.setStyle("-fx-text-fill: #EF4444;");
                instructionLabel.setText("Please start: python_face_server/START_SERVER.bat");
            });
            return;
        }

        // Setup UI
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        // Start camera
        startCamera();
    }

    private void startCamera() {
        new Thread(() -> {
            boolean opened = cameraService.open();

            Platform.runLater(() -> {
                if (opened) {
                    statusLabel.setText("Camera ready");
                    statusLabel.setStyle("-fx-text-fill: #10B981;");
                    instructionLabel.setText("Position your face in the circle and click Capture");
                    startCameraPreview();
                } else {
                    statusLabel.setText("Failed to open camera");
                    statusLabel.setStyle("-fx-text-fill: #EF4444;");
                    instructionLabel.setText("Please check camera permissions");
                }
            });
        }).start();
    }

    private void startCameraPreview() {
        cameraTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 33_000_000) {
                    return;
                }
                lastUpdate = now;

                try {
                    Image frame = cameraService.captureFrame();
                    if (frame != null && cameraView != null) {
                        cameraView.setImage(frame);
                    }
                } catch (Exception e) {
                    System.err.println("[Enroll] Preview error: " + e.getMessage());
                }
            }
        };

        cameraTimer.start();
    }

    @FXML
    private void handleCapture() {
        if (captureButton != null) {
            captureButton.setDisable(true);
        }

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        statusLabel.setText("Capturing...");
        statusLabel.setStyle("-fx-text-fill: #2563EB;");

        new Thread(() -> {
            try {
                // Capture frame
                byte[] frameJpeg = cameraService.captureFrameAsJpeg();
                
                if (frameJpeg == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to capture image");
                        statusLabel.setStyle("-fx-text-fill: #EF4444;");
                        if (captureButton != null) captureButton.setDisable(false);
                        if (progressIndicator != null) progressIndicator.setVisible(false);
                    });
                    return;
                }

                // Enroll with Python server
                Platform.runLater(() -> {
                    statusLabel.setText("Enrolling face...");
                    statusLabel.setStyle("-fx-text-fill: #2563EB;");
                });

                boolean success = faceRecognitionService.enrollFace(userId, frameJpeg);

                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }

                    if (success) {
                        statusLabel.setText("✅ Face ID enrolled successfully!");
                        statusLabel.setStyle("-fx-text-fill: #10B981;");
                        instructionLabel.setText("You can now use Face ID to login");

                        // Close after 2 seconds
                        PauseTransition delay = new PauseTransition(Duration.seconds(2));
                        delay.setOnFinished(e -> {
                            cleanup();
                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                        });
                        delay.play();
                    } else {
                        statusLabel.setText("❌ Enrollment failed");
                        statusLabel.setStyle("-fx-text-fill: #EF4444;");
                        instructionLabel.setText("Please try again with better lighting");
                        if (captureButton != null) captureButton.setDisable(false);
                    }
                });

            } catch (Exception e) {
                System.err.println("[Enroll] Capture error: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #EF4444;");
                    if (captureButton != null) captureButton.setDisable(false);
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        cleanup();
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void cleanup() {
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        if (cameraService != null) {
            cameraService.close();
        }

        Stage stage = (Stage) cameraView.getScene().getWindow();
        stage.close();
    }
}
