package tn.esprit.projet.services;

import java.time.LocalDate;

/**
 * Classe de test pour vérifier le fonctionnement du service météo
 * Exécutez cette classe pour tester l'intégration météo
 */
public class WeatherServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test du Service Météo ===\n");
        
        WeatherService weatherService = new WeatherService();
        
        // Test 1: Météo pour aujourd'hui à Tunis
        System.out.println("📍 Test 1: Météo à Tunis aujourd'hui");
        LocalDate aujourdhui = LocalDate.now();
        WeatherService.WeatherInfo meteoTunis = weatherService.getWeatherForDate("Tunis", aujourdhui);
        System.out.println("   Résultat: " + meteoTunis);
        System.out.println("   Température: " + meteoTunis.getFormattedTemp());
        System.out.println("   Description: " + meteoTunis.getDescription());
        System.out.println("   Emoji: " + meteoTunis.getWeatherEmoji());
        System.out.println();
        
        // Test 2: Météo pour demain à Paris
        System.out.println("📍 Test 2: Météo à Paris demain");
        LocalDate demain = LocalDate.now().plusDays(1);
        WeatherService.WeatherInfo meteoParis = weatherService.getWeatherForDate("Paris", demain);
        System.out.println("   Résultat: " + meteoParis);
        System.out.println();
        
        // Test 3: Météo pour dans 3 jours à Sousse
        System.out.println("📍 Test 3: Météo à Sousse dans 3 jours");
        LocalDate dans3jours = LocalDate.now().plusDays(3);
        WeatherService.WeatherInfo meteoSousse = weatherService.getWeatherForDate("Sousse", dans3jours);
        System.out.println("   Résultat: " + meteoSousse);
        System.out.println();
        
        // Test 4: Test avec une ville invalide
        System.out.println("📍 Test 4: Ville invalide (doit afficher météo par défaut)");
        WeatherService.WeatherInfo meteoInvalide = weatherService.getWeatherForDate("VilleInexistante123", aujourdhui);
        System.out.println("   Résultat: " + meteoInvalide);
        System.out.println();
        
        System.out.println("=== Tests terminés ===");
        System.out.println("\n⚠️ Note: Si vous voyez 'Données météo non disponibles',");
        System.out.println("   vérifiez que vous avez configuré votre clé API dans WeatherService.java");
        System.out.println("   Voir CONFIGURATION_METEO.md pour les instructions");
    }
}
