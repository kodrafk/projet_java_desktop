package tn.esprit.projet.services;

import tn.esprit.projet.models.Additive;
import tn.esprit.projet.utils.MyBDConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class INCIService {

    private Connection cnx;

    public INCIService() {
        cnx = MyBDConnexion.getInstance().getCnx();
    }

    public Additive getAdditiveByCode(String code) {
        String sql = "SELECT * FROM additives_danger WHERE code = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, code.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Additive a = new Additive();
                a.setId(rs.getInt("id"));
                a.setCode(rs.getString("code"));
                a.setName(rs.getString("name"));
                a.setDangerLevel(rs.getInt("danger_level"));
                a.setDescription(rs.getString("description"));
                a.setHealthEffects(rs.getString("health_effects"));
                return a;
            }
        } catch (SQLException e) {
            System.out.println("Erreur INCIService: " + e.getMessage());
        }
        return null;
    }

    public List<Additive> analyzeAdditives(List<String> codes) {
        List<Additive> result = new ArrayList<>();
        for (String code : codes) {
            Additive a = getAdditiveByCode(code);
            if (a != null) result.add(a);
        }
        return result;
    }

    public int calculateInciScore(List<Additive> additives) {
        if (additives.isEmpty()) return 100;

        double total = 0;
        boolean hasCritical = false;
        for (Additive a : additives) {
            total += a.getDangerLevel();
            if (a.getDangerLevel() >= 9) hasCritical = true;
        }

        int score = (int) (100 - (total / additives.size() * 10));
        if (hasCritical && score > 50) score = 50;

        return score;
    }
}