package shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    // Thread-safe storage for tasks
    private static final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    
    // Thread-safe storage for notifications (for UDP server later)
    private static final List<String> notifications = Collections.synchronizedList(new ArrayList<>());

    // Persistence file path
    private static final Path TASKS_FILE = Paths.get("data", "tasks.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Static initializer to load tasks on startup
    static {
        loadTasks();
    }

    // Task operations
    public static void addTask(Task task) {
        tasks.put(task.getId(), task);
        addNotification("TASK_CREATED|" + task.getId() + "|Task created: " + task.getTitle() + "|" + System.currentTimeMillis());
        System.out.println("[DataStore] Task added: " + task.getId());
        saveTasks();
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
            saveTasks();
        }
    }

    public static boolean deleteTask(String id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            addNotification("TASK_DELETED|" + id + "|Task deleted|" + System.currentTimeMillis());
            System.out.println("[DataStore] Task deleted: " + id);
            saveTasks();
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

    // Persistence methods
    private static void saveTasks() {
        try {
            // Create data directory if it doesn't exist
            Files.createDirectories(TASKS_FILE.getParent());
            
            // Save tasks to JSON file
            List<Task> taskList = new ArrayList<>(tasks.values());
            String json = gson.toJson(taskList);
            Files.writeString(TASKS_FILE, json);
            System.out.println("[DataStore] Tasks persisted to " + TASKS_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadTasks() {
        try {
            if (Files.exists(TASKS_FILE)) {
                String json = Files.readString(TASKS_FILE);
                List<Task> taskList = gson.fromJson(json, new TypeToken<List<Task>>(){}.getType());
                
                if (taskList != null) {
                    for (Task task : taskList) {
                        tasks.put(task.getId(), task);
                    }
                    System.out.println("[DataStore] Loaded " + taskList.size() + " tasks from " + TASKS_FILE.toAbsolutePath());
                }
            } else {
                System.out.println("[DataStore] No existing tasks file found, starting fresh");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }
}