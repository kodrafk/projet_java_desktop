package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.ChatMessageRepository;
import tn.esprit.projet.utils.CoachNotification;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.ProfessionalWelcomeNotification;

public class HomeController {

    @FXML private StackPane  contentArea;
    @FXML private Label      lblWelcome;
    @FXML private MenuButton menuAccount;
    @FXML private Button     btnMessages;
    @FXML private Label      lblUnreadBadge;

    private User currentUser;
    private final ChatMessageRepository chatRepo = new ChatMessageRepository();

    @FXML
    public void initialize() {
        if (!Session.isLoggedIn()) {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
            return;
        }

        currentUser = Session.getCurrentUser();

        if (lblWelcome != null && currentUser != null) {
            lblWelcome.setText("Welcome back, " + currentUser.getFirstName() + "! 👋");
        }

        Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();

            // 1. Update badge immediately
            refreshUnreadBadge();

            // 2. Welcome notification
            ProfessionalWelcomeNotification.show(stage, currentUser);

            // 3. Coach notification after 1.8s — with click-to-open callback
            new Thread(() -> {
                try { Thread.sleep(1800); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    int unread = countUnreadFromCoach(currentUser.getId());
                    if (unread > 0) {
                        CoachNotification.show(stage, unread, () -> openChatModal());
                    }
                });
            }).start();
        });
    }

    // ── Badge ─────────────────────────────────────────────────────────────────

    private void refreshUnreadBadge() {
        if (currentUser == null || lblUnreadBadge == null) return;
        int unread = countUnreadFromCoach(currentUser.getId());
        if (unread > 0) {
            lblUnreadBadge.setText(String.valueOf(unread));
            lblUnreadBadge.setVisible(true);
            lblUnreadBadge.setManaged(true);
            // Pulse the Messages button
            if (btnMessages != null) {
                btnMessages.setStyle(
                    "-fx-background-color:#DBEAFE;-fx-text-fill:#1D4ED8;-fx-font-size:13px;" +
                    "-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:8 16;" +
                    "-fx-cursor:hand;-fx-border-color:#3B82F6;-fx-border-radius:20;-fx-border-width:2;");
            }
        } else {
            lblUnreadBadge.setVisible(false);
            lblUnreadBadge.setManaged(false);
        }
    }

    // ── Open chat ─────────────────────────────────────────────────────────────

    @FXML
    private void handleOpenChat() {
        openChatModal();
    }

    private void openChatModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/weight_objective.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setTitle("My Goal & Progress");
            stage.setScene(new Scene(root, 700, 750));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh badge after closing (user may have read messages)
            refreshUnreadBadge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Unread count ──────────────────────────────────────────────────────────

    private int countUnreadFromCoach(int userId) {
        try {
            java.sql.Connection c = tn.esprit.projet.utils.DatabaseConnection.getInstance().getConnection();
            java.sql.PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM chat_messages " +
                "WHERE receiver_id=? AND sender_type='ADMIN' AND is_read=0 AND deleted=0");
            ps.setInt(1, userId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.err.println("[HomeController] countUnread: " + e.getMessage());
        }
        return 0;
    }

    // ── Navbar handlers ───────────────────────────────────────────────────────

    @FXML private void handleMyProfile()       { openModal("profile.fxml",           "My Profile",       640, 720); }
    @FXML private void handleChangePassword()  { openModal("change_password.fxml",   "Change Password",  460, 420); }
    @FXML private void handleMyBadges()        { openModal("badges.fxml",            "My Badges",        860, 820); }
    @FXML private void handleWeeklyChallenges(){ openModal("weekly_challenges.fxml", "Weekly Challenges",560, 680); }
    @FXML private void handleAssistant()       { }
    @FXML private void handleBookAppointment() { }

    @FXML
    private void handleLogout() {
        Session.logout();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login", 1100, 720, false);
    }

    private void openModal(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
