import readline from "node:readline";
import { randomUUID } from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import { chromium, firefox, webkit } from "playwright";

const sessions = new Map();
const browsers = new Map();

function normalizeBrowserName(browserName) {
  switch ((browserName || "chromium").toLowerCase()) {
    case "chrome":
    case "chromium":
      return "chromium";
    case "firefox":
      return "firefox";
    case "safari":
    case "webkit":
      return "webkit";
    default:
      return "chromium";
  }
}

async function getBrowser(browserName) {
  return getBrowserWithConfig(browserName, {});
}

async function getBrowserWithConfig(browserName, browserConfig = {}) {
  const normalizedBrowserName = normalizeBrowserName(browserName);
  const browserKey = JSON.stringify({
    browserName: normalizedBrowserName,
    channel: browserConfig.channel || null,
    executablePath: browserConfig.executablePath || null,
    headless: browserConfig.headless ?? true,
    proxy: browserConfig.launchOptions?.proxy || null,
    launchArgs: browserConfig.launchArgs || [],
  });
  if (browsers.has(browserKey)) {
    return browsers.get(browserKey);
  }

  const browserType =
    normalizedBrowserName === "firefox"
      ? firefox
      : normalizedBrowserName === "webkit"
        ? webkit
        : chromium;
  const launchConfig = {
    headless: browserConfig.headless ?? true,
  };
  if (browserConfig.channel) {
    launchConfig.channel = browserConfig.channel;
  }
  if (browserConfig.executablePath) {
    launchConfig.executablePath = browserConfig.executablePath;
  }
  if (browserConfig.launchArgs?.length) {
    launchConfig.args = browserConfig.launchArgs;
  }
  if (browserConfig.launchOptions?.proxy) {
    launchConfig.proxy = browserConfig.launchOptions.proxy;
  }

  const browser = await browserType.launch(launchConfig);
  browsers.set(browserKey, browser);
  return browser;
}

async function finalizeSessionArtifacts(session) {
  if (!session) {
    return;
  }
  if (session.consoleLogPath) {
    await fs.writeFile(session.consoleLogPath, `${session.consoleMessages.join("\n")}\n`, "utf8");
  }
  if (session.tracePath) {
    await session.context.tracing.stop({ path: session.tracePath }).catch(() => {});
  }
}

async function closeSessionResources(session) {
  await finalizeSessionArtifacts(session);
  for (const page of session.pages.values()) {
    await page.close().catch(() => {});
  }
  await session.context.close().catch(() => {});
}

function getSession(sessionId) {
  const session = sessions.get(sessionId);
  if (!session) {
    throw new Error(`Unknown session: ${sessionId}`);
  }
  return session;
}

function getCurrentPage(session) {
  const page = session.pages.get(session.currentPageId);
  if (!page) {
    throw new Error(`Unknown active page for session: ${session.sessionId}`);
  }
  return page;
}

function getCurrentRoot(session) {
  return session.currentFrame || getCurrentPage(session);
}

function getPendingDialog(session) {
  if (!session.pendingDialog) {
    throw new Error("No alert present");
  }
  return session.pendingDialog;
}

async function getViewportSize(page) {
  const viewport = page.viewportSize();
  if (viewport) {
    return viewport;
  }
  return page.evaluate(() => ({
    width: window.innerWidth || document.documentElement.clientWidth || 0,
    height: window.innerHeight || document.documentElement.clientHeight || 0,
  }));
}

function registerPage(session, page, pageId = `page-${randomUUID()}`) {
  for (const [existingHandle, existingPage] of session.pages.entries()) {
    if (existingPage === page) {
      session.currentPageId = existingHandle;
      return existingHandle;
    }
  }
  session.pages.set(pageId, page);
  session.currentPageId = pageId;
  return pageId;
}

function attachPageObservers(session, page) {
  page.on("console", (message) => {
    session.consoleMessages.push(`[${message.type()}] ${message.text()}`);
  });
  page.on("pageerror", (error) => {
    session.consoleMessages.push(`[pageerror] ${error.message || String(error)}`);
  });
  page.on("close", () => {
    for (const [pageId, trackedPage] of session.pages.entries()) {
      if (trackedPage === page) {
        session.pages.delete(pageId);
        if (session.currentPageId === pageId && session.pages.size > 0) {
          session.currentPageId = session.pages.keys().next().value;
        }
        break;
      }
    }
  });
}

