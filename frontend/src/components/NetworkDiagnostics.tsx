import { useEffect, useState } from "react";
import { getMetrics } from "../services/monitorService";

export default function NetworkDiagnostics() {
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let mounted = true;
    const poll = async () => {
      setLoading(true);
      const res = await getMetrics();
      if (!mounted) return;
      if (res && res.status === "success") setMetrics(res.data);
      else setMetrics({ error: res.message || "No data" });
      setLoading(false);
    };

    poll();
    const iv = setInterval(poll, 5000);
    return () => {
      mounted = false;
      clearInterval(iv);
    };
  }, []);

  if (loading && !metrics) {
    return (
      <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200 text-center">
        <p className="text-sm text-slate-500">Loading network diagnostics...</p>
      </div>
    );
  }

  if (!metrics) {
    return (
      <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200 text-center">
        <p className="text-sm text-rose-500">No metrics available</p>
      </div>
    );
  }

  if (metrics.error) {
    return (
      <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
        <p className="text-sm text-rose-500">Error: {metrics.error}</p>
      </div>
    );
  }

  const data = metrics;

  return (
    <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
      <h3 className="text-lg font-semibold mb-4">Network Diagnostics</h3>
      <div className="grid grid-cols-1 gap-3">
        {Object.keys(data)
          .filter((k) => k !== "timestamp")
          .map((key) => {
            const v = data[key];
            return (
              <div
                key={key}
                className="flex items-center justify-between p-3 bg-slate-50 rounded-md border border-slate-100"
              >
                <div>
                  <div className="font-medium text-slate-800">{key}</div>
                  <div className="text-xs text-slate-500">{v && v.ok ? "reachable" : "unreachable"}</div>
                </div>
                <div className="text-right text-sm text-slate-700">
                  <div>latency: {v && v.latencyMs != null ? `${v.latencyMs} ms` : "n/a"}</div>
                  {v && v.statusCode && <div>status: {v.statusCode}</div>}
                </div>
              </div>
            );
          })}
      </div>
      <div className="mt-4 text-xs text-slate-500">Last: {new Date(data.timestamp || Date.now()).toLocaleTimeString()}</div>
    </div>
  );
}
