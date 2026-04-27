package tn.esprit.projet.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ImageGeneratorService {

    private static final Random random = new Random();
    
    // Catégories d'événements avec mots-clés intelligents
    private static final Map<String, String[]> CATEGORIES_PROMPTS = new HashMap<>();
    
    static {
        // Sports de course et cardio
        CATEGORIES_PROMPTS.put("course|running|run|marathon|jogging|sprint", new String[]{
            "professional runner athlete running outdoor track sunrise dynamic motion",
            "marathon runners group fitness outdoor race competition energy",
            "athletic person jogging park morning healthy lifestyle cardio",
            "sprint athlete track field competition speed power dynamic"
        });
        
        // Yoga et méditation
        CATEGORIES_PROMPTS.put("yoga|meditation|zen|relaxation|stretching", new String[]{
            "peaceful yoga session outdoor nature sunrise calm meditation",
            "yoga class group studio peaceful atmosphere wellness",
            "meditation zen garden tranquil peaceful mindfulness",
            "yoga pose sunset beach peaceful wellness lifestyle"
        });
        
        // Musculation et fitness
        CATEGORIES_PROMPTS.put("gym|musculation|fitness|workout|training|force|strength", new String[]{
            "modern gym equipment fitness training professional athlete",
            "strength training workout dumbbells fitness center motivation",
            "fitness class group training energy dynamic movement",
            "bodybuilding gym professional athlete training power"
        });
        
        // Natation
        CATEGORIES_PROMPTS.put("natation|swimming|piscine|pool", new String[]{
            "professional swimmer pool competition dynamic water splash",
            "swimming training olympic pool athlete performance",
            "aquatic fitness pool exercise wellness water",
            "swimmer underwater professional athletic performance"
        });
        
        // Cyclisme
        CATEGORIES_PROMPTS.put("cyclisme|cycling|vélo|bike|vtt", new String[]{
            "professional cyclist road bike competition speed",
            "mountain biking outdoor trail adventure nature",
            "cycling group road training fitness outdoor",
            "bike race competition athletes dynamic motion"
        });
        
        // Sports d'équipe
        CATEGORIES_PROMPTS.put("football|soccer|basketball|volleyball|team", new String[]{
            "team sport competition stadium dynamic action",
            "football match professional stadium energy crowd",
            "basketball game court athletes competition",
            "volleyball team beach outdoor sport action"
        });
        
        // Nutrition et alimentation
        CATEGORIES_PROMPTS.put("nutrition|diet|alimentation|healthy|food|meal", new String[]{
            "healthy nutrition colorful fresh vegetables fruits balanced meal",
            "nutritious food preparation healthy lifestyle wellness",
            "balanced diet meal planning nutrition wellness",
            "fresh organic food healthy eating lifestyle nutrition"
        });
        
        // Boxe et arts martiaux
        CATEGORIES_PROMPTS.put("boxe|boxing|martial|karate|judo|taekwondo", new String[]{
            "boxing training professional athlete gym power",
            "martial arts training dojo discipline focus",
            "kickboxing fitness class energy dynamic movement",
            "combat sport training professional athlete power"
        });
        
        // Danse et zumba
        CATEGORIES_PROMPTS.put("danse|dance|zumba|aerobic", new String[]{
            "energetic dance fitness class group movement",
            "zumba workout group fitness fun energy",
            "dance studio professional training artistic movement",
            "aerobic dance class fitness energy dynamic"
        });
        
        // Randonnée et outdoor
        CATEGORIES_PROMPTS.put("randonnée|hiking|trekking|montagne|mountain", new String[]{
            "mountain hiking trail outdoor adventure nature",
            "trekking group outdoor nature exploration wellness",
            "hiking path mountain landscape adventure fitness",
            "outdoor trail nature hiking wellness adventure"
        });
        
        // Crossfit et HIIT
        CATEGORIES_PROMPTS.put("crossfit|hiit|interval|intense|bootcamp", new String[]{
            "crossfit training intense workout gym athlete",
            "HIIT workout high intensity fitness training",
            "bootcamp fitness outdoor training group energy",
            "intense workout training gym motivation power"
        });
        
        // Pilates et barre
        CATEGORIES_PROMPTS.put("pilates|barre|core|posture", new String[]{
            "pilates class studio wellness core training",
            "barre workout fitness studio elegant movement",
            "core training pilates mat wellness fitness",
            "pilates reformer studio professional training"
        });
        
        // Sports aquatiques
        CATEGORIES_PROMPTS.put("surf|diving|plongée|kayak|paddle", new String[]{
            "surfing ocean waves sport adventure dynamic",
            "diving underwater ocean exploration adventure",
            "kayaking water sport outdoor nature adventure",
            "paddleboarding water sport fitness outdoor"
        });
        
        // Wellness et spa
        CATEGORIES_PROMPTS.put("wellness|spa|massage|détente|relaxation", new String[]{
            "wellness spa relaxation peaceful atmosphere calm",
            "massage therapy wellness relaxation peaceful",
            "spa treatment wellness relaxation luxury calm",
            "wellness center peaceful atmosphere relaxation"
        });
        
        // Coaching et motivation
        CATEGORIES_PROMPTS.put("coach|coaching|motivation|training|personal", new String[]{
            "personal trainer coaching fitness motivation professional",
            "fitness coach training session motivation energy",
            "coaching group training motivation teamwork",
            "personal training session professional coach gym"
        });
    }

    /**
     * Génère une URL d'image IA intelligente basée sur le titre de l'événement.
     * Analyse le titre pour déterminer le type d'événement et génère une image appropriée.
     */
    public static String genererImageDepuisTitre(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            return genererImageParDefaut();
        }
        
        try {
            String titreLower = titre.toLowerCase();
            String prompt = detecterEtGenererPrompt(titreLower);
            
            // Ajouter des qualificatifs pour améliorer la qualité
            prompt += " professional high quality 4k photography realistic lighting";
            
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            
            // Utiliser un seed basé sur le titre pour avoir la même image pour le même titre
            int seed = Math.abs(titre.hashCode()) % 10000;
            
            return "https://image.pollinations.ai/prompt/" + encodedPrompt + 
                   "?width=800&height=600&nologo=true&seed=" + seed;
        } catch (Exception e) {
            System.err.println("Erreur génération image intelligente: " + e.getMessage());
            return genererImageParDefaut();
        }
    }
    
    /**
     * Détecte le type d'événement et génère un prompt approprié.
     */
    private static String detecterEtGenererPrompt(String titreLower) {
        // Parcourir les catégories pour trouver une correspondance
        for (Map.Entry<String, String[]> entry : CATEGORIES_PROMPTS.entrySet()) {
            String pattern = entry.getKey();
            String[] prompts = entry.getValue();
            
            // Vérifier si le titre contient un des mots-clés de la catégorie
            String[] keywords = pattern.split("\\|");
            for (String keyword : keywords) {
                if (titreLower.contains(keyword)) {
                    // Choisir un prompt aléatoire parmi ceux de la catégorie
                    // Utiliser le hashCode du titre pour avoir toujours le même prompt pour le même titre
                    int index = Math.abs(titreLower.hashCode()) % prompts.length;
                    return prompts[index];
                }
            }
        }
        
        // Si aucune catégorie ne correspond, générer un prompt générique sport/fitness
        return "professional fitness sport event healthy lifestyle wellness training " + titreLower;
    }
    
    /**
     * Génère une image par défaut de haute qualité.
     */
    public static String genererImageParDefaut() {
        // Images par défaut variées de haute qualité
        String[] defaultImages = {
            "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=800&h=600&fit=crop&q=80", // Fitness
            "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800&h=600&fit=crop&q=80", // Gym
            "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&h=600&fit=crop&q=80", // Running
            "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=600&fit=crop&q=80", // Yoga
            "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=800&h=600&fit=crop&q=80"  // Wellness
        };
        
        int index = random.nextInt(defaultImages.length);
        return defaultImages[index];
    }
    
    /**
     * Obtient une image par type d'événement.
     */
    public static String obtenirImageParType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return genererImageParDefaut();
        }
        return genererImageDepuisTitre(type);
    }
    
    /**
     * Génère une image spécifique pour un type de sport.
     */
    public static String genererImageParSport(String sport) {
        if (sport == null || sport.trim().isEmpty()) {
            return genererImageParDefaut();
        }
        
        String sportLower = sport.toLowerCase();
        
        // Mapping direct pour sports spécifiques
        Map<String, String> sportPrompts = new HashMap<>();
        sportPrompts.put("cardio", "cardio workout fitness training dynamic energy");
        sportPrompts.put("strength", "strength training gym weights power muscle");
        sportPrompts.put("flexibility", "flexibility stretching yoga wellness calm");
        sportPrompts.put("endurance", "endurance training running marathon athlete");
        sportPrompts.put("balance", "balance training stability core wellness");
        
        String prompt = sportPrompts.getOrDefault(sportLower, 
            "professional " + sport + " sport fitness training");
        
        return genererImageDepuisTitre(prompt);
    }
}

