package com.znsio.teswiz.mobile.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class HeadSpinMobileCapabilitySetup {
    private static final String PLATFORM_VERSION = "platformVersion";

    private HeadSpinMobileCapabilitySetup() {
    }

    public static PreparedHeadSpinCapabilities prepareCapabilities(Map<String, Object> loadedPlatformCapability,
            String platformName, String appId, int maxDrivers) {
        loadedPlatformCapability.put("headspin:app.id", appId);
        loadedPlatformCapability.remove("app");

        String osVersion = String.valueOf(loadedPlatformCapability.getOrDefault(PLATFORM_VERSION, ""));
        String deviceManufacturer = loadedPlatformCapability.getOrDefault("device", "").toString().toUpperCase();
        loadedPlatformCapability.remove(PLATFORM_VERSION);

        ArrayList<Map<String, String>> devices = new ArrayList<>();
        for (int numDevices = 0; numDevices < maxDrivers; numDevices++) {
            HashMap<String, String> deviceInfo = new HashMap<>();
            deviceInfo.put("platform", platformName.toLowerCase());
            deviceInfo.put("platformVersion", osVersion);
            deviceInfo.put("deviceName", deviceManufacturer);
            devices.add(deviceInfo);
        }
        return new PreparedHeadSpinCapabilities(osVersion, deviceManufacturer, devices);
    }

    public record PreparedHeadSpinCapabilities(String osVersion, String deviceManufacturer,
                                               ArrayList<Map<String, String>> devices) {
    }
}
