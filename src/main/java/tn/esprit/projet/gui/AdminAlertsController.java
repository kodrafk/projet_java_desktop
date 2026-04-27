package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserAlert;
import tn.esprit.projet.models.UserAlert.AlertType;
import tn.esprit.projet.models.UserAlert.AlertCategory;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.UserAlertService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour la gestion des alertes utilisateur (backoffice admin)
 */
public class AdminAlertsController {

    @FXML private Label lblUserInfo;
    @FXML private Label lblUserCount;
    @FXML private TextField txtSearchUser;
    @FXML private VBox userListContainer;
    
    @FXML private TextField txtAlertTitle;
    @FXML private TextArea txtAlertMessage;
    @FXML private ComboBox<AlertType> cmbAlertType;
    @FXML private ComboBox<AlertCategory> cmbAlertCategory;
    @FXML private CheckBox chkHasExpiry;
    @FXML private DatePicker dpExpiryDate;
    @FXML private TextField txtExpiryTime;
    @FXML private CheckBox chkHasAction;
    @FXML private TextField txtActionUrl;
    @FXML private TextField txtActionLabel;
    @FXML private Label lblCharCount;
    
    @FXML private VBox historyContainer;
    @FXML private Button btnRefreshHistory;

    private final UserRepository userRepo = new UserRepository();
    private final UserAlertService alertService = new UserAlertService();
    private User selectedUser;
    private static final int MAX_CHARS = 500;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Setup ComboBoxes
        cmbAlertType.getItems().addAll(AlertType.values());
        cmbAlertType.setValue(AlertType.INFO);
        
        cmbAlertCategory.getItems().addAll(AlertCategory.values());
        cmbAlertCategory.setValue(AlertCategory.SYSTEM);
        
        // Character counter
        txtAlertMessage.textProperty().addListener((obs, old, val) -> {
            int len = val != null ? val.length() : 0;
            lblCharCount.setText(len + " / " + MAX_CHARS);
            if (len > MAX_CHARS) {
                lblCharCount.setStyle("-fx-font-size:11px;-fx-text-fill:#DC2626;-fx-font-weight:bold;");
            } else {
                lblCharCount.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;");
            }
        });
        
        // Expiry controls visibility
        dpExpiryDate.setVisible(false);
        dpExpiryDate.setManaged(false);
        txtExpiryTime.setVisible(false);
        txtExpiryTime.setManaged(false);
        
        chkHasExpiry.selectedProperty().addListener((obs, old, val) -> {
            dpExpiryDate.setVisible(val);
            dpExpiryDate.setManaged(val);
            txtExpiryTime.setVisible(val);
            txtExpiryTime.setManaged(val);
        });
        
        // Action controls visibility
        txtActionUrl.setVisible(false);
        txtActionUrl.setManaged(false);
        txtActionLabel.setVisible(false);
        txtActionLabel.setManaged(false);
        
        chkHasAction.selectedProperty().addListener((obs, old, val) -> {
            txtActionUrl.setVisible(val);
            txtActionUrl.setManaged(val);
            txtActionLabel.setVisible(val);
            txtActionLabel.setManaged(val);
        });
        
        // Search filter
        txtSearchUser.textProperty().addListener((obs, old, val) -> loadUsers());
        
