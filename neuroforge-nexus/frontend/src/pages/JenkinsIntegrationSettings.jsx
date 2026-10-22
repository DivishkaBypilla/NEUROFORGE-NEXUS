import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, CheckCircle2, AlertCircle, Workflow, Trash2, PlugZap } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { canManagePortfolio } from "../lib/useDirectory";
import { api } from "../lib/api";

export default function JenkinsIntegrationSettings() {
  const { user } = useAuth();
  const canManage = canManagePortfolio(user);
  const [settings, setSettings] = useState(null);
  const [baseUrl, setBaseUrl] = useState("");
  const [username, setUsername] = useState("");
  const [apiToken, setApiToken] = useState("");
  const [defaultJob, setDefaultJob] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!canManage) { setLoading(false); return; }
    api.get("/settings/jenkins").then((data) => {
      setSettings(data); setBaseUrl(data.baseUrl || ""); setUsername(data.username || ""); setDefaultJob(data.defaultJob || "");
    }).catch((e) => setError(e.message || "Could not load Jenkins settings.")).finally(() => setLoading(false));
  }, [canManage]);

  async function save(e) {
    e.preventDefault(); setBusy(true); setError(""); setMessage("");
    try {
      const data = await api.put("/settings/jenkins", { baseUrl, username, apiToken: apiToken || null, defaultJob });
      setSettings(data); setApiToken(""); setMessage("Jenkins settings saved for this organization.");
    } catch (e) { setError(e.message || "Could not save Jenkins settings."); }
    finally { setBusy(false); }
  }
  async function testConnection() {
    setBusy(true); setError(""); setMessage("");
    try { const result = await api.post("/settings/jenkins/test", {}); setMessage(result.message || "Connection successful."); }
    catch (e) { setError(e.message || "Connection failed. Save the settings and verify the Jenkins URL and token."); }
    finally { setBusy(false); }
  }
  async function remove() {
    if (!window.confirm("Remove Jenkins integration for this organization?")) return;
    setBusy(true); setError(""); setMessage("");
    try { await api.del("/settings/jenkins"); setSettings(null); setBaseUrl(""); setUsername(""); setApiToken(""); setDefaultJob(""); setMessage("Jenkins integration removed."); }
    catch (e) { setError(e.message || "Could not remove integration."); }
    finally { setBusy(false); }
  }

  if (!canManage) return <div className="empty-state"><h2>Access restricted</h2><p>Integration settings are available to admins and project managers.</p><Link to="/">Return to projects</Link></div>;
  return <div style={{ maxWidth: 760 }}>
    <div className="page-header"><div><h1 className="page-title"><Workflow size={24} /> Jenkins CI/CD Integration</h1><p className="page-sub">Connect Jenkins to check CI server connectivity. Credentials are stored per organization and never returned to the browser.</p></div></div>
    <div style={{ background: "var(--card-bg)", border: "var(--glass-border)", borderRadius: "var(--radius-lg)", padding: 24 }}>
      {loading ? <div className="empty-state"><span className="loading-spinner" /></div> : <>
        <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 20, color: settings?.configured ? "var(--accent-emerald)" : "var(--text-dim)" }}>
          <span style={{ width: 8, height: 8, borderRadius: "50%", background: settings?.configured ? "var(--accent-emerald)" : "var(--text-dim)" }} />
          {settings?.configured ? "Jenkins settings configured" : "Jenkins is not configured"}
        </div>
        <form onSubmit={save}>
          <div className="field"><label htmlFor="jenkins-url">Jenkins base URL</label><input id="jenkins-url" type="url" required value={baseUrl} onChange={e => setBaseUrl(e.target.value)} placeholder="https://jenkins.example.com" /></div>
          <div className="field"><label htmlFor="jenkins-user">Jenkins username</label><input id="jenkins-user" required value={username} onChange={e => setUsername(e.target.value)} placeholder="jenkins-user" /></div>
          <div className="field"><label htmlFor="jenkins-token">Jenkins API token</label><input id="jenkins-token" type="password" value={apiToken} onChange={e => setApiToken(e.target.value)} placeholder={settings?.hasApiToken ? "Token saved — leave blank to keep it" : "Paste a Jenkins user API token"} /><p className="page-sub" style={{ fontSize: 12, marginTop: 6 }}>Use a Jenkins API token rather than your account password. Keep Jenkins reachable from the backend server.</p></div>
          <div className="field"><label htmlFor="jenkins-job">Default job name (optional)</label><input id="jenkins-job" value={defaultJob} onChange={e => setDefaultJob(e.target.value)} placeholder="team-service-main" /></div>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 10, marginTop: 18 }}><button className="btn btn-primary" type="submit" disabled={busy}>{busy ? "Working…" : "Save settings"}</button><button className="btn" type="button" disabled={busy || !settings?.configured} onClick={testConnection}><PlugZap size={16} /> Test connection</button>{settings?.configured && <button className="btn" type="button" disabled={busy} onClick={remove}><Trash2 size={16} /> Remove</button>}</div>
        </form>
      </>}
      {message && <div className="alert" style={{ marginTop: 16, color: "var(--accent-emerald)" }}><CheckCircle2 size={16} /> {message}</div>}
      {error && <div className="alert alert-error" style={{ marginTop: 16 }}><AlertCircle size={16} /> {error}</div>}
    </div>
    <p className="page-sub" style={{ marginTop: 16 }}><Link to="/settings/github"><ArrowLeft size={14} style={{ verticalAlign: "middle" }} /> Back to GitHub integration</Link></p>
  </div>;
}
