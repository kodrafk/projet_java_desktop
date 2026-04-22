package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.projet.models.Publication;
import tn.esprit.projet.models.PublicationComment;
import tn.esprit.projet.models.PublicationLike;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.PublicationCommentService;
import tn.esprit.projet.services.PublicationLikeService;
import tn.esprit.projet.services.PublicationService;
import tn.esprit.projet.utils.SessionManager;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import javafx.geometry.Pos;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.List;

public class BlogFrontController {

    @FXML private TextField newPostTitle;
    @FXML private TextArea newPostContent;
    @FXML private Label lblImagePath;
    @FXML private VBox feedContainer;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;

    private String selectedImagePath = null;
    private int lastKnownPubId = -1;
    private Timeline pollingTimeline;

    private final PublicationService pubService = new PublicationService();
    private final PublicationCommentService commentService = new PublicationCommentService();
    private final PublicationLikeService likeService = new PublicationLikeService();
    private final tn.esprit.projet.services.PublicationReportService reportService = new tn.esprit.projet.services.PublicationReportService();
    
    private static final int REPORT_THRESHOLD = 2;

    @FXML
    public void initialize() {
        if (pollingTimeline != null) pollingTimeline.stop();

        if (!SessionManager.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Non Connecte", "Veuillez vous connecter pour voir le blog.");
            return;
        }

        if (sortCombo != null) {
            sortCombo.getItems().addAll("Plus recentes", "Plus anciennes", "Plus populaires (Likes)");
            sortCombo.setValue("Plus recentes");
            sortCombo.valueProperty().addListener((obs, o, n) -> loadFeed());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> loadFeed());
        }

        loadFeed();

