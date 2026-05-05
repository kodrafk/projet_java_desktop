package tn.esprit.projet.gui.faceid;

/**
 * Face ID UI States
 * Defines all possible states during authentication flow
 */
public enum FaceIDState {
    INITIALIZING("Initializing camera...", "#6B7280"),
    CAMERA_READY("Position your face in the circle", "#2563EB"),
    FACE_DETECTED("Face detected - Hold still", "#10B981"),
    LIVENESS_CHECK("Please blink", "#F59E0B"),
    ANALYZING("Analyzing...", "#2563EB"),
    SUCCESS("Authentication successful ✓", "#10B981"),
    FAILED("Authentication failed", "#EF4444"),
    
    // Error states
    NO_FACE("No face detected", "#EF4444"),
    MULTIPLE_FACES("Multiple faces detected", "#EF4444"),
    FACE_TOO_FAR("Move closer to camera", "#F59E0B"),
    FACE_TOO_CLOSE("Move away from camera", "#F59E0B"),
    LOW_LIGHT("Insufficient lighting", "#F59E0B"),
    LIVENESS_FAILED("Liveness check failed", "#EF4444"),
    NO_MATCH("Face not recognized", "#EF4444"),
    TIMEOUT("Time limit exceeded", "#EF4444"),
    ERROR("An error occurred", "#EF4444");

    private final String message;
    private final String color;

    FaceIDState(String message, String color) {
        this.message = message;
        this.color = color;
    }

    public String getMessage() {
        return message;
    }

    public String getColor() {
        return color;
    }

    public boolean isError() {
        return this == FAILED || this == NO_FACE || this == MULTIPLE_FACES ||
               this == FACE_TOO_FAR || this == FACE_TOO_CLOSE || this == LOW_LIGHT ||
               this == LIVENESS_FAILED || this == NO_MATCH || this == TIMEOUT || this == ERROR;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFinal() {
        return isSuccess() || isError();
    }
}
