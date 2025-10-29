package gateway;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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