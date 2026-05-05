package tn.esprit.projet.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server that serves face-api.js and its model files
 * on localhost so that JavaFX WebView can:
 *   1. Load face-api.js (same-origin)
 *   2. Use getUserMedia (camera) — allowed on localhost HTTP
 *   3. Load model weight files without CORS issues
 */
public class FaceApiServer {

    private static HttpServer server;
    private static int port = -1;

    private static final Map<String, String> MIME = Map.of(
        "js",   "application/javascript",
        "json", "application/json",
        "html", "text/html; charset=utf-8",
        "bin",  "application/octet-stream",
        ""   ,  "application/octet-stream"
    );

    public static synchronized int start() {
        if (server != null) return port;
        try {
            port = findFreePort();
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            server.createContext("/", FaceApiServer::handle);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("[FaceApiServer] Started on http://127.0.0.1:" + port);
            return port;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start FaceApiServer: " + e.getMessage(), e);
        }
    }

    public static synchronized void stop() {
        if (server != null) { server.stop(0); server = null; port = -1; }
    }

    public static int getPort() { return port; }

    private static void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/html/face_id.html";

        // Map URL path to classpath resource
        // /html/... → /html/...
        // /faceapi/... → /faceapi/...
        InputStream is = FaceApiServer.class.getResourceAsStream(path);

        if (is == null) {
            byte[] body = ("Not found: " + path).getBytes();
            ex.sendResponseHeaders(404, body.length);
            ex.getResponseBody().write(body);
            ex.getResponseBody().close();
            return;
        }

        String ext = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : "";
        String mime = MIME.getOrDefault(ext, "application/octet-stream");

        byte[] data = is.readAllBytes();
        is.close();

        ex.getResponseHeaders().set("Content-Type", mime);
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Cache-Control", "max-age=3600");
        ex.sendResponseHeaders(200, data.length);
        ex.getResponseBody().write(data);
        ex.getResponseBody().close();
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
