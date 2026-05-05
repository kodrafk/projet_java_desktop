package tn.esprit.projet.utils;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Tiny embedded HTTP server that serves recaptcha.html on localhost:8888.
 * Required because Google reCAPTCHA refuses file:// URLs.
 * The page is served at http://localhost:8888/recaptcha
 */
public class RecaptchaServer {

    private static final int PORT = 8888;
    private static HttpServer server;
    private static boolean started = false;

    public static synchronized void start() {
        if (started) return;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

            server.createContext("/recaptcha", exchange -> {
                String siteKey = AppConfig.recaptchaSiteKey();
                String html = buildHtml(siteKey);
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });

            server.setExecutor(null);
            server.start();
            started = true;
            System.out.println("[reCAPTCHA-Server] ✅ Started on http://localhost:" + PORT + "/recaptcha");

        } catch (Exception e) {
            System.err.println("[reCAPTCHA-Server] ❌ Failed to start: " + e.getMessage());
        }
    }

    public static String getUrl() {
        return "http://localhost:" + PORT + "/recaptcha";
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            started = false;
        }
    }

    private static String buildHtml(String siteKey) {
        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<script src='https://www.google.com/recaptcha/api.js' async defer></script>"
            + "<style>"
            + "* { margin:0; padding:0; box-sizing:border-box; }"
            + "body { display:flex; justify-content:center; align-items:center;"
            + "       height:100vh; background:transparent; overflow:hidden; }"
            + ".wrap { transform:scale(0.88); transform-origin:center; }"
            + "</style></head><body>"
            + "<div class='wrap'>"
            + "<div class='g-recaptcha'"
            + "     data-sitekey='" + siteKey + "'"
            + "     data-callback='onSuccess'"
            + "     data-expired-callback='onExpired'>"
            + "</div></div>"
            + "<script>"
            + "window.recaptchaToken=null;"
            + "function onSuccess(t){window.recaptchaToken=t;console.log('reCAPTCHA OK');}"
            + "function onExpired(){window.recaptchaToken=null;}"
            + "function getToken(){return window.recaptchaToken;}"
            + "function reset(){if(typeof grecaptcha!='undefined'){grecaptcha.reset();window.recaptchaToken=null;}}"
            + "</script></body></html>";
    }
}
