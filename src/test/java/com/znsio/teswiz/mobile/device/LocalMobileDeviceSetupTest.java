package com.znsio.teswiz.mobile.device;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.exceptions.EnvironmentSetupException;

class LocalMobileDeviceSetupTest {
    @Test
    void shouldFailWhenNoAndroidDevicesAreAvailable() {
        assertThatThrownBy(() -> LocalMobileDeviceSetup.validateAndroidParallelAvailability(0, 1))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessage("No devices available to run the tests");
    }

    @Test
    void shouldFailWhenAndroidDevicesAreFewerThanRequestedParallelCount() {
        assertThatThrownBy(() -> LocalMobileDeviceSetup.validateAndroidParallelAvailability(1, 2))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessageContaining("Fewer devices (1) available to run the tests in parallel");
    }

    @Test
    void shouldFailWhenNoIosDevicesOrSimulatorsAreAvailable() {
        assertThatThrownBy(() -> LocalMobileDeviceSetup.validateIosParallelAvailability(0, 0, 1))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessage("No devices available to run the tests");
    }

    @Test
    void shouldFailWhenBootedIosSimulatorsAreFewerThanRequestedParallelCount() {
        assertThatThrownBy(() -> LocalMobileDeviceSetup.validateIosParallelAvailability(2, 1, 2))
                .isInstanceOf(EnvironmentSetupException.class)
                .hasMessageContaining("Fewer devices (1) available to run the tests in parallel");
    }
}
