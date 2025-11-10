import { useState, useEffect } from "react";
import type { Task } from "@/types";
import { X } from "lucide-react";
import SmartTaskEnhancer from "./SmartTaskEnhancer";

interface TaskFormProps {
  task?: Task | null;
  onSubmit: (taskData: any) => void;
  onClose: () => void;
}

export default function TaskForm({ task, onSubmit, onClose }: TaskFormProps) {
  const [formData, setFormData] = useState({
    title: "",
    assignee: "",
    deadline: "",
    description: "",
    attachedUrl: "",
    weatherNote: "",
    priority: "medium" as "low" | "medium" | "high",
    status: "pending" as "pending" | "in-progress" | "completed",
  });

  useEffect(() => {
    if (task) {
      setFormData({
        title: task.title,
        assignee: task.assignee,
        deadline: task.deadline,
        description: (task as any).description || "",
        attachedUrl: (task as any).attachedUrl || "",
        weatherNote: (task as any).weatherNote || "",
        priority: task.priority,
        status: task.status,
      });
    }
  }, [task]);

  // Close modal on Escape key
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  const handleInsertMotivation = (text: string) => {
    setFormData({
      ...formData,
      description:
        formData.description + (formData.description ? "\n\n" : "") + text,
    });
  };

  const handleSetWeatherReminder = (weatherInfo: string) => {
    setFormData({ ...formData, weatherNote: weatherInfo });
  };

  const handleAttachUrl = (url: string, isValid: boolean) => {
    if (isValid) {
      setFormData({ ...formData, attachedUrl: url });
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <div
      className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onMouseDown={(e) => {
        // close when clicking on backdrop (not the modal content)
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col animate-fade-in border border-slate-200">
        {/* Fixed Header */}
        <div className="flex justify-between items-center p-6 border-b border-slate-200 flex-shrink-0">
          <h2 className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
            {task ? "Edit Task" : "Create New Task"}
          </h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg p-1 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto px-6 py-4">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Title *
              </label>
              <input
                type="text"
                required
                value={formData.title}
                onChange={(e) =>
                  setFormData({ ...formData, title: e.target.value })
                }
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                placeholder="Enter task title"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Assignee *
              </label>
              <input
                type="text"
                required
                value={formData.assignee}
                onChange={(e) =>
                  setFormData({ ...formData, assignee: e.target.value })
                }
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                placeholder="Enter assignee name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Deadline
              </label>
              <input
                type="date"
                value={formData.deadline}
                onChange={(e) =>
                  setFormData({ ...formData, deadline: e.target.value })
                }
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Task Details
              </label>

              {/* Smart Task Enhancer */}
              <div className="mb-4 p-4 bg-gradient-to-br from-slate-50 to-indigo-50 rounded-xl border border-indigo-200">
                <SmartTaskEnhancer
                  onInsertMotivation={handleInsertMotivation}
                  onSetReminder={handleSetWeatherReminder}
                  onAttachUrl={handleAttachUrl}
                />
              </div>

              {/* Description Field */}
              <textarea
                value={formData.description}
                onChange={(e) =>
                  setFormData({ ...formData, description: e.target.value })
                }
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all mb-2"
                placeholder="Task description..."
                rows={3}
              />

              {/* Attached URL Display */}
              {formData.attachedUrl && (
                <div className="mb-2 p-2 bg-green-50 border border-green-200 rounded-lg">
                  <p className="text-xs text-green-700 font-medium">
                    📎 Attached Link:
                  </p>
                  <a
                    href={formData.attachedUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-xs text-green-600 hover:underline break-all"
                  >
                    {formData.attachedUrl}
                  </a>
                </div>
              )}

              {/* Weather Note Display */}
              {formData.weatherNote && (
                <div className="mb-2 p-2 bg-cyan-50 border border-cyan-200 rounded-lg">
                  <p className="text-xs text-cyan-700">
                    {formData.weatherNote}
                  </p>
                </div>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Priority
              </label>
              <select
                value={formData.priority}
                onChange={(e) =>
                  setFormData({ ...formData, priority: e.target.value as any })
                }
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              >
                <option value="low">Low</option>
                <option value="medium">Medium</option>
                <option value="high">High</option>
              </select>
            </div>

            {task && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Status
                </label>
                <select
                  value={formData.status}
                  onChange={(e) =>
                    setFormData({ ...formData, status: e.target.value as any })
                  }
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                >
                  <option value="pending">Pending</option>
                  <option value="in-progress">In Progress</option>
                  <option value="completed">Completed</option>
                </select>
              </div>
            )}
          </form>
        </div>

        {/* Fixed Footer with Buttons */}
        <div className="flex gap-3 p-6 border-t border-slate-200 flex-shrink-0">
          <button
            onClick={handleSubmit}
            type="submit"
            className="flex-1 bg-gradient-to-r from-indigo-600 to-purple-600 text-white py-2.5 px-4 rounded-lg hover:from-indigo-700 hover:to-purple-700 transition-all font-medium shadow-md hover:shadow-lg"
          >
            {task ? "Update Task" : "Create Task"}
          </button>
          <button
            type="button"
            onClick={onClose}
            className="flex-1 bg-slate-100 text-slate-700 py-2.5 px-4 rounded-lg hover:bg-slate-200 transition-colors font-medium"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
