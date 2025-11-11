import type { Task } from "@/types";
import { Edit2, Trash2, Clock, User, Link2, Cloud } from "lucide-react";

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
  const rawNote =
    (task as any).description ||
    (task as any).note ||
    (task as any).notes ||
    "";
  const rawWeather = (task as any).weatherNote || "";

  // Clean up replacement characters and zero-width joiners that sometimes
  // appear when emoji pass through different systems (they show as � or ?).
  const cleanText = (s?: string) =>
    (s || "")
      .replace(/\uFFFD/g, "")
      .replace(/\uFE0F/g, "")
      .replace(/\u200D/g, "")
      .trim();

  const note = cleanText(rawNote);
  const weather = cleanText(rawWeather);

  // Choose accent color by status for left border
  const statusAccent =
    {
      pending: "border-indigo-300",
      "in-progress": "border-sky-300",
      completed: "border-emerald-300",
    }[task.status] || "border-slate-200";

  return (
    <div
      className={`w-full bg-white rounded-xl shadow-sm border ${statusAccent} border-l-4 p-5 hover:shadow-lg transition-all transform hover:-translate-y-0.5 animate-fade-in`}
    >
      <div className="flex justify-between items-start mb-3">
        <h3 className="text-lg font-semibold text-slate-900 flex-1">
          <span className="bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
            {task.title}
          </span>
        </h3>
        <div className="flex gap-2">
          <button
            onClick={() => onEdit(task)}
            className="p-1.5 text-white bg-gradient-to-r from-indigo-500 to-purple-500 hover:from-indigo-600 hover:to-purple-600 rounded-lg transition-all shadow-sm"
            title="Edit task"
          >
            <Edit2 className="w-4 h-4" />
          </button>
          <button
            onClick={() => onDelete(task.id)}
            className="p-1.5 text-white bg-gradient-to-r from-rose-500 to-rose-600 hover:from-rose-600 hover:to-rose-700 rounded-lg transition-all shadow-sm"
            title="Delete task"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 text-sm text-slate-600 mb-2">
        <User className="w-4 h-4 text-slate-500" />
        <span className="font-medium text-slate-700">{task.assignee}</span>
      </div>

      {task.deadline && (
        <div className="flex items-center gap-2 text-sm text-slate-600 mb-3">
          <Clock className="w-4 h-4 text-slate-500" />
          <span className="text-slate-700">
            {new Date(task.deadline).toLocaleDateString()}
          </span>
        </div>
      )}

      {/* Note / Description */}
      {note && (
        <div className="mb-3 p-3 bg-indigo-50 rounded-lg border border-indigo-200">
          <p className="text-sm text-slate-700 whitespace-pre-line">{note}</p>
        </div>
      )}

      {/* Attached URL */}
      {(task as any).attachedUrl && (
        <div className="mb-3 p-2 bg-green-50 rounded-lg border border-green-200 flex items-start gap-2">
          <Link2 className="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-xs font-medium text-green-700 mb-1">
              Resource Link:
            </p>
            <a
              href={(task as any).attachedUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xs text-green-600 hover:text-green-800 hover:underline break-all"
            >
              {(task as any).attachedUrl}
            </a>
          </div>
        </div>
      )}

      {/* Weather Note */}
      {weather && (
        <div className="mb-3 p-2 bg-cyan-50 rounded-lg border border-cyan-200 flex items-start gap-2">
          <Cloud className="w-4 h-4 text-cyan-600 mt-0.5 flex-shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-xs text-cyan-700">{weather}</p>
          </div>
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
