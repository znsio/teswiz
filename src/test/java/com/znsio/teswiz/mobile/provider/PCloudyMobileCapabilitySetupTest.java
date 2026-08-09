package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PCloudyMobileCapabilitySetupTest {
    @Test
    void shouldPreparePCloudyCapabilitiesAndGeneratedDeviceList() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("platformName", "Android");
        platformCapabilities.put("device", "samsung");
        platformCapabilities.put("os_version", "13");
        platformCapabilities.put("appium:appiumVersion", "2.1.0");
        platformCapabilities.put("app", "storage:filename=TheApp.apk");
        platformCapabilities.put("pcloudy:options", new HashMap<>());

        PCloudyMobileCapabilitySetup.PreparedPCloudyCapabilities prepared =
                PCloudyMobileCapabilitySetup.prepareCapabilities(
                        platformCapabilities,
                        "pc-user",
                        "pc-key",
                        "apps/TheApp.apk",
                        2);

        assertThat(platformCapabilities.get("platformVersion")).isEqualTo("13");
        assertThat(platformCapabilities).doesNotContainKeys("app", "appium:appiumVersion");

        Map<String, Object> pCloudyOptions = (Map<String, Object>) platformCapabilities.get("pcloudy:options");
        assertThat(pCloudyOptions.get("pCloudy_Username")).isEqualTo("pc-user");
        assertThat(pCloudyOptions.get("pCloudy_ApiKey")).isEqualTo("pc-key");
        assertThat(pCloudyOptions.get("pCloudy_ApplicationName")).isEqualTo("TheApp.apk");
        assertThat(pCloudyOptions.get("pCloudy_DeviceVersion")).isEqualTo("13");
        assertThat(pCloudyOptions.get("pCloudy_DeviceManufacturer")).isEqualTo("SAMSUNG");
        assertThat(pCloudyOptions.get("appiumVersion")).isEqualTo("2.1.0");

        assertThat(prepared.deviceVersion()).isEqualTo("13");
        assertThat(prepared.deviceManufacturer()).isEqualTo("SAMSUNG");
        assertThat(prepared.devices()).hasSize(2);
        assertThat(prepared.devices().get(0)).containsEntry("pCloudy_DeviceManufacturer", "SAMSUNG");
        assertThat(prepared.devices().get(0)).containsEntry("pCloudy_DeviceVersion", "13");
        assertThat(prepared.devices().get(0)).containsEntry("platform", "android");
    }

    @Test
    void shouldSupportLegacyAppiumVersionKey() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("platformName", "iOS");
        platformCapabilities.put("device", "apple");
        platformCapabilities.put("os_version", "17");
        platformCapabilities.put("appiumVersion", "2.5.1");
        platformCapabilities.put("pcloudy:options", new HashMap<>());

        PCloudyMobileCapabilitySetup.prepareCapabilities(
                platformCapabilities,
                "pc-user",
                "pc-key",
                "apps/TheApp.ipa",
                1);

        assertThat(platformCapabilities).doesNotContainKey("appiumVersion");
        Map<String, Object> pCloudyOptions = (Map<String, Object>) platformCapabilities.get("pcloudy:options");
        assertThat(pCloudyOptions.get("appiumVersion")).isEqualTo("2.5.1");
        assertThat(pCloudyOptions.get("pCloudy_ApplicationName")).isEqualTo("TheApp.ipa");
    }
}
