import { useState, useEffect } from "react";
import { Bell, Globe, ListTodo } from "lucide-react";
import TaskList from "./components/TaskList";
import Notifications from "./components/Notifications";
import ToastContainer from "./components/Toast";
import type { ToastMessage } from "./components/Toast";
import URLServiceDemo from "./components/URLServiceDemo";
import { tcpService } from "./services/tcpService";

function App() {
  const [showNotifications, setShowNotifications] = useState(false);
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState<"tasks" | "url">("tasks");
  const [serverStatus, setServerStatus] = useState({
    tcp: false,
    gateway: false,
    url: false,
    udp: false,
    nio: false,
  });

  // Check server status on mount
  useEffect(() => {
    const checkServers = async () => {
      const status = {
        tcp: false,
        gateway: false,
        url: false,
        udp: false,
        nio: false,
      };

      try {
        // Check gateway (which also checks TCP since gateway forwards to TCP)
        const gatewayResp = await fetch("http://localhost:3000/notifications", {
          method: "GET",
        });
        if (gatewayResp.ok) {
          status.gateway = true;
          status.tcp = true; // Gateway is working, so TCP is working too
        }
      } catch (e) {
        console.log("Gateway not available");
      }

      try {
        // Check URL service
        // Use gateway to probe URL service (POST /url-service). Gateway will return JSON even if action unsupported.
        try {
          const probe = await fetch("http://localhost:3000/url-service", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ action: "PING" }),
          });
          if (probe.ok) status.url = true;
        } catch (e) {
          // fallback: try direct port 8082 (in case gateway not used)
          try {
            const urlResp = await fetch("http://localhost:8082/", {
              method: "GET",
            });
            if (urlResp.ok) status.url = true;
          } catch (e2) {
            // ignore
          }
        }
      } catch (e) {
        console.log("URL service not available");
      }

      // UDP and NIO don't have HTTP endpoints, so we'll assume they're running if TCP is running
      // In a real app, you'd have proper health checks
      status.udp = status.tcp;
      status.nio = status.tcp;

      setServerStatus(status);
    };

    checkServers();
    const interval = setInterval(checkServers, 10000); // Check every 10 seconds

    return () => clearInterval(interval);
  }, []);

  // Subscribe to real-time notifications via SSE
  useEffect(() => {
    const eventSource = tcpService.subscribeToNotifications(
      (notif) => {
        // Show toast notification
        const toast: ToastMessage = {
          id: notif.id,
          title: notif.title,
          body: notif.body,
          type: "info",
          duration: 5000, // auto-dismiss after 5 seconds
        };
        setToasts((prev) => [...prev, toast]);

        // Prepend to notifications so badge updates immediately
        setNotifications((prev) => [notif, ...prev]);

        // Debug log to see what we're receiving
        console.log("Received SSE notification:", notif);
      },
      (err) => {
        console.error("SSE error:", err);
      }
    );

    // Cleanup on unmount
    return () => {
      eventSource.close();
    };
  }, []);

  // Fetch initial notifications once on mount
  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const resp = await tcpService.getNotifications();
        if (mounted && resp.status === "success" && Array.isArray(resp.data)) {
          // sort newest first
          const parseTs = (v: any) => {
            if (!v) return 0;
            if (typeof v === "number") return v;
            const n = Number(v);
            if (!isNaN(n)) return n;
            const p = Date.parse(String(v));
            if (!isNaN(p)) return p;
            return 0;
          };
          const sorted = resp.data
            .slice()
            .sort(
              (a: any, b: any) => parseTs(b.createdAt) - parseTs(a.createdAt)
            );
          setNotifications(sorted);
        }
      } catch (e) {
        console.debug("Failed to load initial notifications:", e);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const dismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <header className="bg-white/80 backdrop-blur-sm shadow-sm border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                NetStream TaskManager
              </h1>
              <p className="text-sm text-slate-600 mt-1">
                Network Programming Project - Java Backend Services
              </p>
            </div>
            <div className="flex items-center gap-4">
              {/* Tab Navigation */}
              <div className="flex gap-2 bg-slate-100 p-1 rounded-xl">
                <button
                  onClick={() => setActiveTab("tasks")}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-all ${
                    activeTab === "tasks"
                      ? "bg-white text-indigo-600 shadow-sm"
                      : "text-slate-600 hover:text-slate-800"
                  }`}
                >
                  <ListTodo className="w-4 h-4" />
                  <span>Tasks</span>
                </button>
                <button
                  onClick={() => setActiveTab("url")}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-all ${
                    activeTab === "url"
                      ? "bg-white text-indigo-600 shadow-sm"
                      : "text-slate-600 hover:text-slate-800"
                  }`}
                >
                  <Globe className="w-4 h-4" />
                  <span>URL Service</span>
                </button>
              </div>

              {/* Notifications Component with Bell Icon */}
              <Notifications
                visible={showNotifications}
                onClose={() => setShowNotifications(false)}
                onToggle={() => setShowNotifications((s) => !s)}
                notifications={notifications}
                onMarkAsRead={(id: string) =>
                  setNotifications((prev) =>
                    prev.map((n) => (n.id === id ? { ...n, read: true } : n))
                  )
                }
                onMarkAllAsRead={() =>
                  setNotifications((prev) =>
                    prev.map((n) => ({ ...n, read: true }))
                  )
                }
                onClearAll={() => setNotifications([])}
                onRefresh={async () => {
                  try {
                    const resp = await tcpService.getNotifications();
                    if (resp.status === "success" && Array.isArray(resp.data)) {
                      setNotifications(
                        resp.data.slice().sort((a: any, b: any) => {
                          const pa =
                            Number(a.createdAt) ||
                            Date.parse(String(a.createdAt)) ||
                            0;
                          const pb =
                            Number(b.createdAt) ||
                            Date.parse(String(b.createdAt)) ||
                            0;
                          return pb - pa;
                        })
                      );
                    }
                  } catch (e) {
                    console.debug("Refresh notifications failed:", e);
                  }
                }}
              />
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        {activeTab === "tasks" ? (
          <div className="max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <TaskList />
          </div>
        ) : (
          <URLServiceDemo />
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white/80 backdrop-blur-sm border-t border-slate-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="text-center text-sm text-slate-500">
            <p className="font-medium text-slate-700">
              NetStream TaskManager - Network Programming Project
            </p>
            <div className="mt-2 flex items-center justify-center gap-4 flex-wrap">
              <span className="flex items-center gap-1.5">
                <span
                  className={`inline-block w-2 h-2 rounded-full ${
                    serverStatus.tcp ? "bg-green-500" : "bg-red-500"
                  }`}
                ></span>
                TCP Server (8080)
              </span>
              <span className="flex items-center gap-1.5">
                <span
                  className={`inline-block w-2 h-2 rounded-full ${
                    serverStatus.gateway ? "bg-green-500" : "bg-red-500"
                  }`}
                ></span>
                HTTP Gateway (3000)
              </span>
              <span className="flex items-center gap-1.5">
                <span
                  className={`inline-block w-2 h-2 rounded-full ${
                    serverStatus.url ? "bg-green-500" : "bg-red-500"
                  }`}
                ></span>
                URL Service (8082)
              </span>
              <span className="flex items-center gap-1.5">
                <span
                  className={`inline-block w-2 h-2 rounded-full ${
                    serverStatus.udp ? "bg-green-500" : "bg-red-500"
                  }`}
                ></span>
                UDP Server (9090)
              </span>
              <span className="flex items-center gap-1.5">
                <span
                  className={`inline-block w-2 h-2 rounded-full ${
                    serverStatus.nio ? "bg-green-500" : "bg-red-500"
                  }`}
                ></span>
                NIO Server (8081)
              </span>
            </div>
          </div>
        </div>
      </footer>

      {/* Toast Notifications */}
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}

export default App;
