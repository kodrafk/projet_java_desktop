package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.RankService;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUserProfilesController {

    @FXML private TextField searchField;
    @FXML private FlowPane userCardsContainer;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterActive;
    @FXML private Button btnFilterFaceId;
    @FXML private Button btnFilterBadges;

    private final UserRepository userRepo = new UserRepository();
    private final BadgeRepository badgeRepo = new BadgeRepository();
    private final RankService rankService = new RankService();
    private List<User> allUsers;
    private String currentFilter = "ALL";

    @FXML
    public void initialize() {
        loadUsers();
        
        // Search listener
        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers());
    }

    private void loadUsers() {
        allUsers = userRepo.findAll();
        displayUsers(allUsers);
    }

    private void displayUsers(List<User> users) {
        userCardsContainer.getChildren().clear();
        
        for (User user : users) {
            VBox card = createUserCard(user);
            userCardsContainer.getChildren().add(card);
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                     "-fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-padding: 18; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // Avatar + Name
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(56, 56);
        avatarPane.setStyle("-fx-background-color: #2E7D5A; -fx-background-radius: 28;");
        
        if (user.getPhotoFilename() != null && !user.getPhotoFilename().isBlank()) {
            File photoFile = new File("uploads/profiles/" + user.getPhotoFilename());
            if (photoFile.exists()) {
                ImageView photoView = new ImageView(new Image(photoFile.toURI().toString()));
                photoView.setFitWidth(56);
                photoView.setFitHeight(56);
                photoView.setPreserveRatio(true);
                photoView.setStyle("-fx-background-radius: 28;");
                Circle clip = new Circle(28, 28, 28);
                photoView.setClip(clip);
                avatarPane.getChildren().add(photoView);
            } else {
                addInitialLabel(avatarPane, user);
            }
        } else {
            addInitialLabel(avatarPane, user);
        }
        
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(user.getFullName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        nameBox.getChildren().addAll(nameLabel, emailLabel);
        
        header.getChildren().addAll(avatarPane, nameBox);
        
        // Status badges
        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label roleLabel = new Label(user.isAdmin() ? "ADMIN" : "USER");
        roleLabel.setStyle(user.isAdmin() 
            ? "-fx-background-color: #EDE9FE; -fx-text-fill: #7C3AED; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;"
            : "-fx-background-color: #DCFCE7; -fx-text-fill: #166534; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        Label statusLabel = new Label(user.isActive() ? "Active" : "Inactive");
        statusLabel.setStyle(user.isActive()
            ? "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;"
            : "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        statusBox.getChildren().addAll(roleLabel, statusLabel);
        
        if (user.hasFaceId()) {
            Label faceIdLabel = new Label("🎭 Face ID");
            faceIdLabel.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-background-radius: 6; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;");
            statusBox.getChildren().add(faceIdLabel);
        }
        
        // Rank & XP
        RankService.RankInfo rankInfo = rankService.getRankInfo(user.getId());
        HBox rankBox = new HBox(8);
        rankBox.setAlignment(Pos.CENTER_LEFT);
        rankBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 8;");
        
        Label rankEmoji = new Label(rankInfo.currentRank.emoji);
        rankEmoji.setStyle("-fx-font-size: 18px;");
        
        VBox rankInfo2 = new VBox(2);
        Label rankTitle = new Label(rankInfo.currentRank.title);
        rankTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label xpLabel = new Label(rankInfo.totalXP + " XP");
        xpLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");
        rankInfo2.getChildren().addAll(rankTitle, xpLabel);
        
        rankBox.getChildren().addAll(rankEmoji, rankInfo2);
        
        // Badges count
        List<tn.esprit.projet.models.UserBadge> userBadges = badgeRepo.findByUser(user.getId());
        int badgeCount = (int) userBadges.stream().filter(ub -> ub.isUnlocked()).count();
        Label badgesLabel = new Label("🏆 " + badgeCount + " Badge" + (badgeCount != 1 ? "s" : ""));
        badgesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        
        // Stats
        HBox statsBox = new HBox(12);
        statsBox.setAlignment(Pos.CENTER);
        
        VBox ageBox = createStatBox("Age", user.getAge() > 0 ? user.getAge() + " yrs" : "—");
        VBox bmiBox = createStatBox("BMI", user.getBmi() > 0 ? String.format("%.1f", user.getBmi()) : "—");
        VBox weightBox = createStatBox("Weight", user.getWeight() > 0 ? user.getWeight() + " kg" : "—");
        
        statsBox.getChildren().addAll(ageBox, bmiBox, weightBox);
        
        // Action button
        Button viewBtn = new Button("View Full Profile");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setPrefHeight(36);
        viewBtn.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openUserProfile(user));
        
        card.getChildren().addAll(header, statusBox, rankBox, badgesLabel, new Separator(), statsBox, viewBtn);
        
        return card;
    }

    private void addInitialLabel(StackPane pane, User user) {
        Label initial = new Label(user.getFirstName() != null && !user.getFirstName().isEmpty()
                ? String.valueOf(user.getFirstName().charAt(0)).toUpperCase() : "?");
        initial.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        pane.getChildren().add(initial);
    }

    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private void openUserProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_show.fxml"));
            Parent root = loader.load();
            AdminUserShowController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Profile - " + user.getFullName());
            stage.setScene(new Scene(root, 900, 720));
            stage.setResizable(false);
            stage.showAndWait();
            
            // Refresh after closing
            loadUsers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        resetFilters();
    }

    @FXML
    private void handleFilterAll() {
        currentFilter = "ALL";
        updateFilterButtons();
        filterUsers();
    }

    @FXML
    private void handleFilterActive() {
        currentFilter = "ACTIVE";
        updateFilterButtons();
        filterUsers();
    }

    @FXML
    private void handleFilterFaceId() {
        currentFilter = "FACEID";
        updateFilterButtons();
        filterUsers();
    }

    @FXML
    private void handleFilterBadges() {
        currentFilter = "BADGES";
        updateFilterButtons();
        filterUsers();
    }

    private void filterUsers() {
        String search = searchField.getText().toLowerCase().trim();
        
        List<User> filtered = allUsers.stream()
            .filter(u -> {
                // Search filter
                if (!search.isEmpty()) {
                    return u.getFullName().toLowerCase().contains(search) ||
                           u.getEmail().toLowerCase().contains(search);
                }
                return true;
            })
            .filter(u -> {
                // Category filter
                switch (currentFilter) {
                    case "ACTIVE": return u.isActive();
                    case "FACEID": return u.hasFaceId();
                    case "BADGES": {
                        List<tn.esprit.projet.models.UserBadge> badges = badgeRepo.findByUser(u.getId());
                        return badges.stream().anyMatch(ub -> ub.isUnlocked());
                    }
                    default: return true;
                }
            })
            .collect(Collectors.toList());
        
        displayUsers(filtered);
    }

    private void updateFilterButtons() {
        String activeStyle = "-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 6; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-size: 12px; -fx-background-radius: 6; -fx-cursor: hand;";
        
        btnFilterAll.setStyle(currentFilter.equals("ALL") ? activeStyle : inactiveStyle);
        btnFilterActive.setStyle(currentFilter.equals("ACTIVE") ? activeStyle : inactiveStyle);
        btnFilterFaceId.setStyle(currentFilter.equals("FACEID") ? activeStyle : inactiveStyle);
        btnFilterBadges.setStyle(currentFilter.equals("BADGES") ? activeStyle : inactiveStyle);
    }

    private void resetFilters() {
        currentFilter = "ALL";
        searchField.clear();
        updateFilterButtons();
    }
}