function buildLocatorRaw(root, locatorReference) {
  const parent = locatorReference.parent ? buildLocator(root, locatorReference.parent) : root;
  return createLocator(parent, locatorReference);
}

function buildLocator(root, locatorReference) {
  return buildLocatorRaw(root, locatorReference).nth(locatorReference.index || 0);
}

async function resolveScriptArg(root, arg) {
  if (arg === null || arg === undefined) {
    return null;
  }
  if (typeof arg !== "object" || Array.isArray(arg)) {
    return arg;
  }
  if (arg.type === "element") {
    const handle = await buildLocator(root, arg.locator).elementHandle();
    if (!handle) {
      throw new Error("Unable to resolve element argument for executeScript");
    }
    return handle;
  }
  return arg;
}

function createLocator(root, locatorReference) {
  const { strategy, value } = locatorReference;
  switch (strategy) {
    case "id":
      return root.locator(`#${value}`);
    case "css":
      return root.locator(value);
    case "xpath":
      return root.locator(`xpath=${value}`);
    case "className":
      return root.locator(`.${value}`);
    case "name":
      return root.locator(`[name="${value}"]`);
    case "tagName":
      return root.locator(value);
    case "linkText":
      return root.getByText(value, { exact: true });
    case "partialLinkText":
      return root.getByText(value, { exact: false });
    default:
      throw new Error(`Unsupported locator strategy: ${strategy}`);
  }
}

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

