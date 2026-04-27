package tn.esprit.projet.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

/**
 * Shared camera helper used by ALL Face ID controllers (login, enroll, register).
 * Provides: open, close, grabFrame, grabJpeg, drawOverlay.
 */
public class CameraFaceHelper {

    private com.github.sarxos.webcam.Webcam webcam;
    private boolean open = false;

    // ── Open / Close ──────────────────────────────────────────────────────────

    public boolean open() {
        try {
            forceUnlockAll();
            webcam = com.github.sarxos.webcam.Webcam.getDefault();
            if (webcam == null) return false;
            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();
            if (!webcam.isOpen()) return false;
            // Warm up
            for (int i = 0; i < 4; i++) { webcam.getImage(); Thread.sleep(60); }
            open = true;
            System.out.println("[Camera] ✅ " + webcam.getName());
            return true;
        } catch (Exception e) {
            System.err.println("[Camera] open error: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        open = false;
        try { if (webcam != null && webcam.isOpen()) webcam.close(); } catch (Exception ignored) {}
    }

    public boolean isOpen() { return open && webcam != null && webcam.isOpen(); }

    // ── Grab frames ───────────────────────────────────────────────────────────

    /** Returns raw BufferedImage from camera, or null. */
    public BufferedImage grabRaw() {
        if (!isOpen()) return null;
        try { return webcam.getImage(); } catch (Exception e) { return null; }
    }

    /** Returns camera frame with professional face-guide overlay as WritableImage. */
    public WritableImage grabWithOverlay(int step) {
        BufferedImage raw = grabRaw();
        if (raw == null) return null;
        BufferedImage overlaid = drawFaceGuide(raw, step);
        return SwingFXUtils.toFXImage(overlaid, null);
    }

    /** Returns JPEG bytes of current frame (no overlay). */
    public byte[] grabJpeg() {
        BufferedImage raw = grabRaw();
        if (raw == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(raw, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) { return null; }
    }

    // ── Professional face-guide overlay ───────────────────────────────────────

    /**
     * Draws the SAME professional overlay on every screen:
     * - Oval with animated green border
     * - Eye guide circles with center dots
     * - Nose dot
     * - Scanning line
     * - Corner brackets
     * - "SCANNING" label
     * - Dark vignette outside oval
     *
     * @param step 0=straight, 1=left, 2=right (shifts oval hint)
     */
    public static BufferedImage drawFaceGuide(BufferedImage src, int step) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw camera frame
        g.drawImage(src, 0, 0, null);

        // Oval parameters
        int cx = w / 2;
        if (step == 1) cx = w / 2 - w / 10;
        if (step == 2) cx = w / 2 + w / 10;
        int cy = (int)(h * 0.47);
        int rx = (int)(w * 0.29);
        int ry = (int)(h * 0.43);

        // Dark vignette outside oval
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, w, h);

        // Clear oval (show camera inside)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
        g.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        // Redraw camera inside oval
        Shape ovalClip = new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2);
        g.setClip(ovalClip);
        g.drawImage(src, 0, 0, null);
        g.setClip(null);

        // Animated pulse
        long t = System.currentTimeMillis();
        float pulse = (float)(0.65 + 0.35 * Math.sin(t / 380.0));

        // Outer glow ring
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0.18f, 0.80f, 0.44f, pulse * 0.35f));
        g.draw(new Ellipse2D.Double(cx - rx - 8, cy - ry - 8, (rx + 8) * 2, (ry + 8) * 2));

        // Main oval border
        g.setStroke(new BasicStroke(3.5f));
        g.setColor(new Color(0.18f, 0.80f, 0.44f, pulse));
        g.draw(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));

        // ── Eye guide circles ──
        int eyeY   = (int)(cy - ry * 0.22);
        int eyeOffX = (int)(rx * 0.40);
        int eyeR   = (int)(rx * 0.13);

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(80, 220, 140, 210));
        // Left eye circle
        g.drawOval(cx - eyeOffX - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);
        // Right eye circle
        g.drawOval(cx + eyeOffX - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2);

        // Eye center dots
        g.setColor(new Color(80, 220, 140, 200));
        int dotR = 4;
        g.fillOval(cx - eyeOffX - dotR, eyeY - dotR, dotR * 2, dotR * 2);
        g.fillOval(cx + eyeOffX - dotR, eyeY - dotR, dotR * 2, dotR * 2);

        // ── Nose dot ──
        int noseY = (int)(cy + ry * 0.08);
        g.setColor(new Color(80, 220, 140, 130));
        g.fillOval(cx - 4, noseY - 4, 8, 8);

        // ── Mouth guide line ──
        int mouthY = (int)(cy + ry * 0.38);
        int mouthW = (int)(rx * 0.35);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(80, 220, 140, 100));
        g.drawLine(cx - mouthW, mouthY, cx + mouthW, mouthY);

        // ── Scanning line ──
        int scanOffset = (int)((t / 7) % (ry * 2));
        int scanY = (cy - ry) + scanOffset;
        double dy = (scanY - cy) / (double) ry;
        if (Math.abs(dy) <= 1.0) {
            double scanHalfW = rx * Math.sqrt(1 - dy * dy);
            g.setColor(new Color(80, 220, 140, 90));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine((int)(cx - scanHalfW), scanY, (int)(cx + scanHalfW), scanY);
        }

        // ── Corner brackets ──
        int margin = 14;
        int bx = cx - rx - margin, by = cy - ry - margin;
        int bw = (rx + margin) * 2, bh = (ry + margin) * 2;
        int bLen = 22;
        g.setColor(new Color(80, 220, 140, 200));
        g.setStroke(new BasicStroke(2.5f));
        drawBracket(g, bx,      by,      1,  1, bLen);
        drawBracket(g, bx + bw, by,     -1,  1, bLen);
        drawBracket(g, bx,      by + bh, 1, -1, bLen);
        drawBracket(g, bx + bw, by + bh,-1, -1, bLen);

        // ── SCANNING text ──
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(80, 220, 140, (int)(200 * pulse)));
        String txt = "● SCANNING";
        int tw = g.getFontMetrics().stringWidth(txt);
        g.drawString(txt, cx - tw / 2, cy + ry + 24);

        g.dispose();
        return out;
    }

    private static void drawBracket(Graphics2D g, int x, int y, int dx, int dy, int len) {
        g.drawLine(x, y, x + dx * len, y);
        g.drawLine(x, y, x, y + dy * len);
    }

    // ── Force-unlock sarxos lock ──────────────────────────────────────────────

    private void forceUnlockAll() {
        try {
            java.util.List<com.github.sarxos.webcam.Webcam> cams =
                com.github.sarxos.webcam.Webcam.getWebcams(5, TimeUnit.SECONDS);
            for (com.github.sarxos.webcam.Webcam cam : cams) forceUnlock(cam);
        } catch (Exception e) {
            try {
                com.github.sarxos.webcam.Webcam def =
                    com.github.sarxos.webcam.Webcam.getDefault(5, TimeUnit.SECONDS);
                if (def != null) forceUnlock(def);
            } catch (Exception ignored) {}
        }
    }

    private void forceUnlock(com.github.sarxos.webcam.Webcam cam) {
        try {
            if (cam.isOpen()) { try { cam.close(); Thread.sleep(200); } catch (Exception ignored) {} }
            // Unlock WebcamLock
            Field lockField = com.github.sarxos.webcam.Webcam.class.getDeclaredField("lock");
            lockField.setAccessible(true);
            Object wl = lockField.get(cam);
            if (wl != null) {
                try { wl.getClass().getMethod("unlock").invoke(wl); } catch (Exception ignored) {}
                try {
                    java.io.File lf = (java.io.File) wl.getClass().getMethod("getLockFile").invoke(wl);
                    if (lf != null && lf.exists()) lf.delete();
                } catch (Exception ignored) {}
                try {
                    Field lf = wl.getClass().getDeclaredField("locked");
                    lf.setAccessible(true);
                    ((AtomicBoolean) lf.get(wl)).set(false);
                } catch (Exception ignored) {}
            }
            // Clear open/disposed
            for (String fn : new String[]{"open", "disposed"}) {
                try {
                    Field f = com.github.sarxos.webcam.Webcam.class.getDeclaredField(fn);
                    f.setAccessible(true);
                    ((AtomicBoolean) f.get(cam)).set(false);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[Unlock] " + e.getMessage());
        }
    }
}
