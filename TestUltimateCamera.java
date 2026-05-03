import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TEST ULTIME DE LA CAMÉRA - TOUTES LES MÉTHODES
 */
public class TestUltimateCamera {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST ULTIME CAMÉRA                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Test 1: Service de découverte
        testDiscoveryService();
        
        // Test 2: Caméra par défaut
        testDefaultCamera();
        
        // Test 3: Toutes les caméras
        testAllCameras();
        
        // Test 4: Force avec timeout
        testWithTimeout();
        
        // Test 5: Différentes résolutions
        testDifferentResolutions();
        
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RÉSULTATS FINAUX                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        
        // Compter les images créées
        int imageCount = 0;
        String[] testImages = {
            "test_default.jpg", "test_camera_0.jpg", "test_camera_1.jpg", 
            "test_timeout.jpg", "test_vga.jpg", "test_qvga.jpg"
        };
        
        for (String imageName : testImages) {
            if (new File(imageName).exists()) {
                imageCount++;
                System.out.println("✅ " + imageName + " créée");
            }
        }
        
        System.out.println();
        if (imageCount > 0) {
            System.out.println("🎉 SUCCÈS! " + imageCount + " test(s) réussi(s)");
            System.out.println("La caméra fonctionne! Face ID devrait marcher maintenant.");
        } else {
            System.out.println("❌ ÉCHEC TOTAL - Aucune image capturée");
            System.out.println("Problème matériel ou pilotes manquants");
        }
    }
    
    private static void testDiscoveryService() {
        System.out.println("🔍 TEST 1: Service de découverte");
        try {
            WebcamDiscoveryService service = Webcam.getDiscoveryService();
            service.stop();
            Thread.sleep(1000);
            service.start();
            Thread.sleep(3000);
            
            List<Webcam> webcams = Webcam.getWebcams();
            System.out.println("   Caméras trouvées: " + webcams.size());
            
            for (int i = 0; i < webcams.size(); i++) {
                System.out.println("   [" + i + "] " + webcams.get(i).getName());
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testDefaultCamera() {
        System.out.println("📷 TEST 2: Caméra par défaut");
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                System.out.println("   ❌ Aucune caméra par défaut");
                return;
            }
            
            System.out.println("   Caméra: " + webcam.getName());
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            
            if (webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    ImageIO.write(image, "jpg", new File("test_default.jpg"));
                    System.out.println("   ✅ Image sauvée: test_default.jpg");
                }
                webcam.close();
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testAllCameras() {
        System.out.println("🎯 TEST 3: Toutes les caméras");
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            
            for (int i = 0; i < Math.min(webcams.size(), 3); i++) {
                Webcam cam = webcams.get(i);
                System.out.println("   Test caméra " + i + ": " + cam.getName());
                
                try {
                    cam.setViewSize(WebcamResolution.VGA.getSize());
                    cam.open();
                    
                    if (cam.isOpen()) {
                        BufferedImage image = cam.getImage();
                        if (image != null) {
                            String filename = "test_camera_" + i + ".jpg";
                            ImageIO.write(image, "jpg", new File(filename));
                            System.out.println("   ✅ " + filename + " créée");
                        }
                        cam.close();
                    }
                    
                } catch (Exception e) {
                    System.out.println("   ❌ Caméra " + i + " erreur: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Erreur générale: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testWithTimeout() {
        System.out.println("⏱️  TEST 4: Avec timeout étendu");
        try {
            List<Webcam> webcams = Webcam.getWebcams(15, TimeUnit.SECONDS);
            System.out.println("   Caméras avec timeout: " + webcams.size());
            
            if (!webcams.isEmpty()) {
                Webcam cam = webcams.get(0);
                cam.open();
                if (cam.isOpen()) {
                    BufferedImage image = cam.getImage();
                    if (image != null) {
                        ImageIO.write(image, "jpg", new File("test_timeout.jpg"));
                        System.out.println("   ✅ test_timeout.jpg créée");
                    }
                    cam.close();
                }
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testDifferentResolutions() {
        System.out.println("📐 TEST 5: Différentes résolutions");
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) return;
            
            // Test VGA
            try {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open();
                if (webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        ImageIO.write(image, "jpg", new File("test_vga.jpg"));
                        System.out.println("   ✅ VGA (640x480): test_vga.jpg");
                    }
                    webcam.close();
                }
            } catch (Exception e) {
                System.out.println("   ❌ VGA erreur: " + e.getMessage());
            }
            
            // Test QVGA
            try {
                Thread.sleep(1000);
                webcam.setViewSize(WebcamResolution.QVGA.getSize());
                webcam.open();
                if (webcam.isOpen()) {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        ImageIO.write(image, "jpg", new File("test_qvga.jpg"));
                        System.out.println("   ✅ QVGA (320x240): test_qvga.jpg");
                    }
                    webcam.close();
                }
            } catch (Exception e) {
                System.out.println("   ❌ QVGA erreur: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Erreur générale: " + e.getMessage());
        }
        System.out.println();
    }
}