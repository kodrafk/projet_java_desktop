package tn.esprit.projet.services;

import tn.esprit.projet.models.AdminMessage;
import tn.esprit.projet.models.PersonalizedMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.AdminMessageRepository;
import tn.esprit.projet.repository.PersonalizedMessageRepository;
import tn.esprit.projet.repository.UserRepository;

/**
 * Service for sending admin messages to users.
 * Saves to BOTH admin_messages (backoffice history) AND personalized_messages
 * (front-office "Messages from Coach" section).
 * SMS is sent via TwilioService (configured in twilio.properties).
 */
public class AdminMessageService {

    private final AdminMessageRepository    adminMsgRepo  = new AdminMessageRepository();
    private final PersonalizedMessageRepository coachMsgRepo = new PersonalizedMessageRepository();
    private final UserRepository            userRepo      = new UserRepository();
    private final TwilioService             twilio        = TwilioService.getInstance();

    public AdminMessageService() {
        adminMsgRepo.ensureTableExists();
        coachMsgRepo.ensureTableExists();
    }

    /**
     * Send a message from admin to user.
     * Persists in admin_messages (backoffice) AND personalized_messages (front-office).
     * Optionally sends an SMS via Twilio.
     *
     * @param adminId     ID of the sending admin
     * @param userId      ID of the recipient user
     * @param messageText Message content
     * @param sendSms     Whether to also send via SMS
     * @return The saved AdminMessage, or null on failure
     */
    public AdminMessage sendMessage(int adminId, int userId, String messageText, boolean sendSms) {

        // ── 1. Save to admin_messages (backoffice conversation history) ────────
        AdminMessage adminMsg = new AdminMessage(userId, adminId, messageText, sendSms);
        AdminMessage savedAdmin = adminMsgRepo.save(adminMsg);
        if (savedAdmin == null) {
            System.err.println("[AdminMessageService] ❌ Failed to save to admin_messages");
            return null;
        }

        // ── 2. Save to personalized_messages (front-office "Messages from Coach") ─
        PersonalizedMessage coachMsg = new PersonalizedMessage(userId, adminId, messageText, sendSms);
        PersonalizedMessage savedCoach = coachMsgRepo.save(coachMsg);
        if (savedCoach == null) {
            System.err.println("[AdminMessageService] ⚠️ Saved to admin_messages but failed to save to personalized_messages");
        } else {
            System.out.println("[AdminMessageService] ✅ Message visible in front-office (personalized_messages id=" + savedCoach.getId() + ")");
        }

        // ── 3. Send SMS via Twilio if requested ────────────────────────────────
        if (sendSms) {
            User user = userRepo.findById(userId);
            String phone = (user != null) ? user.getPhone() : null;

            if (phone != null && !phone.isBlank()) {
                // Ensure E.164 format
                if (!phone.startsWith("+")) phone = "+" + phone;

                String smsBody = "📩 Message from your NutriLife Coach:\n\n" + messageText;
                boolean sent = twilio.sendSms(phone, smsBody);

                String smsStatus = sent ? "sent" : "failed";
                adminMsgRepo.updateSmsStatus(savedAdmin.getId(), smsStatus);
                if (savedCoach != null) coachMsgRepo.updateSmsStatus(savedCoach.getId(), smsStatus);

                savedAdmin.setSmsStatus(smsStatus);
                System.out.println("[AdminMessageService] SMS " + (sent ? "✅ sent" : "❌ failed") + " to " + phone);
            } else {
                adminMsgRepo.updateSmsStatus(savedAdmin.getId(), "no_phone");
                if (savedCoach != null) coachMsgRepo.updateSmsStatus(savedCoach.getId(), "no_phone");
                savedAdmin.setSmsStatus("no_phone");
                System.err.println("[AdminMessageService] ⚠️ SMS requested but user has no phone number");
            }
        }

        System.out.println("[AdminMessageService] ✅ Message sent to user " + userId +
                (sendSms ? " (in-app + SMS)" : " (in-app only)"));
        return savedAdmin;
    }

    /** Unread count for a user (admin_messages table) */
    public int getUnreadCount(int userId) {
        return adminMsgRepo.getUnreadCount(userId);
    }

    /** Mark a single admin_message as read */
    public void markAsRead(int messageId) {
        adminMsgRepo.markAsRead(messageId);
    }

    /** Mark all admin_messages as read for a user */
    public void markAllAsRead(int userId) {
        adminMsgRepo.markAllAsRead(userId);
    }
}
