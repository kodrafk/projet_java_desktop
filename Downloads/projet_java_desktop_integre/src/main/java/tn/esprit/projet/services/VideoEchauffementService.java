package tn.esprit.projet.services;

import tn.esprit.projet.models.VideoEchauffement;
import tn.esprit.projet.models.Evenement;
import java.util.*;

/**
 * Service pour gérer les vidéos d'échauffement
 * Génère automatiquement des vidéos appropriées selon le type d'événement
 */
public class VideoEchauffementService {
    
    // Base de données simulée de vidéos d'échauffement
    private static final Map<String, List<VideoEchauffement>> VIDEOS_PAR_TYPE = new HashMap<>();
    
    static {
        initializerVideos();
    }
    
    /**
     * Initialise la base de données de vidéos d'échauffement
     */
    private static void initializerVideos() {
        // YOGA
        VIDEOS_PAR_TYPE.put("yoga", Arrays.asList(
            new VideoEchauffement(1, "Échauffement Yoga Doux", 
                "Séquence d'échauffement parfaite pour préparer le corps au yoga", 
                "https://www.youtube.com/embed/v7AYKMP6rOE", 
                "https://img.youtube.com/vi/v7AYKMP6rOE/maxresdefault.jpg", 
                480, "yoga", "débutant", 0),
            new VideoEchauffement(2, "Salutation au Soleil", 
                "Enchaînement classique pour échauffer tout le corps", 
                "https://www.youtube.com/embed/73sjOy5N8nE", 
                "https://img.youtube.com/vi/73sjOy5N8nE/maxresdefault.jpg", 
                600, "yoga", "intermédiaire", 0),
            new VideoEchauffement(3, "Yoga Flow Dynamique", 
                "Échauffement énergique pour les pratiquants avancés", 
                "https://www.youtube.com/embed/GLy2rYHwUqY", 
                "https://img.youtube.com/vi/GLy2rYHwUqY/maxresdefault.jpg", 
                720, "yoga", "avancé", 0)
        ));
        
        // MUSCULATION
        VIDEOS_PAR_TYPE.put("musculation", Arrays.asList(
            new VideoEchauffement(4, "Échauffement Musculation Complet", 
                "Préparation articulaire et musculaire avant l'entraînement", 
                "https://www.youtube.com/embed/1Kf5jH8vjQs", 
                "https://img.youtube.com/vi/1Kf5jH8vjQs/maxresdefault.jpg", 
                600, "musculation", "débutant", 0),
            new VideoEchauffement(5, "Activation Musculaire Avancée", 
                "Échauffement spécifique pour les gros groupes musculaires", 
                "https://www.youtube.com/embed/8lDC4Ri9zAQ", 
                "https://img.youtube.com/vi/8lDC4Ri9zAQ/maxresdefault.jpg", 
                480, "musculation", "intermédiaire", 0),
            new VideoEchauffement(6, "Warm-up Powerlifting", 
                "Échauffement pour les mouvements de force", 
                "https://www.youtube.com/embed/EN6HAheRHYQ", 
                "https://img.youtube.com/vi/EN6HAheRHYQ/maxresdefault.jpg", 
                900, "musculation", "avancé", 0)
        ));
        
        // CARDIO
        VIDEOS_PAR_TYPE.put("cardio", Arrays.asList(
            new VideoEchauffement(7, "Échauffement Cardio Léger", 
                "Préparation douce pour les activités cardiovasculaires", 
                "https://www.youtube.com/embed/ReKh8zdLuQY", 
                "https://img.youtube.com/vi/ReKh8zdLuQY/maxresdefault.jpg", 
                300, "cardio", "débutant", 0),
            new VideoEchauffement(8, "Dynamic Warm-up HIIT", 
                "Échauffement dynamique pour l'entraînement intensif", 
                "https://www.youtube.com/embed/M0uO8X3_tEA", 
                "https://img.youtube.com/vi/M0uO8X3_tEA/maxresdefault.jpg", 
                420, "cardio", "intermédiaire", 0),
            new VideoEchauffement(9, "Préparation Course Intensive", 
                "Échauffement complet pour les coureurs expérimentés", 
                "https://www.youtube.com/embed/kLh-uczlPLg", 
                "https://img.youtube.com/vi/kLh-uczlPLg/maxresdefault.jpg", 
                600, "cardio", "avancé", 0)
        ));
        
        // CROSSFIT
        VIDEOS_PAR_TYPE.put("crossfit", Arrays.asList(
            new VideoEchauffement(10, "CrossFit Warm-up Basics", 
                "Échauffement de base pour débuter le CrossFit", 
                "https://www.youtube.com/embed/Jb7xQbON1h4", 
                "https://img.youtube.com/vi/Jb7xQbON1h4/maxresdefault.jpg", 
                480, "crossfit", "débutant", 0),
            new VideoEchauffement(11, "Dynamic CrossFit Prep", 
                "Préparation dynamique pour WOD intensif", 
                "https://www.youtube.com/embed/2jqrSr8qW0E", 
                "https://img.youtube.com/vi/2jqrSr8qW0E/maxresdefault.jpg", 
                600, "crossfit", "intermédiaire", 0),
            new VideoEchauffement(12, "Competition Warm-up", 
                "Échauffement de compétition CrossFit", 
                "https://www.youtube.com/embed/dQw4w9WgXcQ", 
                "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg", 
                720, "crossfit", "avancé", 0)
        ));
        
        // PILATES
        VIDEOS_PAR_TYPE.put("pilates", Arrays.asList(
            new VideoEchauffement(13, "Pilates Gentle Warm-up", 
                "Échauffement doux pour activer le centre", 
                "https://www.youtube.com/embed/K56Z12XjmDM", 
                "https://img.youtube.com/vi/K56Z12XjmDM/maxresdefault.jpg", 
                360, "pilates", "débutant", 0),
            new VideoEchauffement(14, "Core Activation Pilates", 
                "Activation du core avant la séance Pilates", 
                "https://www.youtube.com/embed/ZeqEWLpJUeI", 
                "https://img.youtube.com/vi/ZeqEWLpJUeI/maxresdefault.jpg", 
                480, "pilates", "intermédiaire", 0)
        ));
        
        // DANSE
        VIDEOS_PAR_TYPE.put("danse", Arrays.asList(
            new VideoEchauffement(15, "Échauffement Danse Moderne", 
                "Préparation corporelle pour la danse", 
                "https://www.youtube.com/embed/Eo5H62mCIsg", 
                "https://img.youtube.com/vi/Eo5H62mCIsg/maxresdefault.jpg", 
                420, "danse", "débutant", 0),
            new VideoEchauffement(16, "Dance Warm-up Chorégraphié", 
                "Échauffement avec mini-chorégraphie", 
                "https://www.youtube.com/embed/gCzgmonW1-c", 
                "https://img.youtube.com/vi/gCzgmonW1-c/maxresdefault.jpg", 
                600, "danse", "intermédiaire", 0)
        ));
    }
    
