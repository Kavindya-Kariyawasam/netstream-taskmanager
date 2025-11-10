package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import shared.NotificationBroadcaster;

public class UDPNotificationServer {

    private static final int SERVER_PORT = 9090;
    private boolean running = true;

    // userId -> client address + port
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public void start() {
    try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {

    System.out.println("[UDP] Notification Server started on port " + SERVER_PORT);

        byte[] buffer = new byte[1024];

        // Cleanup inactive clients every 30 seconds
        new java.util.Timer(true).scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                clients.entrySet().removeIf(entry -> {
                    boolean expired = now - entry.getValue().lastSeen > 60000; // 60 seconds
                    if (expired) {
                            System.out.println("Removing inactive user: " + entry.getKey());
                        }
                    return expired;
                });
            }
        }, 0, 30000);


        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            // REGISTER:userId:clientPort
            if (msg.startsWith("REGISTER:")) {
                String[] parts = msg.split(":");
                String userId = parts[1];
                int userPort = Integer.parseInt(parts[2]);
                clients.put(userId, new ClientInfo(address, userPort));
                System.out.println("[UDP] Registered User: " + userId + " at " + address + ":" + userPort);
                continue;
            }

            // HEARTBEAT:userId
            if (msg.startsWith("HEARTBEAT:")) {
                String userId = msg.split(":")[1];
                ClientInfo client = clients.get(userId);
                if (client != null) {
                    client.lastSeen = System.currentTimeMillis();
                    System.out.println("[UDP] Heartbeat received from " + userId);
                }
                continue;
            }

            // ACK:userId (client confirming notification received)
            if (msg.startsWith("ACK:")) {
                String userId = msg.split(":")[1];
                System.out.println("[UDP] ACK received from " + userId);
                continue;
            }
            System.out.println("[UDP] Unknown packet: " + msg);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public void stop() {
        running = false;
        System.out.println("[UDP] Server Stopped");
    }

    public static void broadcast(String message) {
    try (DatagramSocket socket = new DatagramSocket()) {

        // 1. Send to UDP clients (native UDP protocol)
            for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            ClientInfo client = entry.getValue();

            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, client.address, client.port);

            socket.send(packet);
            System.out.println("[UDP] broadcast sent to " + entry.getKey());
        }
        
        // 2. Bridge to HTTP clients (SSE) via NotificationBroadcaster
        // This bridges UDP notifications to browser-compatible format
        try {
            NotificationBroadcaster.enqueue(message);
            System.out.println("[UDP] Forwarded to HTTP clients: " + message);
        } catch (Exception e) {
            System.err.println("Failed to forward to HTTP clients: " + e.getMessage());
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private static class ClientInfo {
    InetAddress address;
    int port;
    long lastSeen;

        ClientInfo(InetAddress a, int p) {
            address = a;
            port = p;
            lastSeen = System.currentTimeMillis();
        }
    }

}
