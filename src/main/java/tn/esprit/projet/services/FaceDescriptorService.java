package tn.esprit.projet.services;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts a 128-float face descriptor from a JavaFX Image.
 *
 * Algorithm: Divide the face region into an 8x8 grid of blocks.
 * For each block, compute 2 values: mean brightness and local contrast.
 * This gives 8*8*2 = 128 values — a lightweight but consistent descriptor.
 *
 * Euclidean distance ≤ 0.6 between two descriptors = same person.
 */
public class FaceDescriptorService {

    private static final int GRID = 8;   // 8x8 grid → 128 values
    private static final double THRESHOLD = 0.6;

    /**
     * Extract 128-float descriptor from a JavaFX Image.
     * The image should be a face crop (or the full frame — we sample the center region).
     */
    public double[] extract(Image image) {
        if (image == null) return null;
        PixelReader pr = image.getPixelReader();
        int W = (int) image.getWidth();
        int H = (int) image.getHeight();
        if (W == 0 || H == 0) return null;

        // Use center 80% of the image as face region
        int x0 = (int)(W * 0.1);
        int y0 = (int)(H * 0.1);
        int fw = (int)(W * 0.8);
        int fh = (int)(H * 0.8);

        int blockW = fw / GRID;
        int blockH = fh / GRID;

        double[] descriptor = new double[GRID * GRID * 2];
        int idx = 0;

        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < GRID; col++) {
                int bx = x0 + col * blockW;
                int by = y0 + row * blockH;

                List<Double> pixels = new ArrayList<>();
                for (int py = by; py < by + blockH && py < H; py++) {
                    for (int px = bx; px < bx + blockW && px < W; px++) {
                        int argb = pr.getArgb(px, py);
                        double r = ((argb >> 16) & 0xFF) / 255.0;
                        double g = ((argb >> 8)  & 0xFF) / 255.0;
                        double b = (argb & 0xFF)         / 255.0;
                        // Luminance
                        pixels.add(0.299 * r + 0.587 * g + 0.114 * b);
                    }
                }

                double mean = pixels.stream().mapToDouble(d -> d).average().orElse(0);
                double variance = pixels.stream()
                        .mapToDouble(d -> (d - mean) * (d - mean))
                        .average().orElse(0);
                double stddev = Math.sqrt(variance);

                descriptor[idx++] = mean;
                descriptor[idx++] = stddev;
            }
        }

        // L2-normalize the descriptor
        return normalize(descriptor);
    }

    /** Euclidean distance between two descriptors */
    public double distance(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) return Double.MAX_VALUE;
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    public boolean matches(double[] a, double[] b) {
        return distance(a, b) <= THRESHOLD;
    }

    /** Serialize descriptor to JSON string */
    public String toJson(double[] d) {
        if (d == null) return null;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < d.length; i++) {
            sb.append(String.format("%.6f", d[i]));
            if (i < d.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Parse JSON string back to double[] */
    public double[] fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            String inner = json.trim().replaceAll("[\\[\\]\\s]", "");
            String[] parts = inner.split(",");
            double[] d = new double[parts.length];
            for (int i = 0; i < parts.length; i++)
                d[i] = Double.parseDouble(parts[i]);
            return d;
        } catch (Exception e) { return null; }
    }

    private double[] normalize(double[] d) {
        double norm = 0;
        for (double v : d) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm == 0) return d;
        double[] result = new double[d.length];
        for (int i = 0; i < d.length; i++) result[i] = d[i] / norm;
        return result;
    }
}
