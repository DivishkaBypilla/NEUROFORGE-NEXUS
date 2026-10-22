import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { consumeGithubState, consumeGithubOrgSlug } from "../lib/github";
import GithubMark from "../components/GithubMark";
import { AlertCircle, ArrowLeft } from "lucide-react";

export default function GithubCallback() {
  const { loginWithGithub } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState("");
  const ran = useRef(false);

  useEffect(() => {
    // Effects can double-fire in dev; the OAuth code is single-use, so make
    // sure this only actually runs once.
    if (ran.current) return;
    ran.current = true;

    const code = searchParams.get("code");
    const state = searchParams.get("state");
    const oauthError = searchParams.get("error_description") || searchParams.get("error");
    const expectedState = consumeGithubState();
    const organizationSlug = consumeGithubOrgSlug();

    if (oauthError) {
      setError(oauthError);
      return;
    }
    if (!code) {
      setError("GitHub didn't return an authorization code.");
      return;
    }
    if (expectedState && state !== expectedState) {
      setError("GitHub sign-in could not be verified (state mismatch). Please try again.");
      return;
    }
    if (!organizationSlug) {
      setError("Lost track of which organization you were signing into. Please try again.");
      return;
    }

    loginWithGithub(code, organizationSlug)
      .then(() => navigate("/", { replace: true }))
      .catch((err) => {
        setError(err.message || "GitHub sign-in failed.");
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="auth-screen">
      <div className="auth-card" style={{ textAlign: "center" }}>
        <div
          className="brand-icon-box"
          style={{ width: 44, height: 44, margin: "0 auto 16px" }}
        >
          <GithubMark size={22} />
        </div>

        {error ? (
          <>
            <h1 className="page-title" style={{ justifyContent: "center" }}>
              GitHub sign-in failed
            </h1>
            <div className="alert alert-error" style={{ marginTop: 16, textAlign: "left" }}>
              <AlertCircle size={18} flexShrink={0} />
              <span>{error}</span>
            </div>
            <Link
              to="/login"
              className="btn btn-primary btn-full"
              style={{ marginTop: 20, height: 44 }}
            >
              <ArrowLeft size={16} />
              <span>Back to sign in</span>
            </Link>
          </>
        ) : (
          <>
            <h1 className="page-title" style={{ justifyContent: "center" }}>
              Finishing GitHub sign-in…
            </h1>
            <p className="page-sub" style={{ marginTop: 8 }}>
              Hang tight, this only takes a moment.
            </p>
            <span
              className="loading-spinner"
              style={{ width: 28, height: 28, margin: "24px auto 0" }}
            />
          </>
        )}
      </div>
    </div>
  );
}
