import { useState } from "react";
import { useDirectory, assignableUsers } from "../lib/useDirectory";
import GithubMark from "./GithubMark";
import {
  FolderPlus,
  AlignLeft,
  Activity,
  AlertTriangle,
  UserCheck,
  Calendar,
  Users,
  X,
  Check,
} from "lucide-react";

const STATUSES = ["PLANNED", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "ARCHIVED"];
const PRIORITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

export default function ProjectForm({
  initial = {},
  onSubmit,
  submitting,
  submitLabel,
  showMembers = false,
}) {
  const { users: allUsers, loading: usersLoading } = useDirectory();
  const users = assignableUsers(allUsers); // exclude admins from team lead / member pickers

  const [name, setName] = useState(initial.name || "");
  const [description, setDescription] = useState(initial.description || "");
  const [status, setStatus] = useState(initial.status || "PLANNED");
  const [priority, setPriority] = useState(initial.priority || "MEDIUM");
  const [teamLead, setTeamLead] = useState(initial.teamLead || "");
  const [startDate, setStartDate] = useState(initial.startDate || "");
  const [endDate, setEndDate] = useState(initial.endDate || "");
  const [memberIds, setMemberIds] = useState(initial.memberIds || []);
  const [githubRepo, setGithubRepo] = useState(initial.githubRepo || "");

  function toggleMember(id) {
    setMemberIds((prev) =>
      prev.includes(id) ? prev.filter((m) => m !== id) : [...prev, id]
    );
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      name,
      description: description || null,
      status,
      priority,
      teamLead: teamLead || null,
      startDate: startDate || null,
      endDate: endDate || null,
      githubRepo: githubRepo.trim() || null,
      ...(showMembers ? { memberIds } : {}),
    });
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        background: "var(--panel-bg-solid)",
        border: "var(--glass-border)",
        borderRadius: "var(--radius-lg)",
        padding: 24,
        marginBottom: 32,
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 10,
          marginBottom: 20,
          paddingBottom: 12,
          borderBottom: "var(--glass-border)",
        }}
      >
        <FolderPlus size={20} style={{ color: "var(--accent-primary)" }} />
        <h3 style={{ fontSize: 16, fontWeight: 700, color: "var(--text-main)" }}>
          {initial.id ? "Edit Project Configuration" : "Configure New Project"}
        </h3>
      </div>

      <div className="field">
        <label htmlFor="name">Project Name *</label>
        <input
          id="name"
          placeholder="e.g. NeuroForge Core Engine v2"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label htmlFor="description">Project Description</label>
        <textarea
          id="description"
          placeholder="Summary of project deliverables, objectives, and tech scope…"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </div>

      <div className="field-row">
        <div className="field">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value)}
          >
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="field">
        <label htmlFor="teamLead">Project Lead / Manager</label>
        <select
          id="teamLead"
          value={teamLead}
          onChange={(e) => setTeamLead(e.target.value)}
          disabled={usersLoading}
        >
          <option value="">— Unassigned —</option>
          {users.map((u) => (
            <option key={u.id} value={u.id}>
              {u.name} ({u.role})
            </option>
          ))}
        </select>
      </div>

      <div className="field-row">
        <div className="field">
          <label htmlFor="startDate">Start Date</label>
          <input
            id="startDate"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="endDate">Target Completion Date</label>
          <input
            id="endDate"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
          />
        </div>
      </div>

      <div className="field">
        <label htmlFor="githubRepo">GitHub Repository</label>
        <div style={{ position: "relative" }}>
          <input
            id="githubRepo"
            placeholder="owner/repo, e.g. octocat/hello-world"
            value={githubRepo}
            onChange={(e) => setGithubRepo(e.target.value)}
            style={{ paddingLeft: 40 }}
          />
          <GithubMark
            size={16}
            style={{
              position: "absolute",
              left: 12,
              top: 13,
              color: "var(--text-dim)",
            }}
          />
        </div>
      </div>

      {showMembers && (
        <div className="field">
          <label>Assign Team Members</label>

          {memberIds.length > 0 && (
            <div
              style={{
                display: "flex",
                flexWrap: "wrap",
                gap: 8,
                marginBottom: 12,
              }}
            >
              {memberIds.map((id) => {
                const u = users.find((x) => x.id === id);
                return (
                  <span
                    key={id}
                    className="tag"
                    style={{
                      background: "rgba(99, 102, 241, 0.12)",
                      borderColor: "rgba(99, 102, 241, 0.3)",
                      color: "#a5b4fc",
                      padding: "5px 10px",
                    }}
                  >
                    <span>{u ? u.name : id}</span>
                    <button
                      type="button"
                      onClick={() => toggleMember(id)}
                      style={{
                        marginLeft: 4,
                        color: "var(--text-muted)",
                        display: "inline-flex",
                      }}
                      aria-label={`Remove ${u ? u.name : id}`}
                    >
                      <X size={14} />
                    </button>
                  </span>
                );
              })}
            </div>
          )}

          <select
            value=""
            disabled={usersLoading}
            onChange={(e) => {
              if (e.target.value) toggleMember(e.target.value);
            }}
          >
            <option value="">
              {usersLoading ? "Loading team…" : "+ Add member to project team…"}
            </option>
            {users
              .filter((u) => !memberIds.includes(u.id))
              .map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} · {u.role}
                </option>
              ))}
          </select>
        </div>
      )}

      <div style={{ display: "flex", justifyContent: "flex-end", gap: 12, marginTop: 12 }}>
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? (
            <span className="loading-spinner" />
          ) : (
            <>
              <Check size={16} />
              <span>{submitLabel}</span>
            </>
          )}
        </button>
      </div>
    </form>
  );
}
