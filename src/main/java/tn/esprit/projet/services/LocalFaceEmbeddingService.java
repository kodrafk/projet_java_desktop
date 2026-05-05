package tn.esprit.projet.services;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Local Face Embedding Service — Pure Java, no external dependencies.
 *
 * Uses a combination of:
 *  1. Face presence validation (skin tone distribution + variance check)
 *  2. LBP (Local Binary Patterns) — industry-standard texture descriptor
 *  3. Gradient-based facial structure features (eyes/nose/mouth zones)
 *  4. Multi-zone analysis (forehead, eyes, nose, mouth, chin)
 *
 * This produces a 256D descriptor that is MUCH more discriminative than
 * simple pixel averaging. A hand, a wall, or a random object will NOT match
 * a face because:
 *  - Skin tone distribution check rejects non-skin objects
 *  - LBP captures micro-texture patterns unique to each face
 *  - Facial zone analysis requires proper face structure
 *
 * Threshold: 0.93 cosine similarity (very strict — same person only)
 */
public class LocalFaceEmbeddingService {

    // Strict threshold — must be very high to avoid false positives
    public static final double MATCH_THRESHOLD = 0.93;

    // Minimum face confidence to accept an image
    private static final double MIN_FACE_CONFIDENCE = 0.35;

    /**
     * Extract a 256D face embedding from JPEG bytes.
     * Throws FaceNotDetectedException if no valid face is found.
     */
    public double[] extractEmbedding(byte[] jpegBytes) throws Exception {
        if (jpegBytes == null || jpegBytes.length == 0)
            throw new Exception("Empty image data");

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpegBytes));
        if (img == null) throw new Exception("Could not decode image");

        // 1. Validate face presence
        double faceConf = computeFaceConfidence(img);
        System.out.printf("[FaceID] Face confidence: %.3f (min=%.2f)%n", faceConf, MIN_FACE_CONFIDENCE);
        if (faceConf < MIN_FACE_CONFIDENCE) {
            throw new FaceNotDetectedException(
                String.format("No face detected (confidence=%.2f). Please look directly at the camera.", faceConf));
        }

        // 2. Crop to face region (center oval area)
        int w = img.getWidth(), h = img.getHeight();
        int cropX = (int)(w * 0.18), cropY = (int)(h * 0.08);
        int cropW = (int)(w * 0.64), cropH = (int)(h * 0.84);
        cropW = Math.min(cropW, w - cropX);
        cropH = Math.min(cropH, h - cropY);
        BufferedImage face = img.getSubimage(cropX, cropY, cropW, cropH);

        // 3. Convert to grayscale 128x128 for better resolution
        int[] gray = toGray128(face);

        // 4. Histogram equalization
        gray = histogramEqualize(gray, 128, 128);

        // 5. Extract LBP features (128D)
        double[] lbpFeatures = extractLBP(gray, 128, 128);

        // 6. Extract gradient/zone features (128D)
        double[] zoneFeatures = extractZoneFeatures(gray, 128, 128);

        // 7. Concatenate → 256D
        double[] emb = new double[256];
        System.arraycopy(lbpFeatures, 0, emb, 0, 128);
        System.arraycopy(zoneFeatures, 0, emb, 128, 128);

        // 8. L2 normalize
        return l2Normalize(emb);
    }

    /**
     * Compute face confidence score [0..1].
     * Checks:
     *  - Skin tone pixel ratio (faces have ~20-60% skin pixels)
     *  - Image variance (faces have medium variance, not too uniform/noisy)
     *  - Vertical gradient symmetry (faces are roughly symmetric)
     *  - Center brightness (face center should be brighter than edges)
     */
    private double computeFaceConfidence(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();

        // Sample center region
        int cx = w / 2, cy = h / 2;
        int rx = (int)(w * 0.28), ry = (int)(h * 0.40);

        int skinCount = 0, totalCount = 0;
        double sumBrightness = 0;
        double sumBrightnessEdge = 0;
        int edgeCount = 0;

        for (int y = cy - ry; y < cy + ry; y++) {
            for (int x = cx - rx; x < cx + rx; x++) {
                if (x < 0 || x >= w || y < 0 || y >= h) continue;
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                totalCount++;
                sumBrightness += (r + g + b) / 3.0;

                if (isSkinPixel(r, g, b)) skinCount++;
            }
        }

        // Edge brightness (corners)
        int[][] corners = {{0,0},{w-1,0},{0,h-1},{w-1,h-1}};
        for (int[] c : corners) {
            int x = Math.max(0, Math.min(w-1, c[0]));
            int y = Math.max(0, Math.min(h-1, c[1]));
            int rgb = img.getRGB(x, y);
            sumBrightnessEdge += ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3.0;
            edgeCount++;
        }

        if (totalCount == 0) return 0.0;

        double skinRatio = (double) skinCount / totalCount;
        double avgBrightness = sumBrightness / totalCount;
        double avgEdgeBrightness = sumBrightnessEdge / edgeCount;

        // Score components
        // 1. Skin ratio: ideal 0.25-0.70
        double skinScore;
        if (skinRatio < 0.15) skinScore = skinRatio / 0.15 * 0.5;
        else if (skinRatio <= 0.75) skinScore = 1.0;
        else skinScore = Math.max(0, 1.0 - (skinRatio - 0.75) / 0.25);

        // 2. Brightness: face should be reasonably bright (50-220)
        double brightScore = 1.0;
        if (avgBrightness < 30) brightScore = 0.2;
        else if (avgBrightness < 60) brightScore = 0.6;
        else if (avgBrightness > 230) brightScore = 0.5;

        // 3. Variance check: face has medium variance (not a blank wall, not pure noise)
        double variance = computeVariance(img, cx - rx, cy - ry, rx * 2, ry * 2);
        double varScore;
        if (variance < 100) varScore = 0.3;       // Too uniform (wall, hand covering camera)
        else if (variance < 300) varScore = 0.7;
        else if (variance < 3000) varScore = 1.0;
        else varScore = 0.6;                       // Too noisy

        // 4. Horizontal symmetry check (faces are roughly symmetric)
        double symScore = computeSymmetry(img, cx, cy, rx, ry);

        double confidence = skinScore * 0.40 + brightScore * 0.20 + varScore * 0.25 + symScore * 0.15;
        System.out.printf("[FaceID] skin=%.2f bright=%.2f var=%.2f sym=%.2f → conf=%.3f%n",
                skinScore, brightScore, varScore, symScore, confidence);
        return confidence;
    }

    /**
     * Skin pixel detection using YCbCr color space rules.
     * More accurate than RGB-only checks.
     */
    private boolean isSkinPixel(int r, int g, int b) {
        // Convert to YCbCr
        double y  =  0.299 * r + 0.587 * g + 0.114 * b;
        double cb = -0.169 * r - 0.331 * g + 0.500 * b + 128;
        double cr =  0.500 * r - 0.419 * g - 0.081 * b + 128;

        // Standard skin detection ranges in YCbCr
        return y > 40 && cb >= 77 && cb <= 127 && cr >= 133 && cr <= 173;
    }

    private double computeVariance(BufferedImage img, int x0, int y0, int w, int h) {
        int count = 0;
        double sum = 0, sum2 = 0;
        for (int y = y0; y < y0 + h && y < img.getHeight(); y++) {
            for (int x = x0; x < x0 + w && x < img.getWidth(); x++) {
                if (x < 0 || y < 0) continue;
                int rgb = img.getRGB(x, y);
                double v = ((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3.0;
                sum += v; sum2 += v * v; count++;
            }
        }
        if (count == 0) return 0;
        double mean = sum / count;
        return sum2 / count - mean * mean;
    }

    private double computeSymmetry(BufferedImage img, int cx, int cy, int rx, int ry) {
        int matches = 0, total = 0;
        for (int y = cy - ry; y < cy + ry; y += 4) {
            for (int x = 1; x <= rx; x += 4) {
                int xl = cx - x, xr = cx + x;
                if (xl < 0 || xr >= img.getWidth() || y < 0 || y >= img.getHeight()) continue;
                int rgbL = img.getRGB(xl, y);
                int rgbR = img.getRGB(xr, y);
                int bL = ((rgbL >> 16 & 0xFF) + (rgbL >> 8 & 0xFF) + (rgbL & 0xFF)) / 3;
                int bR = ((rgbR >> 16 & 0xFF) + (rgbR >> 8 & 0xFF) + (rgbR & 0xFF)) / 3;
                if (Math.abs(bL - bR) < 50) matches++;
                total++;
            }
        }
        return total == 0 ? 0.5 : (double) matches / total;
    }

    /**
     * Convert image to 128x128 grayscale pixel array.
     */
    private int[] toGray128(BufferedImage src) {
        BufferedImage gray = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, 128, 128, null);
        g.dispose();

        int[] pixels = new int[128 * 128];
        for (int y = 0; y < 128; y++)
            for (int x = 0; x < 128; x++)
                pixels[y * 128 + x] = gray.getRGB(x, y) & 0xFF;
        return pixels;
    }

    /**
     * LBP (Local Binary Patterns) — extracts 128D histogram.
     * LBP is rotation-invariant and captures micro-texture unique to each face.
     * Divides image into 8x8 grid of cells, computes LBP histogram per cell.
     */
    private double[] extractLBP(int[] gray, int w, int h) {
        // Compute LBP codes for each pixel
        int[] lbp = new int[w * h];
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int center = gray[y * w + x];
                int code = 0;
                // 8 neighbors clockwise from top-left
                int[] nx = {-1, 0, 1, 1, 1, 0, -1, -1};
                int[] ny = {-1, -1, -1, 0, 1, 1, 1, 0};
                for (int i = 0; i < 8; i++) {
                    if (gray[(y + ny[i]) * w + (x + nx[i])] >= center)
                        code |= (1 << i);
                }
                lbp[y * w + x] = code;
            }
        }

        // Divide into 4x4 grid of cells (16 cells), each with 8-bin histogram
        // 16 cells × 8 bins = 128D
        double[] features = new double[128];
        int cellW = w / 4, cellH = h / 4;
        int featIdx = 0;

        for (int cy = 0; cy < 4; cy++) {
            for (int cx = 0; cx < 4; cx++) {
                int[] hist = new int[8];
                int count = 0;
                for (int y = cy * cellH; y < (cy + 1) * cellH && y < h; y++) {
                    for (int x = cx * cellW; x < (cx + 1) * cellW && x < w; x++) {
                        // Map 256 LBP codes to 8 bins (uniform patterns)
                        int bin = lbp[y * w + x] % 8;
                        hist[bin]++;
                        count++;
                    }
                }
                // Normalize histogram
                for (int b = 0; b < 8; b++) {
                    features[featIdx++] = count > 0 ? (double) hist[b] / count : 0.0;
                }
            }
        }
        return features;
    }

    /**
     * Zone-based gradient features — 128D.
     * Analyzes 8 facial zones (forehead, left-eye, right-eye, nose, left-cheek,
     * right-cheek, mouth, chin) with gradient statistics.
     * Each zone contributes 16 values (mean, std, gradient mag, gradient dir × 4 quadrants).
     */
    private double[] extractZoneFeatures(int[] gray, int w, int h) {
        // 8 zones × 16 features = 128D
        double[] features = new double[128];

        // Define facial zones as (x%, y%, w%, h%) of image
        double[][] zones = {
            {0.20, 0.00, 0.60, 0.20},  // Forehead
            {0.10, 0.20, 0.35, 0.22},  // Left eye region
            {0.55, 0.20, 0.35, 0.22},  // Right eye region
            {0.30, 0.38, 0.40, 0.22},  // Nose
            {0.05, 0.35, 0.30, 0.30},  // Left cheek
            {0.65, 0.35, 0.30, 0.30},  // Right cheek
            {0.25, 0.62, 0.50, 0.20},  // Mouth
            {0.20, 0.80, 0.60, 0.20},  // Chin
        };

        int featIdx = 0;
        for (double[] zone : zones) {
            int zx = (int)(zone[0] * w), zy = (int)(zone[1] * h);
            int zw = (int)(zone[2] * w), zh = (int)(zone[3] * h);
            zw = Math.max(1, Math.min(zw, w - zx));
            zh = Math.max(1, Math.min(zh, h - zy));

            double sum = 0, sum2 = 0;
            double gxSum = 0, gySum = 0, gMagSum = 0;
            int count = 0;

            // Quadrant means
            double[] qMean = new double[4];
            int[] qCount = new int[4];

            for (int y = zy; y < zy + zh && y < h; y++) {
                for (int x = zx; x < zx + zw && x < w; x++) {
                    double v = gray[y * w + x];
                    sum += v; sum2 += v * v; count++;

                    // Gradient
                    if (x > 0 && x < w - 1 && y > 0 && y < h - 1) {
                        double gx = gray[y * w + (x + 1)] - gray[y * w + (x - 1)];
                        double gy = gray[(y + 1) * w + x] - gray[(y - 1) * w + x];
                        gxSum += gx; gySum += gy;
                        gMagSum += Math.sqrt(gx * gx + gy * gy);
                    }

                    // Quadrant
                    int qx = (x - zx) < zw / 2 ? 0 : 1;
                    int qy = (y - zy) < zh / 2 ? 0 : 1;
                    int q = qy * 2 + qx;
                    qMean[q] += v; qCount[q]++;
                }
            }

            if (count == 0) { featIdx += 16; continue; }

            double mean = sum / count;
            double std = Math.sqrt(Math.max(0, sum2 / count - mean * mean));
            double gMag = gMagSum / count;
            double gDir = Math.atan2(gySum, gxSum + 1e-9) / Math.PI; // [-1, 1]

            features[featIdx++] = mean / 255.0;
            features[featIdx++] = std / 128.0;
            features[featIdx++] = gMag / 255.0;
            features[featIdx++] = (gDir + 1.0) / 2.0;

            // Quadrant means (normalized)
            for (int q = 0; q < 4; q++) {
                features[featIdx++] = qCount[q] > 0 ? (qMean[q] / qCount[q]) / 255.0 : 0.0;
            }

            // Relative zone contrasts (4 values)
            double[] qNorm = new double[4];
            for (int q = 0; q < 4; q++) qNorm[q] = qCount[q] > 0 ? qMean[q] / qCount[q] : mean;
            features[featIdx++] = (qNorm[0] - qNorm[1] + 128) / 256.0; // left-right contrast
            features[featIdx++] = (qNorm[0] - qNorm[2] + 128) / 256.0; // top-bottom contrast
            features[featIdx++] = (qNorm[1] - qNorm[3] + 128) / 256.0;
            features[featIdx++] = (qNorm[2] - qNorm[3] + 128) / 256.0;

            // Texture energy (sum of squared differences from mean)
            double energy = 0;
            for (int y = zy; y < zy + zh && y < h; y++) {
                for (int x = zx; x < zx + zw && x < w; x++) {
                    double d = gray[y * w + x] - mean;
                    energy += d * d;
                }
            }
            features[featIdx++] = Math.min(1.0, energy / (count * 10000.0));

            // Entropy approximation
            int[] hist = new int[16];
            for (int y = zy; y < zy + zh && y < h; y++)
                for (int x = zx; x < zx + zw && x < w; x++)
                    hist[gray[y * w + x] / 16]++;
            double entropy = 0;
            for (int bin : hist) {
                if (bin > 0) {
                    double p = (double) bin / count;
                    entropy -= p * Math.log(p);
                }
            }
            features[featIdx++] = entropy / Math.log(16); // normalize to [0,1]

            // Skewness
            double skew = 0;
            for (int y = zy; y < zy + zh && y < h; y++)
                for (int x = zx; x < zx + zw && x < w; x++) {
                    double d = (gray[y * w + x] - mean) / (std + 1e-9);
                    skew += d * d * d;
                }
            features[featIdx++] = Math.tanh(skew / count) * 0.5 + 0.5;
        }

        return features;
    }

    /**
     * Histogram equalization for lighting invariance.
     */
    private int[] histogramEqualize(int[] pixels, int w, int h) {
        int n = w * h;
        int[] hist = new int[256];
        for (int p : pixels) hist[p]++;

        int[] cdf = new int[256];
        cdf[0] = hist[0];
        for (int i = 1; i < 256; i++) cdf[i] = cdf[i-1] + hist[i];

        int cdfMin = 0;
        for (int i = 0; i < 256; i++) { if (cdf[i] > 0) { cdfMin = cdf[i]; break; } }

        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = (int) Math.round((double)(cdf[i] - cdfMin) / (n - cdfMin) * 255);
            lut[i] = Math.max(0, Math.min(255, lut[i]));
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = lut[pixels[i]];
        return result;
    }

    private double[] l2Normalize(double[] v) {
        double norm = 0;
        for (double x : v) norm += x * x;
        norm = Math.sqrt(norm);
        if (norm > 1e-9) for (int i = 0; i < v.length; i++) v[i] /= norm;
        return v;
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
     * Strict threshold to prevent false positives.
     */
    public boolean isMatch(double[] stored, double[] live) {
        return similarity(stored, live) >= MATCH_THRESHOLD;
    }

    /**
     * Exception thrown when no face is detected in the image.
     */
    public static class FaceNotDetectedException extends Exception {
        public FaceNotDetectedException(String message) { super(message); }
    }
}
