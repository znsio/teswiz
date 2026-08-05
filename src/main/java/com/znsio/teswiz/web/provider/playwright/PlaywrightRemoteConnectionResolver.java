package com.znsio.teswiz.web.provider.playwright;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;

public final class PlaywrightRemoteConnectionResolver {
    public Optional<PlaywrightRemoteConnectionDescriptor> resolve(String browserName, PlaywrightBrowserConfig browserConfig,
            PlaywrightExecutionProviderConfig providerConfig, String userPersona) {
        if (!providerConfig.isRemote()) {
            return Optional.empty();
        }

        return Optional.of(switch (providerConfig.providerName().toLowerCase()) {
            case "browserstack" -> new PlaywrightRemoteConnectionDescriptor(
                    buildBrowserStackWsEndpoint(browserName, browserConfig, providerConfig, userPersona));
            case "lambdatest" -> new PlaywrightRemoteConnectionDescriptor(
                    buildLambdaTestWsEndpoint(browserName, browserConfig, providerConfig, userPersona));
            case "headspin" -> throw unsupportedRemoteProviderError("headspin");
            default -> throw new IllegalArgumentException(
                    "Unsupported Playwright remote provider: " + providerConfig.providerName());
        });
    }

    private String buildBrowserStackWsEndpoint(String browserName, PlaywrightBrowserConfig browserConfig,
            PlaywrightExecutionProviderConfig providerConfig, String userPersona) {
        requireCredentials(providerConfig, "BrowserStack");
        Map<String, Object> capabilities = new LinkedHashMap<>();
        Map<String, Object> webCapabilities = providerConfig.webCapabilities();
        Map<String, Object> browserStackOptions = getMap(webCapabilities, "browserstackOptions", "bstack:options");

        capabilities.put("browser", normalizeBrowserStackBrowserName(getString(webCapabilities, "browserName", browserName)));
        putIfPresent(capabilities, "browser_version",
                getFirst(browserStackOptions, "browserVersion", webCapabilities.get("browserVersion")));
        putIfPresent(capabilities, "os", getFirst(browserStackOptions, "os", webCapabilities.get("os")));
        putIfPresent(capabilities, "os_version",
                getFirst(browserStackOptions, "osVersion", webCapabilities.get("osVersion")));
        capabilities.put("browserstack.username", providerConfig.username());
        capabilities.put("browserstack.accessKey", providerConfig.accessKey());
        capabilities.put("name", userPersona == null || userPersona.isBlank() ? "teswiz-playwright-java" : userPersona);

        for (Map.Entry<String, Object> entry : browserStackOptions.entrySet()) {
            String key = entry.getKey();
            if ("browserVersion".equals(key) || "os".equals(key) || "osVersion".equals(key)) {
                continue;
            }
            capabilities.put(mapBrowserStackCapabilityKey(key), entry.getValue());
        }

        if (browserConfig.headless()) {
            capabilities.putIfAbsent("browserstack.playwrightLogs", "true");
        }
        return "wss://cdp.browserstack.com/playwright?caps=" + encode(capabilities);
    }

    private String buildLambdaTestWsEndpoint(String browserName, PlaywrightBrowserConfig browserConfig,
            PlaywrightExecutionProviderConfig providerConfig, String userPersona) {
        requireCredentials(providerConfig, "LambdaTest");
        Map<String, Object> capabilities = new LinkedHashMap<>();
        Map<String, Object> webCapabilities = providerConfig.webCapabilities();
        Map<String, Object> ltOptions = getMap(webCapabilities, "LT:Options", "ltOptions", "lt:options");
        String resolvedBrowserName = getString(webCapabilities, "browserName",
                getString(webCapabilities, "browser", browserName));
        String resolvedBrowserVersion = getString(webCapabilities, "browserVersion",
                getString(webCapabilities, "version", getString(webCapabilities, "browser_version", null)));
        String resolvedPlatformName = resolveLambdaTestPlatformName(webCapabilities, ltOptions);

        capabilities.put("browserName", resolvedBrowserName);
        putIfPresent(capabilities, "browserVersion", resolvedBrowserVersion);
        putIfPresent(capabilities, "platformName", resolvedPlatformName);

        Map<String, Object> normalizedLtOptions = new LinkedHashMap<>(ltOptions);
        putIfAbsent(normalizedLtOptions, "platform", resolvedPlatformName);
        putIfAbsent(normalizedLtOptions, "platformName", resolvedPlatformName);
        putIfAbsent(normalizedLtOptions, "name",
                userPersona == null || userPersona.isBlank() ? "teswiz-playwright-java" : userPersona);
        putIfAbsent(normalizedLtOptions, "build", getString(webCapabilities, "build",
                getString(webCapabilities, "buildName", "teswiz-playwright-java")));
        putIfAbsent(normalizedLtOptions, "user", providerConfig.username());
        putIfAbsent(normalizedLtOptions, "username", providerConfig.username());
        putIfAbsent(normalizedLtOptions, "accessKey", providerConfig.accessKey());
        copyIfPresent(normalizedLtOptions, "resolution", webCapabilities);
        copyIfPresent(normalizedLtOptions, "network", webCapabilities);
        copyIfPresent(normalizedLtOptions, "appProfiling", webCapabilities);
        copyIfPresent(normalizedLtOptions, "console", webCapabilities);
        copyIfPresent(normalizedLtOptions, "visual", webCapabilities);
        copyIfPresent(normalizedLtOptions, "tunnel", webCapabilities);
        normalizedLtOptions.put("headless", browserConfig.headless());
        capabilities.put("LT:Options", normalizedLtOptions);

        return "wss://cdp.lambdatest.com/playwright?capabilities=" + encode(capabilities);
    }

