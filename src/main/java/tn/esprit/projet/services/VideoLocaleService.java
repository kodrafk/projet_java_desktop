package tn.esprit.projet.services;

import tn.esprit.projet.models.VideoEchauffement;
import tn.esprit.projet.models.Evenement;
import java.util.*;
import java.io.File;

/**
 * Service pour gérer les vidéos d'échauffement LOCALES (sans connexion Internet)
 * Utilise des vidéos stockées dans le dossier resources/videos/
 */
public class VideoLocaleService {
    
    // Base de données de vidéos locales
    private static final Map<String, List<VideoEchauffement>> VIDEOS_LOCALES = new HashMap<>();
    
    static {
        System.out.println("🎬 Initialisation du service de vidéos locales...");
        initializerVideosLocales();
        creerDossiersVideos();
        verifierEtCreerVideosDemo();
    }
    
    /**
     * Initialise la base de données de vidéos locales
     */
    private static void initializerVideosLocales() {
        System.out.println("📋 Création de la base de données de vidéos...");
        
        // YOGA - Vidéos locales
        VIDEOS_LOCALES.put("yoga", Arrays.asList(
            new VideoEchauffement(1, "Échauffement Yoga Doux", 
                "Séquence d'échauffement parfaite pour préparer le corps au yoga", 
                "src/main/resources/videos/yoga_doux_8min.mp4", 
                createThumbnailSvg("🧘‍♀️", "Yoga Doux"), 
                480, "yoga", "débutant", 0),
            new VideoEchauffement(2, "Salutation au Soleil", 
                "Enchaînement classique pour échauffer tout le corps", 
                "src/main/resources/videos/salutation_soleil_10min.mp4", 
                createThumbnailSvg("🧘‍♀️", "Salutation Soleil"), 
                600, "yoga", "intermédiaire", 0),
            new VideoEchauffement(3, "Yoga Flow Dynamique", 
                "Échauffement énergique pour les pratiquants avancés", 
                "src/main/resources/videos/yoga_flow_12min.mp4", 
                createThumbnailSvg("🧘‍♀️", "Yoga Flow"), 
                720, "yoga", "avancé", 0)
        ));
        
        // MUSCULATION - Vidéos locales
        VIDEOS_LOCALES.put("musculation", Arrays.asList(
            new VideoEchauffement(4, "Échauffement Musculation Complet", 
                "Préparation articulaire et musculaire avant l'entraînement", 
                "src/main/resources/videos/muscu_complet_10min.mp4", 
                createThumbnailSvg("💪", "Musculation"), 
                600, "musculation", "débutant", 0),
            new VideoEchauffement(5, "Activation Musculaire Avancée", 
                "Échauffement spécifique pour les gros groupes musculaires", 
                "src/main/resources/videos/activation_avancee_8min.mp4", 
                createThumbnailSvg("💪", "Activation"), 
                480, "musculation", "intermédiaire", 0),
            new VideoEchauffement(6, "Warm-up Powerlifting", 
                "Échauffement pour les mouvements de force", 
                "src/main/resources/videos/powerlifting_15min.mp4", 
                createThumbnailSvg("💪", "Powerlifting"), 
                900, "musculation", "avancé", 0)
        ));
        
        // CARDIO - Vidéos locales
        VIDEOS_LOCALES.put("cardio", Arrays.asList(
            new VideoEchauffement(7, "Échauffement Cardio Léger", 
                "Préparation douce pour les activités cardiovasculaires", 
                "src/main/resources/videos/cardio_leger_5min.mp4", 
                createThumbnailSvg("🏃‍♂️", "Cardio Léger"), 
                300, "cardio", "débutant", 0),
            new VideoEchauffement(8, "Dynamic Warm-up HIIT", 
                "Échauffement dynamique pour l'entraînement intensif", 
                "src/main/resources/videos/hiit_dynamique_7min.mp4", 
                createThumbnailSvg("🏃‍♂️", "HIIT"), 
                420, "cardio", "intermédiaire", 0),
            new VideoEchauffement(9, "Préparation Course Intensive", 
                "Échauffement complet pour les coureurs expérimentés", 
                "src/main/resources/videos/course_intensive_10min.mp4", 
                createThumbnailSvg("🏃‍♂️", "Course"), 
                600, "cardio", "avancé", 0)
        ));
        
        // CROSSFIT - Vidéos locales
        VIDEOS_LOCALES.put("crossfit", Arrays.asList(
            new VideoEchauffement(10, "CrossFit Warm-up Basics", 
                "Échauffement de base pour débuter le CrossFit", 
                "src/main/resources/videos/crossfit_basics_8min.mp4", 
                createThumbnailSvg("🏋️‍♀️", "CrossFit Basics"), 
                480, "crossfit", "débutant", 0),
            new VideoEchauffement(11, "Dynamic CrossFit Prep", 
                "Préparation dynamique pour WOD intensif", 
                "src/main/resources/videos/crossfit_prep_10min.mp4", 
                createThumbnailSvg("🏋️‍♀️", "CrossFit Prep"), 
                600, "crossfit", "intermédiaire", 0),
            new VideoEchauffement(12, "Competition Warm-up", 
                "Échauffement de compétition CrossFit", 
                "src/main/resources/videos/crossfit_competition_12min.mp4", 
                createThumbnailSvg("🏋️‍♀️", "Competition"), 
                720, "crossfit", "avancé", 0)
        ));
        
        // PILATES - Vidéos locales
        VIDEOS_LOCALES.put("pilates", Arrays.asList(
            new VideoEchauffement(13, "Pilates Gentle Warm-up", 
                "Échauffement doux pour activer le centre", 
                "src/main/resources/videos/pilates_gentle_6min.mp4", 
                createThumbnailSvg("🤸‍♀️", "Pilates Gentle"), 
                360, "pilates", "débutant", 0),
            new VideoEchauffement(14, "Core Activation Pilates", 
                "Activation du core avant la séance Pilates", 
                "src/main/resources/videos/pilates_core_8min.mp4", 
                createThumbnailSvg("🤸‍♀️", "Core Activation"), 
                480, "pilates", "intermédiaire", 0)
        ));
        
        // DANSE - Vidéos locales
        VIDEOS_LOCALES.put("danse", Arrays.asList(
            new VideoEchauffement(15, "Échauffement Danse Moderne", 
                "Préparation corporelle pour la danse", 
                "src/main/resources/videos/danse_moderne_7min.mp4", 
                createThumbnailSvg("💃", "Danse Moderne"), 
                420, "danse", "débutant", 0),
            new VideoEchauffement(16, "Dance Warm-up Chorégraphié", 
                "Échauffement avec mini-chorégraphie", 
                "src/main/resources/videos/danse_choreo_10min.mp4", 
                createThumbnailSvg("💃", "Chorégraphie"), 
                600, "danse", "intermédiaire", 0)
        ));
        
        System.out.println("✅ " + VIDEOS_LOCALES.size() + " catégories de vidéos initialisées");
        
        // Compter le total de vidéos
        int totalVideos = VIDEOS_LOCALES.values().stream().mapToInt(List::size).sum();
        System.out.println("📹 Total : " + totalVideos + " vidéos d'échauffement disponibles");
    }
    
