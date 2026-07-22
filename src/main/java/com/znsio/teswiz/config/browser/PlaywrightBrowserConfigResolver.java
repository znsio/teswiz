package com.znsio.teswiz.config.browser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Runner;

public class PlaywrightBrowserConfigResolver {
    public PlaywrightBrowserConfig resolve(String browserName, TestExecutionContext context) {
        JSONObject browserConfig = BrowserConfigLoader.load(context);
        maybeRegisterMigrationArtifact(context, browserConfig);
        String browserKey = browserName.toLowerCase();
        if (!browserConfig.has(browserKey)) {
            throw new InvalidTestDataException(
                    String.format("Browser: '%s' is NOT supported for Playwright configuration", browserName));
        }

        JSONObject browserConfigForBrowserType = browserConfig.getJSONObject(browserKey);
        JSONObject playwrightConfig = browserConfigForBrowserType.optJSONObject("playwright");
        JSONObject playwrightLaunchOptions = null == playwrightConfig
                ? new JSONObject()
                : playwrightConfig.optJSONObject("launchOptions");
        JSONObject playwrightContextOptions = null == playwrightConfig
                ? new JSONObject()
                : playwrightConfig.optJSONObject("contextOptions");

        JSONObject legacyHeadlessOptions = browserConfigForBrowserType.optJSONObject("headlessOptions");
        boolean headless = (null != legacyHeadlessOptions && legacyHeadlessOptions.optBoolean("headless", false))
                || Runner.isRunningInHeadlessMode();
        if (null != playwrightLaunchOptions && playwrightLaunchOptions.has("headless")) {
            headless = playwrightLaunchOptions.getBoolean("headless") || Runner.isRunningInHeadlessMode();
        }

        Set<String> launchArgs = new LinkedHashSet<>();
        addNormalizedArguments(browserConfigForBrowserType.optJSONArray("arguments"), launchArgs);
        if (headless && null != legacyHeadlessOptions) {
            addNormalizedArguments(legacyHeadlessOptions.optJSONArray("include"), launchArgs);
        }
        if (null != playwrightLaunchOptions) {
            addNormalizedArguments(playwrightLaunchOptions.optJSONArray("args"), launchArgs);
        }

        Map<String, Object> contextOptions = new LinkedHashMap<>();
        if (null != playwrightContextOptions) {
            contextOptions.putAll(playwrightContextOptions.toMap());
        }
        boolean ignoreHttpErrors = browserConfigForBrowserType.optBoolean("acceptInsecureCerts", false);
        if (null != playwrightContextOptions && playwrightContextOptions.has("ignoreHTTPSErrors")) {
            ignoreHttpErrors = playwrightContextOptions.getBoolean("ignoreHTTPSErrors");
        }
        contextOptions.put("ignoreHTTPSErrors", ignoreHttpErrors);

        String proxyUrl = Runner.getProxyURL();
        Map<String, Object> launchOptions = new LinkedHashMap<>();
        if (null != playwrightLaunchOptions) {
            Map<String, Object> configuredLaunchOptions = playwrightLaunchOptions.toMap();
            configuredLaunchOptions.remove("headless");
            configuredLaunchOptions.remove("args");
            configuredLaunchOptions.remove("channel");
            configuredLaunchOptions.remove("executablePath");
            launchOptions.putAll(configuredLaunchOptions);
        }
        if (null != proxyUrl && !proxyUrl.isBlank()) {
            Map<String, Object> proxy = new LinkedHashMap<>();
            proxy.put("server", proxyUrl);
            if (browserConfigForBrowserType.has("noProxy")) {
                proxy.put("bypass", browserConfigForBrowserType.getString("noProxy"));
            }
            launchOptions.put("proxy", proxy);
        }

        String channel = null;
        String executablePath = resolveExecutablePath(browserConfigForBrowserType, playwrightLaunchOptions);
        if (null != playwrightLaunchOptions) {
            channel = playwrightLaunchOptions.optString("channel", null);
        }

        return new PlaywrightBrowserConfig(browserName, headless, new ArrayList<>(launchArgs), channel, executablePath,
                contextOptions, launchOptions);
    }

    private void maybeRegisterMigrationArtifact(TestExecutionContext context, JSONObject browserConfig) {
        String sourceConfigPath = context.getTestStateAsString(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST);
        if (null == sourceConfigPath || sourceConfigPath.isBlank()) {
            sourceConfigPath = Runner.getBrowserConfigFile();
        }
        PlaywrightBrowserConfigMigrationReporter.registerMigrationArtifact(context, sourceConfigPath, browserConfig,
                new PlaywrightBrowserConfigMigrator());
    }

    private void addNormalizedArguments(JSONArray arguments, Set<String> launchArgs) {
        if (null == arguments) {
            return;
        }

        arguments.forEach(argument -> launchArgs.add(normalizeArgument(argument.toString())));
    }

    private String resolveExecutablePath(JSONObject browserConfigForBrowserType, JSONObject playwrightLaunchOptions) {
        if (null != playwrightLaunchOptions) {
            String configuredExecutablePath = playwrightLaunchOptions.optString("executablePath", null);
            if (null != configuredExecutablePath && !configuredExecutablePath.isBlank()) {
                return configuredExecutablePath;
            }
        }
        String legacyBinary = browserConfigForBrowserType.optString("binary", "");
        if (legacyBinary.isBlank()) {
            return null;
        }
        if (browserConfigForBrowserType.optBoolean("electronAppLoadingPage", false)) {
            return null;
        }
        return legacyBinary;
    }

    private String normalizeArgument(String argument) {
        if (argument.startsWith("-")) {
            return argument;
        }
        return "--" + argument;
    }
}
