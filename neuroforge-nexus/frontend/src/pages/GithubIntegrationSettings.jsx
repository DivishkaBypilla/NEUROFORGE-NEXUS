import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { canManagePortfolio } from "../lib/useDirectory";
import { api } from "../lib/api";
import { githubRedirectUri } from "../lib/github";
import GithubMark from "../components/GithubMark";
import {
  ShieldAlert,
  ArrowLeft,
  Check,
  AlertCircle,
  Trash2,
  ExternalLink,
} from "lucide-react";

export default function GithubIntegrationSettings() {
  const { user } = useAuth();
  const canManage = canManagePortfolio(user);

  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [clientId, setClientId] = useState("");
  const [clientSecret, setClientSecret] = useState("");
  const [apiToken, setApiToken] = useState("");
  const [redirectUri, setRedirectUri] = useState("");

  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saved, setSaved] = useState(false);

  const [removing, setRemoving] = useState(false);
  const [confirmingRemove, setConfirmingRemove] = useState(false);

  useEffect(() => {
    if (!canManage) return;
    let cancelled = false;

    api
      .get("/settings/github")
      .then((data) => {
        if (cancelled) return;
        setSettings(data);
        setClientId(data.clientId || "");
        setRedirectUri(data.redirectUri || githubRedirectUri());
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err.message || "Could not load GitHub settings.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!canManage) {
    return (
      <div className="empty-state" style={{ minHeight: "60vh", justifyContent: "center" }}>
        <div
          className="empty-icon"
          style={{ background: "rgba(244, 63, 94, 0.12)", color: "var(--accent-rose)" }}
        >
          <ShieldAlert size={32} />
        </div>
        <h2 style={{ fontSize: 20, fontWeight: 700, color: "var(--text-main)", marginTop: 16 }}>
          Access Restricted: Integration Settings
        </h2>
        <p className="page-sub" style={{ maxWidth: 480, marginTop: 8 }}>
          Setting up integrations is restricted to Admins and Project Managers.
        </p>
        <Link to="/" className="btn btn-primary" style={{ marginTop: 20 }}>
          <ArrowLeft size={16} />
          <span>Return to Workspace Projects</span>
        </Link>
      </div>
    );
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setSaveError("");
    setSaved(false);
    try {
      const updated = await api.put("/settings/github", {
        clientId: clientId.trim() || null,
        clientSecret: clientSecret.trim() || null,
        apiToken: apiToken.trim() || null,
        redirectUri: redirectUri.trim() || null,
      });
      setSettings(updated);
      setClientSecret("");
      setApiToken("");
      setSaved(true);
      setTimeout(() => setSaved(false), 2500);
    } catch (err) {
      setSaveError(err.message || "Could not save GitHub settings.");
    } finally {
      setSaving(false);
    }
  }

  async function handleRemove() {
    if (!confirmingRemove) {
      setConfirmingRemove(true);
      return;
    }
    setRemoving(true);
    try {
      await api.del("/settings/github");
      setSettings(null);
      setClientId("");
      setClientSecret("");
      setApiToken("");
      setConfirmingRemove(false);
    } catch (err) {
      setSaveError(err.message || "Could not remove the GitHub integration.");
    } finally {
      setRemoving(false);
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <GithubMark size={24} style={{ color: "var(--accent-primary)" }} />
            <span>GitHub Integration</span>
          </h1>
          <p className="page-sub">
            Connect a GitHub OAuth App so people can sign in with GitHub and
            projects can link a repository. Anyone with this page open can
            change it — no redeploy or restart needed.
          </p>
        </div>
      </div>

      {loading ? (
        <div className="empty-state" style={{ padding: "48px 12px" }}>
          <span className="loading-spinner" style={{ width: 26, height: 26 }} />
        </div>
      ) : loadError ? (
        <div className="alert alert-error">
          <AlertCircle size={16} flexShrink={0} />
          <span>{loadError}</span>
        </div>
      ) : (
        <div
          style={{
            background: "var(--card-bg)",
            backdropFilter: "blur(16px)",
            border: "var(--glass-border)",
            borderRadius: "var(--radius-lg)",
            padding: 28,
            maxWidth: 640,
          }}
        >
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              marginBottom: 20,
              fontSize: 13,
              fontWeight: 600,
              color: settings?.configured ? "var(--accent-emerald)" : "var(--text-dim)",
            }}
          >
            <span
              style={{
                width: 8,
                height: 8,
                borderRadius: "50%",
                background: settings?.configured ? "var(--accent-emerald)" : "var(--text-dim)",
              }}
            />
            <span>
              {settings?.configured
                ? "GitHub sign-in is active"
                : "GitHub sign-in is not set up yet"}
            </span>
            {settings?.updatedAt && (
              <span style={{ color: "var(--text-dim)", fontWeight: 400 }}>
                · last changed by {settings.updatedBy || "someone"} on{" "}
                {new Date(settings.updatedAt).toLocaleDateString()}
              </span>
            )}
          </div>

          <form onSubmit={handleSave}>
            <div className="field">
              <label htmlFor="clientId">Client ID</label>
              <input
                id="clientId"
                value={clientId}
                onChange={(e) => setClientId(e.target.value)}
                placeholder="Iv1.xxxxxxxxxxxxxxxx"
              />
            </div>

            <div className="field">
              <label htmlFor="clientSecret">Client Secret</label>
              <input
                id="clientSecret"
                type="password"
                value={clientSecret}
                onChange={(e) => setClientSecret(e.target.value)}
                placeholder={
                  settings?.hasClientSecret
                    ? "•••••••••••••••• (already set — leave blank to keep it)"
                    : "Paste the client secret from GitHub"
                }
              />
            </div>

            <div className="field">
              <label htmlFor="redirectUri">Authorization callback URL</label>
              <input
                id="redirectUri"
                value={redirectUri}
                onChange={(e) => setRedirectUri(e.target.value)}
                placeholder={githubRedirectUri()}
              />
              <p className="page-sub" style={{ fontSize: 12, marginTop: 6 }}>
                Must exactly match the "Authorization callback URL" set on the
                GitHub OAuth App.
              </p>
            </div>

            <div className="field">
              <label htmlFor="apiToken">API token (optional)</label>
              <input
                id="apiToken"
                type="password"
                value={apiToken}
                onChange={(e) => setApiToken(e.target.value)}
                placeholder={
                  settings?.hasApiToken
                    ? "•••••••••••••••• (already set — leave blank to keep it)"
                    : "Personal access token, for private repos or higher rate limits"
                }
              />
            </div>

            {saveError && (
              <div className="alert alert-error" style={{ marginBottom: 16 }}>
                <AlertCircle size={16} flexShrink={0} />
                <span>{saveError}</span>
              </div>
            )}

            <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
              <button className="btn btn-primary" type="submit" disabled={saving}>
                {saving ? (
                  <span className="loading-spinner" />
                ) : saved ? (
                  <Check size={16} />
                ) : null}
                <span>{saved ? "Saved" : "Save"}</span>
              </button>

              {settings?.configured && (
                <button
                  type="button"
                  className="btn"
                  onClick={handleRemove}
                  disabled={removing}
                  style={
                    confirmingRemove
                      ? {
                          background: "rgba(244, 63, 94, 0.15)",
                          color: "var(--accent-rose)",
                          border: "1px solid rgba(244, 63, 94, 0.3)",
                        }
                      : undefined
                  }
                >
                  <Trash2 size={14} />
                  <span>{confirmingRemove ? "Click again to confirm" : "Remove integration"}</span>
                </button>
              )}
            </div>
          </form>

          <div
            style={{
              marginTop: 24,
              paddingTop: 20,
              borderTop: "var(--glass-border)",
              fontSize: 12.5,
              color: "var(--text-muted)",
            }}
          >
            <p style={{ marginBottom: 8 }}>
              Don't have an OAuth App yet? Create one on GitHub, then paste its
              Client ID and Secret above.
            </p>
            <a
              href="https://github.com/settings/developers"
              target="_blank"
              rel="noreferrer"
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: 6,
                color: "var(--accent-primary)",
                fontWeight: 600,
              }}
            >
              <span>GitHub → Settings → Developer settings → OAuth Apps</span>
              <ExternalLink size={12} />
            </a>
          </div>
        </div>
      )}
    </>
  );
}
