import readline from "node:readline";
import { randomUUID } from "node:crypto";
import fs from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";
import {
  BatchInfo,
  ClassicRunner,
  Configuration,
  Eyes,
  MatchLevel,
  RectangleSize,
  Target,
  VisualGridRunner,
  RunnerOptions,
  BrowserType,
  DeviceName,
  ScreenOrientation,
} from "@applitools/eyes-playwright";
import { chromium, firefox, webkit } from "playwright";
import { buildRemoteLaunchDescriptor } from "./worker-provider.mjs";

const sessions = new Map();
const browsers = new Map();
const screenModules = new Map();
const screenRootDirectories = [
  path.resolve(process.cwd(), "src", "test", "resources", "playwright", "screens"),
  path.resolve(process.cwd(), "playwright", "screens"),
];
const BROWSERSTACK_EXECUTOR_PREFIX = "browserstack_executor:";
const LAMBDATEST_ACTION_PREFIX = "lambdatest_action:";
const LEGACY_LAMBDATEST_NAME_PREFIX = "lambda-name=";
const LEGACY_LAMBDATEST_STATUS_PREFIX = "lambda-status=";
const LEGACY_LAMBDATEST_COMMENT_PREFIX = "lambda-comment=";

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

function resolveBrowserType(browserName) {
  const normalizedBrowserName = normalizeBrowserName(browserName);
  return normalizedBrowserName === "firefox"
    ? firefox
    : normalizedBrowserName === "webkit"
      ? webkit
      : chromium;
}

async function getBrowser(browserName) {
  return getBrowserWithConfig(browserName, {});
}

async function getBrowserWithConfig(browserName, browserConfig = {}) {
  const remoteLaunchDescriptor = buildRemoteLaunchDescriptor(
    browserName,
    browserConfig,
    browserConfig.executionProvider || {},
    browserConfig.userPersona || ""
  );
  const browserKey = JSON.stringify({
    browserName: normalizeBrowserName(browserName),
    channel: browserConfig.channel || null,
    executablePath: browserConfig.executablePath || null,
    headless: browserConfig.headless ?? true,
    proxy: browserConfig.launchOptions?.proxy || null,
    launchArgs: browserConfig.launchArgs || [],
    remoteMode: remoteLaunchDescriptor?.mode || null,
    remoteEndpoint: remoteLaunchDescriptor?.wsEndpoint || null,
  });
  if (browsers.has(browserKey)) {
    return browsers.get(browserKey);
  }

  const browser = remoteLaunchDescriptor
    ? await connectRemoteBrowser(browserName, remoteLaunchDescriptor)
    : await launchLocalBrowser(browserName, browserConfig);
  browsers.set(browserKey, browser);
  return browser;
}

async function launchLocalBrowser(browserName, browserConfig) {
  const browserType = resolveBrowserType(browserName);
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
  return browserType.launch(launchConfig);
}

async function connectRemoteBrowser(browserName, remoteLaunchDescriptor) {
  if (remoteLaunchDescriptor.mode !== "connect") {
    throw new Error(`Unsupported Playwright worker remote mode: ${remoteLaunchDescriptor.mode}`);
  }

  const browserType = resolveRemoteBrowserType(browserName);
  return browserType.connect(remoteLaunchDescriptor.wsEndpoint);
}

