import test from "node:test";
import assert from "node:assert/strict";

import {
  buildBrowserStackCapabilities,
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
          providerName: "lambdatest",
          remoteUrl: "https://hub.lambdatest.com/wd/hub",
          username: "lt_user",
          accessKey: "lt_key",
        },
        "buyer"
      ),
    /not yet supported/
  );
});
