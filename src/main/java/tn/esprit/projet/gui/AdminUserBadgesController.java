package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.services.BadgeService;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminUserBadgesController — Admin panel to view user badges
 * Features:
 * - View all user badges (unlocked, in progress, locked)
 * - See badge progression and statistics
 * - Filter badges by category and rarity
 * - Export badge report
 */
public class AdminUserBadgesController {

    @FXML private Label       lblUserName;
    @FXML private Label       lblTotalBadges;
    @FXML private Label       lblUnlockedCount;
    @FXML private Label       lblInProgressCount;
    @FXML private Label       lblLockedCount;
    @FXML private Label       lblCompletionPercent;
    @FXML private ProgressBar progressBar;
    @FXML private TextField   searchField;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterRarity;
    @FXML private VBox        unlockedList;
    @FXML private VBox        inProgressList;
    @FXML private VBox        lockedList;
    @FXML private TabPane     tabPane;

    private User user;
    private final BadgeRepository badgeRepo = new BadgeRepository();
    private final BadgeService badgeService = new BadgeService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BadgeService.BadgesDisplay allBadges;

    public void setUser(User u) {
        this.user = u;
        initialize();
    }

    private void initialize() {
        if (user == null) return;

        // Setup filters
        if (filterCategory != null) {
            filterCategory.getItems().addAll("All", "Getting Started", "Weight Tracking", 
                "Goals", "Consistency", "Health", "Engagement", "Special");
            filterCategory.setValue("All");
            filterCategory.setOnAction(e -> applyFilters());
        }

        if (filterRarity != null) {
            filterRarity.getItems().addAll("All", "Common", "Rare", "Epic", "Legendary");
            filterRarity.setValue("All");
            filterRarity.setOnAction(e -> applyFilters());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((o, a, b) -> applyFilters());
        }

        loadBadges();
    }

    private void loadBadges() {
        // Refresh badges first
        badgeService.refreshBadges(user);
        
        // Get all badges
        allBadges = badgeService.getBadgesForDisplay(user);

        // Update header stats
        set(lblUserName, user.getFullName() + "'s Badges");
        set(lblTotalBadges, String.valueOf(allBadges.total));
        set(lblUnlockedCount, String.valueOf(allBadges.unlockedCount));
        set(lblInProgressCount, String.valueOf(allBadges.inProgress.size()));
        set(lblLockedCount, String.valueOf(allBadges.locked.size()));
        set(lblCompletionPercent, allBadges.percent + "%");

        if (progressBar != null) {
            progressBar.setProgress(allBadges.percent / 100.0);
        }

        // Display badges
        displayBadges(allBadges);
    }

    private void applyFilters() {
        if (allBadges == null) return;

        String search = searchField != null ? searchField.getText().toLowerCase() : "";
        String category = filterCategory != null ? filterCategory.getValue() : "All";
        String rarity = filterRarity != null ? filterRarity.getValue() : "All";

        // Filter unlocked
        List<UserBadge> filteredUnlocked = allBadges.unlocked.stream()
            .filter(ub -> matchesFilters(ub, search, category, rarity))
            .collect(Collectors.toList());

        // Filter in progress
        List<UserBadge> filteredInProgress = allBadges.inProgress.stream()
            .filter(ub -> matchesFilters(ub, search, category, rarity))
            .collect(Collectors.toList());

        // Filter locked
        List<UserBadge> filteredLocked = allBadges.locked.stream()
            .filter(ub -> matchesFilters(ub, search, category, rarity))
            .collect(Collectors.toList());

        // Display filtered results
        BadgeService.BadgesDisplay filtered = new BadgeService.BadgesDisplay(
            filteredUnlocked, filteredInProgress, filteredLocked,
            allBadges.total, allBadges.unlockedCount, allBadges.percent
        );
        displayBadges(filtered);
    }

    private boolean matchesFilters(UserBadge ub, String search, String category, String rarity) {
        // Search filter
        if (!search.isEmpty()) {
            String name = ub.getBadge().getNom().toLowerCase();
            String desc = ub.getBadge().getDescription().toLowerCase();
            String cat = ub.getBadge().getCategorie().toLowerCase();
            if (!name.contains(search) && !desc.contains(search) && !cat.contains(search)) {
                return false;
            }
        }

        // Category filter
        if (!"All".equals(category)) {
            if (!category.equals(ub.getBadge().getCategorie())) {
                return false;
            }
        }

        // Rarity filter
        if (!"All".equals(rarity)) {
            String badgeRarity = ub.getBadge().getRarete();
            if (badgeRarity == null || !rarity.equalsIgnoreCase(badgeRarity)) {
                return false;
            }
        }

        return true;
    }

    private void displayBadges(BadgeService.BadgesDisplay display) {
        buildList(unlockedList, display.unlocked, "unlocked");
        buildList(inProgressList, display.inProgress, "inProgress");
        buildList(lockedList, display.locked, "locked");
    }

    private void buildList(VBox container, List<UserBadge> badges, String type) {
        if (container == null) return;
        container.getChildren().clear();

        if (badges.isEmpty()) {
            Label empty = new Label(switch (type) {
                case "unlocked" -> "No unlocked badges found";
                case "inProgress" -> "No badges in progress";
                default -> "No locked badges found";
            });
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-font-style:italic;-fx-padding:12;");
            container.getChildren().add(empty);
            return;
        }

        for (UserBadge ub : badges) {
            container.getChildren().add(buildBadgeCard(ub, type));
        }
    }

