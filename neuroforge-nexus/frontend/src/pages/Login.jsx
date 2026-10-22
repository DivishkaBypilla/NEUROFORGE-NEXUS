import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { fetchGithubConfig, startGithubLogin } from "../lib/github";
import GithubMark from "../components/GithubMark";
import {
  Sparkles,
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

export default function Login() {
  const { login } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [orgSlug, setOrgSlug] = useState("");
  const [githubConfig, setGithubConfig] = useState({ enabled: false, clientId: null });

  // Each organization has its own GitHub OAuth App, so we only know whether
  // "Continue with GitHub" should appear (and what Client ID to use) once an
  // organization slug has been entered — debounced so it doesn't hammer the
  // backend on every keystroke.
  useEffect(() => {
    if (!orgSlug.trim()) {
      setGithubConfig({ enabled: false, clientId: null });
      return;
    }
    const timer = setTimeout(() => {
      fetchGithubConfig(orgSlug).then(setGithubConfig);
    }, 400);
    return () => clearTimeout(timer);
  }, [orgSlug]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/");
    } catch (err) {
      setError(err.message || "Invalid credentials. Please check and try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-screen">
      {/* Top Navbar Actions */}
      <div
        style={{
          position: "absolute",
          top: 24,
          left: 24,
          right: 24,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          zIndex: 10,
        }}
      >
        <Link to="/landing" className="btn btn-sm">
          <ArrowLeft size={16} />
          <span>Back to Landing</span>
        </Link>
        <button
          className="theme-toggle-btn"
          onClick={toggleTheme}
          title={`Switch to ${theme === "dark" ? "Light" : "Dark"} Mode`}
        >
          {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
        </button>
      </div>

      <div className="auth-card">
        <div style={{ textAlign: "center", marginBottom: 28 }}>
          <div
            className="brand-icon-box"
            style={{ width: 44, height: 44, margin: "0 auto 16px" }}
          >
            <Sparkles size={24} />
          </div>
          <h1 className="page-title" style={{ justifyContent: "center" }}>
            Sign in to Workspace
          </h1>
          <p className="page-sub" style={{ marginTop: 6 }}>
            Enter your email and password to access NeuroForge Nexus.
          </p>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={18} flexShrink={0} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="orgSlug">Organization</label>
            <div style={{ position: "relative" }}>
              <input
                id="orgSlug"
                placeholder="your-company"
                value={orgSlug}
                onChange={(e) => setOrgSlug(e.target.value)}
                style={{ paddingLeft: 40 }}
              />
              <Building2
                size={18}
                style={{
                  position: "absolute",
                  left: 12,
                  top: 12,
                  color: "var(--text-dim)",
                }}
              />
            </div>
            <p className="page-sub" style={{ fontSize: 11.5, marginTop: 4 }}>
              Only needed to sign in with GitHub — your email identifies your
              organization for a normal password sign-in.
            </p>
          </div>

          <div className="field">
            <label htmlFor="email">Work Email</label>
            <div style={{ position: "relative" }}>
              <input
                id="email"
                type="email"
                placeholder="name@neuroforge.com"
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
                <span>Sign in</span>
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
              onClick={() => startGithubLogin(githubConfig.clientId, orgSlug)}
              style={{ height: 44 }}
            >
              <GithubMark size={16} />
              <span>Continue with GitHub</span>
            </button>
          </>
        )}

        <p className="page-sub" style={{ textAlign: "center", marginTop: 24, fontSize: 13 }}>
          Don't have an account?{" "}
          <Link to="/signup" style={{ color: "var(--accent-primary)", fontWeight: 600 }}>
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}
