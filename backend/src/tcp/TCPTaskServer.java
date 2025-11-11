package tcp;

import com.google.gson.JsonObject;
import shared.DataStore;
import shared.JsonUtils;
import shared.Task;
import threading.ThreadPoolManager;
import threading.ExceptionHandler;
import udp.UDPNotificationServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class TCPTaskServer {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private ExecutorService threadPool;

    public TCPTaskServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000); // 1 second timeout for accept()
            running = true;

            threadPool = ThreadPoolManager.getThreadPool();

            System.out.println("[INFO] TCP Server started on port " + port);
            System.out.println("[INFO] Listening for client connections...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[INFO] Client connected: " + clientSocket.getInetAddress());
                    
                    threadPool.submit(() -> handleClient(clientSocket));
                    
                } catch (SocketTimeoutException e) {
                    // Timeout is normal, allows checking 'running' flag
                    continue;
                }
            }

        } catch (IOException e) {
            ExceptionHandler.handle(e, "TCP Server startup");
        } finally {
            stop();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Set read timeout
            clientSocket.setSoTimeout(5000); // 5 seconds

            // Read request
            String request = in.readLine();
            
            if (request == null || request.trim().isEmpty()) {
                out.println(JsonUtils.createErrorResponse("Empty request"));
                return;
            }

            System.out.println("[DEBUG] Received: " + request);

            // Process request and send response
            String response = processRequest(request);
            out.println(response);
            System.out.println("[DEBUG] Sent: " + response);

        } catch (SocketTimeoutException e) {
            ExceptionHandler.handle(e, "Client connection timeout");
        } catch (IOException e) {
            ExceptionHandler.handle(e, "Client communication error");
        } finally {
            try {
                clientSocket.close();
                System.out.println("[INFO] Client disconnected");
            } catch (IOException e) {
                ExceptionHandler.handle(e, "Closing client socket");
            }
        }
    }

    private String processRequest(String requestJson) {
        try {
            JsonObject request = JsonUtils.parseJson(requestJson);
            
            if (!request.has("action")) {
                return JsonUtils.createErrorResponse("Missing 'action' field");
            }

            String action = request.get("action").getAsString();

            switch (action) {
                case "CREATE_TASK":
                    return handleCreateTask(request);
                
                case "GET_TASKS":
                    return handleGetTasks();
                
                case "GET_TASK":
                    return handleGetTask(request);
                
                case "UPDATE_TASK":
                    return handleUpdateTask(request);
                
                case "DELETE_TASK":
                    return handleDeleteTask(request);
                
                default:
                    return JsonUtils.createErrorResponse("Unknown action: " + action);
            }

        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    private String handleCreateTask(JsonObject request) {
        try {
            if (!request.has("data")) {
                return JsonUtils.createErrorResponse("Missing 'data' field");
            }

            JsonObject data = request.getAsJsonObject("data");
            
            // Validate required fields
            if (!data.has("title") || !data.has("assignee")) {
                return JsonUtils.createErrorResponse("Missing required fields: title, assignee");
            }

            // Generate unique ID
            String id = "task_" + System.currentTimeMillis();
            String title = data.get("title").getAsString();
            String assignee = data.get("assignee").getAsString();
            String deadline = data.has("deadline") ? data.get("deadline").getAsString() : "";
            String priority = data.has("priority") ? data.get("priority").getAsString() : "medium";

            // Create and store task
            Task task = new Task(id, title, assignee, deadline, priority);
            // Optional fields
            if (data.has("description")) task.setDescription(data.get("description").getAsString());
            if (data.has("attachedUrl")) task.setAttachedUrl(data.get("attachedUrl").getAsString());
            if (data.has("weatherNote")) task.setWeatherNote(data.get("weatherNote").getAsString());
            DataStore.addTask(task);

            // Broadcast with assignee name in the format:
            // TASK_CREATED|task_123|Task 'title' assigned to John Doe
            String notification = "TASK_CREATED|" + task.getId() + "|Task '" + task.getTitle() + "' assigned to " + task.getAssignee();
            UDPNotificationServer.broadcast(notification);


            // Return success response
            JsonObject responseData = new JsonObject();
            responseData.addProperty("taskId", id);
            responseData.addProperty("message", "Task created successfully");
            
            return JsonUtils.createSuccessResponse(responseData);

        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    private String handleGetTasks() {
        try {
            List<Task> tasks = DataStore.getAllTasks();
            return JsonUtils.createSuccessResponse(tasks);
        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    private String handleGetTask(JsonObject request) {
        try {
            if (!request.has("data")) {
                return JsonUtils.createErrorResponse("Missing 'data' field");
            }

            JsonObject data = request.getAsJsonObject("data");
            
            if (!data.has("taskId")) {
                return JsonUtils.createErrorResponse("Missing 'taskId' field");
            }

            String taskId = data.get("taskId").getAsString();
            Task task = DataStore.getTask(taskId);

            if (task == null) {
                return JsonUtils.createErrorResponse("Task not found: " + taskId);
            }

            return JsonUtils.createSuccessResponse(task);

        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    private String handleUpdateTask(JsonObject request) {
        try {
            if (!request.has("data")) {
                return JsonUtils.createErrorResponse("Missing 'data' field");
            }

            JsonObject data = request.getAsJsonObject("data");
            
            if (!data.has("taskId")) {
                return JsonUtils.createErrorResponse("Missing 'taskId' field");
            }

            String taskId = data.get("taskId").getAsString();
            Task task = DataStore.getTask(taskId);

            if (task == null) {
                return JsonUtils.createErrorResponse("Task not found: " + taskId);
            }

            // Update fields if provided
            if (data.has("title")) {
                task.setTitle(data.get("title").getAsString());
            }
            if (data.has("assignee")) {
                task.setAssignee(data.get("assignee").getAsString());
            }
            if (data.has("status")) {
                task.setStatus(data.get("status").getAsString());
            }
            if (data.has("deadline")) {
                task.setDeadline(data.get("deadline").getAsString());
            }
            if (data.has("priority")) {
                task.setPriority(data.get("priority").getAsString());
            }
            if (data.has("description")) task.setDescription(data.get("description").getAsString());
            if (data.has("attachedUrl")) task.setAttachedUrl(data.get("attachedUrl").getAsString());
            if (data.has("weatherNote")) task.setWeatherNote(data.get("weatherNote").getAsString());

            DataStore.updateTask(taskId, task);

            // Broadcast with format: TASK_UPDATED|task_123|Task 'title' updated by Assignee Name
            String notification = "TASK_UPDATED|" + taskId + "|Task '" + task.getTitle() + "' updated by " + task.getAssignee();
            UDPNotificationServer.broadcast(notification);


            return JsonUtils.createSuccessResponse("Task updated successfully");

        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    private String handleDeleteTask(JsonObject request) {
        try {
            if (!request.has("data")) {
                return JsonUtils.createErrorResponse("Missing 'data' field");
            }

            JsonObject data = request.getAsJsonObject("data");
            
            if (!data.has("taskId")) {
                return JsonUtils.createErrorResponse("Missing 'taskId' field");
            }

            String taskId = data.get("taskId").getAsString();
            
            // Get task before deleting to include assignee in notification
            Task task = DataStore.getTask(taskId);
            boolean deleted = DataStore.deleteTask(taskId);

            if (deleted && task != null) {
                // Broadcast with format: TASK_DELETED|task_123|Task 'title' deleted (was assigned to Assignee Name)
                String notification = "TASK_DELETED|" + taskId + "|Task '" + task.getTitle() + "' deleted (was assigned to " + task.getAssignee() + ")";
                UDPNotificationServer.broadcast(notification);
            }

            if (!deleted) {
                return JsonUtils.createErrorResponse("Task not found: " + taskId);
            }

            return JsonUtils.createSuccessResponse("Task deleted successfully");

        } catch (Exception e) {
            return JsonUtils.createErrorResponse(e);
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                ThreadPoolManager.shutdown();
                System.out.println("[INFO] TCP Server stopped");
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e, "Stopping TCP server");
        }
    }

    // For testing
    public static void main(String[] args) {
        TCPTaskServer server = new TCPTaskServer(8080);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutting down server...");
            server.stop();
        }));
        
        server.start();
    }
}