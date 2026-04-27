package tn.esprit.projet.utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.User;

/**
 * Premium welcome notification shown after login.
 */
public class WelcomeNotification {

    private static final String[] MOTIVATIONS = {
        "Every healthy choice is a step toward your best self.",
        "Your journey to wellness starts with today's commitment.",
        "Small steps every day lead to big transformations.",
        "You've got this! Consistency is the key to success.",
        "Fuel your body, nourish your soul. Let's crush today!",
        "A new day, a new opportunity to be your healthiest self.",
        "Progress, not perfection. Keep moving forward!",
        "Your health is your greatest wealth. Invest in it daily."
    };

    public static void show(Stage owner, User user) {
        if (owner == null || user == null) return;

        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        boolean isAdmin = user.isAdmin();
        
        // Get random motivation message
        String motivation = MOTIVATIONS[(int) (Math.random() * MOTIVATIONS.length)];

        // Colors
        String primaryColor = isAdmin ? "#7C3AED" : "#16A34A";
        String secondaryColor = isAdmin ? "#A78BFA" : "#4ADE80";

        // Avatar
        Circle avatarCircle = new Circle(28);
        avatarCircle.setFill(Color.web(primaryColor));
        
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setColor(Color.web(primaryColor, 0.4));
        avatarGlow.setRadius(16);
        avatarGlow.setSpread(0.3);
        
        Label avatarLbl = new Label(firstName.substring(0, 1).toUpperCase());
        avatarLbl.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;");
        
        StackPane avatar = new StackPane(avatarCircle, avatarLbl);
        avatar.setPrefSize(56, 56);
        avatar.setEffect(avatarGlow);

        // Status dot
        Circle statusDot = new Circle(5);
        statusDot.setFill(Color.web("#10B981"));
        statusDot.setStroke(Color.WHITE);
        statusDot.setStrokeWidth(2);
        
        StackPane statusIndicator = new StackPane(statusDot);
        statusIndicator.setTranslateX(18);
        statusIndicator.setTranslateY(18);
        
        StackPane avatarWithStatus = new StackPane(avatar, statusIndicator);
        StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);

        // Role badge
        String roleIcon = isAdmin ? "👑" : "✨";
        String roleText = isAdmin ? "Administrator" : "Member";
        
        Label roleIconLbl = new Label(roleIcon);
        roleIconLbl.setStyle("-fx-font-size:11px;");
        
        Label roleTextLbl = new Label(roleText);
        roleTextLbl.setStyle(
            "-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:0.5px;"
        );
        
        HBox roleBadge = new HBox(3, roleIconLbl, roleTextLbl);
        roleBadge.setAlignment(Pos.CENTER);
        roleBadge.setPadding(new Insets(3, 10, 3, 10));
        roleBadge.setStyle(
            "-fx-background-color:rgba(" + (isAdmin ? "124,58,237" : "22,163,74") + ",0.12);" +
            "-fx-background-radius:20;" +
            "-fx-border-color:" + primaryColor + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:20;"
        );

        // Greeting
        String welcomeText = "Welcome back, " + firstName + "! 👋";
        
        Label greeting = new Label(welcomeText);
        greeting.setStyle(
            "-fx-font-size:18px;-fx-font-weight:800;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:-0.3px;"
        );

        // Motivation
        Label motivationLbl = new Label(motivation);
        motivationLbl.setWrapText(true);
        motivationLbl.setMaxWidth(280);
        motivationLbl.setStyle(
            "-fx-font-size:12px;-fx-text-fill:#475569;-fx-line-spacing:3;" +
            "-fx-font-weight:500;"
        );

        // Time greeting
        int hour = java.time.LocalTime.now().getHour();
        String timeIcon = hour < 12 ? "☀️" : (hour < 18 ? "🌤️" : "🌙");
        String timeGreeting = hour < 12 ? "Good morning" : (hour < 18 ? "Good afternoon" : "Good evening");
        
        Label timeIconLbl = new Label(timeIcon);
        timeIconLbl.setStyle("-fx-font-size:12px;");
        
