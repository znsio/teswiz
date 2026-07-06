import readline from "node:readline";
import { randomUUID } from "node:crypto";

const sessions = new Map();

function okResponse(requestId, action, payload = {}) {
  return JSON.stringify({ requestId, action, ok: true, payload });
}

function errorResponse(requestId, action, message) {
  return JSON.stringify({ requestId, action, ok: false, payload: { message } });
}

const rl = readline.createInterface({
  input: process.stdin,
  crlfDelay: Infinity,
});

rl.on("line", (line) => {
  if (!line || !line.trim()) {
    return;
  }

  let request;
  try {
    request = JSON.parse(line);
  } catch (error) {
    process.stdout.write(`${errorResponse("unknown", "unknown", "Invalid JSON request")}\n`);
    return;
  }

  const { requestId, action, payload = {} } = request;

  switch (action) {
    case "ping":
      process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
      break;
    case "createSession": {
      const session = {
        sessionId: randomUUID(),
        userPersona: payload.userPersona,
        browserName: payload.browserName,
        contextId: `context-${randomUUID()}`,
        pageId: `page-${randomUUID()}`,
      };
      sessions.set(session.sessionId, session);
      process.stdout.write(`${okResponse(requestId, action, session)}\n`);
      break;
    }
    case "shutdown":
      process.stdout.write(`${okResponse(requestId, action, { status: "bye" })}\n`);
      rl.close();
      break;
    default:
      process.stdout.write(`${errorResponse(requestId, action, `Unsupported action: ${action}`)}\n`);
      break;
  }
});

rl.on("close", () => {
  process.exit(0);
});
