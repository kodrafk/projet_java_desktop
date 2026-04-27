package tn.esprit.projet.services;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connects to camera_server.py (OpenCV-based) via TCP on localhost:7654.
 * Replaces sarxos WebcamService — works reliably on Windows 10/11 + Java 17.
 *
 * Protocol:
 *   → "FRAME\n"          ← "<length>\n<jpeg_bytes>"
 *   → "STATUS\n"         ← JSON {"camera": true/false}
 *   → "RETRY\n"          ← JSON {"camera": true/false}
 *   → JSON + "\n"        ← JSON result (face recognition)
 */
public class CameraServerService {

    private static final int    PORT    = 7654;
    private static final String HOST    = "127.0.0.1";
    private static final int    TIMEOUT = 8000; // ms

    private Socket socket;
    private OutputStream out;
    private InputStream  in;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    // ── Python process management ──────────────────────────────────────────────

    private static Process serverProcess;
    private static final Object PROC_LOCK = new Object();

    public static void startServer() {
        synchronized (PROC_LOCK) {
            if (serverProcess != null && serverProcess.isAlive()) return;
            try {
                String python = FaceEmbeddingService.resolvePython();
                String script = FaceEmbeddingService.resolveScript("camera_server.py");
                ProcessBuilder pb = new ProcessBuilder(python, script, String.valueOf(PORT));
                pb.redirectErrorStream(false);
                serverProcess = pb.start();

                // Drain stderr in background
                Process proc = serverProcess;
                Thread t = new Thread(() -> {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null)
                            System.err.println("[CamSrv] " + line);
                    } catch (Exception ignored) {}
                });
                t.setDaemon(true);
                t.start();

                // Wait for server to be ready (up to 12s)
                long deadline = System.currentTimeMillis() + 12000;
                while (System.currentTimeMillis() < deadline) {
                    try (Socket test = new Socket(HOST, PORT)) {
                        System.out.println("[CamSrv] Server ready on port " + PORT);
                        return;
                    } catch (Exception ignored) {
                        Thread.sleep(200);
                    }
                }
                System.err.println("[CamSrv] Server did not start in time.");
            } catch (Exception e) {
                System.err.println("[CamSrv] Could not start camera_server.py: " + e.getMessage());
            }
        }
    }

    public static void stopServer() {
        synchronized (PROC_LOCK) {
            if (serverProcess != null && serverProcess.isAlive()) {
                serverProcess.destroyForcibly();
                serverProcess = null;
            }
        }
    }

    // ── Connection ─────────────────────────────────────────────────────────────

    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            socket.setSoTimeout(TIMEOUT);
            out = socket.getOutputStream();
            in  = socket.getInputStream();
            connected.set(true);
            return true;
        } catch (Exception e) {
            System.err.println("[CamSrv] Connect failed: " + e.getMessage());
            connected.set(false);
            return false;
        }
    }

    public void disconnect() {
        connected.set(false);
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        socket = null; out = null; in = null;
    }

    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    // ── Frame grab ─────────────────────────────────────────────────────────────

    /** Grab one JPEG frame from the camera server. Returns null on failure. */
    public byte[] grabJpeg() {
        if (!ensureConnected()) return null;
        try {
            send("FRAME\n");
            String header = readLine();
            if (header == null) return null;
            int len = Integer.parseInt(header.trim());
            if (len <= 0) return null;
            return readBytes(len);
        } catch (Exception e) {
            disconnect();
            return null;
        }
    }

    /** Grab one frame as a JavaFX WritableImage. */
    public WritableImage grabFrame() {
        byte[] jpeg = grabJpeg();
        if (jpeg == null) return null;
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(jpeg));
            return bi != null ? SwingFXUtils.toFXImage(bi, null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Check if camera is available. */
    public boolean isCameraAvailable() {
        if (!ensureConnected()) return false;
        try {
            send("STATUS\n");
            String line = readLine();
            if (line == null) return false;
            // Parse JSON properly — handle spaces in {"camera": true}
            return line.contains("\"camera\"") && line.contains("true");
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    /** Ask server to retry opening the camera. */
    public boolean retryCamera() {
        if (!ensureConnected()) return false;
        try {
            send("RETRY\n");
            String line = readLine();
            if (line == null) return false;
            return line.contains("\"camera\"") && line.contains("true");
        } catch (Exception e) {
            disconnect();
            return false;
        }
    }

    /** Send a JSON face recognition command, get JSON response. */
    public String sendJson(String json) {
        if (!ensureConnected()) return "{\"success\":false,\"error\":\"Not connected\"}";
        try {
            send(json + "\n");
            return readLine();
        } catch (Exception e) {
            disconnect();
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean ensureConnected() {
        if (isConnected()) return true;
        return connect();
    }

    private void send(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') return sb.toString();
            sb.append((char) b);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private byte[] readBytes(int len) throws IOException {
        byte[] buf = new byte[len];
        int read = 0;
        while (read < len) {
            int n = in.read(buf, read, len - read);
            if (n < 0) throw new IOException("Stream ended early");
            read += n;
        }
        return buf;
    }
}
