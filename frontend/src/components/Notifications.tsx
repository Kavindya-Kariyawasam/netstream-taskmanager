import { useEffect, useRef, useState } from "react";
import { Bell } from "lucide-react";
import type { Notification as NotificationType } from "@/types";

function formatCreatedAt(value: string | number | undefined) {
  if (value === undefined || value === null) return "";

  // If the backend sends a numeric timestamp (ms or s) or a numeric string, handle it
  if (typeof value === "number") {
    const d = new Date(value);
    if (!isNaN(d.getTime())) return d.toLocaleString();
    return String(value);
  }

  // try parse numeric string
  const num = Number(value);
  if (!isNaN(num)) {
    const d = new Date(num);
    if (!isNaN(d.getTime())) return d.toLocaleString();
  }

  // try Date.parse for ISO strings
  const parsed = Date.parse(value);
  if (!isNaN(parsed)) return new Date(parsed).toLocaleString();

  // fallback to raw string
  return String(value);
}

export default function Notifications({
  visible,
  onClose,
  onToggle,
  notifications,
  onMarkAsRead,
  onMarkAllAsRead,
  onClearAll,
  onRefresh,
}: {
  visible: boolean;
  onClose: () => void;
  onToggle: () => void;
  notifications: NotificationType[];
  onMarkAsRead: (id: string) => void;
  onMarkAllAsRead: () => void;
  onClearAll: () => void;
  onRefresh: () => void;
}) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const rootRef = useRef<HTMLDivElement | null>(null);

  // Close when clicking outside
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (!visible) return;
      const el = rootRef.current;
      if (!el) return;
      if (!(e.target instanceof Node)) return;
      if (!el.contains(e.target)) {
        onClose();
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [visible, onClose]);

  // Close on Escape
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape" && visible) onClose();
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [visible, onClose]);

  const doRefresh = async () => {
    setLoading(true);
    setError(null);
    try {
      await onRefresh();
    } catch (e: any) {
      setError(e?.message || "Failed to refresh");
    } finally {
      setLoading(false);
    }
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="relative" ref={rootRef}>
      {/* Bell Button */}
      <button
        onClick={onToggle}
        className="relative p-2 text-slate-600 hover:text-indigo-600 hover:bg-indigo-50 rounded-full transition-colors"
        title="Notifications"
      >
        <Bell className="w-6 h-6" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-xs font-bold text-white ring-2 ring-white">
            {unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Panel */}
      {visible && (
        <div className="absolute right-0 mt-2 w-96 bg-white rounded-lg shadow-xl border border-slate-200 z-50 max-h-[32rem] flex flex-col">
          {/* Header */}
          <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-gradient-to-r from-indigo-50 to-purple-50">
            <h3 className="text-lg font-semibold text-slate-800">
              Notifications {unreadCount > 0 && `(${unreadCount})`}
            </h3>
            <div className="flex items-center gap-2">
              {notifications.length > 0 && (
                <>
                  {unreadCount > 0 && (
                    <button
                      onClick={onMarkAllAsRead}
                      className="text-xs text-indigo-600 hover:text-indigo-800 font-medium hover:bg-white px-2 py-1 rounded transition-colors"
                    >
                      Mark all read
                    </button>
                  )}
                  <button
                    onClick={onClearAll}
                    className="text-xs text-rose-600 hover:text-rose-800 font-medium hover:bg-white px-2 py-1 rounded transition-colors"
                  >
                    Clear all
                  </button>
                </>
              )}
              <button
                onClick={onClose}
                className="text-slate-500 hover:text-slate-800 text-sm font-medium hover:bg-white px-3 py-1 rounded-lg transition-colors"
              >
                Close
              </button>
            </div>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto">
            {loading && (
              <div className="p-8 text-center text-slate-500">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
                <p className="mt-2">Loading notifications...</p>
              </div>
            )}
            {error && (
              <div className="p-4 text-center text-rose-600 bg-rose-50 m-3 rounded-lg">
                {error}
              </div>
            )}
            {!loading && !error && notifications.length === 0 && (
              <div className="p-8 text-center text-slate-500">
                <Bell className="w-12 h-12 mx-auto mb-3 text-slate-300" />
                <p className="font-medium">No notifications yet</p>
                <p className="text-sm mt-1">
                  You'll see updates here when tasks are created or modified
                </p>
              </div>
            )}
            {!loading && !error && notifications.length > 0 && (
              <div className="divide-y divide-slate-100">
                {notifications.map((n) => (
                  <div
                    key={n.id ?? `${n.title || "notif"}-${n.createdAt ?? ""}`}
                    className={`p-4 hover:bg-slate-50 transition-colors cursor-pointer ${
                      !n.read ? "bg-indigo-50" : ""
                    }`}
                    onClick={() => onMarkAsRead(n.id ?? "")}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1">
                        <p className="font-semibold text-slate-800">
                          {n.title}
                        </p>
                        {n.body && (
                          <div className="text-sm text-slate-600 mt-1 space-y-1">
                            {n.body.split("\n").map((line, idx) => (
                              <p key={idx}>{line}</p>
                            ))}
                          </div>
                        )}
                        <div className="text-xs text-slate-400 mt-2">
                          {formatCreatedAt(n.createdAt)}
                        </div>
                      </div>
                      {!n.read && (
                        <span className="inline-block w-2 h-2 bg-indigo-500 rounded-full flex-shrink-0 mt-1.5"></span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="p-3 border-t border-slate-100 text-center bg-slate-50 flex items-center justify-between">
            <div>
              <button
                onClick={doRefresh}
                className="text-sm text-indigo-600 hover:text-indigo-800 font-medium hover:underline"
              >
                Refresh Notifications
              </button>
            </div>
            <div className="text-xs text-slate-400">Press Esc to close</div>
          </div>
        </div>
      )}
    </div>
  );
}
