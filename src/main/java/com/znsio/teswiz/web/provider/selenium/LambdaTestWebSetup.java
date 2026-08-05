package com.znsio.teswiz.web.provider.selenium;

import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

public final class LambdaTestWebSetup {
    private static final SeleniumWebPlatformCapabilityLoader PLATFORM_CAPABILITY_LOADER =
            new SeleniumWebPlatformCapabilityLoader();
    private static final SeleniumWebExecutionConfigResolver EXECUTION_CONFIG_RESOLVER =
            new SeleniumWebExecutionConfigResolver();

    private LambdaTestWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        Map loadedPlatformCapability = PLATFORM_CAPABILITY_LOADER.load();
        SeleniumWebExecutionConfig executionConfig = EXECUTION_CONFIG_RESOLVER.resolve();
        return LambdaTestWebCapabilitySetup.updateLambdaTestCapabilities(
                capabilities,
                loadedPlatformCapability,
                executionConfig.cloudUser(),
                executionConfig.cloudKey(),
                executionConfig.appName(),
                executionConfig.launchName(),
                executionConfig.logDir(),
                executionConfig.sessionName(),
                executionConfig.cloudUseLocalTesting());
    }
}
