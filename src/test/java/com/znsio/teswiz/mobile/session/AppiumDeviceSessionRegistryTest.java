package com.znsio.teswiz.mobile.session;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class AppiumDeviceSessionRegistryTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        AppiumDeviceSessionRegistry.clear();
    }

    @Test
    void shouldStoreAndReturnCurrentMobileDriverSessionForTheThread() throws Exception {
        MobileDriverSession session = objectMapper.readValue("""
                {
                  "platformName": "android",
                  "udid": "emulator-5554",
                  "deviceName": "Pixel 8"
                }
                """, MobileDriverSession.class);

        AppiumDeviceSessionRegistry.setCurrentDevice(session);

        assertThat(AppiumDeviceSessionRegistry.getCurrentDevice()).isSameAs(session);
        assertThat(AppiumDeviceSessionRegistry.getCurrentDevice().getPlatformName()).isEqualTo("android");
        assertThat(AppiumDeviceSessionRegistry.getCurrentDevice().getUdid()).isEqualTo("emulator-5554");
        assertThat(AppiumDeviceSessionRegistry.getCurrentDevice().getDeviceName()).isEqualTo("Pixel 8");
    }
}
