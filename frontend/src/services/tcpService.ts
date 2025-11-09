import axios from "axios";
import type {
  Task,
  ApiResponse,
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
};
