package com.znsio.teswiz.web.provider.selenium;

import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

public final class BrowserStackWebSetup {
    private static final SeleniumWebPlatformCapabilityLoader PLATFORM_CAPABILITY_LOADER =
            new SeleniumWebPlatformCapabilityLoader();
    private static final SeleniumWebExecutionConfigResolver EXECUTION_CONFIG_RESOLVER =
            new SeleniumWebExecutionConfigResolver();

    private BrowserStackWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        Map<String, Object> loadedPlatformCapability = PLATFORM_CAPABILITY_LOADER.load();
        SeleniumWebExecutionConfig executionConfig = EXECUTION_CONFIG_RESOLVER.resolve();
        return BrowserStackWebCapabilitySetup.updateBrowserStackCapabilities(
                capabilities,
                loadedPlatformCapability,
                executionConfig.appName(),
                executionConfig.launchName(),
                executionConfig.logDir(),
                executionConfig.sessionName(),
                executionConfig.cloudUseLocalTesting(),
                executionConfig.cloudUseProxy(),
                executionConfig.proxyUrl(),
                executionConfig.cloudKey());
    }
}
