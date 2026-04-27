package tn.esprit.projet.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Face embedding service — calls Python DeepFace (ArcFace) via ProcessBuilder.
 * Embeddings are encrypted with AES-256-GCM before storage.
 */
public class FaceEmbeddingService {

    private static final String FACE_EMBEDDING_KEY =
            "2cd597a8f34bd09303bf8b0a3a24428de34f86d8b8b9df1d3d8e89c638488cd5";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Python resolution ──────────────────────────────────────────────────────

    private static String resolvePythonScript() {
        return resolveScript("face_recognition_service.py");
    }

    /** Resolve path to any script in the project root. */
    public static String resolveScript(String scriptName) {
        String[] candidates = {
            scriptName,
            "projetJAV/" + scriptName,
            System.getProperty("user.dir") + "/" + scriptName,
            System.getProperty("user.dir") + "/projetJAV/" + scriptName
        };
        for (String c : candidates) {
            if (new File(c).exists()) return c;
        }
        return scriptName;
    }

    /** Public alias for use by CameraServerService. */
    public static String resolvePython() {
        return getPythonExecutable();
    }

    private static String getPythonExecutable() {
        String home = System.getProperty("user.home");
        String[] winPaths = {
            home + "\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
            home + "\\AppData\\Local\\Programs\\Python\\Python312\\python.exe",
            home + "\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
            home + "\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
            "C:\\Python313\\python.exe",
            "C:\\Python312\\python.exe",
        };
        for (String path : winPaths) {
            if (new File(path).exists()) {
                System.out.println("[FaceID] Python: " + path);
                return path;
            }
        }
        for (String py : new String[]{"python3", "python", "py"}) {
            try {
                Process p = new ProcessBuilder(py, "--version").redirectErrorStream(true).start();
                String out = new BufferedReader(new InputStreamReader(p.getInputStream()))
                        .lines().collect(Collectors.joining());
                p.waitFor();
                if (p.exitValue() == 0 && out.toLowerCase().contains("python")) return py;
            } catch (Exception ignored) {}
        }
        return "python";
    }

    // ── Main Python call ───────────────────────────────────────────────────────

    /**
     * Send a JSON request to the Python face recognition script via stdin/stdout.
     * Reads stderr in real-time (shows DeepFace download/load progress).
     * Timeout: 180 seconds (first run downloads ArcFace model ~100MB).
     */
    public double[] callPythonForEmbedding(String stdinJson) throws Exception {
        String script = resolveScript("face_recognition_service.py");
        String python = getPythonExecutable();

        System.out.println("[FaceID] Calling Python: " + python);

        ProcessBuilder pb = new ProcessBuilder(python, script);
        pb.redirectErrorStream(false);
        Process proc = pb.start();

        // Write JSON to stdin then close
        try (OutputStream os = proc.getOutputStream()) {
            os.write(stdinJson.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        // Read stderr in background (DeepFace progress logs)
        StringBuilder stderrBuf = new StringBuilder();
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    stderrBuf.append(line).append("\n");
                    System.err.println("[FaceID-py] " + line);
                }
            } catch (Exception ignored) {}
        });
        stderrThread.setDaemon(true);
        stderrThread.start();

        // Read stdout (JSON result)
        StringBuilder stdoutBuf = new StringBuilder();
        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    stdoutBuf.append(line).append("\n");
                }
            } catch (Exception ignored) {}
        });
        stdoutThread.setDaemon(true);
        stdoutThread.start();

        // Wait up to 180s (first run downloads ArcFace ~100MB)
        boolean finished = proc.waitFor(180, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            throw new Exception("Face analysis timed out (180s). Check your internet connection for model download.");
        }

        stdoutThread.join(5000);
        stderrThread.join(2000);

        String stdout = stdoutBuf.toString().trim();
        String stderr = stderrBuf.toString().trim();

        if (stdout.isBlank()) {
            String errTail = stderr.length() > 400 ? stderr.substring(stderr.length() - 400) : stderr;
            throw new Exception("Python returned no output.\n" + errTail);
        }

        // Find last JSON line
        String jsonLine = stdout;
        for (String line : stdout.split("\n")) {
            if (line.trim().startsWith("{")) jsonLine = line.trim();
        }

        JsonNode node = MAPPER.readTree(jsonLine);
        if (!node.path("success").asBoolean()) {
            throw new Exception(node.path("error").asText("Unknown error"));
        }

        JsonNode embNode = node.path("embedding");
        double[] embedding = new double[embNode.size()];
        for (int i = 0; i < embNode.size(); i++) embedding[i] = embNode.get(i).asDouble();

        System.out.println("[FaceID] Embedding: " + embedding.length + "D, model=" +
                node.path("model").asText("?"));
        return embedding;
    }

    // ── Legacy methods (kept for compatibility) ────────────────────────────────

    public double[] generateEmbedding(byte[][] images) throws Exception {
        for (int idx : new int[]{1, 0, 2}) {
            if (idx >= images.length || images[idx] == null || images[idx].length == 0) continue;
            try {
                String b64 = Base64.getEncoder().encodeToString(images[idx]);
                return callPythonForEmbedding("{\"command\":\"encode\",\"image\":\"" + b64 + "\"}");
            } catch (Exception ignored) {}
        }
        throw new Exception("Could not generate face embedding.");
    }

    // ── AES-256-GCM Encryption ─────────────────────────────────────────────────

    public static class EncryptedEmbedding {
        public final String encryptedB64, ivB64, tagB64;
        public EncryptedEmbedding(String e, String iv, String t) {
            encryptedB64 = e; ivB64 = iv; tagB64 = t;
        }
    }

    public EncryptedEmbedding encrypt(double[] embedding) throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(embedding.length * 8);
        for (double v : embedding) buf.putDouble(v);
        byte[] data = buf.array();

        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(FACE_EMBEDDING_KEY.getBytes(StandardCharsets.UTF_8));
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        byte[] enc = cipher.doFinal(data);

        return new EncryptedEmbedding(
                Base64.getEncoder().encodeToString(Arrays.copyOf(enc, enc.length - 16)),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(Arrays.copyOfRange(enc, enc.length - 16, enc.length))
        );
    }

    public double[] decrypt(String encB64, String ivB64, String tagB64) throws Exception {
        byte[] enc = Base64.getDecoder().decode(encB64);
        byte[] iv  = Base64.getDecoder().decode(ivB64);
        byte[] tag = Base64.getDecoder().decode(tagB64);
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(FACE_EMBEDDING_KEY.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[enc.length + tag.length];
        System.arraycopy(enc, 0, combined, 0, enc.length);
        System.arraycopy(tag, 0, combined, enc.length, tag.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        byte[] data = cipher.doFinal(combined);

        ByteBuffer buf = ByteBuffer.wrap(data);
        double[] result = new double[data.length / 8];
        for (int i = 0; i < result.length; i++) result[i] = buf.getDouble();
        return result;
    }

    // ── Similarity ─────────────────────────────────────────────────────────────

    public double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null) return 0;
        int len = Math.min(a.length, b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < len; i++) { dot += a[i]*b[i]; na += a[i]*a[i]; nb += b[i]*b[i]; }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public double euclideanDistance(double[] a, double[] b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        int len = Math.min(a.length, b.length);
        double sum = 0;
        for (int i = 0; i < len; i++) { double d = a[i]-b[i]; sum += d*d; }
        return Math.sqrt(sum);
    }

    public boolean isMatch(double[] stored, double[] live) {
        return cosineSimilarity(stored, live) > 0.88;
    }
}
