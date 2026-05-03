import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test DIRECT de la caméra avec sarxos/webcam
 * Compile et lance avec: javac -cp "lib/*" TestDirectCamera.java && java -cp ".:lib/*" TestDirectCamera
 */
public class TestDirectCamera {
    
    public static void main(String[] args) {
        System.out.println("=== TEST CAMERA DIRECT SARXOS ===");
        System.out.println();
        
        try {
            // Démarrer le service de découverte
            System.out.println("1. Démarrage du service de découverte...");
            Webcam.getDiscoveryService().start();
            Thread.sleep(3000);
            
            // Lister les caméras
            System.out.println("2. Recherche des caméras...");
            java.util.List<Webcam> webcams = Webcam.getWebcams();
            
            if (webcams.isEmpty()) {
                System.out.println("❌ AUCUNE CAMÉRA TROUVÉE!");
                System.out.println("Vérifiez:");
                System.out.println("- Caméra connectée");
                System.out.println("- Pilotes installés");
                System.out.println("- Permissions Windows");
                return;
            }
            
            System.out.println("✅ " + webcams.size() + " caméra(s) trouvée(s):");
            for (int i = 0; i < webcams.size(); i++) {
                Webcam cam = webcams.get(i);
                System.out.println("   [" + i + "] " + cam.getName());
            }
            System.out.println();
            
            // Tester la première caméra
            Webcam webcam = webcams.get(0);
            System.out.println("3. Test de la caméra: " + webcam.getName());
            
            // Configurer la résolution
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            System.out.println("   Résolution: " + webcam.getViewSize());
            
            // Ouvrir
            System.out.println("4. Ouverture de la caméra...");
            webcam.open();
            
            if (!webcam.isOpen()) {
                System.out.println("❌ IMPOSSIBLE D'OUVRIR LA CAMÉRA!");
                System.out.println("La caméra est peut-être utilisée par une autre application:");
                System.out.println("- Teams, Zoom, Skype");
                System.out.println("- Chrome, Edge");
                System.out.println("- OBS Studio");
                return;
            }
            
            System.out.println("✅ Caméra ouverte avec succès!");
            
            // Capturer une image
            System.out.println("5. Capture d'une image de test...");
            BufferedImage image = webcam.getImage();
            
            if (image == null) {
                System.out.println("❌ IMPOSSIBLE DE CAPTURER UNE IMAGE!");
                return;
            }
            
            System.out.println("✅ Image capturée: " + image.getWidth() + "x" + image.getHeight());
            
            // Sauvegarder l'image
            File outputFile = new File("test_camera_sarxos.jpg");
            ImageIO.write(image, "jpg", outputFile);
            System.out.println("✅ Image sauvée: " + outputFile.getAbsolutePath());
            
            // Fermer
            webcam.close();
            System.out.println("✅ Caméra fermée");
            
            System.out.println();
            System.out.println("=== TEST RÉUSSI! ===");
            System.out.println("La caméra fonctionne parfaitement avec sarxos/webcam");
            System.out.println("Face ID devrait maintenant fonctionner!");
            
        } catch (Exception e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            
            System.out.println();
            System.out.println("SOLUTIONS POSSIBLES:");
            System.out.println("1. Fermer toutes les apps qui utilisent la caméra");
            System.out.println("2. Redémarrer l'ordinateur");
            System.out.println("3. Vérifier les pilotes de caméra");
            System.out.println("4. Vérifier les permissions Windows");
        }
    }
}