        Publication latest = pubService.getLatestPublication();
        if (latest != null) lastKnownPubId = latest.getId();
        startNotificationPolling();
    }

    private void startNotificationPolling() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            Thread t = new Thread(() -> {
                Publication pub = pubService.getLatestPublication();
                Platform.runLater(() -> {
                    if (pub != null && pub.getId() > lastKnownPubId) {
                        lastKnownPubId = pub.getId();
                        if (SessionManager.getCurrentUser() != null
                                && pub.getUserId() != SessionManager.getCurrentUser().getId()) {
                            showNotification("Nouveau post de " + pub.getAuthorName() + " : \"" + pub.getTitre() + "\"");
                            loadFeed();
                        }
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    private void showNotification(String message) {
        Notifications.create()
                .title("Nouveau Post")
                .text(message)
                .hideAfter(Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT)
                .showInformation();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selected = fc.showOpenDialog(newPostTitle.getScene().getWindow());
        if (selected != null) {
            try {
                File uploadDir = new File("src/main/resources/uploads");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String fileName = System.currentTimeMillis() + "_" + selected.getName();
                File dest = new File(uploadDir, fileName);
                Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = "/uploads/" + fileName;
                if (lblImagePath != null) lblImagePath.setText(selected.getName());
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de copier l'image.");
            }
        }
    }

    @FXML
    private void handleCreatePost() {
        String title = newPostTitle.getText().trim();
        String cont = newPostContent.getText().trim();
        if (title.isEmpty() || cont.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Titre et contenu obligatoires."); return; }
        if (title.length() < 5 || title.length() > 100) { showAlert(Alert.AlertType.WARNING, "Attention", "Titre: 5-100 caracteres."); return; }
        if (cont.length() < 10) { showAlert(Alert.AlertType.WARNING, "Attention", "Contenu: min 10 caracteres."); return; }

        // ── Vérification bad words via API ──────────────────────────────────
        tn.esprit.projet.utils.BadWordsFilter.Result bw =
                tn.esprit.projet.utils.BadWordsFilter.checkAll(title, cont);
        if (bw.isProfanity) {
            showAlert(Alert.AlertType.WARNING,
                    "⚠ Contenu inapproprié",
                    "Votre publication contient des mots inappropriés et ne peut pas être publiée.\n" +
                    "Veuillez modifier le titre ou le contenu avant de soumettre.");
            return;
        }
        // ────────────────────────────────────────────────────────────────────

        User user = SessionManager.getCurrentUser();
        Publication p = new Publication();
        p.setTitre(title); p.setContenu(cont); p.setUserId(user.getId());
        p.setAuthorName(user.getFullName() == null || user.getFullName().isEmpty() ? user.getFirstName() : user.getFullName());
        p.setAuthorAvatar(user.getPhotoFilename());
        p.setAdmin(SessionManager.isAdmin());
        p.setVisibility("public");
        if (selectedImagePath != null) p.setImage(selectedImagePath);

        if (pubService.create(p)) {
            newPostTitle.clear(); newPostContent.clear();
            selectedImagePath = null;
            if (lblImagePath != null) lblImagePath.setText("Aucune image");
            loadFeed();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de creer la publication.");
        }
    }

    @FXML
    private void handleGenerateTitle() {
        String content = newPostContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Contenu vide", "Veuillez d'abord écrire du contenu pour générer un titre.");
            return;
        }

        new Thread(() -> {
            String title = tn.esprit.projet.utils.AIService.generateTitle(content);
            Platform.runLater(() -> {
                if (!title.isEmpty()) {
                    newPostTitle.setText(title);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur IA", "Impossible de générer le titre. Vérifiez votre clé API.");
                }
            });
        }).start();
    }

    @FXML
    private void handleGenerateHashtags() {
        String content = newPostContent.getText().trim();
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Contenu vide", "Veuillez d'abord écrire du contenu pour générer des hashtags.");
            return;
        }

        new Thread(() -> {
            String tags = tn.esprit.projet.utils.AIService.generateHashtags(content);
            Platform.runLater(() -> {
                if (!tags.isEmpty()) {
                    newPostContent.appendText("\n\n" + tags);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur IA", "Impossible de générer les hashtags. Vérifiez votre clé API.");
                }
            });
        }).start();
    }

    private void loadFeed() {
        feedContainer.getChildren().clear();
        List<Publication> pubs = pubService.findAll();

        String search = searchField != null && searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String sort = sortCombo != null && sortCombo.getValue() != null ? sortCombo.getValue() : "Plus recentes";

        if (!search.isEmpty()) {
            pubs = pubs.stream().filter(p ->
                    (p.getTitre() != null && p.getTitre().toLowerCase().contains(search)) ||
                    (p.getContenu() != null && p.getContenu().toLowerCase().contains(search)) ||
                    (p.getAuthorName() != null && p.getAuthorName().toLowerCase().contains(search))
            ).collect(Collectors.toList());
        }

        switch (sort) {
            case "Plus anciennes": pubs.sort(Comparator.comparing(Publication::getCreatedAt)); break;
            case "Plus populaires (Likes)": pubs.sort((a, b) -> Integer.compare(likeService.countLikes(b.getId()), likeService.countLikes(a.getId()))); break;
            default: pubs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())); break;
        }

        List<Integer> highlyReported = reportService.getHighlyReportedPublications(REPORT_THRESHOLD);
        
        // Alerte pour l'admin
        if (SessionManager.isAdmin() && !highlyReported.isEmpty()) {
            Notifications.create()
                .title("🚨 Publications Signalées")
                .text("Il y a " + highlyReported.size() + " publication(s) avec plus de " + REPORT_THRESHOLD + " signalements.")
                .position(Pos.BOTTOM_RIGHT)
                .showWarning();
        }

        for (Publication p : pubs) {
            // Masquer si trop de signalements (sauf pour l'admin)
            if (!SessionManager.isAdmin() && highlyReported.contains(p.getId())) {
                continue;
            }
            feedContainer.getChildren().add(createPublicationCard(p));
        }
    }

    // ─────────────── CARD DESIGN ───────────────
    private VBox createPublicationCard(Publication p) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Gradient accent top bar
        HBox accentBar = new HBox();
        accentBar.setPrefHeight(4);
        accentBar.setStyle("-fx-background-color: linear-gradient(to right, #1a7a3d, #4CAF50); -fx-background-radius: 12 12 0 0;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        // Header row
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle with initial
        String initial = (p.getAuthorName() != null && !p.getAuthorName().isEmpty())
                ? String.valueOf(p.getAuthorName().charAt(0)).toUpperCase() : "?";
        Label avatar = new Label(initial);
        avatar.setMinSize(44, 44); avatar.setMaxSize(44, 44);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a7a3d, #4CAF50); " +
                "-fx-background-radius: 22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17;");

        VBox authorInfo = new VBox(2);
        String badge = p.isAdmin() ? " (Admin)" : "";
        Label authorLbl = new Label(p.getAuthorName() + badge);
        authorLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1C1E21;");
        Label dateLbl = new Label(p.getCreatedAt().toLocalDate().toString());
        dateLbl.setStyle("-fx-text-fill: #8A8D91; -fx-font-size: 11;");
        authorInfo.getChildren().addAll(authorLbl, dateLbl);

        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        header.getChildren().addAll(avatar, authorInfo, hSpacer);

        // Title & content labels (créés avant les boutons pour que indexOf fonctionne)
        Label titleLbl = new Label(p.getTitre());
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 17));
        titleLbl.setStyle("-fx-text-fill: #1C1E21;");
        titleLbl.setWrapText(true);

        String fullContent = p.getContenu();
        String mainText = fullContent;
        String hashtagsPart = "";
        
        // Extraction des hashtags (cherche les # à la fin du texte)
        int hashtagIndex = fullContent.indexOf("#");
        if (hashtagIndex != -1) {
            mainText = fullContent.substring(0, hashtagIndex).trim();
            hashtagsPart = fullContent.substring(hashtagIndex).trim();
        }

        Label contentLbl = new Label(mainText);
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-text-fill: #3E4042; -fx-font-size: 13;");

        // Container pour les hashtags
        FlowPane tagsPane = new FlowPane(8, 8);
        tagsPane.setPadding(new Insets(4, 0, 8, 0));
        if (!hashtagsPart.isEmpty()) {
            String[] tags = hashtagsPart.split("\\s+");
            for (String tag : tags) {
                if (tag.startsWith("#")) {
                    Label tagLbl = new Label(tag);
                    tagLbl.setStyle("-fx-text-fill: #1a7a3d; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: #E8F5E9; -fx-background-radius: 4; -fx-padding: 2 6;");
                    tagsPane.getChildren().add(tagLbl);
                }
            }
        }

        // Action buttons (Owner ou Admin peut modifier)
        if (SessionManager.getCurrentUser().getId() == p.getUserId() || SessionManager.isAdmin()) {
            Button editBtn = makeBtn("Modifier", "#FFF3CD", "#856404");
            editBtn.setOnAction(e -> {
                // Vérifier bad words avant modification
                TextField et = new TextField(p.getTitre());
                et.setStyle("-fx-background-radius: 8; -fx-border-color: #1a7a3d; -fx-border-radius: 8; -fx-padding: 6 10;");
                TextArea ec = new TextArea(p.getContenu());
                ec.setPrefRowCount(3);
                ec.setStyle("-fx-background-radius: 8; -fx-border-color: #1a7a3d; -fx-border-radius: 8;");
                Button sv = makeBtn("Sauvegarder", "#28a745", "white");
                Button cl = makeBtn("Annuler", "#F0F2F5", "#606770");
                
                sv.setOnAction(ev -> {
                    String nt = et.getText().trim(); 
                    String nc = ec.getText().trim();
                    if (nt.length() < 5 || nt.length() > 100) { 
                        showAlert(Alert.AlertType.WARNING, "Attention", "Titre: 5-100 caracteres."); 
                        return; 
                    }
                    if (nc.length() < 10) { 
                        showAlert(Alert.AlertType.WARNING, "Attention", "Contenu: min 10 caracteres."); 
                        return; 
                    }
                    
                    // Note: Vérification bad words désactivée pour la modification
                    // (trop de faux positifs avec l'API)
                    
                    p.setTitre(nt); 
                    p.setContenu(nc);
                    if (pubService.update(p)) {
                        loadFeed();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la publication.");
                    }
                });
                
                cl.setOnAction(ev -> loadFeed());
                
                HBox acts = new HBox(8, sv, cl);
                acts.setAlignment(Pos.CENTER_LEFT);
                
                int ti = content.getChildren().indexOf(titleLbl);
                int ci = content.getChildren().indexOf(contentLbl);
                
                if (ti != -1) content.getChildren().set(ti, et);
                if (ci != -1) { 
                    content.getChildren().set(ci, ec); 
                    content.getChildren().add(ci + 1, acts); 
                }
                editBtn.setDisable(true);
            });
            header.getChildren().add(editBtn);
        }

        if (SessionManager.getCurrentUser().getId() == p.getUserId() || SessionManager.isAdmin()) {
            Button delBtn = makeBtn("Supprimer", "#FFE8E8", "#dc3545");
            delBtn.setOnAction(e -> { if (confirm("Supprimer cette publication ?")) { pubService.delete(p.getId()); loadFeed(); } });
            header.getChildren().add(delBtn);
        } else {
            // Bouton Signaler pour les autres utilisateurs
            Button reportBtn = makeBtn("🚩 Signaler", "transparent", "#606770");
            int reportCount = reportService.countReports(p.getId());
            if (reportCount > 0) reportBtn.setText("🚩 Signaler (" + reportCount + ")");
            
            reportBtn.setOnAction(e -> {
                if (confirm("Voulez-vous signaler cette publication pour contenu inapproprié ?")) {
                    tn.esprit.projet.models.PublicationReport r = new tn.esprit.projet.models.PublicationReport(
                        p.getId(), SessionManager.getCurrentUser().getId(), "Contenu inapproprié"
                    );
                    if (reportService.report(r)) {
                        Notifications.create()
                            .title("Signalement")
                            .text("Merci, votre signalement a été pris en compte.")
                            .position(Pos.BOTTOM_RIGHT)
                            .showInformation();
                        loadFeed();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Déjà signalé", "Vous avez déjà signalé cette publication.");
                    }
                }
            });
            header.getChildren().add(reportBtn);
        }

        // Section Admin : Approuver si signalée
        if (SessionManager.isAdmin()) {
            int reports = reportService.countReports(p.getId());
            if (reports > 0) {
                Button approveBtn = makeBtn("✅ Approuver (" + reports + ")", "#E8F5E9", "#1a7a3d");
                approveBtn.setOnAction(e -> {
                    if (confirm("Approuver cette publication et effacer les " + reports + " signalements ?")) {
                        if (reportService.deleteByPublicationId(p.getId())) {
                            loadFeed();
                        }
                    }
                });
                header.getChildren().add(approveBtn);
            }
        }

        // Image
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            try {
                File file = new File("src/main/resources" + p.getImage());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(640); iv.setPreserveRatio(true);
                        imageBox.getChildren().add(iv);
                        imageBox.setPadding(new Insets(8, 0, 8, 0));
                    }
                }
            } catch (Exception ignored) {}
        }

        // Separator
        Separator sep = new Separator(); sep.setStyle("-fx-opacity: 0.4;");

        // Like / Dislike
        int likes = likeService.countLikes(p.getId());
        int dislikes = likeService.countDislikes(p.getId());
        int userInt = likeService.getUserInteraction(p.getId(), SessionManager.getCurrentUser().getId());

        Button btnLike = new Button("  J'aime  " + likes);
        btnLike.setStyle(userInt == 1
                ? "-fx-background-color: #E8F5E9; -fx-text-fill: #1a7a3d; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 7 20; -fx-cursor: hand; -fx-font-size: 13;"
                : "-fx-background-color: #F0F2F5; -fx-text-fill: #606770; -fx-background-radius: 20; -fx-padding: 7 20; -fx-cursor: hand; -fx-font-size: 13;");
        Button btnDislike = new Button("  Je n'aime pas  " + dislikes);
        btnDislike.setStyle(userInt == 0
                ? "-fx-background-color: #FFE8E8; -fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 7 20; -fx-cursor: hand; -fx-font-size: 13;"
                : "-fx-background-color: #F0F2F5; -fx-text-fill: #606770; -fx-background-radius: 20; -fx-padding: 7 20; -fx-cursor: hand; -fx-font-size: 13;");
        btnLike.setOnAction(e -> handleLike(p, true));
        btnDislike.setOnAction(e -> handleLike(p, false));
        HBox interactions = new HBox(10, btnLike, btnDislike);
        interactions.setAlignment(Pos.CENTER_LEFT);

        // Comments section
        VBox commentsBox = new VBox(8);
        commentsBox.setStyle("-fx-background-color: #F7F8FA; -fx-padding: 12; -fx-background-radius: 8;");
        List<PublicationComment> comments = commentService.findByPublication(p.getId());
        if (!comments.isEmpty()) {
            Label cTitle = new Label(comments.size() + " commentaire(s)");
            cTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #606770; -fx-font-size: 12;");
            commentsBox.getChildren().add(cTitle);
            for (PublicationComment c : comments) commentsBox.getChildren().add(createCommentView(c));
        }

        // Add comment field
        TextField commentField = new TextField();
        commentField.setPromptText("Ecrire un commentaire...");
        commentField.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #DDE0E4; -fx-border-radius: 20; -fx-padding: 8 14; -fx-font-size: 13;");
        HBox.setHgrow(commentField, Priority.ALWAYS);
        Button sendBtn = makeBtn("Envoyer", "#1a7a3d", "white");
        sendBtn.setOnAction(e -> {
            String ct = commentField.getText().trim();
            if (ct.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Attention", "Commentaire vide."); return; }
            if (ct.length() < 3) { showAlert(Alert.AlertType.WARNING, "Attention", "Min 3 caracteres."); return; }
            PublicationComment nc = new PublicationComment();
            nc.setContenu(ct); nc.setPublicationId(p.getId());
            User u = SessionManager.getCurrentUser();
            nc.setUserId(u.getId());
            nc.setAuthorName(u.getFullName() == null || u.getFullName().isEmpty() ? u.getFirstName() : u.getFullName());
            nc.setAuthorAvatar(u.getPhotoFilename()); nc.setAdmin(SessionManager.isAdmin());
            commentService.create(nc); loadFeed();
        });
        HBox addCom = new HBox(8, commentField, sendBtn);
        addCom.setAlignment(Pos.CENTER_LEFT);
        commentsBox.getChildren().add(addCom);

        // Remove placeholder if we added one
        content.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().isEmpty());

        content.getChildren().addAll(header, titleLbl, contentLbl, imageBox, sep);
        if (!tagsPane.getChildren().isEmpty()) content.getChildren().add(tagsPane);
        content.getChildren().addAll(interactions, commentsBox);
        card.getChildren().addAll(accentBar, content);
        return card;
    }

    // ─────────────── COMMENT VIEW ───────────────
    private HBox createCommentView(PublicationComment c) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white; -fx-padding: 8 12; -fx-background-radius: 8;");

        // Mini avatar
        String initial = (c.getAuthorName() != null && !c.getAuthorName().isEmpty())
                ? String.valueOf(c.getAuthorName().charAt(0)).toUpperCase() : "?";
        Label avatar = new Label(initial);
        avatar.setMinSize(30, 30); avatar.setMaxSize(30, 30);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: linear-gradient(to bottom right, #6c757d, #adb5bd); " +
                "-fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");

        VBox textArea = new VBox(2);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        Label authorLbl = new Label(c.getAuthorName());
        authorLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #1C1E21;");
        Label textLbl = new Label(c.getContenu());
        textLbl.setWrapText(true);
        textLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #3E4042;");
        textArea.getChildren().addAll(authorLbl, textLbl);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(avatar, textArea, spacer);

        if (SessionManager.getCurrentUser().getId() == c.getUserId() || SessionManager.isAdmin()) {
            Button editBtn = new Button("Modifier");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d39e00; -fx-font-size: 11; -fx-underline: true; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                TextField ef = new TextField(c.getContenu());
                ef.setStyle("-fx-background-radius: 8; -fx-border-color: #1a7a3d; -fx-border-radius: 8;");
                HBox.setHgrow(ef, Priority.ALWAYS);
                Button sv = makeBtn("OK", "#28a745", "white");
                Button cl = makeBtn("X", "#F0F2F5", "#606770");
                sv.setOnAction(ev -> {
                    String nc = ef.getText().trim();
                    if (nc.isEmpty() || nc.length() < 3) { showAlert(Alert.AlertType.WARNING, "Attention", "Min 3 caracteres."); return; }
                    c.setContenu(nc);
                    if (commentService.update(c)) loadFeed();
                });
                cl.setOnAction(ev -> loadFeed());
                textArea.getChildren().clear();
                textArea.getChildren().addAll(authorLbl, new HBox(6, ef, sv, cl));
                editBtn.setDisable(true);
            });
            box.getChildren().add(editBtn);
        }

        if (SessionManager.getCurrentUser().getId() == c.getUserId() || SessionManager.isAdmin()) {
            Button delBtn = new Button("X");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 11; -fx-cursor: hand;");
            delBtn.setOnAction(e -> { if (confirm("Supprimer ce commentaire ?")) { commentService.delete(c.getId()); loadFeed(); } });
            box.getChildren().add(delBtn);
        }
        return box;
    }

    private Button makeBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-background-radius: 12; -fx-padding: 5 14; -fx-cursor: hand; -fx-font-size: 12;");
        return b;
    }

    private void handleLike(Publication p, boolean isLike) {
        int uId = SessionManager.getCurrentUser().getId();
        int cur = likeService.getUserInteraction(p.getId(), uId);
        if ((isLike && cur == 1) || (!isLike && cur == 0)) {
            likeService.removeInteraction(p.getId(), uId);
        } else {
            PublicationLike like = new PublicationLike();
            like.setPublicationId(p.getId()); like.setUserId(uId); like.setLike(isLike);
            likeService.addInteraction(like);
        }
        loadFeed();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }
}
