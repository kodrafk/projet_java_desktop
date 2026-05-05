package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * PersonalizedMessage — Messages personnalisés envoyés par l'admin aux utilisateurs
 * Affichés dans la section objectives du front office
 */
public class PersonalizedMessage {

    private int id;
    private int userId;              // Destinataire
    private int adminId;             // Expéditeur (admin)
    private String content;          // Contenu du message
    private boolean sendViaSms;      // Envoyé par SMS ?
    private String smsStatus;        // Statut SMS (sent, failed, no_phone, etc.)
    private String smsId;            // Twilio message SID
    private boolean isRead;          // Lu par l'utilisateur ?
    private LocalDateTime sentAt;    // Date d'envoi
    private LocalDateTime readAt;    // Date de lecture

    public PersonalizedMessage() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    public PersonalizedMessage(int userId, int adminId, String content, boolean sendViaSms) {
        this();
        this.userId = userId;
        this.adminId = adminId;
        this.content = content;
        this.sendViaSms = sendViaSms;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isSendViaSms() { return sendViaSms; }
    public void setSendViaSms(boolean sendViaSms) { this.sendViaSms = sendViaSms; }

    public String getSmsStatus() { return smsStatus; }
    public void setSmsStatus(String smsStatus) { this.smsStatus = smsStatus; }

    public String getSmsId() { return smsId; }
    public void setSmsId(String smsId) { this.smsId = smsId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
