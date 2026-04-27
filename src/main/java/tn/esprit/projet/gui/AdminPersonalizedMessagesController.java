package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.PersonalizedMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.PersonalizedMessageService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour l'interface admin de messages personnalisés
 */
public class AdminPersonalizedMessagesController {

    @FXML private Label lblUserInfo;
    @FXML private Label lblUserCount;
    @FXML private TextField txtSearchUser;
    @FXML private VBox userListContainer;
    @FXML private TextArea txtMessageContent;
    @FXML private Label lblCharCount;
    @FXML private CheckBox chkSendSms;
    @FXML private VBox historyContainer;
    @FXML private Button btnRefreshHistory;

    private final UserRepository userRepo = new UserRepository();
    private final PersonalizedMessageService messageService = new PersonalizedMessageService();
    private User selectedUser;
    private static final int MAX_CHARS = 500;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Character counter
        txtMessageContent.textProperty().addListener((obs, old, val) -> {
            int len = val != null ? val.length() : 0;
            lblCharCount.setText(len + " / " + MAX_CHARS);
            if (len > MAX_CHARS) {
                lblCharCount.setStyle("-fx-font-size:11px;-fx-text-fill:#DC2626;-fx-font-weight:bold;");
            } else {
                lblCharCount.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;");
            }
        });

        // Search filter
        txtSearchUser.textProperty().addListener((obs, old, val) -> loadUsers());

        // Load users
        loadUsers();
        
