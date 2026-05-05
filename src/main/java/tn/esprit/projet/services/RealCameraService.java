package tn.esprit.projet.services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * SERVICE CAMÉRA RÉELLE - OUVRE VRAIMENT LA CAMÉRA
 * Version ultra-simplifiée qui marche à 100%
 */
public class RealCameraService {
    
    private Webcam webcam;
    
    public boolean open() {
        try {
            System.out.println("[RealCamera] === OUVERTURE DE LA VRAIE CAMÉRA ===");
            
            // Obtenir la caméra par défaut
            webcam = Webcam.getDefault();
            
            if (webcam == null) {
                System.err.println("[RealCamera] ERROR: No camera found!");
                return false;
            }
            
            System.out.println("[RealCamera] Caméra détectée: " + webcam.getName());
            
            // Configurer la résolution VGA (640x480)
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            System.out.println("[RealCamera] Résolution: " + webcam.getViewSize());
            
            // OUVRIR LA CAMÉRA
            System.out.println("[RealCamera] Ouverture en cours...");
            webcam.open();
            
            // Vérifier que c'est ouvert
            if (!webcam.isOpen()) {
                System.err.println("[RealCamera] ERROR: Failed to open!");
                return false;
            }
            
            System.out.println("[RealCamera] SUCCESS: CAMERA OPENED!");
            
            // Test de capture
            BufferedImage testImage = webcam.getImage();
            if (testImage == null) {
                System.err.println("[RealCamera] ERROR: No image captured!");
                webcam.close();
                return false;
            }
            
            System.out.println("[RealCamera] SUCCESS: IMAGE CAPTURED: " + 
                testImage.getWidth() + "x" + testImage.getHeight());
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[RealCamera] ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public WritableImage grabFrame() {
        if (webcam == null || !webcam.isOpen()) {
            System.err.println("[RealCamera] Caméra non ouverte!");
            return null;
        }
        
        try {
            BufferedImage bufferedImage = webcam.getImage();
            if (bufferedImage != null) {
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
        } catch (Exception e) {
            System.err.println("[RealCamera] Erreur capture: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean isOpen() {
        return webcam != null && webcam.isOpen();
    }
    
    public void close() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("[RealCamera] Caméra fermée");
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
            return new byte[0];
        }
    }
}