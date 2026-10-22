import { useState } from "react";
import { api } from "../lib/api";
import { nameFor, projectAssignableUsers } from "../lib/useDirectory";
import TaskGithubLinks from "./TaskGithubLinks";
import {
  X,
  Edit3,
  Trash2,
  CheckCircle2,
  Circle,
  Plus,
  UserCheck,
  AlignLeft,
  AlertCircle,
  Check,
} from "lucide-react";

const STATUSES = ["TODO", "IN_PROGRESS", "DONE"];
const STATUS_LABEL = { TODO: "To Do", IN_PROGRESS: "In Progress", DONE: "Done" };

export default function TaskDetail({
  task,
  project,
  users,
  currentUser,
  canManage,
  onClose,
  onTaskUpdated,
  onTaskDeleted,
}) {
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description || "");
  const [assigneeId, setAssigneeId] = useState(task.assigneeId || "");
  const [dueDate, setDueDate] = useState(task.dueDate || "");

  const [subtaskTitle, setSubtaskTitle] = useState("");
  const [subtaskAssignee, setSubtaskAssignee] = useState("");
  const [addingSubtask, setAddingSubtask] = useState(false);

  const assignable = projectAssignableUsers(users, project);
  const canEditThisStatus = canManage || task.assigneeId === currentUser?.id;

  async function saveEdits(e) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const updated = await api.put(`/tasks/${task.id}`, {
        title,
        description: description || null,
        assigneeId: assigneeId || null,
        dueDate: dueDate || null,
      });
      onTaskUpdated(updated);
      setEditing(false);
    } catch (err) {
      setError(err.message || "Could not update task specification.");
    } finally {
      setSaving(false);
    }
  }

  async function changeStatus(status) {
    setError("");
    try {
      const updated = await api.patch(`/tasks/${task.id}/status`, { status });
      onTaskUpdated(updated);
    } catch (err) {
      setError(err.message || "Could not update task status.");
    }
  }

  async function deleteTask() {
    if (!confirm(`Delete task "${task.title}"? All subtasks will be removed.`))
      return;
    try {
      await api.del(`/tasks/${task.id}`);
      onTaskDeleted(task.id);
    } catch (err) {
      setError(err.message || "Could not delete task.");
    }
  }

  async function addSubtask(e) {
    e.preventDefault();
    if (!subtaskTitle.trim()) return;
    setAddingSubtask(true);
    setError("");
    try {
      const updated = await api.post(`/tasks/${task.id}/subtasks`, {
        title: subtaskTitle.trim(),
        assigneeId: subtaskAssignee || null,
      });
      onTaskUpdated(updated);
      setSubtaskTitle("");
      setSubtaskAssignee("");
    } catch (err) {
      setError(err.message || "Could not add subtask.");
    } finally {
      setAddingSubtask(false);
    }
  }

  async function changeSubtaskStatus(subtaskId, status) {
    setError("");
    try {
      const updated = await api.patch(
        `/tasks/${task.id}/subtasks/${subtaskId}/status`,
        { status }
      );
      onTaskUpdated(updated);
    } catch (err) {
      setError(err.message || "Could not update subtask.");
    }
  }

  async function deleteSubtask(subtaskId) {
    try {
      const updated = await api.del(`/tasks/${task.id}/subtasks/${subtaskId}`);
      onTaskUpdated(updated);
    } catch (err) {
      setError(err.message || "Could not remove subtask.");
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Header & Actions */}
        <div
          style={{
            display: "flex",
            alignItems: "flex-start",
            justifyContent: "space-between",
            marginBottom: 20,
            paddingBottom: 14,
            borderBottom: "var(--glass-border)",
          }}
        >
          <div style={{ flex: 1, paddingRight: 16 }}>
            <span className="stat-label" style={{ fontSize: 11 }}>
              TASK DETAILS
            </span>
            {!editing && (
              <h2
                style={{
                  fontSize: 18,
                  fontWeight: 700,
                  color: "var(--text-main)",
                  marginTop: 4,
                }}
              >
                {task.title}
              </h2>
            )}
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            {canManage && !editing && (
              <button
                className="btn btn-sm"
                onClick={() => setEditing(true)}
                title="Edit Task"
              >
                <Edit3 size={14} />
                <span>Edit</span>
              </button>
            )}
            {canManage && (
              <button
                className="btn btn-sm btn-danger"
                onClick={deleteTask}
                title="Delete Task"
              >
                <Trash2 size={14} />
              </button>
            )}
            <button
              className="btn btn-sm"
              onClick={onClose}
              style={{ padding: "6px 8px" }}
              aria-label="Close task details modal"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={16} flexShrink={0} />
            <span>{error}</span>
          </div>
        )}

        {/* Editing Mode */}
        {editing ? (
          <form onSubmit={saveEdits}>
            <div className="field">
              <label>Task Title *</label>
              <input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>
            <div className="field">
              <label>Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>
            <div className="field">
              <label>Assignee</label>
              <select
                value={assigneeId}
                onChange={(e) => setAssigneeId(e.target.value)}
              >
                <option value="">— Unassigned —</option>
                {assignable.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Due Date</label>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
              />
            </div>
            <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
              <button
                className="btn btn-sm"
                type="button"
                onClick={() => setEditing(false)}
              >
                Cancel
              </button>
              <button
                className="btn btn-sm btn-primary"
                type="submit"
                disabled={saving}
              >
                {saving ? (
                  <span className="loading-spinner" />
                ) : (
                  <>
                    <Check size={14} />
                    <span>Save Task</span>
                  </>
                )}
              </button>
            </div>
          </form>
        ) : (
          /* View Mode */
          <div>
            {task.description && (
              <p
                style={{
                  fontSize: 14,
                  color: "var(--text-muted)",
                  lineHeight: 1.6,
                  marginBottom: 20,
                }}
              >
                {task.description}
              </p>
            )}

            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
                gap: 16,
                padding: 16,
                background: "var(--card-bg)",
                borderRadius: "var(--radius-md)",
                border: "1px solid var(--card-border)",
                marginBottom: 24,
              }}
            >
              <div>
                <span className="stat-label" style={{ fontSize: 10 }}>
                  Assigned To
                </span>
                <div
                  style={{
                    fontSize: 13,
                    fontWeight: 600,
                    color: "var(--text-main)",
                    marginTop: 4,
                  }}
                >
                  {task.assigneeId
                    ? nameFor(users, task.assigneeId)
                    : "Unassigned"}
                </div>
              </div>

              <div>
                <span className="stat-label" style={{ fontSize: 10 }}>
                  Current Status
                </span>
                <div style={{ marginTop: 4 }}>
                  <select
                    className="mono"
                    value={task.status}
                    disabled={!canEditThisStatus}
                    onChange={(e) => changeStatus(e.target.value)}
                    style={{
                      padding: "6px 12px",
                      fontSize: 12,
                      width: "auto",
                      background: "var(--panel-bg-solid)",
                      border: "1px solid var(--card-border)",
                      color: "var(--text-main)",
                      borderRadius: "var(--radius-sm)",
                    }}
                  >
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {STATUS_LABEL[s]}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <span className="stat-label" style={{ fontSize: 10 }}>
                  Due Date
                </span>
                <div
                  style={{
                    fontSize: 13,
                    fontWeight: 600,
                    color: "var(--text-main)",
                    marginTop: 4,
                  }}
                >
                  {task.dueDate
                    ? new Date(task.dueDate + "T00:00:00").toLocaleDateString(undefined, {
                        month: "short",
                        day: "numeric",
                        year: "numeric",
                      })
                    : "No due date"}
                </div>
              </div>
            </div>

            {/* Subtask Section */}
            <div style={{ borderTop: "var(--glass-border)", paddingTop: 20 }}>
              <h4
                style={{
                  fontSize: 14,
                  fontWeight: 700,
                  color: "var(--text-main)",
                  marginBottom: 12,
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                }}
              >
                <span>
                  Subtasks Checklist{" "}
                  {task.subtasks?.length
                    ? `(${
                        task.subtasks.filter((s) => s.status === "DONE").length
                      }/${task.subtasks.length})`
                    : ""}
                </span>
              </h4>

              {(!task.subtasks || task.subtasks.length === 0) && (
                <p className="page-sub" style={{ fontSize: 13, marginBottom: 16 }}>
                  No subtasks defined yet.
                </p>
              )}

              {task.subtasks && task.subtasks.length > 0 && (
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: 8,
                    marginBottom: 20,
                  }}
                >
                  {task.subtasks.map((s) => {
                    const canEditSubtask =
                      canManage || s.assigneeId === currentUser?.id;
                    const isDone = s.status === "DONE";
                    return (
                      <div
                        key={s.id}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          gap: 12,
                          padding: "10px 14px",
                          background: "var(--card-bg)",
                          border: "1px solid var(--card-border)",
                          borderRadius: "var(--radius-md)",
                        }}
                      >
                        <button
                          type="button"
                          disabled={!canEditSubtask}
                          onClick={() =>
                            changeSubtaskStatus(s.id, isDone ? "TODO" : "DONE")
                          }
                          style={{
                            color: isDone
                              ? "var(--accent-emerald)"
                              : "var(--text-dim)",
                            display: "flex",
                            cursor: "pointer",
                          }}
                        >
                          {isDone ? (
                            <CheckCircle2 size={18} />
                          ) : (
                            <Circle size={18} />
                          )}
                        </button>

                        <span
                          style={{
                            flex: 1,
                            fontSize: 13,
                            textDecoration: isDone ? "line-through" : "none",
                            color: isDone
                              ? "var(--text-dim)"
                              : "var(--text-main)",
                          }}
                        >
                          {s.title}
                        </span>

                        {s.assigneeId && (
                          <span
                            className="mono"
                            style={{
                              fontSize: 11,
                              color: "var(--text-dim)",
                              background: "rgba(148, 163, 184, 0.12)",
                              padding: "2px 8px",
                              borderRadius: 4,
                            }}
                          >
                            {nameFor(users, s.assigneeId)}
                          </span>
                        )}

                        {canManage && (
                          <button
                            type="button"
                            onClick={() => deleteSubtask(s.id)}
                            style={{
                              color: "var(--accent-rose)",
                              fontSize: 12,
                              padding: 2,
                              cursor: "pointer",
                            }}
                            title="Delete Subtask"
                          >
                            <X size={14} />
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Add Subtask Row */}
              {canManage && (
                <form
                  onSubmit={addSubtask}
                  style={{
                    display: "flex",
                    gap: 10,
                    alignItems: "center",
                    flexWrap: "wrap",
                  }}
                >
                  <input
                    placeholder="Add a new subtask action item…"
                    value={subtaskTitle}
                    onChange={(e) => setSubtaskTitle(e.target.value)}
                    style={{
                      flex: "2 1 200px",
                      background: "var(--card-bg)",
                      border: "1px solid var(--card-border)",
                      color: "var(--text-main)",
                    }}
                  />
                  <select
                    value={subtaskAssignee}
                    onChange={(e) => setSubtaskAssignee(e.target.value)}
                    style={{
                      flex: "1 1 120px",
                      background: "var(--card-bg)",
                      border: "1px solid var(--card-border)",
                      color: "var(--text-main)",
                    }}
                  >
                    <option value="">Unassigned</option>
                    {assignable.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.name}
                      </option>
                    ))}
                  </select>
                  <button
                    className="btn btn-sm btn-primary"
                    type="submit"
                    disabled={!subtaskTitle.trim() || addingSubtask}
                  >
                    {addingSubtask ? (
                      <span className="loading-spinner" />
                    ) : (
                      <>
                        <Plus size={14} />
                        <span>Add</span>
                      </>
                    )}
                  </button>
                </form>
              )}
            </div>

            <TaskGithubLinks task={task} project={project} />
          </div>
        )}
      </div>
    </div>
  );
}
