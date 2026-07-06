import readline from "node:readline";
import { randomUUID } from "node:crypto";
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

function getSession(sessionId) {
  const session = sessions.get(sessionId);
  if (!session) {
    throw new Error(`Unknown session: ${sessionId}`);
  }
  return session;
}

function buildLocatorRaw(root, locatorReference) {
  const parent = locatorReference.parent ? buildLocator(root, locatorReference.parent) : root;
  return createLocator(parent, locatorReference);
}

function buildLocator(root, locatorReference) {
  return buildLocatorRaw(root, locatorReference).nth(locatorReference.index || 0);
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
        const context = await browser.newContext(payload.browserConfig?.contextOptions || {});
        const page = await context.newPage();
        const session = {
          sessionId: randomUUID(),
          userPersona: payload.userPersona,
          browserName: payload.browserName,
          contextId: `context-${randomUUID()}`,
          pageId: `page-${randomUUID()}`,
          context,
          page,
        };
        sessions.set(session.sessionId, session);
        process.stdout.write(`${okResponse(requestId, action, {
          sessionId: session.sessionId,
          userPersona: session.userPersona,
          browserName: session.browserName,
          contextId: session.contextId,
          pageId: session.pageId,
        })}\n`);
        break;
      }
      case "navigateTo": {
        const session = getSession(payload.sessionId);
        await session.page.goto(payload.url, { waitUntil: "load" });
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "getCurrentUrl": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { url: session.page.url() })}\n`);
        break;
      }
      case "getTitle": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { title: await session.page.title() })}\n`);
        break;
      }
      case "getPageSource": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { content: await session.page.content() })}\n`);
        break;
      }
      case "screenshot": {
        const session = getSession(payload.sessionId);
        const screenshot = await session.page.screenshot({ type: "png" });
        process.stdout.write(`${okResponse(requestId, action, { base64: screenshot.toString("base64") })}\n`);
        break;
      }
      case "countElements": {
        const session = getSession(payload.sessionId);
        const locator = buildLocatorRaw(session.page, payload.locator);
        process.stdout.write(`${okResponse(requestId, action, { count: await locator.count() })}\n`);
        break;
      }
      case "elementAction": {
        const session = getSession(payload.sessionId);
        const locator = buildLocator(session.page, payload.locator);
        let value;
        switch (payload.elementAction) {
          case "click":
            await locator.click();
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
          default:
            throw new Error(`Unsupported element action: ${payload.elementAction}`);
        }
        process.stdout.write(`${okResponse(requestId, action, { value })}\n`);
        break;
      }
      case "executeScript": {
        const session = getSession(payload.sessionId);
        const value = await session.page.evaluate(payload.script);
        process.stdout.write(`${okResponse(requestId, action, { value })}\n`);
        break;
      }
      case "goBack": {
        const session = getSession(payload.sessionId);
        await session.page.goBack();
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "goForward": {
        const session = getSession(payload.sessionId);
        await session.page.goForward();
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "refresh": {
        const session = getSession(payload.sessionId);
        await session.page.reload({ waitUntil: "load" });
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "closeSession": {
        const session = getSession(payload.sessionId);
        await session.page.close();
        await session.context.close();
        sessions.delete(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { status: "closed" })}\n`);
        break;
      }
      case "shutdown":
        for (const session of sessions.values()) {
          await session.page.close().catch(() => {});
          await session.context.close().catch(() => {});
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
