function normalizeRequestedBrowserName(browserName) {
  switch ((browserName || "chromium").toLowerCase()) {
    case "chrome":
      return "chrome";
    case "edge":
    case "msedge":
      return "edge";
    case "firefox":
      return "playwright-firefox";
    case "safari":
    case "webkit":
      return "playwright-webkit";
    default:
      return "playwright-chromium";
  }
}

function normalizeExecutionProvider(executionProvider = {}) {
  const providerName = (executionProvider.providerName || "local").toLowerCase();
  return {
    providerName,
    remoteUrl: executionProvider.remoteUrl || null,
    apiUrl: executionProvider.apiUrl || null,
    username: executionProvider.username || null,
    accessKey: executionProvider.accessKey || null,
    webCapabilities: executionProvider.webCapabilities || {},
  };
}

function buildRemoteLaunchDescriptor(browserName, browserConfig = {}, executionProvider = {}, userPersona = "") {
  const provider = normalizeExecutionProvider(executionProvider);
  if (!provider.remoteUrl) {
    return null;
  }

  switch (provider.providerName) {
    case "browserstack":
      return {
        mode: "connect",
        wsEndpoint: buildBrowserStackWsEndpoint(browserName, browserConfig, provider, userPersona),
      };
    case "lambdatest":
    case "headspin":
      throw new Error(
        `Playwright TS remote provider '${provider.providerName}' is not yet supported by teswiz worker. ` +
          "The provider config seam is in place, but the provider-specific remote adapter is still pending."
      );
    default:
      throw new Error(`Unsupported Playwright TS remote provider: ${provider.providerName}`);
  }
}

function buildBrowserStackWsEndpoint(browserName, browserConfig, provider, userPersona) {
  if (!provider.username || !provider.accessKey) {
    throw new Error("BrowserStack Playwright execution requires CLOUD_USERNAME and CLOUD_KEY");
  }

  const caps = buildBrowserStackCapabilities(browserName, browserConfig, provider, userPersona);
  return `wss://cdp.browserstack.com/playwright?caps=${encodeURIComponent(JSON.stringify(caps))}`;
}

function buildBrowserStackCapabilities(browserName, browserConfig, provider, userPersona) {
  const webCapabilities = provider.webCapabilities || {};
  const browserstackOptions = getBrowserStackOptions(webCapabilities);
  const caps = {};

  caps.browser = normalizeRequestedBrowserName(webCapabilities.browserName || browserName);
  if (browserstackOptions.browserVersion || webCapabilities.browserVersion) {
    caps.browser_version = browserstackOptions.browserVersion || webCapabilities.browserVersion;
  }
  if (browserstackOptions.os || webCapabilities.os) {
    caps.os = browserstackOptions.os || webCapabilities.os;
  }
  if (browserstackOptions.osVersion || webCapabilities.osVersion) {
    caps.os_version = browserstackOptions.osVersion || webCapabilities.osVersion;
  }

  caps["browserstack.username"] = provider.username;
  caps["browserstack.accessKey"] = provider.accessKey;
  caps.name = userPersona || "teswiz-playwright-ts";

  for (const [key, value] of Object.entries(browserstackOptions)) {
    if (["browserVersion", "os", "osVersion"].includes(key)) {
      continue;
    }
    const mappedKey = mapBrowserStackCapabilityKey(key);
    caps[mappedKey] = value;
  }

  if (browserConfig.headless !== undefined) {
    caps["browserstack.playwrightLogs"] = browserstackOptions.playwrightLogs ?? "true";
  }

  return caps;
}

function getBrowserStackOptions(webCapabilities) {
  if (webCapabilities.browserstackOptions && typeof webCapabilities.browserstackOptions === "object") {
    return webCapabilities.browserstackOptions;
  }
  if (webCapabilities["bstack:options"] && typeof webCapabilities["bstack:options"] === "object") {
    return webCapabilities["bstack:options"];
  }
  return {};
}

function mapBrowserStackCapabilityKey(key) {
  switch (key) {
    case "debug":
      return "browserstack.debug";
    case "networkLogs":
      return "browserstack.networkLogs";
    case "console":
      return "browserstack.console";
    case "local":
      return "browserstack.local";
    case "localIdentifier":
      return "browserstack.localIdentifier";
    case "interactiveDebugging":
      return "browserstack.interactiveDebugging";
    case "playwrightVersion":
      return "browserstack.playwrightVersion";
    default:
      return key.startsWith("browserstack.") ? key : `browserstack.${key}`;
  }
}

export {
  buildBrowserStackCapabilities,
  buildRemoteLaunchDescriptor,
  normalizeExecutionProvider,
};
