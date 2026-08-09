package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BrowserStackMobileCapabilitySetupTest {
    @Test
    void shouldPrepareBrowserStackMobileCapabilitiesAndMigrateLegacyOptions() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("browserstack.networkLogs", "false");
        platformCapabilities.put("browserstack.geoLocation", "IN");
        platformCapabilities.put("browserstack.locale", "en_US");
        platformCapabilities.put("device", "Galaxy S23");

        BrowserStackMobileCapabilitySetup.prepareCapabilities(
                platformCapabilities,
                "bs-user",
                "bs-key",
                "teswiz",
                "nightly-run",
                "./target/Jul",
                "scenario-1",
                "2.0.1",
                true,
                "local-123");

        Map<String, Object> bstackOptions = (Map<String, Object>) platformCapabilities.get("bstack:options");
        assertThat(bstackOptions.get("userName")).isEqualTo("bs-user");
        assertThat(bstackOptions.get("accessKey")).isEqualTo("bs-key");
        assertThat(bstackOptions.get("appiumVersion")).isEqualTo("2.0.1");
        assertThat(bstackOptions.get("projectName")).isEqualTo("teswiz");
        assertThat(bstackOptions.get("buildName")).isEqualTo("nightly-run-.targetJul");
        assertThat(bstackOptions.get("sessionName")).isEqualTo("scenario-1");
        assertThat(bstackOptions.get("debug")).isEqualTo("true");
        assertThat(bstackOptions.get("networkLogs")).isEqualTo("true");
        assertThat(bstackOptions.get("appProfiling")).isEqualTo("true");
        assertThat(bstackOptions.get("local")).isEqualTo("true");
        assertThat(bstackOptions.get("localIdentifier")).isEqualTo("local-123");
        assertThat(bstackOptions.get("geoLocation")).isEqualTo("IN");
        assertThat(platformCapabilities).doesNotContainKeys(
                "browserstack.networkLogs", "browserstack.geoLocation", "browserstack.locale");
    }
}
