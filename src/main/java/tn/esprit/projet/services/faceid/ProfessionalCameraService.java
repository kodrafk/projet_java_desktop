package tn.esprit.projet.services.faceid;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Professional Camera Service
 * Provides real camera feed for Face ID
 */
public class ProfessionalCameraService {

    private com.github.sarxos.webcam.Webcam webcam;
    private boolean isOpen = false;

    /**
     * Open the default camera
     */
    public boolean open() {
        try {
            System.out.println("[Camera] Opening default webcam...");
            
            webcam = com.github.sarxos.webcam.Webcam.getDefault();
            
            if (webcam == null) {
                System.err.println("[Camera] No webcam found!");
                return false;
            }

            // Set resolution
            webcam.setViewSize(com.github.sarxos.webcam.WebcamResolution.VGA.getSize());
            
            // Open camera
            webcam.open();
            isOpen = true;
            
            System.out.println("[Camera] Webcam opened successfully: " + webcam.getName());
            return true;
            
        } catch (Exception e) {
            System.err.println("[Camera] Failed to open webcam: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Capture a frame as JavaFX Image
     */
    public Image captureFrame() {
        if (!isOpen || webcam == null) {
            return null;
        }

        try {
            BufferedImage buffered = webcam.getImage();
            if (buffered == null) {
                return null;
            }

            return convertToFxImage(buffered);
            
        } catch (Exception e) {
            System.err.println("[Camera] Frame capture error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Capture a frame as JPEG bytes
     */
    public byte[] captureFrameAsJpeg() {
        if (!isOpen || webcam == null) {
            return null;
        }

        try {
            BufferedImage buffered = webcam.getImage();
            if (buffered == null) {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buffered, "jpg", baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("[Camera] JPEG capture error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Close the camera
     */
    public void close() {
        if (webcam != null && isOpen) {
            try {
                webcam.close();
                isOpen = false;
                System.out.println("[Camera] Webcam closed");
            } catch (Exception e) {
                System.err.println("[Camera] Error closing webcam: " + e.getMessage());
            }
        }
    }

    /**
     * Check if camera is open
     */
    public boolean isOpen() {
        return isOpen && webcam != null && webcam.isOpen();
    }

    /**
     * Convert BufferedImage to JavaFX Image
     */
    private Image convertToFxImage(BufferedImage buffered) {
        WritableImage fxImage = new WritableImage(buffered.getWidth(), buffered.getHeight());
        PixelWriter pw = fxImage.getPixelWriter();
        
        for (int y = 0; y < buffered.getHeight(); y++) {
            for (int x = 0; x < buffered.getWidth(); x++) {
                pw.setArgb(x, y, buffered.getRGB(x, y));
            }
        }
        
        return fxImage;
    }
}
