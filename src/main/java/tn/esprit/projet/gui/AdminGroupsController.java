package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.projet.models.ChatMessage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserGroup;
import tn.esprit.projet.repository.ChatMessageRepository;
import tn.esprit.projet.repository.UserGroupRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.TwilioService;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminGroupsController {

    // Left panel
    @FXML private VBox  groupsList;
    @FXML private VBox  pinnedList;
    @FXML private VBox  pinnedSection;
    @FXML private Label lblGroupCount;
    @FXML private TextField searchGroups;

    // Detail panel
    @FXML private VBox  emptyState;
    @FXML private VBox  groupDetail;
    @FXML private StackPane groupAvatarPane;
    @FXML private Label lblGroupInitial;
    @FXML private Label lblGroupName;
    @FXML private Label lblGroupMeta;
    @FXML private Button btnPin;

    // Tabs
    @FXML private Button btnTabMembers;
    // btnTabMessage hidden
    @FXML private VBox   tabMembers;
    @FXML private VBox   tabMessage;

    // Members tab
    @FXML private TextField searchUsers;
    @FXML private VBox      searchResults;
    @FXML private VBox      membersList;
    @FXML private Label     lblMemberCount;

    // Message tab
    @FXML private TextArea  txtGroupMessage;
    @FXML private Label     lblGroupCharCount;
    @FXML private Label     lblGroupRecipients;
    @FXML private CheckBox  chkGroupSms;
    @FXML private VBox      broadcastHistory;

    private UserGroup selectedGroup;

    private final UserGroupRepository groupRepo = new UserGroupRepository();
    private final UserRepository      userRepo  = new UserRepository();
    private final ChatMessageRepository chatRepo = new ChatMessageRepository();
    private final TwilioService       twilio    = TwilioService.getInstance();

    private static final String[] COLORS = {
        "#2E7D5A","#3B82F6","#7C3AED","#EC4899","#F59E0B","#EF4444","#0891B2","#059669"
    };
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    public void initialize() {
        groupRepo.ensureTablesExist();
        chatRepo.ensureTableExists();

        txtGroupMessage.textProperty().addListener((obs, old, val) -> {
            int len = val != null ? val.length() : 0;
            lblGroupCharCount.setText(len + " / 500");
            lblGroupCharCount.setStyle("-fx-font-size:11px;-fx-text-fill:" +
                    (len > 500 ? "#DC2626;-fx-font-weight:bold;" : "#94A3B8;"));
        });

        searchUsers.textProperty().addListener((obs, old, val) -> handleSearchUsers(val));

        // Live search on groups
        if (searchGroups != null) {
            searchGroups.textProperty().addListener((obs, old, val) -> loadGroups(val));
        }

        loadGroups();
    }

    // ── Load groups list ──────────────────────────────────────────────────────

    private void loadGroups() { loadGroups(null); }

    private void loadGroups(String filter) {
        User admin = Session.getCurrentUser();
        if (admin == null) return;

        List<UserGroup> all = groupRepo.findByAdmin(admin.getId());

        // Apply search filter
        if (filter != null && !filter.isBlank()) {
            String q = filter.toLowerCase();
            all = all.stream()
                    .filter(g -> g.getName().toLowerCase().contains(q) ||
                            (g.getDescription() != null && g.getDescription().toLowerCase().contains(q)))
                    .toList();
        }

        lblGroupCount.setText(all.size() + " group" + (all.size() != 1 ? "s" : ""));

        List<UserGroup> pinned   = all.stream().filter(UserGroup::isPinned).toList();
        List<UserGroup> unpinned = all.stream().filter(g -> !g.isPinned()).toList();

        // ── Pinned section ────────────────────────────────────────────────────
        if (pinnedSection != null) {
            if (pinned.isEmpty()) {
                pinnedSection.setVisible(false); pinnedSection.setManaged(false);
            } else {
                pinnedSection.setVisible(true); pinnedSection.setManaged(true);
                pinnedList.getChildren().clear();
                for (UserGroup g : pinned) pinnedList.getChildren().add(buildGroupRow(g, true));
            }
        }

        // ── All groups ────────────────────────────────────────────────────────
        groupsList.getChildren().clear();

        if (unpinned.isEmpty() && pinned.isEmpty()) {
            Label empty = new Label(filter != null && !filter.isBlank()
                    ? "No groups match \"" + filter + "\""
                    : "No groups yet.\nClick '+ Create Group' to start.");
            empty.setWrapText(true);
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-padding:16;-fx-text-alignment:center;");
            groupsList.getChildren().add(empty);
            return;
        }

        for (UserGroup g : unpinned) {
            groupsList.getChildren().add(buildGroupRow(g, false));
        }
    }

    private HBox buildGroupRow(UserGroup g) {
        return buildGroupRow(g, false);
    }

    private HBox buildGroupRow(UserGroup g, boolean inPinnedSection) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-radius:10;-fx-cursor:hand;");

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(36, 36);
        avatar.setStyle("-fx-background-color:" + g.getColor() + ";-fx-background-radius:18;");
        Label initial = new Label(g.getName().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:white;");
        avatar.getChildren().add(initial);

        VBox info = new VBox(2);
        HBox nameRow = new HBox(5);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(g.getName());
        name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        nameRow.getChildren().add(name);
        if (g.isPinned()) {
            Label pin = new Label("📌");
            pin.setStyle("-fx-font-size:10px;");
            nameRow.getChildren().add(pin);
        }

        int count = groupRepo.getMemberIds(g.getId()).size();
        Label meta = new Label(count + " member" + (count != 1 ? "s" : ""));
        meta.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");
        info.getChildren().addAll(nameRow, meta);

        row.getChildren().addAll(avatar, info);

        boolean isSelected = selectedGroup != null && selectedGroup.getId() == g.getId();
        String bgSelected = inPinnedSection ? "#FFFBEB" : "#F0FDF4";
        row.setStyle("-fx-background-color:" + (isSelected ? bgSelected : "transparent") +
                ";-fx-background-radius:10;-fx-cursor:hand;");

        row.setOnMouseClicked(e -> selectGroup(g));
        row.setOnMouseEntered(e -> {
            if (selectedGroup == null || selectedGroup.getId() != g.getId())
                row.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:10;-fx-cursor:hand;");
        });
        row.setOnMouseExited(e -> {
            if (selectedGroup == null || selectedGroup.getId() != g.getId())
                row.setStyle("-fx-background-color:transparent;-fx-background-radius:10;-fx-cursor:hand;");
        });

        return row;
    }

    // ── Select group ──────────────────────────────────────────────────────────

    private void selectGroup(UserGroup g) {
        selectedGroup = g;
        loadGroups(searchGroups != null ? searchGroups.getText() : null);

        emptyState.setVisible(false); emptyState.setManaged(false);
        groupDetail.setVisible(true); groupDetail.setManaged(true);

        // Header
        lblGroupInitial.setText(g.getName().substring(0, 1).toUpperCase());
        groupAvatarPane.setStyle("-fx-background-color:" + g.getColor() + ";-fx-background-radius:22;");
        lblGroupName.setText(g.getName());

        List<Integer> memberIds = groupRepo.getMemberIds(g.getId());
        String desc = g.getDescription() != null && !g.getDescription().isBlank()
                ? g.getDescription() + " • " : "";
        lblGroupMeta.setText(desc + memberIds.size() + " members");

        refreshPinButton();

        // Default tab: Members
        showTab("members");
        loadMembers();
        loadBroadcastHistory();
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    @FXML private void handleTabMembers() { showTab("members"); }
    @FXML private void handleTabMessage() { showTab("message"); }

    private void showTab(String tab) {
        boolean m = "members".equals(tab);
        tabMembers.setVisible(m); tabMembers.setManaged(m);
        tabMessage.setVisible(false); tabMessage.setManaged(false); // always hidden

        String active = "-fx-background-color:transparent;-fx-font-size:13px;-fx-font-weight:bold;-fx-cursor:hand;-fx-background-radius:0;-fx-border-color:transparent transparent #2E7D5A transparent;-fx-border-width:0 0 2 0;";
        btnTabMembers.setStyle(active + "-fx-text-fill:#2E7D5A;");
    }

    // ── Members tab ───────────────────────────────────────────────────────────

    private void loadMembers() {
        if (selectedGroup == null) return;
        membersList.getChildren().clear();

        List<User> members = groupRepo.getMembers(selectedGroup.getId());
        lblMemberCount.setText(members.size() + " member" + (members.size() != 1 ? "s" : ""));

        if (members.isEmpty()) {
            Label empty = new Label("No members yet. Search and add users above.");
            empty.setStyle("-fx-font-size:12px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
            membersList.getChildren().add(empty);
            return;
        }

        for (User u : members) {
            membersList.getChildren().add(buildMemberRow(u));
        }
    }

    private HBox buildMemberRow(User u) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#F1F5F9;-fx-border-radius:10;-fx-border-width:1;");

        // Avatar
        StackPane av = new StackPane();
        av.setPrefSize(34, 34);
        av.setStyle("-fx-background-color:#E2E8F0;-fx-background-radius:17;");
        String init = u.getFirstName() != null && !u.getFirstName().isEmpty()
                ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?";
        Label il = new Label(init);
        il.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#475569;");
        av.getChildren().add(il);

        VBox info = new VBox(2);
        Label name = new Label(u.getFullName());
        name.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");
        Label email = new Label(u.getEmail());
        email.setStyle("-fx-font-size:10px;-fx-text-fill:#64748B;");
        info.getChildren().addAll(name, email);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button remove = new Button("✕");
        remove.setStyle("-fx-background-color:#FEF2F2;-fx-text-fill:#DC2626;-fx-font-size:11px;-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:3 8;");
        remove.setOnAction(e -> {
            groupRepo.removeMember(selectedGroup.getId(), u.getId());
            loadMembers();
            lblGroupMeta.setText((groupRepo.getMemberIds(selectedGroup.getId()).size()) + " members");
            toast("Removed " + u.getFirstName(), Toasts.Type.INFO);
        });

        row.getChildren().addAll(av, info, remove);
        return row;
    }

    private void handleSearchUsers(String query) {
        searchResults.getChildren().clear();
        if (query == null || query.length() < 2) {
            searchResults.setVisible(false); searchResults.setManaged(false); return;
        }

        List<User> results = userRepo.searchByEmailOrName(query);
        List<Integer> existing = selectedGroup != null ? groupRepo.getMemberIds(selectedGroup.getId()) : List.of();

        // Filter: only ROLE_USER, not already in group
        results = results.stream()
                .filter(u -> !u.isAdmin() && !existing.contains(u.getId()))
                .limit(5).toList();

        if (results.isEmpty()) {
            searchResults.setVisible(false); searchResults.setManaged(false); return;
        }

        for (User u : results) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color:white;-fx-background-radius:8;-fx-border-color:#E2E8F0;-fx-border-radius:8;-fx-border-width:1;-fx-cursor:hand;");

            Label name = new Label(u.getFullName() + " — " + u.getEmail());
            name.setStyle("-fx-font-size:12px;-fx-text-fill:#1E293B;");
            HBox.setHgrow(name, Priority.ALWAYS);

            Button add = new Button("+ Add");
            add.setStyle("-fx-background-color:#DCFCE7;-fx-text-fill:#166534;-fx-font-size:11px;-fx-background-radius:6;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:3 10;");
            add.setOnAction(e -> {
                groupRepo.addMember(selectedGroup.getId(), u.getId());
                searchUsers.clear();
                searchResults.setVisible(false); searchResults.setManaged(false);
                loadMembers();
                lblGroupMeta.setText(groupRepo.getMemberIds(selectedGroup.getId()).size() + " members");
                toast(u.getFirstName() + " added to group ✓", Toasts.Type.SUCCESS);
            });

            row.getChildren().addAll(name, add);
            searchResults.getChildren().add(row);
        }

        searchResults.setVisible(true); searchResults.setManaged(true);
    }

    @FXML
    private void handleAddMember() {
        handleSearchUsers(searchUsers.getText());
    }

    // ── Send message to group ─────────────────────────────────────────────────

    @FXML
    private void handleSendToGroup() {
        if (selectedGroup == null) return;
        String text = txtGroupMessage.getText().trim();
        if (text.isBlank()) { toast("Write a message first.", Toasts.Type.WARNING); return; }
        if (text.length() > 500) { toast("Message too long (max 500 chars).", Toasts.Type.WARNING); return; }

        User admin = Session.getCurrentUser();
        if (admin == null) return;

        List<Integer> memberIds = groupRepo.getMemberIds(selectedGroup.getId());
        if (memberIds.isEmpty()) { toast("Group has no members.", Toasts.Type.WARNING); return; }

        boolean sendSms = chkGroupSms.isSelected();
        int sent = 0, smsSent = 0;

        for (int userId : memberIds) {
            // Save chat message for each member
            ChatMessage msg = new ChatMessage(
                    admin.getId(), userId,
                    ChatMessage.SenderType.ADMIN,
                    text, null, sendSms);
            ChatMessage saved = chatRepo.save(msg);

            if (saved != null) {
                sent++;
                // SMS
                if (sendSms) {
                    User user = userRepo.findById(userId);
                    if (user != null && user.getPhone() != null && !user.getPhone().isBlank()) {
                        String phone = user.getPhone().startsWith("+") ? user.getPhone() : "+" + user.getPhone();
                        String body = "📩 Message from your NutriLife Coach [" + selectedGroup.getName() + "]:\n\n" + text;
                        boolean ok = twilio.sendSms(phone, body);
                        if (ok) smsSent++;
                        chatRepo.updateSmsStatus(saved.getId(), ok ? "sent" : "failed");
                    }
                }
            }
        }

        txtGroupMessage.clear();
        loadBroadcastHistory();

        String feedback = "Message sent to " + sent + " member" + (sent != 1 ? "s" : "");
        if (sendSms) feedback += " • " + smsSent + " SMS delivered";
        toast(feedback + " ✓", Toasts.Type.SUCCESS);
    }

    private void loadBroadcastHistory() {
        if (selectedGroup == null || broadcastHistory == null) return;
        broadcastHistory.getChildren().clear();

        User admin = Session.getCurrentUser();
        if (admin == null) return;

        List<Integer> memberIds = groupRepo.getMemberIds(selectedGroup.getId());
        if (memberIds.isEmpty()) return;

        // Show last 5 messages sent by this admin to any member of this group
        try {
            java.sql.Connection c = tn.esprit.projet.utils.DatabaseConnection.getInstance().getConnection();
            String inClause = memberIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0");
            java.sql.PreparedStatement ps = c.prepareStatement(
                "SELECT content, sent_at, sent_via_sms, sms_status FROM chat_messages " +
                "WHERE sender_id=? AND sender_type='ADMIN' AND receiver_id IN (" + inClause + ") " +
                "GROUP BY content, sent_at ORDER BY sent_at DESC LIMIT 5");
            ps.setInt(1, admin.getId());
            java.sql.ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String content = rs.getString("content");
                java.sql.Timestamp ts = rs.getTimestamp("sent_at");
                boolean sms = rs.getBoolean("sent_via_sms");

                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 8, 6, 8));
                row.setStyle("-fx-background-color:#F8FAFC;-fx-background-radius:8;");

                Label msg = new Label(content != null && content.length() > 60
                        ? content.substring(0, 60) + "…" : content);
                msg.setStyle("-fx-font-size:11px;-fx-text-fill:#374151;");
                HBox.setHgrow(msg, Priority.ALWAYS);

                String timeStr = ts != null ? ts.toLocalDateTime().format(FMT) : "";
                Label time = new Label(timeStr + (sms ? " 📱" : ""));
                time.setStyle("-fx-font-size:10px;-fx-text-fill:#94A3B8;");

                row.getChildren().addAll(msg, time);
                broadcastHistory.getChildren().add(row);
            }

            if (broadcastHistory.getChildren().isEmpty()) {
                Label empty = new Label("No messages sent yet.");
                empty.setStyle("-fx-font-size:11px;-fx-text-fill:#94A3B8;-fx-font-style:italic;");
                broadcastHistory.getChildren().add(empty);
            }
        } catch (Exception e) {
            System.err.println("[Groups] loadBroadcastHistory: " + e.getMessage());
        }
    }

    // ── Create / Edit / Delete group ──────────────────────────────────────────

    @FXML
    private void handleCreateGroup() {
        showGroupDialog(null);
    }

    @FXML
    private void handleEditGroup() {
        if (selectedGroup != null) showGroupDialog(selectedGroup);
    }

    @FXML
    private void handleTogglePin() {
        if (selectedGroup == null) return;
        User admin = Session.getCurrentUser();
        if (admin == null) return;
        groupRepo.togglePin(selectedGroup.getId(), admin.getId());
        selectedGroup.setPinned(!selectedGroup.isPinned());
        refreshPinButton();
        loadGroups(searchGroups != null ? searchGroups.getText() : null);
        toast(selectedGroup.isPinned() ? "📌 Group pinned" : "Group unpinned", Toasts.Type.SUCCESS);
    }

    private void refreshPinButton() {
        if (btnPin == null || selectedGroup == null) return;
        if (selectedGroup.isPinned()) {
            btnPin.setText("📌 Unpin");
            btnPin.setStyle("-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;-fx-font-size:12px;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:#FDE68A;-fx-border-radius:8;-fx-border-width:1;-fx-padding:6 14;");
        } else {
            btnPin.setText("📌 Pin");
            btnPin.setStyle("-fx-background-color:#FFFBEB;-fx-text-fill:#D97706;-fx-font-size:12px;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:#FDE68A;-fx-border-radius:8;-fx-border-width:1;-fx-padding:6 14;");
        }
    }

    private void showGroupDialog(UserGroup existing) {
        boolean isNew = (existing == null);

        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle(isNew ? "Create Group" : "Edit Group");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:white;");
        root.setPrefWidth(400);

        Label title = new Label(isNew ? "➕ Create a new group" : "✏ Edit group");
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");

        // Name
        Label lblName = new Label("Group name *");
        lblName.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        TextField tfName = new TextField(isNew ? "" : existing.getName());
        tfName.setPromptText("e.g. Weight Loss Team");
        tfName.setStyle("-fx-background-color:#F8FAFC;-fx-border-color:#E2E8F0;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:13px;");

        // Description
        Label lblDesc = new Label("Description (optional)");
        lblDesc.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        TextField tfDesc = new TextField(isNew ? "" : (existing.getDescription() != null ? existing.getDescription() : ""));
        tfDesc.setPromptText("Short description of this group");
        tfDesc.setStyle("-fx-background-color:#F8FAFC;-fx-border-color:#E2E8F0;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-size:13px;");

        // Color picker
        Label lblColor = new Label("Color");
        lblColor.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        HBox colorRow = new HBox(8);
        colorRow.setAlignment(Pos.CENTER_LEFT);
        final String[] chosenColor = { isNew ? COLORS[0] : existing.getColor() };

        for (String c : COLORS) {
            StackPane dot = new StackPane();
            dot.setPrefSize(28, 28);
            dot.setStyle("-fx-background-color:" + c + ";-fx-background-radius:14;-fx-cursor:hand;" +
                    (c.equals(chosenColor[0]) ? "-fx-border-color:white;-fx-border-width:2;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),6,0,0,2);" : ""));
            dot.setOnMouseClicked(e -> {
                chosenColor[0] = c;
                colorRow.getChildren().forEach(n -> {
                    if (n instanceof StackPane sp) {
                        String sc = sp.getStyle().contains(c) ? c : "";
                        // reset all
                        sp.setStyle(sp.getStyle().replaceAll("-fx-border.*?;", "").replaceAll("-fx-effect.*?;", ""));
                    }
                });
                dot.setStyle("-fx-background-color:" + c + ";-fx-background-radius:14;-fx-cursor:hand;" +
                        "-fx-border-color:white;-fx-border-width:2;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),6,0,0,2);");
            });
            colorRow.getChildren().add(dot);
        }

        // Buttons
        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color:#F1F5F9;-fx-text-fill:#374151;-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:8 20;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button(isNew ? "Create" : "Save");
        btnSave.setStyle("-fx-background-color:#2E7D5A;-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:8 20;");
        btnSave.setOnAction(e -> {
            String name = tfName.getText().trim();
            if (name.isBlank()) {
                tfName.setStyle(tfName.getStyle() + "-fx-border-color:#EF4444;");
                return;
            }
            User admin = Session.getCurrentUser();
            if (admin == null) return;

            if (isNew) {
                UserGroup g = new UserGroup(name, tfDesc.getText().trim(), admin.getId(), chosenColor[0]);
                UserGroup saved = groupRepo.save(g);
                if (saved != null) {
                    dialog.close();
                    loadGroups();
                    selectGroup(saved);
                }
            } else {
                existing.setName(name);
                existing.setDescription(tfDesc.getText().trim());
                existing.setColor(chosenColor[0]);
                if (groupRepo.update(existing)) {
                    dialog.close();
                    loadGroups();
                    selectGroup(existing);
                }
            }
        });

        btns.getChildren().addAll(btnCancel, btnSave);
        root.getChildren().addAll(title, lblName, tfName, lblDesc, tfDesc, lblColor, colorRow, btns);

        dialog.setScene(new javafx.scene.Scene(root, 400, 340));
        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteGroup() {
        if (selectedGroup == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete group \"" + selectedGroup.getName() + "\"?\nThis will remove all members from the group.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Group"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                User admin = Session.getCurrentUser();
                if (admin != null && groupRepo.delete(selectedGroup.getId(), admin.getId())) {
                    selectedGroup = null;
                    emptyState.setVisible(true); emptyState.setManaged(true);
                    groupDetail.setVisible(false); groupDetail.setManaged(false);
                    loadGroups();
                    toast("Group deleted.", Toasts.Type.INFO);
                }
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void toast(String msg, Toasts.Type type) {
        Stage s = (Stage) groupsList.getScene().getWindow();
        Toasts.show(s, msg, type);
    }
}
