package com.znsio.teswiz.web.provider.selenium;

import com.znsio.teswiz.entities.Platform;
import org.openqa.selenium.MutableCapabilities;

import java.util.HashMap;
import java.util.Map;

public final class LambdaTestWebCapabilitySetup {
    private static final String LT_OPTIONS = "LT:Options";

    private LambdaTestWebCapabilitySetup() {
    }

    public static MutableCapabilities updateLambdaTestCapabilities(MutableCapabilities capabilities,
            Map loadedPlatformCapability, String authenticationUser, String authenticationKey,
            String projectName, String launchName, String logDir, String sessionName,
            boolean useLocalTesting) {
        String subsetOfLogDir = logDir.replace("/", "").replace("\\", "");
        String buildName = launchName + "-" + subsetOfLogDir;
        setCapabilityIfPresent(capabilities, "browserName", loadedPlatformCapability, "browserName", "browser");
        Object browserVersion = getValueFrom(loadedPlatformCapability, "browserVersion", "version", "browser_version");
        if (browserVersion != null) {
            capabilities.setCapability("browserVersion", browserVersion);
        }

        Object platformValue = getValueFrom(loadedPlatformCapability, "platformName", "platform");
        if (platformValue == null) {
            Object os = loadedPlatformCapability.get("os");
            Object osVersion = loadedPlatformCapability.get("os_version");
            if (os != null && osVersion != null) {
                platformValue = os + " " + osVersion;
            }
        }
        if (platformValue != null) {
            capabilities.setCapability("platformName", platformValue);
        }

        Map<String, Object> ltOptions = new HashMap<>(getOrCreateWebLtOptions(loadedPlatformCapability));
        ltOptions.put("username", authenticationUser);
        ltOptions.put("accessKey", authenticationKey);
        ltOptions.put("project", projectName);
        ltOptions.put("build", getValueFrom(loadedPlatformCapability, "build", "buildName") != null
                ? getValueFrom(loadedPlatformCapability, "build", "buildName")
                : buildName);
        ltOptions.put("name", getValueFrom(loadedPlatformCapability, "name", "sessionName") != null
                ? getValueFrom(loadedPlatformCapability, "name", "sessionName")
                : sessionName);
        ltOptions.put("w3c", true);

        setOptionIfPresent(ltOptions, "resolution", loadedPlatformCapability, "resolution");
        setOptionIfPresent(ltOptions, "network", loadedPlatformCapability, "network");
        setOptionIfPresent(ltOptions, "appProfiling", loadedPlatformCapability, "appProfiling");
        setOptionIfPresent(ltOptions, "console", loadedPlatformCapability, "console");
        setOptionIfPresent(ltOptions, "visual", loadedPlatformCapability, "visual");
        setOptionIfPresent(ltOptions, "tunnel", loadedPlatformCapability, "tunnel");
        if (browserVersion != null) {
            ltOptions.putIfAbsent("browserVersion", browserVersion);
        }
        if (platformValue != null) {
            ltOptions.putIfAbsent("platformName", platformValue);
        }
        if (useLocalTesting) {
            ltOptions.put("tunnel", true);
        }
        capabilities.setCapability(LT_OPTIONS, ltOptions);
        return capabilities;
    }

    private static Map<String, Object> getOrCreateWebLtOptions(Map loadedPlatformCapability) {
        Object existing = loadedPlatformCapability.get(LT_OPTIONS);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        existing = loadedPlatformCapability.get("ltOptions");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        existing = loadedPlatformCapability.get("lt:options");
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        return new HashMap<>();
    }

    private static void setCapabilityIfPresent(MutableCapabilities capabilities, String capabilityName,
            Map loadedPlatformCapability, String... sourceKeys) {
        Object value = getValueFrom(loadedPlatformCapability, sourceKeys);
        if (value != null) {
            capabilities.setCapability(capabilityName, value);
        }
    }

    private static void setOptionIfPresent(Map<String, Object> options, String optionName,
            Map loadedPlatformCapability, String... sourceKeys) {
        Object value = getValueFrom(loadedPlatformCapability, sourceKeys);
        if (value != null) {
            options.put(optionName, value);
        }
    }

    private static Object getValueFrom(Map loadedPlatformCapability, String... sourceKeys) {
        for (String sourceKey : sourceKeys) {
            if (loadedPlatformCapability.containsKey(sourceKey)) {
                return loadedPlatformCapability.get(sourceKey);
            }
        }
        return null;
    }
}
