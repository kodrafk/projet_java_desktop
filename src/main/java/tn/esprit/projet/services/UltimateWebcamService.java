package tn.esprit.projet.services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Webcam service with force-unlock via reflection.
 * Fixes "Webcam has already been locked" by clearing internal sarxos lock flags.
 */
public class UltimateWebcamService {

    private Webcam webcam;
    private boolean isOpen = false;
    private Thread discoveryThread;

    public boolean open() {
        System.out.println("=== ULTIMATE WEBCAM SERVICE ===");

        // Step 0: Force-unlock all webcams FIRST
        forceUnlockAllWebcams();

        // Step 0.5: Try the SIMPLE direct approach first (like ProfessionalCameraService)
        if (trySimpleDirect()) { System.out.println("✅ Simple direct OK"); return true; }

        if (forceRestartDiscovery()) { System.out.println("✅ Method 1 OK"); return true; }
        if (tryAllDrivers())         { System.out.println("✅ Method 2 OK"); return true; }
        if (forceManualDiscovery())  { System.out.println("✅ Method 3 OK"); return true; }
        if (bruteForceAllIndices())  { System.out.println("✅ Method 4 OK"); return true; }
        if (fallbackWithTimeout())   { System.out.println("✅ Method 5 OK"); return true; }

        System.err.println("❌ ALL METHODS FAILED");
        return false;
    }

    /** Simple direct open — same as ProfessionalCameraService which worked before */
    private boolean trySimpleDirect() {
        try {
            System.out.println("[Simple] Trying direct open...");
            webcam = com.github.sarxos.webcam.Webcam.getDefault();
            if (webcam == null) return false;
            webcam.setViewSize(com.github.sarxos.webcam.WebcamResolution.VGA.getSize());
            webcam.open();
            if (!webcam.isOpen()) return false;
            // Warm up — discard first 3 frames
            for (int i = 0; i < 3; i++) { webcam.getImage(); Thread.sleep(80); }
            BufferedImage img = webcam.getImage();
            if (img == null) { webcam.close(); return false; }
            System.out.println("[Simple] ✅ " + webcam.getName() + " " + img.getWidth() + "x" + img.getHeight());
            isOpen = true;
            return true;
        } catch (Exception e) {
            System.err.println("[Simple] " + e.getMessage());
            try { if (webcam != null && webcam.isOpen()) webcam.close(); } catch (Exception ignored) {}
            return false;
        }
    }

    // ── Force-unlock via reflection ───────────────────────────────────────────

    private void forceUnlockAllWebcams() {
        System.out.println("[Unlock] Force-unlocking all webcams...");
        try {
            List<Webcam> cams = Webcam.getWebcams(5, TimeUnit.SECONDS);
            for (Webcam cam : cams) forceUnlockWebcam(cam);
        } catch (Exception e) {
            try {
                Webcam def = Webcam.getDefault(5, TimeUnit.SECONDS);
                if (def != null) forceUnlockWebcam(def);
            } catch (Exception ignored) {}
        }
    }

    private void forceUnlockWebcam(Webcam cam) {
        try {
            if (cam.isOpen()) {
                try { cam.close(); Thread.sleep(300); } catch (Exception ignored) {}
            }

            // 1. Clear AtomicBoolean fields on Webcam itself
            clearBooleanFields(cam, Webcam.class);
            clearBooleanFields(cam, cam.getClass().getSuperclass());

            // 2. Get the WebcamLock object and call unlock() + delete lock file
            try {
                Field lockField = Webcam.class.getDeclaredField("lock");
                lockField.setAccessible(true);
                Object webcamLock = lockField.get(cam);
                if (webcamLock != null) {
                    // Call unlock() method
                    try {
                        java.lang.reflect.Method unlockMethod = webcamLock.getClass().getMethod("unlock");
                        unlockMethod.invoke(webcamLock);
                        System.out.println("[Unlock] Called WebcamLock.unlock() on " + cam.getName());
                    } catch (Exception e) {
                        System.err.println("[Unlock] unlock() failed: " + e.getMessage());
                    }

                    // Clear the 'locked' AtomicBoolean inside WebcamLock
                    clearBooleanFields(webcamLock, webcamLock.getClass());

                    // Delete the lock file on disk
                    try {
                        java.lang.reflect.Method getLockFile = webcamLock.getClass().getMethod("getLockFile");
                        java.io.File lockFile = (java.io.File) getLockFile.invoke(webcamLock);
                        if (lockFile != null && lockFile.exists()) {
                            lockFile.delete();
                            System.out.println("[Unlock] Deleted lock file: " + lockFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        System.err.println("[Unlock] getLockFile failed: " + e.getMessage());
                    }

                    // Stop the updater thread inside WebcamLock
                    try {
                        Field updaterField = webcamLock.getClass().getDeclaredField("updater");
                        updaterField.setAccessible(true);
                        Thread updaterThread = (Thread) updaterField.get(webcamLock);
                        if (updaterThread != null && updaterThread.isAlive()) {
                            updaterThread.interrupt();
                            System.out.println("[Unlock] Interrupted WebcamLock updater thread");
                        }
                    } catch (Exception ignored) {}
                }
            } catch (NoSuchFieldException e) {
                System.err.println("[Unlock] No 'lock' field found in Webcam");
            }

            System.out.println("[Unlock] ✅ " + cam.getName());
        } catch (Exception e) {
            System.err.println("[Unlock] " + e.getMessage());
        }
    }

    private void clearBooleanFields(Object obj, Class<?> cls) {
        if (cls == null || cls == Object.class) return;
        for (Field f : cls.getDeclaredFields()) {
            String name = f.getName().toLowerCase();
            if (name.contains("lock") || name.contains("open") || name.contains("dispos")) {
                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val instanceof AtomicBoolean) {
                        ((AtomicBoolean) val).set(false);
                        System.out.println("[Unlock] Cleared AtomicBoolean '" + f.getName() + "'");
                    } else if (f.getType() == boolean.class) {
                        f.setBoolean(obj, false);
                        System.out.println("[Unlock] Cleared boolean '" + f.getName() + "'");
                    }
                } catch (Exception ignored) {}
            }
        }
        clearBooleanFields(obj, cls.getSuperclass());
    }

