package tn.esprit.projet.services;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;

public class ScannerService {

    private Webcam webcam;

    // ===============================
    // OUVRIR LA WEBCAM
    // ===============================
    public void startCamera() {
        webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
        } else {
            throw new RuntimeException("No webcam detected on this system.");
        }
    }

    // ===============================
    // FERMER LA WEBCAM
    // ===============================
    public void stopCamera() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    // ===============================
    // LIRE LE CODE-BARRES
    // ===============================
    public String scanBarcode() {
        if (webcam == null || !webcam.isOpen()) {
            return null;
        }

        try {
            BufferedImage image = webcam.getImage();

            if (image == null) return null;

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);

            return result.getText(); // retourne le code-barres

        } catch (NotFoundException e) {
            // Aucun code trouvé dans l'image
            return null;
        } catch (Exception e) {
            System.out.println("Scanner error: " + e.getMessage());
            return null;
        }
    }
    // Ajouter cette méthode dans ScannerService.java
    public BufferedImage getFrame() {
        if (webcam != null && webcam.isOpen()) {
            return webcam.getImage();
        }
        return null;
    }
}