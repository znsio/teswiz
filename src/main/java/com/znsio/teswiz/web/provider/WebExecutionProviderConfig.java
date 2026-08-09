package com.znsio.teswiz.web.provider;

import java.util.LinkedHashMap;
import java.util.Map;

public record WebExecutionProviderConfig(
        String providerName,
        String remoteUrl,
        String apiUrl,
        String username,
        String accessKey,
        Map<String, Object> webCapabilities) {

    public WebExecutionProviderConfig {
        webCapabilities = null == webCapabilities ? Map.of() : Map.copyOf(webCapabilities);
    }

    public boolean isRemote() {
        return null != remoteUrl && !remoteUrl.isBlank();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("providerName", providerName);
        if (isRemote()) {
            config.put("remoteUrl", remoteUrl);
        }
        if (null != apiUrl && !apiUrl.isBlank()) {
            config.put("apiUrl", apiUrl);
        }
        if (null != username && !username.isBlank()) {
            config.put("username", username);
        }
        if (null != accessKey && !accessKey.isBlank()) {
            config.put("accessKey", accessKey);
        }
        if (!webCapabilities.isEmpty()) {
            config.put("webCapabilities", webCapabilities);
        }
        return config;
    }

    public static WebExecutionProviderConfig local() {
        return new WebExecutionProviderConfig("local", null, null, null, null, Map.of());
    }
}
