package tn.esprit.projet.models.faceid;

/**
 * Face ID API Response Model
 */
public class FaceIDResponse {
    private boolean success;
    private String message;
    private Integer userId;
    private Double similarity;
    private Double threshold;
    private String errorCode;

    public FaceIDResponse() {}

    public FaceIDResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static FaceIDResponse success(String message) {
        return new FaceIDResponse(true, message);
    }

    public static FaceIDResponse error(String message, String errorCode) {
        FaceIDResponse response = new FaceIDResponse(false, message);
        response.setErrorCode(errorCode);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Double getSimilarity() { return similarity; }
    public void setSimilarity(Double similarity) { this.similarity = similarity; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
}
