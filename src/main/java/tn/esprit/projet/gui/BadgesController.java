package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.services.BadgeService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BadgesController {

    @FXML private Label       lblBadgesCount;
    @FXML private Label       lblUnlockedCount;
    @FXML private Label       lblInProgressCount;
    @FXML private Label       lblLockedCount;
    @FXML private Label       lblPercent;
    @FXML private ProgressBar overallBar;
    @FXML private Label       lblJourneyLabel;
    @FXML private Label       lblNextBadge;
    @FXML private HBox        showcaseBox;
    @FXML private Label       lblShowcaseEmpty;
    @FXML private TextField   searchField;
    @FXML private VBox        unlockedList;
    @FXML private VBox        inProgressList;
    @FXML private VBox        lockedList;

    private final BadgeRepository badgeRepo    = new BadgeRepository();
    private final BadgeService    badgeService = new BadgeService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int userId;

    @FXML
    public void initialize() {
        userId = Session.getCurrentUser().getId();
        badgeRepo.seedDefaultBadges();
        badgeRepo.ensureUserBadges(userId);

        tn.esprit.projet.services.RankService rankService = new tn.esprit.projet.services.RankService();
        tn.esprit.projet.services.RankService.RankInfo rankBefore = rankService.getRankInfo(userId);

        List<String> newBadges = badgeService.refreshBadges(Session.getCurrentUser());
        loadAll();

        if (searchField != null)
            searchField.textProperty().addListener((o, a, b) -> filterBadges(b));

        if (!newBadges.isEmpty()) {
            new Thread(() -> {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    Stage owner = (Stage) unlockedList.getScene().getWindow();
                    for (String b : newBadges)
                        Toasts.show(owner, "🎉 Badge unlocked: " + b, Toasts.Type.SUCCESS);

                    // Check for level up
                    tn.esprit.projet.services.RankService.RankInfo rankAfter = rankService.getRankInfo(userId);
                    if (!rankAfter.currentRank.title.equals(rankBefore.currentRank.title)) {
                        showLevelUpDialog(rankAfter.currentRank);
                    }
                });
            }).start();
        }
    }

    private void loadAll() {
        BadgeService.BadgesDisplay display = badgeService.getBadgesForDisplay(Session.getCurrentUser());
        tn.esprit.projet.services.RankService rankService = new tn.esprit.projet.services.RankService();
        tn.esprit.projet.services.RankService.RankInfo rankInfo = rankService.getRankInfo(userId);

        int total    = display.total;
        int unlocked = display.unlockedCount;
        int pct      = display.percent;

        set(lblBadgesCount, rankInfo.currentRank.emoji + " " + rankInfo.currentRank.title +
                "  ·  " + rankInfo.totalXP + " XP  ·  " + unlocked + "/" + total + " badges");
        set(lblUnlockedCount,    String.valueOf(unlocked));
        set(lblInProgressCount,  String.valueOf(display.inProgress.size()));
        set(lblLockedCount,      String.valueOf(display.locked.size()));
        set(lblPercent,          pct + "%");
        set(lblJourneyLabel,     unlocked + " of " + total + " badges earned");

        if (overallBar != null) overallBar.setProgress(pct / 100.0);

        // Next badge hint
        if (!display.inProgress.isEmpty()) {
            UserBadge next = display.inProgress.get(0);
            int remaining = next.getBadge().getConditionValue() - next.getCurrentValue();
            set(lblNextBadge, "🎯 Next up: \"" + next.getBadge().getNom() + "\" — " +
                    remaining + " more " + getUnit(next.getBadge().getConditionType()) + " to go!");
        } else if (!display.locked.isEmpty()) {
            UserBadge next = display.locked.get(0);
            set(lblNextBadge, "🔓 Start working on: \"" + next.getBadge().getNom() + "\" — " +
                    next.getBadge().getConditionText());
        } else {
            set(lblNextBadge, "🏆 Amazing! You've completed all available badges!");
        }

        // Showcase
        buildShowcase(display.unlocked);

        // Badge lists
        buildList(unlockedList,   display.unlocked,   "unlocked");
        buildList(inProgressList, display.inProgress, "inProgress");
        buildList(lockedList,     display.locked,     "locked");
    }

    private String getUnit(String conditionType) {
        if (conditionType == null) return "steps";
        return switch (conditionType) {
            case "weight_logs"         -> "weight logs";
            case "streak_days"         -> "days";
            case "kg_progress"         -> "kg";
            case "account_age_days"    -> "days";
            case "arena_points"        -> "points";
            case "challenges_done"     -> "challenges";
            default -> "actions";
        };
    }

    // ── Showcase ───────────────────────────────────────────────────────────────

    private void buildShowcase(List<UserBadge> unlocked) {
        if (showcaseBox == null) return;
        showcaseBox.getChildren().clear();

        List<UserBadge> pinned = unlocked.stream().filter(UserBadge::isVitrine).collect(Collectors.toList());

        if (pinned.isEmpty()) {
            if (lblShowcaseEmpty != null) { lblShowcaseEmpty.setVisible(true); lblShowcaseEmpty.setManaged(true); }
            return;
        }
        if (lblShowcaseEmpty != null) { lblShowcaseEmpty.setVisible(false); lblShowcaseEmpty.setManaged(false); }

        for (UserBadge ub : pinned) {
            String couleur   = nvl(ub.getBadge().getCouleur(),   "#2E7D32");
            String couleurBg = nvl(ub.getBadge().getCouleurBg(), "#F0FDF4");

            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPrefWidth(120);
            card.setStyle("-fx-background-color:" + couleurBg + ";-fx-background-radius:14;" +
                    "-fx-padding:12;-fx-border-color:" + couleur + ";-fx-border-radius:14;-fx-border-width:2;" +
                    "-fx-effect:dropshadow(gaussian," + couleur + ",10,0.2,0,0);");

            Label icon = new Label(nvl(ub.getBadge().getSvg(), "🏅"));
            icon.setStyle("-fx-font-size:28px;");

            Label name = new Label(ub.getBadge().getNom());
            name.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";-fx-text-alignment:center;");
            name.setWrapText(true);
            name.setMaxWidth(100);

            Label rarity = new Label(nvl(ub.getBadge().getRarete(), "common").toUpperCase());
            rarity.setStyle(getRarityStyle(ub.getBadge().getRarete()));

            card.getChildren().addAll(icon, name, rarity);
            showcaseBox.getChildren().add(card);
        }

        // Add "+" slot if less than 3
        if (pinned.size() < 3) {
            VBox slot = new VBox(6);
            slot.setAlignment(Pos.CENTER);
            slot.setPrefWidth(100);
            slot.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:14;-fx-padding:12;" +
                    "-fx-border-color:#E2E8F0;-fx-border-radius:14;-fx-border-width:2;-fx-border-style:dashed;");
            Label plus = new Label("＋");
            plus.setStyle("-fx-font-size:24px;-fx-text-fill:#CBD5E1;");
            Label hint = new Label("Pin a badge");
            hint.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");
            slot.getChildren().addAll(plus, hint);
            showcaseBox.getChildren().add(slot);
        }
    }

    // ── Badge list builder ─────────────────────────────────────────────────────

    private void buildList(VBox container, List<UserBadge> badges, String type) {
        if (container == null) return;
        container.getChildren().clear();

        if (badges.isEmpty()) {
            Label empty = new Label(switch (type) {
                case "unlocked"    -> "No badges unlocked yet — start your journey!";
                case "inProgress"  -> "No badges in progress — log your weight or create a recipe!";
                default            -> "All badges unlocked! 🎉";
            });
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-font-style:italic;-fx-padding:8 0;");
            container.getChildren().add(empty);
            return;
        }

        for (UserBadge ub : badges) {
            HBox card = buildCard(ub, type);
            container.getChildren().add(card);
        }
    }

    private HBox buildCard(UserBadge ub, String type) {
        String couleur   = nvl(ub.getBadge().getCouleur(),   "#2E7D32");
        String couleurBg = nvl(ub.getBadge().getCouleurBg(), "#F0FDF4");
        boolean locked   = "locked".equals(type);

        // Icon
        Label iconLbl = new Label(locked ? "🔒" : nvl(ub.getBadge().getSvg(), "🏅"));
        iconLbl.setStyle("-fx-font-size:24px;" + (locked ? "-fx-opacity:0.4;" : ""));
        StackPane iconBox = new StackPane(iconLbl);
        iconBox.setPrefSize(52, 52); iconBox.setMinSize(52, 52);
        iconBox.setStyle("-fx-background-color:" + (locked ? "#F1F5F9" : couleurBg) + ";" +
                "-fx-background-radius:26;-fx-border-color:" + (locked ? "#E2E8F0" : couleur) + ";" +
                "-fx-border-radius:26;-fx-border-width:2;" +
                (ub.isUnlocked() ? "-fx-effect:dropshadow(gaussian," + couleur + ",8,0.25,0,0);" : ""));

        // Name + rarity
        Label nameLbl = new Label(locked ? "???" : ub.getBadge().getNom());
        nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" +
                (locked ? "#94A3B8" : "#1E293B") + ";");

        Label rarityLbl = new Label(nvl(ub.getBadge().getRarete(), "common").toUpperCase());
        rarityLbl.setStyle(getRarityStyle(ub.getBadge().getRarete()));

        HBox nameRow = new HBox(8, nameLbl, rarityLbl);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Description / hint
        String desc = locked
                ? "🔓 " + nvl(ub.getBadge().getConditionText(), "Complete the challenge")
                : nvl(ub.getBadge().getDescription(), "");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + (locked ? "#94A3B8" : "#64748B") + ";");
        descLbl.setWrapText(true);

        VBox info = new VBox(4, nameRow, descLbl);

        // Progress bar for in-progress
        if ("inProgress".equals(type) && ub.getCurrentValue() > 0) {
            int max = ub.getBadge().getConditionValue();
            int cur = ub.getCurrentValue();
            double pct = max > 0 ? (double) cur / max : 0;

            ProgressBar pb = new ProgressBar(pct);
            pb.setPrefWidth(200); pb.setPrefHeight(8);
            pb.setStyle("-fx-accent:" + couleur + ";-fx-background-color:#F1F5F9;-fx-background-radius:4;");

            Label pctLbl = new Label(cur + " / " + max + "  ·  " + (int)(pct * 100) + "% done");
            pctLbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");

            int remaining = max - cur;
            Label tipLbl = new Label("💡 " + remaining + " more " + getUnit(ub.getBadge().getConditionType()) + " to unlock!");
            tipLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#64748B;-fx-font-style:italic;");

            info.getChildren().addAll(pb, pctLbl, tipLbl);
        }

        // Unlock date + reward for unlocked
        if (ub.isUnlocked()) {
            if (ub.getUnlockedAt() != null) {
                Label dateLbl = new Label("✅ Unlocked " + ub.getUnlockedAt().format(FMT));
                dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;-fx-font-weight:bold;");
                info.getChildren().add(dateLbl);
            }
            // Reward tip
            String reward = getBadgeReward(ub.getBadge().getConditionType());
            if (reward != null) {
                Label rewardLbl = new Label("🎁 " + reward);
                rewardLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#7C3AED;-fx-font-weight:bold;" +
                        "-fx-background-color:#EDE9FE;-fx-background-radius:6;-fx-padding:2 8;");
                rewardLbl.setWrapText(true);
                info.getChildren().add(rewardLbl);
            }
        }

        // Category tag
        if (ub.getBadge().getCategorie() != null && !ub.getBadge().getCategorie().isBlank()) {
            Label catLbl = new Label("📁 " + ub.getBadge().getCategorie());
            catLbl.setStyle("-fx-font-size:9px;-fx-text-fill:#94A3B8;");
            info.getChildren().add(catLbl);
        }

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox card = new HBox(14, iconBox, info, spacer);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color:white;-fx-background-radius:16;-fx-padding:14 18;" +
            "-fx-border-color:" + (ub.isUnlocked() ? couleur : (locked ? "#E2E8F0" : "#FED7AA")) + ";" +
            "-fx-border-radius:16;-fx-border-width:" + (ub.isUnlocked() ? "2" : "1") + ";" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),8,0,0,2);" +
            (locked ? "-fx-opacity:0.6;" : "")
        );

        // Hover
        String baseStyle = card.getStyle();
        card.setOnMouseEntered(e -> card.setStyle(baseStyle.replace("rgba(0,0,0,0.05),8", "rgba(0,0,0,0.12),14")));
        card.setOnMouseExited(e  -> card.setStyle(baseStyle));

        // Pin/unpin button for unlocked
        if (ub.isUnlocked()) {
            Button pinBtn = new Button(ub.isVitrine() ? "⭐ Pinned" : "☆ Pin");
            pinBtn.setStyle(
                "-fx-background-color:" + (ub.isVitrine() ? "#FEF3C7" : "transparent") + ";" +
                "-fx-text-fill:" + (ub.isVitrine() ? "#D97706" : "#94A3B8") + ";" +
                "-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:20;-fx-cursor:hand;" +
                "-fx-border-color:" + (ub.isVitrine() ? "#FDE68A" : "#E2E8F0") + ";" +
                "-fx-border-radius:20;-fx-border-width:1.5;-fx-padding:5 12;"
            );
            pinBtn.setOnAction(e -> {
                BadgeService.VitrineResult result = badgeService.toggleVitrine(ub.getId(), userId);
                Stage owner = (Stage) unlockedList.getScene().getWindow();
                if (!result.success) {
                    Toasts.show(owner, "⚠️ Max 3 badges in showcase. Unpin one first.", Toasts.Type.WARNING);
                } else {
                    Toasts.show(owner, result.isVitrine ? "⭐ Badge pinned to showcase!" : "Badge unpinned.", Toasts.Type.SUCCESS);
                    loadAll();
                }
            });
            card.getChildren().add(pinBtn);

            // Fade in
            card.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), card);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }

        return card;
    }

    // ── Badge rewards — what you "get" for each badge ──────────────────────────

    private String getBadgeReward(String conditionType) {
        if (conditionType == null) return null;
        return switch (conditionType) {
            // Weight tracking rewards
            case "weight_logs"      -> "Consistency is the foundation of transformation!";
            
            // Goal rewards
            case "objective_50pct"  -> "You're halfway there! Your dedication is paying off!";
            case "objective_100pct" -> "GOAL ACHIEVED! You proved you can do anything!";
            case "kg_progress"      -> "Every kilogram is a victory. You're unstoppable!";
            
            // Consistency rewards
            case "streak_days"      -> "Daily habits create lasting results. Keep the fire burning!";
            
            // Profile & setup rewards
            case "profile_complete" -> "Complete profiles help us personalize your journey!";
            case "face_id_enrolled" -> "Your account is now ultra-secure with biometric protection!";
            case "photo_uploaded"   -> "A picture is worth a thousand motivations!";
            
            // Health rewards
            case "bmi_normal"       -> "Healthy BMI = reduced health risks. You're thriving!";
            case "account_age_days" -> "Time invested in health is never wasted!";
            
            // Engagement rewards
            case "arena_points"     -> "Competition fuels progress. Keep dominating!";
            case "challenges_done"  -> "Challenges push you beyond your limits!";
            
            // Special achievements
            case "early_morning_log"-> "Morning routines set the tone for success!";
            case "late_night_log"   -> "Night owls can be health champions too!";
            case "perfect_week"     -> "Perfect consistency = perfect results!";
            case "comeback"         -> "It's never too late to restart your journey!";
            
            default -> null;
        };
    }

    // ── Search ─────────────────────────────────────────────────────────────────

    private void filterBadges(String query) {
        if (query == null || query.isBlank()) { loadAll(); return; }
        String q = query.toLowerCase();
        BadgeService.BadgesDisplay display = badgeService.getBadgesForDisplay(Session.getCurrentUser());
        buildList(unlockedList,   display.unlocked.stream().filter(ub -> matches(ub, q)).collect(Collectors.toList()),   "unlocked");
        buildList(inProgressList, display.inProgress.stream().filter(ub -> matches(ub, q)).collect(Collectors.toList()), "inProgress");
        buildList(lockedList,     display.locked.stream().filter(ub -> matches(ub, q)).collect(Collectors.toList()),     "locked");
    }

    private boolean matches(UserBadge ub, String q) {
        return (ub.getBadge().getNom()       != null && ub.getBadge().getNom().toLowerCase().contains(q))
            || (ub.getBadge().getCategorie() != null && ub.getBadge().getCategorie().toLowerCase().contains(q))
            || (ub.getBadge().getRarete()    != null && ub.getBadge().getRarete().toLowerCase().contains(q));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String getRarityStyle(String rarete) {
        return switch (nvl(rarete, "common").toLowerCase()) {
            case "legendary" -> "-fx-background-color:#FEF3C7;-fx-text-fill:#D97706;-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:2 8;";
            case "epic"      -> "-fx-background-color:#EDE9FE;-fx-text-fill:#7C3AED;-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:2 8;";
            case "rare"      -> "-fx-background-color:#DBEAFE;-fx-text-fill:#1D4ED8;-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:2 8;";
            default          -> "-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;-fx-font-size:9px;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:2 8;";
        };
    }

    private void showLevelUpDialog(tn.esprit.projet.services.RankService.Rank rank) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setResizable(false);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(16);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color:white;-fx-padding:36 32;-fx-background-radius:20;");

        // Animated emoji
        javafx.scene.control.Label emojiLbl = new javafx.scene.control.Label(rank.emoji);
        emojiLbl.setStyle("-fx-font-size:64px;");
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(600), emojiLbl);
        st.setFromX(0.3); st.setToX(1.0);
        st.setFromY(0.3); st.setToY(1.0);
        st.setCycleCount(1);
        st.play();

        javafx.scene.control.Label title = new javafx.scene.control.Label("🎊 LEVEL UP!");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#7C3AED;");

        javafx.scene.control.Label rankLbl = new javafx.scene.control.Label("You are now: " + rank.emoji + " " + rank.title);
        rankLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + rank.color + ";");

        javafx.scene.control.Label motivLbl = new javafx.scene.control.Label(rank.motivation);
        motivLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#64748B;-fx-font-style:italic;");
        motivLbl.setWrapText(true);
        motivLbl.setMaxWidth(300);
        motivLbl.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Label xpLbl = new javafx.scene.control.Label(
            "Keep earning badges to reach the next rank!");
        xpLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;");

        javafx.scene.control.Button btn = new javafx.scene.control.Button("🚀  Let's go!");
        btn.setStyle("-fx-background-color:" + rank.color + ";-fx-text-fill:white;-fx-font-size:14px;" +
                "-fx-font-weight:bold;-fx-background-radius:12;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-padding:12 32;" +
                "-fx-effect:dropshadow(gaussian," + rank.color + ",12,0.3,0,0);");
        btn.setOnAction(e -> popup.close());

        root.getChildren().addAll(emojiLbl, title, rankLbl, motivLbl, xpLbl, btn);
        popup.setScene(new javafx.scene.Scene(root, 380, 340));
        popup.showAndWait();
    }

    @FXML private void handleClose() {
        ((Stage) unlockedList.getScene().getWindow()).close();
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : ""); }
    private String nvl(String s, String def) { return (s != null && !s.isBlank()) ? s : def; }
}
