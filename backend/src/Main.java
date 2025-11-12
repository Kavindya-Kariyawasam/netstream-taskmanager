public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO] Starting NetStream TaskManager...");
        System.out.println("=" + "=".repeat(50));

        // 1) Start UDP Notification Server
        udp.UDPNotificationServer udpServer = new udp.UDPNotificationServer();
        Thread udpThread = new Thread(() -> udpServer.start());
        udpThread.start();

        // 2) Start TCP Task Server
        tcp.TCPTaskServer tcpServer = new tcp.TCPTaskServer(8080);
        Thread tcpThread = new Thread(() -> tcpServer.start());
        tcpThread.start();

        // 3) Start HTTP Gateway
        gateway.HttpGateway gateway = new gateway.HttpGateway(3000, "localhost", 8080);
        Thread gatewayThread = new Thread(() -> gateway.start());
        gatewayThread.start();

    // Start URL Integration Service (Member 3)
        url.URLIntegrationService urlService = new url.URLIntegrationService(8082);
        Thread urlThread = new Thread(() -> urlService.start());
        urlThread.start();

        // Start NIO File Server (for multipart uploads/downloads)
        nio.NIOFileServer nioServer = new nio.NIOFileServer(8081);
        Thread nioThread = new Thread(() -> {
            try {
                nioServer.start();
            } catch (Exception e) {
                System.err.println("[NIO] Server failed to start: " + e.getMessage());
            }
        });
        nioThread.start();

    // Start Monitoring Service (port 4000)
    monitor.MonitoringService monitor = new monitor.MonitoringService(4000);
    Thread monitorThread = new Thread(() -> monitor.start());
    monitorThread.start();

        System.out.println("=" + "=".repeat(50));
        System.out.println("[INFO] All servers ready!");
        System.out.println("[INFO] UDP Notification Server: localhost:9090");
        System.out.println("[INFO] TCP Server: localhost:8080 (pure socket)");
        System.out.println("[INFO] HTTP Gateway: localhost:3000 (for browser)");
        System.out.println("[INFO] URL Service: localhost:8082 (external API integration)");
        System.out.println("[INFO] NIO File Server: localhost:8081 (file uploads/downloads)");
        System.out.println("[INFO] Monitoring Service: localhost:4000 (network diagnostics)");
        System.out.println("Press Ctrl+C to stop");

        // Graceful shutdown: stop services and wait briefly for threads to exit.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutting down...");

            // Request servers to stop
            try { udpServer.stop(); } catch (Throwable t) { /* ignore */ }
            try { tcpServer.stop(); } catch (Throwable t) { /* ignore */ }
            try { gateway.stop(); } catch (Throwable t) { /* ignore */ }
            try { urlService.stop(); } catch (Throwable t) { /* ignore */ }
            try { nioServer.stop(); } catch (Throwable t) { /* ignore */ }
            try { monitor.stop(); } catch (Throwable t) { /* ignore */ }

            // Wait for threads to finish (short timeout each). If they don't exit,
            // interrupt them so JVM can terminate cleanly after the hook completes.
            Thread[] threads = new Thread[] { udpThread, tcpThread, gatewayThread, urlThread, nioThread, monitorThread };
            for (Thread t : threads) {
                if (t == null) continue;
                try {
                    t.join(2000); // wait up to 2s
                } catch (InterruptedException ie) {
                    // Restore interrupt status
                    Thread.currentThread().interrupt();
                }
                if (t.isAlive()) {
                    System.out.println("[WARN] Thread did not stop in time: " + t.getName() + "; interrupting");
                    try { t.interrupt(); } catch (Throwable x) { /* ignore */ }
                }
            }

            System.out.println("[INFO] Shutdown hook completed.");
        }));
    }
}
