package tn.esprit.projet.utils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

/**
 * Utilitaire pour télécharger et installer des vidéos d'échauffement
 */
public class VideoDownloader {
    
    private static final String VIDEOS_DIR = "src/main/resources/videos/";
    private static final String THUMBNAILS_DIR = "src/main/resources/images/thumbnails/";
    
    // URLs de vidéos d'exemple (courtes et libres de droits)
    private static final String[][] SAMPLE_VIDEOS = {
        // {filename, url, thumbnail_url}
        {"yoga_doux_8min.mp4", "https://sample-videos.com/zip/10/mp4/SampleVideo_360x240_1mb.mp4", "https://via.placeholder.com/350x200/2E7D5A/FFFFFF?text=Yoga+Doux"},
        {"cardio_leger_5min.mp4", "https://sample-videos.com/zip/10/mp4/SampleVideo_360x240_2mb.mp4", "https://via.placeholder.com/350x200/EF4444/FFFFFF?text=Cardio+Leger"},
        {"muscu_complet_10min.mp4", "https://sample-videos.com/zip/10/mp4/SampleVideo_360x240_5mb.mp4", "https://via.placeholder.com/350x200/3B82F6/FFFFFF?text=Musculation"}
    };
    
    /**
     * Télécharge et installe les vidéos d'exemple
     */
    public static CompletableFuture<Void> installerVideosExemple() {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("🚀 Début du téléchargement des vidéos d'exemple...");
                
                // Créer les dossiers
                creerDossiers();
                
                // Télécharger les vidéos
                for (String[] video : SAMPLE_VIDEOS) {
                    String filename = video[0];
                    String videoUrl = video[1];
                    String thumbUrl = video[2];
                    
                    System.out.println("📥 Téléchargement : " + filename);
                    
                    // Télécharger la vidéo
                    telechargerFichier(videoUrl, VIDEOS_DIR + filename);
                    
                    // Télécharger la thumbnail
                    String thumbName = filename.replace(".mp4", "_thumb.jpg");
                    telechargerFichier(thumbUrl, THUMBNAILS_DIR + thumbName);
                }
                
                System.out.println("✅ Téléchargement terminé ! Redémarrez l'application pour voir les vidéos.");
                
            } catch (Exception e) {
                System.err.println("❌ Erreur téléchargement : " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Crée des vidéos de démonstration locales (plus rapide)
     */
    public static void creerVideosDemo() {
        try {
            System.out.println("🎬 Création des vidéos de démonstration...");
            
            creerDossiers();
            
            // Créer des fichiers vidéo de démonstration (très petits)
            creerVideoDemo("yoga_doux_8min.mp4", "🧘‍♀️ Yoga Doux", "#2E7D5A");
            creerVideoDemo("cardio_leger_5min.mp4", "🏃‍♂️ Cardio Léger", "#EF4444");
            creerVideoDemo("muscu_complet_10min.mp4", "💪 Musculation", "#3B82F6");
            creerVideoDemo("crossfit_basics_8min.mp4", "🏋️‍♀️ CrossFit", "#F59E0B");
            creerVideoDemo("pilates_gentle_6min.mp4", "🤸‍♀️ Pilates", "#8B5CF6");
            creerVideoDemo("danse_moderne_7min.mp4", "💃 Danse", "#EC4899");
            
            System.out.println("✅ Vidéos de démonstration créées !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création démo : " + e.getMessage());
        }
    }
    
    /**
     * Crée une vidéo de démonstration simple
     */
    private static void creerVideoDemo(String filename, String title, String color) {
        try {
            // Créer un fichier vidéo de démonstration plus réaliste
            Path videoPath = Paths.get(VIDEOS_DIR + filename);
            
            // Créer un contenu de démonstration qui simule une vraie vidéo
            StringBuilder demoContent = new StringBuilder();
            demoContent.append("# VIDÉO DE DÉMONSTRATION - ").append(title).append("\n");
            demoContent.append("# Cette vidéo est une démonstration locale\n");
            demoContent.append("# Durée simulée: 5-10 minutes\n");
            demoContent.append("# Type: Échauffement sportif\n\n");
            
            // Ajouter du contenu pour simuler une vidéo plus volumineuse
            for (int i = 0; i < 100; i++) {
                demoContent.append("FRAME_").append(i).append("_").append(title.replace(" ", "_")).append("_DATA\n");
                demoContent.append("TIMESTAMP_").append(i * 3).append("_SECONDS\n");
                demoContent.append("EXERCISE_INSTRUCTION_").append(i % 10).append("\n");
                demoContent.append("DEMO_CONTENT_PADDING_").append("X".repeat(50)).append("\n");
            }
            
            Files.write(videoPath, demoContent.toString().getBytes());
            
            // Créer la thumbnail correspondante
            String thumbName = filename.replace(".mp4", "_thumb.jpg");
            creerThumbnailDemo(thumbName, title, color);
            
            System.out.println("📹 Créé : " + filename + " (" + Files.size(videoPath) + " bytes)");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création " + filename + " : " + e.getMessage());
        }
    }
    
    /**
     * Crée une thumbnail de démonstration
     */
    private static void creerThumbnailDemo(String filename, String title, String color) {
        try {
            // Créer une image SVG simple
            String svg = createDemoSvg(title, color);
            Path thumbPath = Paths.get(THUMBNAILS_DIR + filename);
            Files.write(thumbPath, svg.getBytes());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création thumbnail : " + e.getMessage());
        }
    }
    
    /**
     * Crée un SVG de démonstration
     */
    private static String createDemoSvg(String title, String color) {
        return "<?xml version='1.0' encoding='UTF-8'?>" +
               "<svg width='350' height='200' xmlns='http://www.w3.org/2000/svg'>" +
               "<defs>" +
               "<linearGradient id='grad' x1='0%' y1='0%' x2='100%' y2='100%'>" +
               "<stop offset='0%' style='stop-color:" + color + ";stop-opacity:1' />" +
               "<stop offset='100%' style='stop-color:#1F2937;stop-opacity:1' />" +
               "</linearGradient>" +
               "</defs>" +
               "<rect width='350' height='200' fill='url(#grad)'/>" +
               "<circle cx='175' cy='80' r='30' fill='white' opacity='0.9'/>" +
               "<polygon points='165,70 165,90 185,80' fill='" + color + "'/>" +
               "<text x='175' y='130' font-family='Arial' font-size='16' font-weight='bold' text-anchor='middle' fill='white'>" + title + "</text>" +
               "<text x='175' y='150' font-family='Arial' font-size='12' text-anchor='middle' fill='#D1D5DB'>VIDÉO D'ÉCHAUFFEMENT</text>" +
               "<text x='175' y='170' font-family='Arial' font-size='10' text-anchor='middle' fill='#9CA3AF'>Nutri Coach Pro</text>" +
               "</svg>";
    }
    
    /**
     * Télécharge un fichier depuis une URL
     */
    private static void telechargerFichier(String url, String destination) {
        try {
            URL fileUrl = new URL(url);
            Path destPath = Paths.get(destination);
            
            try (InputStream in = fileUrl.openStream()) {
                Files.copy(in, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("✅ Téléchargé : " + destination);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement " + url + " : " + e.getMessage());
        }
    }
    
    /**
     * Crée les dossiers nécessaires
     */
    private static void creerDossiers() {
        try {
            Files.createDirectories(Paths.get(VIDEOS_DIR));
            Files.createDirectories(Paths.get(THUMBNAILS_DIR));
            System.out.println("📁 Dossiers créés/vérifiés");
        } catch (Exception e) {
            System.err.println("❌ Erreur création dossiers : " + e.getMessage());
        }
    }
    
    /**
     * Vérifie si les vidéos sont installées
     */
    public static boolean videosInstallees() {
        try {
            Path videosDir = Paths.get(VIDEOS_DIR);
            if (!Files.exists(videosDir)) return false;
            
            // Vérifier s'il y a au moins une vidéo
            return Files.list(videosDir)
                       .anyMatch(path -> path.toString().endsWith(".mp4"));
                       
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Compte le nombre de vidéos installées
     */
    public static int compterVideos() {
        try {
            Path videosDir = Paths.get(VIDEOS_DIR);
            if (!Files.exists(videosDir)) return 0;
            
            return (int) Files.list(videosDir)
                             .filter(path -> path.toString().endsWith(".mp4"))
                             .count();
                             
        } catch (Exception e) {
            return 0;
        }
    }
}