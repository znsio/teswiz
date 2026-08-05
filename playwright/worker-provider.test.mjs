import test from "node:test";
import assert from "node:assert/strict";

import {
  buildBrowserStackCapabilities,
  buildLambdaTestCapabilities,
  buildRemoteLaunchDescriptor,
  normalizeExecutionProvider,
} from "./worker-provider.mjs";

test("normalizeExecutionProvider defaults to local execution", () => {
  assert.deepEqual(normalizeExecutionProvider(), {
    providerName: "local",
    remoteUrl: null,
    apiUrl: null,
    username: null,
    accessKey: null,
    webCapabilities: {},
  });
});

test("buildRemoteLaunchDescriptor returns null for local execution", () => {
  assert.equal(buildRemoteLaunchDescriptor("chrome", {}, {}, "buyer"), null);
});

test("buildBrowserStackCapabilities shapes BrowserStack websocket capabilities", () => {
  const capabilities = buildBrowserStackCapabilities(
    "chrome",
    { headless: true },
    {
      username: "bs_user",
      accessKey: "bs_key",
      webCapabilities: {
        browserName: "chrome",
        browserstackOptions: {
          browserVersion: "latest",
          os: "OS X",
          osVersion: "Sonoma",
          debug: true,
          networkLogs: true,
          console: "verbose",
          local: true,
          localIdentifier: "ci-local",
          playwrightVersion: "1.latest",
        },
      },
    },
    "buyer"
  );

  assert.equal(capabilities.browser, "chrome");
  assert.equal(capabilities.browser_version, "latest");
  assert.equal(capabilities.os, "OS X");
  assert.equal(capabilities.os_version, "Sonoma");
  assert.equal(capabilities["browserstack.username"], "bs_user");
  assert.equal(capabilities["browserstack.accessKey"], "bs_key");
  assert.equal(capabilities.name, "buyer");
  assert.equal(capabilities["browserstack.debug"], true);
  assert.equal(capabilities["browserstack.networkLogs"], true);
  assert.equal(capabilities["browserstack.console"], "verbose");
  assert.equal(capabilities["browserstack.local"], true);
  assert.equal(capabilities["browserstack.localIdentifier"], "ci-local");
  assert.equal(capabilities["browserstack.playwrightVersion"], "1.latest");
  assert.equal(capabilities["browserstack.playwrightLogs"], "true");
});

test("buildRemoteLaunchDescriptor creates BrowserStack websocket endpoint", () => {
  const descriptor = buildRemoteLaunchDescriptor(
    "chrome",
    { headless: true },
    {
      providerName: "browserstack",
      remoteUrl: "ignored-for-browserstack-ws",
      username: "bs_user",
      accessKey: "bs_key",
      webCapabilities: {
        browserstackOptions: {
          os: "OS X",
          osVersion: "Sonoma",
        },
      },
    },
    "buyer"
  );

  assert.equal(descriptor.mode, "connect");
  assert.match(descriptor.wsEndpoint, /^wss:\/\/cdp\.browserstack\.com\/playwright\?caps=/);
  const encodedCaps = descriptor.wsEndpoint.split("caps=")[1];
  const caps = JSON.parse(decodeURIComponent(encodedCaps));
  assert.equal(caps["browserstack.username"], "bs_user");
  assert.equal(caps["browserstack.accessKey"], "bs_key");
  assert.equal(caps.os, "OS X");
  assert.equal(caps.os_version, "Sonoma");
  assert.equal(caps.name, "buyer");
});

test("buildRemoteLaunchDescriptor fails fast for unsupported remote providers", () => {
  assert.throws(
    () =>
      buildRemoteLaunchDescriptor(
        "chrome",
        {},
        {
          providerName: "headspin",
          remoteUrl: "https://headspin.example.com",
        },
        "buyer"
      ),
    /not yet supported/
  );
});

test("buildLambdaTestCapabilities shapes capabilities from the current teswiz web config", () => {
  const capabilities = buildLambdaTestCapabilities(
    "chrome",
    { headless: true },
    {
      username: "lt_user",
      accessKey: "lt_key",
      webCapabilities: {
        browserName: "chrome",
        version: "latest",
        platform: "Windows 11",
        resolution: "1920x1080",
        network: true,
        appProfiling: true,
        console: true,
        visual: true,
        tunnel: false,
      },
    },
    "buyer"
  );

  assert.equal(capabilities.browserName, "chrome");
  assert.equal(capabilities.browserVersion, "latest");
  assert.equal(capabilities.platformName, "Windows 11");
  assert.equal(capabilities["LT:Options"].platform, "Windows 11");
  assert.equal(capabilities["LT:Options"].platformName, "Windows 11");
  assert.equal(capabilities["LT:Options"].user, "lt_user");
  assert.equal(capabilities["LT:Options"].username, "lt_user");
  assert.equal(capabilities["LT:Options"].accessKey, "lt_key");
  assert.equal(capabilities["LT:Options"].name, "buyer");
  assert.equal(capabilities["LT:Options"].resolution, "1920x1080");
  assert.equal(capabilities["LT:Options"].network, true);
  assert.equal(capabilities["LT:Options"].appProfiling, true);
  assert.equal(capabilities["LT:Options"].console, true);
  assert.equal(capabilities["LT:Options"].visual, true);
  assert.equal(capabilities["LT:Options"].tunnel, false);
  assert.equal(capabilities["LT:Options"].headless, true);
});

test("buildLambdaTestCapabilities preserves LT options when already present", () => {
  const capabilities = buildLambdaTestCapabilities(
    "chrome",
    {},
    {
      username: "lt_user",
      accessKey: "lt_key",
      webCapabilities: {
        browser: "Chrome",
        browser_version: "109.0",
        os: "Windows",
        os_version: "10",
        "LT:Options": {
          build: "teswiz-build",
          name: "preconfigured-name",
          network: false,
          tunnel: true,
        },
      },
    },
    "buyer"
  );

  assert.equal(capabilities.browserName, "Chrome");
  assert.equal(capabilities.browserVersion, "109.0");
  assert.equal(capabilities.platformName, "Windows 10");
  assert.equal(capabilities["LT:Options"].build, "teswiz-build");
  assert.equal(capabilities["LT:Options"].name, "preconfigured-name");
  assert.equal(capabilities["LT:Options"].network, false);
  assert.equal(capabilities["LT:Options"].tunnel, true);
  assert.equal(capabilities["LT:Options"].user, "lt_user");
});

test("buildRemoteLaunchDescriptor creates LambdaTest websocket endpoint", () => {
  const descriptor = buildRemoteLaunchDescriptor(
    "chrome",
    { headless: true },
    {
      providerName: "lambdatest",
      remoteUrl: "https://mobile-hub.lambdatest.com",
      username: "lt_user",
      accessKey: "lt_key",
      webCapabilities: {
        browserName: "chrome",
        version: "latest",
        platform: "Windows 11",
      },
    },
    "buyer"
  );

  assert.equal(descriptor.mode, "connect");
  assert.match(descriptor.wsEndpoint, /^wss:\/\/cdp\.lambdatest\.com\/playwright\?capabilities=/);
  const encodedCapabilities = descriptor.wsEndpoint.split("capabilities=")[1];
  const capabilities = JSON.parse(decodeURIComponent(encodedCapabilities));
  assert.equal(capabilities.browserName, "chrome");
  assert.equal(capabilities.browserVersion, "latest");
  assert.equal(capabilities["LT:Options"].user, "lt_user");
  assert.equal(capabilities["LT:Options"].accessKey, "lt_key");
  assert.equal(capabilities["LT:Options"].name, "buyer");
});
