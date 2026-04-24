package tn.esprit.projet.services;

import tn.esprit.projet.models.MealPlan;
import tn.esprit.projet.models.MealPlanItem;
import tn.esprit.projet.models.RecetteInfoPlus;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MealPlannerService {

    private final Connection cnx;
    private final MealPlanService mealPlanService;

    public MealPlannerService() {
        cnx             = MyBDConnexion.getInstance().getCnx();
        mealPlanService = new MealPlanService();
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 1 — Analyser le stock → Map<ingredientId, urgence>
    // ═════════════════════════════════════════════════════════
    public Map<Integer, MealPlanItem.UrgenceNiveau> analyserStock() {
        Map<Integer, MealPlanItem.UrgenceNiveau> stockMap = new HashMap<>();

        String sql = "SELECT id, date_peremption FROM ingredient";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                int id = rs.getInt("id");

                // ✅ java.sql.Date directement → a toLocalDate()
                java.sql.Date dp = rs.getDate("date_peremption");

                if (dp == null) {
                    stockMap.put(id, MealPlanItem.UrgenceNiveau.OK);
                    continue;
                }

                LocalDate datePeremption = dp.toLocalDate();
                long joursRestants = today.until(datePeremption).getDays();

                if      (joursRestants <= 2) stockMap.put(id, MealPlanItem.UrgenceNiveau.URGENT);
                else if (joursRestants <= 5) stockMap.put(id, MealPlanItem.UrgenceNiveau.BIENTOT);
                else                         stockMap.put(id, MealPlanItem.UrgenceNiveau.OK);
            }

            System.out.println("✅ Stock analysé → " + stockMap.size() + " ingrédients");

        } catch (SQLException e) {
            System.err.println("❌ analyserStock : " + e.getMessage());
        }
        return stockMap;
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 2 — Calcul besoin calorique (Harris-Benedict)
    // ═════════════════════════════════════════════════════════
    public double calculerBesoinCalorique(int userId,
                                          MealPlan.Objectif objectif) {
        String sql = "SELECT poids, taille, age, genre FROM user WHERE id = ?";
        double bmr = 1800; // valeur par défaut

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double poids  = rs.getDouble("poids");
                double taille = rs.getDouble("taille");
                int    age    = rs.getInt   ("age");
                String genre  = rs.getString("genre");

                if ("homme".equalsIgnoreCase(genre) ||
                        "male".equalsIgnoreCase(genre)) {
                    bmr = 88.36 + (13.4 * poids)
                            + (4.8  * taille)
                            - (5.7  * age);
                } else {
                    bmr = 447.6 + (9.2  * poids)
                            + (3.1  * taille)
                            - (4.3  * age);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ calculerBesoinCalorique : " + e.getMessage());
        }

        // Ajustement objectif
        switch (objectif) {
            case PERTE_POIDS: return bmr * 0.80;
            case PRISE_MASSE: return bmr * 1.20;
            default:          return bmr * 1.00;
        }
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 3 — Filtrer recettes compatibles
    // ═════════════════════════════════════════════════════════
    // Dans filtrerRecettes() — modifier le SELECT
    public List<Map<String, Object>> filtrerRecettes(
            MealPlan.Regime             regime,
            MealPlan                    allergies,
            RecetteInfoPlus.MomentRepas moment) {

        List<Map<String, Object>> recettes = new ArrayList<>();

        // ✅ CORRIGÉ : ajout r.image
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.nom, r.image, rip.calories " +
                        "FROM recette r " +
                        "JOIN recette_info_plus rip ON r.id = rip.recette_id " +
                        "WHERE rip.moment_repas = ?"
        );

        switch (regime) {
            case VEGETARIEN -> sql.append(" AND rip.is_vegetarien = 1");
            case VEGAN      -> sql.append(" AND rip.is_vegan = 1");
            case HALAL      -> sql.append(" AND rip.is_halal = 1");
            default         -> {}
        }

        if (allergies.isAllergieLactose()) sql.append(" AND rip.contains_lactose = 0");
        if (allergies.isAllergieGluten())  sql.append(" AND rip.contains_gluten = 0");
        if (allergies.isAllergieNuts())    sql.append(" AND rip.contains_nuts = 0");
        if (allergies.isAllergieEggs())    sql.append(" AND rip.contains_eggs = 0");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            ps.setString(1, moment.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id",       rs.getInt   ("id"));
                row.put("nom",      rs.getString("nom"));
                row.put("images",    rs.getString("images"));   // 🆕
                row.put("calories", rs.getInt   ("calories"));
                recettes.add(row);
            }

        } catch (SQLException e) {
            System.err.println("❌ filtrerRecettes : " + e.getMessage());
        }

        return recettes;
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 4 — Score Anti-Gaspillage
    // ═════════════════════════════════════════════════════════
    public double calculerScoreAntiGaspillage(
            int recetteId,
            Map<Integer, MealPlanItem.UrgenceNiveau> stockMap) {

        double score = 0;
        int    count = 0;

        String sql = "SELECT ingredient_id " +
                "FROM recette_ingredient " +
                "WHERE recette_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recetteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int ingredientId = rs.getInt("ingredient_id");
                MealPlanItem.UrgenceNiveau urgence =
                        stockMap.getOrDefault(ingredientId, null);

                if (urgence != null) {
                    switch (urgence) {
                        case URGENT  -> score += 3;
                        case BIENTOT -> score += 2;
                        case OK      -> score += 1;
                    }
                }
                count++;
            }

        } catch (SQLException e) {
            System.err.println("❌ calculerScoreAntiGaspillage : " + e.getMessage());
        }

        if (count == 0) return 0;

        // Normaliser sur 100 (max possible = count × 3)
        return (score / (count * 3.0)) * 100;
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 5 — Score Nutritionnel
    // ═════════════════════════════════════════════════════════
    public double calculerScoreNutritionnel(
            int    caloriesRecette,
            double caloriesJournalieres,
            RecetteInfoPlus.MomentRepas moment) {

        // Cible par moment
        double cible;
        switch (moment) {
            case PETIT_DEJEUNER -> cible = caloriesJournalieres * 0.25;
            case DEJEUNER       -> cible = caloriesJournalieres * 0.40;
            case DINER          -> cible = caloriesJournalieres * 0.35;
            default             -> cible = caloriesJournalieres * 0.33;
        }

        if (cible == 0) return 0;

        // Score = 100 - écart relatif en %
        double ecart = Math.abs(caloriesRecette - cible) / cible * 100;
        return Math.max(0, 100 - ecart);
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 6 — Score Final
    // ═════════════════════════════════════════════════════════
    public double calculerScoreFinal(double scoreGaspillage,
                                     double scoreNutrition) {
        return (scoreGaspillage * 0.60) + (scoreNutrition * 0.40);
    }

    // ═════════════════════════════════════════════════════════
    // ÉTAPE 7 — Sélectionner 21 repas (7j × 3)
    // ═════════════════════════════════════════════════════════
    public List<MealPlanItem> selectionner21Repas(
            MealPlan.Regime   regime,
            MealPlan          allergies,
            MealPlan.Objectif objectif,
            int               userId) {

        List<MealPlanItem> plan = new ArrayList<>();
        Map<Integer, MealPlanItem.UrgenceNiveau> stock = analyserStock();
        double caloriesJournalieres = calculerBesoinCalorique(userId, objectif);
        Set<Integer> dejUtilises    = new HashSet<>();
        Random       random         = new Random();

        MealPlanItem.JourNom[]        jours   = MealPlanItem.JourNom.values();
        RecetteInfoPlus.MomentRepas[] moments = RecetteInfoPlus.MomentRepas.values();

        for (MealPlanItem.JourNom jour : jours) {
            for (RecetteInfoPlus.MomentRepas moment : moments) {

                // 1. Filtrer recettes compatibles
                List<Map<String, Object>> recettes =
                        filtrerRecettes(regime, allergies, moment);

                // 2. Calculer score final pour chaque recette
                for (Map<String, Object> r : recettes) {
                    int id  = (int) r.get("id");
                    int cal = (int) r.get("calories");

                    double sg = calculerScoreAntiGaspillage(id, stock);
                    double sn = calculerScoreNutritionnel(cal,
                            caloriesJournalieres, moment);
                    double sf = calculerScoreFinal(sg, sn);

                    r.put("score", sf);
                }

                // 3. Trier par score décroissant
                recettes.sort((a, b) ->
                        Double.compare(
                                (double) b.get("score"),
                                (double) a.get("score")));

                // 4. Prendre TOP 3 non encore utilisées
                List<Map<String, Object>> top3 = new ArrayList<>();
                for (Map<String, Object> r : recettes) {
                    if (!dejUtilises.contains((int) r.get("id"))) {
                        top3.add(r);
                        if (top3.size() == 3) break;
                    }
                }

                // Si top3 vide → prendre première disponible
                if (top3.isEmpty() && !recettes.isEmpty()) {
                    top3.add(recettes.get(0));
                }

                // Si toujours vide → passer ce créneau
                if (top3.isEmpty()) continue;

                // 5. Choisir aléatoirement parmi TOP 3
                Map<String, Object> choisi =
                        top3.get(random.nextInt(top3.size()));

                int    recetteId  = (int)    choisi.get("id");
                String recetteNom = (String) choisi.get("nom");
                int    calories   = (int)    choisi.get("calories");

                dejUtilises.add(recetteId);

                // 6. Déterminer urgence dominante de cette recette
                MealPlanItem.UrgenceNiveau urgence =
                        getUrgenceDominante(recetteId, stock);

                // 7. Créer l'item
                MealPlanItem item = new MealPlanItem(
                        0,
                        jour,
                        MealPlanItem.MomentRepas.valueOf(moment.name()),
                        recetteId,
                        recetteNom,
                        calories,
                        urgence
                );
                plan.add(item);
            }
        }

        System.out.println("✅ Plan généré → " + plan.size() + " repas");
        return plan;
    }

    // ─── Urgence dominante d'une recette ─────────────────────
    private MealPlanItem.UrgenceNiveau getUrgenceDominante(
            int recetteId,
            Map<Integer, MealPlanItem.UrgenceNiveau> stockMap) {

        MealPlanItem.UrgenceNiveau dominant = MealPlanItem.UrgenceNiveau.OK;

        String sql = "SELECT ingredient_id FROM recette_ingredient " +
                "WHERE recette_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recetteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MealPlanItem.UrgenceNiveau u =
                        stockMap.get(rs.getInt("ingredient_id"));

                if (u == null) continue;

                if (u == MealPlanItem.UrgenceNiveau.URGENT)  return u;
                if (u == MealPlanItem.UrgenceNiveau.BIENTOT) dominant = u;
            }

        } catch (SQLException e) {
            System.err.println("❌ getUrgenceDominante : " + e.getMessage());
        }
        return dominant;
    }

    // ═════════════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE — genererPlan()
    // ═════════════════════════════════════════════════════════
    public MealPlan genererPlan(int               userId,
                                MealPlan.Regime   regime,
                                MealPlan.Objectif objectif,
                                boolean           allergieLactose,
                                boolean           allergieGluten,
                                boolean           allergieNuts,
                                boolean           allergieEggs) {

        System.out.println("🤖 Génération du plan pour user=" + userId);

        // 1. Créer le plan
        MealPlan plan = new MealPlan(
                userId, objectif, regime,
                allergieLactose, allergieGluten,
                allergieNuts, allergieEggs);

        // 2. Générer les 21 repas
        List<MealPlanItem> items = selectionner21Repas(
                regime, plan, objectif, userId);

        // 3. Attacher au plan
        plan.setItems(items);

        System.out.println("✅ Plan prêt → " + items.size() + " repas générés");
        return plan;
    }

    // ═════════════════════════════════════════════════════════
    // STATS STOCK pour affichage Screen 1
    // ═════════════════════════════════════════════════════════
    public Map<String, Integer> getStatsStock() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("urgent",  0);
        stats.put("bientot", 0);
        stats.put("ok",      0);

        Map<Integer, MealPlanItem.UrgenceNiveau> stock = analyserStock();

        for (MealPlanItem.UrgenceNiveau u : stock.values()) {
            switch (u) {
                case URGENT  -> stats.merge("urgent",  1, Integer::sum);
                case BIENTOT -> stats.merge("bientot", 1, Integer::sum);
                case OK      -> stats.merge("ok",      1, Integer::sum);
            }
        }
        return stats;
    }

}