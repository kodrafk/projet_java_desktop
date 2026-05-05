package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.models.WeightLog;
import tn.esprit.projet.models.WeightObjective;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.repository.WeightRepository;
import tn.esprit.projet.services.NutritionService;
import tn.esprit.projet.services.RankService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUserProgressController {

    @FXML private Label lblUserName;
    @FXML private Label lblUserEmail;
    @FXML private Label lblCurrentWeight;
    @FXML private Label lblHeight;
    @FXML private Label lblBmi;
    @FXML private Label lblBmiCategory;
    @FXML private Label lblAge;
    @FXML private VBox goalCard;
    @FXML private Label lblGoalText;
    @FXML private ProgressBar goalProgress;
    @FXML private Label lblGoalTip;
    @FXML private LineChart<String, Number> weightChart;
    @FXML private Label lblWeightLogs;
    @FXML private Label lblNoData;
    @FXML private Label lblBadgeCount;
    @FXML private Label lblUnlockedBadges;
    @FXML private Label lblTotalXP;
    @FXML private Label lblRankEmoji;
    @FXML private Label lblRankTitle;

    private User user;
    private final NutritionService nutritionService = new NutritionService();
    private final RankService rankService = new RankService();
    private final BadgeRepository badgeRepo = new BadgeRepository();
    private final WeightRepository weightRepo = new WeightRepository();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setUser(User u) {
        this.user = u;
        if (user != null) {
            loadProgress();
        }
    }

    private void loadProgress() {
        if (user == null) return;

        // Header
        lblUserName.setText(user.getFullName() + "'s Progress");
        lblUserEmail.setText(user.getEmail());

        // Stats cards
        set(lblCurrentWeight, user.getWeight() > 0 ? String.format("%.1f", user.getWeight()) : "—");
        set(lblHeight, user.getHeight() > 0 ? String.format("%.0f", user.getHeight()) : "—");
        set(lblAge, user.getAge() > 0 ? String.valueOf(user.getAge()) : "—");

        if (user.getWeight() > 0 && user.getHeight() > 0) {
            set(lblBmi, String.format("%.1f", user.getBmi()));
            set(lblBmiCategory, user.getBmiCategory());
            
            // Color code BMI category
            String bmiColor = switch (user.getBmiCategory()) {
                case "Normal weight" -> "#16A34A";
                case "Underweight" -> "#0891B2";
                case "Overweight" -> "#F59E0B";
                case "Obese" -> "#DC2626";
                default -> "#64748B";
            };
            lblBmiCategory.setStyle("-fx-font-size:11px;-fx-text-fill:" + bmiColor + ";-fx-font-weight:bold;");
        } else {
            set(lblBmi, "—");
            set(lblBmiCategory, "—");
        }

        // Health goal
        WeightObjective activeGoal = weightRepo.findObjectiveByUser(user.getId());
        
        if (activeGoal != null && activeGoal.isActive()) {
            // Afficher l'objectif actif du front office
            goalCard.setVisible(true);
            goalCard.setManaged(true);
            
            double currentWeight = user.getWeight();
            double targetWeight = activeGoal.getTargetWeight();
            double difference = Math.abs(currentWeight - targetWeight);
            double progress = 0.0;
            
            String goalText;
            if (currentWeight > targetWeight) {
                // Objectif de perte de poids
                goalText = String.format("🎯  Weight Loss Goal\nCurrent: %.1f kg  →  Target: %.1f kg  •  %.1f kg to lose\nDeadline: %s",
                        currentWeight, targetWeight, difference, activeGoal.getTargetDate().format(DATE_FMT));
                progress = Math.max(0, Math.min(1.0, 1.0 - (difference / (currentWeight * 0.2))));
            } else if (currentWeight < targetWeight) {
                // Objectif de gain de poids
                goalText = String.format("🎯  Weight Gain Goal\nCurrent: %.1f kg  →  Target: %.1f kg  •  %.1f kg to gain\nDeadline: %s",
                        currentWeight, targetWeight, difference, activeGoal.getTargetDate().format(DATE_FMT));
                progress = Math.max(0, Math.min(1.0, 1.0 - (difference / (targetWeight * 0.2))));
            } else {
                // Objectif atteint
                goalText = String.format("✅  Goal Achieved!\nCurrent: %.1f kg  =  Target: %.1f kg\nCompleted on: %s",
                        currentWeight, targetWeight, activeGoal.getTargetDate().format(DATE_FMT));
                progress = 1.0;
            }
            
            set(lblGoalText, goalText);
            goalProgress.setProgress(progress);
            
            String progressColor = progress >= 0.7 ? "#16A34A" : progress >= 0.4 ? "#F59E0B" : "#DC2626";
            goalProgress.setStyle("-fx-accent:" + progressColor + ";-fx-background-color:#E8F5E9;-fx-background-radius:5;");
            
            String tip = nutritionService.getDailyTip(user);
            set(lblGoalTip, tip != null ? tip : "Keep going! You're making progress towards your goal.");
            
        } else if (user.getWeight() > 0 && user.getHeight() > 0) {
            // Pas d'objectif actif, mais afficher l'analyse BMI
            NutritionService.HealthAnalysis analysis = nutritionService.analyse(user);
            if (analysis != null) {
                goalCard.setVisible(true);
                goalCard.setManaged(true);

                String goalText;
                if (analysis.kgToLose > 0) {
                    goalText = analysis.progressEmoji + "  " + analysis.motivationTitle + "\n" +
                            String.format("Current BMI: %.1f (%s)  •  Suggested: lose %.1f kg  •  Ideal weight: %.0f–%.0f kg",
                                    analysis.bmi, analysis.bmiCategory, analysis.kgToLose, 
                                    analysis.idealWeightMin, analysis.idealWeightMax);
                } else if (analysis.kgToLose < 0) {
                    goalText = analysis.progressEmoji + "  " + analysis.motivationTitle + "\n" +
                            String.format("Current BMI: %.1f (%s)  •  Suggested: gain %.1f kg  •  Ideal weight: %.0f–%.0f kg",
                                    analysis.bmi, analysis.bmiCategory, Math.abs(analysis.kgToLose),
                                    analysis.idealWeightMin, analysis.idealWeightMax);
                } else {
                    goalText = "✅  " + analysis.motivationTitle + "\n" +
                            String.format("BMI: %.1f (%s)  •  Already in ideal range!", analysis.bmi, analysis.bmiCategory);
                }
                set(lblGoalText, goalText);

                double progress = analysis.bmi >= 18.5 && analysis.bmi < 25.0 ? 1.0
                        : analysis.bmi < 18.5 ? analysis.bmi / 18.5
                        : Math.max(0, 1.0 - (analysis.bmi - 25.0) / 15.0);
                goalProgress.setProgress(progress);
                goalProgress.setStyle("-fx-accent:" + analysis.bmiColor + ";-fx-background-color:#E8F5E9;-fx-background-radius:5;");

                String tip = nutritionService.getDailyTip(user);
                set(lblGoalTip, tip != null ? tip : "");
            }
        } else {
            goalCard.setVisible(true);
            goalCard.setManaged(true);
            set(lblGoalText, "📝  No active goal. User can set a weight goal from the mobile app.");
            set(lblGoalTip, "User needs to complete their profile (weight + height) first.");
        }

        // Weight chart - Show real data from front office
        List<WeightLog> weightLogs = weightRepo.findLogsByUser(user.getId());
        
        if (weightLogs != null && !weightLogs.isEmpty()) {
            lblWeightLogs.setText(weightLogs.size() + " log" + (weightLogs.size() > 1 ? "s" : ""));
            lblNoData.setVisible(false);
            lblNoData.setManaged(false);
            weightChart.setVisible(true);
            weightChart.setManaged(true);

            // Main weight series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Weight (kg)");

            double minW = Double.MAX_VALUE, maxW = Double.MIN_VALUE;
            for (WeightLog log : weightLogs) {
                if (log.getLoggedAt() != null) {
                    String dateStr = log.getLoggedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
                    series.getData().add(new XYChart.Data<>(dateStr, log.getWeight()));
                    if (log.getWeight() < minW) minW = log.getWeight();
                    if (log.getWeight() > maxW) maxW = log.getWeight();
                }
            }

            weightChart.getData().clear();
            weightChart.getData().add(series);
            weightChart.setLegendVisible(false);
            weightChart.setAnimated(false);
            weightChart.setCreateSymbols(true);

            // Style the chart cleanly
            weightChart.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-plot-background-color:#F8FAFC;" +
                "-fx-horizontal-grid-lines-visible:true;" +
                "-fx-vertical-grid-lines-visible:false;");

            // Style the line and dots after layout
            javafx.application.Platform.runLater(() -> {
                // Line color
                javafx.scene.Node line = weightChart.lookup(".chart-series-line");
                if (line != null)
                    line.setStyle("-fx-stroke:#16A34A;-fx-stroke-width:2.5px;");
                // Dots
                for (javafx.scene.Node dot : weightChart.lookupAll(".chart-line-symbol"))
                    dot.setStyle("-fx-background-color:#16A34A,white;-fx-background-radius:5;-fx-padding:5;");
            });

            // Show min/max summary below chart
            if (minW != Double.MAX_VALUE) {
                double firstW = weightLogs.get(0).getWeight();
                double lastW  = weightLogs.get(weightLogs.size() - 1).getWeight();
                double diff   = lastW - firstW;
                String trend  = diff < 0 ? String.format("📉 %.1f kg lost", Math.abs(diff))
                              : diff > 0 ? String.format("📈 +%.1f kg gained", diff)
                              : "➡️ No change";
                String summary = String.format("Min: %.1f kg  •  Max: %.1f kg  •  %s", minW, maxW, trend);
                lblWeightLogs.setText(weightLogs.size() + " log" + (weightLogs.size() > 1 ? "s" : "") + "  •  " + summary);
            }

        } else {
            lblWeightLogs.setText("0 logs");
            lblNoData.setVisible(true);
            lblNoData.setManaged(true);
            lblNoData.setText("📝 No weight logs yet. User can track weight from the app.");
            weightChart.setVisible(false);
            weightChart.setManaged(false);
        }

        // Badges & XP
        loadBadgesProgress();
    }

    private void loadBadgesProgress() {
        List<UserBadge> badges = badgeRepo.findByUser(user.getId());
        long unlocked = badges.stream().filter(UserBadge::isUnlocked).count();
        
        set(lblBadgeCount, unlocked + " / " + badges.size());
        set(lblUnlockedBadges, String.valueOf(unlocked));

        // Get rank info
        RankService.RankInfo rankInfo = rankService.getRankInfo(user.getId());
        set(lblTotalXP, String.valueOf(rankInfo.totalXP));
        set(lblRankEmoji, rankInfo.currentRank.emoji);
        set(lblRankTitle, rankInfo.currentRank.title);
    }

    private void set(Label lbl, String val) {
        if (lbl != null) lbl.setText(val != null ? val : "—");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblUserName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCloseHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color:#DC2626;-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 15;-fx-background-radius:5;");
        }
    }

    @FXML
    private void handleCloseExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Button btn) {
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#64748B;-fx-font-size:20px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 15;-fx-background-radius:5;");
        }
    }
}
