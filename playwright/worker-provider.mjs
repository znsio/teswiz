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
      return {
        mode: "connect",
        wsEndpoint: buildLambdaTestWsEndpoint(browserName, browserConfig, provider, userPersona),
      };
    case "headspin":
      throw unsupportedRemoteProviderError(provider.providerName);
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

function buildLambdaTestWsEndpoint(browserName, browserConfig, provider, userPersona) {
  if (!provider.username || !provider.accessKey) {
    throw new Error("LambdaTest Playwright execution requires CLOUD_USERNAME and CLOUD_KEY");
  }

  const capabilities = buildLambdaTestCapabilities(browserName, browserConfig, provider, userPersona);
  return `wss://cdp.lambdatest.com/playwright?capabilities=${encodeURIComponent(JSON.stringify(capabilities))}`;
}

function buildLambdaTestCapabilities(browserName, browserConfig, provider, userPersona) {
  const webCapabilities = provider.webCapabilities || {};
  const ltOptions = getLambdaTestOptions(webCapabilities);
  const capabilities = {};
  const resolvedBrowserName = webCapabilities.browserName || webCapabilities.browser || browserName;
  const resolvedBrowserVersion =
    webCapabilities.browserVersion || webCapabilities.version || webCapabilities.browser_version;
  const resolvedPlatformName = resolveLambdaTestPlatformName(webCapabilities, ltOptions);

  capabilities.browserName = resolvedBrowserName;
  if (resolvedBrowserVersion) {
    capabilities.browserVersion = resolvedBrowserVersion;
  }

  const normalizedLtOptions = {
    ...ltOptions,
    platform: resolvedPlatformName,
    platformName: resolvedPlatformName,
    name: ltOptions.name || userPersona || "teswiz-playwright-ts",
    build: ltOptions.build || webCapabilities.build || webCapabilities.buildName || "teswiz-playwright-ts",
    user: ltOptions.user || provider.username,
    username: ltOptions.username || provider.username,
    accessKey: ltOptions.accessKey || provider.accessKey,
  };

  copyLambdaTestOptionIfPresent(normalizedLtOptions, "resolution", webCapabilities);
  copyLambdaTestOptionIfPresent(normalizedLtOptions, "network", webCapabilities);
  copyLambdaTestOptionIfPresent(normalizedLtOptions, "appProfiling", webCapabilities);
  copyLambdaTestOptionIfPresent(normalizedLtOptions, "console", webCapabilities);
  copyLambdaTestOptionIfPresent(normalizedLtOptions, "visual", webCapabilities);
  copyLambdaTestOptionIfPresent(normalizedLtOptions, "tunnel", webCapabilities);

  capabilities["LT:Options"] = normalizedLtOptions;
  if (resolvedPlatformName) {
    capabilities.platformName = resolvedPlatformName;
  }
  if (browserConfig.headless !== undefined) {
    capabilities["LT:Options"].headless = browserConfig.headless;
  }
  return capabilities;
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

function getLambdaTestOptions(webCapabilities) {
  if (webCapabilities["LT:Options"] && typeof webCapabilities["LT:Options"] === "object") {
    return { ...webCapabilities["LT:Options"] };
  }
  if (webCapabilities.ltOptions && typeof webCapabilities.ltOptions === "object") {
    return { ...webCapabilities.ltOptions };
  }
  if (webCapabilities["lt:options"] && typeof webCapabilities["lt:options"] === "object") {
    return { ...webCapabilities["lt:options"] };
  }
  return {};
}

function resolveLambdaTestPlatformName(webCapabilities, ltOptions) {
  if (ltOptions.platformName) {
    return ltOptions.platformName;
  }
  if (ltOptions.platform) {
    return ltOptions.platform;
  }
  if (webCapabilities.platformName) {
    return webCapabilities.platformName;
  }
  if (webCapabilities.platform) {
    return webCapabilities.platform;
  }
  if (webCapabilities.os && webCapabilities.os_version) {
    return `${webCapabilities.os} ${webCapabilities.os_version}`;
  }
  return undefined;
}

function copyLambdaTestOptionIfPresent(options, key, webCapabilities) {
  if (options[key] !== undefined) {
    return;
  }
  if (webCapabilities[key] !== undefined) {
    options[key] = webCapabilities[key];
  }
}

function unsupportedRemoteProviderError(providerName) {
  if (providerName === "headspin") {
    return new Error("HeadSpin is not supported with Playwright web engines in teswiz.");
  }
  return new Error(`Playwright TS remote provider '${providerName}' is not supported by teswiz worker.`);
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
  buildLambdaTestCapabilities,
  buildRemoteLaunchDescriptor,
  normalizeExecutionProvider,
};
