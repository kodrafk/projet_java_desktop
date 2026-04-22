package tn.esprit.projet.services;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Generates a 512-float face embedding from 3 image byte arrays
 * (straight, left, right) using the exact algorithm from the Symfony app,
 * then encrypts it with AES-256-GCM.
 */
public class FaceEmbeddingService {

    private static final String FACE_EMBEDDING_KEY =
            "2cd597a8f34bd09303bf8b0a3a24428de34f86d8b8b9df1d3d8e89c638488cd5";

    // ── Embedding generation ───────────────────────────────────────────────────

    /**
     * Generate a 512-float embedding from 3 JPEG byte arrays.
     * @param images array of 3 byte[] (straight, left, right)
     * @return double[512] L2-normalized unit vector
     */
    public double[] generateEmbedding(byte[][] images) throws Exception {
        List<Double> embedding = new ArrayList<>();

        for (byte[] imageBytes : images) {
            processImage(imageBytes, embedding);
        }

        // Normalize to exactly 512 dimensions
        double[] result = new double[512];
        int size = Math.min(embedding.size(), 512);
        for (int i = 0; i < size; i++) result[i] = embedding.get(i);
        // Pad with 0.0 if < 512 (already 0.0 by default)

        // L2 normalization
        return l2Normalize(result);
    }

    private void processImage(byte[] imageBytes, List<Double> embedding) throws Exception {
        // Step 1 — Byte sampling at intervals 500 and 2000
        for (int interval : new int[]{500, 2000}) {
            int count = 0;
            for (int i = 0; i < imageBytes.length && count < 15; i += interval, count++) {
                double value = (imageBytes[i] & 0xFF) / 127.5 - 1.0;
                embedding.add(value);
            }
        }

        // Step 2 — MD5 hash
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5Hash = md5.digest(imageBytes);
        String md5Hex = bytesToHex(md5Hash);
        String md5Sample = md5Hex.substring(0, Math.min(24, md5Hex.length()));
        for (int i = 0; i + 1 < md5Sample.length() && embedding.size() < 350; i += 2) {
            int val = Integer.parseInt(md5Sample.substring(i, i + 2), 16);
            double normalized = (val / 255.0) * 2 - 1;
            embedding.add(normalized);
        }

        // Step 3 — CRC32 hash
        CRC32 crc32 = new CRC32();
        crc32.update(imageBytes);
        String crcHex = Long.toHexString(crc32.getValue());
        String crcSample = crcHex.substring(0, Math.min(24, crcHex.length()));
        for (int i = 0; i + 1 < crcSample.length() && embedding.size() < 350; i += 2) {
            int val = Integer.parseInt(crcSample.substring(i, i + 2), 16);
            double normalized = (val / 255.0) * 2 - 1;
            embedding.add(normalized);
        }

        // Step 4 — Byte frequency analysis on first 5000 bytes (sample every 10)
        int[] frequency = new int[256];
        int sampleSize = 0;
        for (int i = 0; i < Math.min(imageBytes.length, 5000); i += 10) {
            frequency[imageBytes[i] & 0xFF]++;
            sampleSize++;
        }
        for (int i = 0; i < 256; i += 32) {
            double freq = frequency[i] / Math.max(sampleSize / 10.0, 1.0);
            double normalized = freq * 2 - 1;
            embedding.add(normalized);
        }

        // Step 5 — Chunk analysis (10 equal chunks)
        int chunkSize = Math.max(1, imageBytes.length / 10);
        for (int chunk = 0; chunk < 10; chunk++) {
            int start = chunk * chunkSize;
            int end = Math.min((chunk + 1) * chunkSize, imageBytes.length);
            byte[] chunkBytes = Arrays.copyOfRange(imageBytes, start, end);
            CRC32 chunkCrc = new CRC32();
            chunkCrc.update(chunkBytes);
            double value = (chunkCrc.getValue() % 256) / 127.5 - 1.0;
            embedding.add(value);
        }
    }

    private double[] l2Normalize(double[] v) {
        double magnitude = 0;
        for (double x : v) magnitude += x * x;
        magnitude = Math.sqrt(magnitude);
        if (magnitude == 0) return v;
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) result[i] = v[i] / magnitude;
        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ── AES-256-GCM Encryption ─────────────────────────────────────────────────

    public static class EncryptedEmbedding {
        public final String encryptedB64;
        public final String ivB64;
        public final String tagB64;

        public EncryptedEmbedding(String encryptedB64, String ivB64, String tagB64) {
            this.encryptedB64 = encryptedB64;
            this.ivB64        = ivB64;
            this.tagB64       = tagB64;
        }
    }

    /** Encrypt a 512-float embedding with AES-256-GCM */
    public EncryptedEmbedding encrypt(double[] embedding) throws Exception {
        // Serialize embedding to bytes (8 bytes per double)
        ByteBuffer buf = ByteBuffer.allocate(embedding.length * 8);
        for (double v : embedding) buf.putDouble(v);
        byte[] dataBytes = buf.array();

        // Derive 32-byte key from constant
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(FACE_EMBEDDING_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Random 16-byte IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        // Encrypt
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedWithTag = cipher.doFinal(dataBytes);

        // Split: last 16 bytes = GCM tag, rest = ciphertext
        byte[] encrypted = Arrays.copyOf(encryptedWithTag, encryptedWithTag.length - 16);
        byte[] tag = Arrays.copyOfRange(encryptedWithTag, encryptedWithTag.length - 16, encryptedWithTag.length);

        return new EncryptedEmbedding(
                Base64.getEncoder().encodeToString(encrypted),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(tag)
        );
    }

    /** Decrypt an encrypted embedding back to double[512] */
    public double[] decrypt(String encryptedB64, String ivB64, String tagB64) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encryptedB64);
        byte[] iv        = Base64.getDecoder().decode(ivB64);
        byte[] tag       = Base64.getDecoder().decode(tagB64);

        // Derive key
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(FACE_EMBEDDING_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Reconstruct ciphertext + tag
        byte[] encryptedWithTag = new byte[encrypted.length + tag.length];
        System.arraycopy(encrypted, 0, encryptedWithTag, 0, encrypted.length);
        System.arraycopy(tag, 0, encryptedWithTag, encrypted.length, tag.length);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] dataBytes = cipher.doFinal(encryptedWithTag);

        // Deserialize doubles
        ByteBuffer buf = ByteBuffer.wrap(dataBytes);
        double[] embedding = new double[dataBytes.length / 8];
        for (int i = 0; i < embedding.length; i++) embedding[i] = buf.getDouble();
        return embedding;
    }

    // ── Similarity ─────────────────────────────────────────────────────────────

    /** Cosine similarity between two unit vectors (both L2-normalized) */
    public double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null) return 0;
        int len = Math.min(a.length, b.length);
        double dot = 0;
        for (int i = 0; i < len; i++) dot += a[i] * b[i];
        return dot; // already unit vectors, so dot product = cosine similarity
    }

    /** Euclidean distance */
    public double euclideanDistance(double[] a, double[] b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        int len = Math.min(a.length, b.length);
        double sum = 0;
        for (int i = 0; i < len; i++) { double d = a[i] - b[i]; sum += d * d; }
        return Math.sqrt(sum);
    }

    /** Returns true if similarity >= 0.75 (cosine) or distance <= 0.6 (euclidean) */
    public boolean isMatch(double[] stored, double[] live) {
        double sim = cosineSimilarity(stored, live);
        return sim >= 0.75;
    }
}
