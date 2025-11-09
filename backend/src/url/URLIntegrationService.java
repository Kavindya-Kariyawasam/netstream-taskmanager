package url;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import threading.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import javax.net.ssl.SSLHandshakeException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class URLIntegrationService {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final Gson gson = new Gson();
    private static final String QUOTE_API = "https://api.quotable.io/random";
    private String cachedQuote = null;
    private String cachedAuthor = null;
    private long lastQuoteFetch = 0;
    private static final long CACHE_DURATION = 300000;

    public URLIntegrationService(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("[INFO] URL Service started on port " + port);
            System.out.println("[INFO] Ready to handle external API requests...");

            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                } catch (IOException e) {
                    if (running) {
                        ExceptionHandler.handle(e, "URL Service - accepting connection");
                    }
                }
            }

        } catch (IOException e) {
            ExceptionHandler.handle(e, "URL Service - server startup");
        } finally {
            stop();
        }
    }

    private void handleClient(Socket client) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("[REQUEST] " + requestLine);

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendErrorResponse(out, 400, "Bad request");
                return;
            }

            String method = parts[0];
            String path = parts[1];

            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                // currently we don't need specific headers here, but consume them safely
            }

            if ("GET".equals(method)) {
                if (path.startsWith("/api/quote")) {
                    handleQuoteRequest(out);
                } else if (path.startsWith("/api/avatar/")) {
                    String email = path.substring("/api/avatar/".length());
                    handleAvatarRequest(out, email);
                } else {
                    sendErrorResponse(out, 404, "Endpoint not found");
                }
            } else {
                sendErrorResponse(out, 405, "Method not allowed");
            }

        } catch (Exception e) {
            ExceptionHandler.handle(e, "URL Service - handling client");
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
    }

    private void handleQuoteRequest(PrintWriter out) {
        try {
            long now = System.currentTimeMillis();
            
            if (cachedQuote == null || (now - lastQuoteFetch) > CACHE_DURATION) {
                System.out.println("[INFO] Fetching fresh quote from API...");
                
                URL url = new URL(QUOTE_API);
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "NetStream-URLService/1.0");

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                            cachedQuote = jsonResponse.get("content").getAsString();
                            cachedAuthor = jsonResponse.get("author").getAsString();
                            lastQuoteFetch = now;
                            System.out.println("[SUCCESS] Quote fetched: " + cachedQuote);
                        }
                    } else {
                        throw new IOException("API returned code: " + responseCode);
                    }
                } catch (SSLHandshakeException ssle) {
                    // Common in development if system CA store doesn't trust the remote cert.
                    System.out.println("[WARN] SSL handshake failed while fetching quote, using fallback quote: " + ssle.getMessage());
                    // proceed to fallback below
                }
            } else {
                System.out.println("[CACHE] Using cached quote");
            }

            Map<String, String> responseData = new HashMap<>();
            responseData.put("quote", cachedQuote);
            responseData.put("author", cachedAuthor);

            sendJsonResponse(out, 200, responseData);

        } catch (Exception e) {
            ExceptionHandler.handle(e, "URL Service - quote API");
            
            Map<String, String> fallbackQuote = new HashMap<>();
            fallbackQuote.put("quote", "The only way to do great work is to love what you do.");
            fallbackQuote.put("author", "Steve Jobs");
            sendJsonResponse(out, 200, fallbackQuote);
        }
    }

    private void handleAvatarRequest(PrintWriter out, String email) {
        try {
            String trimmedEmail = email.trim().toLowerCase();
            String hash = md5(trimmedEmail);
            String gravatarUrl = "https://www.gravatar.com/avatar/" + hash + "?d=identicon&s=200";

            Map<String, String> responseData = new HashMap<>();
            responseData.put("avatarUrl", gravatarUrl);
            responseData.put("email", trimmedEmail);

            System.out.println("[SUCCESS] Generated avatar URL for: " + trimmedEmail);
            sendJsonResponse(out, 200, responseData);

        } catch (Exception e) {
            ExceptionHandler.handle(e, "URL Service - avatar generation");
            sendErrorResponse(out, 500, "Failed to generate avatar");
        }
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : messageDigest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void sendJsonResponse(PrintWriter out, int statusCode, Map<String, String> data) {
        String body = gson.toJson(data);
        out.println("HTTP/1.1 " + statusCode + " OK");
        out.println("Content-Type: application/json");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
        out.flush();
    }

    private void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        sendJsonResponse(out, statusCode, error);
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[INFO] URL Service stopped");
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e, "URL Service - shutdown");
        }
    }

    public static void main(String[] args) {
        URLIntegrationService service = new URLIntegrationService(8082);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[INFO] Shutting down URL service...");
            service.stop();
        }));
        
        service.start();
    }
}