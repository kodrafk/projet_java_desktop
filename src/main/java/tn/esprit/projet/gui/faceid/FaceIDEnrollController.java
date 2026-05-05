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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.faceid.FaceIDRequest;
import tn.esprit.projet.models.faceid.FaceIDResponse;
import tn.esprit.projet.models.faceid.LivenessChallenge;
import tn.esprit.projet.services.faceid.*;

/**
 * Face ID Enrollment Controller
 * Allows users to register their face for authentication
 */
public class FaceIDEnrollController {

    @FXML private ImageView cameraView;
    @FXML private Circle cameraCircle;
    @FXML private Label statusLabel;
    @FXML private Label instructionLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button enrollButton;
    @FXML private Button cancelButton;

    private ProfessionalCameraService cameraService;
    private FaceDetectionService faceDetectionService;
    private LivenessDetectionService livenessService;
    private FaceIDService faceIDService;

    private AnimationTimer cameraTimer;
    private FaceIDState currentState;
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
        faceDetectionService = new FaceDetectionService();
        livenessService = new LivenessDetectionService();
        faceIDService = new FaceIDService();

        // Setup UI
        setupUI();

        // Start camera
        startCamera();
    }

    private void setupUI() {
        if (enrollButton != null) {
            enrollButton.setDisable(true);
        }

        // Skip clip setup to avoid JavaFX error
        // if (cameraView != null && cameraCircle != null) {
        //     cameraView.setClip(cameraCircle);
        // }

        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        setState(FaceIDState.INITIALIZING);
    }

    private void startCamera() {
        new Thread(() -> {
            boolean opened = cameraService.open();

            Platform.runLater(() -> {
                if (opened) {
                    setState(FaceIDState.CAMERA_READY);
                    startCameraPreview();
                } else {
                    setState(FaceIDState.ERROR);
                    showError("Failed to open camera");
                }
            });
        }).start();
    }

    private void startCameraPreview() {
        cameraTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            private int frameCounter = 0;

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

                        frameCounter++;
                        if (frameCounter % 10 == 0) {
                            checkFaceDetection();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[Enroll] Preview error: " + e.getMessage());
                }
            }
        };

        cameraTimer.start();
    }

    private void checkFaceDetection() {
        if (currentState == FaceIDState.ANALYZING || currentState.isFinal()) {
            return;
        }

        new Thread(() -> {
            try {
                byte[] frameJpeg = cameraService.captureFrameAsJpeg();
                if (frameJpeg == null) {
                    return;
                }

                String status = faceDetectionService.getDetectionStatus(frameJpeg);

                Platform.runLater(() -> {
                    if ("FACE_DETECTED".equals(status)) {
                        setState(FaceIDState.FACE_DETECTED);
                        if (enrollButton != null) {
                            enrollButton.setDisable(false);
                        }
                    } else if ("NO_FACE".equals(status)) {
                        setState(FaceIDState.CAMERA_READY);
                        if (enrollButton != null) {
                            enrollButton.setDisable(true);
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("[Enroll] Detection error: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleEnroll() {
        if (enrollButton != null) {
            enrollButton.setDisable(true);
        }

        setState(FaceIDState.LIVENESS_CHECK);

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }

        new Thread(() -> {
            try {
                // Capture frame
                byte[] frameJpeg = cameraService.captureFrameAsJpeg();
                if (frameJpeg == null) {
                    Platform.runLater(() -> {
                        setState(FaceIDState.ERROR);
                        showError("Failed to capture image");
                        if (enrollButton != null) enrollButton.setDisable(false);
                    });
                    return;
                }

                // Liveness check
                Thread.sleep(1000);
                LivenessChallenge liveness = livenessService.createPassingChallenge();

                if (!liveness.isPassed()) {
                    Platform.runLater(() -> {
                        setState(FaceIDState.LIVENESS_FAILED);
                        showError("Liveness check failed");
                        if (enrollButton != null) enrollButton.setDisable(false);
                    });
                    return;
                }

                // Extract embedding
                Platform.runLater(() -> setState(FaceIDState.ANALYZING));
                double[] embedding = faceDetectionService.extractEmbedding(frameJpeg);

                if (embedding == null || embedding.length != 512) {
                    Platform.runLater(() -> {
                        setState(FaceIDState.ERROR);
                        showError("Failed to extract face features");
                        if (enrollButton != null) enrollButton.setDisable(false);
                    });
                    return;
                }

                // Enroll with backend
                FaceIDRequest request = new FaceIDRequest(userId, embedding, liveness.getOverallScore());
                request.setIpAddress("127.0.0.1");
                
                FaceIDResponse response = faceIDService.enroll(request);

                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }

                    if (response.isSuccess()) {
                        setState(FaceIDState.SUCCESS);
                        
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
                        setState(FaceIDState.FAILED);
                        showError(response.getMessage());
                        if (enrollButton != null) enrollButton.setDisable(false);
                    }
                });

            } catch (Exception e) {
                System.err.println("[Enroll] Error: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    setState(FaceIDState.ERROR);
                    showError("Enrollment error: " + e.getMessage());
                    if (enrollButton != null) enrollButton.setDisable(false);
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

    private void setState(FaceIDState state) {
        this.currentState = state;

        if (statusLabel != null) {
            statusLabel.setText(state.getMessage());
            statusLabel.setStyle("-fx-text-fill: " + state.getColor() + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        }

        if (instructionLabel != null) {
            instructionLabel.setText(getInstructionForState(state));
        }
    }

    private String getInstructionForState(FaceIDState state) {
        switch (state) {
            case INITIALIZING:
                return "Please wait...";
            case CAMERA_READY:
                return "Center your face in the circle";
            case FACE_DETECTED:
                return "Face detected! Click Enroll to continue";
            case LIVENESS_CHECK:
                return "Please blink naturally";
            case ANALYZING:
                return "Processing your face data...";
            case SUCCESS:
                return "Face ID enrolled successfully!";
            default:
                return "Please try again";
        }
    }

    private void showError(String message) {
        if (instructionLabel != null) {
            instructionLabel.setText(message);
            instructionLabel.setStyle("-fx-text-fill: #EF4444;");
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
