package com.znsio.teswiz.web.provider.selenium;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

public final class BrowserStackWebSetup {
    private static final SeleniumWebSessionNameResolver SESSION_NAME_RESOLVER = new SeleniumWebSessionNameResolver();

    private BrowserStackWebSetup() {
    }

    public static MutableCapabilities updateCapabilities(MutableCapabilities capabilities) {
        String capabilityFile = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        Map<String, Object> loadedPlatformCapability = loadedCapabilityFile.get(Platform.web.name());
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
