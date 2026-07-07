package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class HeadSpinMobileCapabilitySetupTest {
    @Test
    void shouldPrepareHeadSpinMobileCapabilitiesAndDeviceList() {
        Map<String, Object> platformCapabilities = new HashMap<>();
        platformCapabilities.put("app", "TheApp.apk");
        platformCapabilities.put("platformVersion", "13");
        platformCapabilities.put("device", "samsung");

        HeadSpinMobileCapabilitySetup.PreparedHeadSpinCapabilities prepared =
                HeadSpinMobileCapabilitySetup.prepareCapabilities(
                        platformCapabilities, "android", "headspin-app-1", 2);

        assertThat(platformCapabilities.get("headspin:app.id")).isEqualTo("headspin-app-1");
        assertThat(platformCapabilities).doesNotContainKeys("app", "platformVersion");
        assertThat(prepared.osVersion()).isEqualTo("13");
        assertThat(prepared.deviceManufacturer()).isEqualTo("SAMSUNG");
        assertThat(prepared.devices()).hasSize(2);
        assertThat(prepared.devices().get(0)).containsEntry("platform", "android");
        assertThat(prepared.devices().get(0)).containsEntry("platformVersion", "13");
        assertThat(prepared.devices().get(0)).containsEntry("deviceName", "SAMSUNG");
    }
}
