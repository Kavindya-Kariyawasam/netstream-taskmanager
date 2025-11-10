//index.ts
export interface Task {
  id: string;
  title: string;
  assignee: string;
  status: "pending" | "in-progress" | "completed";
  deadline: string;
  priority: "low" | "medium" | "high";
  createdAt: string;
  updatedAt: string;
}

export interface Notification {
  id: string;
  title: string;
  body?: string;
  // backend may return ISO string or a numeric timestamp
  createdAt: string | number;
  read?: boolean;
}

export interface ApiResponse<T = any> {
  status: "success" | "error";
  data?: T;
  message?: string;
}

export interface CreateTaskRequest {
  action: "CREATE_TASK";
  data: {
    title: string;
    assignee: string;
    deadline?: string;
    priority?: "low" | "medium" | "high";
  };
}

export interface GetTasksRequest {
  action: "GET_TASKS";
}

export interface UpdateTaskRequest {
  action: "UPDATE_TASK";
  data: {
    taskId: string;
    title?: string;
    assignee?: string;
    status?: "pending" | "in-progress" | "completed";
    deadline?: string;
    priority?: "low" | "medium" | "high";
  };
}

export interface DeleteTaskRequest {
  action: "DELETE_TASK";
  data: {
    taskId: string;
  };
}

export interface Notification {
  id: string;
  type: "TASK_CREATED" | "TASK_UPDATED" | "TASK_DELETED" | "TASK_ASSIGNED";
  taskId: string;
  message: string;
  timestamp: number;
  read: boolean;
}

export interface Quote {
  quote: string;
  author: string;
}

export interface Avatar {
  avatarUrl: string;
  email: string;
}

export interface FileUploadResponse {
  status: string;
  fileId: string;
  fileName: string;
  size: number;
}
