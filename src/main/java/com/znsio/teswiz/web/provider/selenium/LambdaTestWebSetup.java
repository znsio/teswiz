package com.znsio.teswiz.web.provider.selenium;

import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

import com.znsio.teswiz.runner.Setup;

public final class LambdaTestWebSetup {
    private static final SeleniumWebSessionNameResolver SESSION_NAME_RESOLVER = new SeleniumWebSessionNameResolver();
    private static final SeleniumWebPlatformCapabilityLoader PLATFORM_CAPABILITY_LOADER =
            new SeleniumWebPlatformCapabilityLoader();

    private LambdaTestWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        Map loadedPlatformCapability = PLATFORM_CAPABILITY_LOADER.load();
        return LambdaTestWebCapabilitySetup.updateLambdaTestCapabilities(
                capabilities,
                loadedPlatformCapability,
                Setup.getFromConfigs(Setup.CLOUD_USERNAME),
                Setup.getFromConfigs(Setup.CLOUD_KEY),
                Setup.getFromConfigs(Setup.APP_NAME),
                Setup.getFromConfigs(Setup.LAUNCH_NAME),
                Setup.getFromConfigs(Setup.LOG_DIR),
                SESSION_NAME_RESOLVER.resolve(),
                Setup.getBooleanValueFromConfigs(Setup.CLOUD_USE_LOCAL_TESTING));
    }
}
