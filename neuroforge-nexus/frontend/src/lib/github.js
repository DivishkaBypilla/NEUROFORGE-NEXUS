import { api } from "./api";

const STATE_KEY = "github_oauth_state";
const ORG_KEY = "github_oauth_org_slug";

// Must exactly match whatever the backend has stored as the redirect_uri
// (Settings, or the GITHUB_REDIRECT_URI env var fallback) and the
// "Authorization callback URL" set on that organization's GitHub OAuth App.
export function githubRedirectUri() {
  return `${window.location.origin}/auth/github/callback`;
}

// Fetches whether GitHub sign-in is set up for a given organization and, if
// so, the (non-secret) client ID to use — set at runtime by that org's
// admin/project manager under Settings, so this never needs a frontend
// rebuild or env var. Each organization has its own GitHub OAuth App, so the
// org's slug is required.
export async function fetchGithubConfig(orgSlug) {
  if (!orgSlug || !orgSlug.trim()) {
    return { enabled: false, clientId: null, redirectUri: null };
  }
  try {
    return await api.get(`/auth/github/config?org=${encodeURIComponent(orgSlug.trim())}`);
  } catch {
    // Backend unreachable, or standalone/mock mode — treat as "not set up"
    // rather than breaking the login page.
    return { enabled: false, clientId: null, redirectUri: null };
  }
}

// Redirects the browser to GitHub's OAuth consent screen, using the given
// organization's Client ID. A random `state` value and the org's slug are
// both stashed in sessionStorage: `state` as basic CSRF protection for the
// redirect round-trip, and the slug so the callback page knows which
// organization's GitHub App to complete the exchange against.
export function startGithubLogin(clientId, orgSlug) {
  if (!clientId || !orgSlug) return;

  const state = crypto.randomUUID();
  sessionStorage.setItem(STATE_KEY, state);
  sessionStorage.setItem(ORG_KEY, orgSlug.trim());

  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: githubRedirectUri(),
    scope: "read:user user:email",
    state,
  });

  window.location.href = `https://github.com/login/oauth/authorize?${params.toString()}`;
}

// Returns the stored state value and clears it (one-time use).
export function consumeGithubState() {
  const state = sessionStorage.getItem(STATE_KEY);
  sessionStorage.removeItem(STATE_KEY);
  return state;
}

// Returns the stored organization slug and clears it (one-time use).
export function consumeGithubOrgSlug() {
  const slug = sessionStorage.getItem(ORG_KEY);
  sessionStorage.removeItem(ORG_KEY);
  return slug;
}
