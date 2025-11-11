import { useEffect, useState } from "react";
import { getMetrics } from "../services/monitorService";

export default function NetworkDiagnostics() {
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState<Record<string, number[]>>({});

  useEffect(() => {
    let mounted = true;
    const poll = async () => {
      setLoading(true);
      const res = await getMetrics();
      if (!mounted) return;
      if (res && res.status === "success") {
        const data = res.data;
        setMetrics(data);
        // accumulate latency history per service key
        const keys = Object.keys(data).filter((k) => !["timestamp", "counters"].includes(k));
        setHistory((prev) => {
          const next: Record<string, number[]> = { ...prev };
          keys.forEach((k) => {
            const v = data[k];
            const lat = v && typeof v.latencyMs === "number" ? v.latencyMs : 0;
            const arr = (next[k] || []).slice(-19); // keep last 19, add 1 => 20
            arr.push(lat);
            next[k] = arr;
          });
          return next;
        });
      }
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
  const counters = data.counters || {};

  return (
    <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
      <h3 className="text-lg font-semibold mb-4">Network Diagnostics</h3>
      <div className="grid grid-cols-1 gap-3">
        {Object.keys(data)
          .filter((k) => k !== "timestamp" && k !== "counters")
          .map((key) => {
            const v = data[key];
            const hist = history[key] || [];
            const max = Math.max(1, ...hist);
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
                  {/* tiny latency sparkline */}
                  <div className="mt-2 h-6 flex items-end gap-0.5">
                    {hist.map((val, i) => (
                      <span
                        key={i}
                        className="inline-block w-1 bg-indigo-400"
                        style={{ height: `${Math.max(2, (val / max) * 24)}px` }}
                        title={val + " ms"}
                      />
                    ))}
                  </div>
                </div>
              </div>
            );
          })}
      </div>
      <div className="mt-4 text-xs text-slate-500">Last: {new Date(data.timestamp || Date.now()).toLocaleTimeString()}</div>

      {/* Protocol counters */}
      <div className="mt-6">
        <h4 className="text-sm font-semibold text-slate-700 mb-2">Protocol Counters</h4>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <CounterCard title="TCP" data={counters.tcp} fields={["connections","active","requests","bytesIn","bytesOut"]} />
          <CounterCard title="HTTP" data={counters.http} fields={["connections","active","requests","bytesIn","bytesOut"]} />
          <CounterCard title="UDP" data={counters.udp} fields={["packetsIn","packetsOut","bytesIn","bytesOut"]} />
        </div>
      </div>
    </div>
  );
}

function CounterCard({ title, data, fields }: { title: string; data: any; fields: string[] }) {
  return (
    <div className="p-3 bg-slate-50 rounded-md border border-slate-100">
      <div className="font-medium text-slate-800 mb-2">{title}</div>
      {data ? (
        <div className="grid grid-cols-2 gap-1 text-xs text-slate-700">
          {fields.map((f) => (
            <div key={f} className="flex justify-between">
              <span className="text-slate-500">{f}</span>
              <span>{formatNumber(data[f])}</span>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-xs text-slate-500">n/a</div>
      )}
    </div>
  );
}

function formatNumber(n: any) {
  const v = typeof n === "number" ? n : Number(n ?? 0);
  return v.toLocaleString();
}
