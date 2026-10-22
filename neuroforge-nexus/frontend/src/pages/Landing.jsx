import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import {
  Sparkles,
  KanbanSquare,
  ShieldCheck,
  FolderKanban,
  CheckSquare,
  ArrowRight,
  Sun,
  Moon,
  Zap,
  Users,
  Layers,
  Check,
} from "lucide-react";

export default function Landing() {
  const { user } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      {/* Top Navbar */}
      <nav className="landing-nav">
        <div style={{ display: "flex", alignItems: "center", gap: 10, flexShrink: 0 }}>
          <div className="brand-icon-box">
            <Sparkles size={20} />
          </div>
          <div className="brand-text">
            <span className="brand-title">NEUROFORGE</span>
            <span className="brand-subtitle">NEXUS PLATFORM</span>
          </div>
        </div>

        <div className="landing-nav-links">
          <a href="#features" className="landing-nav-link">
            Features
          </a>
          <a href="#enterprise" className="landing-nav-link">
            Enterprise Security
          </a>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: 10, flexShrink: 0 }}>
          <button
            className="theme-toggle-btn"
            onClick={toggleTheme}
            title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
          >
            {theme === "dark" ? <Sun size={17} /> : <Moon size={17} />}
          </button>

          {user ? (
            <Link to="/" className="btn btn-primary btn-sm">
              <span>Go to Workspace</span>
              <ArrowRight size={15} />
            </Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-sm">
                <span>Sign In</span>
              </Link>
              <Link to="/signup" className="btn btn-primary btn-sm">
                <span>Get Started</span>
              </Link>
            </>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <header className="landing-hero">
        <div className="landing-badge">
          <Zap size={14} />
          <span>NEUROFORGE NEXUS v2.0 · ENTERPRISE SDLC SUITE</span>
        </div>

        <h1 className="landing-title">
          Unified Workspaces & <br />
          <span className="landing-title-gradient">
            Kanban Collaboration at Scale
          </span>
        </h1>

        <p className="landing-sub">
          Accelerate multi-team engineering delivery with real-time task boards,
          fine-grained role governance, project portfolios, and actionable subtask checklists.
        </p>

        <div className="landing-cta-group">
          {user ? (
            <Link to="/" className="btn btn-primary" style={{ padding: "14px 28px", fontSize: 15 }}>
              <span>Launch Your Workspace</span>
              <ArrowRight size={18} />
            </Link>
          ) : (
            <>
              <Link
                to="/login"
                className="btn btn-primary"
                style={{ padding: "14px 28px", fontSize: 15 }}
              >
                <span>Sign In to Workspace</span>
                <ArrowRight size={18} />
              </Link>
              <Link
                to="/signup"
                className="btn"
                style={{ padding: "14px 24px", fontSize: 15 }}
              >
                <span>Create Free Account</span>
              </Link>
            </>
          )}
        </div>
      </header>

      {/* Feature Highlights Grid */}
      <section id="features" className="feature-grid">
        <div className="feature-card">
          <div className="feature-icon-box">
            <KanbanSquare size={24} />
          </div>
          <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--text-main)" }}>
            Visual Kanban Workflows
          </h3>
          <p style={{ color: "var(--text-muted)", fontSize: 14, lineHeight: 1.6 }}>
            Track project deliverables seamlessly across To Do, In Progress, and Completed stages with real-time status transitions.
          </p>
        </div>

        <div className="feature-card">
          <div
            className="feature-icon-box"
            style={{ background: "rgba(139, 92, 246, 0.12)", color: "var(--accent-secondary)" }}
          >
            <ShieldCheck size={24} />
          </div>
          <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--text-main)" }}>
            Fine-Grained Governance
          </h3>
          <p style={{ color: "var(--text-muted)", fontSize: 14, lineHeight: 1.6 }}>
            Enforce role-based access control (RBAC) across Admins, Project Managers, Developers, and Testers to maintain project security.
          </p>
        </div>

        <div className="feature-card">
          <div
            className="feature-icon-box"
            style={{ background: "rgba(6, 182, 212, 0.12)", color: "var(--accent-cyan)" }}
          >
            <FolderKanban size={24} />
          </div>
          <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--text-main)" }}>
            Multi-Project Portfolios
          </h3>
          <p style={{ color: "var(--text-muted)", fontSize: 14, lineHeight: 1.6 }}>
            Oversee active workstreams, priority deliverables, team member allocations, and timeline milestones from a single dashboard.
          </p>
        </div>

        <div className="feature-card">
          <div
            className="feature-icon-box"
            style={{ background: "rgba(16, 185, 129, 0.12)", color: "var(--accent-emerald)" }}
          >
            <CheckSquare size={24} />
          </div>
          <h3 style={{ fontSize: 18, fontWeight: 700, color: "var(--text-main)" }}>
            Actionable Subtasks
          </h3>
          <p style={{ color: "var(--text-muted)", fontSize: 14, lineHeight: 1.6 }}>
            Break down complex engineering epics into precise subtasks with live completion indicators for developer efficiency.
          </p>
        </div>
      </section>

      {/* Enterprise Security Section */}
      <section
        id="enterprise"
        style={{
          maxWidth: 1200,
          margin: "40px auto 80px",
          padding: "0 24px",
          width: "100%",
        }}
      >
        <div
          style={{
            background: "var(--card-bg)",
            backdropFilter: "blur(20px)",
            border: "var(--glass-border)",
            borderRadius: "var(--radius-lg)",
            padding: 40,
            boxShadow: "var(--shadow-md)",
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))",
            gap: 32,
            alignItems: "center",
          }}
        >
          <div>
            <div
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: 8,
                fontSize: 12,
                fontWeight: 700,
                color: "var(--accent-cyan)",
                textTransform: "uppercase",
                letterSpacing: "0.08em",
                marginBottom: 12,
              }}
            >
              <Layers size={16} />
              <span>Enterprise Grade Platform</span>
            </div>
            <h2
              style={{
                fontSize: 32,
                fontWeight: 800,
                color: "var(--text-main)",
                marginBottom: 16,
                lineHeight: 1.2,
              }}
            >
              Engineered for High-Velocity Software Delivery
            </h2>
            <p style={{ color: "var(--text-muted)", fontSize: 15, lineHeight: 1.7, marginBottom: 24 }}>
              NeuroForge Nexus provides enterprise-ready audit logging, secure token authentication, role escalation protection, and responsive workspace boards.
            </p>
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              {[
                "Strict member-level project assignment filtering",
                "Role escalation protections for Admin & PM accounts",
                "Real-time theme switching with dark/light mode persistence",
                "Clean REST interface integration for production scalability",
              ].map((item, idx) => (
                <div key={idx} style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <div
                    style={{
                      width: 22,
                      height: 22,
                      borderRadius: "50%",
                      background: "rgba(16, 185, 129, 0.15)",
                      color: "var(--accent-emerald)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      flexShrink: 0,
                    }}
                  >
                    <Check size={14} />
                  </div>
                  <span style={{ fontSize: 14, color: "var(--text-main)", fontWeight: 500 }}>
                    {item}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div
            style={{
              background: "var(--panel-bg-solid)",
              border: "var(--glass-border)",
              borderRadius: "var(--radius-md)",
              padding: 24,
              boxShadow: "var(--shadow-sm)",
            }}
          >
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 12,
                marginBottom: 16,
                paddingBottom: 16,
                borderBottom: "var(--glass-border)",
              }}
            >
              <Users size={20} style={{ color: "var(--accent-primary)" }} />
              <div>
                <div style={{ fontWeight: 700, color: "var(--text-main)", fontSize: 15 }}>
                  Access Governance Roster
                </div>
                <div style={{ fontSize: 12, color: "var(--text-dim)" }}>
                  Configured system privileges
                </div>
              </div>
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {[
                { role: "ADMIN", desc: "Full administrative access & portfolio governance" },
                { role: "PROJECT_MANAGER", desc: "Manage assigned projects, teams & kanban boards" },
                { role: "DEVELOPER", desc: "Execute tasks, manage subtasks & update status" },
                { role: "TESTER", desc: "Verify QA criteria & manage issue boards" },
              ].map((r, i) => (
                <div
                  key={i}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: 10,
                    borderRadius: "var(--radius-sm)",
                    background: "var(--card-bg)",
                  }}
                >
                  <div>
                    <span className="mono" style={{ fontSize: 12, fontWeight: 700, color: "var(--accent-cyan)" }}>
                      {r.role}
                    </span>
                    <div style={{ fontSize: 11.5, color: "var(--text-muted)", marginTop: 2 }}>
                      {r.desc}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer
        style={{
          marginTop: "auto",
          borderTop: "var(--glass-border)",
          padding: "24px 48px",
          background: "var(--panel-bg)",
        }}
      >
        <div
          style={{
            maxWidth: 1200,
            margin: "0 auto",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            flexWrap: "wrap",
            gap: 20,
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div className="brand-icon-box" style={{ width: 28, height: 28 }}>
              <Sparkles size={16} />
            </div>
            <span className="mono" style={{ fontSize: 13, fontWeight: 700, color: "var(--text-main)" }}>
              NEUROFORGE // NEXUS PLATFORM
            </span>
          </div>

          <div style={{ fontSize: 12, color: "var(--text-dim)" }}>
            © {new Date().getFullYear()} NeuroForge Systems. All rights reserved.
          </div>
        </div>
      </footer>
    </div>
  );
}
