import { useEffect, useRef, useState } from "react";
import { streamChat } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import {
  Sparkles,
  X,
  Send,
  Minus,
  Bot,
  RotateCcw,
} from "lucide-react";

const WELCOME_MESSAGE = {
  role: "assistant",
  content:
    "Hi, I'm NeuroForge AI. I can help you check your projects and tasks, review progress, or create a new task — just tell me what you need.",
};

// Used only when the real /chat endpoint can't be reached at all (backend
// offline, Ollama not running, etc.) so the widget still demonstrates its
// behavior in the app's existing "standalone mode" spirit instead of just
// throwing an error at the user.
function mockReplyFor(message) {
  const m = message.toLowerCase();
  if (m.includes("task") && (m.includes("create") || m.includes("add") || m.includes("new"))) {
    return "I'd normally walk you through creating a task here — which project should it go in? (Note: I'm currently offline, so this is a simulated reply. Start the backend and Ollama to talk to the real NeuroForge AI.)";
  }
  if (m.includes("project")) {
    return "I can usually list your projects and their progress once connected to the backend. Right now the NeuroForge AI service isn't reachable, so this is a simulated response — check that the Spring Boot backend and Ollama are running.";
  }
  return "I can't reach the NeuroForge AI service right now (the backend or the Ollama model server may be offline), so here's a simulated placeholder reply. Once both are running, ask me again and I'll use your real project and task data.";
}

async function simulateStream(text, onChunk) {
  const words = text.split(" ");
  let full = "";
  for (let i = 0; i < words.length; i++) {
    const delta = (i === 0 ? "" : " ") + words[i];
    full += delta;
    onChunk(delta, full);
    // eslint-disable-next-line no-await-in-loop
    await new Promise((resolve) => setTimeout(resolve, 18));
  }
  return full;
}

export default function ChatWidget() {
  const { isAuthenticated } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([WELCOME_MESSAGE]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const scrollRef = useRef(null);
  const abortRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, open]);

  useEffect(() => {
    // Cancel any in-flight stream if the widget unmounts (e.g. logout).
    return () => abortRef.current?.abort();
  }, []);

  if (!isAuthenticated) return null;

  function resetChat() {
    abortRef.current?.abort();
    setMessages([WELCOME_MESSAGE]);
    setError("");
    setSending(false);
  }

  async function handleSend(e) {
    e.preventDefault();
    const text = input.trim();
    if (!text || sending) return;

    setError("");
    setInput("");
    setMessages((prev) => [
      ...prev,
      { role: "user", content: text },
      { role: "assistant", content: "" },
    ]);
    setSending(true);

    function applyChunk(_delta, full) {
      setMessages((prev) => {
        const next = [...prev];
        next[next.length - 1] = { role: "assistant", content: full };
        return next;
      });
    }

    const controller = new AbortController();
    abortRef.current = controller;

    try {
      const full = await streamChat(text, {
        onChunk: applyChunk,
        signal: controller.signal,
      });
      if (!full) {
        applyChunk("", "Hmm, I didn't get a response back. Try rephrasing that?");
      }
    } catch (err) {
      if (err?.name === "AbortError") return;
      // Backend/AI model unreachable — fall back to a simulated reply so the
      // widget still feels alive in standalone mode.
      try {
        await simulateStream(mockReplyFor(text), applyChunk);
        setError("Running in standalone mode — the NeuroForge AI backend isn't reachable.");
      } catch {
        /* aborted mid-fallback, ignore */
      }
    } finally {
      setSending(false);
      abortRef.current = null;
    }
  }

  return (
    <>
      {!open && (
        <button
          className="chat-fab"
          onClick={() => setOpen(true)}
          aria-label="Open NeuroForge AI chat"
          title="Ask NeuroForge AI"
        >
          <Sparkles size={22} />
        </button>
      )}

      {open && (
        <div className="chat-panel">
          <div className="chat-panel-header">
            <div className="chat-panel-title">
              <div className="chat-panel-icon">
                <Bot size={16} />
              </div>
              <div>
                <div className="chat-panel-name">NeuroForge AI</div>
                <div className="chat-panel-sub">Project & task assistant</div>
              </div>
            </div>
            <div className="chat-panel-actions">
              <button
                type="button"
                className="chat-icon-btn"
                onClick={resetChat}
                title="Reset conversation"
              >
                <RotateCcw size={14} />
              </button>
              <button
                type="button"
                className="chat-icon-btn"
                onClick={() => setOpen(false)}
                title="Minimize"
              >
                <Minus size={14} />
              </button>
              <button
                type="button"
                className="chat-icon-btn"
                onClick={() => {
                  abortRef.current?.abort();
                  setOpen(false);
                }}
                title="Close"
              >
                <X size={14} />
              </button>
            </div>
          </div>

          <div className="chat-panel-body" ref={scrollRef}>
            {messages.map((msg, i) => (
              <div
                key={i}
                className={`chat-bubble-row ${msg.role === "user" ? "self" : ""}`}
              >
                <div className={`chat-bubble ${msg.role === "user" ? "chat-bubble-user" : "chat-bubble-ai"}`}>
                  {msg.content || (
                    <span className="chat-typing">
                      <span />
                      <span />
                      <span />
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>

          {error && <div className="chat-panel-error">{error}</div>}

          <form className="chat-panel-input-row" onSubmit={handleSend}>
            <input
              type="text"
              placeholder="Ask about your tasks or projects…"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={sending}
              autoFocus
            />
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={sending || !input.trim()}
              aria-label="Send message"
            >
              {sending ? <span className="loading-spinner" /> : <Send size={14} />}
            </button>
          </form>
        </div>
      )}
    </>
  );
}
