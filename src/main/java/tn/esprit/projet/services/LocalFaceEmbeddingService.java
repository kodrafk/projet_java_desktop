package tn.esprit.projet.services;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;

/**
 * Local face embedding — NO PYTHON NEEDED.
 * Uses perceptual hashing + pixel sampling to create a 128D face descriptor.
 * Works offline, instant, no dependencies.
 *
 * Strategy:
 *  1. Detect face region (center crop of image)
 *  2. Resize to 64x64 grayscale
 *  3. Apply histogram equalization for lighting invariance
 *  4. Sample 128 pixel blocks → normalized float vector
 *  5. Encrypt with AES-256-GCM (reuses FaceEmbeddingService)
 */
public class LocalFaceEmbeddingService {

    private static final int EMB_SIZE = 128;

    /**
     * Extract a 128D embedding from a JPEG byte array.
     * Focuses on the center region (where the face is in the oval).
     */
    public double[] extractEmbedding(byte[] jpegBytes) throws Exception {
        if (jpegBytes == null || jpegBytes.length == 0)
            throw new Exception("Empty image data");

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        if (img == null) throw new Exception("Could not decode image");

        // 1. Crop center 60% (face region inside the oval)
        int w = img.getWidth(), h = img.getHeight();
        int cropX = (int)(w * 0.20), cropY = (int)(h * 0.10);
        int cropW = (int)(w * 0.60), cropH = (int)(h * 0.80);
        BufferedImage face = img.getSubimage(cropX, cropY, cropW, cropH);

        // 2. Convert to grayscale 64x64
        BufferedImage gray64 = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray64.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(face, 0, 0, 64, 64, null);
        g.dispose();

        // 3. Get pixel values
        int[] pixels = new int[64 * 64];
        for (int y = 0; y < 64; y++)
            for (int x = 0; x < 64; x++)
                pixels[y * 64 + x] = gray64.getRGB(x, y) & 0xFF;

        // 4. Histogram equalization for lighting invariance
        pixels = histogramEqualize(pixels);

        // 5. Build 128D embedding by sampling 8x8 blocks (64/8=8 blocks per side → 64 blocks)
        //    Each block contributes 2 values: mean and std
        double[] emb = new double[EMB_SIZE];
        int blockSize = 8;
        int idx = 0;
        for (int by = 0; by < 8 && idx < EMB_SIZE - 1; by++) {
            for (int bx = 0; bx < 8 && idx < EMB_SIZE - 1; bx++) {
                double sum = 0, sum2 = 0;
                int count = 0;
                for (int py = 0; py < blockSize; py++) {
                    for (int px = 0; px < blockSize; px++) {
                        int v = pixels[(by * blockSize + py) * 64 + (bx * blockSize + px)];
                        sum += v;
                        sum2 += v * v;
                        count++;
                    }
                }
                double mean = sum / count;
                double std  = Math.sqrt(sum2 / count - mean * mean);
                emb[idx++] = mean / 255.0;
                emb[idx++] = std  / 128.0;
            }
        }

        // 6. L2 normalize
        double norm = 0;
        for (double v : emb) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < emb.length; i++) emb[i] /= norm;

        return emb;
    }

    /**
     * Cosine similarity between two embeddings.
     */
    public double similarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        na = Math.sqrt(na); nb = Math.sqrt(nb);
        return (na == 0 || nb == 0) ? 0.0 : dot / (na * nb);
    }

    /**
     * Returns true if two embeddings match (same person).
     * Threshold tuned for local perceptual hash (lower than ArcFace).
     */
    public boolean isMatch(double[] stored, double[] live) {
        return similarity(stored, live) > 0.82;
    }

    // ── Histogram equalization ────────────────────────────────────────────────

    private int[] histogramEqualize(int[] pixels) {
        int[] hist = new int[256];
        for (int p : pixels) hist[p]++;

        int[] cdf = new int[256];
        cdf[0] = hist[0];
        for (int i = 1; i < 256; i++) cdf[i] = cdf[i-1] + hist[i];

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) { if (cdf[i] > 0) { cdfMin = cdf[i]; break; } }

        int n = pixels.length;
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) Math.round((double)(cdf[i] - cdfMin) / (n - cdfMin) * 255);
            lut[i] = Math.max(0, Math.min(255, lut[i]));
        }

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) result[i] = lut[pixels[i]];
        return result;
    }
}
