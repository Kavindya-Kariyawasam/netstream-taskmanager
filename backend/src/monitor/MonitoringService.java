package monitor;

import shared.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight MonitoringService
 * - Probes other local services for reachability and latency
 * - Exposes simple JSON metrics at GET /metrics on the configured port
 */
public class MonitoringService {
    private final int port;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public MonitoringService(int port) {
        this.port = port;
    }

    public void start() {
        running = true;
        // Start background probe thread
        Thread probeThread = new Thread(() -> {
            while (running) {
                try {
                    // Sleep between probes
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        probeThread.setDaemon(true);
        probeThread.start();

        // Start HTTP endpoint server
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[MONITOR] Service started on port " + port);

            while (running) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                } catch (Exception e) {
                    if (running) System.err.println("[MONITOR] Error accepting client: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[MONITOR] Failed to start: " + e.getMessage());
        }
    }

    private void handleClient(Socket client) {
        try {
            client.setSoTimeout(3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

            String requestLine = in.readLine();
            if (requestLine == null) return;

            // We only handle GET /metrics
            if (requestLine.startsWith("GET /metrics")) {
                Map<String, Object> metrics = collectMetrics();
                String json = JsonUtils.createSuccessResponse(metrics);

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Access-Control-Allow-Origin: *");
                out.println("Content-Length: " + json.getBytes().length);
                out.println();
                out.println(json);
                out.flush();
            } else {
                // Simple 404
                String body = "{\"status\":\"error\",\"message\":\"Not found\"}";
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: application/json");
                out.println("Content-Length: " + body.getBytes().length);
                out.println();
                out.println(body);
                out.flush();
            }

        } catch (Exception e) {
            System.err.println("[MONITOR] Client handling error: " + e.getMessage());
        }
    }

    /**
     * Collect basic metrics by probing the local services.
     * This is intentionally lightweight: reachability + latency + simple protocol note.
     */
    private Map<String, Object> collectMetrics() {
        Map<String, Object> m = new HashMap<>();

        // Gateway (HTTP)
        m.put("gateway", probeHttp("http://localhost:3000/notifications"));

        // TCP Server (pure socket) - measure connect + simple GET_TASKS action
        m.put("tcp", probeTcpTaskServer("localhost", 8080));

        // URL Integration Service (TCP JSON) - use PING action
        m.put("urlService", probeTcpUrlService("localhost", 8082));

        // NIO File Server (HTTP-ish)
        m.put("nio", probeHttp("http://localhost:8081/"));

        // UDP server - attempt to send a small UDP heartbeat and consider it reachable if send succeeds
        m.put("udp", probeUdp("localhost", 9090));

        m.put("timestamp", System.currentTimeMillis());

        return m;
    }

    private Map<String, Object> probeHttp(String urlStr) {
        Map<String, Object> r = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", true);
            r.put("statusCode", code);
            r.put("latencyMs", elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", false);
            r.put("error", e.getMessage());
            r.put("latencyMs", elapsed);
        }
        return r;
    }

    private Map<String, Object> probeTcpTaskServer(String host, int port) {
        Map<String, Object> r = new HashMap<>();
        long start = System.currentTimeMillis();
        try (Socket s = new Socket(host, port)) {
            s.setSoTimeout(3000);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // Send a light-weight GET_TASKS request
            String req = "{\"action\":\"GET_TASKS\"}";
            out.println(req);
            out.println(); // terminate

            String response = in.readLine();
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", true);
            r.put("latencyMs", elapsed);
            r.put("responsePreview", response != null ? (response.length() > 120 ? response.substring(0, 120) + "..." : response) : null);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", false);
            r.put("error", e.getMessage());
            r.put("latencyMs", elapsed);
        }
        return r;
    }

    private Map<String, Object> probeTcpUrlService(String host, int port) {
        Map<String, Object> r = new HashMap<>();
        long start = System.currentTimeMillis();
        try (Socket s = new Socket(host, port)) {
            s.setSoTimeout(3000);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // PING action the URL service understands
            String req = "{\"action\":\"PING\"}";
            out.println(req);
            out.println();

            String response = in.readLine();
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", true);
            r.put("latencyMs", elapsed);
            r.put("responsePreview", response != null ? (response.length() > 120 ? response.substring(0, 120) + "..." : response) : null);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", false);
            r.put("error", e.getMessage());
            r.put("latencyMs", elapsed);
        }
        return r;
    }

    private Map<String, Object> probeUdp(String host, int port) {
        Map<String, Object> r = new HashMap<>();
        long start = System.currentTimeMillis();
        try (DatagramSocket ds = new DatagramSocket()) {
            byte[] payload = "HEARTBEAT:monitor".getBytes();
            InetAddress addr = InetAddress.getByName(host);
            DatagramPacket p = new DatagramPacket(payload, payload.length, addr, port);
            ds.send(p);
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", true);
            r.put("latencyMs", elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            r.put("ok", false);
            r.put("error", e.getMessage());
            r.put("latencyMs", elapsed);
        }
        return r;
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        MonitoringService s = new MonitoringService(4000);
        s.start();
    }
}
