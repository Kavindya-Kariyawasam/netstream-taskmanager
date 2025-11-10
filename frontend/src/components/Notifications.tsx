import { useEffect, useState } from "react";
import { tcpService } from "@/services/tcpService";
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

export default function Notifications({ visible, onClose }: { visible: boolean; onClose: () => void; }) {
    const [notifications, setNotifications] = useState<NotificationType[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!visible) return;
        fetchNotifications();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [visible]);

    const fetchNotifications = async () => {
        setLoading(true);
        setError(null);
        try {
            const resp = await tcpService.getNotifications();
            if (resp.status === "success" && resp.data) {
                // sort newest first by createdAt (support string or number timestamps)
                const parseTs = (v: string | number | undefined) => {
                    if (v === undefined || v === null) return 0;
                    if (typeof v === "number") return v;
                    const num = Number(v);
                    if (!isNaN(num)) return num;
                    const p = Date.parse(String(v));
                    if (!isNaN(p)) return p;
                    return 0;
                };

                const sorted = resp.data.slice().sort((a, b) => parseTs(b.createdAt) - parseTs(a.createdAt));
                setNotifications(sorted);
            } else {
                setError(resp.message || "Failed to load notifications");
            }
        } catch (err: any) {
            setError(err.message || "Network error");
        } finally {
            setLoading(false);
        }
    };

    if (!visible) return null;

    return (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-slate-200 z-50">
            <div className="p-3 border-b border-slate-100 flex items-center justify-between">
                <h3 className="text-sm font-medium text-slate-700">Notifications</h3>
                <button onClick={onClose} className="text-slate-500 hover:text-slate-800">Close</button>
            </div>

            <div className="max-h-64 overflow-auto p-3">
                {loading && <p className="text-sm text-slate-500">Loading...</p>}
                {error && (
                    <div className="text-sm text-rose-600">{error}</div>
                )}

                {!loading && !error && notifications.length === 0 && (
                    <p className="text-sm text-slate-500">No notifications</p>
                )}

                {!loading && !error && notifications.map((n, i) => (
                    // use stable key when possible, fallback to index-based composite key
                    <div key={n.id ?? `${n.title || "notif"}-${i}-${n.createdAt ?? ""}`} className="py-2 border-b last:border-b-0">
                        <div className="text-sm font-medium text-slate-800">{n.title}</div>
                        {n.body && <div className="text-xs text-slate-500 mt-1">{n.body}</div>}
                        <div className="text-xs text-slate-400 mt-1">{formatCreatedAt(n.createdAt)}</div>
                    </div>
                ))}
            </div>

            <div className="p-3 border-t border-slate-100 text-center">
                <button onClick={fetchNotifications} className="text-sm text-indigo-600 hover:underline">Refresh</button>
            </div>
        </div>
    );
}
