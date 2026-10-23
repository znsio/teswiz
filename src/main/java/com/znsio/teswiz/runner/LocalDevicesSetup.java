package com.znsio.teswiz.runner;

import com.znsio.teswiz.mobile.device.LocalMobileDeviceSetup;

@Deprecated(forRemoval = false)
class LocalDevicesSetup {
    private LocalDevicesSetup() {
    }

    static void setupLocalExecution() {
        LocalMobileDeviceSetup.setupLocalExecution();
    }

    static void setupLocalIOSExecution() {
        LocalMobileDeviceSetup.setupLocalIOSExecution();
    }
}
