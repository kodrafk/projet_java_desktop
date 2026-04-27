package tn.esprit.projet.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;

/**
 * Webcam access via camera_server.py (OpenCV-based Python server).
 * Replaces the unreliable sarxos/bridj approach on Windows + Java 17.
 *
 * The Python server is started once per JVM session and reused.
 */
public class WebcamService {

    private final CameraServerService server = new CameraServerService();

    public boolean open() {
        // Ensure the Python camera server is running
        CameraServerService.startServer();
        // Connect and check camera — retry a few times while it warms up
        if (!server.connect()) {
            System.err.println("[Webcam] Could not connect to camera server.");
            return false;
        }
        for (int i = 0; i < 6; i++) {
            boolean ok = server.isCameraAvailable();
            if (ok) {
                System.out.println("[Webcam] Camera available: true");
                return true;
            }
            System.out.println("[Webcam] Camera not ready yet, retrying... (" + (i+1) + "/6)");
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            // Ask server to retry opening the camera
            if (i == 2) server.retryCamera();
        }
        System.err.println("[Webcam] Camera unavailable after retries.");
        return false;
    }

    public WritableImage grabFrame() {
        return server.grabFrame();
    }

    public byte[] grabFrameAsJpeg() {
        byte[] jpeg = server.grabJpeg();
        return jpeg != null ? jpeg : new byte[0];
    }

    public byte[] toJpeg(BufferedImage src, float quality) {
        try {
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
        return server.isConnected();
    }

    public void close() {
        server.disconnect();
        // Note: we do NOT stop the Python server here — it is shared across
        // enroll and verify windows. Call CameraServerService.stopServer()
        // only on application exit.
    }
}
