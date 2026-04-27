package tn.esprit.projet.services;

import tn.esprit.projet.models.PersonalizedMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.PersonalizedMessageRepository;
import tn.esprit.projet.repository.UserRepository;

import java.util.List;

/**
 * Service pour gérer les messages personnalisés admin → user
 * Avec support SMS via Twilio
 */
public class PersonalizedMessageService {

    private final PersonalizedMessageRepository messageRepo = new PersonalizedMessageRepository();
    private final UserRepository userRepo = new UserRepository();
    private final TwilioService twilioService = TwilioService.getInstance();

    public PersonalizedMessageService() {
        messageRepo.ensureTableExists();
    }

    /**
     * Envoyer un message personnalisé à un utilisateur
     * @param adminId ID de l'admin expéditeur
     * @param userId ID de l'utilisateur destinataire
     * @param content Contenu du message
     * @param sendViaSms Envoyer aussi par SMS ?
     * @return Le message sauvegardé
     */
    public PersonalizedMessage sendMessage(int adminId, int userId, String content, boolean sendViaSms) {
        // Créer le message
        PersonalizedMessage msg = new PersonalizedMessage(userId, adminId, content, sendViaSms);
        
        // Sauvegarder en base
        PersonalizedMessage savedMsg = messageRepo.save(msg);
        
        if (savedMsg == null) {
            System.err.println("[PersonalizedMessageService] ❌ Failed to save message");
            return null;
        }

        // Envoyer par SMS si demandé
        if (sendViaSms) {
            User user = userRepo.findById(userId);
            if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                boolean smsSent = sendSmsToUser(user.getPhone(), content);
                if (smsSent) {
                    savedMsg.setSmsStatus("sent");
                    messageRepo.updateSmsStatus(savedMsg.getId(), "sent");
                } else {
                    savedMsg.setSmsStatus("failed");
                    messageRepo.updateSmsStatus(savedMsg.getId(), "failed");
                }
            } else {
                savedMsg.setSmsStatus("no_phone");
                messageRepo.updateSmsStatus(savedMsg.getId(), "no_phone");
                System.err.println("[PersonalizedMessageService] ⚠️ User has no phone number");
            }
        }

        System.out.println("[PersonalizedMessageService] ✅ Message sent to user " + userId + 
                          (sendViaSms ? " (with SMS)" : " (in-app only)"));
        
        return savedMsg;
    }

    /**
     * Envoyer un SMS via Twilio
     */
    private boolean sendSmsToUser(String phoneNumber, String content) {
        if (!twilioService.isConfigured()) {
            System.err.println("[PersonalizedMessageService] ⚠️ Twilio not configured");
            return false;
        }

        // Formater le message
        String smsBody = "💬 Message from your coach:\n\n" + content;
        
        // Envoyer via Twilio
        return twilioService.sendSms(phoneNumber, smsBody);
    }

    /**
     * Récupérer tous les messages d'un utilisateur
     */
    public List<PersonalizedMessage> getMessagesForUser(int userId) {
        return messageRepo.findByUserId(userId);
    }

    /**
     * Récupérer les messages non lus d'un utilisateur
     */
    public List<PersonalizedMessage> getUnreadMessagesForUser(int userId) {
        return messageRepo.findUnreadByUserId(userId);
    }

    /**
     * Compter les messages non lus
     */
    public int countUnreadMessages(int userId) {
        return messageRepo.countUnreadByUserId(userId);
    }

    /**
     * Marquer un message comme lu
     */
    public void markAsRead(int messageId) {
        messageRepo.markAsRead(messageId);
    }

    /**
     * Marquer tous les messages comme lus
     */
    public void markAllAsRead(int userId) {
        messageRepo.markAllAsReadByUserId(userId);
    }

    /**
     * Récupérer l'historique des messages envoyés par un admin
     */
    public List<PersonalizedMessage> getMessagesSentByAdmin(int adminId) {
        return messageRepo.findByAdminId(adminId);
    }

    /**
     * Supprimer un message
     */
    public boolean deleteMessage(int messageId) {
        return messageRepo.delete(messageId);
    }
}
