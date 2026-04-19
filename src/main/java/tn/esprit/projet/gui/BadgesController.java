package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BadgesController {

    @FXML private VBox unlockedList;
    @FXML private VBox inProgressList;
    @FXML private VBox lockedList;
    @FXML private Label lblUnlockedCount;
    @FXML private Label lblInProgressCount;
    @FXML private Label lblLockedCount;

    private final BadgeRepository repo = new BadgeRepository();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] CHALLENGES = {
        "Eat 5 servings of vegetables today! 🥦",
        "Drink 8 glasses of water today! 💧",
        "Try a new fruit you've never eaten before! 🍍",
        "Cook a meal from scratch tonight! 🍳",
        "Replace one snack with nuts or seeds! 🌰",
        "Eat a rainbow — include 5 different colored foods! 🌈",
        "Skip processed sugar for the entire day! 🚫🍬",
        "Add a probiotic food to your diet today! 🥛",
    };

    @FXML
    public void initialize() {
        int userId = Session.getCurrentUser().getId();
        repo.seedDefaultBadges();
        repo.ensureUserBadges(userId);
        loadBadges(userId);
    }

    private void loadBadges(int userId) {
        List<UserBadge> all = repo.findByUser(userId);
        List<UserBadge> unlocked   = all.stream().filter(UserBadge::isUnlocked).collect(Collectors.toList());
        List<UserBadge> inProgress = all.stream().filter(ub -> !ub.isUnlocked() && ub.getCurrentValue() > 0).collect(Collectors.toList());
        List<UserBadge> locked     = all.stream().filter(ub -> !ub.isUnlocked() && ub.getCurrentValue() == 0).collect(Collectors.toList());

        set(lblUnlockedCount,   String.valueOf(unlocked.size()));
        set(lblInProgressCount, String.valueOf(inProgress.size()));
        set(lblLockedCount,     String.valueOf(locked.size()));

        if (unlockedList   != null) { unlockedList.getChildren().clear();   unlocked.forEach(ub -> unlockedList.getChildren().add(buildCard(ub, userId))); }
        if (inProgressList != null) { inProgressList.getChildren().clear(); inProgress.forEach(ub -> inProgressList.getChildren().add(buildCard(ub, userId))); }
        if (lockedList     != null) { lockedList.getChildren().clear();     locked.forEach(ub -> lockedList.getChildren().add(buildCard(ub, userId))); }
    }

    private HBox buildCard(UserBadge ub, int userId) {
        Label icon = new Label(ub.getBadge().getIcon() != null ? ub.getBadge().getIcon() : "🏅");
        icon.setStyle("-fx-font-size:26px;");
        StackPane iconBox = new StackPane(icon);
        iconBox.setPrefSize(50, 50);
        iconBox.setStyle("-fx-background-color:" + (ub.isUnlocked() ? "#DCFCE7" : "#F1F5F9") + ";-fx-background-radius:25;");

        Label name = new Label(ub.getBadge().getName());
        name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        Label desc = new Label(ub.getBadge().getDescription());
        desc.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
        VBox info = new VBox(3, name, desc);

        if (!ub.isUnlocked() && ub.getCurrentValue() > 0) {
            int pct = ub.getProgression();
            ProgressBar pb = new ProgressBar(pct / 100.0);
            pb.setPrefWidth(200); pb.setPrefHeight(6);
            Label pctLbl = new Label(pct + "% (" + ub.getCurrentValue() + "/" + ub.getBadge().getConditionValue() + ")");
            pctLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");
            info.getChildren().addAll(pb, pctLbl);
        }

        if (ub.isUnlocked() && ub.getUnlockedAt() != null) {
            Label date = new Label("Unlocked " + ub.getUnlockedAt().format(FMT));
            date.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;");
            info.getChildren().add(date);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox card = new HBox(12, iconBox, info, spacer);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:12 16;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        if (ub.isUnlocked()) {
            Button btnV = new Button(ub.isVitrine() ? "★ Vitrine" : "☆ Vitrine");
            btnV.setStyle("-fx-background-color:" + (ub.isVitrine() ? "#FEF3C7" : "#F1F5F9") +
                    ";-fx-text-fill:" + (ub.isVitrine() ? "#D97706" : "#64748B") +
                    ";-fx-font-size:11px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:4 10;");
            btnV.setOnAction(e -> {
                boolean newState = !ub.isVitrine();
                if (!repo.setVitrine(ub.getId(), userId, newState)) {
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setContentText("You can only display 3 badges in your vitrine.");
                    a.showAndWait();
                } else {
                    loadBadges(userId);
                }
            });
            card.getChildren().add(btnV);
        }
        return card;
    }

    @FXML private void handleSpinWheel() {
        List<UserBadge> all = repo.findByUser(Session.getCurrentUser().getId());
        if (all.isEmpty()) return;
        UserBadge prize = all.get(new Random().nextInt(all.size()));
        Stage owner = (Stage) unlockedList.getScene().getWindow();
        Toasts.show(owner, "🎰 You spun: " + prize.getBadge().getIcon() + " " + prize.getBadge().getName() + "!", Toasts.Type.INFO);
    }

    @FXML private void handleSurpriseChallenge() {
        String challenge = CHALLENGES[new Random().nextInt(CHALLENGES.length)];
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("🎯 Surprise Challenge!");
        a.setContentText(challenge);
        a.showAndWait();
    }

    @FXML private void handleClose() {
        ((Stage) unlockedList.getScene().getWindow()).close();
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }
}
