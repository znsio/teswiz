package com.znsio.teswiz.mobile.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BrowserStackMobileCapabilitySetup {
    private static final String BROWSERSTACK_KEY_PREFIX = "browserstack.";
    private static final String BSTACK_OPTIONS_CAPABILITY = "bstack:options";
    private static final String LOCALE = "locale";

    private BrowserStackMobileCapabilitySetup() {
    }

    public static void prepareCapabilities(Map<String, Object> loadedPlatformCapability,
            String authenticationUser, String authenticationKey, String projectName,
            String launchName, String logDir, String sessionName, Object appiumVersion,
            boolean useLocalTesting, String localIdentifier) {
        String subsetOfLogDir = logDir.replace("/", "").replace("\\", "");
        Object existingOptions = loadedPlatformCapability.get(BSTACK_OPTIONS_CAPABILITY);
        Map<String, Object> bstackOptions = existingOptions instanceof Map
                ? (Map<String, Object>) existingOptions
                : new java.util.HashMap<>();

        migrateLegacyBrowserStackOptions(loadedPlatformCapability, bstackOptions);
        bstackOptions.put("userName", authenticationUser);
        bstackOptions.put("accessKey", authenticationKey);
        if (null != appiumVersion) {
            bstackOptions.put("appiumVersion", appiumVersion);
        }
        bstackOptions.put("projectName", projectName);
        bstackOptions.put("buildName", launchName + "-" + subsetOfLogDir);
        bstackOptions.put("sessionName", sessionName);
        bstackOptions.put("debug", "true");
        bstackOptions.put("networkLogs", "true");
        bstackOptions.put("appProfiling", "true");
        if (useLocalTesting) {
            bstackOptions.put("local", "true");
            bstackOptions.put("localIdentifier", localIdentifier);
        }
        loadedPlatformCapability.put(BSTACK_OPTIONS_CAPABILITY, bstackOptions);
    }

    static void migrateLegacyBrowserStackOptions(Map<String, Object> loadedPlatformCapability,
            Map<String, Object> bstackOptions) {
        List<String> keysToRemove = new ArrayList<>();
        loadedPlatformCapability.forEach((key, value) -> {
            if (key.startsWith(BROWSERSTACK_KEY_PREFIX)) {
                String normalizedKey = key.substring(BROWSERSTACK_KEY_PREFIX.length());
                if (LOCALE.equals(normalizedKey)) {
                    keysToRemove.add(key);
                    return;
                }
                bstackOptions.put(normalizedKey, value);
                keysToRemove.add(key);
            }
        });
        keysToRemove.forEach(loadedPlatformCapability::remove);
    }
}