rl.on("line", async (line) => {
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

  try {
    switch (action) {
      case "ping":
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      case "createSession": {
        const browser = await getBrowserWithConfig(payload.browserName, payload.browserConfig || {});
        const sessionId = randomUUID();
        const artifactPath = payload.artifactPath || null;
        if (artifactPath) {
          await fs.mkdir(artifactPath, { recursive: true });
        }
        const tracePath = artifactPath ? path.join(artifactPath, `${payload.userPersona}-${sessionId}-trace.zip`) : null;
        const harPath = artifactPath ? path.join(artifactPath, `${payload.userPersona}-${sessionId}-network.har`) : null;
        const consoleLogPath = artifactPath
          ? path.join(artifactPath, `${payload.userPersona}-${sessionId}-console.log`)
          : null;
        const contextOptions = { ...(payload.browserConfig?.contextOptions || {}) };
        if (harPath) {
          contextOptions.recordHar = { path: harPath };
        }
        const context = await browser.newContext(contextOptions);
        if (tracePath) {
          await context.tracing.start({ screenshots: true, snapshots: true, sources: true });
        }
        const page = await context.newPage();
        const consoleMessages = [];
        const session = {
          sessionId,
          userPersona: payload.userPersona,
          browserName: payload.browserName,
          contextId: `context-${randomUUID()}`,
          context,
          pages: new Map(),
          currentPageId: null,
          currentFrame: null,
          pendingDialog: null,
          tracePath,
          harPath,
          consoleLogPath,
          consoleMessages,
        };
        context.on("dialog", (dialog) => {
          session.pendingDialog = {
            dialog,
            type: dialog.type(),
            message: dialog.message(),
            defaultValue: dialog.defaultValue(),
          };
        });
        attachPageObservers(session, page);
        const pageId = registerPage(session, page);
        context.on("page", (newPage) => {
          attachPageObservers(session, newPage);
          registerPage(session, newPage);
        });
        sessions.set(session.sessionId, session);
        process.stdout.write(`${okResponse(requestId, action, {
          sessionId: session.sessionId,
          userPersona: session.userPersona,
          browserName: session.browserName,
          contextId: session.contextId,
          pageId,
        })}\n`);
        break;
      }
      case "navigateTo": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).goto(payload.url, { waitUntil: "load" });
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "getCurrentUrl": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { url: getCurrentPage(session).url() })}\n`);
        break;
      }
      case "getTitle": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { title: await getCurrentPage(session).title() })}\n`);
        break;
      }
      case "getPageSource": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { content: await getCurrentPage(session).content() })}\n`);
        break;
      }
      case "getWindowHandles": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { handles: [...session.pages.keys()] })}\n`);
        break;
      }
      case "getWindowHandle": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { handle: session.currentPageId })}\n`);
        break;
      }
      case "switchToWindow": {
        const session = getSession(payload.sessionId);
        if (!session.pages.has(payload.handle)) {
          throw new Error(`Unknown window handle: ${payload.handle}`);
        }
        session.currentPageId = payload.handle;
        session.currentFrame = null;
        process.stdout.write(`${okResponse(requestId, action, { handle: session.currentPageId })}\n`);
        break;
      }
      case "openNewWindow": {
        const session = getSession(payload.sessionId);
        const page = await session.context.newPage();
        const handle = registerPage(session, page);
        session.currentFrame = null;
        process.stdout.write(`${okResponse(requestId, action, { handle })}\n`);
        break;
      }
      case "switchToFrame": {
        const session = getSession(payload.sessionId);
        const root = getCurrentRoot(session);
        const frameElement = await root
          .locator(`iframe#${payload.nameOrId}, iframe[name="${payload.nameOrId}"], frame#${payload.nameOrId}, frame[name="${payload.nameOrId}"]`)
          .first()
          .elementHandle();
        const frame = frameElement ? await frameElement.contentFrame() : null;
        if (!frame) {
          throw new Error(`Unable to switch to frame: ${payload.nameOrId}`);
        }
        session.currentFrame = frame;
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "switchToFrameByIndex": {
        const session = getSession(payload.sessionId);
        const root = getCurrentRoot(session);
        const frameElement = await root.locator("iframe, frame").nth(payload.index).elementHandle();
        const frame = frameElement ? await frameElement.contentFrame() : null;
        if (!frame) {
          throw new Error(`Unable to switch to frame at index: ${payload.index}`);
        }
        session.currentFrame = frame;
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "switchToFrameElement": {
        const session = getSession(payload.sessionId);
        const root = getCurrentRoot(session);
        const frameElement = await buildLocator(root, payload.locator).elementHandle();
        const frame = frameElement ? await frameElement.contentFrame() : null;
        if (!frame) {
          throw new Error("Unable to switch to the provided frame element");
        }
        session.currentFrame = frame;
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "switchToDefaultContent": {
        const session = getSession(payload.sessionId);
        session.currentFrame = null;
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "switchToParentFrame": {
        const session = getSession(payload.sessionId);
        if (!session.currentFrame) {
          process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
          break;
        }
        const parentFrame = session.currentFrame.parentFrame();
        session.currentFrame = parentFrame && parentFrame !== getCurrentPage(session).mainFrame() ? parentFrame : null;
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "getWindowSize": {
        const session = getSession(payload.sessionId);
        const size = await getViewportSize(getCurrentPage(session));
        process.stdout.write(`${okResponse(requestId, action, size)}\n`);
        break;
      }
      case "getWindowPosition": {
        process.stdout.write(`${okResponse(requestId, action, { x: 0, y: 0 })}\n`);
        break;
      }
      case "setWindowSize": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).setViewportSize({ width: payload.width, height: payload.height });
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "maximizeWindow": {
        const session = getSession(payload.sessionId);
        const size = await getCurrentPage(session).evaluate(() => ({
          width: window.screen?.availWidth || window.innerWidth || document.documentElement.clientWidth || 1920,
          height: window.screen?.availHeight || window.innerHeight || document.documentElement.clientHeight || 1080,
        }));
        await getCurrentPage(session).setViewportSize(size);
        process.stdout.write(`${okResponse(requestId, action, size)}\n`);
        break;
      }
      case "getAlert": {
        const session = getSession(payload.sessionId);
        const pendingDialog = getPendingDialog(session);
        process.stdout.write(`${okResponse(requestId, action, {
          type: pendingDialog.type,
          message: pendingDialog.message,
          defaultValue: pendingDialog.defaultValue,
        })}\n`);
        break;
      }
      case "acceptAlert": {
        const session = getSession(payload.sessionId);
        const pendingDialog = getPendingDialog(session);
        await pendingDialog.dialog.accept(payload.text);
        session.pendingDialog = null;
        process.stdout.write(`${okResponse(requestId, action, { status: "accepted" })}\n`);
        break;
      }
      case "dismissAlert": {
        const session = getSession(payload.sessionId);
        const pendingDialog = getPendingDialog(session);
        await pendingDialog.dialog.dismiss();
        session.pendingDialog = null;
        process.stdout.write(`${okResponse(requestId, action, { status: "dismissed" })}\n`);
        break;
      }
      case "screenshot": {
        const session = getSession(payload.sessionId);
        const screenshot = await getCurrentPage(session).screenshot({ type: "png" });
        process.stdout.write(`${okResponse(requestId, action, { base64: screenshot.toString("base64") })}\n`);
        break;
      }
      case "countElements": {
        const session = getSession(payload.sessionId);
        const locator = buildLocatorRaw(getCurrentRoot(session), payload.locator);
        process.stdout.write(`${okResponse(requestId, action, { count: await locator.count() })}\n`);
        break;
      }
      case "elementAction": {
        const session = getSession(payload.sessionId);
        const locator = buildLocator(getCurrentRoot(session), payload.locator);
        let value;
        switch (payload.elementAction) {
          case "click":
            await locator.click({ noWaitAfter: true });
            value = true;
            break;
          case "type":
            await locator.click();
            await locator.pressSequentially(payload.value);
            value = true;
            break;
          case "clear":
            await locator.clear();
            value = true;
            break;
          case "getText":
            value = await locator.textContent();
            break;
          case "getTagName":
            value = await locator.evaluate((node) => node.tagName.toLowerCase());
            break;
          case "getAttribute":
            value = payload.value === "value" && (await locator.evaluate((node) => "value" in node))
              ? await locator.inputValue().catch(async () => locator.getAttribute(payload.value))
              : await locator.getAttribute(payload.value);
            break;
          case "isVisible":
            value = await locator.isVisible();
            break;
          case "isEnabled":
            value = await locator.isEnabled();
            break;
          case "isSelected":
            value = await locator.isChecked().catch(() => false);
            break;
          case "getCssValue":
            value = await locator.evaluate((node, propertyName) => window.getComputedStyle(node).getPropertyValue(propertyName), payload.value);
            break;
          case "getBoundingBox": {
            const box = await locator.boundingBox();
            value = box
              ? {
                  x: Math.round(box.x),
                  y: Math.round(box.y),
                  width: Math.round(box.width),
                  height: Math.round(box.height),
                }
              : { x: 0, y: 0, width: 0, height: 0 };
            break;
          }
          default:
            throw new Error(`Unsupported element action: ${payload.elementAction}`);
        }
        process.stdout.write(`${okResponse(requestId, action, { value })}\n`);
        break;
      }
      case "executeScript": {
        const session = getSession(payload.sessionId);
        const root = getCurrentRoot(session);
        const scriptArgs = await Promise.all((payload.args || []).map((arg) => resolveScriptArg(root, arg)));
        const value = await root.evaluate(
          ([script, args]) => {
            const executor = new Function("args", `return (function() { ${script} }).apply(null, args);`);
            return executor(args);
          },
          [payload.script, scriptArgs],
        );
        process.stdout.write(`${okResponse(requestId, action, { value })}\n`);
        break;
      }
      case "goBack": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).goBack();
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "goForward": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).goForward();
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "refresh": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).reload({ waitUntil: "load" });
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "closeSession": {
        const session = getSession(payload.sessionId);
        await closeSessionResources(session);
        sessions.delete(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { status: "closed" })}\n`);
        break;
      }
      case "shutdown":
        for (const session of sessions.values()) {
          await closeSessionResources(session);
        }
        sessions.clear();
        for (const browser of browsers.values()) {
          await browser.close().catch(() => {});
        }
        browsers.clear();
        process.stdout.write(`${okResponse(requestId, action, { status: "bye" })}\n`);
        rl.close();
        break;
      default:
        process.stdout.write(`${errorResponse(requestId, action, `Unsupported action: ${action}`)}\n`);
        break;
    }
  } catch (error) {
    process.stdout.write(`${errorResponse(requestId, action, error.message || String(error))}\n`);
  }
});

rl.on("close", () => {
  process.exit(0);
});
