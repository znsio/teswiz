package com.znsio.teswiz.web.provider;

import static com.znsio.teswiz.runner.Runner.NOT_SET;

import java.util.LinkedHashMap;
import java.util.Map;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;

public final class WebExecutionProviderConfigResolver {
    private static final String[] CLOUD_NAME_PATH = {
            "serverConfig", "server", "plugin", "device-farm", "cloud", "cloudName"
    };
    private static final String[] CLOUD_URL_PATH = {
            "serverConfig", "server", "plugin", "device-farm", "cloud", "url"
    };
    private static final String[] CLOUD_API_URL_PATH = {
            "serverConfig", "server", "plugin", "device-farm", "cloud", "apiUrl"
    };

    private final WebExecutionProviderResolver providerResolver;

    public WebExecutionProviderConfigResolver() {
        this(new WebExecutionProviderResolver());
    }

    WebExecutionProviderConfigResolver(WebExecutionProviderResolver providerResolver) {
        this.providerResolver = providerResolver;
    }

    public WebExecutionProviderConfig resolve() {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilities = Setup.getLoadedCapabilities();
        if (null == capabilityFile || capabilityFile.isBlank() || NOT_SET.equalsIgnoreCase(capabilityFile)
                || null == loadedCapabilities || loadedCapabilities.isEmpty()) {
            return WebExecutionProviderConfig.local();
        }

        String providerName = resolveProviderName(capabilityFile, loadedCapabilities);
        if ("local".equalsIgnoreCase(providerName)) {
            return WebExecutionProviderConfig.local();
        }

        String remoteUrl = readValue(capabilityFile, loadedCapabilities, CLOUD_URL_PATH);
        String apiUrl = readValue(capabilityFile, loadedCapabilities, CLOUD_API_URL_PATH);
        String username = normalize(Setup.getFromConfigs(Setup.CLOUD_USERNAME));
        String accessKey = normalize(Setup.getFromConfigs(Setup.CLOUD_KEY));
        Map<String, Object> webCapabilities = extractWebCapabilities(loadedCapabilities);

        return new WebExecutionProviderConfig(providerName, remoteUrl, apiUrl, username, accessKey, webCapabilities);
    }

    private String resolveProviderName(String capabilityFile, Map<String, Map> loadedCapabilities) {
        String cloudName = readValue(capabilityFile, loadedCapabilities, CLOUD_NAME_PATH);
        return providerResolver.resolve(cloudName).name();
    }

    private String readValue(String capabilityFile, Map<String, Map> loadedCapabilities, String[] path) {
        try {
            return normalize(JsonFile.getValueFromLoadedJsonMap(capabilityFile, path, loadedCapabilities));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        if (null == value || value.isBlank() || NOT_SET.equalsIgnoreCase(value) || "docker".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private Map<String, Object> extractWebCapabilities(Map<String, Map> loadedCapabilities) {
        Object webNode = loadedCapabilities.get("web");
        if (!(webNode instanceof Map<?, ?> webCapabilities)) {
            return Map.of();
        }
        Map<String, Object> normalizedCapabilities = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : webCapabilities.entrySet()) {
            if (entry.getKey() instanceof String key) {
                normalizedCapabilities.put(key, entry.getValue());
            }
        }
        return normalizedCapabilities;
    }
}