        Label timeText = new Label(timeGreeting + "! Have a productive session.");
        timeText.setStyle("-fx-font-size:10px;-fx-text-fill:#64748B;-fx-font-weight:500;");
        
        HBox footer = new HBox(5, timeIconLbl, timeText);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Close button
        Label closeBtn = new Label("✕");
        closeBtn.setStyle(
            "-fx-font-size:14px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
            "-fx-padding:3 6;-fx-background-radius:6;-fx-font-weight:bold;"
        );
        closeBtn.setOnMouseEntered(e -> 
            closeBtn.setStyle(
                "-fx-font-size:14px;-fx-text-fill:#EF4444;-fx-cursor:hand;" +
                "-fx-padding:3 6;-fx-background-color:#FEE2E2;-fx-background-radius:6;" +
                "-fx-font-weight:bold;"
            )
        );
        closeBtn.setOnMouseExited(e -> 
            closeBtn.setStyle(
                "-fx-font-size:14px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
                "-fx-padding:3 6;-fx-background-radius:6;-fx-font-weight:bold;"
            )
        );

        // Layout
        VBox textContent = new VBox(6, roleBadge, greeting, motivationLbl);
        textContent.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox topRow = new HBox(12, avatarWithStatus, textContent, closeBtn);
        topRow.setAlignment(Pos.TOP_LEFT);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle("-fx-background-color:linear-gradient(to right, transparent, #E2E8F0, transparent);");

        VBox mainContent = new VBox(12, topRow, divider, footer);
        mainContent.setPadding(new Insets(20, 24, 18, 24));
        mainContent.setPrefWidth(400);
        mainContent.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:16;" +
            "-fx-border-color:#E2E8F0;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:16;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),20,0,0,6);"
        );

        // Accent bar with gradient
        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setPrefWidth(400);
        accentBar.setStyle(
            "-fx-background-color:linear-gradient(to right," + primaryColor + "," + secondaryColor + ");" +
            "-fx-background-radius:16 16 0 0;"
        );

        VBox card = new VBox(0, accentBar, mainContent);

        // Popup
        Popup popup = new Popup();
        popup.getContent().add(card);
        popup.setAutoFix(true);
        popup.setAutoHide(false);

        // Wait for window to be ready
        PauseTransition wait = new PauseTransition(Duration.millis(500));
        wait.setOnFinished(ev -> {
            try {
                // Professional positioning - top-right with proper margins
                double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
                double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
                
                double x, y;
                
                // If window is maximized or fullscreen, use screen coordinates
                if (owner.isMaximized() || owner.getWidth() > screenWidth * 0.8) {
                    x = screenWidth - 450;  // 450px from right edge
                    y = 80;                 // 80px from top
                } else {
                    // Use window coordinates for windowed mode
                    x = owner.getX() + owner.getWidth() - 450;
                    y = owner.getY() + 80;
                }
                
                // Ensure popup stays on screen
                if (x < 20) x = 20;
                if (y < 20) y = 20;
                
                popup.show(owner, x, y);

                // Animations
                card.setOpacity(0);
                card.setTranslateY(-20);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), card);
                slideIn.setFromY(-20);
                slideIn.setToY(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition enter = new ParallelTransition(fadeIn, slideIn);

                // Duration: 10s for users, 6.5s for admins
                double holdDuration = isAdmin ? 6.5 : 10.0;
                PauseTransition hold = new PauseTransition(Duration.seconds(holdDuration));

                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), card);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> popup.hide());

                SequentialTransition seq = new SequentialTransition(enter, hold, fadeOut);
                seq.play();

                // Close button
                closeBtn.setOnMouseClicked(e -> {
                    seq.stop();
                    FadeTransition quickFade = new FadeTransition(Duration.millis(200), card);
                    quickFade.setFromValue(card.getOpacity());
                    quickFade.setToValue(0);
                    quickFade.setOnFinished(fe -> popup.hide());
                    quickFade.play();
                });

                // Pause on hover
                card.setOnMouseEntered(e -> hold.pause());
                card.setOnMouseExited(e -> hold.play());

            } catch (Exception e) {
                System.err.println("Could not show welcome notification: " + e.getMessage());
            }
        });
        wait.play();
    }
}
