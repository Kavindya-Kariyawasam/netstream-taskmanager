package udp;

import shared.DataStore;
import threading.ExceptionHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UDPNotificationServer {
    private final int port;
    private DatagramSocket socket;
    private volatile boolean running = false;
    private final List<ClientInfo> subscribedClients = new CopyOnWriteArrayList<>();
    private int lastNotificationIndex = 0;

    public UDPNotificationServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            running = true;
            System.out.println("[INFO] UDP Server started on port " + port);
            System.out.println("[INFO] Listening for client subscriptions...");

            Thread broadcastThread = new Thread(this::broadcastNotifications);
            broadcastThread.setDaemon(true);
            broadcastThread.start();

            while (running) {
                try {
                    byte[] buffer = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength()).trim();
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    if ("SUBSCRIBE".equals(message)) {
                        addClient(clientAddress, clientPort);
                        System.out.println("[INFO] Client subscribed: " + clientAddress + ":" + clientPort);
                        sendAck(clientAddress, clientPort);
                    } else if ("UNSUBSCRIBE".equals(message)) {
                        removeClient(clientAddress, clientPort);
                        System.out.println("[INFO] Client unsubscribed: " + clientAddress + ":" + clientPort);
                    }

                } catch (IOException e) {
                    if (running) {
                        ExceptionHandler.handle(e, "UDP Server - receiving subscription");
                    }
                }
            }

        } catch (SocketException e) {
            ExceptionHandler.handle(e, "UDP Server - socket creation");
        } finally {
            stop();
        }
    }

    private void broadcastNotifications() {
        System.out.println("[INFO] Notification broadcaster started");
        
        while (running) {
            try {
                List<String> notifications = DataStore.getNotifications();
                
                if (notifications.size() > lastNotificationIndex) {
                    for (int i = lastNotificationIndex; i < notifications.size(); i++) {
                        String notification = notifications.get(i);
                        broadcastToAll(notification);
                        System.out.println("[BROADCAST] " + notification);
                    }
                    lastNotificationIndex = notifications.size();
                }

                Thread.sleep(100);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                ExceptionHandler.handle(e, "UDP Server - broadcasting");
            }
        }
    }

    private void broadcastToAll(String message) {
        byte[] data = message.getBytes();
        List<ClientInfo> clientsToRemove = new ArrayList<>();

        for (ClientInfo client : subscribedClients) {
            try {
                DatagramPacket packet = new DatagramPacket(
                    data, 
                    data.length, 
                    client.address, 
                    client.port
                );
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("[WARN] Failed to send to client: " + client.address);
                clientsToRemove.add(client);
            }
        }

        subscribedClients.removeAll(clientsToRemove);
    }

    private void sendAck(InetAddress address, int port) {
        try {
            String ack = "SUBSCRIBED";
            byte[] data = ack.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            ExceptionHandler.handle(e, "UDP Server - sending ACK");
        }
    }

    private void addClient(InetAddress address, int port) {
        ClientInfo newClient = new ClientInfo(address, port);
        subscribedClients.removeIf(c -> c.address.equals(address) && c.port == port);
        subscribedClients.add(newClient);
    }

    private void removeClient(InetAddress address, int port) {
        subscribedClients.removeIf(c -> c.address.equals(address) && c.port == port);
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("[INFO] UDP Server stopped");
        }
    }

    private static class ClientInfo {
        final InetAddress address;
        final int port;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }
    }

    public static void main(String[] args) {
        UDPNotificationServer server = new UDPNotificationServer(9090);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[INFO] Shutting down UDP server...");
            server.stop();
        }));
        
        server.start();
    }
}