    private static String normalizeBrowserStackBrowserName(String browserName) {
        return switch (browserName.toLowerCase()) {
            case "chrome" -> "chrome";
            case "edge", "msedge" -> "edge";
            case "firefox" -> "playwright-firefox";
            case "safari", "webkit" -> "playwright-webkit";
            default -> "playwright-chromium";
        };
    }

    private static String mapBrowserStackCapabilityKey(String key) {
        return switch (key) {
            case "debug" -> "browserstack.debug";
            case "networkLogs" -> "browserstack.networkLogs";
            case "console" -> "browserstack.console";
            case "local" -> "browserstack.local";
            case "localIdentifier" -> "browserstack.localIdentifier";
            case "interactiveDebugging" -> "browserstack.interactiveDebugging";
            case "playwrightVersion" -> "browserstack.playwrightVersion";
            default -> key.startsWith("browserstack.") ? key : "browserstack." + key;
        };
    }

    private static String resolveLambdaTestPlatformName(Map<String, Object> webCapabilities, Map<String, Object> ltOptions) {
        String explicitPlatformName = getString(ltOptions, "platformName", getString(ltOptions, "platform", null));
        if (null != explicitPlatformName) {
            return explicitPlatformName;
        }
        explicitPlatformName = getString(webCapabilities, "platformName", getString(webCapabilities, "platform", null));
        if (null != explicitPlatformName) {
            return explicitPlatformName;
        }
        Object os = webCapabilities.get("os");
        Object osVersion = webCapabilities.get("os_version");
        if (null != os && null != osVersion) {
            return os + " " + osVersion;
        }
        return null;
    }

    private static void requireCredentials(PlaywrightExecutionProviderConfig providerConfig, String providerName) {
        if (providerConfig.username() == null || providerConfig.username().isBlank()
                || providerConfig.accessKey() == null || providerConfig.accessKey().isBlank()) {
            throw new IllegalArgumentException(providerName + " Playwright execution requires CLOUD_USERNAME and CLOUD_KEY");
        }
    }

    private static String encode(Map<String, Object> capabilities) {
        return URLEncoder.encode(new JSONObject(capabilities).toString(), StandardCharsets.UTF_8);
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (null != value) {
            target.put(key, value);
        }
    }

    private static void putIfAbsent(Map<String, Object> target, String key, Object value) {
        if (null != value) {
            target.putIfAbsent(key, value);
        }
    }

    private static void copyIfPresent(Map<String, Object> target, String key, Map<String, Object> source) {
        if (!target.containsKey(key) && source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private static Object getFirst(Map<String, Object> source, String key, Object fallback) {
        return source.containsKey(key) ? source.get(key) : fallback;
    }

    private static Map<String, Object> getMap(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof Map<?, ?> mapValue) {
                Map<String, Object> normalizedMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                    if (entry.getKey() instanceof String stringKey) {
                        normalizedMap.put(stringKey, entry.getValue());
                    }
                }
                return normalizedMap;
            }
        }
        return Map.of();
    }

    private static String getString(Map<String, Object> source, String key, String fallback) {
        Object value = source.get(key);
        return null == value ? fallback : String.valueOf(value);
    }

    private static UnsupportedOperationException unsupportedRemoteProviderError(String providerName) {
        return new UnsupportedOperationException(
                "Playwright remote provider '%s' is not yet supported for the current engine".formatted(providerName));
    }
}
