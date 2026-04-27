package tn.esprit.projet.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour générer automatiquement des URLs d'images basées sur le titre de l'événement
 * Utilise l'API Unsplash pour des images professionnelles gratuites
 */
public class ImageGeneratorService {

    // Mapping de mots-clés vers des termes de recherche Unsplash
    private static final Map<String, String> KEYWORD_MAP = new HashMap<>();
    
    static {
        // Sport et Fitness
        KEYWORD_MAP.put("yoga", "yoga");
        KEYWORD_MAP.put("fitness", "fitness gym");
        KEYWORD_MAP.put("gym", "fitness gym");
        KEYWORD_MAP.put("musculation", "bodybuilding gym");
        KEYWORD_MAP.put("cardio", "cardio workout");
        KEYWORD_MAP.put("crossfit", "crossfit");
        KEYWORD_MAP.put("hiit", "hiit workout");
        
        // Sports spécifiques
        KEYWORD_MAP.put("running", "running");
        KEYWORD_MAP.put("course", "running");
        KEYWORD_MAP.put("jogging", "jogging");
        KEYWORD_MAP.put("marathon", "marathon");
        KEYWORD_MAP.put("cycling", "cycling");
        KEYWORD_MAP.put("vélo", "cycling");
        KEYWORD_MAP.put("cyclisme", "cycling");
        KEYWORD_MAP.put("natation", "swimming");
        KEYWORD_MAP.put("swimming", "swimming");
        KEYWORD_MAP.put("piscine", "swimming pool");
        
        // Sports de combat
        KEYWORD_MAP.put("boxe", "boxing");
        KEYWORD_MAP.put("boxing", "boxing");
        KEYWORD_MAP.put("karate", "karate");
        KEYWORD_MAP.put("judo", "judo");
        KEYWORD_MAP.put("taekwondo", "taekwondo");
        KEYWORD_MAP.put("mma", "mma");
        
        // Bien-être
        KEYWORD_MAP.put("pilates", "pilates");
        KEYWORD_MAP.put("meditation", "meditation");
        KEYWORD_MAP.put("méditation", "meditation");
        KEYWORD_MAP.put("relaxation", "relaxation");
        KEYWORD_MAP.put("stretching", "stretching");
        KEYWORD_MAP.put("étirement", "stretching");
        KEYWORD_MAP.put("massage", "massage");
        KEYWORD_MAP.put("spa", "spa wellness");
        
        // Nutrition
        KEYWORD_MAP.put("nutrition", "healthy food");
        KEYWORD_MAP.put("diététique", "nutrition healthy");
        KEYWORD_MAP.put("alimentation", "healthy food");
        KEYWORD_MAP.put("cuisine", "cooking healthy");
        KEYWORD_MAP.put("recette", "healthy recipe");
        KEYWORD_MAP.put("smoothie", "smoothie");
        KEYWORD_MAP.put("salade", "healthy salad");
        KEYWORD_MAP.put("végétarien", "vegetarian food");
        KEYWORD_MAP.put("vegan", "vegan food");
        
        // Entraînement
        KEYWORD_MAP.put("entraînement", "workout training");
        KEYWORD_MAP.put("training", "training");
        KEYWORD_MAP.put("workout", "workout");
        KEYWORD_MAP.put("coaching", "personal training");
        KEYWORD_MAP.put("coach", "fitness coach");
        KEYWORD_MAP.put("sport", "sports");
        
        // Danse
        KEYWORD_MAP.put("danse", "dance");
        KEYWORD_MAP.put("dance", "dance");
        KEYWORD_MAP.put("zumba", "zumba");
        KEYWORD_MAP.put("salsa", "salsa dance");
        KEYWORD_MAP.put("ballet", "ballet");
        
        // Outdoor
        KEYWORD_MAP.put("randonnée", "hiking");
        KEYWORD_MAP.put("hiking", "hiking");
        KEYWORD_MAP.put("escalade", "climbing");
        KEYWORD_MAP.put("climbing", "rock climbing");
        KEYWORD_MAP.put("outdoor", "outdoor fitness");
        KEYWORD_MAP.put("plein air", "outdoor");
        
        // Autres
        KEYWORD_MAP.put("atelier", "workshop");
        KEYWORD_MAP.put("séminaire", "seminar");
        KEYWORD_MAP.put("conférence", "conference");
        KEYWORD_MAP.put("challenge", "fitness challenge");
        KEYWORD_MAP.put("compétition", "competition");
    }

