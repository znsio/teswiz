package com.znsio.teswiz.config.browser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public final class PlaywrightBrowserConfigMigrator {
    public MigrationResult migrate(JSONObject originalConfig) {
        JSONObject migratedConfig = new JSONObject(originalConfig.toString());
        List<String> warnings = new ArrayList<>();
        boolean changed = false;

        for (String browserKey : migratedConfig.keySet()) {
            Object browserValue = migratedConfig.get(browserKey);
            if (!(browserValue instanceof JSONObject browserConfigForBrowserType)) {
                continue;
            }
            if (!isSupportedBrowser(browserKey)) {
                continue;
            }
            if (browserConfigForBrowserType.has("playwright")) {
                continue;
            }

            browserConfigForBrowserType.put("playwright", buildPlaywrightBlock(browserConfigForBrowserType));
            changed = true;
            collectWarnings(browserKey, browserConfigForBrowserType, warnings);
        }

        return new MigrationResult(changed ? migratedConfig : null, warnings);
    }

    private boolean isSupportedBrowser(String browserKey) {
        return browserKey.equalsIgnoreCase("chrome")
                || browserKey.equalsIgnoreCase("firefox")
                || browserKey.equalsIgnoreCase("safari");
    }

    private JSONObject buildPlaywrightBlock(JSONObject browserConfigForBrowserType) {
        JSONObject playwright = new JSONObject();
        JSONObject launchOptions = new JSONObject();
        JSONObject contextOptions = new JSONObject();

        JSONObject headlessOptions = browserConfigForBrowserType.optJSONObject("headlessOptions");
        boolean headless = null != headlessOptions && headlessOptions.optBoolean("headless", false);
        launchOptions.put("headless", headless);

        JSONArray args = new JSONArray();
        addNormalizedArguments(browserConfigForBrowserType.optJSONArray("arguments"), args);
        if (headless && null != headlessOptions) {
            addNormalizedArguments(headlessOptions.optJSONArray("include"), args);
        }
        if (!args.isEmpty()) {
            launchOptions.put("args", args);
        }
        String legacyBinary = browserConfigForBrowserType.optString("binary", "");
        if (!legacyBinary.isBlank() && !browserConfigForBrowserType.optBoolean("electronAppLoadingPage", false)) {
            launchOptions.put("executablePath", legacyBinary);
        }

        contextOptions.put("ignoreHTTPSErrors", browserConfigForBrowserType.optBoolean("acceptInsecureCerts", false));

        playwright.put("launchOptions", launchOptions);
        playwright.put("contextOptions", contextOptions);
        return playwright;
    }

    private void addNormalizedArguments(JSONArray arguments, JSONArray target) {
        if (null == arguments) {
            return;
        }
        arguments.forEach(argument -> target.put(normalizeArgument(argument.toString())));
    }

    private String normalizeArgument(String argument) {
        if (argument.startsWith("-")) {
            return argument;
        }
        return "--" + argument;
    }

    private void collectWarnings(String browserKey, JSONObject browserConfigForBrowserType, List<String> warnings) {
        if (browserConfigForBrowserType.has("binary")) {
            String binary = browserConfigForBrowserType.optString("binary", "");
            if (!binary.isBlank() && browserConfigForBrowserType.optBoolean("electronAppLoadingPage", false)) {
                warnings.add(String.format(
                        "%s.binary was not migrated automatically because electronAppLoadingPage=true makes the legacy binary likely Electron-specific.",
                        browserKey));
            } else if (!binary.isBlank()) {
                warnings.add(String.format(
                        "%s.binary was copied to playwright.launchOptions.executablePath. Review it before replacing the old config.",
                        browserKey));
            }
        }
        if (browserConfigForBrowserType.has("noProxy")) {
            warnings.add(String.format(
                    "%s.noProxy remains supported as a legacy field and is still used when a proxy is configured via Runner/Setup.",
                    browserKey));
        }
        if (browserConfigForBrowserType.has("excludeSwitches")) {
            warnings.add(String.format("%s.excludeSwitches is Selenium-specific and was not copied into Playwright.",
                    browserKey));
        }
        if (browserConfigForBrowserType.has("preferences")) {
            warnings.add(String.format("%s.preferences is Selenium-specific and may need manual Playwright mapping.",
                    browserKey));
        }
        if (browserConfigForBrowserType.has("firefoxProfile")) {
            warnings.add(String.format(
                    "%s.firefoxProfile is Selenium-specific and may need manual Playwright mapping.", browserKey));
        }
    }

    public record MigrationResult(JSONObject migratedConfig, List<String> warnings) {
    }
}
