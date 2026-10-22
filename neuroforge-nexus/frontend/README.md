# NeuroForge Nexus — Platform & Team Collaboration

**NeuroForge Nexus** is a modern, enterprise-grade SDLC workspace and team collaboration platform designed for high-velocity software engineering delivery. Built with React 19, Vite, and a custom vanilla CSS design system, it provides visual Kanban workflows, fine-grained role governance (RBAC), multi-project portfolio tracking, and interactive subtask checklists.

---

## 🌟 Key Features & Recent Enhancements

### 1. Default Public Landing Experience
* **Root Route Landing (`/`)**: Unauthenticated visitors land directly on the Public Landing Page featuring engineering highlights, security architecture, role rosters, and dark/light mode preview.
* **Seamless Workspace Entry**: Direct navigation to Sign In (`/login`) and Account Creation (`/signup`), redirecting authenticated users automatically into the workspace dashboard (`/`).

### 2. Fine-Grained Project Access Control (RBAC)
* **Project Membership Guards**: Standard users (Developers, Testers, Project Managers) are strictly restricted to projects where they are assigned as **Owner**, **Team Lead**, or **Member**.
* **Administrative Governance**: Platform Admins retain full visibility across all workspace project portfolios.
* **Direct URL Guards**: Unauthorized deep-link navigation to `/projects/:id` or `/projects/:id/board` presents an Access Denied guard screen.

### 3. Visual Kanban & Subtask Workflows
* **Drag-Free Kanban Boards**: Interactive columns (**TO DO**, **IN PROGRESS**, **DONE**) with instant status transition controls.
* **Actionable Subtask Checklists**: Break down complex engineering tasks into subtasks with live progress indicators.

### 4. Workspace Team Directory
* **Search & Role Filtering**: Filter team members instantly by name, email, or role (**ADMIN**, **PROJECT_MANAGER**, **DEVELOPER**, **TESTER**, **DEVOPS_ENGINEER**).
* **Role Management**: Admins can update team member roles directly from the directory table.
* **One-Click ID Copying**: Copy unique user IDs for assignment and integration.

### 5. High-Contrast Light & Dark Theme System
* **Dual Theme Support**: Smooth dark/light mode toggle with theme persistence saved to `localStorage` (`neuroforge_theme`).
* **Enhanced UI Contrast**: Refined color combinations for all interactive elements, ensuring high-contrast visibility on button hover states (e.g., *Create Project*, *Cancel Creation*, *Back to Projects*).
* **CSS Compatibility**: Fully compliant CSS with standard vendor prefixes (`line-clamp`, `background-clip`).

### 6. Standardized Enterprise Authentication
* **Clean Sign-In Flow**: Enforced standard credential authentication by removing legacy quick demo sign-in options across Login and Landing pages.

---

## 🛠️ Technology Stack

* **Core Framework**: React 19 + Vite
* **Routing**: `react-router-dom` v7 with nested layouts and route guards
* **Styling**: Vanilla CSS (`src/index.css`) utilizing design tokens, glassmorphism, dynamic gradients, and CSS custom properties
* **State & Context**:
  * `AuthContext`: Authentication session state, persistent credentials, and role identities.
  * `ThemeContext`: Dynamic Light/Dark mode state management.
* **Iconography**: `lucide-react`

---

## 📁 Project Structure

```
d:\Neuroforge-frontend-main/
├── src/
│   ├── components/
│   │   ├── Layout.jsx          # Workspace shell header, sidebar, theme toggle, and sign out
│   │   ├── ProtectedRoute.jsx  # Authentication route guard
│   │   ├── ProjectForm.jsx     # Create/Edit project modal
│   │   └── TaskDetail.jsx      # Task details modal & subtask checklist editor
│   ├── context/
│   │   ├── AuthContext.jsx     # Session management & user profile identity
│   │   └── ThemeContext.jsx    # Theme switching & localStorage persistence
│   ├── lib/
│   │   ├── api.js              # REST API client
│   │   ├── mockBackend.js      # In-memory mock backend server fallback
│   │   └── useDirectory.js     # User directory hook & RBAC assignment helper
│   ├── pages/
│   │   ├── Dashboard.jsx       # Workspace projects overview & status metrics
│   │   ├── Landing.jsx         # Public product landing page
│   │   ├── Login.jsx           # Sign in page
│   │   ├── ProjectDetail.jsx   # Detailed project roster & metadata
│   │   ├── Signup.jsx          # User registration page
│   │   ├── TaskBoard.jsx       # Kanban task board
│   │   └── Team.jsx            # Workspace team directory
│   ├── App.jsx                 # Application route tree & RootGuard
│   ├── index.css               # Global design tokens, theme variables, & layout styles
│   └── main.jsx                # Application entry point
├── package.json
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
* Node.js (v18 or higher recommended)
* npm or yarn

### Installation & Local Setup

1. **Clone or navigate to the repository directory**:
   ```bash
   cd d:\Neuroforge-frontend-main
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Start the development server**:
   ```bash
   npm run dev
   ```
   Open `http://localhost:3000` (or the URL shown in your terminal) to view the application.

4. **Build for production**:
   ```bash
   npm run build
   ```

---

## 🔒 Access Control Roster Summary

| Role | Workspace Scope | Permissions |
| :--- | :--- | :--- |
| **ADMIN** | Global Workspace | Full access to all projects, Kanban boards, team roles, and settings. |
| **PROJECT_MANAGER** | Assigned Projects | Manage assigned project configurations, team assignments, and tasks. |
| **DEVELOPER** | Assigned Projects | View assigned projects, update task statuses, and manage subtasks. |
| **TESTER** | Assigned Projects | View assigned projects, test tasks, and log/update task progress. |
