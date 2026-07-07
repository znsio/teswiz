package com.znsio.teswiz.mobile.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class PCloudyMobileCapabilitySetup {
    private static final String APPIUM_VERSION = "appiumVersion";
    private static final String APPIUM_VERSION_NAMESPACED = "appium:appiumVersion";
    private static final String APP = "app";
    private static final String DEVICE = "device";
    private static final String OS_VERSION = "os_version";
    private static final String PLATFORM_NAME = "platformName";
    private static final String PLATFORM_VERSION = "platformVersion";
    private static final String PCLOUDY_OPTIONS = "pcloudy:options";

    private PCloudyMobileCapabilitySetup() {
    }

    public static PreparedPCloudyCapabilities prepareCapabilities(Map<String, Object> loadedPlatformCapability,
            String username, String apiKey, String appPath, int maxDrivers) {
        String deviceVersion = String.valueOf(loadedPlatformCapability.getOrDefault(OS_VERSION, ""));
        String deviceManufacturer = String.valueOf(loadedPlatformCapability.getOrDefault(DEVICE, "")).toUpperCase();
        String platform = String.valueOf(loadedPlatformCapability.getOrDefault(PLATFORM_NAME, "")).toLowerCase();
        String appiumVersion = extractAppiumVersion(loadedPlatformCapability);

        loadedPlatformCapability.remove(APP);
        if (!deviceVersion.isEmpty()) {
            loadedPlatformCapability.put(PLATFORM_VERSION, deviceVersion);
        }

        Map<String, Object> pCloudyOptions = getOrCreatePCloudyOptions(loadedPlatformCapability);
        pCloudyOptions.put("pCloudy_Username", username);
        pCloudyOptions.put("pCloudy_ApiKey", apiKey);
        pCloudyOptions.put("pCloudy_ApplicationName", new File(appPath).getName());
        pCloudyOptions.put("pCloudy_DeviceVersion", deviceVersion);
        pCloudyOptions.put("pCloudy_DeviceManufacturer", deviceManufacturer);
        pCloudyOptions.put(APPIUM_VERSION, appiumVersion);

        ArrayList<Map<String, String>> devices = new ArrayList<>();
        for (int numDevices = 0; numDevices < maxDrivers; numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap<>();
            deviceInfo.put("pCloudy_DeviceManufacturer", deviceManufacturer);
            deviceInfo.put("pCloudy_DeviceVersion", deviceVersion);
            deviceInfo.put("platform", platform);
            devices.add(deviceInfo);
        }
        return new PreparedPCloudyCapabilities(deviceVersion, deviceManufacturer, devices);
    }

    private static String extractAppiumVersion(Map<String, Object> loadedPlatformCapability) {
        Object version = loadedPlatformCapability.containsKey(APPIUM_VERSION)
                ? loadedPlatformCapability.remove(APPIUM_VERSION)
                : loadedPlatformCapability.remove(APPIUM_VERSION_NAMESPACED);
        return String.valueOf(version);
    }

    private static Map<String, Object> getOrCreatePCloudyOptions(Map<String, Object> loadedPlatformCapability) {
        Object existing = loadedPlatformCapability.get(PCLOUDY_OPTIONS);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> options = new HashMap<>();
        loadedPlatformCapability.put(PCLOUDY_OPTIONS, options);
        return options;
    }

    public record PreparedPCloudyCapabilities(String deviceVersion, String deviceManufacturer,
                                              ArrayList<Map<String, String>> devices) {
    }
}
