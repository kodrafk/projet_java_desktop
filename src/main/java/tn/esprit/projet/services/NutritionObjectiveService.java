package tn.esprit.projet.services;

import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NutritionObjectiveService {

    private final Connection cnx;

    public NutritionObjectiveService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
    }

    public List<NutritionObjective> getAll() {
        List<NutritionObjective> list = new ArrayList<>();
        if (cnx == null) {
            System.err.println("⚠️ No DB connection — returning empty objectives list.");
            return list;
        }
        String sql = "SELECT * FROM nutrition_objective ORDER BY created_at DESC";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching objectives: " + e.getMessage());
        }
        return list;
    }

    public NutritionObjective getById(int id) {
        if (cnx == null) return null;
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM nutrition_objective WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching objective: " + e.getMessage());
        }
        return null;
    }

    public NutritionObjective getActive() {
        if (cnx == null) return null;
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM nutrition_objective WHERE status = 'active' LIMIT 1")) {
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching active: " + e.getMessage());
        }
        return null;
    }

    public void save(NutritionObjective obj) {
        if (cnx == null) {
            System.err.println("❌ Cannot save: no DB connection.");
            return;
        }
        // user_id is NOT NULL in Symfony schema — use a default value of 1
        // or check if there's a user with id=1, otherwise use NULL if allowed
        String sql = """
            INSERT INTO nutrition_objective
            (title, description, goal_type, plan_level, target_calories, target_protein,
             target_carbs, target_fats, target_water, status, planned_start_date,
             start_date, end_date, auto_activate, created_at, updated_at, user_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW(),
                (SELECT id FROM user LIMIT 1))
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getGoalType());
            ps.setString(4, obj.getPlanLevel());
            ps.setInt(5, obj.getTargetCalories());
            ps.setDouble(6, obj.getTargetProtein());
            ps.setDouble(7, obj.getTargetCarbs());
            ps.setDouble(8, obj.getTargetFats());
            ps.setDouble(9, obj.getTargetWater());
            ps.setString(10, obj.getStatus());
            ps.setObject(11, obj.getPlannedStartDate() != null
                    ? java.sql.Date.valueOf(obj.getPlannedStartDate()) : null);
            ps.setObject(12, obj.getStartDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getStartDate().atStartOfDay()) : null);
            ps.setObject(13, obj.getEndDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getEndDate().atStartOfDay()) : null);
            ps.setBoolean(14, obj.isAutoActivate());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                obj.setId(keys.getInt(1));
                System.out.println("✅ Saved objective ID: " + obj.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saving objective: " + e.getMessage());
            // Fallback: try without user_id subquery
            saveFallback(obj);
        }
    }

    private void saveFallback(NutritionObjective obj) {
        // Try to find any user id
        int userId = 1;
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM user LIMIT 1")) {
            if (rs.next()) userId = rs.getInt(1);
        } catch (SQLException ignored) {}

        String sql = """
            INSERT INTO nutrition_objective
            (title, description, goal_type, plan_level, target_calories, target_protein,
             target_carbs, target_fats, target_water, status, planned_start_date,
             start_date, end_date, auto_activate, created_at, updated_at, user_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW(),?)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getGoalType());
            ps.setString(4, obj.getPlanLevel());
            ps.setInt(5, obj.getTargetCalories());
            ps.setDouble(6, obj.getTargetProtein());
            ps.setDouble(7, obj.getTargetCarbs());
            ps.setDouble(8, obj.getTargetFats());
            ps.setDouble(9, obj.getTargetWater());
            ps.setString(10, obj.getStatus());
            ps.setObject(11, obj.getPlannedStartDate() != null
                    ? java.sql.Date.valueOf(obj.getPlannedStartDate()) : null);
            ps.setObject(12, obj.getStartDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getStartDate().atStartOfDay()) : null);
            ps.setObject(13, obj.getEndDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getEndDate().atStartOfDay()) : null);
            ps.setBoolean(14, obj.isAutoActivate());
            ps.setInt(15, userId);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                obj.setId(keys.getInt(1));
                System.out.println("✅ Saved objective ID (fallback): " + obj.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Fallback save failed: " + e.getMessage());
        }
    }

    public void update(NutritionObjective obj) {
        if (cnx == null) { System.err.println("❌ Cannot update: no DB connection."); return; }
        String sql = """
            UPDATE nutrition_objective SET
            title=?, description=?, goal_type=?, plan_level=?, target_calories=?,
            target_protein=?, target_carbs=?, target_fats=?, target_water=?,
            status=?, planned_start_date=?, start_date=?, end_date=?, auto_activate=?,
            updated_at=NOW()
            WHERE id=?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, obj.getTitle());
            ps.setString(2, obj.getDescription());
            ps.setString(3, obj.getGoalType());
            ps.setString(4, obj.getPlanLevel());
            ps.setInt(5, obj.getTargetCalories());
            ps.setDouble(6, obj.getTargetProtein());
            ps.setDouble(7, obj.getTargetCarbs());
            ps.setDouble(8, obj.getTargetFats());
            ps.setDouble(9, obj.getTargetWater());
            ps.setString(10, obj.getStatus());
            ps.setObject(11, obj.getPlannedStartDate() != null
                    ? java.sql.Date.valueOf(obj.getPlannedStartDate()) : null);
            ps.setObject(12, obj.getStartDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getStartDate().atStartOfDay()) : null);
            ps.setObject(13, obj.getEndDate() != null
                    ? java.sql.Timestamp.valueOf(obj.getEndDate().atStartOfDay()) : null);
            ps.setBoolean(14, obj.isAutoActivate());
            ps.setInt(15, obj.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error updating objective: " + e.getMessage());
        }
    }

    public void delete(int id) {
        if (cnx == null) { System.err.println("❌ Cannot delete: no DB connection."); return; }
        // Delete daily logs first (FK constraint)
        try (PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM daily_log WHERE nutrition_objective_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting daily logs: " + e.getMessage());
        }
        try (PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM nutrition_objective WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting objective: " + e.getMessage());
        }
    }

    public void activate(NutritionObjective obj) {
        obj.setStatus("active");
        LocalDate start = obj.getPlannedStartDate() != null
                ? obj.getPlannedStartDate() : LocalDate.now();
        obj.setStartDate(start);
        obj.setEndDate(start.plusDays(6));
        update(obj);
        new DailyLogService().createLogsForObjective(obj.getId(), start);
    }

    public void pause(NutritionObjective obj) {
        obj.setStatus("paused");
        update(obj);
    }

    public void resume(NutritionObjective obj) {
        obj.setStatus("active");
        update(obj);
    }

    private NutritionObjective map(ResultSet rs) throws SQLException {
        NutritionObjective obj = new NutritionObjective();
        obj.setId(rs.getInt("id"));
        obj.setTitle(rs.getString("title"));
        obj.setDescription(rs.getString("description"));
        obj.setGoalType(rs.getString("goal_type"));
        obj.setPlanLevel(rs.getString("plan_level"));
        obj.setTargetCalories(rs.getInt("target_calories"));
        obj.setTargetProtein(rs.getDouble("target_protein"));
        obj.setTargetCarbs(rs.getDouble("target_carbs"));
        obj.setTargetFats(rs.getDouble("target_fats"));
        obj.setTargetWater(rs.getDouble("target_water"));
        obj.setStatus(rs.getString("status"));
        obj.setAutoActivate(rs.getBoolean("auto_activate"));
        try {
            Date planned = rs.getDate("planned_start_date");
            if (planned != null) obj.setPlannedStartDate(planned.toLocalDate());
        } catch (SQLException ignored) {}
        try {
            Timestamp start = rs.getTimestamp("start_date");
            if (start != null) obj.setStartDate(start.toLocalDateTime().toLocalDate());
        } catch (SQLException ignored) {}
        try {
            Timestamp end = rs.getTimestamp("end_date");
            if (end != null) obj.setEndDate(end.toLocalDateTime().toLocalDate());
        } catch (SQLException ignored) {}
        try {
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) obj.setCreatedAt(created.toLocalDateTime().toLocalDate());
        } catch (SQLException ignored) {}
        return obj;
    }
}