    /**
     * Crée une thumbnail SVG dynamique
     */
    private static String createThumbnailSvg(String emoji, String title) {
        String svg = "<svg width='350' height='200' xmlns='http://www.w3.org/2000/svg'>" +
               "<defs>" +
               "<linearGradient id='grad' x1='0%' y1='0%' x2='100%' y2='100%'>" +
               "<stop offset='0%' style='stop-color:#2E7D5A;stop-opacity:1' />" +
               "<stop offset='100%' style='stop-color:#1F4D3A;stop-opacity:1' />" +
               "</linearGradient>" +
               "</defs>" +
               "<rect width='350' height='200' fill='url(#grad)'/>" +
               "<text x='175' y='80' font-family='Arial' font-size='60' text-anchor='middle' fill='white'>" + emoji + "</text>" +
               "<text x='175' y='130' font-family='Arial' font-size='14' font-weight='bold' text-anchor='middle' fill='white'>" + title.toUpperCase() + "</text>" +
               "<text x='175' y='150' font-family='Arial' font-size='12' text-anchor='middle' fill='#D7E6DF'>VIDÉO D'ÉCHAUFFEMENT</text>" +
               "<text x='175' y='170' font-family='Arial' font-size='10' text-anchor='middle' fill='#9CA3AF'>Nutri Coach Pro</text>" +
               "</svg>";
        
        return "data:image/svg+xml;base64," + 
               java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }
    
    /**
     * Récupère les vidéos locales pour un événement donné
     */
    public List<VideoEchauffement> getVideosParEvenement(Evenement evenement) {
        if (evenement == null) {
            System.out.println("⚠️ Événement null, retour de vidéos génériques");
            return getVideosGeneriques();
        }
        
        String typeDetecte = detecterTypeEvenement(evenement);
        System.out.println("🔍 Type détecté pour '" + evenement.getNom() + "' : " + typeDetecte);
        
        List<VideoEchauffement> videos = VIDEOS_LOCALES.get(typeDetecte);
        
        if (videos == null || videos.isEmpty()) {
            System.out.println("⚠️ Aucune vidéo trouvée pour le type '" + typeDetecte + "', utilisation des vidéos génériques");
            videos = getVideosGeneriques();
        }
        
        // Associer l'ID de l'événement aux vidéos
        List<VideoEchauffement> videosAssociees = new ArrayList<>();
        for (VideoEchauffement video : videos) {
            VideoEchauffement videoCopiee = new VideoEchauffement(
                video.getId(), video.getTitre(), video.getDescription(),
                video.getUrlVideo(), video.getThumbnail(), video.getDuree(),
                video.getTypeEvenement(), video.getNiveau(), evenement.getId()
            );
            videosAssociees.add(videoCopiee);
        }
        
        System.out.println("✅ " + videosAssociees.size() + " vidéos retournées pour l'événement");
        return videosAssociees;
    }
    
