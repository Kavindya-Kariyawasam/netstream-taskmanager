import type { Notification } from "@/types";

class UDPService {
  private listeners: Array<(notification: Notification) => void> = [];
  private pollingInterval: number | null = null;
  private notificationCache: Set<string> = new Set();

  subscribe(callback: (notification: Notification) => void) {
    this.listeners.push(callback);

    if (!this.pollingInterval) {
      this.startPolling();
    }

    return () => {
      this.listeners = this.listeners.filter((cb) => cb !== callback);
      if (this.listeners.length === 0) {
        this.stopPolling();
      }
    };
  }

  private startPolling() {
    this.pollingInterval = window.setInterval(async () => {
      try {
        const response = await fetch("http://localhost:3000", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ action: "GET_NOTIFICATIONS" }),
        });

        const data = await response.json();

        if (data.status === "success" && Array.isArray(data.data)) {
          data.data.forEach((notifStr: string) => {
            if (!this.notificationCache.has(notifStr)) {
              this.notificationCache.add(notifStr);
              const notification = this.parseNotification(notifStr);
              if (notification) {
                this.listeners.forEach((listener) => listener(notification));
              }
            }
          });

          if (this.notificationCache.size > 100) {
            const arr = Array.from(this.notificationCache);
            this.notificationCache = new Set(arr.slice(-50));
          }
        }
      } catch (error) {
        console.error("Failed to fetch notifications:", error);
      }
    }, 2000);
  }

  private stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
  }

  private parseNotification(notifStr: string): Notification | null {
    try {
      const parts = notifStr.split("|");
      if (parts.length < 4) return null;

      const [type, taskId, message, timestamp] = parts;

      return {
        id: `notif_${timestamp}_${Math.random()}`,
        type: type as any,
        taskId,
        message,
        timestamp: parseInt(timestamp),
        read: false,
      };
    } catch (error) {
      console.error("Failed to parse notification:", error);
      return null;
    }
  }
}

export const udpService = new UDPService();
