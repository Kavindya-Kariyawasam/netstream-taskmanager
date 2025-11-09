import { useState, useEffect } from "react";
import { Bell, X, Trash2, UserPlus, Megaphone, Sparkles } from "lucide-react";
import { udpService } from "@/services/udpService";
import type { Notification } from "@/types";

export default function NotificationBell() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);

  useEffect(() => {
    const unsubscribe = udpService.subscribe((notification) => {
      setNotifications((prev) => [notification, ...prev].slice(0, 50));
    });

    return unsubscribe;
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const handleMarkAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const handleClearAll = () => {
    setNotifications([]);
  };

  const removeNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const getNotificationIcon = (type: Notification["type"]) => {
    switch (type) {
      case "TASK_CREATED":
        return <Sparkles className="w-5 h-5 text-indigo-600" />;
      case "TASK_UPDATED":
        return <Megaphone className="w-5 h-5 text-sky-600" />;
      case "TASK_DELETED":
        return <Trash2 className="w-5 h-5 text-rose-600" />;
      case "TASK_ASSIGNED":
        return <UserPlus className="w-5 h-5 text-emerald-600" />;
      default:
        return <Bell className="w-5 h-5 text-slate-600" />;
    }
  };

  const formatTime = (ts: number) => {
    try {
      return new Date(ts).toLocaleString();
    } catch (e) {
      return "-";
    }
  };

  return (
    <div className="relative">
      <button
        onClick={() => setShowDropdown((s) => !s)}
        className="relative p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-full transition-colors"
        aria-label="Notifications"
      >
        <Bell className="w-6 h-6" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex items-center justify-center w-5 h-5 text-[10px] font-bold text-white bg-rose-500 rounded-full">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {showDropdown && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setShowDropdown(false)}
            aria-hidden
          />

          <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-2xl border border-slate-200 z-50">
            <div className="flex items-center justify-between p-4 border-b border-slate-200">
              <h3 className="font-semibold text-slate-900">Notifications</h3>

              <div className="flex items-center gap-2">
                {notifications.length > 0 && (
                  <>
                    <button
                      onClick={handleMarkAllRead}
                      className="text-xs text-indigo-600 hover:text-indigo-800"
                    >
                      Mark all read
                    </button>
                    <button
                      onClick={handleClearAll}
                      className="text-xs text-rose-600 hover:text-rose-800"
                    >
                      Clear all
                    </button>
                  </>
                )}

                <button
                  onClick={() => setShowDropdown(false)}
                  className="text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg p-1 transition-colors"
                  title="Close"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="max-h-96 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="p-8 text-center text-slate-500">
                  <Bell className="w-12 h-12 mx-auto mb-2 opacity-50" />
                  <p>No notifications yet</p>
                </div>
              ) : (
                notifications.map((notif) => (
                  <div
                    key={notif.id}
                    className={`p-4 border-b border-slate-100 hover:bg-slate-50 transition-colors ${
                      !notif.read ? "bg-indigo-50" : ""
                    }`}
                  >
                    <div className="flex items-start gap-3">
                      <div className="mt-0.5">
                        {getNotificationIcon(notif.type)}
                      </div>

                      <div className="flex-1">
                        <p className="text-sm text-slate-900">
                          {notif.message}
                        </p>
                        <p className="text-xs text-slate-500 mt-1">
                          {formatTime(notif.timestamp)}
                        </p>
                      </div>

                      <div className="ml-2 flex-shrink-0">
                        <button
                          onClick={() => removeNotification(notif.id)}
                          className="p-1 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded transition-colors"
                          title="Delete"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
