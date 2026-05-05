package tn.esprit.projet.repository;

import tn.esprit.projet.models.WeightLog;
import tn.esprit.projet.models.WeightObjective;
import tn.esprit.projet.utils.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WeightRepository {

    private static boolean schemaInitialized = false;

    /** Call once at app startup to guarantee all tables exist. Never throws. */
    public static void initSchema() {
        if (schemaInitialized) return;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection c = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/nutrilife_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    DatabaseConfig.USER, DatabaseConfig.PASSWORD);
                 Statement st = c.createStatement()) {

                // Disable FK checks so order doesn't matter
                st.executeUpdate("SET FOREIGN_KEY_CHECKS=0");

                // weight_log — check if it has the right columns, recreate if not
                boolean wlOk = false;
                try (ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='weight_log' AND COLUMN_NAME='photo'")) {
                    wlOk = rs.next() && rs.getInt(1) > 0;
                }
                if (!wlOk) {
                    st.executeUpdate("DROP TABLE IF EXISTS `weight_log`");
                    st.executeUpdate(
                        "CREATE TABLE `weight_log` (" +
                        "  `id`        INT AUTO_INCREMENT PRIMARY KEY," +
                        "  `user_id`   INT NOT NULL," +
                        "  `weight`    DOUBLE NOT NULL," +
                        "  `photo`     VARCHAR(255) DEFAULT NULL," +
                        "  `note`      VARCHAR(255) DEFAULT NULL," +
                        "  `logged_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                    System.out.println("[WeightRepo] ✅ weight_log created");
                }

                // weight_objective — check if it has start_weight, recreate if not
                boolean woOk = false;
                try (ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='weight_objective' AND COLUMN_NAME='start_weight'")) {
                    woOk = rs.next() && rs.getInt(1) > 0;
                }
                if (!woOk) {
                    st.executeUpdate("DROP TABLE IF EXISTS `weight_objective`");
                    st.executeUpdate(
                        "CREATE TABLE `weight_objective` (" +
                        "  `id`            INT AUTO_INCREMENT PRIMARY KEY," +
                        "  `user_id`       INT NOT NULL UNIQUE," +
                        "  `start_weight`  DOUBLE NOT NULL," +
                        "  `target_weight` DOUBLE NOT NULL," +
                        "  `start_date`    DATE NOT NULL," +
                        "  `target_date`   DATE NOT NULL," +
                        "  `start_photo`   VARCHAR(255) DEFAULT NULL," +
                        "  `is_active`     TINYINT(1) NOT NULL DEFAULT 1," +
                        "  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                    System.out.println("[WeightRepo] ✅ weight_objective created");
                }

                // progress_photo and message — safe to use IF NOT EXISTS
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `progress_photo` (" +
                    "  `id`         INT AUTO_INCREMENT PRIMARY KEY," +
                    "  `user_id`    INT NOT NULL," +
                    "  `filename`   VARCHAR(255) NOT NULL," +
                    "  `caption`    TEXT DEFAULT NULL," +
                    "  `weight`     DOUBLE DEFAULT NULL," +
                    "  `taken_at`   DATETIME DEFAULT NULL," +
                    "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `message` (" +
                    "  `id`          INT AUTO_INCREMENT PRIMARY KEY," +
                    "  `sender_id`   INT NOT NULL," +
                    "  `receiver_id` INT NOT NULL," +
                    "  `content`     TEXT NOT NULL," +
                    "  `is_read`     TINYINT(1) NOT NULL DEFAULT 0," +
                    "  `sent_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  `read_at`     DATETIME DEFAULT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

                st.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
                schemaInitialized = true;
                System.out.println("[WeightRepo] ✅ Schema ready");

                // Ensure gallery_access_enabled column exists on user table
                try {
                    st.executeUpdate(
                        "ALTER TABLE `user` ADD COLUMN `gallery_access_enabled` TINYINT(1) NOT NULL DEFAULT 0");
                    System.out.println("[WeightRepo] ✅ gallery_access_enabled column added");
                } catch (SQLException colEx) {
                    // Column already exists — ignore
                }

                // Ensure phone column exists on user table
                try {
                    st.executeUpdate(
                        "ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL");
                    System.out.println("[WeightRepo] ✅ phone column added");
                } catch (SQLException colEx) {
                    // Column already exists — ignore
                }
            }
        } catch (Exception e) {
            // Log but never crash — tables may already exist from a previous run
            System.err.println("[WeightRepo] initSchema warning: " + e.getMessage());
        }
    }

    private Connection conn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection c = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/nutrilife_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                DatabaseConfig.USER, DatabaseConfig.PASSWORD);
            c.setAutoCommit(true);
            return c;
        } catch (Exception e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    // ── Weight Objective ───────────────────────────────────────────────────────

    public WeightObjective findObjectiveByUser(int userId) {
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM weight_objective WHERE user_id=? AND is_active=1 LIMIT 1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapObjective(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void saveObjective(WeightObjective obj) {
        // Deactivate previous objectives
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(
                "UPDATE weight_objective SET is_active=0 WHERE user_id=?")) {
            ps.setInt(1, obj.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        String sql = "INSERT INTO weight_objective " +
                "(user_id,start_weight,target_weight,start_date,target_date,start_photo,is_active,created_at) " +
                "VALUES (?,?,?,?,?,?,1,NOW())";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, obj.getUserId());
            ps.setDouble(2, obj.getStartWeight());
            ps.setDouble(3, obj.getTargetWeight());
            ps.setDate(4, Date.valueOf(obj.getStartDate()));
            ps.setDate(5, Date.valueOf(obj.getTargetDate()));
            ps.setString(6, obj.getStartPhoto());
            int rows = ps.executeUpdate();
            System.out.println("[WeightRepo] saveObjective rows=" + rows + " user=" + obj.getUserId());
            if (rows == 0) System.err.println("[WeightRepo] WARNING: 0 rows inserted for objective!");
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) obj.setId(keys.getInt(1));
        } catch (SQLException e) {
            System.err.println("[WeightRepo] saveObjective ERROR: " + e.getMessage() + " (SQLState=" + e.getSQLState() + ")");
            e.printStackTrace();
            throw new RuntimeException("saveObjective failed: " + e.getMessage(), e);
        }
    }

    public void updateObjectivePhoto(int objectiveId, String photo) {
        try (Connection c = conn(); Statement st = c.createStatement()) {
            st.executeUpdate("UPDATE weight_objective SET start_photo='" + photo + "' WHERE id=" + objectiveId);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<WeightLog> findLogsByUser(int userId) {
        List<WeightLog> list = new ArrayList<>();
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM weight_log WHERE user_id=? ORDER BY logged_at ASC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapLog(rs));
            System.out.println("[WeightRepo] findLogsByUser(" + userId + ") = " + list.size() + " rows");
        } catch (SQLException e) {
            System.err.println("[WeightRepo] findLogsByUser ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public WeightLog findLatestLog(int userId) {
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM weight_log WHERE user_id=? ORDER BY logged_at DESC LIMIT 1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapLog(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void saveLog(WeightLog log) {
        String sql = "INSERT INTO weight_log (user_id,weight,photo,note,logged_at) VALUES (?,?,?,?,NOW())";
        try (Connection c = conn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getUserId());
            ps.setDouble(2, log.getWeight());
            ps.setString(3, log.getPhoto());
            ps.setString(4, log.getNote());
            int rows = ps.executeUpdate();
            System.out.println("[WeightRepo] saveLog rows=" + rows + " user=" + log.getUserId() + " weight=" + log.getWeight());
            if (rows == 0) System.err.println("[WeightRepo] WARNING: 0 rows inserted! Check table columns.");
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                log.setId(keys.getInt(1));
                System.out.println("[WeightRepo] saveLog new ID: " + log.getId());
            }
        } catch (SQLException e) {
            System.err.println("[WeightRepo] saveLog ERROR: " + e.getMessage() + " (SQLState=" + e.getSQLState() + ")");
            e.printStackTrace();
            throw new RuntimeException("saveLog failed: " + e.getMessage(), e);
        }
    }

    public void deleteLog(int logId) {
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(
                "DELETE FROM weight_log WHERE id=?")) {
            ps.setInt(1, logId);
            ps.executeUpdate();
            System.out.println("[WeightRepo] deleted log id=" + logId);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private WeightObjective mapObjective(ResultSet rs) throws SQLException {
        WeightObjective o = new WeightObjective();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setStartWeight(rs.getDouble("start_weight"));
        o.setTargetWeight(rs.getDouble("target_weight"));
        Date sd = rs.getDate("start_date");
        if (sd != null) o.setStartDate(sd.toLocalDate());
        Date td = rs.getDate("target_date");
        if (td != null) o.setTargetDate(td.toLocalDate());
        o.setStartPhoto(rs.getString("start_photo"));
        o.setActive(rs.getBoolean("is_active"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) o.setCreatedAt(ca.toLocalDateTime());
        return o;
    }

    private WeightLog mapLog(ResultSet rs) throws SQLException {
        WeightLog l = new WeightLog();
        l.setId(rs.getInt("id"));
        l.setUserId(rs.getInt("user_id"));
        l.setWeight(rs.getDouble("weight"));
        l.setPhoto(rs.getString("photo"));
        l.setNote(rs.getString("note"));
        Timestamp la = rs.getTimestamp("logged_at");
        if (la != null) l.setLoggedAt(la.toLocalDateTime());
        return l;
    }

    private void exec(String sql) {
        try (Connection c = conn(); Statement st = c.createStatement()) { st.executeUpdate(sql); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}
