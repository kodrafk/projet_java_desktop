package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Service intelligent de météo qui détecte automatiquement
 * si un événement nécessite des informations météo
 */
public class SmartWeatherService {
    
    private final WeatherService weatherService;
    
    // Mots-clés indiquant un lieu EXTÉRIEUR (plein air)
    private static final List<String> OUTDOOR_KEYWORDS = Arrays.asList(
        // Lieux naturels
        "parc", "jardin", "plage", "lac", "mer", "montagne", "forêt", "campagne",
        "plein air", "outdoor", "extérieur", "dehors",
        
        // Infrastructures extérieures
        "stade", "terrain", "piste", "court", "parcours", "circuit",
        "rue", "avenue", "boulevard", "place", "esplanade",
        
        // Activités extérieures
        "course", "marathon", "trail", "randonnée", "vélo", "cyclisme",
        "football", "rugby", "tennis", "golf", "athlétisme",
        "jogging", "running", "marche", "trekking",
        
        // Événements extérieurs
        "festival", "fête", "marché", "kermesse", "carnaval",
        "pique-nique", "barbecue", "camping",
        
        // Mots arabes pour lieux extérieurs
        "حديقة", "شاطئ", "بحيرة", "جبل", "ملعب", "في الهواء الطلق"
    );
    
    // Mots-clés indiquant un lieu INTÉRIEUR
    private static final List<String> INDOOR_KEYWORDS = Arrays.asList(
        // Bâtiments
        "salle", "gymnase", "gym", "centre", "club", "studio", "complexe",
        "restaurant", "café", "bar", "hôtel", "auberge",
        "école", "université", "institut", "académie",
        "piscine couverte", "indoor", "intérieur",
        
        // Infrastructures couvertes
        "hall", "auditorium", "amphithéâtre", "théâtre", "cinéma",
        "musée", "galerie", "bibliothèque",
        
        // Mots arabes pour lieux intérieurs
        "قاعة", "مطعم", "نادي", "مركز", "داخلي"
    );
    
    public SmartWeatherService() {
        this.weatherService = new WeatherService();
    }
    
    /**
     * Détermine intelligemment si un événement nécessite des infos météo
     * @param evenement L'événement à analyser
     * @return true si l'événement est en plein air, false sinon
     */
    public boolean isOutdoorEvent(Evenement evenement) {
        if (evenement == null) return false;
        
        String lieu = evenement.getLieu();
        String nom = evenement.getNom();
        String description = evenement.getDescription();
        
        // Combiner tous les textes pour l'analyse
        String texteComplet = (lieu + " " + nom + " " + description).toLowerCase();
        
        // Score pour déterminer si c'est extérieur ou intérieur
        int scoreExterieur = 0;
        int scoreInterieur = 0;
        
        // Analyser les mots-clés extérieurs
        for (String keyword : OUTDOOR_KEYWORDS) {
            if (texteComplet.contains(keyword.toLowerCase())) {
                scoreExterieur++;
            }
        }
        
        // Analyser les mots-clés intérieurs
        for (String keyword : INDOOR_KEYWORDS) {
            if (texteComplet.contains(keyword.toLowerCase())) {
                scoreInterieur++;
            }
        }
        
        // Décision: si plus de mots-clés extérieurs, c'est un événement outdoor
        return scoreExterieur > scoreInterieur;
    }
    
    /**
     * Récupère la météo UNIQUEMENT si l'événement est en plein air
     * @param evenement L'événement
     * @return WeatherInfo si outdoor, null si indoor
     */
    public WeatherService.WeatherInfo getWeatherIfOutdoor(Evenement evenement) {
        if (!isOutdoorEvent(evenement)) {
            return null; // Pas de météo pour les événements intérieurs
        }
        
        // Événement extérieur -> récupérer la météo
        LocalDate dateEvent = evenement.getDate_debut().toLocalDate();
        String ville = extraireVille(evenement.getLieu());
        
        return weatherService.getWeatherForDate(ville, dateEvent);
    }
    
    /**
     * Extrait le nom de la ville depuis le lieu
     * Exemples: "Parc du Belvédère, Tunis" -> "Tunis"
     *           "Stade Olympique de Radès" -> "Radès"
     */
    private String extraireVille(String lieu) {
        if (lieu == null || lieu.isEmpty()) {
            return weatherService.getDefaultCity();
        }
        
        // Si le lieu contient une virgule, prendre ce qui est après
        if (lieu.contains(",")) {
            String[] parts = lieu.split(",");
            return parts[parts.length - 1].trim();
        }
        
        // Sinon, chercher des noms de villes tunisiennes connues
        String lieuLower = lieu.toLowerCase();
        String[] villesTunisiennes = {
            "tunis", "sfax", "sousse", "kairouan", "bizerte", "gabès", "ariana",
            "gafsa", "monastir", "ben arous", "kasserine", "médenine", "nabeul",
            "tataouine", "béja", "jendouba", "mahdia", "siliana", "kébili",
            "zaghouan", "manouba", "tozeur", "radès", "la marsa", "carthage"
        };
        
        for (String ville : villesTunisiennes) {
            if (lieuLower.contains(ville)) {
                return ville.substring(0, 1).toUpperCase() + ville.substring(1);
            }
        }
        
        // Par défaut, retourner la ville configurée
        return weatherService.getDefaultCity();
    }
    
    /**
     * Retourne une explication de pourquoi la météo est affichée ou non
     * Utile pour le debug
     */
    public String getWeatherDecisionExplanation(Evenement evenement) {
        if (isOutdoorEvent(evenement)) {
            return "🌤️ Événement en plein air - Météo affichée";
        } else {
            return "🏢 Événement en intérieur - Météo non nécessaire";
        }
    }
    
    /**
     * Analyse détaillée pour le debug
     */
    public String getDetailedAnalysis(Evenement evenement) {
        String texte = (evenement.getLieu() + " " + evenement.getNom() + " " + 
                       evenement.getDescription()).toLowerCase();
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("Analyse de l'événement: ").append(evenement.getNom()).append("\n");
        analysis.append("Lieu: ").append(evenement.getLieu()).append("\n\n");
        
        analysis.append("Mots-clés EXTÉRIEUR détectés:\n");
        for (String keyword : OUTDOOR_KEYWORDS) {
            if (texte.contains(keyword.toLowerCase())) {
                analysis.append("  ✓ ").append(keyword).append("\n");
            }
        }
        
        analysis.append("\nMots-clés INTÉRIEUR détectés:\n");
        for (String keyword : INDOOR_KEYWORDS) {
            if (texte.contains(keyword.toLowerCase())) {
                analysis.append("  ✓ ").append(keyword).append("\n");
            }
        }
        
        analysis.append("\nDécision: ").append(getWeatherDecisionExplanation(evenement));
        
        return analysis.toString();
    }
}
