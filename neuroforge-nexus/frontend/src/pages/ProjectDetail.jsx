import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { api } from "../lib/api";
import { useAuth, initialsFor } from "../context/AuthContext";
import {
  useDirectory,
  nameFor,
  assignableUsers,
  canManagePortfolio,
  isUserAssignedToProject,
} from "../lib/useDirectory";
import ProjectForm from "../components/ProjectForm";
import GitHubPanel from "../components/GitHubPanel";
import {
  ArrowLeft,
  KanbanSquare,
  Rocket,
  Edit3,
  Trash2,
  Users,
  UserPlus,
  Calendar,
  Clock,
  ShieldCheck,
  UserCheck,
  AlertCircle,
  X,
} from "lucide-react";

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { users } = useDirectory();
  const [project, setProject] = useState(null);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [newMemberId, setNewMemberId] = useState("");
  const [addingMember, setAddingMember] = useState(false);

  const canManage = canManagePortfolio(user);

  async function load() {
    setError("");
    try {
      const data = await api.get(`/projects/${id}`);
      setProject(data);
    } catch (err) {
      setError(err.message || "Could not load project details.");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleUpdate(values) {
    setSaving(true);
    setError("");
    try {
      const updated = await api.put(`/projects/${id}`, values);
      setProject(updated);
      setEditing(false);
    } catch (err) {
      setError(err.message || "Could not update project.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!confirm(`Delete project "${project.name}"? This action cannot be undone.`))
      return;
    try {
      await api.del(`/projects/${id}`);
      navigate("/");
    } catch (err) {
      setError(err.message || "Could not delete project.");
    }
  }

  async function handleAddMember(e) {
    e.preventDefault();
    if (!newMemberId) return;
    setAddingMember(true);
    setError("");
    try {
      const updated = await api.post(`/projects/${id}/members/${newMemberId}`);
      setProject(updated);
      setNewMemberId("");
    } catch (err) {
      setError(err.message || "Could not add team member.");
    } finally {
      setAddingMember(false);
    }
  }

  async function handleRemoveMember(memberId) {
    try {
      const updated = await api.del(`/projects/${id}/members/${memberId}`);
      setProject(updated);
    } catch (err) {
      setError(err.message || "Could not remove team member.");
    }
  }

  if (error && !project) {
    return (
      <>
        <Link to="/" className="btn btn-sm" style={{ marginBottom: 20 }}>
          <ArrowLeft size={16} />
          <span>Back to Projects</span>
        </Link>
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{error}</span>
        </div>
      </>
    );
  }

  if (!project) {
    return (
      <div className="empty-state">
        <span className="loading-spinner" style={{ width: 32, height: 32 }} />
        <p style={{ marginTop: 12 }}>Loading project specification…</p>
      </div>
    );
  }

  if (!isUserAssignedToProject(user, project)) {
    return (
      <>
        <Link to="/" className="btn btn-sm" style={{ marginBottom: 20, display: "inline-flex" }}>
          <ArrowLeft size={16} />
          <span>Back to Projects</span>
        </Link>
        <div className="alert alert-error" style={{ marginTop: 12 }}>
          <AlertCircle size={18} flexShrink={0} />
          <span>Access Denied: You are not assigned to this project workspace.</span>
        </div>
      </>
    );
  }

  const currentMembers = project.memberIds || [];
  const pickableUsers = assignableUsers(users).filter(
    (u) => !currentMembers.includes(u.id)
  );

  const roster = [];
  if (project.ownerId)
    roster.push({ id: project.ownerId, label: "Owner", removable: false });
  if (project.teamLead && project.teamLead !== project.ownerId) {
    roster.push({ id: project.teamLead, label: "Team Lead", removable: false });
  }
  currentMembers
    .filter((m) => m !== project.ownerId && m !== project.teamLead)
    .forEach((m) => roster.push({ id: m, label: "Member", removable: true }));

  return (
    <>
      <Link
        to="/"
        className="btn btn-sm"
        style={{ marginBottom: 20, display: "inline-flex" }}
      >
        <ArrowLeft size={16} />
        <span>Back to Projects</span>
      </Link>

      <div className="page-header" style={{ marginBottom: 24 }}>
        <div>
          <h1 className="page-title">{project.name}</h1>
          <p className="page-sub">
            {project.description || "No description specified for this workspace."}
          </p>
        </div>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
          <Link className="btn btn-primary" to={`/projects/${id}/board`}>
            <KanbanSquare size={16} />
            <span>Open Kanban Board</span>
          </Link>
          <Link className="btn" to={`/projects/${id}/delivery`}>
            <Rocket size={16} />
            <span>Sprints & DevOps</span>
          </Link>

          {canManage && (
            <>
              <button
                className="btn"
                onClick={() => setEditing((v) => !v)}
              >
                <Edit3 size={16} />
                <span>{editing ? "Cancel Edit" : "Edit Workspace"}</span>
              </button>
              <button className="btn btn-danger" onClick={handleDelete}>
                <Trash2 size={16} />
                <span>Delete</span>
              </button>
            </>
          )}
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{error}</span>
        </div>
      )}

      {editing && canManage ? (
        <ProjectForm
          initial={project}
          onSubmit={handleUpdate}
          submitting={saving}
          submitLabel="Save Changes"
        />
      ) : (
        <div
          style={{
            background: "var(--card-bg)",
            backdropFilter: "blur(16px)",
            border: "var(--glass-border)",
            borderRadius: "var(--radius-lg)",
            padding: 28,
            marginBottom: 32,
          }}
        >
          {/* Metadata Sheet Grid */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
              gap: 20,
              paddingBottom: 24,
              borderBottom: "var(--glass-border)",
              marginBottom: 28,
            }}
          >
            <div>
              <span className="stat-label" style={{ fontSize: 11 }}>
                Status
              </span>
              <div style={{ marginTop: 6 }}>
                <span className={`tag tag-${project.status}`}>
                  {project.status}
                </span>
              </div>
            </div>

            <div>
              <span className="stat-label" style={{ fontSize: 11 }}>
                Priority
              </span>
              <div
                style={{
                  marginTop: 6,
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                <span className={`dot dot-${project.priority}`} />
                <span
                  className="mono"
                  style={{ fontSize: 13, fontWeight: 600, color: "#ffffff" }}
                >
                  {project.priority}
                </span>
              </div>
            </div>

            <div>
              <span className="stat-label" style={{ fontSize: 11 }}>
                Workspace Owner
              </span>
              <div
                style={{
                  marginTop: 6,
                  fontSize: 13.5,
                  fontWeight: 600,
                  color: "var(--text-main)",
                }}
              >
                {nameFor(users, project.ownerId)}
              </div>
            </div>

            <div>
              <span className="stat-label" style={{ fontSize: 11 }}>
                Team Lead
              </span>
              <div
                style={{
                  marginTop: 6,
                  fontSize: 13.5,
                  fontWeight: 600,
                  color: "var(--text-main)",
                }}
              >
                {project.teamLead ? nameFor(users, project.teamLead) : "—"}
              </div>
            </div>

            <div>
              <span className="stat-label" style={{ fontSize: 11 }}>
                Timeline
              </span>
              <div
                className="mono"
                style={{
                  marginTop: 6,
                  fontSize: 12.5,
                  color: "var(--text-muted)",
                }}
              >
                {project.startDate || "—"} → {project.endDate || "—"}
              </div>
            </div>
          </div>

          {/* Team Roster Section */}
          <div>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                marginBottom: 16,
              }}
            >
              <h3
                style={{
                  fontSize: 15,
                  fontWeight: 700,
                  color: "#ffffff",
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                <Users size={18} style={{ color: "var(--accent-primary)" }} />
                <span>Assigned Project Team ({roster.length})</span>
              </h3>
            </div>

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))",
                gap: 14,
                marginBottom: canManage ? 20 : 0,
              }}
            >
              {roster.map((r) => {
                const person = users.find((u) => u.id === r.id);
                return (
                  <div
                    key={r.id}
                    style={{
                      background: "rgba(11, 14, 20, 0.6)",
                      border: "1px solid rgba(255, 255, 255, 0.08)",
                      borderRadius: "var(--radius-md)",
                      padding: 14,
                      display: "flex",
                      alignItems: "center",
                      gap: 12,
                    }}
                  >
                    <span className="avatar">
                      {initialsFor(person?.name || r.id)}
                    </span>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div
                        style={{
                          fontSize: 13.5,
                          fontWeight: 600,
                          color: "var(--text-main)",
                          whiteSpace: "nowrap",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                        }}
                      >
                        {person ? person.name : r.id}
                      </div>
                      <div
                        style={{
                          fontSize: 11.5,
                          color: "var(--text-dim)",
                          whiteSpace: "nowrap",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                        }}
                      >
                        {person?.email || "No email available"}
                      </div>
                    </div>
                    <div
                      style={{
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "flex-end",
                        gap: 4,
                      }}
                    >
                      <span className="tag" style={{ fontSize: 10 }}>
                        {r.label}
                      </span>
                      {canManage && r.removable && (
                        <button
                          type="button"
                          onClick={() => handleRemoveMember(r.id)}
                          style={{
                            fontSize: 11,
                            color: "var(--accent-rose)",
                            cursor: "pointer",
                          }}
                        >
                          Remove
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Add Member Form */}
            {canManage && (
              <form
                onSubmit={handleAddMember}
                style={{
                  display: "flex",
                  gap: 10,
                  maxWidth: 420,
                  marginTop: 16,
                  paddingTop: 16,
                  borderTop: "var(--glass-border)",
                }}
              >
                <select
                  value={newMemberId}
                  onChange={(e) => setNewMemberId(e.target.value)}
                  style={{ flex: 1 }}
                >
                  <option value="">+ Add team member to roster…</option>
                  {pickableUsers.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.name} · {u.role}
                    </option>
                  ))}
                </select>
                <button
                  className="btn btn-sm btn-primary"
                  type="submit"
                  disabled={!newMemberId || addingMember}
                >
                  {addingMember ? (
                    <span className="loading-spinner" />
                  ) : (
                    <>
                      <UserPlus size={14} />
                      <span>Add</span>
                    </>
                  )}
                </button>
              </form>
            )}
          </div>
        </div>
      )}

      <div style={{ marginTop: 24 }}>
        <GitHubPanel
          project={project}
          canManage={canManage}
          onProjectUpdated={setProject}
        />
      </div>
    </>
  );
}
