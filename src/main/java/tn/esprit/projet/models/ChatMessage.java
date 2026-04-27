package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Bidirectional chat message between admin and user.
 * Both sides can send text + images, edit and delete their own messages.
 */
public class ChatMessage {

    public enum SenderType { ADMIN, USER }

    private int           id;
    private int           senderId;
    private int           receiverId;
    private SenderType    senderType;   // ADMIN or USER
    private String        content;      // text content (null if image-only)
    private String        imagePath;    // relative path under uploads/chat/
    private boolean       edited;
    private boolean       deleted;
    private boolean       isRead;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private LocalDateTime readAt;

    // SMS fields (only for admin→user messages)
    private boolean       sentViaSms;
    private String        smsStatus;

    public ChatMessage() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
        this.edited = false;
        this.deleted = false;
    }

    public ChatMessage(int senderId, int receiverId, SenderType senderType,
                       String content, String imagePath, boolean sentViaSms) {
        this();
        this.senderId   = senderId;
        this.receiverId = receiverId;
        this.senderType = senderType;
        this.content    = content;
        this.imagePath  = imagePath;
        this.sentViaSms = sentViaSms;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public int getSenderId()                  { return senderId; }
    public void setSenderId(int v)            { this.senderId = v; }

    public int getReceiverId()                { return receiverId; }
    public void setReceiverId(int v)          { this.receiverId = v; }

    public SenderType getSenderType()         { return senderType; }
    public void setSenderType(SenderType v)   { this.senderType = v; }

    public String getContent()                { return content; }
    public void setContent(String v)          { this.content = v; }

    public String getImagePath()              { return imagePath; }
    public void setImagePath(String v)        { this.imagePath = v; }

    public boolean isEdited()                 { return edited; }
    public void setEdited(boolean v)          { this.edited = v; }

    public boolean isDeleted()                { return deleted; }
    public void setDeleted(boolean v)         { this.deleted = v; }

    public boolean isRead()                   { return isRead; }
    public void setRead(boolean v)            { this.isRead = v; }

    public LocalDateTime getSentAt()          { return sentAt; }
    public void setSentAt(LocalDateTime v)    { this.sentAt = v; }

    public LocalDateTime getEditedAt()        { return editedAt; }
    public void setEditedAt(LocalDateTime v)  { this.editedAt = v; }

    public LocalDateTime getReadAt()          { return readAt; }
    public void setReadAt(LocalDateTime v)    { this.readAt = v; }

    public boolean isSentViaSms()             { return sentViaSms; }
    public void setSentViaSms(boolean v)      { this.sentViaSms = v; }

    public String getSmsStatus()              { return smsStatus; }
    public void setSmsStatus(String v)        { this.smsStatus = v; }

    public boolean isFromAdmin()              { return senderType == SenderType.ADMIN; }
    public boolean hasImage()                 { return imagePath != null && !imagePath.isBlank(); }
    public boolean hasText()                  { return content != null && !content.isBlank(); }
}
