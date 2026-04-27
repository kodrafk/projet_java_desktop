package tn.esprit.projet.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserAlert;
import tn.esprit.projet.models.UserAlert.AlertType;
import tn.esprit.projet.services.UserAlertService;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Widget d'affichage des alertes utilisateur pour le front office
 * Peut être intégré dans n'importe quelle vue
 */
public class UserAlertsWidget extends VBox {

    private final UserAlertService alertService = new UserAlertService();
    private final VBox alertsContainer;
    private final Label lblNoAlerts;
    private final Label lblAlertCount;
    private User currentUser;
    private Stage parentStage;
    
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UserAlertsWidget() {
        this.setSpacing(12);
        this.setPadding(new Insets(16));
        this.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        
        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔔 Alerts");
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        lblAlertCount = new Label("0");
        lblAlertCount.setStyle("-fx-font-size:11px;-fx-text-fill:white;-fx-background-color:#DC2626;-fx-background-radius:10;-fx-padding:2 8;-fx-font-weight:bold;");
        lblAlertCount.setVisible(false);
        
        Button btnRefresh = new Button("🔄");
        btnRefresh.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:4 10;-fx-cursor:hand;");
        btnRefresh.setOnAction(e -> refresh());
        
        header.getChildren().addAll(title, spacer, lblAlertCount, btnRefresh);
        
        // Alerts container
        alertsContainer = new VBox(10);
        alertsContainer.setPadding(new Insets(8, 0, 0, 0));
        
        // Empty state
        lblNoAlerts = new Label("No active alerts");
        lblNoAlerts.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-padding:20;");
        lblNoAlerts.setAlignment(Pos.CENTER);
        lblNoAlerts.setMaxWidth(Double.MAX_VALUE);
        
        // ScrollPane for alerts
        ScrollPane scrollPane = new ScrollPane(alertsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        scrollPane.setMaxHeight(400);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        this.getChildren().addAll(header, scrollPane);
    }

    /**
     * Définir l'utilisateur courant et charger ses alertes
     */
    public void setUser(User user, Stage stage) {
        this.currentUser = user;
        this.parentStage = stage;
        refresh();
    }

    /**
     * Rafraîchir les alertes
     */
    public void refresh() {
        if (currentUser == null) return;
        
        alertsContainer.getChildren().clear();
        
        List<UserAlert> alerts = alertService.getActiveAlertsForUser(currentUser.getId());
        
        if (alerts.isEmpty()) {
            alertsContainer.getChildren().add(lblNoAlerts);
            lblAlertCount.setVisible(false);
            return;
        }
        
        // Update count badge
        int unreadCount = (int) alerts.stream().filter(a -> !a.isRead()).count();
        if (unreadCount > 0) {
            lblAlertCount.setText(String.valueOf(unreadCount));
            lblAlertCount.setVisible(true);
        } else {
            lblAlertCount.setVisible(false);
        }
        
        // Display alerts
        for (UserAlert alert : alerts) {
            alertsContainer.getChildren().add(createAlertCard(alert));
        }
    }

    /**
     * Créer une carte d'alerte
     */
    private VBox createAlertCard(UserAlert alert) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        
        // Style based on type
        String bgColor = switch (alert.getType()) {
            case INFO -> "#EFF6FF";
            case WARNING -> "#FEF3C7";
            case URGENT -> "#FEE2E2";
            case SUCCESS -> "#DCFCE7";
        };
        
        String borderColor = switch (alert.getType()) {
            case INFO -> "#3B82F6";
            case WARNING -> "#F59E0B";
            case URGENT -> "#DC2626";
            case SUCCESS -> "#10B981";
        };
        
        card.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:10;-fx-border-color:" + borderColor + ";-fx-border-width:2;-fx-border-radius:10;");
        
        // Header with icon and date
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label(alert.getTypeIcon() + " " + alert.getCategoryIcon());
        icon.setStyle("-fx-font-size:16px;");
        
        Label date = new Label(alert.getCreatedAt().format(DATE_FMT));
        date.setStyle("-fx-font-size:10px;-fx-text-fill:#64748B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Unread indicator
        if (!alert.isRead()) {
            Label unread = new Label("●");
            unread.setStyle("-fx-font-size:14px;-fx-text-fill:" + borderColor + ";");
            header.getChildren().addAll(icon, date, spacer, unread);
        } else {
            header.getChildren().addAll(icon, date, spacer);
        }
        
        // Title
        Label title = new Label(alert.getTitle());
        title.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        title.setWrapText(true);
        
        // Message
        Label message = new Label(alert.getMessage());
        message.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        message.setWrapText(true);
        
        card.getChildren().addAll(header, title, message);
        
        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        // Action button if available
        if (alert.getActionUrl() != null && !alert.getActionUrl().isEmpty()) {
            Button btnAction = new Button(alert.getActionLabel() != null ? alert.getActionLabel() : "View");
            btnAction.setStyle("-fx-background-color:" + borderColor + ";-fx-text-fill:white;-fx-font-size:11px;-fx-background-radius:6;-fx-padding:6 12;-fx-cursor:hand;");
            btnAction.setOnAction(e -> handleAction(alert));
            actions.getChildren().add(btnAction);
        }
        
        // Mark as read button
        if (!alert.isRead()) {
            Button btnRead = new Button("Mark as read");
            btnRead.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;-fx-font-size:11px;-fx-background-radius:6;-fx-padding:6 12;-fx-cursor:hand;");
            btnRead.setOnAction(e -> {
                alertService.markAsRead(alert.getId());
                refresh();
                if (parentStage != null) {
                    Toasts.show(parentStage, "Alert marked as read", Toasts.Type.SUCCESS);
                }
            });
            actions.getChildren().add(btnRead);
        }
        
        // Dismiss button
        Button btnDismiss = new Button("✕");
        btnDismiss.setStyle("-fx-background-color:transparent;-fx-text-fill:#94A3B8;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:4 8;");
        btnDismiss.setOnAction(e -> {
            alertService.dismissAlert(alert.getId());
            refresh();
            if (parentStage != null) {
                Toasts.show(parentStage, "Alert dismissed", Toasts.Type.INFO);
            }
        });
        actions.getChildren().add(btnDismiss);
        
        card.getChildren().add(actions);
        
        return card;
    }

    /**
     * Gérer l'action d'une alerte
     */
    private void handleAction(UserAlert alert) {
        // Mark as read when action is clicked
        if (!alert.isRead()) {
            alertService.markAsRead(alert.getId());
        }
        
        // TODO: Navigate to the action URL
        // This would require integration with your navigation system
        System.out.println("Action clicked: " + alert.getActionUrl());
        
        if (parentStage != null) {
            Toasts.show(parentStage, "Action: " + alert.getActionUrl(), Toasts.Type.INFO);
        }
        
        refresh();
    }

    /**
     * Obtenir le nombre d'alertes non lues
     */
    public int getUnreadCount() {
        if (currentUser == null) return 0;
        return alertService.countUnreadAlerts(currentUser.getId());
    }
}
