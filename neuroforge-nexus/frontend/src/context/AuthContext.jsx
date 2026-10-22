import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { api, setAccessToken, getAccessToken } from "../lib/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(getAccessToken());
  const [user, setUser] = useState(null); // { id, name, email, role }
  const [userLoading, setUserLoading] = useState(!!getAccessToken());

  const fetchProfile = useCallback(async () => {
    try {
      const profile = await api.get("/users/me");
      setUser(profile);
      return profile;
    } catch {
      // token invalid/expired and refresh failed — fall back to signed-out state
      setAccessToken(null);
      setToken(null);
      setUser(null);
      return null;
    } finally {
      setUserLoading(false);
    }
  }, []);

  // On first load, if a token is already stored, hydrate the profile.
  useEffect(() => {
    if (token) fetchProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = useCallback(
    async (email, password) => {
      const data = await api.post("/auth/login", { email, password });
      setAccessToken(data.accessToken);
      setToken(data.accessToken);
      await fetchProfile();
      return data;
    },
    [fetchProfile]
  );

  const loginWithGithub = useCallback(
    async (code, organizationSlug) => {
      const data = await api.post("/auth/github/callback", { code, organizationSlug });
      setAccessToken(data.accessToken);
      setToken(data.accessToken);
      await fetchProfile();
      return data;
    },
    [fetchProfile]
  );

  const signup = useCallback(async (name, email, password, organizationName, organizationSlug) => {
    return api.post("/auth/signup", { name, email, password, organizationName, organizationSlug });
  }, []);

  const logout = useCallback(() => {
    setAccessToken(null);
    setToken(null);
    setUser(null);
  }, []);

  const value = {
    token,
    user,
    userLoading,
    isAuthenticated: !!token,
    login,
    loginWithGithub,
    signup,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

export function initialsFor(name) {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  const first = parts[0]?.[0] || "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (first + last).toUpperCase();
}