    /**
     * Détecte le type d'événement basé sur le nom et la description
     */
    private String detecterTypeEvenement(Evenement evenement) {
        String nom = evenement.getNom().toLowerCase();
        String description = evenement.getDescription() != null ? evenement.getDescription().toLowerCase() : "";
        String texteComplet = nom + " " + description;
        
        if (texteComplet.contains("yoga") || texteComplet.contains("méditation") || texteComplet.contains("zen")) {
            return "yoga";
        }
        if (texteComplet.contains("musculation") || texteComplet.contains("muscle") || texteComplet.contains("force") || 
            texteComplet.contains("haltère") || texteComplet.contains("poids")) {
            return "musculation";
        }
        if (texteComplet.contains("cardio") || texteComplet.contains("course") || texteComplet.contains("running") || 
            texteComplet.contains("vélo") || texteComplet.contains("endurance")) {
            return "cardio";
        }
        if (texteComplet.contains("crossfit") || texteComplet.contains("cross fit") || texteComplet.contains("wod")) {
            return "crossfit";
        }
        if (texteComplet.contains("pilates") || texteComplet.contains("core") || texteComplet.contains("posture")) {
            return "pilates";
        }
        if (texteComplet.contains("danse") || texteComplet.contains("dance") || texteComplet.contains("chorégraphie") || 
            texteComplet.contains("zumba")) {
            return "danse";
        }
        
        return "cardio"; // Par défaut
    }
    
    /**
     * Retourne des vidéos génériques si aucun type spécifique n'est détecté
     */
    private List<VideoEchauffement> getVideosGeneriques() {
        return Arrays.asList(
            new VideoEchauffement(100, "Échauffement Général", 
                "Échauffement complet pour toute activité sportive", 
                "src/main/resources/videos/general_8min.mp4", 
                createThumbnailSvg("🏃‍♂️", "Général"), 
                480, "général", "débutant", 0),
            new VideoEchauffement(101, "Mobilité Articulaire", 
                "Préparation des articulations avant l'effort", 
                "src/main/resources/videos/mobilite_6min.mp4", 
                createThumbnailSvg("🤸‍♂️", "Mobilité"), 
                360, "général", "intermédiaire", 0)
        );
    }
    
    /**
     * Récupère toutes les vidéos locales disponibles
     */
    public List<VideoEchauffement> getToutesLesVideos() {
        List<VideoEchauffement> toutesVideos = new ArrayList<>();
        for (List<VideoEchauffement> videos : VIDEOS_LOCALES.values()) {
            toutesVideos.addAll(videos);
        }
        return toutesVideos;
    }
    
    /**
     * Crée les dossiers nécessaires pour les vidéos locales
     */
    public static void creerDossiersVideos() {
        try {
            File videoDir = new File("src/main/resources/videos");
            File thumbDir = new File("src/main/resources/images/thumbnails");
            
            if (!videoDir.exists()) {
                boolean created = videoDir.mkdirs();
                if (created) {
                    System.out.println("📁 Dossier vidéos créé : " + videoDir.getAbsolutePath());
                }
            }
            
            if (!thumbDir.exists()) {
                boolean created = thumbDir.mkdirs();
                if (created) {
                    System.out.println("📁 Dossier thumbnails créé : " + thumbDir.getAbsolutePath());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création dossiers : " + e.getMessage());
        }
    }
    
    /**
     * Vérifie et crée automatiquement les vidéos de démonstration si nécessaire
     */
    private static void verifierEtCreerVideosDemo() {
        try {
            File videoDir = new File("src/main/resources/videos");
            
            if (!videoDir.exists() || videoDir.listFiles() == null || videoDir.listFiles().length < 5) {
                System.out.println("🎬 Création automatique des vidéos de démonstration...");
                
                try {
                    Class<?> downloaderClass = Class.forName("tn.esprit.projet.utils.VideoDownloader");
                    java.lang.reflect.Method creerVideosDemo = downloaderClass.getMethod("creerVideosDemo");
                    creerVideosDemo.invoke(null);
                    System.out.println("✅ Vidéos de démonstration créées automatiquement !");
                } catch (Exception e) {
                    System.out.println("⚠️ VideoDownloader non disponible, mode démonstration uniquement");
                }
            } else {
                System.out.println("✅ Vidéos déjà présentes (" + videoDir.listFiles().length + " fichiers)");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur vérification vidéos : " + e.getMessage());
        }
    }
}