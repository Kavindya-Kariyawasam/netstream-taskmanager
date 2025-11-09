public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO] Starting NetStream TaskManager...");
        System.out.println("=" + "=".repeat(50));

        // Start TCP server (PURE SOCKET)
        tcp.TCPTaskServer tcpServer = new tcp.TCPTaskServer(8080);
        Thread tcpThread = new Thread(() -> tcpServer.start(), "TCP-Server-Thread");
        tcpThread.start();

        // Start HTTP Gateway (for browser access)
        gateway.HttpGateway gateway = new gateway.HttpGateway(3000, "localhost", 8080);
        Thread gatewayThread = new Thread(() -> gateway.start(), "Gateway-Thread");
        gatewayThread.start();

        // Start UDP Notification Server
        udp.UDPNotificationServer udpServer = new udp.UDPNotificationServer(9090);
        Thread udpThread = new Thread(() -> udpServer.start(), "UDP-Server-Thread");
        udpThread.start();

        // Start NIO File Server
        nio.NIOFileServer nioServer = new nio.NIOFileServer(8081, 10);
        Thread nioThread = new Thread(() -> {
            try {
                nioServer.start();
            } catch (Exception e) {
                System.err.println("[ERROR] NIO Server failed: " + e.getMessage());
            }
        }, "NIO-Server-Thread");
        nioThread.start();

        // Start URL Integration Service
        //url.URLIntegrationService urlService = new url.URLIntegrationService(8082);
        //Thread urlThread = new Thread(() -> urlService.start(), "URL-Service-Thread");
        //urlThread.start();

        System.out.println("=" + "=".repeat(50));
        System.out.println("[INFO] All servers ready!");
        System.out.println("[INFO] TCP Server: localhost:8080 (pure socket)");
        System.out.println("[INFO] HTTP Gateway: localhost:3000 (for browser)");
        System.out.println("[INFO] UDP Notification Server: localhost:9090");
        System.out.println("[INFO] NIO File Server: localhost:8081");
        System.out.println("[INFO] URL Integration Service: localhost:8082");
        System.out.println("Press Ctrl+C to stop");
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutting down...");
            tcpServer.stop();
            gateway.stop();
            udpServer.stop();
            nioServer.stop();
            //urlService.stop();
        }));
    }
}