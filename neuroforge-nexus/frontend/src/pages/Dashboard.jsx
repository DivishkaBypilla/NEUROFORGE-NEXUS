import { useEffect, useState, useMemo } from "react";
import { Link } from "react-router-dom";
import { api } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { useDirectory, nameFor, canManagePortfolio, isUserAssignedToProject } from "../lib/useDirectory";
import ProjectForm from "../components/ProjectForm";
import {
  FolderKanban,
  Plus,
  Search,
  Filter,
  Users,
  Activity,
  ShieldAlert,
  Sparkles,
  ArrowUpRight,
  KanbanSquare,
  AlertCircle,
  FolderOpen,
  Calendar,
} from "lucide-react";

export default function Dashboard() {
  const { user } = useAuth();
  const { users } = useDirectory();
  const [projects, setProjects] = useState(null);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [creating, setCreating] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedStatusFilter, setSelectedStatusFilter] = useState("ALL");

  const canManage = canManagePortfolio(user);

  async function load() {
    setError("");
    try {
      const data = await api.get("/projects");
      setProjects(data || []);
    } catch (err) {
      setError(err.message || "Could not load project portfolio.");
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(values) {
    setCreating(true);
    setError("");
    try {
      await api.post("/projects", { ...values, ownerId: user?.id });
      setShowForm(false);
      await load();
    } catch (err) {
      setError(err.message || "Could not create project.");
    } finally {
      setCreating(false);
    }
  }

  // Filter projects assigned to the current user (Admins see all projects)
  const userAssignedProjects = useMemo(() => {
    if (!projects) return [];
    return projects.filter((p) => isUserAssignedToProject(user, p));
  }, [projects, user]);

  // Filter assigned projects by Search Query & Selected Status Filter
  const filteredProjects = useMemo(() => {
    return userAssignedProjects.filter((p) => {
      const matchesSearch =
        p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (p.description &&
          p.description.toLowerCase().includes(searchQuery.toLowerCase()));
      const matchesStatus =
        selectedStatusFilter === "ALL" || p.status === selectedStatusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [userAssignedProjects, searchQuery, selectedStatusFilter]);

  // Statistics calculation based on assigned projects
  const stats = useMemo(() => {
    const assignedMemberIds = new Set();
    userAssignedProjects.forEach((p) => {
      if (p.ownerId) assignedMemberIds.add(p.ownerId);
      if (p.teamLead) assignedMemberIds.add(p.teamLead);
      if (Array.isArray(p.memberIds)) {
        p.memberIds.forEach((m) => assignedMemberIds.add(m));
      }
      if (Array.isArray(p.members)) {
        p.members.forEach((m) => {
          if (typeof m === "string") assignedMemberIds.add(m);
          else if (m?.id) assignedMemberIds.add(m.id);
        });
      }
    });

    return {
      total: userAssignedProjects.length,
      active: userAssignedProjects.filter((p) => p.status === "IN_PROGRESS").length,
      critical: userAssignedProjects.filter(
        (p) => p.priority === "HIGH" || p.priority === "CRITICAL"
      ).length,
      team: canManage ? users.length : assignedMemberIds.size,
    };
  }, [userAssignedProjects, users, canManage]);

  return (
    <>
      {/* Page Header Banner */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <FolderKanban size={26} style={{ color: "var(--accent-primary)" }} />
            <span>Projects & Engineering Workspaces</span>
          </h1>
          <p className="page-sub">
            {canManage
              ? "Oversee, configure, and monitor software delivery across your organization."
              : "Projects and active engineering deliverables assigned to you."}
          </p>
        </div>

        {canManage && (
          <button
            className="btn btn-primary"
            onClick={() => setShowForm((v) => !v)}
          >
            <Plus size={18} />
            <span>{showForm ? "Cancel Creation" : "Create New Project"}</span>
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{error}</span>
        </div>
      )}

      {/* Enterprise Dashboard Stat Banner */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">Total Projects</span>
            <span className="stat-value">{stats.total}</span>
          </div>
          <div className="stat-icon">
            <FolderKanban size={22} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">Active Workstreams</span>
            <span className="stat-value" style={{ color: "#a5b4fc" }}>
              {stats.active}
            </span>
          </div>
          <div
            className="stat-icon"
            style={{
              background: "rgba(139, 92, 246, 0.12)",
              color: "var(--accent-secondary)",
            }}
          >
            <Activity size={22} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">High Priority Items</span>
            <span className="stat-value" style={{ color: "var(--accent-amber)" }}>
              {stats.critical}
            </span>
          </div>
          <div
            className="stat-icon"
            style={{
              background: "rgba(245, 158, 11, 0.12)",
              color: "var(--accent-amber)",
            }}
          >
            <ShieldAlert size={22} />
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">Workspace Team</span>
            <span className="stat-value" style={{ color: "var(--accent-cyan)" }}>
              {stats.team}
            </span>
          </div>
          <div
            className="stat-icon"
            style={{
              background: "rgba(6, 182, 212, 0.12)",
              color: "var(--accent-cyan)",
            }}
          >
            <Users size={22} />
          </div>
        </div>
      </div>

      {/* Inline Creation Form */}
      {showForm && canManage && (
        <ProjectForm
          onSubmit={handleCreate}
          submitting={creating}
          submitLabel="Create Project"
          showMembers
        />
      )}

      {/* Filter and Search Bar */}
      <div
        style={{
          display: "flex",
          gap: 16,
          marginBottom: 24,
          flexWrap: "wrap",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <div style={{ position: "relative", minWidth: 260, flex: 1 }}>
          <input
            type="text"
            placeholder="Search projects by name or keywords…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{
              paddingLeft: 38,
              background: "var(--card-bg)",
              borderColor: "rgba(255, 255, 255, 0.1)",
              borderRadius: "var(--radius-md)",
            }}
          />
          <Search
            size={16}
            style={{
              position: "absolute",
              left: 12,
              top: 13,
              color: "var(--text-dim)",
            }}
          />
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <Filter size={16} style={{ color: "var(--text-dim)" }} />
          <select
            value={selectedStatusFilter}
            onChange={(e) => setSelectedStatusFilter(e.target.value)}
            style={{
              width: "auto",
              background: "var(--card-bg)",
              borderColor: "rgba(255, 255, 255, 0.1)",
              borderRadius: "var(--radius-md)",
              paddingRight: 32,
            }}
          >
            <option value="ALL">All Statuses</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PLANNED">Planned</option>
            <option value="COMPLETED">Completed</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </div>
      </div>

      {/* Loading Skeleton */}
      {projects === null && !error && (
        <div className="empty-state">
          <span className="loading-spinner" style={{ width: 32, height: 32 }} />
          <p style={{ marginTop: 12 }}>Loading project portfolio…</p>
        </div>
      )}

      {/* Empty State */}
      {projects && filteredProjects.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">
            <FolderOpen size={28} />
          </div>
          <h3 style={{ fontSize: 16, fontWeight: 600, color: "var(--text-main)" }}>
            No projects found
          </h3>
          <p className="page-sub" style={{ maxWidth: 400 }}>
            {searchQuery || selectedStatusFilter !== "ALL"
              ? "No projects matched your search criteria. Try adjusting your filters."
              : canManage
              ? "No projects created yet. Click 'Create New Project' above to get started."
              : "You are not enrolled in any projects currently."}
          </p>
        </div>
      )}

      {/* Project Cards Grid */}
      {filteredProjects.length > 0 && (
        <div className="project-grid">
          {filteredProjects.map((p) => (
            <div key={p.id} className="project-card">
              <div>
                <div className="project-card-header">
                  <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                    <span
                      className={`dot dot-${p.priority || "MEDIUM"}`}
                      title={`Priority: ${p.priority || "MEDIUM"}`}
                    />
                    <span className="project-name">{p.name}</span>
                  </div>
                  <span className={`tag tag-${p.status || "PLANNED"}`}>
                    {p.status || "PLANNED"}
                  </span>
                </div>

                <p className="project-desc" style={{ marginTop: 10 }}>
                  {p.description || "No description provided for this project."}
                </p>
              </div>

              <div>
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    fontSize: 12,
                    color: "var(--text-muted)",
                    marginBottom: 14,
                  }}
                >
                  <span>
                    Lead:{" "}
                    <strong style={{ color: "var(--text-main)" }}>
                      {p.teamLead ? nameFor(users, p.teamLead) : "Unassigned"}
                    </strong>
                  </span>
                  {p.endDate && (
                    <span
                      style={{
                        display: "inline-flex",
                        alignItems: "center",
                        gap: 4,
                        fontFamily: "var(--mono)",
                        fontSize: 11,
                      }}
                    >
                      <Calendar size={12} />
                      {p.endDate}
                    </span>
                  )}
                </div>

                <div className="project-card-footer">
                  <Link
                    to={`/projects/${p.id}`}
                    className="btn btn-sm"
                    style={{ flex: 1, justifyContent: "center" }}
                  >
                    <span>Overview</span>
                    <ArrowUpRight size={14} />
                  </Link>
                  <Link
                    to={`/projects/${p.id}/board`}
                    className="btn btn-sm btn-primary"
                    style={{ flex: 1, justifyContent: "center" }}
                  >
                    <KanbanSquare size={14} />
                    <span>Board</span>
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
