package shared;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Network programming concept: Producer-Consumer pattern using BlockingQueue
 * Bridges UDP notifications to HTTP long-polling clients
 */
public class NotificationBroadcaster {
    
    // Thread-safe list of connected HTTP clients waiting for notifications
    private static final CopyOnWriteArrayList<PrintWriter> httpClients = new CopyOnWriteArrayList<>();
    
    // Blocking queue for notification distribution (producer-consumer pattern)
    private static final BlockingQueue<String> notificationQueue = new LinkedBlockingQueue<>();
    
    // Start background thread to distribute notifications
    static {
        Thread distributorThread = new Thread(() -> {
            System.out.println("[NotificationBroadcaster] Started notification distributor thread");
            while (true) {
                try {
                    // Block until a notification is available (blocking I/O concept)
                    String notification = notificationQueue.take();
                    
                    // Broadcast to all connected HTTP clients
                    broadcastToHttpClients(notification);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        distributorThread.setDaemon(true);
        distributorThread.start();
    }
    
    /**
     * Add notification to queue (called by DataStore when tasks change)
     */
    public static void enqueue(String notification) {
        try {
            notificationQueue.offer(notification, 100, TimeUnit.MILLISECONDS);
            System.out.println("[NotificationBroadcaster] Queued: " + notification);
        } catch (InterruptedException e) {
            System.err.println("[NotificationBroadcaster] Failed to enqueue: " + e.getMessage());
        }
    }
    
    /**
     * Register an HTTP client for long-polling (blocking until notification arrives)
     * Returns the next available notification or null after timeout
     */
    public static String waitForNotification(long timeoutMs) {
        try {
            return notificationQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * Broadcast notification to all HTTP clients immediately
     */
    private static void broadcastToHttpClients(String notification) {
        int sent = 0;
        for (PrintWriter client : httpClients) {
            try {
                // Send as Server-Sent Events format
                client.println("data: " + notification);
                client.println(); // Empty line to complete the event
                client.flush();
                sent++;
            } catch (Exception e) {
                // Client disconnected, remove it
                httpClients.remove(client);
            }
        }
        if (sent > 0) {
            System.out.println("[NotificationBroadcaster] Sent to " + sent + " HTTP clients");
        }
    }
    
    /**
     * Register HTTP client for SSE streaming
     */
    public static void addHttpClient(PrintWriter client) {
        httpClients.add(client);
        System.out.println("[NotificationBroadcaster] HTTP client registered. Total: " + httpClients.size());
    }
    
    /**
     * Unregister HTTP client
     */
    public static void removeHttpClient(PrintWriter client) {
        httpClients.remove(client);
        System.out.println("[NotificationBroadcaster] HTTP client removed. Total: " + httpClients.size());
    }
    
    /**
     * Get count of connected HTTP clients
     */
    public static int getHttpClientCount() {
        return httpClients.size();
    }
}
