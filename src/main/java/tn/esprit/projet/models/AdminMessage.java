package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Model for messages sent from admin to users
 */
public class AdminMessage {
    private int id;
    private int userId;              // Recipient user ID
    private int adminId;             // Sender admin ID
    private String message;          // Message content
    private boolean sentViaSms;      // Was SMS sent?
    private boolean isRead;          // Has user read the message?
    private LocalDateTime sentAt;    // When was it sent
    private LocalDateTime readAt;    // When was it read
    private String smsStatus;        // Twilio SMS status (sent, failed, delivered)
    private String smsId;            // Twilio message SID

    // Constructors
    public AdminMessage() {}

    public AdminMessage(int userId, int adminId, String message, boolean sentViaSms) {
        this.userId = userId;
        this.adminId = adminId;
        this.message = message;
        this.sentViaSms = sentViaSms;
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSentViaSms() {
        return sentViaSms;
    }

    public void setSentViaSms(boolean sentViaSms) {
        this.sentViaSms = sentViaSms;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(String smsStatus) {
        this.smsStatus = smsStatus;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }

    @Override
    public String toString() {
        return "AdminMessage{" +
                "id=" + id +
                ", userId=" + userId +
                ", adminId=" + adminId +
                ", message='" + message + '\'' +
                ", sentViaSms=" + sentViaSms +
                ", isRead=" + isRead +
                ", sentAt=" + sentAt +
                '}';
    }
}
