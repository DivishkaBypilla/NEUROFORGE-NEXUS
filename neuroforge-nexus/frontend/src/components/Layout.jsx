import { useState, useEffect } from "react";
import { NavLink, useNavigate, Outlet, useLocation, Link } from "react-router-dom";
import { useAuth, initialsFor } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { canManagePortfolio, isUserAssignedToProject } from "../lib/useDirectory";
import { api } from "../lib/api";
import ChatWidget from "./ChatWidget";
import {
  FolderKanban,
  Users,
  LogOut,
  Menu,
  X,
  Sparkles,
  Layers,
  ChevronRight,
  Sun,
  Moon,
  UserCheck,
  Settings,
  Bug,
} from "lucide-react";

export default function Layout() {
  const { logout, user } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [assignedProjects, setAssignedProjects] = useState([]);

  const canManage = canManagePortfolio(user);

  useEffect(() => {
    if (user && !canManage) {
      api.get("/projects")
        .then((data) => {
          if (Array.isArray(data)) {
            const assigned = data.filter((p) => isUserAssignedToProject(user, p));
            setAssignedProjects(assigned);
          }
        })
        .catch(() => {});
    }
  }, [user, canManage]);

  function handleLogout() {
    logout();
    navigate("/");
  }

  const getPageTitle = () => {
    if (location.pathname === "/") return "Projects Dashboard";
    if (location.pathname.startsWith("/team")) return "Team Directory";
    if (location.pathname.startsWith("/bugs")) return "Bug Reports";
    if (location.pathname.startsWith("/settings/jenkins")) return "Jenkins CI/CD Integration";
    if (location.pathname.includes("/delivery")) return "Sprints & DevOps";
    if (location.pathname.includes("/board")) return "Kanban Task Board";
    if (location.pathname.startsWith("/projects/")) return "Project Details";
    return "NeuroForge Nexus";
  };

  return (
    <div className="shell">
      {/* Mobile Top Navigation Header */}
      <div className="mobile-header">
        <Link to="/" className="brand" style={{ padding: 0 }}>
          <div className="brand-icon-box" style={{ width: 28, height: 28 }}>
            <Sparkles size={16} />
          </div>
          <span className="brand-title" style={{ fontSize: 13 }}>
            NEUROFORGE // NEXUS
          </span>
        </Link>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <button
            className="theme-toggle-btn"
            onClick={toggleTheme}
            title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
          >
            {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
          </button>
          <button
            className="btn btn-sm"
            onClick={() => setMobileOpen((v) => !v)}
            aria-label="Toggle navigation menu"
          >
            {mobileOpen ? <X size={18} /> : <Menu size={18} />}
          </button>
        </div>
      </div>

      {/* Mobile Backdrop Overlay */}
      <div
        className={`mobile-overlay ${mobileOpen ? "open" : ""}`}
        onClick={() => setMobileOpen(false)}
      />

      {/* Sidebar Navigation */}
      <aside className={`sidebar ${mobileOpen ? "open" : ""}`}>
        <div className="sidebar-header">
          <Link to="/" className="brand">
            <div className="brand-icon-box">
              <Sparkles size={20} />
            </div>
            <div className="brand-text">
              <span className="brand-title">NEUROFORGE</span>
              <span className="brand-subtitle">NEXUS PLATFORM</span>
            </div>
          </Link>

          <nav className="nav">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `nav-item ${isActive ? "active" : ""}`
              }
              onClick={() => setMobileOpen(false)}
            >
              <FolderKanban size={18} />
              <span>Projects</span>
            </NavLink>

            <NavLink to="/bugs" className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`} onClick={() => setMobileOpen(false)}>
              <Bug size={18} /><span>Bug Reports</span>
            </NavLink>

            {canManage ? (
              <>
                <NavLink
                  to="/team"
                  className={({ isActive }) =>
                    `nav-item ${isActive ? "active" : ""}`
                  }
                  onClick={() => setMobileOpen(false)}
                >
                  <Users size={18} />
                  <span>Team Directory</span>
                </NavLink>

                <NavLink
                  to="/settings/github"
                  className={({ isActive }) =>
                    `nav-item ${isActive ? "active" : ""}`
                  }
                  onClick={() => setMobileOpen(false)}
                >
                  <Settings size={18} />
                  <span>Integrations</span>
                </NavLink>
                <NavLink
                  to="/settings/jenkins"
                  className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
                  onClick={() => setMobileOpen(false)}
                >
                  <Settings size={18} />
                  <span>Jenkins CI/CD</span>
                </NavLink>
              </>
            ) : (
              <>
                {assignedProjects.length > 0 ? (
                  assignedProjects.map((p) => (
                    <NavLink
                      key={p.id}
                      to={`/projects/${p.id}`}
                      className={({ isActive }) =>
                        `nav-item ${isActive ? "active" : ""}`
                      }
                      onClick={() => setMobileOpen(false)}
                      title={`Team members in ${p.name}`}
                    >
                      <UserCheck size={18} />
                      <span
                        style={{
                          fontSize: 12.5,
                          whiteSpace: "nowrap",
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                        }}
                      >
                        Team Members in {p.name}
                      </span>
                    </NavLink>
                  ))
                ) : (
                  <NavLink
                    to="/"
                    className="nav-item"
                    onClick={() => setMobileOpen(false)}
                  >
                    <Users size={18} />
                    <span>My Project Team</span>
                  </NavLink>
                )}
              </>
            )}
          </nav>
        </div>

        <div className="sidebar-footer">
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span style={{ fontSize: 12, fontWeight: 600, color: "var(--text-muted)" }}>
              THEME MODE
            </span>
            <button
              className="theme-toggle-btn"
              onClick={toggleTheme}
              title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
            >
              {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
            </button>
          </div>

          {user && (
            <div className="user-profile-card">
              <span className="avatar">{initialsFor(user.name)}</span>
              <div className="user-info">
                <div className="user-name">{user.name}</div>
                <div className="user-role-badge">
                  {user.role || "MEMBER"}
                </div>
              </div>
            </div>
          )}
          <button className="btn btn-full" onClick={handleLogout}>
            <LogOut size={16} />
            <span>Sign out</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        <header
          style={{
            marginBottom: 24,
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            fontSize: 12,
            color: "var(--text-dim)",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <Layers size={14} />
            <span>Workspace</span>
            <ChevronRight size={12} />
            <span style={{ color: "var(--text-muted)", fontWeight: 500 }}>
              {getPageTitle()}
            </span>
          </div>

          <button
            className="theme-toggle-btn"
            onClick={toggleTheme}
            title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
          >
            {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
          </button>
        </header>

        <Outlet />
      </main>

      <ChatWidget />
    </div>
  );
}
