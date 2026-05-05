package tn.esprit.projet.services;

import tn.esprit.projet.models.UserAlert;
import tn.esprit.projet.models.UserAlert.AlertType;
import tn.esprit.projet.models.UserAlert.AlertCategory;
import tn.esprit.projet.repository.UserAlertRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour gérer les alertes utilisateur
 */
public class UserAlertService {

    private final UserAlertRepository alertRepo = new UserAlertRepository();

    public UserAlertService() {
        alertRepo.ensureTableExists();
    }

    /**
     * Créer une nouvelle alerte
     */
    public UserAlert createAlert(int adminId, int userId, String title, String message, 
                                 AlertType type, AlertCategory category) {
        UserAlert alert = new UserAlert(userId, adminId, title, message, type, category);
        return alertRepo.save(alert);
    }

    /**
     * Créer une alerte avec expiration
     */
    public UserAlert createAlertWithExpiry(int adminId, int userId, String title, String message, 
                                          AlertType type, AlertCategory category, LocalDateTime expiresAt) {
        UserAlert alert = new UserAlert(userId, adminId, title, message, type, category);
        alert.setExpiresAt(expiresAt);
        return alertRepo.save(alert);
    }

    /**
     * Créer une alerte avec action
     */
    public UserAlert createAlertWithAction(int adminId, int userId, String title, String message, 
                                          AlertType type, AlertCategory category, 
                                          String actionUrl, String actionLabel) {
        UserAlert alert = new UserAlert(userId, adminId, title, message, type, category);
        alert.setActionUrl(actionUrl);
        alert.setActionLabel(actionLabel);
        return alertRepo.save(alert);
    }

    /**
     * Récupérer toutes les alertes actives pour un utilisateur
     */
    public List<UserAlert> getActiveAlertsForUser(int userId) {
        return alertRepo.findActiveByUserId(userId);
    }

    /**
     * Récupérer les alertes non lues pour un utilisateur
     */
    public List<UserAlert> getUnreadAlertsForUser(int userId) {
        return alertRepo.findUnreadByUserId(userId);
    }

    /**
     * Compter les alertes non lues
     */
    public int countUnreadAlerts(int userId) {
        return alertRepo.countUnreadByUserId(userId);
    }

    /**
     * Marquer une alerte comme lue
     */
    public boolean markAsRead(int alertId) {
        return alertRepo.markAsRead(alertId);
    }

    /**
     * Fermer une alerte
     */
    public boolean dismissAlert(int alertId) {
        return alertRepo.dismiss(alertId);
    }

    /**
     * Récupérer toutes les alertes créées par un admin
     */
    public List<UserAlert> getAlertsByAdmin(int adminId) {
        return alertRepo.findByAdminId(adminId);
    }

    /**
     * Nettoyer les alertes expirées
     */
    public int cleanupExpiredAlerts() {
        return alertRepo.deleteExpired();
    }

    // ── Méthodes de commodité pour créer des alertes courantes ──

    public UserAlert sendHealthAlert(int adminId, int userId, String title, String message) {
        return createAlert(adminId, userId, title, message, AlertType.WARNING, AlertCategory.HEALTH);
    }

    public UserAlert sendGoalAlert(int adminId, int userId, String title, String message) {
        return createAlert(adminId, userId, title, message, AlertType.INFO, AlertCategory.GOAL);
    }

    public UserAlert sendUrgentAlert(int adminId, int userId, String title, String message) {
        return createAlert(adminId, userId, title, message, AlertType.URGENT, AlertCategory.SYSTEM);
    }

    public UserAlert sendSuccessAlert(int adminId, int userId, String title, String message) {
        return createAlert(adminId, userId, title, message, AlertType.SUCCESS, AlertCategory.GOAL);
    }

    public UserAlert sendReminder(int adminId, int userId, String title, String message, LocalDateTime expiresAt) {
        return createAlertWithExpiry(adminId, userId, title, message, AlertType.INFO, AlertCategory.REMINDER, expiresAt);
    }
}
