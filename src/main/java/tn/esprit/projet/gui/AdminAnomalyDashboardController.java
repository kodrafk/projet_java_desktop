package tn.esprit.projet.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;import tn.esprit.projet.models.HealthAnomaly;
import tn.esprit.projet.models.HealthAlert;
import tn.esprit.projet.repository.AnomalyRepository;
import tn.esprit.projet.services.AnomalyDetectionService;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.MyBDConnexion;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminAnomalyDashboardController {

    @FXML private Label lblTotalAnomalies;
    @FXML private Label lblHighRiskUsers;
    @FXML private Label lblActiveAlerts;
    @FXML private Label lblAccuracy;
    @FXML private Label lblLastUpdate;
    @FXML private Label lblAnomalyCount;
    @FXML private Label lblRiskCount;
    @FXML private Label lblAlertCount;
    @FXML private Label lblSystemStatus;
    @FXML private Label lblAnomalySubtitle;
    @FXML private Label lblRiskSubtitle;
    @FXML private Label lblAlertSubtitle;
    @FXML private Label lblAccuracySubtitle;

    @FXML private Button btnRunDetection;
    @FXML private Button btnRefresh;
    @FXML private Button btnExport;
    @FXML private ProgressIndicator progressIndicator;

    // Toast bar
    @FXML private javafx.scene.layout.HBox toastBar;
    @FXML private Label toastIcon;
    @FXML private Label toastMessage;
    @FXML private Label toastDetail;

    @FXML private TableView<Map<String, Object>> tableAnomalies;
    @FXML private TableColumn<Map<String, Object>, String> colUserName;
    @FXML private TableColumn<Map<String, Object>, String> colUserEmail;
    @FXML private TableColumn<Map<String, Object>, String> colAnomalyType;
    @FXML private TableColumn<Map<String, Object>, String> colSeverity;
    @FXML private TableColumn<Map<String, Object>, String> colValue;
    @FXML private TableColumn<Map<String, Object>, String> colDetectedAt;
    @FXML private TableColumn<Map<String, Object>, String> colStatus;
    @FXML private TableColumn<Map<String, Object>, String> colAction;

    @FXML private TableView<Map<String, Object>> tableHighRisk;
    @FXML private TableColumn<Map<String, Object>, String> colRiskUserName;
    @FXML private TableColumn<Map<String, Object>, String> colRiskUserEmail;
    @FXML private TableColumn<Map<String, Object>, String> colRiskScore;
    @FXML private TableColumn<Map<String, Object>, String> colInactivityDays;
    @FXML private TableColumn<Map<String, Object>, String> colLastActive;
    @FXML private TableColumn<Map<String, Object>, String> colWeightTrend;
    @FXML private TableColumn<Map<String, Object>, String> colPrediction;

    @FXML private TableView<Map<String, Object>> tableAlerts;
    @FXML private TableColumn<Map<String, Object>, String> colAlertUser;
    @FXML private TableColumn<Map<String, Object>, String> colAlertTitle;
    @FXML private TableColumn<Map<String, Object>, String> colAlertPriority;
    @FXML private TableColumn<Map<String, Object>, String> colAlertRisk;
    @FXML private TableColumn<Map<String, Object>, String> colAlertRec;
    @FXML private TableColumn<Map<String, Object>, String> colAlertDate;

    private final AnomalyRepository repository = new AnomalyRepository();
    private final DateTimeFormatter dtFmt   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Map<Integer, String[]> userCache = new HashMap<>();

    @FXML
    public void initialize() {
        loadUserCache();
        setupTables();
        ensureDataExists();
        loadDashboardData();
        updateLastUpdateTime();
    }

    private void loadUserCache() {
        try (Statement st = MyBDConnexion.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery("SELECT id, first_name, last_name, email FROM `user` LIMIT 200")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = (rs.getString("first_name") + " " + rs.getString("last_name")).trim();
                userCache.put(id, new String[]{name.isEmpty() ? "User #" + id : name, rs.getString("email")});
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] User cache error: " + e.getMessage());
        }
    }

    private String userName(int uid)  { String[] u = userCache.get(uid); return u != null ? u[0] : "User #" + uid; }
    private String userEmail(int uid) { String[] u = userCache.get(uid); return u != null ? u[1] : ""; }

    private void ensureDataExists() {
        try (Statement st = MyBDConnexion.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM health_anomalies")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (Exception e) { return; }
        AnomalyDetectionService svc = new AnomalyDetectionService();
        for (int uid = 1; uid <= 20; uid++) {
            try {
                List<HealthAnomaly> list = svc.detectAnomalies(uid);
                for (HealthAnomaly a : list) svc.saveAnomaly(a);
                for (HealthAlert al : svc.generateAlerts(list)) svc.saveAlert(al);
            } catch (Exception ignored) {}
        }
    }

    private void setupTables() {
        if (tableAnomalies != null) {
            colUserName.setCellValueFactory(d -> prop(d.getValue(), "user_name"));
            colUserEmail.setCellValueFactory(d -> prop(d.getValue(), "user_email"));
            colAnomalyType.setCellValueFactory(d -> prop(d.getValue(), "issue"));
            colValue.setCellValueFactory(d -> prop(d.getValue(), "score"));
            colDetectedAt.setCellValueFactory(d -> prop(d.getValue(), "detected"));
            colStatus.setCellValueFactory(d -> prop(d.getValue(), "status"));
            colAction.setCellValueFactory(d -> prop(d.getValue(), "action"));
            colSeverity.setCellValueFactory(d -> prop(d.getValue(), "level"));
            colSeverity.setCellFactory(col -> colorCell(Map.of(
                "CRITICAL","#DC2626","HIGH","#EA580C","MEDIUM","#D97706","LOW","#16A34A")));
            colStatus.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); setStyle(""); return; }
                    setText(v);
                    setStyle(v.equals("Active") ? "-fx-text-fill:#DC2626;-fx-font-weight:bold;" : "-fx-text-fill:#16A34A;");
                }
            });
        }
        if (tableHighRisk != null) {
            colRiskUserName.setCellValueFactory(d -> prop(d.getValue(), "user_name"));
            colRiskUserEmail.setCellValueFactory(d -> prop(d.getValue(), "user_email"));
            colInactivityDays.setCellValueFactory(d -> prop(d.getValue(), "inactive_days"));
            colLastActive.setCellValueFactory(d -> prop(d.getValue(), "last_active"));
            colWeightTrend.setCellValueFactory(d -> prop(d.getValue(), "weight_trend"));
            colPrediction.setCellValueFactory(d -> prop(d.getValue(), "priority"));
            colRiskScore.setCellValueFactory(d -> prop(d.getValue(), "risk_score"));
            colRiskScore.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || v == null) { setText(null); setStyle(""); return; }
                    setText(v);
                    try {
                        double s = Double.parseDouble(v.replace("%",""));
                        if (s >= 80) setStyle("-fx-text-fill:#DC2626;-fx-font-weight:bold;");
                        else if (s >= 65) setStyle("-fx-text-fill:#EA580C;-fx-font-weight:bold;");
                        else setStyle("-fx-text-fill:#D97706;-fx-font-weight:bold;");
                    } catch (Exception ignored) {}
                }
            });
            colPrediction.setCellFactory(col -> colorCell(Map.of(
                "Urgent","#DC2626","High Priority","#EA580C","Medium Priority","#D97706","Monitor","#16A34A")));
        }
        if (tableAlerts != null) {
            colAlertUser.setCellValueFactory(d -> prop(d.getValue(), "user_name"));
            colAlertTitle.setCellValueFactory(d -> prop(d.getValue(), "title"));
            colAlertRisk.setCellValueFactory(d -> prop(d.getValue(), "risk_score"));
            colAlertRec.setCellValueFactory(d -> prop(d.getValue(), "recommendation"));
            colAlertDate.setCellValueFactory(d -> prop(d.getValue(), "created"));
            colAlertPriority.setCellValueFactory(d -> prop(d.getValue(), "priority"));
            colAlertPriority.setCellFactory(col -> colorCell(Map.of(
                "CRITICAL","#DC2626","HIGH","#EA580C","MEDIUM","#D97706","LOW","#16A34A")));
        }
    }

    private TableCell<Map<String, Object>, String> colorCell(Map<String, String> colors) {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v);
                String c = colors.get(v);
                setStyle(c != null ? "-fx-text-fill:" + c + ";-fx-font-weight:bold;" : "");
            }
        };
    }

    private void loadDashboardData() {
        try {
            Connection conn = MyBDConnexion.getInstance().getCnx();

            int total    = queryInt(conn, "SELECT COUNT(*) FROM health_anomalies");
            int critical = queryInt(conn, "SELECT COUNT(*) FROM health_anomalies WHERE severity >= 80 AND resolved = FALSE");
            int pending  = queryInt(conn, "SELECT COUNT(*) FROM health_alerts WHERE acknowledged = FALSE");
            int resolved = queryInt(conn, "SELECT COUNT(*) FROM health_anomalies WHERE resolved = TRUE");
            double avgRisk = queryDouble(conn, "SELECT AVG(abandonment_risk) FROM user_health_metrics WHERE abandonment_risk > 0");

            setLabel(lblTotalAnomalies, String.valueOf(total));
            setLabel(lblHighRiskUsers,  String.valueOf(critical));
            setLabel(lblActiveAlerts,   String.valueOf(pending));
            setLabel(lblAccuracy,       String.format("%.0f%%", avgRisk));
            setLabel(lblAnomalySubtitle,  resolved + " resolved, " + (total - resolved) + " active");
            setLabel(lblRiskSubtitle,     critical + " users with severity >= 80");
            setLabel(lblAlertSubtitle,    pending + " awaiting admin review");
            setLabel(lblAccuracySubtitle, "average dropout risk score");

            // Anomalies
            List<Map<String, Object>> anomalyRows = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT * FROM health_anomalies ORDER BY severity DESC, detected_at DESC LIMIT 50")) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    double sev = rs.getDouble("severity");
                    String type = rs.getString("type");
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("user_name",  userName(uid));
                    row.put("user_email", userEmail(uid));
                    row.put("issue",      formatIssue(type));
                    row.put("level",      severityLevel(sev));
                    row.put("score",      String.format("%.0f / 100", sev));
                    row.put("detected",   rs.getTimestamp("detected_at") != null
                        ? rs.getTimestamp("detected_at").toLocalDateTime().format(dtFmt) : "");
                    row.put("status",     rs.getBoolean("resolved") ? "Resolved" : "Active");
                    row.put("action",     recommendedAction(type, sev));
                    anomalyRows.add(row);
                }
            }
            if (tableAnomalies != null) tableAnomalies.setItems(FXCollections.observableArrayList(anomalyRows));
            setLabel(lblAnomalyCount, anomalyRows.size() + " records");

            // High risk users
            List<Map<String, Object>> riskRows = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT user_id, abandonment_risk, days_since_last_log, weight_change_7days " +
                     "FROM user_health_metrics WHERE abandonment_risk > 40 " +
                     "ORDER BY abandonment_risk DESC LIMIT 30")) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    double risk = rs.getDouble("abandonment_risk");
                    int days = rs.getInt("days_since_last_log");
                    double wc = rs.getDouble("weight_change_7days");
                    String trend = wc < -2 ? "Rapid loss (" + String.format("%.1f",wc) + " kg)"
                        : wc > 2 ? "Rapid gain (+" + String.format("%.1f",wc) + " kg)"
                        : wc < 0 ? "Slight loss (" + String.format("%.1f",wc) + " kg)"
                        : wc > 0 ? "Slight gain (+" + String.format("%.1f",wc) + " kg)" : "Stable";
                    String priority = risk >= 80 ? "Urgent" : risk >= 65 ? "High Priority" : risk >= 50 ? "Medium Priority" : "Monitor";
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("user_name",    userName(uid));
                    row.put("user_email",   userEmail(uid));
                    row.put("risk_score",   String.format("%.1f%%", risk));
                    row.put("inactive_days", days + " days");
                    row.put("last_active",  LocalDate.now().minusDays(days).format(dateFmt));
                    row.put("weight_trend", trend);
                    row.put("priority",     priority);
                    riskRows.add(row);
                }
            }
            if (tableHighRisk != null) tableHighRisk.setItems(FXCollections.observableArrayList(riskRows));
            setLabel(lblRiskCount, riskRows.size() + " users");

            // Alerts
            List<Map<String, Object>> alertRows = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT user_id, title, priority, risk_score, recommendation, created_at " +
                     "FROM health_alerts WHERE acknowledged = FALSE " +
                     "ORDER BY risk_score DESC LIMIT 30")) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("user_name",     userName(uid));
                    row.put("title",         rs.getString("title"));
                    row.put("priority",      rs.getString("priority"));
                    row.put("risk_score",    String.format("%.0f%%", rs.getDouble("risk_score")));
                    row.put("recommendation", rs.getString("recommendation"));
                    row.put("created",       rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime().format(dtFmt) : "");
                    alertRows.add(row);
                }
            }
            if (tableAlerts != null) tableAlerts.setItems(FXCollections.observableArrayList(alertRows));
            setLabel(lblAlertCount, alertRows.size() + " pending");
            setLabel(lblSystemStatus, "Operational");

        } catch (Exception e) {
            System.err.println("[Dashboard] Load error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatIssue(String t) {
        if (t == null) return "Unknown";
        return switch (t) {
            case "RAPID_WEIGHT_LOSS"    -> "Rapid weight loss";
            case "RAPID_WEIGHT_GAIN"    -> "Rapid weight gain";
            case "PROLONGED_INACTIVITY" -> "Prolonged inactivity";
            case "YO_YO_PATTERN"        -> "Yo-yo weight pattern";
            case "UNREALISTIC_GOAL"     -> "Unrealistic goal";
            case "ABANDONMENT_RISK"     -> "High dropout risk";
            case "ABNORMAL_BEHAVIOR"    -> "Abnormal activity";
            default -> t.replace("_"," ").toLowerCase();
        };
    }

    private String severityLevel(double s) {
        return s >= 80 ? "CRITICAL" : s >= 60 ? "HIGH" : s >= 40 ? "MEDIUM" : "LOW";
    }

    private String recommendedAction(String t, double s) {
        if (s >= 80) return "Contact immediately";
        if (t == null) return "Monitor closely";
        return switch (t) {
            case "RAPID_WEIGHT_LOSS","RAPID_WEIGHT_GAIN" -> "Nutritionist review";
            case "PROLONGED_INACTIVITY" -> "Send re-engagement";
            case "ABANDONMENT_RISK"     -> "Personal follow-up";
            case "YO_YO_PATTERN"        -> "Stabilization plan";
            case "UNREALISTIC_GOAL"     -> "Goal adjustment";
            default -> "Monitor closely";
        };
    }

    private int    queryInt(Connection c, String sql)    { try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { return r.next() ? r.getInt(1) : 0; } catch (Exception e) { return 0; } }
    private double queryDouble(Connection c, String sql) { try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { return r.next() ? r.getDouble(1) : 0.0; } catch (Exception e) { return 0.0; } }

    private javafx.beans.property.SimpleStringProperty prop(Map<String, Object> m, String k) {
        Object v = m.get(k); return new javafx.beans.property.SimpleStringProperty(v != null ? v.toString() : "");
    }

    private void setLabel(Label l, String t) { if (l != null) l.setText(t); }
    private void updateLastUpdateTime() { setLabel(lblLastUpdate, "Updated: " + LocalDateTime.now().format(dtFmt)); }

    // ── Toast notification (no blocking popup) ────────────────────────────────
    private javafx.animation.Timeline toastTimeline;

    private void showToast(boolean success, String message, String detail) {
        if (toastBar == null) return;
        if (toastTimeline != null) toastTimeline.stop();

        toastIcon.setText(success ? "✓" : "✕");
        toastIcon.setStyle("-fx-font-size:16px; -fx-text-fill:" + (success ? "#4ADE80" : "#F87171") + ";");
        toastBar.setStyle("-fx-background-color:" + (success ? "#1E293B" : "#450A0A") +
                ";-fx-background-radius:8;-fx-padding:12 20;");
        toastMessage.setText(message);
        toastDetail.setText(detail != null ? detail : "");
        toastBar.setVisible(true);
        toastBar.setManaged(true);

        // Auto-hide after 4 seconds
        toastTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), e -> {
                toastBar.setVisible(false);
                toastBar.setManaged(false);
            })
        );
        toastTimeline.play();
    }

    @FXML
    private void handleRunDetection() {
        if (progressIndicator != null) progressIndicator.setVisible(true);
        if (btnRunDetection != null) { btnRunDetection.setDisable(true); btnRunDetection.setText("⏳  Scanning..."); }
        setLabel(lblSystemStatus, "Scanning...");

        new Thread(() -> {
            long t0 = System.currentTimeMillis();
            int found = 0, scanned = 0;
            AnomalyDetectionService svc = new AnomalyDetectionService();
            // Only scan users that actually exist in the DB
            List<Integer> userIds = getRealUserIds();
            for (int uid : userIds) {
                try {
                    List<HealthAnomaly> list = svc.detectAnomalies(uid);
                    for (HealthAnomaly a : list) { svc.saveAnomaly(a); found++; }
                    for (HealthAlert al : svc.generateAlerts(list)) svc.saveAlert(al);
                    scanned++;
                } catch (Exception ignored) {}
            }
            long ms = System.currentTimeMillis() - t0;
            final int ff = found, ss = scanned;
            javafx.application.Platform.runLater(() -> {
                if (progressIndicator != null) progressIndicator.setVisible(false);
                if (btnRunDetection != null) { btnRunDetection.setDisable(false); btnRunDetection.setText("▶  Run Analysis"); }
                loadDashboardData();
                updateLastUpdateTime();
                setLabel(lblSystemStatus, "Operational");

                // Professional result message
                String title, detail;
                if (ff == 0) {
                    title = "✅ All users are healthy — no new anomalies detected";
                    detail = ss + " users scanned in " + ms + " ms • System is up to date";
                } else if (ff <= 3) {
                    title = ff + " new anomaly" + (ff > 1 ? "ies" : "") + " detected — review recommended";
                    detail = ss + " users scanned in " + ms + " ms • Check the tables below";
                } else {
                    title = "⚠ " + ff + " new anomalies detected — immediate attention required";
                    detail = ss + " users scanned in " + ms + " ms • Prioritize CRITICAL cases";
                }
                showToast(ff == 0, title, detail);
            });
        }).start();
    }

    private List<Integer> getRealUserIds() {
        List<Integer> ids = new ArrayList<>();
        try (Statement st = MyBDConnexion.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM `user` ORDER BY id")) {
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (Exception e) {
            // fallback range
            for (int i = 1; i <= 30; i++) ids.add(i);
        }
        return ids;
    }

    @FXML
    private void handleRefresh() {
        if (btnRefresh != null) { btnRefresh.setDisable(true); btnRefresh.setText("⏳  Refreshing..."); }

        new Thread(() -> {
            // Reload user cache in case new users were added
            loadUserCache();
            javafx.application.Platform.runLater(() -> {
                loadDashboardData();
                updateLastUpdateTime();
                if (btnRefresh != null) { btnRefresh.setDisable(false); btnRefresh.setText("↺  Refresh"); }
                showToast(true, "Dashboard refreshed", "All data reloaded from database");
            });
        }).start();
    }

    @FXML
    private void handleExport() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Save Health Report");
        fc.setInitialFileName("NutriLife_Health_Report_" + LocalDate.now() + ".csv");
        fc.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists()) fc.setInitialDirectory(desktop);

        javafx.stage.Window window = null;
        if (btnExport != null && btnExport.getScene() != null)
            window = btnExport.getScene().getWindow();

        File file = fc.showSaveDialog(window);
        if (file == null) return;

        // Use semicolon separator — Excel (French/European locale) uses ; not ,
        final String SEP = ";";

        try (java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {

            // UTF-8 BOM — required for Excel to detect encoding correctly
            osw.write('\uFEFF');

            PrintWriter pw = new PrintWriter(osw);

            // ── Title block ───────────────────────────────────────────────
            pw.println("NutriLife" + SEP + "Health Monitoring Report");
            pw.println("Generated" + SEP + LocalDateTime.now().format(dtFmt));
            pw.println(SEP);

            // ── Section 1: Anomalies ──────────────────────────────────────
            pw.println("DETECTED ANOMALIES");
            pw.println(String.join(SEP, "User", "Email", "Issue Detected",
                "Severity Level", "Score (/100)", "Detected On", "Status", "Recommended Action"));
            if (tableAnomalies != null) {
                for (Map<String, Object> r : tableAnomalies.getItems()) {
                    pw.println(String.join(SEP,
                        q(safe(r,"user_name")), q(safe(r,"user_email")), q(safe(r,"issue")),
                        q(safe(r,"level")),
                        q(safe(r,"score").replace(" / 100","").trim()),
                        q(safe(r,"detected")), q(safe(r,"status")), q(safe(r,"action"))));
                }
            }
            pw.println(SEP);

            // ── Section 2: High risk users ────────────────────────────────
            pw.println("USERS REQUIRING ATTENTION");
            pw.println(String.join(SEP, "User", "Email", "Dropout Risk",
                "Days Inactive", "Last Activity", "Weight Trend", "Priority"));
            if (tableHighRisk != null) {
                for (Map<String, Object> r : tableHighRisk.getItems()) {
                    pw.println(String.join(SEP,
                        q(safe(r,"user_name")), q(safe(r,"user_email")),
                        q(safe(r,"risk_score").replace(",",".")),
                        q(safe(r,"inactive_days")), q(safe(r,"last_active")),
                        q(safe(r,"weight_trend")), q(safe(r,"priority"))));
                }
            }
            pw.println(SEP);

            // ── Section 3: Pending alerts ─────────────────────────────────
            pw.println("PENDING ALERTS");
            pw.println(String.join(SEP, "User", "Alert", "Priority",
                "Risk Score", "Recommended Action", "Date"));
            if (tableAlerts != null) {
                for (Map<String, Object> r : tableAlerts.getItems()) {
                    pw.println(String.join(SEP,
                        q(safe(r,"user_name")), q(safe(r,"title")), q(safe(r,"priority")),
                        q(safe(r,"risk_score").replace(",",".")),
                        q(safe(r,"recommendation")), q(safe(r,"created"))));
                }
            }

            pw.flush();
            showToast(true, "Report exported successfully", file.getName() + " saved to " + file.getParent());

        } catch (Exception e) {
            showToast(false, "Export failed", e.getMessage());
            System.err.println("[Dashboard] Export error: " + e.getMessage());
        }
    }

    /** Wrap value in double-quotes, escaping any internal double-quotes */
    private String q(String v) {
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private String safe(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : "";
    }

    private void showAlert(String title, String msg) {
        AlertUtil.show(AlertUtil.Type.INFO, title, msg);
    }
}