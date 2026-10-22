import { useEffect, useState, useMemo } from "react";
import {
  useDirectory,
  invalidateDirectoryCache,
  canManagePortfolio,
} from "../lib/useDirectory";
import { useAuth, initialsFor } from "../context/AuthContext";
import { api } from "../lib/api";
import { Link } from "react-router-dom";
import {
  Users,
  Search,
  Copy,
  Check,
  ShieldAlert,
  AlertCircle,
  Filter,
  ArrowLeft,
  Trash2,
  UserX,
} from "lucide-react";

const ROLES = [
  "ADMIN",
  "PROJECT_MANAGER",
  "DEVELOPER",
  "TESTER",
  "DEVOPS_ENGINEER",
];

const ROLE_COLORS = {
  ADMIN: { bg: "rgba(139, 92, 246, 0.15)", color: "#c4b5fd", border: "rgba(139, 92, 246, 0.3)" },
  PROJECT_MANAGER: { bg: "rgba(99, 102, 241, 0.15)", color: "#a5b4fc", border: "rgba(99, 102, 241, 0.3)" },
  DEVELOPER: { bg: "rgba(16, 185, 129, 0.15)", color: "#6ee7b7", border: "rgba(16, 185, 129, 0.3)" },
  TESTER: { bg: "rgba(245, 158, 11, 0.15)", color: "#fcd34d", border: "rgba(245, 158, 11, 0.3)" },
  DEVOPS_ENGINEER: { bg: "rgba(6, 182, 212, 0.15)", color: "#67e8f9", border: "rgba(6, 182, 212, 0.3)" },
};

