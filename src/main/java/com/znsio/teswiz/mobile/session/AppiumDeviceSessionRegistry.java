package com.znsio.teswiz.mobile.session;

public final class AppiumDeviceSessionRegistry {
    private static final ThreadLocal<MobileDriverSession> CURRENT_DEVICE = new ThreadLocal<>();

    private AppiumDeviceSessionRegistry() {
    }

    public static MobileDriverSession getCurrentDevice() {
        return CURRENT_DEVICE.get();
    }

    public static void setCurrentDevice(MobileDriverSession device) {
        CURRENT_DEVICE.set(device);
    }

    public static void clear() {
        CURRENT_DEVICE.remove();
    }
}
