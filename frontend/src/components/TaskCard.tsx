import type { Task } from "@/types";
import { Edit2, Trash2, Clock, User } from "lucide-react";

interface TaskCardProps {
  task: Task;
  onEdit: (task: Task) => void;
  onDelete: (taskId: string) => void;
}

const statusColors = {
  pending: "bg-amber-100 text-amber-800 border border-amber-200",
  "in-progress": "bg-sky-100 text-sky-800 border border-sky-200",
  completed: "bg-emerald-100 text-emerald-800 border border-emerald-200",
};

const priorityColors = {
  low: "bg-slate-100 text-slate-700 border border-slate-200",
  medium: "bg-orange-100 text-orange-800 border border-orange-200",
  high: "bg-rose-100 text-rose-800 border border-rose-200",
};

export default function TaskCard({ task, onEdit, onDelete }: TaskCardProps) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-5 hover:shadow-md hover:border-indigo-200 transition-all animate-fade-in">
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-semibold text-slate-900 flex-1">
          {task.title}
        </h3>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(task)}
            className="p-1.5 text-indigo-600 hover:text-indigo-800 hover:bg-indigo-50 rounded-lg transition-colors"
            title="Edit task"
          >
            <Edit2 className="w-4 h-4" />
          </button>
          <button
            onClick={() => onDelete(task.id)}
            className="p-1.5 text-rose-600 hover:text-rose-800 hover:bg-rose-50 rounded-lg transition-colors"
            title="Delete task"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 text-sm text-slate-600 mb-2">
        <User className="w-4 h-4" />
        <span>{task.assignee}</span>
      </div>

      {task.deadline && (
        <div className="flex items-center gap-2 text-sm text-slate-600 mb-3">
          <Clock className="w-4 h-4" />
          <span>{new Date(task.deadline).toLocaleDateString()}</span>
        </div>
      )}

      <div className="flex gap-2">
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${
            statusColors[task.status]
          }`}
        >
          {task.status}
        </span>
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${
            priorityColors[task.priority]
          }`}
        >
          {task.priority}
        </span>
      </div>
    </div>
  );
}
