import { useMemo, useState } from "react";
import { nameFor } from "../lib/useDirectory";
import { ChevronLeft, ChevronRight, CalendarX2 } from "lucide-react";

const STATUS_COLOR = {
  TODO: "var(--accent-cyan)",
  IN_PROGRESS: "var(--accent-primary)",
  DONE: "var(--accent-emerald)",
};

const WEEKDAY_LABELS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MAX_CHIPS_PER_DAY = 3;

function toDateKey(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

// Builds a 6x7 grid of Date objects covering the given month, padded with
// the trailing days of the previous month and the leading days of the next
// month so every week row is complete (standard month-calendar layout).
function buildMonthGrid(year, month) {
  const firstOfMonth = new Date(year, month, 1);
  const startOffset = firstOfMonth.getDay();
  const gridStart = new Date(year, month, 1 - startOffset);

  const days = [];
  for (let i = 0; i < 42; i++) {
    days.push(new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + i));
  }
  return days;
}

export default function TaskCalendarView({ tasks, users, onSelectTask }) {
  const today = new Date();
  const [cursor, setCursor] = useState(new Date(today.getFullYear(), today.getMonth(), 1));

  const tasksByDate = useMemo(() => {
    const map = new Map();
    for (const task of tasks) {
      if (!task.dueDate) continue;
      const key = task.dueDate.slice(0, 10);
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(task);
    }
    return map;
  }, [tasks]);

  const unscheduled = useMemo(() => tasks.filter((t) => !t.dueDate), [tasks]);

  const days = useMemo(
    () => buildMonthGrid(cursor.getFullYear(), cursor.getMonth()),
    [cursor]
  );

  const monthLabel = cursor.toLocaleDateString(undefined, {
    month: "long",
    year: "numeric",
  });

  const todayKey = toDateKey(today);

  return (
    <div>
      <div className="calendar-toolbar">
        <div className="calendar-nav">
          <button
            type="button"
            className="btn btn-sm"
            onClick={() => setCursor((c) => new Date(c.getFullYear(), c.getMonth() - 1, 1))}
            aria-label="Previous month"
          >
            <ChevronLeft size={16} />
          </button>
          <span className="calendar-month-label">{monthLabel}</span>
          <button
            type="button"
            className="btn btn-sm"
            onClick={() => setCursor((c) => new Date(c.getFullYear(), c.getMonth() + 1, 1))}
            aria-label="Next month"
          >
            <ChevronRight size={16} />
          </button>
        </div>
        <button
          type="button"
          className="btn btn-sm"
          onClick={() => setCursor(new Date(today.getFullYear(), today.getMonth(), 1))}
        >
          Today
        </button>
      </div>

      <div className="calendar-grid">
        {WEEKDAY_LABELS.map((w) => (
          <div key={w} className="calendar-weekday">
            {w}
          </div>
        ))}

        {days.map((day) => {
          const key = toDateKey(day);
          const inMonth = day.getMonth() === cursor.getMonth();
          const dayTasks = tasksByDate.get(key) || [];
          const overflow = dayTasks.length - MAX_CHIPS_PER_DAY;

          return (
            <div
              key={key}
              className={`calendar-cell ${inMonth ? "" : "outside"} ${key === todayKey ? "today" : ""}`}
            >
              <div className="calendar-cell-date">{day.getDate()}</div>
              <div className="calendar-cell-tasks">
                {dayTasks.slice(0, MAX_CHIPS_PER_DAY).map((task) => (
                  <button
                    key={task.id}
                    type="button"
                    className="calendar-task-chip"
                    style={{ borderLeftColor: STATUS_COLOR[task.status] || "var(--accent-cyan)" }}
                    onClick={() => onSelectTask(task.id)}
                    title={`${task.title}${task.assigneeId ? " — " + nameFor(users, task.assigneeId) : ""}`}
                  >
                    {task.title}
                  </button>
                ))}
                {overflow > 0 && (
                  <span className="calendar-task-overflow">+{overflow} more</span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <div className="calendar-unscheduled">
        <div className="calendar-unscheduled-header">
          <CalendarX2 size={14} />
          <span>Unscheduled ({unscheduled.length})</span>
        </div>
        {unscheduled.length === 0 ? (
          <p className="page-sub" style={{ fontSize: 12.5 }}>
            Every task has a due date.
          </p>
        ) : (
          <div className="calendar-unscheduled-list">
            {unscheduled.map((task) => (
              <button
                key={task.id}
                type="button"
                className="calendar-task-chip"
                style={{ borderLeftColor: STATUS_COLOR[task.status] || "var(--accent-cyan)" }}
                onClick={() => onSelectTask(task.id)}
              >
                {task.title}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
