package shared;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    // Thread-safe storage for tasks
    private static final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    
    // Thread-safe storage for notifications (for UDP server later)
    private static final List<String> notifications = Collections.synchronizedList(new ArrayList<>());

    // Task operations
    public static void addTask(Task task) {
        tasks.put(task.getId(), task);
        addNotification("TASK_CREATED|" + task.getId() + "|Task created: " + task.getTitle() + "|" + System.currentTimeMillis());
        System.out.println("[DataStore] Task added: " + task.getId());
    }

    public static Task getTask(String id) {
        return tasks.get(id);
    }

    public static List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public static void updateTask(String id, Task updatedTask) {
        if (tasks.containsKey(id)) {
            tasks.put(id, updatedTask);
            addNotification("TASK_UPDATED|" + id + "|Task updated: " + updatedTask.getTitle() + "|" + System.currentTimeMillis());
            System.out.println("[DataStore] Task updated: " + id);
        }
    }

    public static boolean deleteTask(String id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            addNotification("TASK_DELETED|" + id + "|Task deleted|" + System.currentTimeMillis());
            System.out.println("[DataStore] Task deleted: " + id);
            return true;
        }
        return false;
    }

    public static int getTaskCount() {
        return tasks.size();
    }

    // Notification operations (for UDP server)
    public static void addNotification(String notification) {
        notifications.add(notification);
        // Keep only last 100 notifications
        if (notifications.size() > 100) {
            notifications.remove(0);
        }
        // Note: Broadcasting is handled by UDPNotificationServer.broadcast()
        // which forwards to both UDP clients AND HTTP clients via NotificationBroadcaster
    }

    public static List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public static void clearNotifications() {
        notifications.clear();
    }

    // Utility methods
    public static void clear() {
        tasks.clear();
        notifications.clear();
        System.out.println("[DataStore] All data cleared");
    }

    public static boolean taskExists(String id) {
        return tasks.containsKey(id);
    }
}