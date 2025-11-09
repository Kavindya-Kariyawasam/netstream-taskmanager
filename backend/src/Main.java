public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO] Starting NetStream TaskManager...");
        System.out.println("=" + "=".repeat(50));

        // Start TCP server (PURE SOCKET)
        tcp.TCPTaskServer tcpServer = new tcp.TCPTaskServer(8080);
        Thread tcpThread = new Thread(() -> tcpServer.start());
        tcpThread.start();

        // Start HTTP Gateway (for browser access)
        gateway.HttpGateway gateway = new gateway.HttpGateway(3000, "localhost", 8080);
        Thread gatewayThread = new Thread(() -> gateway.start());
        gatewayThread.start();

        // Start URL Integration Service (Member 3)
        url.URLIntegrationService urlService = new url.URLIntegrationService(8082);
        Thread urlThread = new Thread(() -> urlService.start());
        urlThread.start();

        System.out.println("=" + "=".repeat(50));
        System.out.println("[INFO] All servers ready!");
        System.out.println("[INFO] TCP Server: localhost:8080 (pure socket)");
        System.out.println("[INFO] HTTP Gateway: localhost:3000 (for browser)");
        System.out.println("[INFO] URL Service: localhost:8082 (external API integration)");
        System.out.println("Press Ctrl+C to stop");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutting down...");
            tcpServer.stop();
            gateway.stop();
            urlService.stop();
        }));
    }
}