        // Show empty history
        showEmptyHistory();
    }

    /**
     * Charger la liste des utilisateurs
     */
    private void loadUsers() {
        userListContainer.getChildren().clear();
        
        List<User> users = userRepo.findAll();
        System.out.println("[DEBUG] Total users from DB: " + users.size());
        
        // Debug: afficher tous les utilisateurs et leurs rôles
        for (User u : users) {
            System.out.println("[DEBUG] User: " + u.getEmail() + " | Role: " + u.getRole() + " | FullName: " + u.getFullName());
        }
        
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
        
        System.out.println("[DEBUG] Filtered users (excluding current admin): " + filteredUsers.size());
        
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
        
        // Email
        Label email = new Label(user.getEmail());
        email.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
        
        // Phone
        card.getChildren().addAll(header, email);
        
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            Label phone = new Label("📱 " + user.getPhone());
            phone.setStyle("-fx-font-size:11px;-fx-text-fill:#2E7D32;");
            card.getChildren().add(phone);
        } else {
            Label noPhone = new Label("📱 No phone");
            noPhone.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
            card.getChildren().add(noPhone);
        }
        
        // Click handler
        card.setOnMouseClicked(e -> selectUser(user, card));
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            if (selectedUser == null || selectedUser.getId() != user.getId()) {
                card.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:10;-fx-border-color:#2E7D32;-fx-border-radius:10;-fx-cursor:hand;");
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
        
        // Update UI
        lblUserInfo.setText("Sending to: " + user.getFullName() + " (" + user.getEmail() + ")");
        
        // Highlight selected card
        userListContainer.getChildren().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;-fx-cursor:hand;");
            }
        });
        card.setStyle("-fx-background-color:#E8F5E9;-fx-background-radius:10;-fx-border-color:#2E7D32;-fx-border-width:2;-fx-border-radius:10;-fx-cursor:hand;");
        
        // Load message history for this user
        loadHistoryForUser(user.getId());
    }

    /**
     * Envoyer un message
     */
    @FXML
    private void handleSendMessage() {
        if (selectedUser == null) {
            showToast("Please select a user first", Toasts.Type.WARNING);
            return;
        }
        
        String content = txtMessageContent.getText();
        if (content == null || content.trim().isEmpty()) {
            showToast("Please write a message", Toasts.Type.WARNING);
            return;
        }
        
        if (content.length() > MAX_CHARS) {
            showToast("Message exceeds " + MAX_CHARS + " characters", Toasts.Type.WARNING);
            return;
        }
        
        User admin = Session.getCurrentUser();
        if (admin == null) {
            showToast("No admin session found", Toasts.Type.ERROR);
            return;
        }
        
        boolean sendSms = chkSendSms.isSelected();
        
        // Send message
        PersonalizedMessage sent = messageService.sendMessage(
            admin.getId(), 
            selectedUser.getId(), 
            content.trim(), 
            sendSms
        );
        
        if (sent != null) {
            String successMsg = "Message sent to " + selectedUser.getFullName();
            
            if (sendSms) {
                if ("no_phone".equals(sent.getSmsStatus())) {
                    showToast(successMsg + " (in-app only - no phone)", Toasts.Type.WARNING);
                } else if ("failed".equals(sent.getSmsStatus())) {
                    showToast(successMsg + " (in-app only - SMS failed)", Toasts.Type.WARNING);
                } else {
                    showToast(successMsg + " (in-app + SMS)", Toasts.Type.SUCCESS);
                }
            } else {
                showToast(successMsg, Toasts.Type.SUCCESS);
            }
            
            // Clear form
            txtMessageContent.clear();
            chkSendSms.setSelected(false);
            
            // Refresh history
            loadHistoryForUser(selectedUser.getId());
        } else {
            showToast("Failed to send message", Toasts.Type.ERROR);
        }
    }

    /**
     * Effacer le message
     */
    @FXML
    private void handleClearMessage() {
        txtMessageContent.clear();
        chkSendSms.setSelected(false);
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
     * Charger l'historique des messages pour un utilisateur
     */
    private void loadHistoryForUser(int userId) {
        historyContainer.getChildren().clear();
        
        User admin = Session.getCurrentUser();
        if (admin == null) return;
        
        List<PersonalizedMessage> messages = messageService.getMessagesSentByAdmin(admin.getId())
                .stream()
                .filter(m -> m.getUserId() == userId)
                .toList();
        
        if (messages.isEmpty()) {
            showEmptyHistory();
            return;
        }
        
        for (PersonalizedMessage msg : messages) {
            historyContainer.getChildren().add(createHistoryCard(msg));
        }
    }

    /**
     * Créer une carte d'historique
     */
    private VBox createHistoryCard(PersonalizedMessage msg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#E5E7EB;-fx-border-radius:10;");
        
        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label date = new Label(msg.getSentAt().format(DATE_FMT));
        date.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#64748B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Status badges
        HBox badges = new HBox(6);
        
        if (msg.isRead()) {
            Label readBadge = new Label("✓ Read");
            readBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#2E7D32;-fx-background-color:#E8F5E9;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(readBadge);
        } else {
            Label unreadBadge = new Label("● Unread");
            unreadBadge.setStyle("-fx-font-size:10px;-fx-text-fill:#D97706;-fx-background-color:#FEF3C7;-fx-background-radius:4;-fx-padding:3 8;");
            badges.getChildren().add(unreadBadge);
        }
        
        if (msg.isSendViaSms()) {
            String smsText = "📱 ";
            String smsStyle;
            if ("sent".equals(msg.getSmsStatus())) {
                smsText += "SMS sent";
                smsStyle = "-fx-font-size:10px;-fx-text-fill:#2E7D32;-fx-background-color:#E8F5E9;-fx-background-radius:4;-fx-padding:3 8;";
            } else if ("no_phone".equals(msg.getSmsStatus())) {
                smsText += "No phone";
                smsStyle = "-fx-font-size:10px;-fx-text-fill:#94A3B8;-fx-background-color:#F1F5F9;-fx-background-radius:4;-fx-padding:3 8;";
            } else {
                smsText += "SMS failed";
                smsStyle = "-fx-font-size:10px;-fx-text-fill:#DC2626;-fx-background-color:#FEE2E2;-fx-background-radius:4;-fx-padding:3 8;";
            }
            Label smsBadge = new Label(smsText);
            smsBadge.setStyle(smsStyle);
            badges.getChildren().add(smsBadge);
        }
        
        header.getChildren().addAll(date, spacer, badges);
        
        // Content
        Label content = new Label(msg.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        
        card.getChildren().addAll(header, content);
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
        
        Label icon = new Label("📭");
        icon.setStyle("-fx-font-size:36px;");
        
        Label text = new Label("No messages sent yet");
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

    @FXML
    private void handleCloseHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:18px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 12;-fx-background-radius:6;");
        }
    }

    @FXML
    private void handleCloseUnhover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#94A3B8;-fx-font-size:18px;-fx-font-weight:bold;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:0 0 0 16;");
        }
    }
}
