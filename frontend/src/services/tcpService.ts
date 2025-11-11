import axios from "axios";
import type {
  Task,
  ApiResponse,
  Notification,
  CreateTaskRequest,
  GetTasksRequest,
  UpdateTaskRequest,
  DeleteTaskRequest,
} from "@/types";

// Connect to HTTP Gateway (for browser clients) which forwards to the TCP server
const TCP_GATEWAY_URL = "http://localhost:3000";

// Create axios instance
const tcpClient = axios.create({
  baseURL: TCP_GATEWAY_URL,
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Helper function to send requests
const sendRequest = async <T>(data: any): Promise<ApiResponse<T>> => {
  try {
    const response = await tcpClient.post("/", data);
    return response.data;
  } catch (error: any) {
    if (error.response) {
      return error.response.data;
    }
    throw new Error(error.message || "Network error");
  }
};

export const tcpService = {
  // Create a new task
  createTask: async (taskData: {
    title: string;
    assignee: string;
    deadline?: string;
    priority?: "low" | "medium" | "high";
    description?: string;
    attachedUrl?: string;
    weatherNote?: string;
  }): Promise<ApiResponse<{ taskId: string; message: string }>> => {
    const request: CreateTaskRequest = {
      action: "CREATE_TASK",
      data: taskData,
    };
    return sendRequest(request);
  },

  // Get all tasks
  getTasks: async (): Promise<ApiResponse<Task[]>> => {
    const request: GetTasksRequest = {
      action: "GET_TASKS",
    };
    return sendRequest(request);
  },

  // Get single task
  getTask: async (taskId: string): Promise<ApiResponse<Task>> => {
    const request = {
      action: "GET_TASK",
      data: { taskId },
    };
    return sendRequest(request);
  },

  // Update task
  updateTask: async (
    taskId: string,
    updates: Partial<Omit<Task, "id" | "createdAt" | "updatedAt">>
  ): Promise<ApiResponse<string>> => {
    const request: UpdateTaskRequest = {
      action: "UPDATE_TASK",
      data: {
        taskId,
        ...updates,
      },
    };
    return sendRequest(request);
  },

  // Delete task
  deleteTask: async (taskId: string): Promise<ApiResponse<string>> => {
    const request: DeleteTaskRequest = {
      action: "DELETE_TASK",
      data: { taskId },
    };
    return sendRequest(request);
  },

  // Get notifications (HTTP GET /notifications on the gateway)
  getNotifications: async (): Promise<ApiResponse<Notification[]>> => {
    try {
      const response = await tcpClient.get("/notifications");
      const body = response.data as ApiResponse<any>;

      // Backend compatibility: some backends return an array of pipe-separated strings
      // Format can be 3 or 4 parts:
      // "TASK_CREATED|task_123|New task assigned to John|1729350000000" (4 parts)
      // "TASK_UPDATED|task_123|completion of report" (3 parts)
      if (
        body &&
        body.status === "success" &&
        Array.isArray(body.data) &&
        body.data.length > 0 &&
        typeof body.data[0] === "string"
      ) {
        const mapped = (body.data as string[]).map((s, i) => {
          const parts = s.split("|");
          const eventType = parts[0] || "Notification";
          const taskId = parts[1] || `notif_${i}`;
          const message = parts[2] || eventType;
          const timestamp = parts[3]; // may be undefined

          return {
            id: taskId,
            title: message, // Use message as title for stored notifications
            body: eventType.replace(/_/g, " "), // Event type as body
            createdAt: timestamp ? Number(timestamp) || timestamp : Date.now(),
          } as Notification;
        });
        return { status: "success", data: mapped };
      }

      return body;
    } catch (error: any) {
      if (error.response) return error.response.data;
      throw new Error(error.message || "Network error");
    }
  },

  // Subscribe to real-time notifications via Server-Sent Events (SSE)
  // Returns an EventSource; caller is responsible for cleanup (.close())
  subscribeToNotifications: (
    onNotification: (notif: Notification) => void,
    onError?: (err: Event) => void
  ): EventSource => {
    const eventSource = new EventSource(`${TCP_GATEWAY_URL}/events`);

    eventSource.onmessage = (event) => {
      try {
        // Backend sends pipe-separated strings with 3 OR 4 parts:
        // Format: "TASK_CREATED|task_123|Task 'Complete the Report' assigned to Hermiony"
        // Format: "TASK_UPDATED|task_123|Task 'helu' updated by hello"
        const data = event.data;
        if (typeof data === "string" && data.includes("|")) {
          const parts = data.split("|");
          const eventType = parts[0] || "Notification";
          const taskId = parts[1] || `notif_${Date.now()}`;
          const message = parts[2] || "";
          const timestamp = parts[3]; // may be undefined

          // Parse the message to extract task title and assignee/updater
          // Expected formats:
          // - "Task 'Complete the Report' assigned to Hermiony"
          // - "Task 'helu' updated by hello"
          let taskTitle = "";
          let person = "";
          let personLabel = "Assigned to";

          // Extract task title from single quotes
          const titleMatch = message.match(/Task '([^']+)'/);
          if (titleMatch) {
            taskTitle = titleMatch[1];
          }

          // Extract assignee after "assigned to"
          const assigneeMatch = message.match(/assigned to (.+)/);
          if (assigneeMatch) {
            person = assigneeMatch[1];
            personLabel = "Assigned to";
          } else {
            // Extract updater after "updated by"
            const updaterMatch = message.match(/updated by (.+)/);
            if (updaterMatch) {
              person = updaterMatch[1];
              personLabel = "Updated by";
            }
          }

          // Format a more informative title based on event type
          const eventTypeFormatted = eventType.replace(/_/g, " ");

          // Build structured body
          let body = "";
          if (taskTitle && person) {
            body = `Task: ${taskTitle}\n${personLabel}: ${person}`;
          } else if (taskTitle) {
            body = `Task: ${taskTitle}`;
          } else {
            // Fallback to raw message if parsing fails
            body = message;
          }

          const notif: Notification = {
            id: taskId,
            title: eventTypeFormatted, // e.g., "TASK CREATED" or "TASK UPDATED"
            body: body,
            createdAt: timestamp ? Number(timestamp) || timestamp : Date.now(),
          };
          onNotification(notif);
        }
      } catch (err) {
        console.error("Error parsing SSE notification:", err);
      }
    };

    eventSource.onerror = (err) => {
      console.error("SSE connection error:", err);
      onError?.(err);
    };

    return eventSource;
  },
};
