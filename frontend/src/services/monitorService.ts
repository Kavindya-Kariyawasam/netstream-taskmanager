export async function getMetrics(): Promise<any> {
  try {
    const resp = await fetch("http://localhost:4000/metrics", { method: "GET" });
    if (!resp.ok) throw new Error(`Status ${resp.status}`);
    const j = await resp.json();
    return j;
  } catch (e) {
    return { status: "error", message: (e instanceof Error) ? e.message : String(e) };
  }
}
