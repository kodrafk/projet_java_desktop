package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.projet.services.EthicalPointsManager;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.MealPlanService;
import tn.esprit.projet.services.RecetteService;
import tn.esprit.projet.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class KitchenRankController {

    // HEADER
    @FXML private Label lblLevelIcon;
    @FXML private Label lblCurrentLevel;
    @FXML private Label lblLevelDescription;
    @FXML private Label lblTotalPoints;

    // PROGRESS
    @FXML private ProgressBar progressLevel;
    @FXML private Label lblProgressLabel;
    @FXML private Label lblProgressPoints;
    @FXML private Label lblNextLevel;

    // LEVEL BOXES
    @FXML private VBox level1Box;
    @FXML private VBox level2Box;
    @FXML private VBox level3Box;

    // LEVEL STATUS
    @FXML private Label lblLevel1Status;
    @FXML private Label lblLevel2Status;
    @FXML private Label lblLevel3Status;

    // GRIDS
    @FXML private GridPane gridLevel1;
    @FXML private GridPane gridLevel2;
    @FXML private GridPane gridLevel3;

    // SERVICES
    private RecetteService recetteService;
    private IngredientService ingredientService;
    private MealPlanService mealPlanService;

    @FXML
    public void initialize() {
        recetteService = new RecetteService();
        ingredientService = new IngredientService();
        mealPlanService = new MealPlanService();

        loadCurrentLevel();
        loadAllBadges();
    }

    // ═══════════════════════════════════
    // CURRENT LEVEL
    // ═══════════════════════════════════
    private void loadCurrentLevel() {
        int totalPoints = EthicalPointsManager.getTotalPoints();
        int currentLevel = EthicalPointsManager.getCurrentLevel();
        boolean level1Complete = EthicalPointsManager.isLevel1Complete();
        boolean level2Complete = EthicalPointsManager.isLevel2Complete();

        lblTotalPoints.setText(totalPoints + " pts");

        if (currentLevel == 1) {
            lblLevelIcon.setText("🌱");
            lblCurrentLevel.setText("Level 1 — Beginner");
            lblLevelDescription.setText("Complete all 3 Level 1 badges to unlock Level 2!");
            lblNextLevel.setText("Next: 🌿 Level 2 — Ethical Cook");

            // Progression sur badges Level 1
            long unlockedL1 = countUnlockedLevel1();
            double progress = unlockedL1 / 3.0;
            progressLevel.setProgress(progress);
            lblProgressPoints.setText(unlockedL1 + " / 3 badges completed");

        } else if (currentLevel == 2) {
            lblLevelIcon.setText("🌿");
            lblCurrentLevel.setText("Level 2 — Ethical Cook");
            lblLevelDescription.setText("Complete all 3 Level 2 badges to unlock Level 3!");
            lblNextLevel.setText("Next: 👑 Level 3 — Ethical Legend");

            long unlockedL2 = countUnlockedLevel2();
            double progress = unlockedL2 / 3.0;
            progressLevel.setProgress(progress);
            lblProgressPoints.setText(unlockedL2 + " / 3 badges completed");

        } else {
            lblLevelIcon.setText("👑");
            lblCurrentLevel.setText("Level 3 — Ethical Legend");
            lblLevelDescription.setText("You've reached the highest rank! You're an Ethical Legend!");
            lblNextLevel.setText("🎉 Maximum level reached!");

            long unlockedL3 = countUnlockedLevel3();
            progressLevel.setProgress(unlockedL3 / 3.0);
            lblProgressPoints.setText(unlockedL3 + " / 3 badges completed");
        }

        updateLevelStatuses(currentLevel);
    }

    private long countUnlockedLevel1() {
        long count = 0;
        if (EthicalPointsManager.isBadgeUnlocked(101)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(102)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(103)) count++;
        return count;
    }

    private long countUnlockedLevel2() {
        long count = 0;
        if (EthicalPointsManager.isBadgeUnlocked(104)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(105)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(106)) count++;
        return count;
    }

    private long countUnlockedLevel3() {
        long count = 0;
        if (EthicalPointsManager.isBadgeUnlocked(107)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(108)) count++;
        if (EthicalPointsManager.isBadgeUnlocked(109)) count++;
        return count;
    }
    private int getCurrentLevel(int points) {
        return EthicalPointsManager.getCurrentLevel();
    }

    private void updateLevelStatuses(int currentLevel) {
        // Level 1 — toujours accessible
        lblLevel1Status.setText(currentLevel == 1 ? "🔓 Current" : "✅ Completed");
        lblLevel1Status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27AE60; -fx-background-color: #DCFCE7; -fx-background-radius: 6; -fx-padding: 3 8;");
        level1Box.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: " +
                (currentLevel == 1 ? "#2ECC71" : "#A7F3D0") + "; -fx-border-radius: 14; -fx-border-width: " +
                (currentLevel == 1 ? "2" : "1") + "; -fx-padding: 20;");

        // Level 2 — accessible seulement si Level 1 complet
        if (currentLevel >= 2) {
            lblLevel2Status.setText(currentLevel == 2 ? "🔓 Current" : "✅ Completed");
            lblLevel2Status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27AE60; -fx-background-color: #DCFCE7; -fx-background-radius: 6; -fx-padding: 3 8;");
            level2Box.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: " +
                    (currentLevel == 2 ? "#2ECC71" : "#A7F3D0") + "; -fx-border-radius: 14; -fx-border-width: " +
                    (currentLevel == 2 ? "2" : "1") + "; -fx-padding: 20;");
        } else {
            lblLevel2Status.setText("🔒 Complete Level 1 first");
            lblLevel2Status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B; -fx-background-color: #F1F5F9; -fx-background-radius: 6; -fx-padding: 3 8;");
            level2Box.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-padding: 20; -fx-opacity: 0.5;");
        }

        // Level 3 — accessible seulement si Level 2 complet
        if (currentLevel >= 3) {
            lblLevel3Status.setText("🔓 Current");
            lblLevel3Status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27AE60; -fx-background-color: #DCFCE7; -fx-background-radius: 6; -fx-padding: 3 8;");
            level3Box.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #2ECC71; -fx-border-radius: 14; -fx-border-width: 2; -fx-padding: 20;");
        } else {
            lblLevel3Status.setText("🔒 Complete Level 2 first");
            lblLevel3Status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B; -fx-background-color: #F1F5F9; -fx-background-radius: 6; -fx-padding: 3 8;");
            level3Box.setStyle("-fx-background-color: #FAFAFA; -fx-background-radius: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-padding: 20; -fx-opacity: 0.5;");
        }
    }

    // ═══════════════════════════════════
    // ALL BADGES
    // ═══════════════════════════════════
    private void loadAllBadges() {
        int totalPoints = EthicalPointsManager.getTotalPoints();
        int currentLevel = getCurrentLevel(totalPoints);
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        int recipeCount = recetteService.countTotal();
        int scanCount = EthicalPointsManager.getScanCount();
        int boycottCount = EthicalPointsManager.getBoycottRejectCount();
        int completedPlans = EthicalPointsManager.getCompletedMealPlanCount();
        int ingredientCount = ingredientService.getAll().size();

        // ═══ LEVEL 1 BADGES ═══
        List<BadgeInfo> level1Badges = new ArrayList<>();
        level1Badges.add(new BadgeInfo("First Recipe", "Create your first recipe", "first_recipe.png",
                recipeCount >= 1, recipeCount, 1));
        level1Badges.add(new BadgeInfo("First Scan", "Scan your first product", "first_scan.png",
                scanCount >= 1, scanCount, 1));
        level1Badges.add(new BadgeInfo("Boycott Hero", "Reject 3 boycotted products", "boycott_hero.png",
                boycottCount >= 3, boycottCount, 3));

        fillGrid(gridLevel1, level1Badges, true);

        // ═══ LEVEL 2 BADGES ═══
        boolean level2Unlocked = currentLevel >= 2;
        List<BadgeInfo> level2Badges = new ArrayList<>();
        level2Badges.add(new BadgeInfo("Meal Plan Champion", "Complete a full meal plan", "meal_champ.png",
                level2Unlocked && completedPlans >= 1, completedPlans, 1));
        level2Badges.add(new BadgeInfo("Master Chef", "Create 20 recipes", "master_chef.png",
                level2Unlocked && recipeCount >= 20, recipeCount, 20));
        level2Badges.add(new BadgeInfo("Weekly Warrior", "Complete all 21 meals in a week", "weekly_warrior.png",
                level2Unlocked && completedPlans >= 1, completedPlans, 1));

        fillGrid(gridLevel2, level2Badges, level2Unlocked);

        // ═══ LEVEL 3 BADGES ═══
        boolean level3Unlocked = currentLevel >= 3;
        List<BadgeInfo> level3Badges = new ArrayList<>();
        level3Badges.add(new BadgeInfo("Ingredient Master", "Add 50 ingredients to your kitchen", "ingredient_master.png",
                level3Unlocked && ingredientCount >= 50, ingredientCount, 50));
        level3Badges.add(new BadgeInfo("Ethical Guardian", "Reject 10 boycotted products", "ethical_guardian.png",
                level3Unlocked && boycottCount >= 10, boycottCount, 10));
        level3Badges.add(new BadgeInfo("Legend Crown", "Reach 500 ethical points", "ethical_legend.png",
                level3Unlocked && totalPoints >= 500, totalPoints, 500));

        fillGrid(gridLevel3, level3Badges, level3Unlocked);
    }

    // ═══════════════════════════════════
    // BADGE CARD BUILDER
    // ═══════════════════════════════════
    private void fillGrid(GridPane grid, List<BadgeInfo> badges, boolean levelUnlocked) {
        grid.getChildren().clear();
        for (int i = 0; i < badges.size(); i++) {
            BadgeInfo badge = badges.get(i);
            VBox card = buildBadgeCard(badge, levelUnlocked);
            grid.add(card, i, 0);
        }
    }

    private VBox buildBadgeCard(BadgeInfo badge, boolean levelUnlocked) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);

        if (!levelUnlocked) {
            card.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-padding: 16; -fx-opacity: 0.5;");
        } else if (badge.unlocked) {
            card.setStyle("-fx-background-color: #F0FFF4; -fx-background-radius: 14; -fx-border-color: #2ECC71; -fx-border-radius: 14; -fx-border-width: 2; -fx-padding: 16;");
        } else {
            card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-padding: 16;");
        }

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(56, 56);
        imageContainer.setMaxSize(56, 56);

        try {
            var imgUrl = getClass().getResource("/images/" + badge.imageName);
            if (imgUrl != null) {
                ImageView iv = new ImageView(new Image(imgUrl.toExternalForm()));
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                if (!badge.unlocked || !levelUnlocked) iv.setStyle("-fx-opacity: 0.3;");
                imageContainer.getChildren().add(iv);
            }
        } catch (Exception e) {
            Label fallback = new Label(badge.unlocked && levelUnlocked ? "🏆" : "🔒");
            fallback.setStyle("-fx-font-size: 24px;");
            imageContainer.getChildren().add(fallback);
        }

        // Name
        Label nameLabel = new Label(badge.name);
        nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " +
                (badge.unlocked && levelUnlocked ? "#1E293B" : "#94A3B8") + ";");
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);

        // Description
        Label descLabel = new Label(badge.description);
        descLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8;");
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setWrapText(true);

        // Status
        String statusText;
        String statusStyle;

        if (!levelUnlocked) {
            statusText = "🔒 Level locked";
            statusStyle = "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-background-color: #F1F5F9; -fx-background-radius: 6; -fx-padding: 2 6;";
        } else if (badge.unlocked) {
            statusText = "✅ Unlocked";
            statusStyle = "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #27AE60; -fx-background-color: #DCFCE7; -fx-background-radius: 6; -fx-padding: 2 6;";
        } else {
            statusText = "🔒 " + badge.currentValue + " / " + badge.targetValue;
            statusStyle = "-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #64748B; -fx-background-color: #F1F5F9; -fx-background-radius: 6; -fx-padding: 2 6;";
        }

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle(statusStyle);

        card.getChildren().addAll(imageContainer, nameLabel, descLabel, statusLabel);
        return card;
    }

    // ═══════════════════════════════════
    // BADGE DATA CLASS
    // ═══════════════════════════════════
    private static class BadgeInfo {
        String name;
        String description;
        String imageName;
        boolean unlocked;
        int currentValue;
        int targetValue;

        BadgeInfo(String name, String description, String imageName,
                  boolean unlocked, int currentValue, int targetValue) {
            this.name = name;
            this.description = description;
            this.imageName = imageName;
            this.unlocked = unlocked;
            this.currentValue = currentValue;
            this.targetValue = targetValue;
        }
    }
}