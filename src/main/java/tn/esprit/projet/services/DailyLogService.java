package tn.esprit.projet.services;

import tn.esprit.projet.models.DailyLog;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyLogService {

    private final Connection cnx;

    public DailyLogService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
    }

    public List<DailyLog> getByObjectiveId(int objectiveId) {
        List<DailyLog> list = new ArrayList<>();
        String sql = "SELECT * FROM daily_log WHERE nutrition_objective_id = ? ORDER BY day_number";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, objectiveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return list;
    }

    public DailyLog getById(int id) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM daily_log WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching log: " + e.getMessage());
        }
        return null;
    }

    public void save(DailyLog log) {
        String sql = """
            INSERT INTO daily_log
            (nutrition_objective_id, day_number, date, completed,
             calories_consumed, protein_consumed, carbs_consumed, fats_consumed,
             water_consumed, mood, notes, meals, selected_foods, custom_foods)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NULL,NULL)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getNutritionObjectiveId());
            ps.setInt(2, log.getDayNumber());
            ps.setObject(3, log.getDate() != null
                    ? java.sql.Date.valueOf(log.getDate()) : null);
            ps.setBoolean(4, log.isCompleted());
            ps.setObject(5, log.getCaloriesConsumed() > 0 ? log.getCaloriesConsumed() : null);
            ps.setObject(6, log.getProteinConsumed() > 0 ? log.getProteinConsumed() : null);
            ps.setObject(7, log.getCarbsConsumed() > 0 ? log.getCarbsConsumed() : null);
            ps.setObject(8, log.getFatsConsumed() > 0 ? log.getFatsConsumed() : null);
            ps.setObject(9, log.getWaterConsumed() > 0 ? log.getWaterConsumed() : null);
            ps.setString(10, log.getMood());
            ps.setString(11, log.getNotes());
            ps.setString(12, log.getMealsJson());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) log.setId(keys.getInt(1));
        } catch (SQLException e) {
            System.err.println("Error saving log: " + e.getMessage());
        }
    }

    public void update(DailyLog log) {
        String sql = """
            UPDATE daily_log SET
            completed=?, calories_consumed=?, protein_consumed=?, carbs_consumed=?,
            fats_consumed=?, water_consumed=?, mood=?, notes=?, meals=?
            WHERE id=?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, log.isCompleted());
            ps.setObject(2, log.getCaloriesConsumed() > 0 ? log.getCaloriesConsumed() : null);
            ps.setObject(3, log.getProteinConsumed() > 0 ? log.getProteinConsumed() : null);
            ps.setObject(4, log.getCarbsConsumed() > 0 ? log.getCarbsConsumed() : null);
            ps.setObject(5, log.getFatsConsumed() > 0 ? log.getFatsConsumed() : null);
            ps.setObject(6, log.getWaterConsumed() > 0 ? log.getWaterConsumed() : null);
            ps.setString(7, log.getMood());
            ps.setString(8, log.getNotes());
            ps.setString(9, log.getMealsJson());
            ps.setInt(10, log.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating log: " + e.getMessage());
        }
    }

    public void createLogsForObjective(int objectiveId, LocalDate startDate) {
        for (int i = 0; i < 7; i++) {
            DailyLog log = new DailyLog();
            log.setNutritionObjectiveId(objectiveId);
            log.setDayNumber(i + 1);
            log.setDate(startDate.plusDays(i));
            save(log);
        }
        System.out.println("✅ Created 7 daily logs for objective " + objectiveId);
    }

    public void deleteByObjectiveId(int objectiveId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "DELETE FROM daily_log WHERE nutrition_objective_id = ?")) {
            ps.setInt(1, objectiveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting logs: " + e.getMessage());
        }
    }

    private DailyLog map(ResultSet rs) throws SQLException {
        DailyLog log = new DailyLog();
        log.setId(rs.getInt("id"));
        log.setNutritionObjectiveId(rs.getInt("nutrition_objective_id"));
        log.setDayNumber(rs.getInt("day_number"));
        Date d = rs.getDate("date");
        if (d != null) log.setDate(d.toLocalDate());
        log.setCompleted(rs.getBoolean("completed"));
        // Read DB columns as source of truth for totals
        int dbCal    = rs.getInt("calories_consumed");
        double dbPro = rs.getDouble("protein_consumed");
        double dbCarb = rs.getDouble("carbs_consumed");
        double dbFat  = rs.getDouble("fats_consumed");
        log.setWaterConsumed(rs.getDouble("water_consumed"));
        log.setMood(rs.getString("mood"));
        log.setNotes(rs.getString("notes"));
        // Parse meals JSON (may recalculate totals from meal breakdown)
        try {
            String mealsJson = rs.getString("meals");
            if (mealsJson != null && !mealsJson.isBlank()) {
                log.parseMealsJson(mealsJson);
            }
        } catch (SQLException ignored) {}
        // Always override totals with the authoritative DB columns
        if (dbCal > 0)  log.setCaloriesConsumed(dbCal);
        if (dbPro > 0)  log.setProteinConsumed(dbPro);
        if (dbCarb > 0) log.setCarbsConsumed(dbCarb);
        if (dbFat > 0)  log.setFatsConsumed(dbFat);
        return log;
    }
}
