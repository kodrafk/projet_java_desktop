package tn.esprit.projet.services;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Draws a face-guide oval + corner brackets on a camera frame.
 * Green when face is centered, white otherwise.
 */
public class FaceCameraOverlay {

    /**
     * Draw face guide overlay on a camera frame.
     *
     * @param source  raw camera frame
     * @param faceDetected  true = green oval, false = white oval
     * @param step    0=straight, 1=left, 2=right (shifts oval hint)
     * @return new WritableImage with overlay drawn
     */
    public static WritableImage draw(WritableImage source, boolean faceDetected, int step) {
        if (source == null) return null;

        int W = (int) source.getWidth();
        int H = (int) source.getHeight();
        WritableImage out = new WritableImage(W, H);
        PixelReader  pr   = source.getPixelReader();
        PixelWriter  pw   = out.getPixelWriter();

        // Copy original pixels
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                pw.setArgb(x, y, pr.getArgb(x, y));

        // Oval parameters — center with slight shift for left/right steps
        int cx = W / 2;
        if (step == 1) cx = W / 2 - W / 10;  // hint left
        if (step == 2) cx = W / 2 + W / 10;  // hint right
        int cy = H / 2 - H / 20;             // slightly above center (face is upper half)
        int rx = (int)(W * 0.22);             // horizontal radius
        int ry = (int)(H * 0.38);             // vertical radius (taller for face)

        // Oval color: green if face detected, white otherwise
        int ovalArgb = faceDetected
                ? 0xFF4CAF50   // green
                : 0xFFFFFFFF;  // white
        int shadowArgb = 0x66000000; // semi-transparent black shadow

        // Draw oval outline (3px thick)
        for (int thickness = 0; thickness < 3; thickness++) {
            int trx = rx - thickness;
            int try_ = ry - thickness;
            if (trx <= 0 || try_ <= 0) break;
            drawOval(pw, cx, cy, trx, try_, W, H, ovalArgb);
        }

        // Darken area OUTSIDE the oval (focus effect)
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double dx = (double)(x - cx) / rx;
                double dy = (double)(y - cy) / ry;
                if (dx * dx + dy * dy > 1.0) {
                    int orig = pr.getArgb(x, y);
                    int r = (int)(((orig >> 16) & 0xFF) * 0.45);
                    int g = (int)(((orig >>  8) & 0xFF) * 0.45);
                    int b = (int)(( orig        & 0xFF) * 0.45);
                    pw.setArgb(x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
                }
            }
        }

        // Re-draw oval on top (after darkening)
        for (int thickness = 0; thickness < 4; thickness++) {
            int trx = rx - thickness;
            int try_ = ry - thickness;
            if (trx <= 0 || try_ <= 0) break;
            drawOval(pw, cx, cy, trx, try_, W, H, ovalArgb);
        }

        // Corner brackets (top-left, top-right, bottom-left, bottom-right of oval bounding box)
        int bx1 = cx - rx, by1 = cy - ry;
        int bx2 = cx + rx, by2 = cy + ry;
        int bLen = Math.min(W, H) / 10;
        drawBracket(pw, bx1, by1,  1,  1, bLen, W, H, ovalArgb);
        drawBracket(pw, bx2, by1, -1,  1, bLen, W, H, ovalArgb);
        drawBracket(pw, bx1, by2,  1, -1, bLen, W, H, ovalArgb);
        drawBracket(pw, bx2, by2, -1, -1, bLen, W, H, ovalArgb);

        return out;
    }

    private static void drawOval(PixelWriter pw, int cx, int cy, int rx, int ry,
                                  int W, int H, int argb) {
        // Parametric oval
        int steps = (int)(2 * Math.PI * Math.max(rx, ry) * 2);
        for (int i = 0; i < steps; i++) {
            double angle = 2 * Math.PI * i / steps;
            int x = cx + (int)(rx * Math.cos(angle));
            int y = cy + (int)(ry * Math.sin(angle));
            if (x >= 0 && x < W && y >= 0 && y < H)
                pw.setArgb(x, y, argb);
        }
    }

    private static void drawBracket(PixelWriter pw, int x, int y, int dx, int dy,
                                     int len, int W, int H, int argb) {
        // Horizontal arm
        for (int i = 0; i < len; i++) {
            int px = x + dx * i, py = y;
            if (px >= 0 && px < W && py >= 0 && py < H) pw.setArgb(px, py, argb);
            if (px >= 0 && px < W && py+1 >= 0 && py+1 < H) pw.setArgb(px, py+1, argb);
        }
        // Vertical arm
        for (int i = 0; i < len; i++) {
            int px = x, py = y + dy * i;
            if (px >= 0 && px < W && py >= 0 && py < H) pw.setArgb(px, py, argb);
            if (px+1 >= 0 && px+1 < W && py >= 0 && py < H) pw.setArgb(px+1, py, argb);
        }
    }
}
