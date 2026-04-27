package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML private ImageView photoView;
    @FXML private Label     lblFullName;
    @FXML private Label     lblEmail;
    @FXML private Label     lblBirthday;
    @FXML private Label     lblAge;
    @FXML private Label     lblWeight;
    @FXML private Label     lblHeight;
    @FXML private Label     lblBmi;
    @FXML private Label     lblWelcomeMessage;
    @FXML private Label     lblStatus;
    @FXML private Label     lblMemberSince;
    @FXML private Label     lblFaceId;
    @FXML private Label     lblRole;
    @FXML private Label     lblAvatarInitials;
    @FXML private Label     lblRankBadge;
    // Rank / XP card
    @FXML private javafx.scene.layout.VBox rankCard;
    @FXML private Label     lblRankEmoji;
    @FXML private Label     lblRankTitle;
    @FXML private Label     lblRankRarity;
    @FXML private Label     lblRankMotivation;
    @FXML private Label     lblTotalXP;
    @FXML private Label     lblXpProgress;
    @FXML private Label     lblNextRank;
    @FXML private javafx.scene.control.ProgressBar xpBar;
    @FXML private Label     lblXpToNext;
    // Health card
    @FXML private javafx.scene.layout.VBox healthCard;
    @FXML private Label     lblHealthGoal;
    @FXML private javafx.scene.control.ProgressBar healthProgress;
    @FXML private Label     lblHealthTip;

    // Buttons to hide for admins
    @FXML private Button btnMyBadges;
    @FXML private Button btnWeightGoal;
    // Gallery access toggle
    @FXML private CheckBox chkGalleryAccess;
    @FXML private Label    lblGalleryAccessStatus;

    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DTFULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository repo = new UserRepository();
    private final tn.esprit.projet.services.NutritionService nutritionService = new tn.esprit.projet.services.NutritionService();
    private final tn.esprit.projet.services.RankService rankService = new tn.esprit.projet.services.RankService();

    @FXML
    public void initialize() {
        if (!Session.isLoggedIn()) {
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
            return;
        }
        loadProfile();
    }

    public void loadProfile() {
        // Refresh from DB
        User u = repo.findById(Session.getCurrentUser().getId());
        if (u != null) Session.login(u);
        u = Session.getCurrentUser();

        set(lblFullName, u.getFullName());
        set(lblEmail, u.getEmail());
        set(lblBirthday, u.getBirthday() != null ? u.getBirthday().format(D_FMT) : "—");
        set(lblAge, u.getAge() > 0 ? u.getAge() + " years old" : "—");
        set(lblWeight, u.getWeight() > 0 ? u.getWeight() + " kg" : "—");
        set(lblHeight, u.getHeight() > 0 ? u.getHeight() + " cm" : "—");

        if (u.getWeight() > 0 && u.getHeight() > 0) {
            set(lblBmi, String.format("%.2f (%s)", u.getBmi(), u.getBmiCategory()));
        } else {
            set(lblBmi, "—");
        }

        set(lblWelcomeMessage, u.getWelcomeMessage() != null && !u.getWelcomeMessage().isBlank()
                ? u.getWelcomeMessage() : "");

        if (lblStatus != null) {
            lblStatus.setText(u.isActive() ? "Active" : "Inactive");
            lblStatus.setStyle(u.isActive()
                    ? "-fx-text-fill:#16A34A;-fx-font-weight:bold;"
                    : "-fx-text-fill:#DC2626;-fx-font-weight:bold;");
        }

        set(lblMemberSince, u.getCreatedAt() != null ? u.getCreatedAt().format(DT_FMT) : "—");

        if (lblFaceId != null) {
            if (u.hasFaceId() && u.getFaceIdEnrolledAt() != null) {
                lblFaceId.setText("Enrolled (since " + u.getFaceIdEnrolledAt().format(DTFULL) + ")");
                lblFaceId.setStyle("-fx-text-fill:#16A34A;");
            } else {
                lblFaceId.setText("Not enrolled");
                lblFaceId.setStyle("-fx-text-fill:#64748B;");
            }
        }

        set(lblRole, u.isAdmin() ? "Administrator" : "User");

        // ── Hide Rank/XP and Badges for Admins ────────────────────────────────
        boolean isAdmin = u.isAdmin();
        
        if (rankCard != null) {
            rankCard.setVisible(!isAdmin);
            rankCard.setManaged(!isAdmin);
        }
        
        if (lblRankBadge != null) {
            lblRankBadge.setVisible(!isAdmin);
            lblRankBadge.setManaged(!isAdmin);
        }

        // Hide badges and weight goal buttons for admins
        if (btnMyBadges != null) {
            btnMyBadges.setVisible(!isAdmin);
            btnMyBadges.setManaged(!isAdmin);
        }
        
        if (btnWeightGoal != null) {
            btnWeightGoal.setVisible(!isAdmin);
            btnWeightGoal.setManaged(!isAdmin);
        }

        // Only load rank/badges for non-admin users
        if (!isAdmin) {
            loadRankCard(u.getId());
        }

        // ── Health objective card (show for all users) ─────────────────────────
        if (u.getWeight() > 0 && u.getHeight() > 0) {
            tn.esprit.projet.services.NutritionService.HealthAnalysis a = nutritionService.analyse(u);
            if (a != null && healthCard != null) {
                healthCard.setVisible(true); healthCard.setManaged(true);

                // Goal text
                String goalText;
                if (a.kgToLose > 0) {
                    goalText = a.progressEmoji + "  " + a.motivationTitle + "\n"
                            + String.format("Current BMI: %.1f (%s)  •  Goal: lose %.1f kg  •  Ideal weight: %.0f–%.0f kg",
                            a.bmi, a.bmiCategory, a.kgToLose, a.idealWeightMin, a.idealWeightMax);
                } else if (a.kgToLose < 0) {
                    goalText = a.progressEmoji + "  " + a.motivationTitle + "\n"
                            + String.format("Current BMI: %.1f (%s)  •  Goal: gain %.1f kg  •  Ideal weight: %.0f–%.0f kg",
                            a.bmi, a.bmiCategory, Math.abs(a.kgToLose), a.idealWeightMin, a.idealWeightMax);
                } else {
                    goalText = "✅  " + a.motivationTitle + "\n"
                            + String.format("BMI: %.1f (%s)  •  You are in the ideal range!", a.bmi, a.bmiCategory);
                }
                set(lblHealthGoal, goalText);

                // Progress bar: 0 = obese, 1 = ideal
                double progress = a.bmi >= 18.5 && a.bmi < 25.0 ? 1.0
                        : a.bmi < 18.5 ? a.bmi / 18.5
                        : Math.max(0, 1.0 - (a.bmi - 25.0) / 15.0);
                if (healthProgress != null) {
                    healthProgress.setProgress(progress);
                    healthProgress.setStyle("-fx-accent:" + a.bmiColor + ";-fx-background-color:#E8F5E9;-fx-background-radius:4;");
                }

                // Daily tip
                String tip = nutritionService.getDailyTip(u);
                set(lblHealthTip, tip != null ? tip : "");
            }
        } else if (healthCard != null) {
            healthCard.setVisible(true); healthCard.setManaged(true);
            set(lblHealthGoal, "📝  Complete your profile (weight + height) to see your personalized health goal.");
            set(lblHealthTip, "Click 'Edit Profile' to add your measurements.");
        }

        // Avatar
        if (lblAvatarInitials != null)
            lblAvatarInitials.setText(u.getFirstName() != null && !u.getFirstName().isEmpty()
                    ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?");

        if (photoView != null) {
            if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
                File f = new File("uploads/profiles/" + u.getPhotoFilename());
                if (f.exists()) {
                    photoView.setImage(new Image(f.toURI().toString()));
                    if (lblAvatarInitials != null) lblAvatarInitials.setVisible(false);
                }
            }
        }

        // Gallery access toggle
        if (chkGalleryAccess != null) {
            chkGalleryAccess.setSelected(u.isGalleryAccessEnabled());
            updateGalleryAccessLabel(u.isGalleryAccessEnabled());
        }
    }

    private void loadRankCard(int userId) {
        tn.esprit.projet.services.RankService.RankInfo info = rankService.getRankInfo(userId);
        tn.esprit.projet.services.RankService.Rank rank = info.currentRank;

        // Rank badge in name row
        if (lblRankBadge != null) {
            lblRankBadge.setText(rank.emoji + " " + rank.title);
            lblRankBadge.setStyle(
                "-fx-background-color:" + rank.bgColor + ";" +
                "-fx-text-fill:" + rank.color + ";" +
                "-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;");
        }

        // Rank card
        if (rankCard != null) {
            // Update border color to match rank
            rankCard.setStyle(
                "-fx-background-color:white;-fx-background-radius:14;-fx-padding:16;" +
                "-fx-border-color:" + rank.color + ";-fx-border-radius:14;-fx-border-width:2;" +
                "-fx-effect:dropshadow(gaussian," + rank.color + ",8,0.15,0,0);");
        }

        set(lblRankEmoji,      rank.emoji);
        set(lblRankTitle,      rank.title);
        set(lblRankMotivation, rank.motivation);
        set(lblTotalXP,        info.totalXP + " XP");

        // Rarity label style
        String rarityLabel = info.totalXP >= 1000 ? "LEGENDARY" :
                             info.totalXP >= 600  ? "EPIC" :
                             info.totalXP >= 300  ? "RARE" : "COMMON";
        String rarityStyle = switch (rarityLabel) {
            case "LEGENDARY" -> "-fx-background-color:#FEF3C7;-fx-text-fill:#D97706;";
            case "EPIC"      -> "-fx-background-color:#EDE9FE;-fx-text-fill:#7C3AED;";
            case "RARE"      -> "-fx-background-color:#DBEAFE;-fx-text-fill:#1D4ED8;";
            default          -> "-fx-background-color:#F1F5F9;-fx-text-fill:#64748B;";
        };
        if (lblRankRarity != null) {
            lblRankRarity.setText(rarityLabel);
            lblRankRarity.setStyle(rarityStyle +
                "-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:2 8;");
        }

        // XP bar
        if (xpBar != null) {
            xpBar.setProgress(info.progress);
            xpBar.setStyle("-fx-accent:" + rank.color + ";-fx-background-color:#F1F5F9;-fx-background-radius:5;");
        }

        if (info.nextRank != null) {
            set(lblXpProgress, info.xpInLevel + " / " + info.xpNeededForNext + " XP");
            set(lblNextRank,   "Next: " + info.nextRank.emoji + " " + info.nextRank.title);
            set(lblXpToNext,   "💡 " + info.xpToNext + " more XP to reach " + info.nextRank.title +
                    " — earn badges to gain XP!");
        } else {
            set(lblXpProgress, "MAX RANK");
            set(lblNextRank,   "");
            set(lblXpToNext,   "👑 You've reached the highest rank — Legend!");
        }
    }

    @FXML private void handleEditProfile() {
        openModal("edit_profile.fxml", "Edit Profile", 560, 640);
    }

    @FXML private void handleChangePassword() {
        openModal("change_password.fxml", "Change Password", 460, 360);
    }

    @FXML private void handleManageFaceId() {
        openModal("face_id_management.fxml", "Manage Face ID", 480, 360);
    }

    @FXML private void handleWelcomeMessage() {
        openModal("welcome_message.fxml", "Welcome Message", 480, 340);
    }

    @FXML private void handleMyBadges() {
        openModal("badges.fxml", "My Badges", 800, 760);
    }

    @FXML private void handleWeightObjective() {
        openModal("weight_objective.fxml", "My Goal & Progress", 700, 750);
    }

    @FXML
    private void handleGalleryAccessToggle() {
        if (chkGalleryAccess == null) return;
        boolean enabled = chkGalleryAccess.isSelected();
        User u = Session.getCurrentUser();
        repo.setGalleryAccess(u.getId(), enabled);
        u.setGalleryAccessEnabled(enabled);
        Session.login(u);
        updateGalleryAccessLabel(enabled);
    }

    private void updateGalleryAccessLabel(boolean enabled) {
        if (lblGalleryAccessStatus == null) return;
        if (enabled) {
            lblGalleryAccessStatus.setText("🔓 Admin can view your weight progress photos.");
            lblGalleryAccessStatus.setStyle(
                "-fx-font-size:11px;-fx-text-fill:#16A34A;" +
                "-fx-background-color:#DCFCE7;-fx-background-radius:8;-fx-padding:8 12;");
        } else {
            lblGalleryAccessStatus.setText("🔒 Your photos are private — only you can see them.");
            lblGalleryAccessStatus.setStyle(
                "-fx-font-size:11px;-fx-text-fill:#64748B;" +
                "-fx-background-color:#F8FAFC;-fx-background-radius:8;-fx-padding:8 12;");
        }
    }

    @FXML private void handleDeactivate() {
        boolean confirmed = AlertUtil.confirm("Deactivate Account",
            "Are you sure you want to deactivate your account?\n\n" +
            "You will be logged out immediately and will not be able to log in until an administrator reactivates your account.");
        if (confirmed) {
            repo.setActive(Session.getCurrentUser().getId(), false);
            Session.logout();
            Stage stage = (Stage) lblFullName.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
        }
    }

    private void openModal(String fxml, String title, int w, int h) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, w, h));
            stage.setResizable(false);
            stage.showAndWait();
            loadProfile();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val != null ? val : "—"); }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) lblFullName.getScene().getWindow();
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
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:5 15;-fx-background-radius:5;");
        }
    }
}