export default function Team() {
  const { user } = useAuth();
  const { users, loading, error: loadError } = useDirectory();
  const [rows, setRows] = useState(users);
  const [savingId, setSavingId] = useState(null);
  const [rowError, setRowError] = useState({});
  const [copiedId, setCopiedId] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");

  const isAdmin = user?.role === "ADMIN";
  const seesWholeWorkspace = canManagePortfolio(user);

  useEffect(() => {
    setRows(users);
  }, [users]);

  // Access guard: Only ADMIN and PROJECT_MANAGER can view the global workspace Team Directory
  if (!seesWholeWorkspace) {
    return (
      <div className="empty-state" style={{ minHeight: "60vh", justifyContent: "center" }}>
        <div
          className="empty-icon"
          style={{ background: "rgba(244, 63, 94, 0.12)", color: "var(--accent-rose)" }}
        >
          <ShieldAlert size={32} />
        </div>
        <h2 style={{ fontSize: 20, fontWeight: 700, color: "var(--text-main)", marginTop: 16 }}>
          Access Restricted: Workspace Team Directory
        </h2>
        <p className="page-sub" style={{ maxWidth: 480, marginTop: 8 }}>
          The full organization directory is restricted to Admins and Project Managers.
          To view your project team members, select your assigned project from the workspace.
        </p>
        <Link to="/" className="btn btn-primary" style={{ marginTop: 20 }}>
          <ArrowLeft size={16} />
          <span>Return to Workspace Projects</span>
        </Link>
      </div>
    );
  }

  async function handleRoleChange(userId, newRole) {
    setSavingId(userId);
    setRowError((prev) => ({ ...prev, [userId]: "" }));
    try {
      await api.patch(`/users/${userId}/role?role=${newRole}`);
      setRows((prev) =>
        prev.map((r) => (r.id === userId ? { ...r, role: newRole } : r))
      );
      invalidateDirectoryCache();
    } catch (err) {
      setRowError((prev) => ({
        ...prev,
        [userId]: err.message || "Could not update role.",
      }));
    } finally {
      setSavingId(null);
    }
  }

  async function handleDeleteUser(userId) {
    if (!window.confirm("Are you sure you want to remove this user from the workspace?")) return;
    setSavingId(userId);
    try {
      await api.del(`/users/${userId}`);
      setRows((prev) => prev.filter((u) => u.id !== userId));
      invalidateDirectoryCache();
    } catch (err) {
      setRowError((prev) => ({
        ...prev,
        [userId]: err.message || "Could not delete user.",
      }));
    } finally {
      setSavingId(null);
    }
  }

  async function handleRemoveAllNonAdmins() {
    if (!window.confirm("Are you sure you want to remove ALL non-admin users from the workspace?")) return;
    try {
      await api.del("/users/non-admin");
      setRows((prev) => prev.filter((u) => u.role === "ADMIN"));
      invalidateDirectoryCache();
    } catch (err) {
      alert(err.message || "Could not remove non-admin users.");
    }
  }

  function handleCopy(id) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(id);
      setCopiedId(id);
      setTimeout(() => setCopiedId(null), 2000);
    }
  }

  const filteredRows = useMemo(() => {
    return rows.filter((u) => {
      const matchesSearch =
        u.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        u.email.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [rows, searchQuery, roleFilter]);

  const hasNonAdmins = useMemo(() => rows.some((u) => u.role !== "ADMIN"), [rows]);

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <Users size={26} style={{ color: "var(--accent-primary)" }} />
            <span>Workspace Team Directory</span>
          </h1>
          <p className="page-sub">
            {isAdmin
              ? "All workspace members. Admins can manage roles or remove users."
              : seesWholeWorkspace
              ? "All engineering and product team members in the organization."
              : "Teammates you collaborate with across active projects."}
          </p>
        </div>
        {isAdmin && hasNonAdmins && (
          <button
            className="btn"
            onClick={handleRemoveAllNonAdmins}
            style={{
              background: "rgba(244, 63, 94, 0.15)",
              color: "var(--accent-rose)",
              border: "1px solid rgba(244, 63, 94, 0.3)",
              display: "flex",
              alignItems: "center",
              gap: 8,
              padding: "8px 16px",
              fontWeight: 600,
            }}
          >
            <UserX size={16} />
            <span>Remove All Non-Admins</span>
          </button>
        )}
      </div>

      {loadError && (
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{loadError}</span>
        </div>
      )}

      {/* Search and Role Filter */}
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
            placeholder="Filter members by name or email…"
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
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            style={{
              width: "auto",
              background: "var(--card-bg)",
              borderColor: "rgba(255, 255, 255, 0.1)",
              borderRadius: "var(--radius-md)",
            }}
          >
            <option value="ALL">All Roles</option>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading && (
        <div className="empty-state">
          <span className="loading-spinner" style={{ width: 32, height: 32 }} />
          <p style={{ marginTop: 12 }}>Loading team directory…</p>
        </div>
      )}

      {!loading && filteredRows.length === 0 && !loadError && (
        <div className="empty-state">
          <div className="empty-icon">
            <Users size={28} />
          </div>
          <h3 style={{ fontSize: 16, fontWeight: 600, color: "var(--text-main)" }}>
            No team members found
          </h3>
          <p className="page-sub">No workspace users matched your search criteria.</p>
        </div>
      )}

      {!loading && filteredRows.length > 0 && (
        <div className="table-container">
          <table className="custom-table">
            <thead>
              <tr>
                <th>Member</th>
                <th>Work Email</th>
                <th>Role & Permissions</th>
                <th style={{ textAlign: "right" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((u) => {
                const styleObj = ROLE_COLORS[u.role] || {
                  bg: "rgba(255,255,255,0.06)",
                  color: "var(--text-muted)",
                  border: "rgba(255,255,255,0.1)",
                };
                return (
                  <tr key={u.id}>
                    <td>
                      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                        <span className="avatar">{initialsFor(u.name)}</span>
                        <div style={{ fontWeight: 600, color: "var(--text-main)" }}>
                          {u.name}
                        </div>
                      </div>
                    </td>

                    <td style={{ color: "var(--text-muted)" }}>{u.email}</td>

                    <td>
                      {isAdmin ? (
                        <select
                          className="mono"
                          value={u.role}
                          disabled={savingId === u.id}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          style={{
                            background: styleObj.bg,
                            border: `1px solid ${styleObj.border}`,
                            color: styleObj.color,
                            fontSize: 11,
                            fontWeight: 600,
                            padding: "5px 10px",
                            borderRadius: "var(--radius-sm)",
                            width: "auto",
                          }}
                        >
                          {ROLES.map((r) => (
                            <option key={r} value={r} style={{ background: "#121721" }}>
                              {r}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <span
                          className="tag"
                          style={{
                            background: styleObj.bg,
                            color: styleObj.color,
                            borderColor: styleObj.border,
                          }}
                        >
                          {u.role}
                        </span>
                      )}

                      {rowError[u.id] && (
                        <div
                          style={{
                            fontSize: 11,
                            color: "var(--accent-rose)",
                            marginTop: 4,
                          }}
                        >
                          {rowError[u.id]}
                        </div>
                      )}
                    </td>

                    <td style={{ textAlign: "right" }}>
                      <button
                        className="btn btn-sm"
                        onClick={() => handleCopy(u.id)}
                        title="Copy unique User ID"
                      >
                        {copiedId === u.id ? (
                          <>
                            <Check size={14} style={{ color: "var(--accent-emerald)" }} />
                            <span style={{ color: "var(--accent-emerald)" }}>Copied</span>
                          </>
                        ) : (
                          <>
                            <Copy size={14} />
                            <span>Copy ID</span>
                          </>
                        )}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
