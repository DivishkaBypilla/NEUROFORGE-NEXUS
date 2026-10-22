import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { fetchGithubConfig, startGithubLogin } from "../lib/github";
import GithubMark from "../components/GithubMark";
import {
  Sparkles,
  User,
  Mail,
  Lock,
  Eye,
  EyeOff,
  AlertCircle,
  ArrowRight,
  Sun,
  Moon,
  ArrowLeft,
  Building2,
} from "lucide-react";

export default function Signup() {
  const { signup, login } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  // "create": this is a brand new company — a new Organization is created
  // and this person becomes its Admin. "join": joining a company already
  // using NeuroForge, identified by the slug its Admin shared.
  const [orgMode, setOrgMode] = useState("create");
  const [organizationName, setOrganizationName] = useState("");
  const [organizationSlug, setOrganizationSlug] = useState("");

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [githubConfig, setGithubConfig] = useState({ enabled: false, clientId: null });

  // GitHub sign-up only makes sense for JOINING an existing organization —
  // there's no GitHub App to use yet for a company that doesn't exist until
  // this form is submitted. So the GitHub option only appears once a slug
  // has been entered in "join" mode.
  useEffect(() => {
    if (orgMode !== "join" || !organizationSlug.trim()) {
      setGithubConfig({ enabled: false, clientId: null });
      return;
    }
    const timer = setTimeout(() => {
      fetchGithubConfig(organizationSlug).then(setGithubConfig);
    }, 400);
    return () => clearTimeout(timer);
  }, [orgMode, organizationSlug]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await signup(
        name,
        email,
        password,
        orgMode === "create" ? organizationName : undefined,
        orgMode === "join" ? organizationSlug : undefined
      );
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err.message || "Could not create account. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-screen">
      {/* Top Header Actions */}
      <div
        style={{
          position: "absolute",
          top: 24,
          left: 24,
          right: 24,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        <Link to="/landing" className="btn btn-sm">
          <ArrowLeft size={14} />
          <span>Back to Landing</span>
        </Link>

        <button
          className="theme-toggle-btn"
          onClick={toggleTheme}
          title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
        >
          {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
        </button>
      </div>

      <div className="auth-card">
        <div style={{ textAlign: "center", marginBottom: 28 }}>
          <div
            className="brand-icon-box"
            style={{ width: 44, height: 44, margin: "0 auto 14px", borderRadius: 12 }}
          >
            <Sparkles size={24} />
          </div>
          <h1
            style={{
              fontSize: 22,
              fontWeight: 800,
              letterSpacing: "-0.02em",
              color: "var(--text-main)",
            }}
          >
            JOIN NEUROFORGE
          </h1>
          <p className="page-sub" style={{ fontSize: 13, marginTop: 4 }}>
            Create your engineering workspace account
          </p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={18} flexShrink={0} />
            <span>{error}</span>
          </div>
        )}

        <div className="view-switcher" style={{ marginBottom: 18, width: "100%" }}>
          <button
            type="button"
            className={`view-switcher-btn ${orgMode === "create" ? "active" : ""}`}
            style={{ flex: 1, justifyContent: "center" }}
            onClick={() => setOrgMode("create")}
          >
            <span>New company</span>
          </button>
          <button
            type="button"
            className={`view-switcher-btn ${orgMode === "join" ? "active" : ""}`}
            style={{ flex: 1, justifyContent: "center" }}
            onClick={() => setOrgMode("join")}
          >
            <span>Join existing</span>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {orgMode === "create" ? (
            <div className="field">
              <label htmlFor="organizationName">Company Name</label>
              <div style={{ position: "relative" }}>
                <input
                  id="organizationName"
                  placeholder="Acme Corp"
                  value={organizationName}
                  onChange={(e) => setOrganizationName(e.target.value)}
                  required
                  style={{ paddingLeft: 40 }}
                />
                <Building2
                  size={18}
                  style={{ position: "absolute", left: 12, top: 12, color: "var(--text-dim)" }}
                />
              </div>
              <p className="page-sub" style={{ fontSize: 11.5, marginTop: 4 }}>
                You'll be this workspace's first Admin.
              </p>
            </div>
          ) : (
            <div className="field">
              <label htmlFor="organizationSlug">Organization</label>
              <div style={{ position: "relative" }}>
                <input
                  id="organizationSlug"
                  placeholder="acme-corp"
                  value={organizationSlug}
                  onChange={(e) => setOrganizationSlug(e.target.value)}
                  required
                  style={{ paddingLeft: 40 }}
                />
                <Building2
                  size={18}
                  style={{ position: "absolute", left: 12, top: 12, color: "var(--text-dim)" }}
                />
              </div>
              <p className="page-sub" style={{ fontSize: 11.5, marginTop: 4 }}>
                Ask your organization's admin for this handle.
              </p>
            </div>
          )}

          <div className="field">
            <label htmlFor="name">Full Name</label>
            <div style={{ position: "relative" }}>
              <input
                id="name"
                type="text"
                placeholder="Jane Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                style={{ paddingLeft: 40 }}
              />
              <User
                size={18}
                style={{
                  position: "absolute",
                  left: 12,
                  top: 12,
                  color: "var(--text-dim)",
                }}
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="email">Work Email</label>
            <div style={{ position: "relative" }}>
              <input
                id="email"
                type="email"
                placeholder="name@company.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                style={{ paddingLeft: 40 }}
              />
              <Mail
                size={18}
                style={{
                  position: "absolute",
                  left: 12,
                  top: 12,
                  color: "var(--text-dim)",
                }}
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="password">Password</label>
            <div style={{ position: "relative" }}>
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{ paddingLeft: 40, paddingRight: 40 }}
              />
              <Lock
                size={18}
                style={{
                  position: "absolute",
                  left: 12,
                  top: 12,
                  color: "var(--text-dim)",
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                style={{
                  position: "absolute",
                  right: 12,
                  top: 10,
                  color: "var(--text-dim)",
                  padding: 2,
                }}
                aria-label="Toggle password visibility"
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button
            className="btn btn-primary btn-full"
            type="submit"
            disabled={loading}
            style={{ marginTop: 8, height: 44 }}
          >
            {loading ? (
              <span className="loading-spinner" />
            ) : (
              <>
                <span>Create account</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

        {githubConfig.enabled && (
          <>
            <div className="auth-divider">
              <span>or</span>
            </div>
            <button
              type="button"
              className="btn btn-full"
              onClick={() => startGithubLogin(githubConfig.clientId, organizationSlug)}
              style={{ height: 44 }}
            >
              <GithubMark size={16} />
              <span>Continue with GitHub</span>
            </button>
          </>
        )}

        <p className="page-sub" style={{ textAlign: "center", marginTop: 24, fontSize: 13 }}>
          Already have an account?{" "}
          <Link to="/login" style={{ color: "var(--accent-primary)", fontWeight: 600 }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
