package tn.esprit.projet.services;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * Webcam access using sarxos webcam-capture library.
 *
 * Requires JVM flags (already set in pom.xml):
 *   --add-opens=java.base/java.lang=ALL-UNNAMED
 *   --add-opens=java.base/java.io=ALL-UNNAMED
 *   --add-opens=java.base/java.util=ALL-UNNAMED
 *   --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
 */
public class WebcamService {

    private Webcam webcam;

    public boolean open() {
        try {
            // Discover webcams with a 5-second timeout
            List<Webcam> list = Webcam.getWebcams(5, TimeUnit.SECONDS);
            if (list == null || list.isEmpty()) {
                System.err.println("[Webcam] No camera found.");
                return false;
            }

            webcam = list.get(0);
            System.out.println("[Webcam] Found: " + webcam.getName());

            // If already locked, try to close it first then reopen
            if (webcam.isOpen()) {
                webcam.close();
                Thread.sleep(500);
            }

            // Try to set 640x480, fall back to default if unsupported
            try {
                webcam.setCustomViewSizes(new Dimension(640, 480));
                webcam.setViewSize(new Dimension(640, 480));
            } catch (Exception ignored) {}

            webcam.open();
            System.out.println("[Webcam] Opened: " + webcam.isOpen());
            return webcam.isOpen();

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("locked")) {
                System.err.println("[Webcam] Camera is in use by another application. Close Teams, Zoom, or browser camera tabs.");
            } else {
                System.err.println("[Webcam] Open failed: " + msg);
            }
            return false;
        }
    }

    public WritableImage grabFrame() {
        if (!isOpen()) return null;
        try {
            BufferedImage bi = webcam.getImage();
            return bi != null ? SwingFXUtils.toFXImage(bi, null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] grabFrameAsJpeg() {
        if (!isOpen()) return new byte[0];
        try {
            BufferedImage bi = webcam.getImage();
            return bi != null ? toJpeg(bi, 0.8f) : new byte[0];
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public byte[] toJpeg(BufferedImage src, float quality) {
        try {
            // JPEG requires RGB (no alpha channel)
            BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.createGraphics().drawImage(src, 0, 0, java.awt.Color.WHITE, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgb, null, null), param);
            writer.dispose();
            ios.close();
            return out.toByteArray();
        } catch (Exception e) {
            System.err.println("[Webcam] JPEG encode error: " + e.getMessage());
            return new byte[0];
        }
    }

    public boolean isOpen() {
        return webcam != null && webcam.isOpen();
    }

    public void close() {
        try {
            if (isOpen()) webcam.close();
        } catch (Exception ignored) {}
        webcam = null;
    }
}
