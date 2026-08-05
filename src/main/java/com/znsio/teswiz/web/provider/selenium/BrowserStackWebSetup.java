package com.znsio.teswiz.web.provider.selenium;

import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

import com.znsio.teswiz.runner.Setup;

public final class BrowserStackWebSetup {
    private static final SeleniumWebSessionNameResolver SESSION_NAME_RESOLVER = new SeleniumWebSessionNameResolver();
    private static final SeleniumWebPlatformCapabilityLoader PLATFORM_CAPABILITY_LOADER =
            new SeleniumWebPlatformCapabilityLoader();

    private BrowserStackWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        Map<String, Object> loadedPlatformCapability = PLATFORM_CAPABILITY_LOADER.load();
        return BrowserStackWebCapabilitySetup.updateBrowserStackCapabilities(
                capabilities,
                loadedPlatformCapability,
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                SESSION_NAME_RESOLVER.resolve(),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_PROXY),
                Setup.getFromConfigs(Setup.PROXY_URL),
                Setup.getFromConfigs(Setup.CLOUD_KEY));
    }
}
