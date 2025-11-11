package shared;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple global metrics registry for protocol counters.
 * Thread-safe via AtomicLongs; intentionally lightweight with no external deps.
 */
public final class MetricsRegistry {
    // TCP
    public static final AtomicLong tcpConnections = new AtomicLong(0);
    public static final AtomicLong tcpActiveConnections = new AtomicLong(0);
    public static final AtomicLong tcpRequests = new AtomicLong(0);
    public static final AtomicLong tcpBytesIn = new AtomicLong(0);
    public static final AtomicLong tcpBytesOut = new AtomicLong(0);

    // HTTP (Gateway)
    public static final AtomicLong httpConnections = new AtomicLong(0);
    public static final AtomicLong httpActiveConnections = new AtomicLong(0);
    public static final AtomicLong httpRequests = new AtomicLong(0);
    public static final AtomicLong httpBytesIn = new AtomicLong(0);
    public static final AtomicLong httpBytesOut = new AtomicLong(0);

    // UDP
    public static final AtomicLong udpPacketsIn = new AtomicLong(0);
    public static final AtomicLong udpPacketsOut = new AtomicLong(0);
    public static final AtomicLong udpBytesIn = new AtomicLong(0);
    public static final AtomicLong udpBytesOut = new AtomicLong(0);

    private MetricsRegistry() {}

    public static Map<String, Object> snapshot() {
        Map<String, Object> m = new HashMap<>();

        Map<String, Object> tcp = new HashMap<>();
        tcp.put("connections", tcpConnections.get());
        tcp.put("active", tcpActiveConnections.get());
        tcp.put("requests", tcpRequests.get());
        tcp.put("bytesIn", tcpBytesIn.get());
        tcp.put("bytesOut", tcpBytesOut.get());
        m.put("tcp", tcp);

        Map<String, Object> http = new HashMap<>();
        http.put("connections", httpConnections.get());
        http.put("active", httpActiveConnections.get());
        http.put("requests", httpRequests.get());
        http.put("bytesIn", httpBytesIn.get());
        http.put("bytesOut", httpBytesOut.get());
        m.put("http", http);

        Map<String, Object> udp = new HashMap<>();
        udp.put("packetsIn", udpPacketsIn.get());
        udp.put("packetsOut", udpPacketsOut.get());
        udp.put("bytesIn", udpBytesIn.get());
        udp.put("bytesOut", udpBytesOut.get());
        m.put("udp", udp);

        m.put("timestamp", System.currentTimeMillis());
        return m;
    }
}
