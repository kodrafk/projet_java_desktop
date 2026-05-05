package tn.esprit.projet.models.faceid;

/**
 * Face ID API Request Model
 */
public class FaceIDRequest {
    private Integer userId;
    private double[] embedding;
    private double livenessScore;
    private String deviceId;
    private String ipAddress;

    public FaceIDRequest() {}

    public FaceIDRequest(Integer userId, double[] embedding, double livenessScore) {
        this.userId = userId;
        this.embedding = embedding;
        this.livenessScore = livenessScore;
    }

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }

    public double getLivenessScore() { return livenessScore; }
    public void setLivenessScore(double livenessScore) { this.livenessScore = livenessScore; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