function resolveRemoteBrowserType(browserName) {
  const normalizedBrowserName = normalizeBrowserName(browserName);
  if (normalizedBrowserName === "chromium") {
    return chromium;
  }
  return chromium;
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

function addVisualProperties(eyes, customProperties = {}) {
  for (const [key, value] of Object.entries(customProperties)) {
    eyes.addProperty(key, value);
  }
}

async function openVisualSession(session, request) {
  const runner = request.useUfg
    ? new VisualGridRunner(new RunnerOptions().testConcurrency(request.testConcurrency))
    : new ClassicRunner();
  const eyes = new Eyes(runner);
  const configuration = new Configuration();
  configuration.setServerUrl(request.serverUrl);
  configuration.setApiKey(request.apiKey);
  configuration.setBranchName(request.branchName);
  configuration.setEnvironmentName(request.environmentName);
  configuration.setMatchLevel(MatchLevel[request.defaultMatchLevel] || MatchLevel.Strict);
  configuration.setSaveNewTests(request.saveNewTests);
  if (request.baselineEnvName) {
    configuration.setBaselineEnvName(request.baselineEnvName);
  }
  configuration.setBatch(new BatchInfo(request.batchMetadata.name));
  if (request.useUfg) {
    addUfgTargets(configuration, request.ufgTargets || []);
  }
  eyes.setConfiguration(configuration);
  eyes.setIsDisabled(!request.enabled);
  addVisualProperties(eyes, request.customProperties);
  await eyes.open(
    getCurrentPage(session),
    request.appName,
    request.testName,
    new RectangleSize(request.viewportSize.width, request.viewportSize.height),
  );
  session.visualSession = {
    eyes,
    runner,
    disabled: !request.enabled,
  };
}

function getVisualSession(session) {
  if (!session.visualSession) {
    throw new Error("Visual session is not open");
  }
  return session.visualSession;
}

async function checkVisualWindow(session, payload) {
  const visualSession = getVisualSession(session);
  let target = Target.window();
  if (payload.fully) {
    target = target.fully();
  }
  if (payload.matchLevel) {
    target = target.matchLevel(MatchLevel[payload.matchLevel] || MatchLevel.Strict);
  }
  await visualSession.eyes.check(payload.tag, target);
}

function addUfgTargets(configuration, ufgTargets) {
  for (const target of ufgTargets) {
    if (target.browserType) {
      configuration.addBrowser(target.width, target.height, BrowserType[target.browserType]);
      continue;
    }
    configuration.addDeviceEmulation(target.deviceName, ScreenOrientation[target.screenOrientation || "PORTRAIT"]);
  }
}

function toVisualResultPayload(result) {
  return {
    name: result?.name ?? null,
    appName: result?.appName ?? null,
    batchName: result?.batchName ?? null,
    batchId: result?.batchId ?? null,
    branchName: result?.branchName ?? null,
    hostOS: result?.hostOS ?? null,
    hostApp: result?.hostApp ?? null,
    duration: result?.duration ?? 0,
    steps: result?.steps ?? 0,
    matches: result?.matches ?? 0,
    mismatches: result?.mismatches ?? 0,
    missing: result?.missing ?? 0,
    url: result?.url ?? null,
    isNew: result?.isNew ?? null,
    status: result?.status ?? "Passed",
  };
}

function toVisualResultEntry(container) {
  return {
    browserInfo: container.browserInfo ? JSON.stringify(container.browserInfo) : null,
    testResults: toVisualResultPayload(container.testResults),
  };
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

function normalizeCookie(cookie) {
  const normalizedCookie = {
    name: cookie.name,
    value: cookie.value,
  };
  if (cookie.url) {
    normalizedCookie.url = cookie.url;
  } else {
    normalizedCookie.path = cookie.path || "/";
    if (cookie.domain) {
      normalizedCookie.domain = cookie.domain;
    }
  }
  if (cookie.expires) {
    normalizedCookie.expires = cookie.expires;
  }
  if (cookie.secure !== undefined) {
    normalizedCookie.secure = cookie.secure;
  }
  if (cookie.httpOnly !== undefined) {
    normalizedCookie.httpOnly = cookie.httpOnly;
  }
  if (cookie.sameSite) {
    normalizedCookie.sameSite = cookie.sameSite;
  }
  return normalizedCookie;
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

async function getScreenSize(page) {
  return page.evaluate(() => ({
    width: window.screen?.availWidth || window.innerWidth || document.documentElement.clientWidth || 1920,
    height: window.screen?.availHeight || window.innerHeight || document.documentElement.clientHeight || 1080,
  }));
}

async function countLocatorMatches(locator, timeoutMs = 0) {
  if (!timeoutMs || timeoutMs <= 0) {
    return locator.count();
  }

  const startedAt = Date.now();
  while (Date.now() - startedAt <= timeoutMs) {
    const count = await locator.count();
    if (count > 0) {
      return count;
    }
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
  return locator.count();
}

async function isFileInput(locator) {
  return locator.evaluate((node) => {
    return node instanceof HTMLInputElement && node.type === "file";
  }).catch(() => false);
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
    const entry = {
      level: message.type(),
      message: message.text(),
      timestamp: Date.now(),
    };
    session.consoleEntries.push(entry);
    session.consoleMessages.push(`[${message.type()}] ${message.text()}`);
  });
  page.on("pageerror", (error) => {
    const message = error.message || String(error);
    session.consoleEntries.push({
      level: "error",
      message,
      timestamp: Date.now(),
    });
    session.consoleMessages.push(`[pageerror] ${message}`);
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
    case "text":
      return root.getByText(value, { exact: true });
    case "role": {
      const [role, accessibleName] = value.split("|", 2);
      return root.getByRole(role, { name: accessibleName });
    }
    case "testId":
      return root.getByTestId(value);
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

function isCloudControlScript(script) {
  return script.startsWith(BROWSERSTACK_EXECUTOR_PREFIX)
    || script.startsWith(LAMBDATEST_ACTION_PREFIX)
    || script.startsWith(LEGACY_LAMBDATEST_NAME_PREFIX)
    || script.startsWith(LEGACY_LAMBDATEST_STATUS_PREFIX)
    || script.startsWith(LEGACY_LAMBDATEST_COMMENT_PREFIX);
}

async function executeCloudControlScript(session, script) {
  if (script.startsWith(BROWSERSTACK_EXECUTOR_PREFIX) || script.startsWith(LAMBDATEST_ACTION_PREFIX)) {
    return getCurrentPage(session).evaluate(() => {}, script);
  }
  if (script.startsWith(LEGACY_LAMBDATEST_NAME_PREFIX)) {
    return getCurrentPage(session).evaluate(() => {}, buildLambdaTestNameCommand(script.slice(LEGACY_LAMBDATEST_NAME_PREFIX.length)));
  }
  if (script.startsWith(LEGACY_LAMBDATEST_STATUS_PREFIX)) {
    session.pendingLambdaTestStatus = script.slice(LEGACY_LAMBDATEST_STATUS_PREFIX.length);
    return session.pendingLambdaTestStatus;
  }
  if (script.startsWith(LEGACY_LAMBDATEST_COMMENT_PREFIX)) {
    const status = session.pendingLambdaTestStatus || "passed";
    session.pendingLambdaTestStatus = null;
    return getCurrentPage(session).evaluate(
      () => {},
      buildLambdaTestStatusCommand(status, script.slice(LEGACY_LAMBDATEST_COMMENT_PREFIX.length)),
    );
  }
  return null;
}

function buildLambdaTestNameCommand(testName) {
  return `${LAMBDATEST_ACTION_PREFIX} ${JSON.stringify({
    action: "setTestName",
    arguments: { name: testName },
  })}`;
}

function buildLambdaTestStatusCommand(status, remark) {
  return `${LAMBDATEST_ACTION_PREFIX} ${JSON.stringify({
    action: "setTestStatus",
    arguments: { status, remark },
  })}`;
}

async function loadScreenModule(screenModulePath) {
  const normalizedPath = path.normalize(screenModulePath);
  const candidatePaths = screenRootDirectories
    .map((screenRootDirectory) => ({
      rootDirectory: screenRootDirectory,
      absolutePath: path.resolve(screenRootDirectory, normalizedPath),
    }))
    .filter(({ rootDirectory, absolutePath }) => absolutePath.startsWith(rootDirectory));

  if (!candidatePaths.length) {
    throw new Error(`Invalid screen module path: ${screenModulePath}`);
  }

  const existingModule = await (async () => {
    for (const candidate of candidatePaths) {
      try {
        await fs.access(candidate.absolutePath);
        return candidate.absolutePath;
      } catch {
      }
    }
    return null;
  })();

  if (!existingModule) {
    throw new Error(`Unable to find screen module: ${screenModulePath}`);
  }

  if (screenModules.has(existingModule)) {
    return screenModules.get(existingModule);
  }
  const importedModule = await import(pathToFileURL(existingModule).href);
  screenModules.set(existingModule, importedModule);
  return importedModule;
}

function getScreenAction(screenModule, actionName) {
  const action = screenModule[actionName];
  if (typeof action !== "function") {
    throw new Error(`Unsupported screen action: ${actionName}`);
  }
  return action;
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
        const browserConfig = {
          ...(payload.browserConfig || {}),
          userPersona: payload.userPersona,
        };
        const browser = await getBrowserWithConfig(payload.browserName, browserConfig);
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
        const contextOptions = { ...(browserConfig.contextOptions || {}) };
        if (harPath) {
          contextOptions.recordHar = { path: harPath };
        }
        const context = await browser.newContext(contextOptions);
        if (tracePath) {
          await context.tracing.start({ screenshots: true, snapshots: true, sources: true });
        }
        const page = await context.newPage();
        const consoleMessages = [];
        const consoleEntries = [];
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
          windowPosition: { x: 0, y: 0 },
          lastWindowSize: null,
          navigationTimeoutMs: 30000,
          tracePath,
          harPath,
          consoleLogPath,
          consoleMessages,
          consoleEntries,
          pendingLambdaTestStatus: null,
          visualSession: null,
        };
        context.setDefaultNavigationTimeout(session.navigationTimeoutMs);
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
          traceFile: tracePath ? path.basename(tracePath) : "",
          harFile: harPath ? path.basename(harPath) : "",
          consoleFile: consoleLogPath ? path.basename(consoleLogPath) : "",
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
        session.lastWindowSize = size;
        process.stdout.write(`${okResponse(requestId, action, size)}\n`);
        break;
      }
      case "getWindowPosition": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, session.windowPosition)}\n`);
        break;
      }
      case "setWindowSize": {
        const session = getSession(payload.sessionId);
        await getCurrentPage(session).setViewportSize({ width: payload.width, height: payload.height });
        session.lastWindowSize = { width: payload.width, height: payload.height };
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "setWindowPosition": {
        const session = getSession(payload.sessionId);
        session.windowPosition = { x: payload.x, y: payload.y };
        process.stdout.write(`${okResponse(requestId, action, session.windowPosition)}\n`);
        break;
      }
      case "maximizeWindow": {
        const session = getSession(payload.sessionId);
        const size = await getScreenSize(getCurrentPage(session));
        await getCurrentPage(session).setViewportSize(size);
        session.lastWindowSize = size;
        process.stdout.write(`${okResponse(requestId, action, size)}\n`);
        break;
      }
      case "minimizeWindow": {
        const session = getSession(payload.sessionId);
        session.lastWindowSize = session.lastWindowSize || (await getViewportSize(getCurrentPage(session)));
        const minimizedSize = { width: 240, height: 160 };
        await getCurrentPage(session).setViewportSize(minimizedSize);
        process.stdout.write(`${okResponse(requestId, action, minimizedSize)}\n`);
        break;
      }
      case "fullscreenWindow": {
        const session = getSession(payload.sessionId);
        const size = await getScreenSize(getCurrentPage(session));
        await getCurrentPage(session).setViewportSize(size);
        session.lastWindowSize = size;
        process.stdout.write(`${okResponse(requestId, action, size)}\n`);
        break;
      }
      case "setNavigationTimeout": {
        const session = getSession(payload.sessionId);
        session.navigationTimeoutMs = payload.timeoutMs;
        session.context.setDefaultNavigationTimeout(payload.timeoutMs);
        process.stdout.write(`${okResponse(requestId, action, { timeoutMs: payload.timeoutMs })}\n`);
        break;
      }
      case "addCookie": {
        const session = getSession(payload.sessionId);
        await session.context.addCookies([normalizeCookie(payload.cookie)]);
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "getCookies": {
        const session = getSession(payload.sessionId);
        const cookies = await session.context.cookies();
        process.stdout.write(`${okResponse(requestId, action, { cookies })}\n`);
        break;
      }
      case "deleteCookieNamed": {
        const session = getSession(payload.sessionId);
        const remainingCookies = (await session.context.cookies()).filter((cookie) => cookie.name !== payload.name);
        await session.context.clearCookies();
        if (remainingCookies.length > 0) {
          await session.context.addCookies(remainingCookies.map(normalizeCookie));
        }
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "deleteAllCookies": {
        const session = getSession(payload.sessionId);
        await session.context.clearCookies();
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
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
      case "getConsoleLogs": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, { entries: session.consoleEntries })}\n`);
        break;
      }
      case "countElements": {
        const session = getSession(payload.sessionId);
        const locator = buildLocatorRaw(getCurrentRoot(session), payload.locator);
        process.stdout.write(
          `${okResponse(requestId, action, { count: await countLocatorMatches(locator, payload.timeoutMs || 0) })}\n`,
        );
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
            if (await isFileInput(locator)) {
              await locator.setInputFiles(payload.value);
            } else {
              await locator.click();
              await locator.pressSequentially(payload.value);
            }
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
        if (isCloudControlScript(payload.script)) {
          const value = await executeCloudControlScript(session, payload.script);
          process.stdout.write(`${okResponse(requestId, action, { value: value ?? null })}\n`);
          break;
        }
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
      case "screenAction": {
        const session = getSession(payload.sessionId);
        const screenModule = await loadScreenModule(payload.screenModule);
        const screenAction = getScreenAction(screenModule, payload.actionName);
        const value = await screenAction(
          {
            session,
            context: session.context,
            page: getCurrentPage(session),
            root: getCurrentRoot(session),
          },
          ...(payload.arguments || []),
        );
        process.stdout.write(`${okResponse(requestId, action, { value: value ?? null })}\n`);
        break;
      }
      case "visualOpen": {
        const session = getSession(payload.sessionId);
        await openVisualSession(session, payload.request);
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "visualCheckWindow": {
        const session = getSession(payload.sessionId);
        await checkVisualWindow(session, payload);
        process.stdout.write(`${okResponse(requestId, action, { status: "ok" })}\n`);
        break;
      }
      case "visualStatus": {
        const session = getSession(payload.sessionId);
        process.stdout.write(`${okResponse(requestId, action, {
          disabled: !session.visualSession || session.visualSession.disabled,
        })}\n`);
        break;
      }
      case "visualClose": {
        const session = getSession(payload.sessionId);
        if (!session.visualSession || session.visualSession.disabled) {
          process.stdout.write(`${okResponse(requestId, action, { disabled: true })}\n`);
          break;
        }
        if (session.visualSession.runner?.type === "ufg") {
          await session.visualSession.eyes.close(false);
          const summary = await session.visualSession.runner.getAllTestResults(false);
          session.visualSession = null;
          process.stdout.write(`${okResponse(requestId, action, {
            entries: summary.getAllResults().map(toVisualResultEntry),
          })}\n`);
          break;
        }
        const result = await session.visualSession.eyes.close(false);
        session.visualSession = null;
        process.stdout.write(`${okResponse(requestId, action, toVisualResultPayload(result))}\n`);
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
