package tn.esprit.projet.test;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.services.MeteoService;
import java.time.LocalDateTime;

/**
 * Test du service météo pour vérifier la détection plein air
 * et la récupération des données météo.
 */
public class TestMeteoService {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TEST METEO SERVICE");
        System.out.println("========================================\n");
        
        MeteoService meteoService = new MeteoService();
        
        // Test 1: Lieu en plein air (stade)
        System.out.println("─── Test 1: STADE RADES (plein air) ───");
        testLieu(meteoService, "STADE RADES", LocalDateTime.of(2026, 4, 26, 10, 0));
        
        // Test 2: Lieu en plein air (parc)
        System.out.println("\n─── Test 2: parc sidi bou said (plein air) ───");
        testLieu(meteoService, "parc sidi bou said", LocalDateTime.of(2026, 4, 27, 14, 0));
        
        // Test 3: Lieu intérieur (gym)
        System.out.println("\n─── Test 3: california gym marsa (intérieur) ───");
        testLieu(meteoService, "california gym marsa", LocalDateTime.of(2026, 4, 28, 9, 0));
        
        // Test 4: Lieu avec préfixe [OUTDOOR]
        System.out.println("\n─── Test 4: [OUTDOOR] Complexe Sportif (plein air explicite) ───");
        testLieu(meteoService, "[OUTDOOR] Complexe Sportif", LocalDateTime.of(2026, 4, 29, 16, 0));
        
        // Test 5: Événement passé récent (dans les 7 derniers jours)
        System.out.println("\n─── Test 5: STADE RADES - événement passé (22 avril) ───");
        testLieu(meteoService, "STADE RADES", LocalDateTime.of(2026, 4, 22, 10, 0));
        
        System.out.println("\n========================================");
        System.out.println("   FIN DES TESTS");
        System.out.println("========================================");
    }
    
    private static void testLieu(MeteoService service, String lieu, LocalDateTime date) {
        System.out.println("Lieu: " + lieu);
        System.out.println("Date: " + date);
        
        // Test détection plein air
        boolean isOutdoor = MeteoService.isOutdoor(lieu);
        System.out.println("Détection plein air: " + (isOutdoor ? "✅ OUI" : "❌ NON"));
        
        if (isOutdoor) {
            String lieuPropre = MeteoService.getLieuPropre(lieu);
            System.out.println("Lieu propre: " + lieuPropre);
        }
        
        // Test récupération météo
        Evenement ev = new Evenement(
            "Test Event",
            date,
            date.plusHours(2),
            lieu,
            "actif",
            "test.jpg",
            "Description test",
            "Coach Test",
            "Objectifs test",
            0.0,
            50
        );
        
        MeteoService.MeteoResult meteo = service.getMeteo(ev);
        
        if (meteo.disponible) {
            System.out.println("Météo: ✅ DISPONIBLE");
            System.out.println("  " + meteo.emoji + " " + meteo.description);
            System.out.println("  Température: " + Math.round(meteo.tempMin) + "°C - " + Math.round(meteo.tempMax) + "°C");
            System.out.println("  Précipitations: " + meteo.precipitationMm + " mm");
            System.out.println("  Humidité: " + meteo.humidite + "%");
            System.out.println("  Message badge: " + meteo.message);
        } else {
            System.out.println("Météo: ❌ INDISPONIBLE");
            System.out.println("  Raison: " + meteo.description);
        }
    }
}
