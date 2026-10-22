import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { ThemeProvider } from "./context/ThemeContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import GithubCallback from "./pages/GithubCallback";
import Dashboard from "./pages/Dashboard";
import ProjectDetail from "./pages/ProjectDetail";
import Team from "./pages/Team";
import GithubIntegrationSettings from "./pages/GithubIntegrationSettings";
import TaskBoard from "./pages/TaskBoard";
import BugReports from "./pages/BugReports";
import JenkinsIntegrationSettings from "./pages/JenkinsIntegrationSettings";
import ProjectDelivery from "./pages/ProjectDelivery";

function RootGuard() {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Landing />;
  }
  return <Layout />;
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <Routes>
            <Route path="/landing" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/auth/github/callback" element={<GithubCallback />} />

            <Route
              path="/"
              element={<RootGuard />}
            >
              <Route index element={<Dashboard />} />
              <Route
                path="projects/:id"
                element={
                  <ProtectedRoute>
                    <ProjectDetail />
                  </ProtectedRoute>
                }
              />
              <Route
                path="projects/:id/board"
                element={
                  <ProtectedRoute>
                    <TaskBoard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="projects/:id/delivery"
                element={<ProtectedRoute><ProjectDelivery /></ProtectedRoute>}
              />
              <Route
                path="bugs"
                element={<ProtectedRoute><BugReports /></ProtectedRoute>}
              />
              <Route
                path="team"
                element={
                  <ProtectedRoute>
                    <Team />
                  </ProtectedRoute>
                }
              />
              <Route
                path="settings/github"
                element={
                  <ProtectedRoute>
                    <GithubIntegrationSettings />
                  </ProtectedRoute>
                }
              />
              <Route
                path="settings/jenkins"
                element={
                  <ProtectedRoute>
                    <JenkinsIntegrationSettings />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* Catch-all redirect to Root */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}
