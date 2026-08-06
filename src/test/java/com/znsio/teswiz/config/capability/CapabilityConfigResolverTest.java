package com.znsio.teswiz.config.capability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CapabilityConfigResolverTest {
    @Test
    void shouldResolveAppPathForPlatform() {
        Map<String, Map> loadedCapabilities = sampleLoadedCapabilities();

        String appPath = CapabilityConfigResolver.getAppPath("caps/sample.json", "android", loadedCapabilities);

        assertThat(appPath).isEqualTo("apps/sample.apk");
    }

    @Test
    void shouldResolveCloudNameOnlyForSupportedExecutionModes() {
        Map<String, Map> loadedCapabilities = sampleLoadedCapabilities();

        String cloudName = CapabilityConfigResolver.getCloudName(true, false, false, false,
                "caps/sample.json", loadedCapabilities, "NOT_SET");
        String notSetCloudName = CapabilityConfigResolver.getCloudName(false, false, false, false,
                "caps/sample.json", loadedCapabilities, "NOT_SET");

        assertThat(cloudName).isEqualTo("browserstack");
        assertThat(notSetCloudName).isEqualTo("NOT_SET");
    }

    @Test
    void shouldResolveCloudUrls() {
        Map<String, Map> loadedCapabilities = sampleLoadedCapabilities();

        String cloudUrl = CapabilityConfigResolver.getCloudUrl("caps/sample.json", loadedCapabilities);
        String cloudApiUrl = CapabilityConfigResolver.getCloudApiUrl("caps/sample.json", loadedCapabilities);

        assertThat(cloudUrl).isEqualTo("https://hub-cloud.browserstack.com/wd/hub");
        assertThat(cloudApiUrl).isEqualTo("https://api-cloud.browserstack.com/app-automate/");
    }

    private Map<String, Map> sampleLoadedCapabilities() {
        Map<String, Map> loadedCapabilities = new LinkedHashMap<>();
        Map<String, Object> androidCapabilities = new LinkedHashMap<>();
        androidCapabilities.put("app", "apps/sample.apk");
        loadedCapabilities.put("android", androidCapabilities);

        Map<String, Object> cloud = new LinkedHashMap<>();
        cloud.put("cloudName", "browserstack");
        cloud.put("url", "https://hub-cloud.browserstack.com/wd/hub");
        cloud.put("apiUrl", "https://api-cloud.browserstack.com/app-automate/");
        cloud.put("devices", new ArrayList<>());

        Map<String, Object> deviceFarm = new LinkedHashMap<>();
        deviceFarm.put("cloud", cloud);
        Map<String, Object> plugin = new LinkedHashMap<>();
        plugin.put("device-farm", deviceFarm);
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("plugin", plugin);
        Map<String, Object> serverConfig = new LinkedHashMap<>();
        serverConfig.put("server", server);
        loadedCapabilities.put("serverConfig", serverConfig);
        return loadedCapabilities;
    }
}
