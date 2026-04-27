package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.services.RankService;
import tn.esprit.projet.services.WeeklyChallengeService;
import tn.esprit.projet.services.WeeklyChallengeService.Challenge;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeeklyChallengesController {

    @FXML private Label  lblWeekLabel;
    @FXML private Label  lblDaysLeft;
    @FXML private Label  lblWeeklyXP;
    @FXML private Label  lblPerfectWeek;
    @FXML private Label  lblCompletedCount;
    @FXML private VBox   challengeContainer;
    @FXML private VBox   perfectWeekCard;
    @FXML private ProgressBar perfectWeekBar;
    @FXML private Label  lblPerfectWeekProgress;
    @FXML private Canvas confettiCanvas;

    private final WeeklyChallengeService challengeService = new WeeklyChallengeService();
    private final RankService rankService = new RankService();
    private int userId;

    // Confetti particles
    private final List<Confetti> particles = new ArrayList<>();
    private AnimationTimer confettiTimer;

    @FXML
    public void initialize() {
        userId = Session.getCurrentUser().getId();
        loadChallenges();
    }

    public void loadChallenges() {
        List<Challenge> challenges = challengeService.getWeeklyChallenges(userId);

        // Week label
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd   = weekStart.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        set(lblWeekLabel, "Week of " + weekStart.format(fmt) + " – " + weekEnd.format(fmt));

        // Days left
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), weekEnd) + 1;
        set(lblDaysLeft, daysLeft + " day" + (daysLeft == 1 ? "" : "s") + " left");

        // XP this week
        int weeklyXP = challengeService.getTotalWeeklyXP(userId);
        set(lblWeeklyXP, weeklyXP + " XP earned this week");

        // Completed count
        long completedCount = challenges.stream().filter(c -> c.completed).count();
        set(lblCompletedCount, completedCount + "/3");

        // Perfect week
        boolean perfect = challengeService.isPerfectWeek(userId) && completedCount == 3;
        if (perfect) {
            set(lblPerfectWeek, "🎊 PERFECT WEEK ACHIEVED!");
            if (lblPerfectWeek != null)
                lblPerfectWeek.setStyle("-fx-font-size:12px;-fx-text-fill:#FDE68A;-fx-font-weight:bold;");
        } else {
            set(lblPerfectWeek, completedCount + "/3 challenges done");
        }

        // Perfect week progress bar
        if (perfectWeekBar != null) perfectWeekBar.setProgress(completedCount / 3.0);
        set(lblPerfectWeekProgress, completedCount + " / 3 challenges completed");
        if (perfectWeekCard != null && perfect) {
            perfectWeekCard.setStyle(perfectWeekCard.getStyle().replace(
                "-fx-border-color:#FDE68A", "-fx-border-color:#16A34A"));
        }

        // Build challenge cards
        if (challengeContainer != null) {
            challengeContainer.getChildren().clear();
            for (Challenge ch : challenges) {
                challengeContainer.getChildren().add(buildChallengeCard(ch));
            }
        }
    }

    private VBox buildChallengeCard(Challenge ch) {
        boolean done = ch.completed;

        // Colors
        String borderColor = done ? "#16A34A" : "#E2E8F0";
        String bgColor     = done ? "#F0FDF4" : "white";
        String xpColor     = "#7C3AED";

        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color:" + bgColor + ";-fx-background-radius:16;-fx-padding:18 20;" +
            "-fx-border-color:" + borderColor + ";-fx-border-radius:16;-fx-border-width:2;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),10,0,0,2);"
        );

        // Top row: emoji + title + XP badge
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Icon circle
        Label iconLbl = new Label(done ? "✅" : (ch.emoji != null ? ch.emoji : "🎯"));
        iconLbl.setStyle("-fx-font-size:26px;");
        StackPane iconBox = new StackPane(iconLbl);
        iconBox.setPrefSize(50, 50); iconBox.setMinSize(50, 50);
        iconBox.setStyle(
            "-fx-background-color:" + (done ? "#DCFCE7" : "#F1F5F9") + ";" +
            "-fx-background-radius:25;-fx-border-color:" + (done ? "#16A34A" : "#E2E8F0") + ";" +
            "-fx-border-radius:25;-fx-border-width:2;"
        );

        // Title + description
        Label titleLbl = new Label(ch.title != null ? ch.title : "Challenge");
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" +
                (done ? "#16A34A" : "#1E293B") + ";");

        Label descLbl = new Label(ch.description != null ? ch.description : "");
        descLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
        descLbl.setWrapText(true);

        VBox titleBox = new VBox(3, titleLbl, descLbl);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // XP reward
        Label xpLbl = new Label("+" + ch.xpReward + " XP");
        xpLbl.setStyle(
            "-fx-background-color:" + (done ? "#EDE9FE" : "#F5F3FF") + ";" +
            "-fx-text-fill:" + xpColor + ";-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-background-radius:10;-fx-padding:4 10;"
        );

        topRow.getChildren().addAll(iconBox, titleBox, xpLbl);

        // Progress bar
        ProgressBar pb = new ProgressBar(ch.getProgress());
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(10);
        pb.setStyle(
            "-fx-accent:" + (done ? "#16A34A" : "#7C3AED") + ";" +
            "-fx-background-color:#F1F5F9;-fx-background-radius:5;"
        );

        // Progress label
        String progressText = done
            ? "✅ Completed!"
            : ch.currentValue + " / " + ch.targetValue + " — " + (int)(ch.getProgress() * 100) + "% done";
        Label progressLbl = new Label(progressText);
        progressLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" +
                (done ? "#16A34A" : "#64748B") + ";");

        card.getChildren().addAll(topRow, pb, progressLbl);

        // Badge reward
        if (ch.badgeReward != null && !ch.badgeReward.isBlank()) {
            Label badgeLbl = new Label("🏅 Unlocks badge: \"" + ch.badgeReward + "\"");
            badgeLbl.setStyle(
                "-fx-font-size:10px;-fx-text-fill:" + (done ? "#16A34A" : "#D97706") + ";" +
                "-fx-background-color:" + (done ? "#DCFCE7" : "#FEF3C7") + ";" +
                "-fx-background-radius:6;-fx-padding:3 10;-fx-font-weight:bold;"
            );
            card.getChildren().add(badgeLbl);
        }

        // Completed at
        if (done && ch.completedAt != null) {
            Label dateLbl = new Label("Completed " + ch.completedAt.format(
                    DateTimeFormatter.ofPattern("EEE, MMM d 'at' HH:mm")));
            dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");
            card.getChildren().add(dateLbl);
        }

        // Fade in
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        return card;
    }

    // ── Confetti animation ─────────────────────────────────────────────────────

    public void triggerConfetti() {
        if (confettiCanvas == null) return;

        particles.clear();
        Random rnd = new Random();
        String[] colors = {"#FF6B6B","#FFE66D","#4ECDC4","#45B7D1","#96CEB4","#FFEAA7","#DDA0DD","#98D8C8"};

        for (int i = 0; i < 80; i++) {
            Confetti p = new Confetti();
            p.x     = rnd.nextDouble() * 560;
            p.y     = -10 - rnd.nextDouble() * 100;
            p.vx    = (rnd.nextDouble() - 0.5) * 4;
            p.vy    = 2 + rnd.nextDouble() * 4;
            p.size  = 6 + rnd.nextDouble() * 8;
            p.color = colors[rnd.nextInt(colors.length)];
            p.rot   = rnd.nextDouble() * 360;
            p.rotV  = (rnd.nextDouble() - 0.5) * 8;
            p.shape = rnd.nextInt(3); // 0=rect, 1=circle, 2=triangle
            particles.add(p);
        }

        if (confettiTimer != null) confettiTimer.stop();

        confettiTimer = new AnimationTimer() {
            int frames = 0;
            @Override public void handle(long now) {
                frames++;
                GraphicsContext gc = confettiCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, confettiCanvas.getWidth(), confettiCanvas.getHeight());

                boolean anyVisible = false;
                for (Confetti p : particles) {
                    p.x   += p.vx;
                    p.y   += p.vy;
                    p.vy  += 0.1; // gravity
                    p.rot += p.rotV;
                    p.alpha = Math.max(0, 1.0 - (p.y / 700.0));

                    if (p.y < 700 && p.alpha > 0) {
                        anyVisible = true;
                        gc.save();
                        gc.translate(p.x, p.y);
                        gc.rotate(p.rot);
                        gc.setGlobalAlpha(p.alpha);
                        gc.setFill(Color.web(p.color));
                        switch (p.shape) {
                            case 0 -> gc.fillRect(-p.size/2, -p.size/4, p.size, p.size/2);
                            case 1 -> gc.fillOval(-p.size/2, -p.size/2, p.size, p.size);
                            case 2 -> gc.fillPolygon(
                                new double[]{0, p.size/2, -p.size/2},
                                new double[]{-p.size/2, p.size/2, p.size/2}, 3);
                        }
                        gc.restore();
                    }
                }

                if (!anyVisible || frames > 180) {
                    gc.clearRect(0, 0, confettiCanvas.getWidth(), confettiCanvas.getHeight());
                    stop();
                }
            }
        };
        confettiTimer.start();
    }

    private static class Confetti {
        double x, y, vx, vy, size, rot, rotV, alpha;
        String color;
        int shape;
    }

    @FXML private void handleClose() {
        if (confettiTimer != null) confettiTimer.stop();
        ((Stage) challengeContainer.getScene().getWindow()).close();
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : ""); }

    // ── Static method called from other controllers when events happen ─────────

    /**
     * Call this from WeightObjectiveController after saving a weight log.
     * eventType: "weight_log", "weight_photo", "recipe", "objective"
     */
    public static void notifyEvent(int userId, String eventType, Stage ownerStage) {
        WeeklyChallengeService svc = new WeeklyChallengeService();
        RankService rankSvc = new RankService();

        List<String> completed = svc.updateProgress(userId, eventType);
        if (completed.isEmpty()) return;

        // Show toast for each completed challenge
        Platform.runLater(() -> {
            for (String entry : completed) {
                String[] parts = entry.split("\\|");
                int xp = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                String badge = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;

                if (ownerStage != null) {
                    Toasts.show(ownerStage, "⚡ Challenge completed! +" + xp + " XP", Toasts.Type.SUCCESS);
                    if (badge != null)
                        Toasts.show(ownerStage, "🏅 Badge unlocked: " + badge, Toasts.Type.SUCCESS);
                }
            }

            // Check perfect week
            if (svc.isPerfectWeek(userId) && ownerStage != null) {
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() ->
                        Toasts.show(ownerStage, "🏆 PERFECT WEEK! +50 bonus XP!", Toasts.Type.SUCCESS));
                }).start();
            }
        });
    }
}
