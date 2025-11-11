package url;

import shared.JsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * URL/URI External Integration Service
 * Handles external API integration, URL validation, and file uploads/downloads
 * Member 3 - URLs/URIs & URLConnection
 */
public class URLIntegrationService {
    private int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private static final String UPLOAD_DIR = "uploads/";

    // API Endpoints
    // Using ZenQuotes as backup - more reliable SSL certificate
    private static final String QUOTES_API = "https://zenquotes.io/api/random";
    private static final String QUOTES_API_FALLBACK = "https://api.quotable.io/random";
    private static final String GRAVATAR_BASE = "https://www.gravatar.com/avatar/";
    private static final int TIMEOUT = 10000; // 10 seconds

    public URLIntegrationService(int port) {
        this.port = port;
        createUploadDirectory();
    }

    /**
     * Start the URL Integration Service
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(0); // No timeout for accept()
            running = true;

            System.out.println("[URL Service] Started on port " + port);
            System.out.println("[URL Service] Ready to handle external API requests");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (SocketTimeoutException e) {
                    // Continue loop
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[URL Service] Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[URL Service] Failed to start: " + e.getMessage());
        }
    }

    /**
     * Stop the service
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("[URL Service] Stopped");
        } catch (IOException e) {
            System.err.println("[URL Service] Error stopping: " + e.getMessage());
        }
    }

    /**
     * Handle client requests
     */
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            clientSocket.setSoTimeout(TIMEOUT);

            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line);
            }

            String request = requestBuilder.toString();
            if (request.isEmpty()) {
                out.println(JsonUtils.createErrorResponse("Empty request"));
                return;
            }

            System.out.println("[URL Service] Request: " + request);

            JsonObject jsonRequest = JsonParser.parseString(request).getAsJsonObject();
            String action = jsonRequest.get("action").getAsString();
            String response = processAction(action, jsonRequest);

            out.println(response);
            System.out.println("[URL Service] Response sent");

        } catch (SocketTimeoutException e) {
            System.err.println("[URL Service] Client timeout");
        } catch (Exception e) {
            System.err.println("[URL Service] Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Process different actions
     */
    private String processAction(String action, JsonObject request) {
        try {
            switch (action.toUpperCase()) {
                case "GET_QUOTE":
                    return getMotivationalQuote();

                case "GET_AVATAR":
                    String email = request.getAsJsonObject("data").get("email").getAsString();
                    return getGravatarAvatar(email);

                case "VALIDATE_URL":
                    String url = request.getAsJsonObject("data").get("url").getAsString();
                    return validateURL(url);

                case "DOWNLOAD_FILE":
                    String fileUrl = request.getAsJsonObject("data").get("url").getAsString();
                    String fileName = request.getAsJsonObject("data").has("fileName")
                            ? request.getAsJsonObject("data").get("fileName").getAsString()
                            : null;
                    return downloadFile(fileUrl, fileName);

                case "GET_WEATHER":
                    String city = request.getAsJsonObject("data").get("city").getAsString();
                    return getWeatherInfo(city);

                case "FETCH_API":
                    String apiUrl = request.getAsJsonObject("data").get("url").getAsString();
                    String method = request.getAsJsonObject("data").has("method")
                            ? request.getAsJsonObject("data").get("method").getAsString()
                            : "GET";
                    return fetchFromAPI(apiUrl, method);

                case "PING":
                    // Lightweight health probe used by gateway/frontend
                    Map<String, Object> ok = new HashMap<>();
                    ok.put("status", "ok");
                    ok.put("service", "URLIntegrationService");
                    ok.put("time", System.currentTimeMillis());
                    return JsonUtils.createSuccessResponse(ok);

                default:
                    return JsonUtils.createErrorResponse("Unknown action: " + action);
            }
        } catch (Exception e) {
            return JsonUtils.createErrorResponse("Error processing action: " + e.getMessage());
        }
    }

    /**
     * Fetch motivational quote from API
     * Uses ZenQuotes API which has better SSL certificate support
     */
    private String getMotivationalQuote() {
        try {
            URL url = new URL(QUOTES_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // ZenQuotes returns an array with one object:
                // [{"q":"quote","a":"author","h":"html"}]
                String jsonResponse = response.toString().trim();

                // Check if it's an array (ZenQuotes format)
                if (jsonResponse.startsWith("[")) {
                    jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
                }

                JsonObject quoteJson = JsonParser.parseString(jsonResponse).getAsJsonObject();

                Map<String, String> quoteData = new HashMap<>();

                // Handle both ZenQuotes (q, a) and Quotable (content, author) formats
                if (quoteJson.has("q")) {
                    quoteData.put("quote", quoteJson.get("q").getAsString());
                    quoteData.put("author", quoteJson.get("a").getAsString());
                    quoteData.put("source", "zenquotes.io");
                } else {
                    quoteData.put("quote", quoteJson.get("content").getAsString());
                    quoteData.put("author", quoteJson.get("author").getAsString());
                    quoteData.put("source", "quotable.io");
                }

                return JsonUtils.createSuccessResponse(quoteData);
            } else {
                return JsonUtils.createErrorResponse("Failed to fetch quote. HTTP " + responseCode);
            }
        } catch (MalformedURLException e) {
            return JsonUtils.createErrorResponse("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            return JsonUtils.createErrorResponse("Network error: " + e.getMessage());
        } catch (Exception e) {
            return JsonUtils.createErrorResponse("Error fetching quote: " + e.getMessage());
        }
    }

    /**
     * Generate Gravatar avatar URL from email
     */
    private String getGravatarAvatar(String email) {
        try {
            String hash = md5Hash(email.trim().toLowerCase());
            String avatarUrl = GRAVATAR_BASE + hash + "?s=200&d=identicon";

            Map<String, String> result = new HashMap<>();
            result.put("email", email);
            result.put("avatarUrl", avatarUrl);
            result.put("hash", hash);
            result.put("note", "Default size: 200px, Default image: identicon");

            return JsonUtils.createSuccessResponse(result);
        } catch (Exception e) {
            return JsonUtils.createErrorResponse("Error generating avatar URL: " + e.getMessage());
        }
    }

    /**
     * Validate URL format and accessibility
     */
    private String validateURL(String urlString) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("url", urlString);

        try {
            // Parse URL
            URL url = new URL(urlString);
            validation.put("valid", true);
            validation.put("protocol", url.getProtocol());
            validation.put("host", url.getHost());
            validation.put("port", url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
            validation.put("path", url.getPath());

            // Test connectivity
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            validation.put("accessible", responseCode >= 200 && responseCode < 400);
            validation.put("httpStatus", responseCode);

            return JsonUtils.createSuccessResponse(validation);

        } catch (MalformedURLException e) {
            validation.put("valid", false);
            validation.put("accessible", false);
            validation.put("error", "Malformed URL: " + e.getMessage());
            return JsonUtils.createSuccessResponse(validation);
        } catch (IOException e) {
            validation.put("valid", true);
            validation.put("accessible", false);
            validation.put("error", "Not accessible: " + e.getMessage());
            return JsonUtils.createSuccessResponse(validation);
        }
    }

    /**
     * Download file from URL
     */
    private String downloadFile(String fileUrl, String fileName) {
        try {
            URL url = new URL(fileUrl);

            // Parse filename if not provided
            if (fileName == null || fileName.isEmpty()) {
                String path = url.getPath();
                fileName = path.substring(path.lastIndexOf('/') + 1);
                if (fileName.isEmpty()) {
                    fileName = "downloaded_" + System.currentTimeMillis();
                }
            }

            // Open connection
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            // Get file size
            long fileSize = conn.getContentLengthLong();
            String contentType = conn.getContentType();

            // Download file
            Path outputPath = Paths.get(UPLOAD_DIR, fileName);
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("filePath", outputPath.toString());
            result.put("fileSize", fileSize);
            result.put("contentType", contentType);
            result.put("sourceUrl", fileUrl);

            return JsonUtils.createSuccessResponse(result);

        } catch (MalformedURLException e) {
            return JsonUtils.createErrorResponse("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            return JsonUtils.createErrorResponse("Download failed: " + e.getMessage());
        }
    }

    /**
     * Get weather information (using free weather API)
     * Note: This is a demo implementation using wttr.in
     */
    private String getWeatherInfo(String city) {
        try {
            String apiUrl = "https://wttr.in/" + URLEncoder.encode(city, "UTF-8") + "?format=j1";
            URL url = new URL(apiUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject weatherJson = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject currentCondition = weatherJson.getAsJsonArray("current_condition")
                        .get(0).getAsJsonObject();

                Map<String, String> weatherData = new HashMap<>();
                weatherData.put("city", city);
                weatherData.put("temperature", currentCondition.get("temp_C").getAsString() + "°C");
                weatherData.put("feelsLike", currentCondition.get("FeelsLikeC").getAsString() + "°C");
                weatherData.put("description", currentCondition.getAsJsonArray("weatherDesc")
                        .get(0).getAsJsonObject().get("value").getAsString());
                weatherData.put("humidity", currentCondition.get("humidity").getAsString() + "%");
                weatherData.put("windSpeed", currentCondition.get("windspeedKmph").getAsString() + " km/h");

                return JsonUtils.createSuccessResponse(weatherData);
            } else {
                return JsonUtils.createErrorResponse("Failed to fetch weather. HTTP " + responseCode);
            }
        } catch (Exception e) {
            return JsonUtils.createErrorResponse("Weather API error: " + e.getMessage());
        }
    }

    /**
     * Generic API fetcher
     */
    private String fetchFromAPI(String apiUrl, String method) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method.toUpperCase());
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "NetStream-TaskManager/1.0");

            int responseCode = conn.getResponseCode();

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Map<String, Object> result = new HashMap<>();
            result.put("url", apiUrl);
            result.put("method", method);
            result.put("statusCode", responseCode);
            result.put("response", response.toString());
            result.put("success", responseCode >= 200 && responseCode < 300);

            return JsonUtils.createSuccessResponse(result);

        } catch (MalformedURLException e) {
            return JsonUtils.createErrorResponse("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            return JsonUtils.createErrorResponse("API request failed: " + e.getMessage());
        }
    }

    /**
     * Create upload directory if it doesn't exist
     */
    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("[URL Service] Created upload directory: " + UPLOAD_DIR);
            }
        } catch (IOException e) {
            System.err.println("[URL Service] Failed to create upload directory: " + e.getMessage());
        }
    }

    /**
     * Calculate MD5 hash (for Gravatar)
     */
    private String md5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
