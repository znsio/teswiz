package com.znsio.teswiz.mobile.provider;

import java.util.HashMap;
import java.util.Map;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

public final class LambdaTestMobileCapabilitySetup {
    private static final String LT_OPTIONS_APPIUM = "lt:options";
    private static final String DEVICE = "device";
    private static final String PLATFORM_VERSION = "platformVersion";
    private static final String OS_VERSION = "os_version";

    private LambdaTestMobileCapabilitySetup() {
    }

    public static void prepareCapabilities(Map loadedPlatformCapability, String authenticationUser,
            String authenticationKey, String projectName, String launchName, String logDir,
            boolean useLocalTesting) {
        Map<String, Object> ltOptions = getOrCreateMobileLtOptions(loadedPlatformCapability);
        ltOptions.put("username", authenticationUser);
        ltOptions.put("accessKey", authenticationKey);
        ltOptions.put("project", projectName);
        String subsetOfLogDir = logDir.replace("/", "").replace("\\", "");
        ltOptions.put("build", launchName + "-" + subsetOfLogDir);
        ltOptions.put("w3c", true);

        if (useLocalTesting) {
            ltOptions.put("tunnel", true);
        }

        normalizeMobileCapabilities(loadedPlatformCapability);
        loadedPlatformCapability.put(LT_OPTIONS_APPIUM, ltOptions);
    }

    public static String resolveAppReference(Map loadedPlatformCapability, String configuredAppPath) {
        if (isLambdaTestAppReference(configuredAppPath)) {
            return configuredAppPath;
        }

        Object capabilityApp = loadedPlatformCapability.get("app");
        if (capabilityApp != null && isLambdaTestAppReference(String.valueOf(capabilityApp))) {
            return String.valueOf(capabilityApp);
        }

        throw new InvalidTestDataException(
                "CLOUD_UPLOAD_APP=false for LambdaTest requires APP_PATH or the platform capability 'app' to be a valid LambdaTest app id like 'lt://APP123'.");
    }

    private static void normalizeMobileCapabilities(Map loadedPlatformCapability) {
        Object platformVersion = loadedPlatformCapability.getOrDefault(PLATFORM_VERSION,
                loadedPlatformCapability.getOrDefault(OS_VERSION, ""));
        if (!String.valueOf(platformVersion).isEmpty()) {
            loadedPlatformCapability.put(PLATFORM_VERSION, platformVersion);
        }

        Object deviceName = loadedPlatformCapability.getOrDefault("deviceName",
                loadedPlatformCapability.getOrDefault(DEVICE, ""));
        if (!String.valueOf(deviceName).isEmpty()) {
            loadedPlatformCapability.put("deviceName", deviceName);
        }

        loadedPlatformCapability.remove(DEVICE);
        loadedPlatformCapability.remove(OS_VERSION);
    }

    private static Map<String, Object> getOrCreateMobileLtOptions(Map loadedPlatformCapability) {
        Object existing = loadedPlatformCapability.get(LT_OPTIONS_APPIUM);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> ltOptions = new HashMap<>();
        loadedPlatformCapability.put(LT_OPTIONS_APPIUM, ltOptions);
        return ltOptions;
    }

    private static boolean isLambdaTestAppReference(String appReference) {
        return appReference != null && appReference.startsWith("lt://");
    }
}
