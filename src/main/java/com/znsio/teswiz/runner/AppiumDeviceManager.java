package com.znsio.teswiz.runner;

import com.znsio.teswiz.mobile.session.AppiumDeviceSessionRegistry;
import com.znsio.teswiz.mobile.session.MobileDriverSession;

/**
 * Device Manager - Handles all device related information's e.g UDID, Model, etc
 */
@Deprecated(forRemoval = false)
public class AppiumDeviceManager {
    public static DriverSession getAppiumDevice() {
        return DriverSession.from(AppiumDeviceSessionRegistry.getCurrentDevice());
    }

    protected static void setDevice(DriverSession device) {
        AppiumDeviceSessionRegistry.setCurrentDevice(device);
    }
}
