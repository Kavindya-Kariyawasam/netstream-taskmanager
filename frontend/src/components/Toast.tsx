import { useEffect, useState } from "react";
import { X } from "lucide-react";

export interface ToastMessage {
    id: string;
    title: string;
    body?: string;
    type?: "info" | "success" | "warning" | "error";
    duration?: number; // ms, default 5000
}

interface ToastProps {
    toast: ToastMessage;
    onDismiss: (id: string) => void;
}

function Toast({ toast, onDismiss }: ToastProps) {
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        const duration = toast.duration ?? 5000;
        if (duration > 0) {
            const timer = setTimeout(() => {
                setIsExiting(true);
                setTimeout(() => onDismiss(toast.id), 300); // wait for exit animation
            }, duration);
            return () => clearTimeout(timer);
        }
    }, [toast, onDismiss]);

    const typeColors = {
        info: "bg-blue-600 border-blue-700 text-white",
        success: "bg-green-600 border-green-700 text-white",
        warning: "bg-amber-600 border-amber-700 text-white",
        error: "bg-rose-600 border-rose-700 text-white",
    };

    const colorClass = typeColors[toast.type ?? "info"];

    return (
        <div
            className={`${colorClass} border rounded-lg shadow-lg p-4 min-w-80 max-w-sm transition-all duration-300 ${isExiting ? "opacity-0 translate-x-8" : "opacity-100 translate-x-0"
                }`}
        >
            <div className="flex items-start justify-between">
                <div className="flex-1">
                    <p className="font-bold text-sm uppercase tracking-wide">{toast.title}</p>
                    {toast.body && (
                        <div className="text-sm mt-2 space-y-1">
                            {toast.body.split('\n').map((line, idx) => (
                                <p key={idx} className="font-medium">{line}</p>
                            ))}
                        </div>
                    )}
                </div>
                <button
                    onClick={() => {
                        setIsExiting(true);
                        setTimeout(() => onDismiss(toast.id), 300);
                    }}
                    className="ml-3 text-white opacity-80 hover:opacity-100 transition-opacity"
                >
                    <X className="w-4 h-4" />
                </button>
            </div>
        </div>
    );
}

export default function ToastContainer({ toasts, onDismiss }: { toasts: ToastMessage[]; onDismiss: (id: string) => void }) {
    return (
        <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 pointer-events-none">
            {toasts.map((t) => (
                <div key={t.id} className="pointer-events-auto">
                    <Toast toast={t} onDismiss={onDismiss} />
                </div>
            ))}
        </div>
    );
}
