package com.znsio.teswiz.config.capability;

import java.util.Map;

import com.znsio.teswiz.tools.JsonFile;

public final class CapabilityConfigResolver {
    private CapabilityConfigResolver() {
    }

    public static String getAppPath(String capabilityFile, String platformName, Map<String, Map> loadedCapabilities) {
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{platformName, "app"}, loadedCapabilities);
    }

    public static String getCloudName(boolean runningInCi,
            boolean isApi,
            boolean isCli,
            boolean isPdf,
            String capabilityFile,
            Map<String, Map> loadedCapabilities,
            String notSetValue) {
        if (runningInCi && !isApi && !isCli && !isPdf) {
            return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                    new String[]{"serverConfig", "server", "plugin",
                            "device-farm", "cloud", "cloudName"}, loadedCapabilities);
        }
        return notSetValue;
    }

    public static String getCloudUrl(String capabilityFile, Map<String, Map> loadedCapabilities) {
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "url"}, loadedCapabilities);
    }

    public static String getCloudApiUrl(String capabilityFile, Map<String, Map> loadedCapabilities) {
        return JsonFile.getValueFromLoadedJsonMap(capabilityFile,
                new String[]{"serverConfig", "server", "plugin",
                        "device-farm", "cloud", "apiUrl"}, loadedCapabilities);
    }
}