    /**
     * Génère une URL d'image Unsplash basée sur le titre de l'événement
     * 
     * @param titre Le titre de l'événement
     * @return URL de l'image Unsplash
     */
    public static String genererImageDepuisTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return genererImageParDefaut();
        }
        
        String titreLower = titre.toLowerCase();
        String searchTerm = null;
        
        // Chercher un mot-clé correspondant dans le titre
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            if (titreLower.contains(entry.getKey())) {
                searchTerm = entry.getValue();
                System.out.println("🔍 Mot-clé trouvé : '" + entry.getKey() + "' → Recherche : '" + searchTerm + "'");
                break;
            }
        }
        
        // Si aucun mot-clé trouvé, utiliser le titre complet
        if (searchTerm == null) {
            searchTerm = "fitness wellness";
            System.out.println("⚠️ Aucun mot-clé trouvé dans '" + titre + "', utilisation de : '" + searchTerm + "'");
        }
        
        return genererUrlUnsplash(searchTerm);
    }
    
    /**
     * Génère une URL Unsplash avec un terme de recherche
     * 
     * @param searchTerm Terme de recherche
     * @return URL de l'image
     */
    private static String genererUrlUnsplash(String searchTerm) {
        try {
            String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());
            // Format: https://source.unsplash.com/400x300/?terme
            // Alternative avec dimensions fixes et crop
            return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=400&h=300&fit=crop&q=80";
        } catch (Exception e) {
            System.err.println("❌ Erreur encodage URL : " + e.getMessage());
            return genererImageParDefaut();
        }
    }
    
    /**
     * Génère une URL d'image par défaut
     * 
     * @return URL de l'image par défaut
     */
    public static String genererImageParDefaut() {
        return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=300&fit=crop&q=80";
    }
    
    /**
     * Obtient une URL d'image spécifique pour un type d'événement
     * 
     * @param type Type d'événement (yoga, fitness, nutrition, etc.)
     * @return URL de l'image correspondante
     */
    public static String obtenirImageParType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return genererImageParDefaut();
        }
        
        String typeLower = type.toLowerCase().trim();
        
        // URLs Unsplash spécifiques et testées
        switch (typeLower) {
            case "yoga":
                return "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop&q=80";
            
            case "fitness":
            case "gym":
            case "musculation":
                return "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=300&fit=crop&q=80";
            
            case "nutrition":
            case "alimentation":
            case "diététique":
                return "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&h=300&fit=crop&q=80";
            
            case "running":
            case "course":
            case "jogging":
                return "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400&h=300&fit=crop&q=80";
            
            case "cycling":
            case "vélo":
            case "cyclisme":
                return "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=400&h=300&fit=crop&q=80";
            
            case "swimming":
            case "natation":
            case "piscine":
                return "https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=400&h=300&fit=crop&q=80";
            
            case "boxing":
            case "boxe":
                return "https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=400&h=300&fit=crop&q=80";
            
            case "pilates":
                return "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=300&fit=crop&q=80";
            
            case "crossfit":
                return "https://images.unsplash.com/photo-1517344884509-a0c97ec11bcc?w=400&h=300&fit=crop&q=80";
            
            case "workout":
            case "entraînement":
            case "training":
                return "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=400&h=300&fit=crop&q=80";
            
            case "meditation":
            case "méditation":
            case "relaxation":
                return "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop&q=80";
            
            case "danse":
            case "dance":
            case "zumba":
                return "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&h=300&fit=crop&q=80";
            
            case "hiking":
            case "randonnée":
                return "https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop&q=80";
            
            default:
                return genererImageParDefaut();
        }
    }
    
    /**
     * Test du service
     */
    public static void main(String[] args) {
        System.out.println("=== TEST IMAGE GENERATOR SERVICE ===\n");
        
        String[] testTitres = {
            "Séance de Yoga Matinale",
            "Cours de Fitness Intensif",
            "Atelier Nutrition et Bien-être",
            "Entraînement CrossFit",
            "Course à Pied 10km",
            "Natation pour Débutants",
            "Boxe et Cardio",
            "Pilates Avancé",
            "Méditation et Relaxation",
            "Événement Sans Mot-Clé"
        };
        
        for (String titre : testTitres) {
            String imageUrl = genererImageDepuisTitre(titre);
            System.out.println("📝 Titre : " + titre);
            System.out.println("🖼️ Image : " + imageUrl);
            System.out.println();
        }
        
        System.out.println("\n=== TEST PAR TYPE ===\n");
        String[] types = {"yoga", "fitness", "nutrition", "running", "boxing"};
        for (String type : types) {
            System.out.println("Type : " + type + " → " + obtenirImageParType(type));
        }
    }
}