    // ── Open methods ──────────────────────────────────────────────────────────

    private boolean forceRestartDiscovery() {
        try {
            System.out.println("[M1] Force restart discovery...");
            WebcamDiscoveryService svc = Webcam.getDiscoveryService();
            svc.stop();
            Thread.sleep(1500);
            svc.start();
            Thread.sleep(2000);
            webcam = Webcam.getDefault();
            if (webcam != null) { forceUnlockWebcam(webcam); return tryOpenWebcam(webcam, "M1"); }
        } catch (Exception e) { System.err.println("[M1] " + e.getMessage()); }
        return false;
    }

    private boolean tryAllDrivers() {
        try {
            System.out.println("[M2] Try all drivers...");
            Webcam.setDriver(new WebcamDefaultDriver());
            Thread.sleep(1000);
            List<Webcam> cams = Webcam.getWebcams(10, TimeUnit.SECONDS);
            for (int i = 0; i < cams.size(); i++) {
                Webcam cam = cams.get(i);
                forceUnlockWebcam(cam);
                if (tryOpenWebcam(cam, "M2-" + i)) { webcam = cam; return true; }
            }
        } catch (Exception e) { System.err.println("[M2] " + e.getMessage()); }
        return false;
    }

    private boolean forceManualDiscovery() {
        try {
            System.out.println("[M3] Manual discovery...");
            discoveryThread = new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    try {
                        Webcam c = Webcam.getDefault();
                        if (c != null) { System.out.println("[M3] Found: " + c.getName()); break; }
                        Thread.sleep(1000);
                    } catch (Exception ignored) {}
                }
            });
            discoveryThread.start();
            discoveryThread.join(8000);
            webcam = Webcam.getDefault();
            if (webcam != null) { forceUnlockWebcam(webcam); return tryOpenWebcam(webcam, "M3"); }
        } catch (Exception e) { System.err.println("[M3] " + e.getMessage()); }
        return false;
    }

    private boolean bruteForceAllIndices() {
        try {
            System.out.println("[M4] Brute force indices...");
            List<Webcam> cams = Webcam.getWebcams();
            for (int i = 0; i < Math.min(cams.size(), 5); i++) {
                Webcam cam = cams.get(i);
                forceUnlockWebcam(cam);
                if (tryOpenWebcam(cam, "M4-" + i)) { webcam = cam; return true; }
            }
        } catch (Exception e) { System.err.println("[M4] " + e.getMessage()); }
        return false;
    }

    private boolean fallbackWithTimeout() {
        try {
            System.out.println("[M5] Fallback extended timeout...");
            List<Webcam> cams = Webcam.getWebcams(30, TimeUnit.SECONDS);
            for (Webcam cam : cams) {
                forceUnlockWebcam(cam);
                for (Dimension res : new Dimension[]{
                    WebcamResolution.QVGA.getSize(),
                    WebcamResolution.VGA.getSize(),
                    new Dimension(160, 120)
                }) {
                    try {
                        cam.setViewSize(res);
                        if (tryOpenWebcam(cam, "M5-" + res.width + "x" + res.height)) {
                            webcam = cam; return true;
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) { System.err.println("[M5] " + e.getMessage()); }
        return false;
    }

    private boolean tryOpenWebcam(Webcam cam, String tag) {
        try {
            if (cam.isOpen()) {
                try { cam.close(); } catch (Exception ignored) {}
                Thread.sleep(400);
                forceUnlockWebcam(cam);
            }
            if (cam.getViewSizes().length > 0) cam.setViewSize(cam.getViewSizes()[0]);
            cam.open();
            if (!cam.isOpen()) return false;
            BufferedImage img = cam.getImage();
            if (img == null) { cam.close(); return false; }
            System.out.println("[" + tag + "] ✅ " + img.getWidth() + "x" + img.getHeight());
            isOpen = true;
            return true;
        } catch (Exception e) {
            System.err.println("[" + tag + "] " + e.getMessage());
            try { if (cam.isOpen()) cam.close(); } catch (Exception ignored) {}
            return false;
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public WritableImage grabFrame() {
        if (!isOpen || webcam == null || !webcam.isOpen()) return null;
        try {
            BufferedImage img = webcam.getImage();
            if (img != null) return SwingFXUtils.toFXImage(img, null);
        } catch (Exception e) {
            try { webcam.close(); Thread.sleep(300); webcam.open(); } catch (Exception ignored) {}
        }
        return null;
    }

    public byte[] grabFrameAsJpeg() {
        WritableImage fx = grabFrame();
        if (fx == null) return new byte[0];
        try {
            BufferedImage img = SwingFXUtils.fromFXImage(fx, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) { return new byte[0]; }
    }

    public boolean isOpen() { return isOpen && webcam != null && webcam.isOpen(); }

    public void close() {
        try {
            if (discoveryThread != null && discoveryThread.isAlive()) discoveryThread.interrupt();
            if (webcam != null && webcam.isOpen()) webcam.close();
            isOpen = false;
        } catch (Exception e) { System.err.println("[Close] " + e.getMessage()); }
    }

    public String getCameraInfo() {
        return webcam != null
            ? "Camera: " + webcam.getName() + " | " + webcam.getViewSize() + " | open=" + webcam.isOpen()
            : "No camera";
    }
}
