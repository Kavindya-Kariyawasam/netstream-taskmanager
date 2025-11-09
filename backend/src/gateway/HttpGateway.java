package gateway;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import shared.DataStore;
import shared.JsonUtils;
import shared.NotificationBroadcaster;

public class HttpGateway {
    private final int httpPort;
    private final String tcpHost;
    private final int tcpPort;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public HttpGateway(int httpPort, String tcpHost, int tcpPort) {
        this.httpPort = httpPort;
        this.tcpHost = tcpHost;
        this.tcpPort = tcpPort;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(httpPort);
            running = true;
            System.out.println("[INFO] HTTP Gateway started on port " + httpPort);
            System.out.println("[INFO] Forwarding to TCP server at " + tcpHost + ":" + tcpPort);

            while (running) {
                try {
                    Socket browserClient = serverSocket.accept();
                    new Thread(() -> handleBrowserRequest(browserClient)).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Gateway error: " + e.getMessage());
        }
    }

    private void handleBrowserRequest(Socket browserClient) {
        try (
            BufferedReader browserIn = new BufferedReader(
                new InputStreamReader(browserClient.getInputStream()));
            PrintWriter browserOut = new PrintWriter(browserClient.getOutputStream(), true)
        ) {
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

            // Read and skip HTTP headers
            String line;
            int contentLength = 0;
            while ((line = browserIn.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }

            // Read JSON body from browser
            String jsonBody = "";
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                browserIn.read(buffer, 0, contentLength);
                jsonBody = new String(buffer);
            }

            System.out.println("[DEBUG] JSON Body: " + jsonBody);

            // Forward to TCP server
            String tcpResponse = forwardToTcpServer(jsonBody);

            // Send HTTP response back to browser
            sendHttpResponse(browserOut, tcpResponse);

        } catch (IOException e) {
            System.err.println("Error handling browser request: " + e.getMessage());
        } finally {
            try {
                browserClient.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private String forwardToTcpServer(String jsonRequest) {
        try (
            Socket tcpSocket = new Socket(tcpHost, tcpPort);
            PrintWriter tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
            BufferedReader tcpIn = new BufferedReader(
                new InputStreamReader(tcpSocket.getInputStream()))
        ) {
            // Send JSON to TCP server
            tcpOut.println(jsonRequest);
            System.out.println("[DEBUG] Forwarded to TCP: " + jsonRequest);

            // Read JSON response from TCP server
            String response = tcpIn.readLine();
            System.out.println("[DEBUG] Received from TCP: " + response);

            return response;

        } catch (IOException e) {
            System.err.println("Error communicating with TCP server: " + e.getMessage());
            return "{\"status\":\"error\",\"message\":\"Cannot connect to TCP server\"}";
        }
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
        } catch (IOException e) {
            System.err.println("Error stopping gateway: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        HttpGateway gateway = new HttpGateway(3000, "localhost", 8080);
        gateway.start();
    }
}