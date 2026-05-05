package tn.esprit.projet.utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.esprit.projet.models.User;

/**
 * Premium animated motivational welcome notification with glassmorphism design.
 * Injects directly into the scene's root pane for reliable display.
 */
public class WelcomePopup {

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

    /**
     * Show welcome notification by injecting into a StackPane.
     * @param stackPane The StackPane to inject into (contentArea)
     * @param user The logged-in user
     */
    public static void show(StackPane stackPane, User user) {
        if (stackPane == null || user == null) return;

        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        boolean isAdmin = user.isAdmin();
        String motivation = MOTIVATIONS[(int) (Math.random() * MOTIVATIONS.length)];

        // ── Color scheme ───────────────────────────────────────────────────────
        String primaryColor   = isAdmin ? "#7C3AED" : "#16A34A";
        String secondaryColor = isAdmin ? "#A78BFA" : "#4ADE80";

        // ── Avatar with glow effect ────────────────────────────────────────────
        String avatarLetter = firstName.substring(0, 1).toUpperCase();
        
        Circle avatarCircle = new Circle(32);
        avatarCircle.setFill(Color.web(primaryColor));
        
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setColor(Color.web(primaryColor, 0.4));
        avatarGlow.setRadius(20);
        avatarGlow.setSpread(0.3);
        
        Label avatarLbl = new Label(avatarLetter);
        avatarLbl.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:white;");
        
        StackPane avatar = new StackPane(avatarCircle, avatarLbl);
        avatar.setPrefSize(64, 64);
        avatar.setEffect(avatarGlow);

        // Subtle pulse animation for avatar
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), avatar);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        // ── Status indicator (online dot) ──────────────────────────────────────
        Circle statusDot = new Circle(6);
        statusDot.setFill(Color.web("#10B981"));
        statusDot.setStroke(Color.WHITE);
        statusDot.setStrokeWidth(2.5);
        
        DropShadow dotGlow = new DropShadow();
        dotGlow.setColor(Color.web("#10B981", 0.6));
        dotGlow.setRadius(8);
        statusDot.setEffect(dotGlow);
        
        StackPane statusIndicator = new StackPane(statusDot);
        statusIndicator.setTranslateX(20);
        statusIndicator.setTranslateY(20);
        
        StackPane avatarWithStatus = new StackPane(avatar, statusIndicator);
        StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);

        // ── Role badge with icon ───────────────────────────────────────────────
        String roleIcon = isAdmin ? "👑" : "✨";
        String roleText = isAdmin ? "Administrator" : "Member";
        String roleBgColor = isAdmin ? "rgba(124,58,237,0.12)" : "rgba(22,163,74,0.12)";
        
        Label roleIcon_lbl = new Label(roleIcon);
        roleIcon_lbl.setStyle("-fx-font-size:12px;");
        
        Label roleText_lbl = new Label(roleText);
        roleText_lbl.setStyle(
            "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:0.5px;"
        );
        
        HBox roleBadge = new HBox(4, roleIcon_lbl, roleText_lbl);
        roleBadge.setAlignment(Pos.CENTER);
        roleBadge.setPadding(new Insets(4, 12, 4, 12));
        roleBadge.setStyle(
            "-fx-background-color:" + roleBgColor + ";" +
            "-fx-background-radius:20;" +
            "-fx-border-color:" + primaryColor + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:20;"
        );

        // ── Greeting with gradient text effect ─────────────────────────────────
        Label greeting = new Label("Welcome back, " + firstName + "!");
        greeting.setStyle(
            "-fx-font-size:22px;-fx-font-weight:800;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:-0.5px;"
        );

        // ── Motivation text with refined typography ────────────────────────────
        Label motivationLbl = new Label(motivation);
        motivationLbl.setWrapText(true);
        motivationLbl.setMaxWidth(320);
        motivationLbl.setStyle(
            "-fx-font-size:13px;-fx-text-fill:#475569;-fx-line-spacing:4;" +
            "-fx-font-weight:500;"
        );

        // ── Time-based greeting ────────────────────────────────────────────────
        int hour = java.time.LocalTime.now().getHour();
        String timeGreeting;
        String timeIcon;
        if (hour < 12) {
            timeGreeting = "Good morning";
            timeIcon = "☀️";
        } else if (hour < 18) {
            timeGreeting = "Good afternoon";
            timeIcon = "🌤️";
        } else {
            timeGreeting = "Good evening";
            timeIcon = "🌙";
        }

        Label timeIcon_lbl = new Label(timeIcon);
        timeIcon_lbl.setStyle("-fx-font-size:13px;");
        
        Label timeText = new Label(timeGreeting + "! Have a productive session.");
        timeText.setStyle(
            "-fx-font-size:11px;-fx-text-fill:#64748B;-fx-font-weight:500;"
        );
        
        HBox footer = new HBox(6, timeIcon_lbl, timeText);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 0, 0, 0));

        // ── Elegant divider ────────────────────────────────────────────────────
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle(
            "-fx-background-color:linear-gradient(to right, transparent, #E2E8F0, transparent);"
        );

        // ── Close button with hover effect ─────────────────────────────────────
        Label closeBtn = new Label("✕");
        closeBtn.setStyle(
            "-fx-font-size:16px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
            "-fx-padding:4 8;-fx-background-radius:8;" +
            "-fx-font-weight:bold;"
        );
        closeBtn.setOnMouseEntered(e -> 
            closeBtn.setStyle(
                "-fx-font-size:16px;-fx-text-fill:#EF4444;-fx-cursor:hand;" +
                "-fx-padding:4 8;-fx-background-color:#FEE2E2;-fx-background-radius:8;" +
                "-fx-font-weight:bold;"
            )
        );
        closeBtn.setOnMouseExited(e -> 
            closeBtn.setStyle(
                "-fx-font-size:16px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
                "-fx-padding:4 8;-fx-background-radius:8;" +
                "-fx-font-weight:bold;"
            )
        );

        // ── Content layout ─────────────────────────────────────────────────────
        VBox textContent = new VBox(8, roleBadge, greeting, motivationLbl);
        textContent.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox topRow = new HBox(16, avatarWithStatus, textContent, closeBtn);
        topRow.setAlignment(Pos.TOP_LEFT);

        VBox mainContent = new VBox(16, topRow, divider, footer);
        mainContent.setPadding(new Insets(24, 26, 20, 26));
        mainContent.setPrefWidth(420);

        // ── Glassmorphism card with premium shadow ─────────────────────────────
        StackPane card = new StackPane(mainContent);
        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:20;" +
            "-fx-border-color:#E2E8F0;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:20;"
        );

        // Premium shadow effect
        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.rgb(0, 0, 0, 0.12));
        cardShadow.setRadius(32);
        cardShadow.setSpread(0.0);
        cardShadow.setOffsetY(12);
        card.setEffect(cardShadow);

        // ── Accent gradient bar ────────────────────────────────────────────────
        Rectangle accentBar = new Rectangle(420, 5);
        accentBar.setArcWidth(20);
        accentBar.setArcHeight(20);
        accentBar.setFill(javafx.scene.paint.LinearGradient.valueOf(
            "linear-gradient(to right, " + primaryColor + ", " + secondaryColor + ")"
        ));

        VBox notificationCard = new VBox(0, accentBar, card);
        notificationCard.setStyle("-fx-background-radius:20;");
        notificationCard.setMouseTransparent(false);

        // ── Position in top-right corner ───────────────────────────────────────
        StackPane overlay = new StackPane(notificationCard);
        overlay.setAlignment(Pos.TOP_RIGHT);
        overlay.setPadding(new Insets(20, 20, 0, 0));
        overlay.setMouseTransparent(false);
        overlay.setPickOnBounds(false); // Only card is clickable

        // Add directly to StackPane
        stackPane.getChildren().add(overlay);

        // ── Premium entrance animation ─────────────────────────────────────────
        notificationCard.setOpacity(0);
        notificationCard.setTranslateY(-30);
        notificationCard.setScaleX(0.92);
        notificationCard.setScaleY(0.92);

        PauseTransition initialDelay = new PauseTransition(Duration.millis(400));
        initialDelay.setOnFinished(ev -> {

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationCard);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), notificationCard);
            slideIn.setFromY(-30);
            slideIn.setToY(0);
            slideIn.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1));

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), notificationCard);
            scaleIn.setFromX(0.92); scaleIn.setFromY(0.92);
            scaleIn.setToX(1.0); scaleIn.setToY(1.0);
            scaleIn.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1));

            ParallelTransition enter = new ParallelTransition(fadeIn, slideIn, scaleIn);

            // Different duration for users vs admins
            double holdDuration = isAdmin ? 6.5 : 10.0;
            PauseTransition hold = new PauseTransition(Duration.seconds(holdDuration));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(600), notificationCard);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(600), notificationCard);
            slideOut.setToY(-20);
            
            ParallelTransition exit = new ParallelTransition(fadeOut, slideOut);
            exit.setOnFinished(e -> stackPane.getChildren().remove(overlay));

            SequentialTransition seq = new SequentialTransition(enter, hold, exit);
            seq.play();
            
            // Start avatar pulse after entrance
            enter.setOnFinished(e -> pulse.play());

            // ── Close button interaction ───────────────────────────────────────
            closeBtn.setOnMouseClicked(e -> {
                seq.stop();
                pulse.stop();
                
                FadeTransition quickFade = new FadeTransition(Duration.millis(250), notificationCard);
                quickFade.setFromValue(notificationCard.getOpacity());
                quickFade.setToValue(0);
                
                ScaleTransition quickScale = new ScaleTransition(Duration.millis(250), notificationCard);
                quickScale.setToX(0.9);
                quickScale.setToY(0.9);
                
                ParallelTransition quickExit = new ParallelTransition(quickFade, quickScale);
                quickExit.setOnFinished(fe -> stackPane.getChildren().remove(overlay));
                quickExit.play();
            });

            // Pause timer on hover
            notificationCard.setOnMouseEntered(e -> {
                hold.pause();
                ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), notificationCard);
                hoverScale.setToX(1.02);
                hoverScale.setToY(1.02);
                hoverScale.play();
            });
            
            notificationCard.setOnMouseExited(e -> {
                hold.play();
                ScaleTransition unhoverScale = new ScaleTransition(Duration.millis(200), notificationCard);
                unhoverScale.setToX(1.0);
                unhoverScale.setToY(1.0);
                unhoverScale.play();
            });
        });
        initialDelay.play();
    }
}
