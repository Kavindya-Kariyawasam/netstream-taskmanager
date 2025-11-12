package gateway;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import shared.DataStore;
import shared.JsonUtils;
import shared.NotificationBroadcaster;
import threading.ThreadPoolManager;
import threading.ExceptionHandler;
import shared.MetricsRegistry;

public class HttpGateway {
    private final int httpPort;
    private final String tcpHost;
    private final int tcpPort;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private ExecutorService threadPool;

    public HttpGateway(int httpPort, String tcpHost, int tcpPort) {
        this.httpPort = httpPort;
        this.tcpHost = tcpHost;
        this.tcpPort = tcpPort;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(httpPort);
            running = true;
            threadPool = ThreadPoolManager.getThreadPool();
            System.out.println("[INFO] HTTP Gateway started on port " + httpPort);
            System.out.println("[INFO] Forwarding to TCP server at " + tcpHost + ":" + tcpPort);

            while (running) {
                try {
                    Socket browserClient = serverSocket.accept();
                    threadPool.submit(() -> handleBrowserRequest(browserClient));
                    MetricsRegistry.httpConnections.incrementAndGet();
                    MetricsRegistry.httpActiveConnections.incrementAndGet();
                } catch (IOException e) {
                    if (running) {
                        ExceptionHandler.handle(e, "Gateway accepting connection");
                    }
                }
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e, "Gateway startup");
        }
    }

    private void handleBrowserRequest(Socket browserClient) {
        try (
                BufferedReader browserIn = new BufferedReader(
                        new InputStreamReader(browserClient.getInputStream()));
                PrintWriter browserOut = new PrintWriter(browserClient.getOutputStream(), true)) {
        MetricsRegistry.httpRequests.incrementAndGet();
            // Read HTTP request from browser
            String requestLine = browserIn.readLine();
            System.out.println("[INFO] HTTP Request: " + requestLine);

            // Quick route: GET /notifications -> return stored notifications
            if (requestLine != null && requestLine.startsWith("GET /notifications ")) {
                String jsonResponse = JsonUtils.createSuccessResponse(DataStore.getNotifications());
                sendHttpResponse(browserOut, jsonResponse);
                return;
            }

            // SSE endpoint: GET /events -> stream real-time notifications
            if (requestLine != null && requestLine.startsWith("GET /events ")) {
                handleEventStream(browserClient, browserIn, browserOut);
                return;
            }

            // Parse the request path
            String requestPath = "/";
            if (requestLine != null && requestLine.contains(" ")) {
                String[] parts = requestLine.split(" ");
                if (parts.length >= 2) {
                    requestPath = parts[1];
                }
            }

            // Read and skip HTTP headers
            String line;
            int contentLength = 0;
            while ((line = browserIn.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }

            // Handle OPTIONS request (CORS preflight)
            if (requestLine != null && requestLine.startsWith("OPTIONS")) {
                sendCorsResponse(browserOut);
                return;
            }

            // Read JSON body from browser
            String jsonBody = "";
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                browserIn.read(buffer, 0, contentLength);
                jsonBody = new String(buffer);
                MetricsRegistry.httpBytesIn.addAndGet(contentLength);
            }

            System.out.println("[DEBUG] Request Path: " + requestPath);
            System.out.println("[DEBUG] JSON Body: " + jsonBody);

            // Route to appropriate service based on path
            String response;
            if (requestPath.startsWith("/url-service")) {
                // Forward to URL Service (port 8082)
                response = forwardToService("localhost", 8082, jsonBody);
            } else {
                // Forward to TCP server (port 8080)
                response = forwardToService(tcpHost, tcpPort, jsonBody);
            }

            // Send HTTP response back to browser
            sendHttpResponse(browserOut, response);
            if (response != null) {
                MetricsRegistry.httpBytesOut.addAndGet(response.length());
            }

        } catch (IOException e) {
            ExceptionHandler.handle(e, "Gateway handling browser request");
        } finally {
            try {
                browserClient.close();
                MetricsRegistry.httpActiveConnections.decrementAndGet();
            } catch (IOException e) {
                ExceptionHandler.handle(e, "Gateway closing browser client");
            }
        }
    }

    private String forwardToService(String host, int port, String jsonRequest) {
        try (
                Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {
            // Send JSON to service
            out.println(jsonRequest);
            out.println(); // Empty line to indicate end of request
            System.out.println("[DEBUG] Forwarded to " + host + ":" + port + " -> " + jsonRequest);

            // Read JSON response from service
            String response = in.readLine();
            System.out.println("[DEBUG] Received from " + host + ":" + port + " -> " + response);

            return response != null ? response : "{\"status\":\"error\",\"message\":\"No response from server\"}";

        } catch (IOException e) {
            ExceptionHandler.handle(e, "Gateway forwarding to service " + host + ":" + port);
            return "{\"status\":\"error\",\"message\":\"Cannot connect to service at " + host + ":" + port + "\"}";
        }
    }

    private void sendCorsResponse(PrintWriter out) {
        out.println("HTTP/1.1 204 No Content");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: POST, GET, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
        out.println();
        out.flush();
    }

    private void sendHttpResponse(PrintWriter out, String jsonResponse) {
        // Send HTTP headers
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: POST, GET, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
        out.println("Content-Length: " + jsonResponse.length());
        out.println();
        out.println(jsonResponse);
        out.flush();
    }

    /**
     * Handle Server-Sent Events (SSE) stream for real-time notifications
     * Network concept: Persistent HTTP connection with chunked transfer
     */
    private void handleEventStream(Socket socket, BufferedReader in, PrintWriter out) {
        try {
            // Skip remaining HTTP headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Skip headers
            }

            // Send SSE headers
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/event-stream");
            out.println("Cache-Control: no-cache");
            out.println("Connection: keep-alive");
            out.println("Access-Control-Allow-Origin: *");
            out.println();
            out.flush();

            System.out.println("[SSE] Client connected for event stream");

            // Register client with broadcaster
            NotificationBroadcaster.addHttpClient(out);

            // Keep connection alive - send periodic heartbeat
            while (!socket.isClosed() && socket.isConnected()) {
                try {
                    // Send heartbeat every 30 seconds to keep connection alive
                    Thread.sleep(30000);
                    out.println(": heartbeat");
                    out.println();
                    out.flush();
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("[SSE] Client disconnected: " + e.getMessage());
        } finally {
            NotificationBroadcaster.removeHttpClient(out);
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            ThreadPoolManager.shutdown();
        } catch (IOException e) {
            ExceptionHandler.handle(e, "Gateway stopping");
        }
    }

    public static void main(String[] args) {
        HttpGateway gateway = new HttpGateway(3000, "localhost", 8080);
        gateway.start();
    }
}