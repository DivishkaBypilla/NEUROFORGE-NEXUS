import { useEffect, useState } from "react";
import { api } from "./api";

// Simple in-memory cache so navigating between pages doesn't re-fetch every time.
let cache = null;
let inflight = null;

async function fetchDirectory() {
  if (cache) return cache;
  if (!inflight) {
    inflight = api.get("/users/directory").then((data) => {
      cache = data;
      inflight = null;
      return data;
    });
  }
  return inflight;
}

export function invalidateDirectoryCache() {
  cache = null;
}

export function useDirectory() {
  const [users, setUsers] = useState(cache);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(!cache);

  useEffect(() => {
    if (cache) return;
    let cancelled = false;
    setLoading(true);
    fetchDirectory()
      .then((data) => {
        if (!cancelled) setUsers(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message || "Could not load team directory");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return { users: users || [], loading, error };
}

export function nameFor(users, id) {
  const match = users.find((u) => u.id === id);
  return match ? match.name : id;
}

// Admins administer the platform, not individual projects — keep them out of
// team lead / member pickers. (The backend enforces this too; this just keeps
// the dropdown from offering an option that would be rejected.)
export function assignableUsers(users) {
  return users.filter((u) => u.role !== "ADMIN");
}

// People actually on a project (owner, team lead, members) — who a task can be
// assigned to. Mirrors the backend's TaskServiceImpl#validateAssignee.
export function projectAssignableUsers(users, project) {
  if (!project) return [];
  const ids = new Set(
    [project.ownerId, project.teamLead, ...(project.memberIds || [])].filter(Boolean)
  );
  return assignableUsers(users).filter((u) => ids.has(u.id));
}

// Mirrors TaskSecurity#canManageProjectTasks on the backend: ADMIN/PROJECT_MANAGER
// can manage any project's tasks; everyone else only if they're this project's
// team lead. Used to decide whether to show "New task" / edit / delete controls.
export function canManageProjectTasks(user, project) {
  if (!user || !project) return false;
  if (user.role === "ADMIN" || user.role === "PROJECT_MANAGER") return true;
  return user.id === project.teamLead;
}

// Mirrors the backend's PROJECT_CREATE/PROJECT_UPDATE/PROJECT_DELETE/
// PROJECT_ASSIGN_TEAM permissions — only ADMIN/PROJECT_MANAGER hold these
// globally.
export function canManagePortfolio(user) {
  return user?.role === "ADMIN" || user?.role === "PROJECT_MANAGER";
}

// Checks whether a user is assigned to a project (as Owner, Team Lead, or Member).
// Admins can see all projects. Non-admins only see projects where they are assigned.
export function isUserAssignedToProject(user, project) {
  if (!user || !project) return false;
  if (user.role === "ADMIN") return true;

  const uid = user.id;
  const email = user.email;

  // Check owner or team lead
  if (project.ownerId && (project.ownerId === uid || project.ownerId === email)) return true;
  if (project.teamLead && (project.teamLead === uid || project.teamLead === email)) return true;

  // Check memberIds array
  if (Array.isArray(project.memberIds)) {
    if (project.memberIds.includes(uid) || (email && project.memberIds.includes(email))) {
      return true;
    }
  }

  // Check members array if populated
  if (Array.isArray(project.members)) {
    if (
      project.members.some(
        (m) =>
          m === uid ||
          m === email ||
          m?.id === uid ||
          m?.email === email
      )
    ) {
      return true;
    }
  }

  return false;
}
