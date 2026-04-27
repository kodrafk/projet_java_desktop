package tn.esprit.projet.test;

import tn.esprit.projet.services.MeteoService;
import tn.esprit.projet.models.Evenement;
import java.time.LocalDateTime;

/**
 * Test du système intelligent de détection plein air et météo
 */
public class TestMeteoIntelligent {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   TEST SYSTÈME INTELLIGENT MÉTÉO                          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
        
        MeteoService meteoService = new MeteoService();
        
        // Test 1: STADE RADES (doit être détecté comme plein air)
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: STADE RADES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev1 = new Evenement(
            "Match de Football",
            LocalDateTime.now().plusDays(3),
            LocalDateTime.now().plusDays(3).plusHours(2),
            "STADE RADES",
            "actif",
            "match.jpg",
            "Grand match",
            "Coach Ahmed",
            "Objectifs sportifs",
            50.0
        );
        MeteoService.MeteoResult meteo1 = meteoService.getMeteo(ev1);
        afficherResultat(meteo1);
        
        // Test 2: Parc Sidi Bou Said (doit être détecté comme plein air)
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: Parc Sidi Bou Said");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev2 = new Evenement(
            "Yoga en plein air",
            LocalDateTime.now().plusDays(5),
            LocalDateTime.now().plusDays(5).plusHours(1),
            "Parc Sidi Bou Said",
            "actif",
            "yoga.jpg",
            "Séance de yoga",
            "Coach Fatma",
            "Relaxation",
            0.0
        );
        MeteoService.MeteoResult meteo2 = meteoService.getMeteo(ev2);
        afficherResultat(meteo2);
        
        // Test 3: California Gym Marsa (doit être détecté comme intérieur)
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: California Gym Marsa (INTÉRIEUR)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev3 = new Evenement(
            "Cours de fitness",
            LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(2).plusHours(1),
            "California Gym Marsa",
            "actif",
            "fitness.jpg",
            "Cours de fitness",
            "Coach Mohamed",
            "Musculation",
            30.0
        );
        MeteoService.MeteoResult meteo3 = meteoService.getMeteo(ev3);
        afficherResultat(meteo3);
        
        // Test 4: Plage Hammamet (doit être détecté comme plein air)
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: Plage Hammamet");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev4 = new Evenement(
            "Beach Volleyball",
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(7).plusHours(3),
            "Plage Hammamet",
            "actif",
            "beach.jpg",
            "Tournoi de beach volley",
            "Coach Karim",
            "Sport plage",
            25.0
        );
        MeteoService.MeteoResult meteo4 = meteoService.getMeteo(ev4);
        afficherResultat(meteo4);
        
        // Test 5: Avec préfixe [OUTDOOR] explicite
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 5: [OUTDOOR] Jardin Public Tunis");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev5 = new Evenement(
            "Course à pied",
            LocalDateTime.now().plusDays(4),
            LocalDateTime.now().plusDays(4).plusHours(2),
            "[OUTDOOR] Jardin Public Tunis",
            "actif",
            "running.jpg",
            "Marathon",
            "Coach Ali",
            "Endurance",
            15.0
        );
        MeteoService.MeteoResult meteo5 = meteoService.getMeteo(ev5);
        afficherResultat(meteo5);
        
        // Test 6: Corniche La Marsa (doit être détecté comme plein air)
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 6: Corniche La Marsa");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Evenement ev6 = new Evenement(
            "Jogging matinal",
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusHours(1),
            "Corniche La Marsa",
            "actif",
            "jogging.jpg",
            "Jogging au bord de mer",
            "Coach Sami",
            "Cardio",
            0.0
        );
        MeteoService.MeteoResult meteo6 = meteoService.getMeteo(ev6);
        afficherResultat(meteo6);
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   FIN DES TESTS                                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }
    
    private static void afficherResultat(MeteoService.MeteoResult meteo) {
        System.out.println("\n📊 RÉSULTAT:");
        if (meteo.disponible) {
            System.out.println("   ✅ Météo disponible");
            System.out.println("   " + meteo.emoji + " " + meteo.description);
            System.out.println("   🌡️  Températures: " + Math.round(meteo.tempMin) + "°C - " + Math.round(meteo.tempMax) + "°C");
            if (meteo.precipitationMm > 0) {
                System.out.println("   🌧️  Précipitations: " + meteo.precipitationMm + " mm");
            }
            if (meteo.humidite > 0) {
                System.out.println("   💧 Humidité: " + meteo.humidite + "%");
            }
        } else {
            System.out.println("   ❌ Météo indisponible");
            System.out.println("   Raison: " + meteo.description);
        }
    }
}
