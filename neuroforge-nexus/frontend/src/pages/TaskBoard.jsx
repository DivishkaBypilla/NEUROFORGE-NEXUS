import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../lib/api";
import { useAuth, initialsFor } from "../context/AuthContext";
import {
  useDirectory,
  nameFor,
  projectAssignableUsers,
  canManageProjectTasks,
  isUserAssignedToProject,
} from "../lib/useDirectory";
import TaskDetail from "../components/TaskDetail";
import TaskListView from "../components/TaskListView";
import TaskCalendarView from "../components/TaskCalendarView";
import {
  ArrowLeft,
  Plus,
  KanbanSquare,
  CheckCircle2,
  Clock,
  ListTodo,
  AlertCircle,
  X,
  Check,
  ChevronDown,
  ChevronUp,
  Circle,
  Rows3,
  CalendarDays,
} from "lucide-react";

const VIEWS = [
  { key: "board", label: "Board", icon: KanbanSquare },
  { key: "list", label: "List", icon: Rows3 },
  { key: "calendar", label: "Calendar", icon: CalendarDays },
];

const COLUMNS = [
  { status: "TODO", label: "To Do", icon: ListTodo, color: "var(--accent-cyan)" },
  { status: "IN_PROGRESS", label: "In Progress", icon: Clock, color: "var(--accent-primary)" },
  { status: "DONE", label: "Done", icon: CheckCircle2, color: "var(--accent-emerald)" },
];