    private HBox buildBadgeCard(UserBadge ub, String type) {
        String couleur = nvl(ub.getBadge().getCouleur(), "#2E7D32");
        String couleurBg = nvl(ub.getBadge().getCouleurBg(), "#F0FDF4");
        boolean locked = "locked".equals(type);

        // Icon - Show actual icon even for locked badges, just with reduced opacity
        Label iconLbl = new Label(nvl(ub.getBadge().getSvg(), "🏅"));
        iconLbl.setStyle("-fx-font-size:28px;" + (locked ? "-fx-opacity:0.4;" : ""));
        StackPane iconBox = new StackPane(iconLbl);
        iconBox.setPrefSize(60, 60);
        iconBox.setStyle("-fx-background-color:" + (locked ? "#F8FAFC" : couleurBg) + ";" +
                "-fx-background-radius:30;-fx-border-color:" + (locked ? "#CBD5E1" : couleur) + ";" +
                "-fx-border-radius:30;-fx-border-width:" + (locked ? "1" : "2") + ";" +
                (locked ? "-fx-opacity:0.6;" : ""));

        // Name + Category - Show real name even for locked badges
        Label nameLbl = new Label(ub.getBadge().getNom());
        nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" +
                (locked ? "#94A3B8" : "#1E293B") + ";");

        Label categoryLbl = new Label("📁 " + ub.getBadge().getCategorie());
        categoryLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#64748B;");

        Label rarityLbl = new Label(nvl(ub.getBadge().getRarete(), "common").toUpperCase());
        rarityLbl.setStyle(getRarityStyle(ub.getBadge().getRarete()));
        if (locked) {
            rarityLbl.setOpacity(0.5);
        }

        HBox nameRow = new HBox(8, nameLbl, rarityLbl);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Description - Show condition for locked, description for others
        String desc = locked ? "🔓 " + nvl(ub.getBadge().getConditionText(), "Complete the challenge to unlock")
                : nvl(ub.getBadge().getDescription(), "");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + (locked ? "#64748B" : "#64748B") + ";" +
                (locked ? "-fx-font-style:italic;" : ""));
        descLbl.setWrapText(true);
        descLbl.setMaxWidth(400);

        VBox info = new VBox(4, nameRow, categoryLbl, descLbl);

        // Progress for in-progress badges
        if ("inProgress".equals(type) && ub.getCurrentValue() > 0) {
            int max = ub.getBadge().getConditionValue();
            int cur = ub.getCurrentValue();
            double pct = max > 0 ? (double) cur / max : 0;

            ProgressBar pb = new ProgressBar(pct);
            pb.setPrefWidth(250);
            pb.setStyle("-fx-accent:" + couleur + ";");

            Label pctLbl = new Label(cur + " / " + max + "  ·  " + (int)(pct * 100) + "%");
            pctLbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:" + couleur + ";");

            info.getChildren().addAll(pb, pctLbl);
        }

        // Unlock date for unlocked badges
        if (ub.isUnlocked() && ub.getUnlockedAt() != null) {
            Label dateLbl = new Label("✅ Unlocked: " + ub.getUnlockedAt().format(FMT));
            dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#16A34A;-fx-font-weight:bold;");
            info.getChildren().add(dateLbl);
        }

        // Locked indicator
        if (locked) {
            Label lockedLbl = new Label("🔒 Locked");
            lockedLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;-fx-font-weight:bold;" +
                    "-fx-background-color:#F1F5F9;-fx-background-radius:6;-fx-padding:2 8;");
            info.getChildren().add(lockedLbl);
        }

        // Vitrine indicator
        if (ub.isVitrine()) {
            Label vitrineLbl = new Label("⭐ Pinned to Showcase");
            vitrineLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#D97706;-fx-font-weight:bold;" +
                    "-fx-background-color:#FEF3C7;-fx-background-radius:6;-fx-padding:2 8;");
            info.getChildren().add(vitrineLbl);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox card = new HBox(16, iconBox, info, spacer);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 18;" +
                "-fx-border-color:" + (ub.isUnlocked() ? couleur : "#E2E8F0") + ";" +
                "-fx-border-radius:12;-fx-border-width:" + (ub.isUnlocked() ? "2" : "1") + ";" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);" +
                (locked ? "-fx-opacity:0.85;" : ""));

        return card;
    }

    private String getRarityStyle(String rarete) {
        return switch (nvl(rarete, "common").toLowerCase()) {
            case "legendary" -> "-fx-background-color:#FEF3C7;-fx-text-fill:#D97706;-fx-font-size:9px;" +
                    "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 8;";
            case "epic" -> "-fx-background-color:#EDE9FE;-fx-text-fill:#7C3AED;-fx-font-size:9px;" +
                    "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 8;";
            case "rare" -> "-fx-background-color:#DBEAFE;-fx-text-fill:#1D4ED8;-fx-font-size:9px;" +
                    "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 8;";
            default -> "-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;-fx-font-size:9px;" +
                    "-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 8;";
        };
    }

    @FXML
    private void handleExportReport() {
        // TODO: Export badge report to PDF or CSV
        Stage owner = (Stage) lblUserName.getScene().getWindow();
        Toasts.show(owner, "Badge report export feature coming soon!", Toasts.Type.INFO);
    }

    @FXML
    private void handleRefresh() {
        loadBadges();
        Stage owner = (Stage) lblUserName.getScene().getWindow();
        Toasts.show(owner, "Badges refreshed successfully!", Toasts.Type.SUCCESS);
    }

    @FXML
    private void handleClose() {
        ((Stage) lblUserName.getScene().getWindow()).close();
    }

    private void set(Label lbl, String val) {
        if (lbl != null) lbl.setText(val != null ? val : "—");
    }

    private String nvl(String s, String def) {
        return (s != null && !s.isBlank()) ? s : def;
    }
}
