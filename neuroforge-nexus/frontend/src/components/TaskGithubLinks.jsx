import { useEffect, useState } from "react";
import { api } from "../lib/api";
import GithubMark from "./GithubMark";
import { ExternalLink, Copy, Check, AlertCircle } from "lucide-react";

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

export default function TaskGithubLinks({ task, project }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const taskRef = task.taskId ? `TASK-${task.taskId.replace(/-/g, "").slice(0, 8)}` : null;

  useEffect(() => {
    if (!project?.githubRepo) return;
    let cancelled = false;
    setLoading(true);
    setError("");

    api
      .get(`/tasks/${task.id}/github`)
      .then((data) => {
        if (!cancelled) setItems(data || []);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || "Could not load linked GitHub activity.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [task.id, project?.githubRepo]);

  if (!project?.githubRepo) return null;

  function copyRef() {
    if (!taskRef) return;
    navigator.clipboard?.writeText(taskRef);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  }

  return (
    <div style={{ marginTop: 24, paddingTop: 20, borderTop: "var(--glass-border)" }}>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 10,
          flexWrap: "wrap",
          gap: 8,
        }}
      >
        <h3
          style={{
            fontSize: 14,
            fontWeight: 700,
            color: "var(--text-main)",
            display: "flex",
            alignItems: "center",
            gap: 8,
          }}
        >
          <GithubMark size={16} style={{ color: "var(--accent-primary)" }} />
          <span>Linked GitHub Activity</span>
        </h3>

        {taskRef && (
          <button
            type="button"
            className="btn btn-sm"
            onClick={copyRef}
            title="Reference this task in a PR/issue title or branch name to link it here"
          >
            {copied ? <Check size={12} /> : <Copy size={12} />}
            <span className="mono">{taskRef}</span>
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} flexShrink={0} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <span className="loading-spinner" style={{ width: 20, height: 20 }} />
      ) : items.length === 0 ? (
        !error && (
          <p className="page-sub" style={{ fontSize: 12.5 }}>
            No PRs or issues reference {taskRef} yet. Include that tag in a PR
            title, description, or branch name to link it here.
          </p>
        )
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
              <span className={`github-item-state github-item-state-${item.state}`}>
                {item.state}
              </span>
              <span className="github-item-meta">{timeAgo(item.updatedAt)}</span>
              <ExternalLink size={13} style={{ color: "var(--text-dim)" }} />
            </a>
          ))}
        </div>
      )}
    </div>
  );
}
