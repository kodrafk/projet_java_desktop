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
 * Professional welcome notification with perfect positioning and animations.
 */
public class ProfessionalWelcomeNotification {

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

        int randomIndex = (int) (Math.random() * MOTIVATIONS.length);
        String motivation = MOTIVATIONS[randomIndex];

        // Colors
        String primaryColor = isAdmin ? "#7C3AED" : "#16A34A";
        String secondaryColor = isAdmin ? "#A78BFA" : "#4ADE80";

        // Avatar with enhanced glow
        Circle avatarCircle = new Circle(30);
        avatarCircle.setFill(Color.web(primaryColor));
        
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setColor(Color.web(primaryColor, 0.5));
        avatarGlow.setRadius(18);
        avatarGlow.setSpread(0.4);
        
        Label avatarLbl = new Label(firstName.substring(0, 1).toUpperCase());
        avatarLbl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;");
        
        StackPane avatar = new StackPane(avatarCircle, avatarLbl);
        avatar.setPrefSize(60, 60);
        avatar.setEffect(avatarGlow);

        // Animated pulse for avatar
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), avatar);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.08); pulse.setToY(1.08);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        // Status indicator (online dot)
        Circle statusDot = new Circle(6);
        statusDot.setFill(Color.web("#10B981"));
        statusDot.setStroke(Color.WHITE);
        statusDot.setStrokeWidth(2.5);
        
        DropShadow dotGlow = new DropShadow();
        dotGlow.setColor(Color.web("#10B981", 0.7));
        dotGlow.setRadius(10);
        statusDot.setEffect(dotGlow);
        
        StackPane statusIndicator = new StackPane(statusDot);
        statusIndicator.setTranslateX(20);
        statusIndicator.setTranslateY(20);
        
        StackPane avatarWithStatus = new StackPane(avatar, statusIndicator);
        StackPane.setAlignment(statusIndicator, Pos.BOTTOM_RIGHT);

        // Role badge with enhanced styling
        String roleIcon = isAdmin ? "👑" : "✨";
        String roleText = isAdmin ? "Administrator" : "Member";
        
        Label roleIconLbl = new Label(roleIcon);
        roleIconLbl.setStyle("-fx-font-size:12px;");
        
        Label roleTextLbl = new Label(roleText);
        roleTextLbl.setStyle(
            "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:0.8px;"
        );
        
        HBox roleBadge = new HBox(4, roleIconLbl, roleTextLbl);
        roleBadge.setAlignment(Pos.CENTER);
        roleBadge.setPadding(new Insets(4, 12, 4, 12));
        roleBadge.setStyle(
            "-fx-background-color:rgba(" + (isAdmin ? "124,58,237" : "22,163,74") + ",0.15);" +
            "-fx-background-radius:20;" +
            "-fx-border-color:" + primaryColor + ";" +
            "-fx-border-width:1.5;" +
            "-fx-border-radius:20;"
        );

        // Greeting with proper name
        String welcomeText = "Welcome back, " + firstName + "!";
        
        Label greeting = new Label(welcomeText);
        greeting.setStyle(
            "-fx-font-size:20px;-fx-font-weight:800;-fx-text-fill:" + primaryColor + ";" +
            "-fx-letter-spacing:-0.4px;"
        );

        // Translation below main greeting - removed bilingual feature

        // Motivation text
        Label motivationLbl = new Label(motivation);
        motivationLbl.setWrapText(true);
        motivationLbl.setMaxWidth(300);
        motivationLbl.setStyle(
            "-fx-font-size:13px;-fx-text-fill:#475569;-fx-line-spacing:4;" +
            "-fx-font-weight:500;"
        );

        VBox motivationSection = new VBox(3, motivationLbl);
        motivationSection.setAlignment(Pos.TOP_LEFT);

        // Time-based greeting
        int hour = java.time.LocalTime.now().getHour();
        String timeIcon = hour < 12 ? "☀️" : (hour < 18 ? "🌤️" : "🌙");
        String timeGreeting = hour < 12 ? "Good morning" : (hour < 18 ? "Good afternoon" : "Good evening");
        String sessionText = "Have a productive session.";
        
        Label timeIconLbl = new Label(timeIcon);
        timeIconLbl.setStyle("-fx-font-size:13px;");
        
        Label timeText = new Label(timeGreeting + "! " + sessionText);
        timeText.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;-fx-font-weight:500;");
        
        HBox footer = new HBox(6, timeIconLbl, timeText);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 0, 0, 0));

        // Elegant divider with gradient
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setStyle(
            "-fx-background-color:linear-gradient(to right, transparent, " + primaryColor + ", transparent);" +
            "-fx-opacity:0.3;"
        );

        // Close button with enhanced hover
        Label closeBtn = new Label("✕");
        closeBtn.setStyle(
            "-fx-font-size:16px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
            "-fx-padding:6 8;-fx-background-radius:8;" +
            "-fx-font-weight:bold;"
        );
        
        closeBtn.setOnMouseEntered(e -> {
            closeBtn.setStyle(
                "-fx-font-size:16px;-fx-text-fill:#EF4444;-fx-cursor:hand;" +
                "-fx-padding:6 8;-fx-background-color:#FEE2E2;-fx-background-radius:8;" +
                "-fx-font-weight:bold;"
            );
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), closeBtn);
            scaleUp.setToX(1.1); scaleUp.setToY(1.1);
            scaleUp.play();
        });
        
        closeBtn.setOnMouseExited(e -> {
            closeBtn.setStyle(
                "-fx-font-size:16px;-fx-text-fill:#94A3B8;-fx-cursor:hand;" +
                "-fx-padding:6 8;-fx-background-radius:8;" +
                "-fx-font-weight:bold;"
            );
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), closeBtn);
            scaleDown.setToX(1.0); scaleDown.setToY(1.0);
            scaleDown.play();
        });

        // Content layout
        VBox greetingSection = new VBox(4, greeting);
        greetingSection.setAlignment(Pos.TOP_LEFT);
        
        VBox textContent = new VBox(8, roleBadge, greetingSection, motivationSection);
        textContent.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        HBox topRow = new HBox(16, avatarWithStatus, textContent, closeBtn);
        topRow.setAlignment(Pos.TOP_LEFT);

        VBox mainContent = new VBox(16, topRow, divider, footer);
        mainContent.setPadding(new Insets(22, 26, 20, 26));
        mainContent.setPrefWidth(450);  // Increased width for translations

        // Premium glassmorphism card
        StackPane card = new StackPane(mainContent);
        card.setStyle(
            "-fx-background-color:rgba(255,255,255,0.95);" +
            "-fx-background-radius:18;" +
            "-fx-border-color:rgba(255,255,255,0.8);" +
            "-fx-border-width:1;" +
            "-fx-border-radius:18;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),25,0,0,8);"
        );

        // Enhanced gradient accent bar
        Region accentBar = new Region();
        accentBar.setPrefHeight(5);
        accentBar.setPrefWidth(450);  // Match new width
        accentBar.setStyle(
            "-fx-background-color:linear-gradient(45deg, " + primaryColor + ", " + secondaryColor + ", " + primaryColor + ");" +
            "-fx-background-radius:18 18 0 0;"
        );

        VBox notificationCard = new VBox(0, accentBar, card);
        notificationCard.setStyle("-fx-background-radius:18;");

        // Popup with enhanced positioning
        Popup popup = new Popup();
        popup.getContent().add(notificationCard);
        popup.setAutoFix(true);
        popup.setAutoHide(false);

        // Professional positioning system
        PauseTransition wait = new PauseTransition(Duration.millis(600));
        wait.setOnFinished(ev -> {
            try {
                // Get screen dimensions
                javafx.geometry.Rectangle2D screen = javafx.stage.Screen.getPrimary().getVisualBounds();
                double screenWidth = screen.getWidth();
                double screenHeight = screen.getHeight();
                
                double x, y;
                
                // Smart positioning based on window state
                if (owner.isMaximized() || owner.getWidth() > screenWidth * 0.8) {
                    // Maximized window - use screen coordinates
                    x = screenWidth - 480;  // 30px margin from right (450 + 30)
                    y = 90;                 // 90px from top
                } else {
                    // Windowed mode - use window coordinates
                    x = owner.getX() + owner.getWidth() - 480;
                    y = owner.getY() + 90;
                }
                
                // Ensure popup stays fully on screen
                if (x < 20) x = 20;
                if (x + 450 > screenWidth) x = screenWidth - 470;
                if (y < 20) y = 20;
                if (y + 250 > screenHeight) y = screenHeight - 270;
                
                popup.show(owner, x, y);

                // Premium entrance animation
                notificationCard.setOpacity(0);
                notificationCard.setTranslateY(-40);
                notificationCard.setScaleX(0.9);
                notificationCard.setScaleY(0.9);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(600), notificationCard);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), notificationCard);
                slideIn.setFromY(-40);
                slideIn.setToY(0);
                slideIn.setInterpolator(Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94));

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), notificationCard);
                scaleIn.setFromX(0.9); scaleIn.setFromY(0.9);
                scaleIn.setToX(1.0); scaleIn.setToY(1.0);
                scaleIn.setInterpolator(Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94));

                ParallelTransition enter = new ParallelTransition(fadeIn, slideIn, scaleIn);

                // Duration based on user type
                double holdDuration = isAdmin ? 6.5 : 10.0;
                PauseTransition hold = new PauseTransition(Duration.seconds(holdDuration));

                // Smooth exit animation
                FadeTransition fadeOut = new FadeTransition(Duration.millis(700), notificationCard);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(700), notificationCard);
                slideOut.setToY(-30);
                
                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(700), notificationCard);
                scaleOut.setToX(0.95);
                scaleOut.setToY(0.95);
                
                ParallelTransition exit = new ParallelTransition(fadeOut, slideOut, scaleOut);
                exit.setOnFinished(e -> popup.hide());

                SequentialTransition seq = new SequentialTransition(enter, hold, exit);
                seq.play();
                
                // Start avatar pulse after entrance
                enter.setOnFinished(e -> pulse.play());

                // Close button interaction
                closeBtn.setOnMouseClicked(e -> {
                    seq.stop();
                    pulse.stop();
                    
                    FadeTransition quickFade = new FadeTransition(Duration.millis(300), notificationCard);
                    quickFade.setFromValue(notificationCard.getOpacity());
                    quickFade.setToValue(0);
                    
                    ScaleTransition quickScale = new ScaleTransition(Duration.millis(300), notificationCard);
                    quickScale.setToX(0.9);
                    quickScale.setToY(0.9);
                    
                    ParallelTransition quickExit = new ParallelTransition(quickFade, quickScale);
                    quickExit.setOnFinished(fe -> popup.hide());
                    quickExit.play();
                });

                // Enhanced hover interactions — safe pause/resume
                notificationCard.setOnMouseEntered(e -> {
                    try {
                        if (hold.getStatus() == javafx.animation.Animation.Status.RUNNING) hold.pause();
                    } catch (Exception ignored) {}
                    ScaleTransition hoverScale = new ScaleTransition(Duration.millis(200), notificationCard);
                    hoverScale.setToX(1.03);
                    hoverScale.setToY(1.03);
                    hoverScale.play();
                });
                
                notificationCard.setOnMouseExited(e -> {
                    try {
                        if (hold.getStatus() == javafx.animation.Animation.Status.PAUSED) hold.play();
                    } catch (Exception ignored) {}
                    ScaleTransition unhoverScale = new ScaleTransition(Duration.millis(200), notificationCard);
                    unhoverScale.setToX(1.0);
                    unhoverScale.setToY(1.0);
                    unhoverScale.play();
                });

            } catch (Exception e) {
                System.err.println("Could not show welcome notification: " + e.getMessage());
            }
        });
        wait.play();
    }
}