        // Load users
        loadUsers();
        showEmptyHistory();
    }

    /**
     * Charger la liste des utilisateurs
     */
    private void loadUsers() {
        userListContainer.getChildren().clear();
        
        List<User> users = userRepo.findAll();
        System.out.println("[DEBUG AdminAlerts] Total users from DB: " + users.size());
        
        String searchTerm = txtSearchUser.getText();
        if (searchTerm == null) searchTerm = "";
        searchTerm = searchTerm.toLowerCase().trim();
        
        // Filtrer les utilisateurs selon le terme de recherche (TOUS les utilisateurs, y compris admins)
        List<User> filteredUsers = new ArrayList<>();
        User currentAdmin = Session.getCurrentUser();
        
        for (User u : users) {
            // Exclure l'admin connecté lui-même
            if (currentAdmin != null && u.getId() == currentAdmin.getId()) {
                continue;
            }
            
            // Vérifier le terme de recherche
            boolean matchesSearch = searchTerm.isEmpty();
            if (!matchesSearch) {
                String fullName = u.getFullName();
                String email = u.getEmail();
                if (fullName != null && fullName.toLowerCase().contains(searchTerm)) matchesSearch = true;
                if (email != null && email.toLowerCase().contains(searchTerm)) matchesSearch = true;
            }
            
            if (matchesSearch) {
                filteredUsers.add(u);
            }
        }
        
        System.out.println("[DEBUG AdminAlerts] Filtered users (excluding current admin): " + filteredUsers.size());
        
        lblUserCount.setText(filteredUsers.size() + " user" + (filteredUsers.size() > 1 ? "s" : ""));
        
        if (filteredUsers.isEmpty()) {
            Label empty = new Label("No users found");
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-padding:20;");
            userListContainer.getChildren().add(empty);
            return;
        }
        
        for (User user : filteredUsers) {
            userListContainer.getChildren().add(createUserCard(user));
        }
    }

    /**
     * Créer une carte utilisateur
     */
    private VBox createUserCard(User user) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;-fx-cursor:hand;");
        
        // Header avec nom et badge admin
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        header.getChildren().add(name);
        
        // Badge Admin si l'utilisateur est admin
        if (user.isAdmin()) {
            Label adminBadge = new Label("👑 Admin");
            adminBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#DC2626;-fx-background-color:#FEE2E2;-fx-background-radius:4;-fx-padding:2 6;-fx-font-weight:bold;");
            header.getChildren().add(adminBadge);
        }
        
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
        
        card.getChildren().addAll(header, email);
        
        // Unread alerts count
        int unreadCount = alertService.countUnreadAlerts(user.getId());
        if (unreadCount > 0) {
            Label unread = new Label("🔔 " + unreadCount + " unread alert" + (unreadCount > 1 ? "s" : ""));
            unread.setStyle("-fx-font-size:11px;-fx-text-fill:#DC2626;-fx-font-weight:bold;");
            card.getChildren().add(unread);
        }
        
        card.setOnMouseClicked(e -> selectUser(user, card));
        
        card.setOnMouseEntered(e -> {
            if (selectedUser == null || selectedUser.getId() != user.getId()) {
                card.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:10;-fx-border-color:#7C3AED;-fx-border-radius:10;-fx-cursor:hand;");
            }
        });
        card.setOnMouseExited(e -> {
            if (selectedUser == null || selectedUser.getId() != user.getId()) {
                card.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;-fx-cursor:hand;");
            }
        });
        
        return card;
    }

    /**
     * Sélectionner un utilisateur
     */
    private void selectUser(User user, VBox card) {
        this.selectedUser = user;
        lblUserInfo.setText("Sending alert to: " + user.getFullName() + " (" + user.getEmail() + ")");
        
        userListContainer.getChildren().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;-fx-cursor:hand;");
            }
        });
        card.setStyle("-fx-background-color:#F3E8FF;-fx-background-radius:10;-fx-border-color:#7C3AED;-fx-border-width:2;-fx-border-radius:10;-fx-cursor:hand;");
        
        loadHistoryForUser(user.getId());
    }

    /**
     * Envoyer une alerte
     */
    @FXML
    private void handleSendAlert() {
        if (selectedUser == null) {
            showToast("Please select a user first", Toasts.Type.WARNING);
            return;
        }
        
        String title = txtAlertTitle.getText();
        if (title == null || title.trim().isEmpty()) {
            showToast("Please enter an alert title", Toasts.Type.WARNING);
            return;
        }
        
        String message = txtAlertMessage.getText();
        if (message == null || message.trim().isEmpty()) {
            showToast("Please enter an alert message", Toasts.Type.WARNING);
            return;
        }
        
        if (message.length() > MAX_CHARS) {
            showToast("Message exceeds " + MAX_CHARS + " characters", Toasts.Type.WARNING);
            return;
        }
        
        User admin = Session.getCurrentUser();
        if (admin == null) {
            showToast("No admin session found", Toasts.Type.ERROR);
            return;
        }
        
        AlertType type = cmbAlertType.getValue();
        AlertCategory category = cmbAlertCategory.getValue();
        
        UserAlert alert;
        
        // Check if has expiry
        if (chkHasExpiry.isSelected() && dpExpiryDate.getValue() != null) {
            String timeStr = txtExpiryTime.getText();
            if (timeStr == null || timeStr.trim().isEmpty()) {
                timeStr = "23:59";
            }
            
            try {
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                
                LocalDateTime expiresAt = dpExpiryDate.getValue().atTime(hour, minute);
                
                if (chkHasAction.isSelected()) {
                    String actionUrl = txtActionUrl.getText();
                    String actionLabel = txtActionLabel.getText();
                    if (actionUrl != null && !actionUrl.trim().isEmpty()) {
                        alert = alertService.createAlertWithAction(admin.getId(), selectedUser.getId(), 
                            title.trim(), message.trim(), type, category, actionUrl.trim(), 
                            actionLabel != null ? actionLabel.trim() : "View");
                        alert.setExpiresAt(expiresAt);
                        alertService.createAlertWithExpiry(admin.getId(), selectedUser.getId(), 
                            title.trim(), message.trim(), type, category, expiresAt);
                    } else {
                        alert = alertService.createAlertWithExpiry(admin.getId(), selectedUser.getId(), 
                            title.trim(), message.trim(), type, category, expiresAt);
                    }
                } else {
                    alert = alertService.createAlertWithExpiry(admin.getId(), selectedUser.getId(), 
                        title.trim(), message.trim(), type, category, expiresAt);
                }
            } catch (Exception e) {
                showToast("Invalid time format. Use HH:MM", Toasts.Type.ERROR);
                return;
            }
        } else if (chkHasAction.isSelected()) {
            String actionUrl = txtActionUrl.getText();
            String actionLabel = txtActionLabel.getText();
            if (actionUrl == null || actionUrl.trim().isEmpty()) {
                showToast("Please enter an action URL", Toasts.Type.WARNING);
                return;
            }
            alert = alertService.createAlertWithAction(admin.getId(), selectedUser.getId(), 
                title.trim(), message.trim(), type, category, actionUrl.trim(), 
                actionLabel != null ? actionLabel.trim() : "View");
        } else {
            alert = alertService.createAlert(admin.getId(), selectedUser.getId(), 
                title.trim(), message.trim(), type, category);
        }
        
        if (alert != null) {
            showToast("Alert sent to " + selectedUser.getFullName(), Toasts.Type.SUCCESS);
            clearForm();
            loadHistoryForUser(selectedUser.getId());
        } else {
            showToast("Failed to send alert", Toasts.Type.ERROR);
        }
    }

    /**
     * Effacer le formulaire
     */
    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void clearForm() {
        txtAlertTitle.clear();
        txtAlertMessage.clear();
        cmbAlertType.setValue(AlertType.INFO);
        cmbAlertCategory.setValue(AlertCategory.SYSTEM);
        chkHasExpiry.setSelected(false);
        dpExpiryDate.setValue(null);
        txtExpiryTime.clear();
        chkHasAction.setSelected(false);
        txtActionUrl.clear();
        txtActionLabel.clear();
    }

    /**
     * Rafraîchir l'historique
     */
    @FXML
    private void handleRefreshHistory() {
        if (selectedUser != null) {
            loadHistoryForUser(selectedUser.getId());
            showToast("History refreshed", Toasts.Type.SUCCESS);
        }
    }

    /**
     * Charger l'historique des alertes pour un utilisateur
     */
    private void loadHistoryForUser(int userId) {
        historyContainer.getChildren().clear();
        
        User admin = Session.getCurrentUser();
        if (admin == null) return;
        
        List<UserAlert> alerts = alertService.getAlertsByAdmin(admin.getId())
                .stream()
                .filter(a -> a.getUserId() == userId)
                .toList();
        
        if (alerts.isEmpty()) {
            showEmptyHistory();
            return;
        }
        
        for (UserAlert alert : alerts) {
            historyContainer.getChildren().add(createHistoryCard(alert));
        }
    }

    /**
     * Créer une carte d'historique
     */
    private VBox createHistoryCard(UserAlert alert) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;");
        
        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label(alert.getTypeIcon() + " " + alert.getCategoryIcon());
        icon.setStyle("-fx-font-size:14px;");
        
        Label date = new Label(alert.getCreatedAt().format(DATE_FMT));
        date.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#64748B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Status badges
        HBox badges = new HBox(6);
        
        if (alert.isRead()) {
            Label readBadge = new Label("✓ Read");
            readBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#2E7D32;-fx-background-color:#E8F5E9;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(readBadge);
        } else {
            Label unreadBadge = new Label("● Unread");
            unreadBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#D97706;-fx-background-color:#FEF3C7;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(unreadBadge);
        }
        
        if (alert.isDismissed()) {
            Label dismissedBadge = new Label("✕ Dismissed");
            dismissedBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;-fx-background-color:#F1F5F9;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(dismissedBadge);
        }
        
        if (alert.isExpired()) {
            Label expiredBadge = new Label("⏱ Expired");
            expiredBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#DC2626;-fx-background-color:#FEE2E2;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(expiredBadge);
        }
        
        header.getChildren().addAll(icon, date, spacer, badges);
        
        // Title
        Label title = new Label(alert.getTitle());
        title.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        
        // Message
        Label message = new Label(alert.getMessage());
        message.setWrapText(true);
        message.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        
        card.getChildren().addAll(header, title, message);
        return card;
    }

    /**
     * Afficher l'état vide de l'historique
     */
    private void showEmptyHistory() {
        historyContainer.getChildren().clear();
        
        VBox empty = new VBox(10);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(40));
        
        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size:36px;");
        
        Label text = new Label("No alerts sent yet");
        text.setStyle("-fx-font-size:13px;-fx-text-fill:#94A3B8;");
        
        empty.getChildren().addAll(icon, text);
        historyContainer.getChildren().add(empty);
    }

    /**
     * Afficher un toast
     */
    private void showToast(String message, Toasts.Type type) {
        Stage stage = (Stage) lblUserInfo.getScene().getWindow();
        Toasts.show(stage, message, type);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblUserInfo.getScene().getWindow();
        stage.close();
    }
}
