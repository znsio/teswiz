package com.znsio.teswiz.runner.atd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Device Manager - Handles all device related information's e.g UDID, Model, etc
 */
public class ATD_AppiumDeviceManager {
    private static final Logger LOGGER = LogManager.getLogger(ATD_AppiumDeviceManager.class.getName());
    private static final ThreadLocal<DriverSession> appiumDevice = new ThreadLocal<>();


    public static DriverSession getAppiumDevice() {
        return appiumDevice.get();
    }

    protected static void setDevice(DriverSession device) {
        appiumDevice.set(device);
    }
}
