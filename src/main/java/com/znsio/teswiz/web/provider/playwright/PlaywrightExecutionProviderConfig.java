package com.znsio.teswiz.web.provider.playwright;

import java.util.LinkedHashMap;
import java.util.Map;

public record PlaywrightExecutionProviderConfig(
        String providerName,
        String remoteUrl,
        String apiUrl,
        String username,
        String accessKey) {

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
        return config;
    }

    public static PlaywrightExecutionProviderConfig local() {
        return new PlaywrightExecutionProviderConfig("local", null, null, null, null);
    }
}
