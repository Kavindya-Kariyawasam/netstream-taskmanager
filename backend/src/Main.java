public class Main {
    public static void main(String[] args) {
        System.out.println("[INFO] Starting NetStream TaskManager...");
        System.out.println("=" + "=".repeat(50));

        // Currently starting only the TCP server
        
        tcp.TCPTaskServer tcpServer = new tcp.TCPTaskServer(8080);
        
        // Start TCP server in a separate thread
        Thread tcpThread = new Thread(() -> tcpServer.start());
        tcpThread.start();
        
        System.out.println("=" + "=".repeat(50));
        System.out.println("[INFO] All servers ready!");
        System.out.println("Press Ctrl+C to stop");
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutting down...");
            tcpServer.stop();
        }));
    }
}