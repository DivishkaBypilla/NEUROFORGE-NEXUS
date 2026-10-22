import { useEffect, useState } from "react";
import { api } from "../lib/api";
import GithubMark from "./GithubMark";
import {
  GitPullRequest,
  CircleDot,
  GitCommitHorizontal,
  ExternalLink,
  Check,
  Pencil,
  AlertCircle,
} from "lucide-react";

const TABS = [
  { key: "pulls", label: "Pull Requests", icon: GitPullRequest },
  { key: "issues", label: "Issues", icon: CircleDot },
  { key: "commits", label: "Commits", icon: GitCommitHorizontal },
];

function timeAgo(iso) {
  if (!iso) return "";
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.round(diffMs / 60000);
  if (mins < 60) return `${Math.max(mins, 0)}m ago`;
  const hours = Math.round(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
}

export default function GitHubPanel({ project, canManage, onProjectUpdated }) {
  const [editingRepo, setEditingRepo] = useState(!project.githubRepo);
  const [repoInput, setRepoInput] = useState(project.githubRepo || "");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");

  const [tab, setTab] = useState("pulls");
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    setEditingRepo(!project.githubRepo);
    setRepoInput(project.githubRepo || "");
  }, [project.githubRepo]);

  useEffect(() => {
    if (!project.githubRepo) return;
    let cancelled = false;
    setLoading(true);
    setLoadError("");

    const endpoint =
      tab === "pulls" ? "pulls" : tab === "issues" ? "issues" : "commits";

    api
      .get(`/projects/${project.id}/github/${endpoint}`)
      .then((data) => {
        if (!cancelled) setItems(data || []);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err.message || "Could not load GitHub data.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [project.id, project.githubRepo, tab]);

  async function handleSaveRepo(e) {
    e.preventDefault();
    setSaving(true);
    setSaveError("");
    try {
      const updated = await api.put(`/projects/${project.id}`, {
        githubRepo: repoInput.trim() || null,
      });
      onProjectUpdated(updated);
      setEditingRepo(false);
    } catch (err) {
      setSaveError(err.message || "Could not link that repository.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div
      style={{
        background: "var(--card-bg)",
        backdropFilter: "blur(16px)",
        border: "var(--glass-border)",
        borderRadius: "var(--radius-lg)",
        padding: 28,
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 16,
          flexWrap: "wrap",
          gap: 12,
        }}
      >
        <h3
          style={{
            fontSize: 15,
            fontWeight: 700,
            color: "var(--text-main)",
            display: "flex",
            alignItems: "center",
            gap: 8,
          }}
        >
          <GithubMark size={18} style={{ color: "var(--accent-primary)" }} />
          <span>GitHub</span>
        </h3>

        {!editingRepo && project.githubRepo && (
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <a
              href={`https://github.com/${project.githubRepo}`}
              target="_blank"
              rel="noreferrer"
              className="mono"
              style={{ fontSize: 12.5, color: "var(--text-muted)" }}
            >
              {project.githubRepo}
            </a>
            {canManage && (
              <button
                type="button"
                className="btn btn-sm"
                onClick={() => setEditingRepo(true)}
              >
                <Pencil size={12} />
                <span>Change</span>
              </button>
            )}
          </div>
        )}
      </div>

      {editingRepo ? (
        canManage ? (
          <form onSubmit={handleSaveRepo} style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
            <input
              value={repoInput}
              onChange={(e) => setRepoInput(e.target.value)}
              placeholder="owner/repo, e.g. octocat/hello-world"
              style={{ flex: 1, minWidth: 220 }}
            />
            <button className="btn btn-sm btn-primary" type="submit" disabled={saving}>
              {saving ? <span className="loading-spinner" /> : <Check size={14} />}
              <span>Save</span>
            </button>
            {project.githubRepo && (
              <button
                type="button"
                className="btn btn-sm"
                onClick={() => {
                  setEditingRepo(false);
                  setRepoInput(project.githubRepo);
                  setSaveError("");
                }}
              >
                Cancel
              </button>
            )}
          </form>
        ) : (
          <p className="page-sub" style={{ fontSize: 12.5 }}>
            No GitHub repository linked to this project yet.
          </p>
        )
      ) : null}

      {saveError && (
        <div className="alert alert-error" style={{ marginTop: 12 }}>
          <AlertCircle size={16} flexShrink={0} />
          <span>{saveError}</span>
        </div>
      )}

      {!editingRepo && project.githubRepo && (
        <div style={{ marginTop: 18 }}>
          <div className="view-switcher" style={{ marginBottom: 16 }}>
            {TABS.map((t) => {
              const TabIcon = t.icon;
              return (
                <button
                  key={t.key}
                  type="button"
                  className={`view-switcher-btn ${tab === t.key ? "active" : ""}`}
                  onClick={() => setTab(t.key)}
                >
                  <TabIcon size={14} />
                  <span>{t.label}</span>
                </button>
              );
            })}
          </div>

          {loadError && (
            <div className="alert alert-error">
              <AlertCircle size={16} flexShrink={0} />
              <span>{loadError}</span>
            </div>
          )}

          {loading ? (
            <div className="empty-state" style={{ padding: "32px 12px" }}>
              <span className="loading-spinner" style={{ width: 24, height: 24 }} />
            </div>
          ) : items.length === 0 ? (
            !loadError && (
              <p className="page-sub" style={{ fontSize: 12.5 }}>
                No {tab === "commits" ? "recent commits" : `open ${tab}`} found.
              </p>
            )
          ) : tab === "commits" ? (
            <div className="github-item-list">
              {items.map((c) => (
                <a
                  key={c.sha}
                  href={c.url}
                  target="_blank"
                  rel="noreferrer"
                  className="github-item-row"
                >
                  <span className="mono github-item-sha">{c.sha}</span>
                  <span className="github-item-title">{c.message}</span>
                  <span className="github-item-meta">
                    {c.authorLogin || c.authorName || "unknown"} · {timeAgo(c.date)}
                  </span>
                  <ExternalLink size={13} style={{ color: "var(--text-dim)" }} />
                </a>
              ))}
            </div>
          ) : (
            <div className="github-item-list">
              {items.map((item) => (
                <a
                  key={item.number}
                  href={item.url}
                  target="_blank"
                  rel="noreferrer"
                  className="github-item-row"
                >
                  <span className="mono github-item-sha">#{item.number}</span>
                  <span className="github-item-title">{item.title}</span>
                  <span className="github-item-meta">
                    {item.authorLogin || "unknown"} · {timeAgo(item.updatedAt)}
                  </span>
                  <ExternalLink size={13} style={{ color: "var(--text-dim)" }} />
                </a>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
