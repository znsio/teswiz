package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.znsio.teswiz.mobile.session.AppiumDeviceSessionRegistry;

class AppiumDeviceManagerCompatibilityTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        AppiumDeviceSessionRegistry.clear();
    }

    @Test
    void shouldDelegateLegacyAppiumDeviceManagerCallsToTheSharedMobileSessionRegistry() throws Exception {
        DriverSession session = objectMapper.readValue("""
                {
                  "platformName": "android",
                  "udid": "emulator-5554",
                  "deviceName": "Pixel 8"
                }
                """, DriverSession.class);

        AppiumDeviceManager.setDevice(session);

        assertThat(AppiumDeviceManager.getAppiumDevice()).isSameAs(session);
        assertThat(AppiumDeviceManager.getAppiumDevice().getDeviceName()).isEqualTo("Pixel 8");
        assertThat(AppiumDeviceSessionRegistry.getCurrentDevice()).isSameAs(session);
    }

    @Test
    void shouldAdaptNewMobileSessionInstancesBackToLegacyDriverSessionView() throws Exception {
        var session = objectMapper.readValue("""
                {
                  "platformName": "android",
                  "udid": "emulator-5554",
                  "deviceName": "Pixel 8"
                }
                """, com.znsio.teswiz.mobile.session.MobileDriverSession.class);

        AppiumDeviceSessionRegistry.setCurrentDevice(session);

        assertThat(AppiumDeviceManager.getAppiumDevice()).isNotNull();
        assertThat(AppiumDeviceManager.getAppiumDevice().getPlatformName()).isEqualTo("android");
        assertThat(AppiumDeviceManager.getAppiumDevice().getUdid()).isEqualTo("emulator-5554");
        assertThat(AppiumDeviceManager.getAppiumDevice().getDeviceName()).isEqualTo("Pixel 8");
    }
}
