package udp;

import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class UDPClientListener {

    public static void main(String[] args) {
        String userId = "user1"; // Each client must have a unique userId
        int clientPort = 9091;    // Default client listening port (different from server's 9090)
        String serverHost = "localhost";
        int serverPort = 9090;

        // Optional args: [serverHost] [serverPort] [userId] [clientPort]
        if (args != null) {
            if (args.length >= 1 && args[0] != null && !args[0].isEmpty()) {
                serverHost = args[0];
            }
            if (args.length >= 2) {
                try {
                    serverPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid server port provided, using default 9090");
                    serverPort = 9090;
                }
            }
            if (args.length >= 3 && args[2] != null && !args[2].isEmpty()) {
                userId = args[2];
            }
            if (args.length >= 4) {
                try {
                    clientPort = Integer.parseInt(args[3]);
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid client port provided, letting OS pick a free port.");
                    clientPort = 0;
                }
            }
        }

        // Prevent accidental bind to the same port as the server
        if (clientPort != 0 && clientPort == serverPort) {
            System.err.println("Warning: requested client port " + clientPort + " equals server port " + serverPort + ". Switching to ephemeral port to avoid conflict.");
            clientPort = 0; // let OS pick a free port
        }

        // Make final copies for use inside inner classes (TimerTask requires captured vars to be final/effectively final)
        final String finalUserId = userId;
        final int finalServerPort = serverPort;

        try {
            InetAddress serverIP = InetAddress.getByName(serverHost);
            DatagramSocket socket = new DatagramSocket(clientPort); // OS picks free port
            clientPort = socket.getLocalPort(); // Get the assigned port

            System.out.println("[UDP] Client port: " + clientPort);
            System.out.println("[UDP] Registered as user: " + userId);

            // 1️⃣ Register with server
            String registerMsg = "REGISTER:" + userId + ":" + clientPort;
            DatagramPacket registerPacket = new DatagramPacket(
                    registerMsg.getBytes(), 
                    registerMsg.length(), 
                    serverIP, 
                    serverPort
            );
            try {
                socket.send(registerPacket);
            } catch (ConnectException ce) {
                System.err.println("Failed to send registration to server at " + serverHost + ":" + serverPort + " - " + ce.getMessage());
                socket.close();
                return;
            }
            System.out.println("[UDP] Registration sent to server");

            // 2️⃣ Start heartbeat every 5 seconds
            Timer heartbeatTimer = new Timer(true);
            heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String heartbeatMsg = "HEARTBEAT:" + finalUserId;
                        DatagramPacket heartbeatPacket = new DatagramPacket(
                                heartbeatMsg.getBytes(),
                                heartbeatMsg.length(),
                                serverIP,
                                finalServerPort
                        );
                        socket.send(heartbeatPacket);
                        System.out.println("[UDP] Heartbeat sent");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 5000);

            // 3️⃣ Listen for incoming notifications
            System.out.println("📡 Listening for notifications...");
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);

                String message = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
                // Print both emoji (when supported) and a plain ASCII line so logs are visible in all terminals
                System.out.println("🔔 Notification received: " + message);
                System.out.println("[UDP] Notification received: " + message);

                // Send ACK to server
                String ackMsg = "ACK:" + userId;
                DatagramPacket ackPacket = new DatagramPacket(
                        ackMsg.getBytes(),
                        ackMsg.length(),
                        serverIP,
                        serverPort
                );
                socket.send(ackPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
