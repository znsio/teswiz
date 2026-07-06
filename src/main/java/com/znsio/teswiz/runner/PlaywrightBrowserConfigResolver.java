package com.znsio.teswiz.runner;

import static com.znsio.teswiz.runner.Setup.HEADLESS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.exceptions.InvalidTestDataException;

class PlaywrightBrowserConfigResolver {
    PlaywrightBrowserConfig resolve(String browserName, TestExecutionContext context) {
        JSONObject browserConfig = BrowserConfigLoader.load(context);
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
                || Setup.getBooleanValueFromConfigs(HEADLESS);
        if (null != playwrightLaunchOptions && playwrightLaunchOptions.has("headless")) {
            headless = playwrightLaunchOptions.getBoolean("headless") || Setup.getBooleanValueFromConfigs(HEADLESS);
        }

        List<String> launchArgs = new ArrayList<>();
        addNormalizedArguments(browserConfigForBrowserType.optJSONArray("arguments"), launchArgs);
        if (headless && null != legacyHeadlessOptions) {
            addNormalizedArguments(legacyHeadlessOptions.optJSONArray("include"), launchArgs);
        }
        if (null != playwrightLaunchOptions) {
            addNormalizedArguments(playwrightLaunchOptions.optJSONArray("args"), launchArgs);
        }

        Map<String, Object> contextOptions = new LinkedHashMap<>();
        boolean ignoreHttpErrors = browserConfigForBrowserType.optBoolean("acceptInsecureCerts", false);
        if (null != playwrightContextOptions && playwrightContextOptions.has("ignoreHTTPSErrors")) {
            ignoreHttpErrors = playwrightContextOptions.getBoolean("ignoreHTTPSErrors");
        }
        contextOptions.put("ignoreHTTPSErrors", ignoreHttpErrors);
        if (null != playwrightContextOptions && playwrightContextOptions.has("viewport")) {
            contextOptions.put("viewport", playwrightContextOptions.getJSONObject("viewport").toMap());
        }

        String proxyUrl = Runner.getProxyURL();
        Map<String, Object> launchOptions = new LinkedHashMap<>();
        if (null != proxyUrl && !proxyUrl.isBlank()) {
            Map<String, Object> proxy = new LinkedHashMap<>();
            proxy.put("server", proxyUrl);
            if (browserConfigForBrowserType.has("noProxy")) {
                proxy.put("bypass", browserConfigForBrowserType.getString("noProxy"));
            }
            launchOptions.put("proxy", proxy);
        }

        String channel = null;
        String executablePath = null;
        if (null != playwrightLaunchOptions) {
            channel = playwrightLaunchOptions.optString("channel", null);
            executablePath = playwrightLaunchOptions.optString("executablePath", null);
        }

        return new PlaywrightBrowserConfig(browserName, headless, launchArgs, channel, executablePath,
                contextOptions, launchOptions);
    }

    private void addNormalizedArguments(JSONArray arguments, List<String> launchArgs) {
        if (null == arguments) {
            return;
        }

        arguments.forEach(argument -> launchArgs.add(normalizeArgument(argument.toString())));
    }

    private String normalizeArgument(String argument) {
        if (argument.startsWith("-")) {
            return argument;
        }
        return "--" + argument;
    }
}
