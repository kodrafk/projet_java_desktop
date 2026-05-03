package tn.esprit.projet.services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Service webcam DIRECT - Bypass du serveur Python
 * Utilise directement sarxos/webcam pour éviter les problèmes de serveur
 */
public class DirectWebcamService {
    
    private Webcam webcam;
    private boolean isOpen = false;
    
    public boolean open() {
        try {
            System.out.println("[DirectCam] Recherche de caméras...");
            
            // Forcer la détection des webcams
            Webcam.getDiscoveryService().stop();
            Webcam.getDiscoveryService().start();
            Thread.sleep(2000); // Attendre la détection
            
            webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("[DirectCam] Aucune caméra trouvée!");
                return false;
            }
            
            System.out.println("[DirectCam] Caméra trouvée: " + webcam.getName());
            
            // Configurer la résolution
            Dimension[] sizes = webcam.getViewSizes();
            if (sizes.length > 0) {
                // Utiliser VGA si disponible, sinon la première résolution
                Dimension targetSize = WebcamResolution.VGA.getSize();
                boolean vgaSupported = false;
                for (Dimension size : sizes) {
                    if (size.equals(targetSize)) {
                        vgaSupported = true;
                        break;
                    }
                }
                webcam.setViewSize(vgaSupported ? targetSize : sizes[0]);
                System.out.println("[DirectCam] Résolution: " + webcam.getViewSize());
            }
            
            // Ouvrir la caméra
            System.out.println("[DirectCam] Ouverture de la caméra...");
            webcam.open();
            
            if (webcam.isOpen()) {
                System.out.println("[DirectCam] ✅ Caméra ouverte avec succès!");
                
                // Test de capture
                BufferedImage testImage = webcam.getImage();
                if (testImage != null) {
                    System.out.println("[DirectCam] ✅ Test de capture réussi: " + 
                        testImage.getWidth() + "x" + testImage.getHeight());
                    isOpen = true;
                    return true;
                } else {
                    System.err.println("[DirectCam] ❌ Impossible de capturer une image");
                    webcam.close();
                    return false;
                }
            } else {
                System.err.println("[DirectCam] ❌ Impossible d'ouvrir la caméra");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("[DirectCam] Erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public WritableImage grabFrame() {
        if (!isOpen || webcam == null || !webcam.isOpen()) {
            return null;
        }
        
        try {
            BufferedImage bufferedImage = webcam.getImage();
            if (bufferedImage != null) {
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
        } catch (Exception e) {
            System.err.println("[DirectCam] Erreur capture: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean isOpen() {
        return isOpen && webcam != null && webcam.isOpen();
    }
    
    public void close() {
        try {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
                System.out.println("[DirectCam] Caméra fermée");
            }
            isOpen = false;
        } catch (Exception e) {
            System.err.println("[DirectCam] Erreur fermeture: " + e.getMessage());
        }
    }
    
    public byte[] grabFrameAsJpeg() {
        WritableImage fxImage = grabFrame();
        if (fxImage == null) return new byte[0];
        
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("[DirectCam] Erreur JPEG: " + e.getMessage());
            return new byte[0];
        }
    }
}