export default function TaskBoard() {
  const { id: projectId } = useParams();
  const { user } = useAuth();
  const { users } = useDirectory();

  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState(null);
  const [error, setError] = useState("");
  const [selectedTaskId, setSelectedTaskId] = useState(null);
  const [showNewTask, setShowNewTask] = useState(false);
  const [creating, setCreating] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newDescription, setNewDescription] = useState("");
  const [newAssignee, setNewAssignee] = useState("");
  const [newDueDate, setNewDueDate] = useState("");
  const [view, setView] = useState("board");

  // Card expandable subtask dropdown state
  const [expandedTasks, setExpandedTasks] = useState({});
  const [quickSubtaskTitles, setQuickSubtaskTitles] = useState({});

  async function load() {
    setError("");
    try {
      const [projectData, taskData] = await Promise.all([
        api.get(`/projects/${projectId}`),
        api.get(`/tasks/project/${projectId}`),
      ]);
      setProject(projectData);
      setTasks(taskData || []);
    } catch (err) {
      setError(err.message || "Could not load project kanban board.");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId]);

  const canManage = canManageProjectTasks(user, project);
  const assignable = projectAssignableUsers(users, project);

  function upsertTask(updated) {
    setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
  }

  function removeTask(taskId) {
    setTasks((prev) => prev.filter((t) => t.id !== taskId));
    setSelectedTaskId(null);
  }

  async function handleCreateTask(e) {
    e.preventDefault();
    if (!newTitle.trim()) return;
    setCreating(true);
    setError("");
    try {
      const created = await api.post("/tasks", {
        projectId,
        title: newTitle.trim(),
        description: newDescription || null,
        assigneeId: newAssignee || null,
        dueDate: newDueDate || null,
      });
      setTasks((prev) => [...(prev || []), created]);
      setNewTitle("");
      setNewDescription("");
      setNewAssignee("");
      setNewDueDate("");
      setShowNewTask(false);
    } catch (err) {
      setError(err.message || "Could not create task.");
    } finally {
      setCreating(false);
    }
  }

  async function quickStatusChange(task, status) {
    setError("");
    try {
      const updated = await api.patch(`/tasks/${task.id}/status`, { status });
      upsertTask(updated);
    } catch (err) {
      setError(err.message || "Could not update status.");
    }
  }

  function toggleExpandTask(taskId) {
    setExpandedTasks((prev) => ({ ...prev, [taskId]: !prev[taskId] }));
  }

  async function handleToggleSubtaskOnCard(task, subtaskId, currentStatus) {
    const nextStatus = currentStatus === "DONE" ? "TODO" : "DONE";
    try {
      const updated = await api.patch(
        `/tasks/${task.id}/subtasks/${subtaskId}/status`,
        { status: nextStatus }
      );
      upsertTask(updated);
    } catch (err) {
      setError(err.message || "Could not update subtask.");
    }
  }

  async function handleAddSubtaskOnCard(e, taskId) {
    e.preventDefault();
    const title = quickSubtaskTitles[taskId] || "";
    if (!title.trim()) return;
    try {
      const updated = await api.post(`/tasks/${taskId}/subtasks`, {
        title: title.trim(),
        assigneeId: null,
      });
      upsertTask(updated);
      setQuickSubtaskTitles((prev) => ({ ...prev, [taskId]: "" }));
    } catch (err) {
      setError(err.message || "Could not add subtask.");
    }
  }

  if (error && !project) {
    return (
      <>
        <Link to={`/projects/${projectId}`} className="btn btn-sm" style={{ marginBottom: 20 }}>
          <ArrowLeft size={16} />
          <span>Back to Project Overview</span>
        </Link>
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{error}</span>
        </div>
      </>
    );
  }

  if (!project || !tasks) {
    return (
      <div className="empty-state">
        <span className="loading-spinner" style={{ width: 32, height: 32 }} />
        <p style={{ marginTop: 12 }}>Loading Kanban task board…</p>
      </div>
    );
  }

  if (!isUserAssignedToProject(user, project)) {
    return (
      <>
        <Link to="/" className="btn btn-sm" style={{ marginBottom: 20, display: "inline-flex" }}>
          <ArrowLeft size={16} />
          <span>Back to Projects</span>
        </Link>
        <div className="alert alert-error" style={{ marginTop: 12 }}>
          <AlertCircle size={18} flexShrink={0} />
          <span>Access Denied: You are not assigned to this project task board.</span>
        </div>
      </>
    );
  }

  const selectedTask = tasks.find((t) => t.id === selectedTaskId) || null;

  return (
    <>
      <Link
        to={`/projects/${projectId}`}
        className="btn btn-sm"
        style={{ marginBottom: 20, display: "inline-flex" }}
      >
        <ArrowLeft size={16} />
        <span>Back to Project Overview</span>
      </Link>

      <div className="page-header" style={{ marginBottom: 24 }}>
        <div>
          <h1 className="page-title">
            <KanbanSquare size={26} style={{ color: "var(--accent-primary)" }} />
            <span>{project.name} — Task Board</span>
          </h1>
          <p className="page-sub">
            {canManage
              ? "Create, prioritize, and assign tasks across engineering workflow stages."
              : "Track task progress and update status for your assigned items."}
          </p>
        </div>

        {canManage && (
          <button
            className="btn btn-primary"
            onClick={() => setShowNewTask((v) => !v)}
          >
            <Plus size={16} />
            <span>{showNewTask ? "Cancel Creation" : "Create Task"}</span>
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} flexShrink={0} />
          <span>{error}</span>
        </div>
      )}

      {/* New Task Inline Form */}
      {showNewTask && (
        <form
          onSubmit={handleCreateTask}
          style={{
            background: "var(--card-bg)",
            backdropFilter: "blur(16px)",
            border: "var(--glass-border)",
            borderRadius: "var(--radius-lg)",
            padding: 24,
            marginBottom: 28,
          }}
        >
          <h3 style={{ fontSize: 16, fontWeight: 700, color: "var(--text-main)", marginBottom: 16 }}>
            Create New Kanban Task
          </h3>
          <div className="field">
            <label>Task Title *</label>
            <input
              placeholder="e.g. Implement OAuth Security Handler"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="field">
            <label>Description</label>
            <textarea
              placeholder="Task details and acceptance criteria…"
              value={newDescription}
              onChange={(e) => setNewDescription(e.target.value)}
            />
          </div>
          <div className="field">
            <label>Assignee</label>
            <select
              value={newAssignee}
              onChange={(e) => setNewAssignee(e.target.value)}
            >
              <option value="">— Unassigned —</option>
              {assignable.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} ({u.role})
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Due Date</label>
            <input
              type="date"
              value={newDueDate}
              onChange={(e) => setNewDueDate(e.target.value)}
            />
          </div>
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 10 }}>
            <button
              className="btn btn-primary"
              type="submit"
              disabled={creating || !newTitle.trim()}
            >
              {creating ? (
                <span className="loading-spinner" />
              ) : (
                <>
                  <Check size={16} />
                  <span>Create Task</span>
                </>
              )}
            </button>
          </div>
        </form>
      )}

      {/* View Switcher */}
      <div className="view-switcher">
        {VIEWS.map((v) => {
          const IconComponent = v.icon;
          return (
            <button
              key={v.key}
              type="button"
              className={`view-switcher-btn ${view === v.key ? "active" : ""}`}
              onClick={() => setView(v.key)}
            >
              <IconComponent size={14} />
              <span>{v.label}</span>
            </button>
          );
        })}
      </div>

      {view === "list" && (
        <TaskListView tasks={tasks} users={users} onSelectTask={setSelectedTaskId} />
      )}

      {view === "calendar" && (
        <TaskCalendarView tasks={tasks} users={users} onSelectTask={setSelectedTaskId} />
      )}

      {/* Kanban Board Grid */}
      {view === "board" && (
      <div className="kanban-board">
        {COLUMNS.map((col) => {
          const colTasks = tasks.filter((t) => t.status === col.status);
          const IconComponent = col.icon;
          return (
            <div className="kanban-col" key={col.status}>
              <div className="kanban-col-header">
                <div className="kanban-col-title">
                  <IconComponent size={16} style={{ color: col.color }} />
                  <span>{col.label}</span>
                </div>
                <span className="count-badge">{colTasks.length}</span>
              </div>

              {colTasks.length === 0 ? (
                <div
                  style={{
                    padding: "36px 12px",
                    textAlign: "center",
                    color: "var(--text-dim)",
                    fontSize: 13,
                  }}
                >
                  No tasks in {col.label.toLowerCase()}
                </div>
              ) : (
                colTasks.map((task) => {
                  const canEditStatus =
                    canManage || task.assigneeId === user?.id;
                  const isExpanded = !!expandedTasks[task.id];
                  const doneCount =
                    task.subtasks?.filter((s) => s.status === "DONE").length || 0;
                  const totalSubtasks = task.subtasks?.length || 0;

                  return (
                    <div
                      className="kanban-card"
                      key={task.id}
                      onClick={() => setSelectedTaskId(task.id)}
                    >
                      <div className="kanban-card-title">{task.title}</div>

                      <div className="kanban-card-footer">
                        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                          <span
                            className="avatar"
                            style={{ width: 22, height: 22, fontSize: 10 }}
                          >
                            {task.assigneeId
                              ? initialsFor(nameFor(users, task.assigneeId))
                              : "?"}
                          </span>
                          <span>
                            {task.assigneeId
                              ? nameFor(users, task.assigneeId)
                              : "Unassigned"}
                          </span>
                        </div>

                        {/* Subtask Accordion Dropdown Trigger */}
                        <button
                          type="button"
                          className={`card-subtask-badge ${isExpanded ? "active" : ""}`}
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleExpandTask(task.id);
                          }}
                          title="Toggle Subtasks Dropdown Checklist"
                        >
                          <ListTodo size={12} />
                          <span>
                            {totalSubtasks > 0 ? `${doneCount}/${totalSubtasks}` : "Subtasks"}
                          </span>
                          {isExpanded ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
                        </button>
                      </div>

                      {/* Dropped Down Subtasks Accordion Content */}
                      {isExpanded && (
                        <div
                          className="card-subtasks-dropdown"
                          onClick={(e) => e.stopPropagation()}
                        >
                          <div className="card-subtasks-header">
                            <span>Subtask Checklist ({doneCount}/{totalSubtasks})</span>
                          </div>

                          {(!task.subtasks || task.subtasks.length === 0) && (
                            <div className="card-subtasks-empty">
                              No subtasks added yet.
                            </div>
                          )}

                          {task.subtasks && task.subtasks.length > 0 && (
                            <div className="card-subtasks-list">
                              {task.subtasks.map((s) => {
                                const isDone = s.status === "DONE";
                                return (
                                  <div key={s.id} className="card-subtask-row">
                                    <button
                                      type="button"
                                      className="card-subtask-check"
                                      onClick={() => handleToggleSubtaskOnCard(task, s.id, s.status)}
                                    >
                                      {isDone ? (
                                        <CheckCircle2 size={14} style={{ color: "var(--accent-emerald)" }} />
                                      ) : (
                                        <Circle size={14} style={{ color: "var(--text-dim)" }} />
                                      )}
                                    </button>
                                    <span className={`card-subtask-title ${isDone ? "done" : ""}`}>
                                      {s.title}
                                    </span>
                                    {s.assigneeId && (
                                      <span className="card-subtask-assignee">
                                        {nameFor(users, s.assigneeId)}
                                      </span>
                                    )}
                                  </div>
                                );
                              })}
                            </div>
                          )}

                          {canManage && (
                            <form
                              onSubmit={(e) => handleAddSubtaskOnCard(e, task.id)}
                              className="card-subtask-add-form"
                            >
                              <input
                                type="text"
                                placeholder="+ Add subtask…"
                                value={quickSubtaskTitles[task.id] || ""}
                                onChange={(e) =>
                                  setQuickSubtaskTitles((prev) => ({
                                    ...prev,
                                    [task.id]: e.target.value,
                                  }))
                                }
                                className="card-subtask-input"
                              />
                              <button type="submit" className="btn btn-sm btn-primary" style={{ padding: "4px 8px" }}>
                                <Plus size={12} />
                              </button>
                            </form>
                          )}
                        </div>
                      )}

                      {canEditStatus && (
                        <select
                          className="mono"
                          value={task.status}
                          onClick={(e) => e.stopPropagation()}
                          onChange={(e) => quickStatusChange(task, e.target.value)}
                          style={{
                            width: "100%",
                            marginTop: 6,
                            background: "var(--card-bg)",
                            fontSize: 11,
                            padding: "5px 8px",
                            borderRadius: "var(--radius-sm)",
                            borderColor: "var(--card-border)",
                          }}
                        >
                          {COLUMNS.map((c) => (
                            <option key={c.status} value={c.status}>
                              Move to: {c.label}
                            </option>
                          ))}
                        </select>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          );
        })}
      </div>
      )}

      {/* Task Details Modal Overlay */}
      {selectedTask && (
        <TaskDetail
          task={selectedTask}
          project={project}
          users={users}
          currentUser={user}
          canManage={canManage}
          onClose={() => setSelectedTaskId(null)}
          onTaskUpdated={upsertTask}
          onTaskDeleted={removeTask}
        />
      )}
    </>
  );
}
