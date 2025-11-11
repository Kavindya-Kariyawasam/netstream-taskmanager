import { useState, useEffect } from "react";
import { tcpService } from "@/services/tcpService";
import type { Task } from "@/types";
import TaskCard from "./TaskCard";
import TaskForm from "./TaskForm";
import { Plus, RefreshCw, Loader2 } from "lucide-react";

export default function TaskList() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [filterStatus, setFilterStatus] = useState<
    "all" | "pending" | "in-progress" | "completed"
  >("all");
  const [sortBy, setSortBy] = useState<
    "newest" | "oldest" | "deadline" | "priority"
  >("newest");

  // Fetch tasks on component mount
  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await tcpService.getTasks();

      if (response.status === "success" && response.data) {
        setTasks(response.data);
      } else {
        setError(response.message || "Failed to fetch tasks");
      }
    } catch (err: any) {
      setError(err.message || "Network error");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (taskData: any) => {
    try {
      const response = await tcpService.createTask(taskData);

      if (response.status === "success") {
        await fetchTasks();
        setShowForm(false);
        // alert("Task created successfully!");
      } else {
        // alert("Error: " + response.message);
        console.error("Task creation failed:", response.message);
      }
    } catch (err: any) {
      // alert("Error: " + err.message);
      console.error("Task creation error:", err.message);
    }
  };

  const handleUpdateTask = async (taskData: any) => {
    if (!editingTask) return;

    try {
      const response = await tcpService.updateTask(editingTask.id, taskData);

      if (response.status === "success") {
        await fetchTasks();
        setShowForm(false);
        setEditingTask(null);
        // alert("Task updated successfully!");
      } else {
        // alert("Error: " + response.message);
        console.error("Task update failed:", response.message);
      }
    } catch (err: any) {
      // alert("Error: " + err.message);
      console.error("Task update error:", err.message);
    }
  };

  const handleDeleteTask = async (taskId: string) => {
    if (!confirm("Are you sure you want to delete this task?")) return;

    try {
      const response = await tcpService.deleteTask(taskId);

      if (response.status === "success") {
        await fetchTasks();
        // alert("Task deleted successfully!");
      } else {
        // alert("Error: " + response.message);
        console.error("Task deletion failed:", response.message);
      }
    } catch (err: any) {
      // alert("Error: " + err.message);
      console.error("Task deletion error:", err.message);
    }
  };

  const handleEditTask = (task: Task) => {
    setEditingTask(task);
    setShowForm(true);
  };

  const handleCloseForm = () => {
    setShowForm(false);
    setEditingTask(null);
  };

  // Apply a deterministic sort before filtering so the order is stable.
  const filteredTasks = (() => {
    const sorted = tasks.slice();
    if (sortBy === "newest") {
      sorted.sort((a, b) => {
        const ta = a.createdAt ? Date.parse(String(a.createdAt)) : 0;
        const tb = b.createdAt ? Date.parse(String(b.createdAt)) : 0;
        return tb - ta;
      });
    } else if (sortBy === "oldest") {
      // oldest first by createdAt
      sorted.sort((a, b) => {
        const ta = a.createdAt ? Date.parse(String(a.createdAt)) : 0;
        const tb = b.createdAt ? Date.parse(String(b.createdAt)) : 0;
        return ta - tb;
      });
    } else if (sortBy === "deadline") {
      // earliest deadline first; tasks without deadlines go to the end
      sorted.sort((a, b) => {
        const da = a.deadline ? Date.parse(String(a.deadline)) : Infinity;
        const db = b.deadline ? Date.parse(String(b.deadline)) : Infinity;
        return da - db;
      });
    } else if (sortBy === "priority") {
      const score = (p?: string) => (p === "high" ? 3 : p === "medium" ? 2 : 1);
      sorted.sort((a, b) => score(b.priority) - score(a.priority));
    }

    return filterStatus === "all"
      ? sorted
      : sorted.filter((task) => task.status === filterStatus);
  })();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">Tasks</h2>
          <p className="text-slate-600 mt-1">
            {tasks.length} {tasks.length === 1 ? "task" : "tasks"} total
          </p>
        </div>
        <div className="flex gap-3 items-center">
          <label className="text-sm text-slate-600 mr-2">Sort:</label>
          <div className="relative">
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value as any)}
              className="appearance-none px-3 py-2 pr-10 border border-slate-200 rounded-md bg-white text-sm text-slate-700"
            >
              <option value="newest">Newest</option>
              <option value="oldest">Oldest</option>
              <option value="deadline">Deadline</option>
              <option value="priority">Priority</option>
            </select>
            {/* Custom arrow positioned inside so it doesn't sit on the border */}
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-500">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="w-4 h-4"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M10 12a1 1 0 01-.707-.293l-3-3a1 1 0 011.414-1.414L10 9.586l2.293-2.293a1 1 0 011.414 1.414l-3 3A1 1 0 0110 12z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          </div>

          <div className="ml-2" />

          <button
            onClick={fetchTasks}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors disabled:opacity-50 shadow-sm"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <RefreshCw className="w-4 h-4" />
            )}
            Refresh
          </button>
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg hover:from-indigo-700 hover:to-purple-700 transition-all shadow-md hover:shadow-lg"
          >
            <Plus className="w-4 h-4" />
            New Task
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex gap-2 border-b border-slate-200">
        {(["all", "pending", "in-progress", "completed"] as const).map(
          (status) => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-4 py-2 font-medium transition-colors ${
                filterStatus === status
                  ? "text-indigo-600 border-b-2 border-indigo-600"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              {status === "all"
                ? "All"
                : status.charAt(0).toUpperCase() +
                  status.slice(1).replace("-", " ")}
              <span className="ml-2 text-sm">
                (
                {status === "all"
                  ? tasks.length
                  : tasks.filter((t) => t.status === status).length}
                )
              </span>
            </button>
          )
        )}
      </div>

      {/* Loading State */}
      {loading && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
          <span className="ml-3 text-slate-600">Loading tasks...</span>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-lg">
          <p className="font-medium">Error</p>
          <p className="text-sm mt-1">{error}</p>
          <button
            onClick={fetchTasks}
            className="mt-2 text-sm text-rose-600 hover:text-rose-800 underline"
          >
            Try again
          </button>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && filteredTasks.length === 0 && (
        <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-slate-200">
          <p className="text-slate-600 text-lg">No tasks found</p>
          <p className="text-slate-500 text-sm mt-2">
            {filterStatus === "all"
              ? "Create your first task to get started!"
              : `No ${filterStatus} tasks yet`}
          </p>
          {filterStatus === "all" && (
            <button
              onClick={() => setShowForm(true)}
              className="mt-4 px-6 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg hover:from-indigo-700 hover:to-purple-700 transition-all shadow-md hover:shadow-lg"
            >
              Create Task
            </button>
          )}
        </div>
      )}

      {/* Task Grid */}
      {!loading && !error && filteredTasks.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onEdit={handleEditTask}
              onDelete={handleDeleteTask}
            />
          ))}
        </div>
      )}

      {/* Task Form Modal */}
      {showForm && (
        <TaskForm
          task={editingTask}
          onSubmit={editingTask ? handleUpdateTask : handleCreateTask}
          onClose={handleCloseForm}
        />
      )}
    </div>
  );
}