    /**
     * Récupère les vidéos d'échauffement pour un événement donné
     */
    public List<VideoEchauffement> getVideosParEvenement(Evenement evenement) {
        if (evenement == null) return new ArrayList<>();
        
        String typeDetecte = detecterTypeEvenement(evenement);
        List<VideoEchauffement> videos = VIDEOS_PAR_TYPE.get(typeDetecte);
        
        if (videos == null) {
            // Retourner des vidéos génériques si type non trouvé
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
        
        return videosAssociees;
    }
    
    /**
     * Détecte le type d'événement basé sur le nom et la description
     */
    private String detecterTypeEvenement(Evenement evenement) {
        String nom = evenement.getNom().toLowerCase();
        String description = evenement.getDescription() != null ? evenement.getDescription().toLowerCase() : "";
        String texteComplet = nom + " " + description;
        
        // Détection par mots-clés
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
        
        // Par défaut, retourner cardio (plus générique)
        return "cardio";
    }
    
    /**
     * Retourne des vidéos génériques si aucun type spécifique n'est détecté
     */
    private List<VideoEchauffement> getVideosGeneriques() {
        return Arrays.asList(
            new VideoEchauffement(100, "Échauffement Général", 
                "Échauffement complet pour toute activité sportive", 
                "https://www.youtube.com/embed/ReKh8zdLuQY", 
                "https://img.youtube.com/vi/ReKh8zdLuQY/maxresdefault.jpg", 
                480, "général", "débutant", 0),
            new VideoEchauffement(101, "Mobilité Articulaire", 
                "Préparation des articulations avant l'effort", 
                "https://www.youtube.com/embed/M0uO8X3_tEA", 
                "https://img.youtube.com/vi/M0uO8X3_tEA/maxresdefault.jpg", 
                360, "général", "intermédiaire", 0)
        );
    }
    
    /**
     * Récupère une vidéo par son ID
     */
    public VideoEchauffement getVideoParId(int id) {
        for (List<VideoEchauffement> videos : VIDEOS_PAR_TYPE.values()) {
            for (VideoEchauffement video : videos) {
                if (video.getId() == id) {
                    return video;
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère toutes les vidéos disponibles
     */
    public List<VideoEchauffement> getToutesLesVideos() {
        List<VideoEchauffement> toutesVideos = new ArrayList<>();
        for (List<VideoEchauffement> videos : VIDEOS_PAR_TYPE.values()) {
            toutesVideos.addAll(videos);
        }
        return toutesVideos;
    }
}