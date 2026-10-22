import { useMemo, useState } from "react";
import { nameFor } from "../lib/useDirectory";
import {
  ListTodo,
  Clock,
  CheckCircle2,
  Circle,
  ChevronUp,
  ChevronDown,
} from "lucide-react";

const STATUS_META = {
  TODO: { label: "To Do", color: "var(--accent-cyan)", icon: ListTodo },
  IN_PROGRESS: { label: "In Progress", color: "var(--accent-primary)", icon: Clock },
  DONE: { label: "Done", color: "var(--accent-emerald)", icon: CheckCircle2 },
};

const FILTERS = ["ALL", "TODO", "IN_PROGRESS", "DONE"];
const FILTER_LABEL = { ALL: "All", TODO: "To Do", IN_PROGRESS: "In Progress", DONE: "Done" };

function formatDueDate(dueDate) {
  if (!dueDate) return null;
  const d = new Date(dueDate + "T00:00:00");
  if (Number.isNaN(d.getTime())) return null;
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

function isOverdue(dueDate, status) {
  if (!dueDate || status === "DONE") return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return new Date(dueDate + "T00:00:00") < today;
}

export default function TaskListView({ tasks, users, onSelectTask }) {
  const [filter, setFilter] = useState("ALL");
  const [sortDesc, setSortDesc] = useState(false);

  const filtered = useMemo(() => {
    const list =
      filter === "ALL" ? tasks : tasks.filter((t) => t.status === filter);
    return [...list].sort((a, b) => {
      const aDate = a.dueDate ? new Date(a.dueDate).getTime() : Infinity;
      const bDate = b.dueDate ? new Date(b.dueDate).getTime() : Infinity;
      return sortDesc ? bDate - aDate : aDate - bDate;
    });
  }, [tasks, filter, sortDesc]);

  return (
    <div>
      <div className="list-view-toolbar">
        <div className="list-view-filters">
          {FILTERS.map((f) => {
            const count =
              f === "ALL" ? tasks.length : tasks.filter((t) => t.status === f).length;
            return (
              <button
                key={f}
                type="button"
                className={`list-filter-chip ${filter === f ? "active" : ""}`}
                onClick={() => setFilter(f)}
              >
                {FILTER_LABEL[f]}
                <span className="list-filter-count">{count}</span>
              </button>
            );
          })}
        </div>

        <button
          type="button"
          className="btn btn-sm"
          onClick={() => setSortDesc((v) => !v)}
          title="Toggle due date sort order"
        >
          <span>Due Date</span>
          {sortDesc ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
        </button>
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state" style={{ padding: "48px 12px" }}>
          <ListTodo size={28} style={{ color: "var(--text-dim)" }} />
          <p style={{ marginTop: 10 }}>No tasks match this filter.</p>
        </div>
      ) : (
        <div className="task-list-table">
          <div className="task-list-header-row">
            <span>Task</span>
            <span>Status</span>
            <span>Assignee</span>
            <span>Due</span>
            <span>Subtasks</span>
          </div>

          {filtered.map((task) => {
            const meta = STATUS_META[task.status] || STATUS_META.TODO;
            const StatusIcon = meta.icon;
            const doneCount = task.subtasks?.filter((s) => s.status === "DONE").length || 0;
            const totalSubtasks = task.subtasks?.length || 0;
            const dueLabel = formatDueDate(task.dueDate);
            const overdue = isOverdue(task.dueDate, task.status);

            return (
              <div
                key={task.id}
                className="task-list-row"
                onClick={() => onSelectTask(task.id)}
              >
                <span className="task-list-title" title={task.title}>
                  {task.title}
                </span>

                <span className="task-list-status" style={{ color: meta.color }}>
                  <StatusIcon size={14} />
                  {meta.label}
                </span>

                <span className="task-list-assignee">
                  {task.assigneeId ? nameFor(users, task.assigneeId) : "Unassigned"}
                </span>

                <span className={`task-list-due ${overdue ? "overdue" : ""}`}>
                  {dueLabel || "—"}
                </span>

                <span className="task-list-subtasks">
                  {totalSubtasks > 0 ? (
                    <>
                      {doneCount === totalSubtasks ? (
                        <CheckCircle2 size={13} style={{ color: "var(--accent-emerald)" }} />
                      ) : (
                        <Circle size={13} style={{ color: "var(--text-dim)" }} />
                      )}
                      {doneCount}/{totalSubtasks}
                    </>
                  ) : (
                    "—"
                  )}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
